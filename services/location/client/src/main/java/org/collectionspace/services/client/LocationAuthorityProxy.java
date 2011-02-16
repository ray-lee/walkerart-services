package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.location.LocationauthoritiesCommonList;
import org.collectionspace.services.location.LocationsCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 */
@Path(LocationAuthorityClient.SERVICE_PATH + "/")
@Produces("application/xml")
@Consumes("application/xml")
public interface LocationAuthorityProxy extends CollectionSpaceProxy {

    // List Locationauthorities
    @GET
    ClientResponse<LocationauthoritiesCommonList> readList();

    //(C)reate
    @POST
    ClientResponse<Response> create(byte[] xmlPayload);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<String> read(@PathParam("csid") String csid);

    //(R)ead by name
    @GET
    @Path("/urn:cspace:name({name})")
    ClientResponse<String> readByName(@PathParam("name") String name);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<String> update(@PathParam("csid") String csid, byte[] xmlPayload);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);

    // List Items matching a partial term or keywords.
    @GET
    @Produces({"application/xml"})
    @Path("/{vcsid}/items/")
    ClientResponse<LocationsCommonList> readItemList(
    		@PathParam("vcsid") String vcsid,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);

    /**
     * @param parentcsid
     * @param itemcsid
     * @param csid
     * @return
     * @see org.collectionspace.services.client.IntakeProxy#getAuthorityRefs(java.lang.String)
     */
    @GET
    @Path("{csid}/items/{itemcsid}/refObjs")
    @Produces("application/xml")
    ClientResponse<AuthorityRefDocList> getReferencingObjects(
            @PathParam("csid") String parentcsid,
            @PathParam("itemcsid") String itemcsid);

    // List Items for a named authority matching a partial term or keywords.
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({specifier})/items/")
    ClientResponse<LocationsCommonList> readItemListForNamedAuthority(
    		@PathParam("specifier") String specifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);

    //(C)reate Item
    @POST
    @Path("/{vcsid}/items/")
    ClientResponse<Response> createItem(@PathParam("vcsid") String vcsid, byte[] xmlPayload);

    //(R)ead Item
    @GET
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<String> readItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid);

    //(U)pdate Item
    @PUT
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<String> updateItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid, byte[] xmlPayload);

    //(D)elete Item
    @DELETE
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<Response> deleteItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid);

     // List Contacts
    @GET
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    ClientResponse<ContactsCommonList> readContactList(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid);

    //(C)reate Contact
    @POST
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    ClientResponse<Response> createContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            byte[] xmlPayload);

     //(R)ead Contact
    @GET
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<String> readContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);

    //(U)pdate Contact
    @PUT
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<String> updateContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            byte[] xmlPayload);

    //(D)elete Contact
    @DELETE
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<Response> deleteContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);

}
