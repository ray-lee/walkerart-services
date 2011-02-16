/**	
 * OrgAuthorityClient.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision$
 * $LastChangedDate$
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

//import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * The Class OrgAuthorityClient.
 */
public class OrgAuthorityClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "orgauthorities";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;
	//
	// Subitem constants
	//
	public static final String SERVICE_ITEM_NAME = "organizations";
	public static final String SERVICE_PATH_ITEMS_COMPONENT = "items";	//FIXME: REM - This should be defined in an AuthorityClient base class
	public static final String SERVICE_ITEM_PAYLOAD_NAME = SERVICE_ITEM_NAME;
	//
	// Payload Part/Schema part names
	//
	public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME + 
		PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
	public static final String SERVICE_ITEM_COMMON_PART_NAME = SERVICE_ITEM_NAME + 
		PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
	
    @Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    /**
     * Gets the item common part name.
     *
     * @return the item common part name
     */
    public String getItemCommonPartName() {
        return getCommonPartName(SERVICE_ITEM_NAME);
    }
    
    /** The org authority proxy. */
    private OrgAuthorityProxy orgAuthorityProxy;

    /**
     * Instantiates a new org authority client.
     */
    public OrgAuthorityClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.CollectionSpaceClient#getProxy()
     */
    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.orgAuthorityProxy;
    }    

    /**
     * Sets the proxy.
     */
    public void setProxy() {
        if (useAuth()) {
            orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class,
                    getBaseURL());
        }
    }

    /**
     * Gets the single instance of OrgAuthorityClient.
     *
     * @return single instance of OrgAuthorityClient //FIXME: This is wrong.  There should NOT be a static instance of the client
     */
