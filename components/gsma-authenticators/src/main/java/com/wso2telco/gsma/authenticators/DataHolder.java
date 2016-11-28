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
package com.wso2telco.gsma.authenticators;

import com.wso2telco.gsma.authenticators.abcd.Authentication;
import com.wso2telco.gsma.authenticators.abcd.AuthenticationLevels;
import com.wso2telco.gsma.authenticators.config.MobileConnectConfig;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DataHolder.
 */
public class DataHolder {

    /** The loa config. */
    private AuthenticationLevels authenticationLevels;
    
    /** The mobile connect config. */
    private MobileConnectConfig mobileConnectConfig;

    private Map<String, Authentication> authenticationLevelMap = null;

    /** The this instance. */
    private static DataHolder thisInstance = new DataHolder();

    /**
     * Instantiates a new data holder.
     */
    private DataHolder() {}

    /**
     * Gets the single instance of DataHolder.
     *
     * @return single instance of DataHolder
     */
    public static DataHolder getInstance() {
        return thisInstance;
    }

    /**
     * Sets the LOA config.
     *
     * @param config the new LOA config
     */
    public void setAuthenticationLevels(AuthenticationLevels authenticationLevels) {
        this.authenticationLevels = authenticationLevels;
    }

    /**
     * Gets the LOA config.
     *
     * @return the LOA config
     */
    public AuthenticationLevels getAuthenticationLevels() {
        return authenticationLevels;
    }

    /**
     * Gets the mobile connect config.
     *
     * @return the mobile connect config
     */
    public MobileConnectConfig getMobileConnectConfig() {
        return mobileConnectConfig;
    }

    /**
     * Sets the mobile connect config.
     *
     * @param mobileConnectConfig the new mobile connect config
     */
    public void setMobileConnectConfig(MobileConnectConfig mobileConnectConfig) {
        this.mobileConnectConfig = mobileConnectConfig;
    }

    public Map<String, Authentication> getAuthenticationLevelMap() {
        return authenticationLevelMap;
    }

    public void setAuthenticationLevelMap(
            Map<String, Authentication> authenticationLevelMap) {
        this.authenticationLevelMap = authenticationLevelMap;
    }
}
