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
package com.wso2telco.gsma.authenticators.internal;

import com.wso2telco.gsma.authenticators.abcd.Authentication;
import com.wso2telco.gsma.authenticators.abcd.AuthenticationLevel;
import com.wso2telco.gsma.authenticators.abcd.AuthenticationLevels;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.user.core.service.RealmService;

import com.wso2telco.gsma.authenticators.*;
import com.wso2telco.gsma.authenticators.config.ConfigLoader;
import com.wso2telco.gsma.authenticators.headerenrich.HeaderEnrichmentAuthenticator;
import com.wso2telco.gsma.authenticators.sms.SMSAuthenticator;
import com.wso2telco.gsma.authenticators.ussd.USSDAuthenticator;
import com.wso2telco.gsma.authenticators.ussd.USSDPinAuthenticator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomAuthenticatorServiceComponent.
 */
// 
@Component(name = "com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent")
@Reference(
        name = "user.realmservice.default",
        referenceInterface = org.wso2.carbon.user.core.service.RealmService.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setRealmService",
        unbind = "unsetRealmService"
)
public class CustomAuthenticatorServiceComponent {

    /** The log. */
    private static Log log = LogFactory.getLog(CustomAuthenticatorServiceComponent.class);

    /** The realm service. */
    private static RealmService realmService;

    /**
     * Activate.
     *
     * @param ctxt the ctxt
     */
    @Activate
    protected void activate(ComponentContext ctxt) {

         
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new PinAuthenticator(), null);

        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new HeaderEnrichmentAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new LOACompositeAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new OpCoCompositeAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MSISDNAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new GSMAMSISDNAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new USSDAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new USSDPinAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new SMSAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MSSAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MSSPinAuthenticator(), null);
        
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new SelfAuthenticator(), null);


        AuthenticationLevels config = ConfigLoader.getInstance().getAuthenticationLevels();

        DataHolder.getInstance().setAuthenticationLevels(config);

        DataHolder.getInstance().setMobileConnectConfig(ConfigLoader.getInstance().getMobileConnectConfig());
        Map<String, Authentication> authenticationMap = loadAuthenticationMap(config);
        DataHolder.getInstance().setAuthenticationLevelMap(authenticationMap);

        if (log.isDebugEnabled()) {
            log.debug("Custom Application Authenticator bundle is activated");
        }
    }

    /**
     * Deactivate.
     *
     * @param ctxt the ctxt
     */
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Custom Application Authenticator bundle is deactivated");
        }
    }

    /**
     * Sets the realm service.
     *
     * @param realmService the new realm service
     */
    protected void setRealmService(RealmService realmService) {
    	if (log.isDebugEnabled()) {
        log.debug("Setting the Realm Service");
    	}
        CustomAuthenticatorServiceComponent.realmService = realmService;
    }

    /**
     * Unset realm service.
     *
     * @param realmService the realm service
     */
    protected void unsetRealmService(RealmService realmService) {
    	if (log.isDebugEnabled()) {
        log.debug("UnSetting the Realm Service");
    	}
        CustomAuthenticatorServiceComponent.realmService = null;
    }

    /**
     * Gets the realm service.
     *
     * @return the realm service
     */
    public static RealmService getRealmService() {
        return realmService;
    }

    private Map<String, Authentication> loadAuthenticationMap(AuthenticationLevels authenticationLevels) {
        Map<String, Authentication> authenticationMap = new HashMap<>();
        List<AuthenticationLevel> authenticationLevelsList = authenticationLevels.getAuthenticationLevelList();
        for (AuthenticationLevel authenticationLevel: authenticationLevelsList) {
            Authentication authentication = authenticationLevel.getAuthentication();
            String authenticationLevelValue = authenticationLevel.getLevel();
            authenticationMap.put(authenticationLevelValue, authentication);
        }
        return authenticationMap;
    }

}
