/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.user.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import com.wso2telco.config.ConfigLoader;
import com.wso2telco.config.DataHolder;
import com.wso2telco.config.Scope;
import com.wso2telco.config.ScopeConfigs;
import com.wso2telco.util.ClaimUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimInfoMultipleScopeResponseBuilder.
 */
public class ClaimInfoMultipleScopeResponseBuilder implements UserInfoResponseBuilder {
    
    /** The log. */
    private static Log log = LogFactory.getLog(ClaimInfoMultipleScopeResponseBuilder.class);
    
    /** The openid scopes. */
    List<String> openidScopes = Arrays.asList("profile", "email", "address", "phone", "openid");

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder#getResponseString(org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO)
     */
    public String getResponseString(OAuth2TokenValidationResponseDTO tokenResponse) throws UserInfoEndpointException, OAuthSystemException {

        Map<ClaimMapping, String> userAttributes = getUserAttributesFromCache(tokenResponse);
        Map<String, Object> claims = null;
        //read claimValues per scope from scope-config.xml
        DataHolder.getInstance().setScopeConfigs(ConfigLoader.getInstance().getScopeConfigs());
        ScopeConfigs scopeConfigs = DataHolder.getInstance().getScopeConfigs();

        if (userAttributes != null) {
            UserInfoClaimRetriever retriever = UserInfoEndpointConfig.getInstance().getUserInfoClaimRetriever();
            claims = retriever.getClaimsMap(userAttributes);

            boolean hasAcr = claims.containsKey("acr");
            boolean hasAmr = claims.containsKey("amr");
            if (hasAcr && hasAmr && userAttributes.size() == 2) {
                //userattributes only contains only acr and amr values
                userAttributes = null;

            }
        }
        // Commenting out this since a null entry resides inside cache
//        if (userAttributes == null || userAttributes.isEmpty()) {
//            if (log.isDebugEnabled()) {
//                log.debug("User attributes not found in cache. Trying to retrieve from user store.");
//            }
            try {
                claims = ClaimUtil.getClaimsFromUserStore(tokenResponse);
            } catch (Exception e) {
                throw new UserInfoEndpointException("Error while retrieving claims from user store.");
            }
//        }

        String contextPath = System.getProperty("request.context.path");
        String[] requestedScopes = tokenResponse.getScope();

        if (contextPath.equals("/oauth2")) {
            //oauth2/userinfo requests should serve scopes in openid connect onl
            requestedScopes = getValidScopes(requestedScopes);
        }
        Map<String, Object> requestedClaims = getRequestedClaims(requestedScopes, scopeConfigs, claims);

        try {
            return JSONUtils.buildJSON(requestedClaims);

        } catch (JSONException e) {
            throw new UserInfoEndpointException("Error while generating the response JSON");
        }
    }

    /**
     * Gets the user attributes from cache.
     *
     * @param tokenResponse the token response
     * @return the user attributes from cache
     */
    private Map<ClaimMapping, String> getUserAttributesFromCache(OAuth2TokenValidationResponseDTO tokenResponse) {
        AuthorizationGrantCacheKey cacheKey = new AuthorizationGrantCacheKey(tokenResponse
                .getAuthorizationContextToken().getTokenString());
        AuthorizationGrantCacheEntry cacheEntry = (AuthorizationGrantCacheEntry) AuthorizationGrantCache.getInstance().getValueFromCache(cacheKey);
        if (cacheEntry == null) {
            return new HashMap<ClaimMapping, String>();
        }
        return cacheEntry.getUserAttributes();
    }

    /**
     * Gets the requested claims.
     *
     * @param scopes the scopes
     * @param scopeConfigs the scope configs
     * @param totalClaims the total claims
     * @return the requested claims
     */
    private Map<String, Object> getRequestedClaims(String[] scopes, ScopeConfigs scopeConfigs, Map<String, Object> totalClaims) {
        Map<String, Object> requestedClaims = new HashMap<String, Object>();
        String[] attributes;
        if (scopeConfigs != null) {
            for (String scopeName : scopes) {
            	log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + scopeName);
                for (Scope scope : scopeConfigs.getScopes().getScopeList()) {
                	log.info("<<<<<<< " + scope.getName());
                    if (scopeName.equals(scope.getName())) {
                        attributes = new String[scope.getClaims().getClaimValues().size()];
                        requestedClaims = addClaims(totalClaims, requestedClaims, scope.getClaims().getClaimValues().toArray(attributes));
                        log.info("added claim ");
                        break;
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
            }
        }
        return requestedClaims;
    }


    /**
     * Adds the claims.
     *
     * @param claims the claims
     * @param requestedClaims the requested claims
     * @param attributeList the attribute list
     * @return the map
     */
    private Map<String, Object> addClaims(Map<String, Object> claims, Map<String, Object> requestedClaims, String[] attributeList) {
        int attributeIndex = 0;
        while (attributeIndex < attributeList.length) {
            if (claims.get(attributeList[attributeIndex]) != null) {
                requestedClaims.put(attributeList[attributeIndex], claims.get(attributeList[attributeIndex]));
            }
            attributeIndex++;
        }
        return requestedClaims;
    }

    /**
     * Gets the valid scopes.
     *
     * @param requestedScopes the requested scopes
     * @return the valid scopes
     */
    private String[] getValidScopes(String[] requestedScopes) {
        List<String> validScopes = new ArrayList<String>();
        for (String scope : requestedScopes) {
            if (!openidScopes.contains(scope)) {
                continue;
            }
            validScopes.add(scope);
        }
        return validScopes.toArray(new String[validScopes.size()]);
    }
}
