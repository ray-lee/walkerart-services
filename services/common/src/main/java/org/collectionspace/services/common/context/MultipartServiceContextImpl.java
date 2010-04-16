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
package org.collectionspace.services.common.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * MultipartServiceContextImpl takes Multipart Input/Output
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class MultipartServiceContextImpl
        extends RemoteServiceContextImpl<MultipartInput, MultipartOutput>
        implements MultipartServiceContext {

    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(MultipartServiceContextImpl.class);

    /**
     * Instantiates a new multipart service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected MultipartServiceContextImpl(String serviceName)
			throws UnauthorizedException {
    	super(serviceName);
    	setOutput(new MultipartOutput());
    }
    
    /**
     * Instantiates a new multipart service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected MultipartServiceContextImpl(String serviceName, MultipartInput theInput)
    		throws UnauthorizedException {
        super(serviceName, theInput);
        setOutput(new MultipartOutput());
    }

    /**
     * Instantiates a new multipart service context impl.
     * 
     * @param serviceName the service name
     * @param queryParams the query params
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected MultipartServiceContextImpl(String serviceName,
    		MultipartInput theInput,
    		MultivaluedMap<String, String> queryParams) throws UnauthorizedException {
    	super(serviceName, theInput, queryParams);
    	setOutput(new MultipartOutput());
    }

    /**
     * Gets the input part.
     * 
     * @param label the label
     * 
     * @return the input part
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private InputPart getInputPart(String label) throws IOException {
        if (getInput() != null) {
            MultipartInput fdip = getInput();

            for (InputPart part : fdip.getParts()) {
                String partLabel = part.getHeaders().getFirst("label");
                if (label.equalsIgnoreCase(partLabel)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("getInputPart found part with label=" + partLabel
                                + "\npayload=" + part.getBodyAsString());
                    }
                    return part;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPart(java.lang.String, java.lang.Class)
     */
    @Override
    public Object getInputPart(String label, Class clazz) throws IOException {
        Object obj = null;
        InputPart part = getInputPart(label);
        if (part != null) {
            obj = part.getBody(clazz, null);
        }
        return obj;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPartAsString(java.lang.String)
     */
    @Override
    public String getInputPartAsString(String label) throws IOException {
        InputPart part = getInputPart(label);
        if (part != null) {
            return part.getBodyAsString();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPartAsStream(java.lang.String)
     */
    @Override
    public InputStream getInputPartAsStream(String label) throws IOException {
        InputPart part = getInputPart(label);
        if (part != null) {
            return new ByteArrayInputStream(part.getBodyAsString().getBytes());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#addOutputPart(java.lang.String, org.w3c.dom.Document, java.lang.String)
     */
    @Override
    public void addOutputPart(String label, Document doc, String contentType) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DocumentUtils.writeDocument(doc, baos);
            baos.close();
            OutputPart part = getOutput().addPart(new String(baos.toByteArray()),
                    MediaType.valueOf(contentType));
            part.getHeaders().add("label", label);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.RemoteServiceContextImpl#getLocalContext(java.lang.String)
     */
    @Override
    public ServiceContext getLocalContext(String localContextClassName) throws Exception {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        Class ctxClass = cloader.loadClass(localContextClassName);
        if (!ServiceContext.class.isAssignableFrom(ctxClass)) {
            throw new IllegalArgumentException("getLocalContext requires "
                    + " implementation of " + ServiceContext.class.getName());
        }

        Constructor ctor = ctxClass.getConstructor(java.lang.String.class);
        ServiceContext ctx = (ServiceContext) ctor.newInstance(getServiceName());
        return ctx;
    }
}
