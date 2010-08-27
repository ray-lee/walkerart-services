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

import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.collectionspace.services.person.PersonsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

/**
 * @version $Revision:$
 */
@Path("/personauthorities/")
@Produces({"multipart/mixed"})
@Consumes({"multipart/mixed"})
public interface PersonAuthorityProxy extends CollectionSpaceProxy {

    // List Personauthorities
    @GET
    @Produces({"application/xml"})
    ClientResponse<PersonauthoritiesCommonList> readList();

    //(C)reate
    @POST
    ClientResponse<Response> create(MultipartOutput multipart);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<MultipartInput> read(@PathParam("csid") String csid);

    //(R)ead by name
    @GET
    @Path("/urn:cspace:name({name})")
    ClientResponse<MultipartInput> readByName(@PathParam("name") String name);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<MultipartInput> update(@PathParam("csid") String csid, MultipartOutput multipart);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);

    // List Items with options for matching a partial term or keywords.
    @GET
    @Produces("application/xml")
    @Path("/{csid}/items/")
    ClientResponse<PersonsCommonList>readItemList(
            @PathParam("csid") String parentcsid,
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
    ClientResponse<PersonsCommonList> readItemListForNamedAuthority(
    		@PathParam("specifier") String specifier,
            @QueryParam (IQueryManager.SEARCH_TYPE_PARTIALTERM) String partialTerm,
            @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords);

    //(C)reate Item
    @POST
    @Path("/{vcsid}/items/")
    ClientResponse<Response> createItem(@PathParam("vcsid") String vcsid, MultipartOutput multipart);

    //(R)ead Item
    @GET
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<MultipartInput> readItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid);

    //(R)ead Named Item
    @GET
    @Path("/{vcsid}/items/urn:cspace:name({specifier})")
    ClientResponse<MultipartInput> readNamedItem(@PathParam("vcsid") String vcsid, @PathParam("specifier") String specifier);

    //(R)ead Item In Named Authority
    @GET
    @Path("/urn:cspace:name({specifier})/items/{csid}")
    ClientResponse<MultipartInput> readItemInNamedAuthority(@PathParam("specifier") String specifier, @PathParam("csid") String csid);

    //(R)ead Named Item In Named Authority
    @GET
    @Path("/urn:cspace:name({specifier})/items/urn:cspace:name({itemspecifier})")
    ClientResponse<MultipartInput> readNamedItemInNamedAuthority(@PathParam("specifier") String specifier, @PathParam("itemspecifier") String itemspecifier);

    //(U)pdate Item
    @PUT
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<MultipartInput> updateItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid, MultipartOutput multipart);

    //(D)elete Item
    @DELETE
    @Path("/{vcsid}/items/{csid}")
    ClientResponse<Response> deleteItem(@PathParam("vcsid") String vcsid, @PathParam("csid") String csid);

    // List Contacts
    @GET
    @Produces({"application/xml"})
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    ClientResponse<ContactsCommonList> readContactList(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid);
    @GET
    @Produces({"application/xml"})
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<ContactsCommonList> readContactListForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier);
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/")
    ClientResponse<ContactsCommonList> readContactListForItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid);
    @GET
    @Produces({"application/xml"})
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<ContactsCommonList> readContactListForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier);

    //(C)reate Contact
    @POST
    @Path("/{parentcsid}/items/{itemcsid}/contacts/")
    ClientResponse<Response> createContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            MultipartOutput multipart);
    @POST
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<Response> createContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            MultipartOutput multipart);
    @POST
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/")
    ClientResponse<Response> createContactForItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            MultipartOutput multipart);
    @POST
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/")
    ClientResponse<Response> createContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            MultipartOutput multipart);

     //(R)ead Contact
    @GET
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<MultipartInput> readContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @GET
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<MultipartInput> readContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
    @GET
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    ClientResponse<MultipartInput> readContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @GET
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<MultipartInput> readContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);

    //(U)pdate Contact
    @PUT
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<MultipartInput> updateContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            MultipartOutput multipart);
    @PUT
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<MultipartInput> updateContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid,
            MultipartOutput multipart);
    @PUT
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    ClientResponse<MultipartInput> updateContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid,
            MultipartOutput multipart);
    @PUT
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<MultipartInput> updateContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid,
            MultipartOutput multipart);

    //(D)elete Contact
    @DELETE
    @Path("/{parentcsid}/items/{itemcsid}/contacts/{csid}")
    ClientResponse<Response> deleteContact(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @DELETE
    @Path("/{parentcsid}/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<Response> deleteContactForNamedItem(
            @PathParam("parentcsid") String parentcsid,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
    @DELETE
    @Path("/urn:cspace:name({parentspecifier})/items/{itemcsid}/contacts/{csid}")
    ClientResponse<Response> deleteContactInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemcsid") String itemcsid,
            @PathParam("csid") String csid);
    @DELETE
    @Path("/urn:cspace:name({parentspecifier})/items/urn:cspace:name({itemspecifier})/contacts/{csid}")
    ClientResponse<Response> deleteContactForNamedItemInNamedAuthority(
            @PathParam("parentspecifier") String parentspecifier,
            @PathParam("itemspecifier") String itemspecifier,
            @PathParam("csid") String csid);
}
