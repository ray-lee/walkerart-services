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
package org.collectionspace.services.authorization.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RolesList;

import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for Role
 * @author 
 */
public class RoleDocumentHandler
        extends AbstractDocumentHandlerImpl<Role, RolesList, Role, List> {

    private final Logger logger = LoggerFactory.getLogger(RoleDocumentHandler.class);
    private Role role;
    private RolesList rolesList;

    @Override
    public void handleCreate(DocumentWrapper<Role> wrapDoc) throws Exception {
        String id = UUID.randomUUID().toString();
        Role role = wrapDoc.getWrappedObject();
        role.setRoleName(fixRoleName(role.getRoleName()));
        role.setCsid(id);
        //FIXME: if admin updating the role is a CS admin rather than
        //the tenant admin, tenant id should be retrieved from the request
        role.setTenantId(getServiceContext().getTenantId());
    }

    @Override
    public void handleUpdate(DocumentWrapper<Role> wrapDoc) throws Exception {
        Role roleFound = wrapDoc.getWrappedObject();
        Role roleReceived = getCommonPart();
        roleReceived.setRoleName(fixRoleName(roleReceived.getRoleName()));
        merge(roleReceived, roleFound);
    }

    /**
     * merge manually merges the from from to the to role
     * -this method is created due to inefficiency of JPA EM merge
     * @param from
     * @param to
     * @return merged role
     */
    private Role merge(Role from, Role to) throws Exception {
        //role name cannot be changed
        if (!(from.getRoleName().equalsIgnoreCase(to.getRoleName()))) {
            String msg = "Role name cannot be changed " + to.getRoleName();
            logger.error(msg);
            throw new BadRequestException(msg);
        }
        if (from.getRoleGroup() != null) {
            to.setRoleGroup(from.getRoleGroup());
        }
        if (from.getDescription() != null) {
            to.setDescription(from.getDescription());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("merged role=" + JaxbUtils.toString(to, Role.class));
        }
        return to;
    }

    @Override
    public void completeUpdate(DocumentWrapper<Role> wrapDoc) throws Exception {
        Role upAcc = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(role);
        sanitize(upAcc);
    }

    @Override
    public void handleGet(DocumentWrapper<Role> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        sanitize(getCommonPart());
        getServiceContext().setOutput(role);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List> wrapDoc) throws Exception {
        RolesList rolesList = extractCommonPartList(wrapDoc);
        setCommonPartList(rolesList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public Role extractCommonPart(
            DocumentWrapper<Role> wrapDoc)
            throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public void fillCommonPart(Role obj, DocumentWrapper<Role> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountDocumentHandler");
    }

    @Override
    public RolesList extractCommonPartList(
            DocumentWrapper<List> wrapDoc)
            throws Exception {

        RolesList rolesList = new RolesList();
        List<Role> list = new ArrayList<Role>();
        rolesList.setRoles(list);
        for (Object obj : wrapDoc.getWrappedObject()) {
            Role role = (Role) obj;
            sanitize(role);
            list.add(role);
        }
        return rolesList;
    }

    @Override
    public Role getCommonPart() {
        return role;
    }

    @Override
    public void setCommonPart(Role role) {
        this.role = role;
    }

    @Override
    public RolesList getCommonPartList() {
        return rolesList;
    }

    @Override
    public void setCommonPartList(RolesList rolesList) {
        this.rolesList = rolesList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter() {
        DocumentFilter filter = new RoleJpaFilter(this.getServiceContext());
        return filter;
    }

    /**
     * sanitize removes data not needed to be sent to the consumer
     * @param roleFound
     */
    private void sanitize(Role role) {
        role.setTenantId(null);
    }

    private String fixRoleName(String role) {
        String roleName = role.toUpperCase();
        String rolePrefix = "ROLE_";
        if (!roleName.startsWith(rolePrefix)) {
            roleName = rolePrefix + roleName;
        }
        return roleName;
    }
}
