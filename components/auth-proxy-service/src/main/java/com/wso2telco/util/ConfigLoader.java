package com.wso2telco.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * ConfigLoader class has implemented with SIngleton patten. This file is used to load mobile-connect.xml file.
 */
public class ConfigLoader {
    private Log log = LogFactory.getLog(ConfigLoader.class);

    private MobileConnectConfig mobileConnectConfig;
    private static ConfigLoader loader;

    private ConfigLoader() {
        try {
            this.mobileConnectConfig = initMConnectConfig();
        } catch (JAXBException e) {
            log.error("Error occurred while initiating mobile-connector config file", e);
        }
    }

    /**
     * Get instance of the ConfigLoader class.
     * @return
     */
    public static ConfigLoader getInstance() {
        if(loader == null) {
           loader = new ConfigLoader();
        }
        return loader;
    }

    /**
     * Initiate mobile-connect.xml file.
     * @return the configuration of mobile-connect.xml file.
     * @throws JAXBException
     */
    private MobileConnectConfig initMConnectConfig() throws JAXBException {
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "mobile-connect.xml";
        File file = new File(configPath);
        JAXBContext jaxbContext = JAXBContext.newInstance(MobileConnectConfig.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MobileConnectConfig) unmarshaller.unmarshal(file);
    }

    public MobileConnectConfig getMobileConnectConfig(){
        return mobileConnectConfig;
    }
}
