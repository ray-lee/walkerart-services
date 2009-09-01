package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path("/collectionobjects/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface CollectionObjectProxy {

    @GET
    ClientResponse<CollectionObjectList> readList();

    //(C)reate
    @POST
    ClientResponse<Response> create(CollectionObject co);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<CollectionObject> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<CollectionObject> update(@PathParam("csid") String csid, CollectionObject co);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}