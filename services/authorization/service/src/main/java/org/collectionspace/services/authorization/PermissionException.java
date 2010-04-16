/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.authorization;

/**
 *
 * @author 
 */
public class PermissionException extends Exception {

    /**
     * Creates a new instance of <code>PermissionException</code> without detail message.
     */
    public PermissionException() {
    }

    /**
     * Constructs an instance of <code>PermissionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PermissionException(String msg) {
        super(msg);
    }

    public PermissionException(String msg, Throwable cause) {
        super(msg, cause);
    }


    public PermissionException(Throwable cause) {
        super(cause);
    }
}
