/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.common.security;

import org.collectionspace.authentication.AuthN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * createPasswordHash creates password has using configured digest algorithm
     * and encoding
     * @param user
     * @param password in cleartext
     * @return hashed password
     */
    public static String createPasswordHash(String username, String password) {
        //TODO: externalize digest algo and encoding
        return org.jboss.security.Util.createPasswordHash("SHA-256", //digest algo
                "base64", //encoding
                null, //charset
                username,
                password);
    }

    /**
     * validatePassword validates password per configured password policy
     * @param password
     */
    public static void validatePassword(String password) {
        //TODO: externalize password length
        if (password == null) {
            String msg = "Password missing ";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (password.length() < 8 || password.length() > 24) {
            String msg = "Password length should be >8 and <24";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * isCSpaceAdmin check if authenticated user is a CSpace administrator
     * @param tenantId
     * @return
     */
    public static boolean isCSpaceAdmin() {
        String tenantId = AuthN.get().getCurrentTenantId();
        if (tenantId != null) {
            if (!"0".equals(tenantId)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
