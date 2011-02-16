/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * ObjectExitClient.java
 *
 * $LastChangedRevision: 2108 $
 * $LastChangedDate: 2010-05-17 18:25:37 -0700 (Mon, 17 May 2010) $
 *
 */
public class ObjectExitClient extends AbstractServiceClientImpl {
	public static final String SERVICE_NAME = "objectexit";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PAYLOAD_NAME = SERVICE_NAME;	
    /**
     *
     */
    private ObjectExitProxy objectexitProxy;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	
	@Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }
    
    /**
     *
     * Default constructor for ObjectExitClient class.
     *
     */
    public ObjectExitClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    @Override
    public CollectionSpaceProxy getProxy() {
    	return this.objectexitProxy;
    }    

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            objectexitProxy = ProxyFactory.create(ObjectExitProxy.class,
                    getBaseURL(), new ApacheHttpClientExecutor(getHttpClient()));
        } else {
            objectexitProxy = ProxyFactory.create(ObjectExitProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
//    public static ObjectExitClient getInstance() {
//        return instance;
//    }

    /**
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#getObjectExit()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return objectexitProxy.readList();
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#getAuthorityRefs(java.lang.String)
     */
    public ClientResponse<AuthorityRefList> getAuthorityRefs(String csid) {
        return objectexitProxy.getAuthorityRefs(csid);
    }


    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#getObjectExit(java.lang.String)
     */
    public ClientResponse<String> read(String csid) {
        return objectexitProxy.read(csid);
    }

    /**
     * @param objectexit
     * @return
     *
     */
    public ClientResponse<Response> create(PoxPayloadOut xmlPayload) {
        return objectexitProxy.create(xmlPayload.getBytes());
    }

    /**
     * @param csid
     * @param objectexit
     * @return
     */
    public ClientResponse<String> update(String csid, PoxPayloadOut xmlPayload) {
        return objectexitProxy.update(csid, xmlPayload.getBytes());

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.ObjectExitProxy#deleteObjectExit(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return objectexitProxy.delete(csid);
    }
}