//    public static OrgAuthorityClient getInstance() {
//        return instance;
//    }

    /**
     * Read list.
     *
     * @return the client response
     */
    public ClientResponse<OrgauthoritiesCommonList> readList() {
        return orgAuthorityProxy.readList();
    }

    /**
     * Read.
     *
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> read(String csid) {
        return orgAuthorityProxy.read(csid);
    }

    /**
     * Read by name.
     *
     * @param name the name
     * @return the client response
     */
    public ClientResponse<String> readByName(String name) {
        return orgAuthorityProxy.readByName(name);
    }

    /**
     * Creates the.
     *
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return orgAuthorityProxy.create(xmlPayload.getBytes());
    }

    /**
     * Update.
     *
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return orgAuthorityProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * Delete.
     *
     * @param csid the csid
     * @return the client response
     */
    @Override
    public ClientResponse<Response> delete(String csid) {
        return orgAuthorityProxy.delete(csid);
    }

    /**
     * Read item list, filtering by partial term match, or keywords. Only one of
     * partialTerm or keywords should be specified. If both are specified, keywords
     * will be ignored.
     *
     * @param inAuthority the parent authority
     * @param partialTerm A partial term on which to match,
     *     which will filter list results to return only matched resources.
     * @param keywords A set of keywords on which to match,
     *     which will filter list results to return only matched resources.
     * @return the client response
     */
    public ClientResponse<OrganizationsCommonList> 
    		readItemList(String inAuthority, String partialTerm, String keywords) {
        return orgAuthorityProxy.readItemList(inAuthority, partialTerm, keywords);
    }
    
    /**
     * Gets the referencing objects.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the referencing objects
     */
    public ClientResponse<AuthorityRefDocList> getReferencingObjects(String parentcsid, String csid) {
        return orgAuthorityProxy.getReferencingObjects(parentcsid, csid);
    }
    

    /**
     * Read item list for named vocabulary, filtering by partial term match, or keywords. Only one of
     * partialTerm or keywords should be specified. If both are specified, keywords
     * will be ignored.
     *
     * @param specifier the specifier
     * @param partialTerm A partial term on which to match,
     *     which will filter list results to return only matched resources.
     * @param keywords A set of keywords on which to match,
     *     which will filter list results to return only matched resources.
     * @return the client response
     */
    public ClientResponse<OrganizationsCommonList> 
    		readItemListForNamedAuthority(String specifier, String partialTerm, String keywords) {
        return orgAuthorityProxy.readItemListForNamedAuthority(specifier, partialTerm, keywords);
    }

    /**
     * Gets the item authority refs.
     *
     * @param parentcsid the parentcsid
     * @param csid the csid
     * @return the item authority refs
     */
    public ClientResponse<AuthorityRefList> getItemAuthorityRefs(String parentcsid, String csid) {
        return orgAuthorityProxy.getItemAuthorityRefs(parentcsid, csid);
    }

    /**
     * Read item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItem(String vcsid, String csid) {
        return orgAuthorityProxy.readItem(vcsid, csid);
    }

    /**
     * Read named item.
     *
     * @param vcsid the vcsid
     * @param shortId the shortIdentifier
     * @return the client response
     */
    public ClientResponse<String> readNamedItem(String vcsid, String shortId) {
        return orgAuthorityProxy.readNamedItem(vcsid, shortId);
    }

    /**
     * Read item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readItemInNamedAuthority(String authShortId, String csid) {
        return orgAuthorityProxy.readItemInNamedAuthority(authShortId, csid);
    }

    /**
     * Read named item in Named Authority.
     *
     * @param authShortId the shortIdentifier for the Authority
     * @param itemShortId the shortIdentifier for the item
     * @return the client response
     */
    public ClientResponse<String> readNamedItemInNamedAuthority(String authShortId, String itemShortId) {
        return orgAuthorityProxy.readNamedItemInNamedAuthority(authShortId, itemShortId);
    }

    /**
     * Creates the item.
     *
     * @param vcsid the vcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createItem(String vcsid, PoxPayloadOut multipart) {
        return orgAuthorityProxy.createItem(vcsid, multipart.getBytes());
    }

    /**
     * Update item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateItem(String vcsid, String csid, PoxPayloadOut xmlPayload) {
        return orgAuthorityProxy.updateItem(vcsid, csid, xmlPayload.getBytes());

    }

    /**
     * Delete item.
     *
     * @param vcsid the vcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return orgAuthorityProxy.deleteItem(vcsid, csid);
    }

    /***************************************************************************
     * 
     * Contact sub-resource interfaces
     * 
     ***************************************************************************/
    
    /**
     * Creates the contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<Response> createContact(String parentcsid,
            String itemcsid, PoxPayloadOut xmlPayload) {
        return orgAuthorityProxy.createContact(parentcsid, itemcsid, xmlPayload.getBytes());
    }

    /**
     * Creates the contact.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param multipart
     * @return the client response
     */
    public ClientResponse<Response> createContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		PoxPayloadOut xmlPayload) {
    	return orgAuthorityProxy.createContactForNamedItem(parentcsid, itemspecifier, xmlPayload.getBytes());
    }
    /**
     * Creates the contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @param multipart
     * @return the client response
     */
    public ClientResponse<Response> createContactForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		PoxPayloadOut xmlPayload) {
    	return orgAuthorityProxy.createContactForItemInNamedAuthority(parentspecifier, itemcsid, xmlPayload.getBytes());
    }
    /**
     * Creates the contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param multipart
     * @return the client response
     */
    public ClientResponse<Response> createContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		PoxPayloadOut xmlPayload) {
    	return orgAuthorityProxy.createContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, xmlPayload.getBytes());
    }
    
    /**
     * Read contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<String> readContact(String parentcsid,
            String itemcsid, String csid) {
        return orgAuthorityProxy.readContact(parentcsid, itemcsid, csid);
    }
    
    /**
     * Read contact.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid
     * @return the client response
     */
    public ClientResponse<String> readContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid){
    	return orgAuthorityProxy.readContactForNamedItem(parentcsid, itemspecifier, csid);
    }

    /**
     * Read contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @param csid
     * @return the client response
     */
    public ClientResponse<String> readContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid){
    	return orgAuthorityProxy.readContactInNamedAuthority(parentspecifier, itemcsid, csid);
    }

    /**
     * Read contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid
     * @return the client response
     */
    public ClientResponse<String> readContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid){
    	return orgAuthorityProxy.readContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, csid);
    }
            

    /**
     * Read contact list.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactList(String parentcsid,
            String itemcsid) {
        return orgAuthorityProxy.readContactList(parentcsid, itemcsid);
    }
    
    /**
     * Read contact list.
     *
     * @param parentcsid
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactListForNamedItem(
    		String parentcsid,
    		String itemspecifier){
    	return orgAuthorityProxy.readContactList(parentcsid, itemspecifier);
    }

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactListForItemInNamedAuthority(
    		String parentspecifier,
    		String itemcsid){
    	return orgAuthorityProxy.readContactList(parentspecifier, itemcsid);
    }

    /**
     * Read contact list.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @return the client response
     */
    public ClientResponse<ContactsCommonList> readContactListForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier){
    	return orgAuthorityProxy.readContactList(parentspecifier, itemspecifier);
    }
            

    /**
     * Update contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContact(String parentcsid,
            String itemcsid, String csid, PoxPayloadOut xmlPayload) {
        return orgAuthorityProxy.updateContact(parentcsid, itemcsid, csid, xmlPayload.getBytes());
    }
    
    /**
     * Update contact.
     *
     * @param parentcsid the parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return orgAuthorityProxy.updateContactForNamedItem(parentcsid, itemspecifier, csid, xmlPayload.getBytes());
    }

    /**
     * Update contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return orgAuthorityProxy.updateContactInNamedAuthority(parentspecifier, itemcsid, csid, xmlPayload.getBytes());
    }

    /**
     * Update contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @param multipart the multipart
     * @return the client response
     */
    public ClientResponse<String> updateContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid,
    		PoxPayloadOut xmlPayload) {
    	return orgAuthorityProxy.updateContactForNamedItemInNamedAuthority(parentspecifier, itemspecifier, csid, xmlPayload.getBytes());
    }


    /**
     * Delete contact.
     *
     * @param parentcsid the parentcsid
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContact(String parentcsid,
        String itemcsid, String csid) {
        return orgAuthorityProxy.deleteContact(parentcsid,
            itemcsid, csid);
    }
    
    /**
     * Delete contact.
     *
     * @param parentcsid the parentcsid
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContactForNamedItem(
    		String parentcsid,
    		String itemspecifier,
    		String csid) {
    	return orgAuthorityProxy.deleteContactForNamedItem(parentcsid,
    			itemspecifier, csid);
    }

    /**
     * Delete contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemcsid the itemcsid
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContactInNamedAuthority(
    		String parentspecifier,
    		String itemcsid,
    		String csid) {
    	return orgAuthorityProxy.deleteContactInNamedAuthority(parentspecifier,
    			itemcsid, csid);
    }

    /**
     * Delete contact.
     *
     * @param parentspecifier (shortIdentifier)
     * @param itemspecifier (shortIdentifier)
     * @param csid the csid
     * @return the client response
     */
    public ClientResponse<Response> deleteContactForNamedItemInNamedAuthority(
    		String parentspecifier,
    		String itemspecifier,
    		String csid) {
    	return orgAuthorityProxy.deleteContactForNamedItemInNamedAuthority(parentspecifier,
    			itemspecifier, csid);
    }

}
