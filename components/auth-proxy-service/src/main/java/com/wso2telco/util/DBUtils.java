/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.util;


import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used to read operator properties.
 */
public class DBUtils {
    private static DataSource dataSource = null;

    private static void initializeDatasource() throws NamingException {
        if (dataSource != null) {
            return;
        }

        String dataSourceName = null;
        MobileConnectConfig mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
        try {
            Context ctx = new InitialContext();
            dataSourceName = mobileConnectConfigs.getAuthProxy().getDataSourceName();
            if (dataSourceName != null) {
                dataSource = (DataSource) ctx.lookup(dataSourceName);
            } else {
                throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } catch (NamingException e) {
            throw new NamingException("Exception occurred while initiating data source : " + dataSourceName);
        }
    }

    private static Connection getConnection() throws SQLException, NamingException {
        initializeDatasource();
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Sessions Datasource not initialized properly");
    }

    /**
     * Get Operator Property by operator name and property key.
     * @param operatorName Operator Name.
     * @param propertyKey operator property key.
     * @return operator property value.
     * @throws SQLException
     * @throws NamingException
     */
    public static String getOperatorProperty(String operatorName, String propertyKey) throws SQLException,
                                                                                          NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String propertyValue = null;
        String queryToGetOperatorProperty = "SELECT propertyValue FROM operators_properties prop LEFT JOIN operators" +
                " op ON op.ID=prop.operatorId AND LOWER(op.operatorName)=? AND prop.propertyKey=?";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setString(1, operatorName.toLowerCase());
            preparedStatement.setString(2, propertyKey);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                propertyValue = resultSet.getString(AuthProxyConstants.PROPERTY_VALUE);
            }

        } catch (SQLException e) {
            throw new SQLException("Error occurred while retrieving operator property : " + propertyKey + " of " +
                                           "operator : " + operatorName, e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        }
        finally {
            preparedStatement.close();
            resultSet.close();
            connection.close();
        }
        return propertyValue;
    }
}