package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @version $Revision: 2108 $
 */
@Path(BatchClient.SERVICE_PATH + "/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface BatchProxy extends CollectionSpaceCommonListPoxProxy {
}
