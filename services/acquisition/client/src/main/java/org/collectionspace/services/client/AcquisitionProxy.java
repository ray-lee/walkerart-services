package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path(AcquisitionClient.SERVICE_PATH_PROXY)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface AcquisitionProxy extends CollectionSpaceCommonListPoxProxy {
	
}
