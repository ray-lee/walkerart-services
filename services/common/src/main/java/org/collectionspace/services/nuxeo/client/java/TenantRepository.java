/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.nuxeo.client.java;

import java.util.Hashtable;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.RepositoryClientConfigType;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.tenant.RepositoryDomainType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TenatnRepository is used to retrieve workspaces for a tenant as well as to
 * create workspaces for each service used by a teant
 * @author
 */
public class TenantRepository {

    final private static TenantRepository self = new TenantRepository();
    final Logger logger = LoggerFactory.getLogger(TenantRepository.class);
    //tenant-qualified service, workspace
    private Hashtable<String, String> serviceWorkspaces = new Hashtable<String, String>();

    private TenantRepository() {
    }

    public static TenantRepository get() {
        return self;
    }

    /**
     * getWorkspaceId for a tenant's service
     * @param tenantId
     * @param serviceName
     * @return workspace id
     */
    public String getWorkspaceId(String tenantId, String serviceName) {
        String tenantService =
                TenantBindingConfigReaderImpl.getTenantQualifiedServiceName(tenantId, serviceName);
        return serviceWorkspaces.get(tenantService);
    }

    /**
     * setup is called at initialization time to setup tenant specific repository(s)
     * it will create repository domain(s) and
     * also service workspaces for all the services used by given tenant(s)
     * @param hashtable <tenant name, tenantbinding>
     * @throws Exception
     */
    synchronized public void setup(Hashtable<String, TenantBindingType> tenantBindings)
            throws Exception {
        for (TenantBindingType tenantBinding : tenantBindings.values()) {
            setup(tenantBinding);
        }
    }

    /**
     * setup sets up repository(s) for given tenant
     * it will create repository domains and also service workspaces if needed
     * @param tenantBinding
     * @throws Exception
     */
    synchronized public void setup(TenantBindingType tenantBinding) throws Exception {
        ServiceMain svcMain = ServiceMain.getInstance();
        RepositoryClientConfigType rclientConfig =
                svcMain.getServicesConfigReader().getConfiguration().getRepositoryClient();
        ClientType clientType = svcMain.getClientType();
        if (clientType.equals(ClientType.JAVA)
                && rclientConfig.getName().equalsIgnoreCase("nuxeo-java")) {
            for (RepositoryDomainType repositoryDomain : tenantBinding.getRepositoryDomain()) {
                createDomain(tenantBinding, repositoryDomain);
                createWorkspaces(tenantBinding, repositoryDomain);
            }
        }
    }

    private RepositoryClient getRepositoryClient(RepositoryDomainType repositoryDomain) {
        String clientName = repositoryDomain.getRepositoryClient();
        if (clientName == null) {
            String msg = "Could not find repository client=" + clientName
                    + " for repositoryDomain=" + repositoryDomain.getName();
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return RepositoryClientFactory.getInstance().getClient(clientName);
    }

    /**
     * createDomain retrieves domain id for the given domain or creates one
     * @param tenantBinding
     * @param repositoryDomain
     * @throws Exception
     */
    synchronized private void createDomain(TenantBindingType tenantBinding,
            RepositoryDomainType repositoryDomain) throws Exception {
        String domainName = repositoryDomain.getName();
        RepositoryClient repositoryClient = getRepositoryClient(repositoryDomain);
        String domainId = repositoryClient.getDomainId(domainName);
        if (domainId == null) {
            domainId = repositoryClient.createDomain(domainName);
            if (logger.isDebugEnabled()) {
                logger.debug("created repository domain for " + domainName
                        + " id=" + domainId);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("found repository domain for " + domainName
                        + " id=" + domainId);
            }
        }
    }

    /**
     * createWorkspaces retrieves workspace ids for each domain specified in
     * the tenant binding, if workspace does not exist, it creates one
     * @param tenantBinding
     * @param repositoryDomain
     * @throws Exception
     */
    synchronized private void createWorkspaces(TenantBindingType tenantBinding,
            RepositoryDomainType repositoryDomain) throws Exception {

        RepositoryClient repositoryClient = getRepositoryClient(repositoryDomain);

        //retrieve all workspace ids for a domain
        //domain specific table of workspace name and id
        Hashtable<String, String> workspaceIds =
                repositoryClient.retrieveWorkspaceIds(repositoryDomain.getName());
        //verify if workspace exists for each service from the tenant binding
        for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
            String serviceName = serviceBinding.getName();
            String repositoryDomainName = serviceBinding.getRepositoryDomain();
            if (repositoryDomainName == null) {
                //no repository needed for this service...skip
                if (logger.isInfoEnabled()) {
                    logger.info("The service " + serviceName
                            + " for tenant=" + tenantBinding.getName()
                            + " does not seem to require a document repository.");
                }
                continue;
            }
            if (repositoryDomainName.isEmpty()) {
                String msg = "Invalid repositoryDomain for " + serviceName
                        + " for tenant=" + tenantBinding.getName();
                logger.error(msg);
                continue;
            }
            repositoryDomainName = repositoryDomainName.trim();
            if (!repositoryDomain.getName().equalsIgnoreCase(repositoryDomainName)) {
                continue;
            }
            String workspaceId = null;
            //workspace name is service name by convention
            String workspace = serviceName.toLowerCase();
            //if workspaceid is in the binding, take it, else retrieve
            workspaceId = serviceBinding.getRepositoryWorkspaceId();
            if (workspaceId == null) {
                workspaceId = workspaceIds.get(workspace);
                if (workspaceId == null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to retrieve workspace ID for " + workspace
                                + " from repository, creating a new workspace ...");
                    }
                    workspaceId = repositoryClient.createWorkspace(
                            repositoryDomain.getName(),
                            serviceBinding.getName());
                    if (workspaceId == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Failed to create workspace in repository"
                                    + " for service=" + workspace
                                    + " for tenant=" + tenantBinding.getName());
                        }
                        continue;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully created workspace in repository"
                                + " id=" + workspaceId + " for service=" + workspace
                                + " for tenant=" + tenantBinding.getName());
                    }
                }
            }
            if (workspaceId != null) {
                String tenantService =
                        TenantBindingConfigReaderImpl.getTenantQualifiedServiceName(
                        tenantBinding.getId(), serviceName);
                serviceWorkspaces.put(tenantService, workspaceId);
                if (logger.isInfoEnabled()) {
                    logger.info("Created/retrieved repository workspace="
                            + workspace + " id=" + workspaceId
                            + " for service=" + serviceName
                            + " for tenant=" + tenantBinding.getName());
                }
            }
        }//rof for service binding
    }
}
