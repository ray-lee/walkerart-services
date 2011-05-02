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
package org.collectionspace.services.nuxeo.client.java;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.authorization.AccountPermission;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.context.JaxRsContext;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.common.vocabulary.RefNameUtils;
import org.dom4j.Element;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;

import org.nuxeo.ecm.core.schema.types.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;

/**
 * RemoteDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 * @param <T> 
 * @param <TL> 
 */
public abstract class   RemoteDocumentModelHandlerImpl<T, TL>
        extends DocumentModelHandler<T, TL> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RemoteDocumentModelHandlerImpl.class);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#setServiceContext(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public void setServiceContext(ServiceContext ctx) {  //FIXME: Apply proper generics to ServiceContext<PoxPayloadIn, PoxPayloadOut>
        if (ctx instanceof MultipartServiceContext) {
            super.setServiceContext(ctx);
        } else {
            throw new IllegalArgumentException("setServiceContext requires instance of "
                    + MultipartServiceContext.class.getName());
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        //return at least those document part(s) that were received
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        PoxPayloadIn input = ctx.getInput();
        if (input != null) {
	        List<PayloadInputPart> inputParts = ctx.getInput().getParts();
	        for (PayloadInputPart part : inputParts) {
	            String partLabel = part.getLabel();
                try{
                    ObjectPartType partMeta = partsMetaMap.get(partLabel);
        //            extractPart(docModel, partLabel, partMeta);
                    Map<String, Object> unQObjectProperties = extractPart(docModel, partLabel, partMeta);
                    addOutputPart(unQObjectProperties, partLabel, partMeta);
                } catch (Throwable t){
                    System.out.println("===============================\r\nUnable to addOutputPart: "+partLabel+" error: "+t);
                }
	        }
        } else {
        	if (logger.isWarnEnabled() == true) {
        		logger.warn("MultipartInput part was null for document id = " +
        				docModel.getName());
        	}
        }
    }

    /**
     * Adds the output part.
     *
     * @param unQObjectProperties the un q object properties
     * @param schema the schema
     * @param partMeta the part meta
     * @throws Exception the exception
     * MediaType.APPLICATION_XML_TYPE
     */
    protected void addOutputPart(Map<String, Object> unQObjectProperties, String schema, ObjectPartType partMeta)
            throws Exception {
        Element doc = DocumentUtils.buildDocument(partMeta, schema,
                unQObjectProperties);
        if (logger.isDebugEnabled() == true) {
            logger.debug(doc.asXML());
        }
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        ctx.addOutputPart(schema, doc, partMeta.getContent().getContentType());
    }
    
    /**
     * Extract paging info.
     *
     * @param commonsList the commons list
     * @return the tL
     * @throws Exception the exception
     */
    public TL extractPagingInfo(TL theCommonList, DocumentWrapper<DocumentModelList> wrapDoc)
            throws Exception {
        AbstractCommonList commonList = (AbstractCommonList) theCommonList;

        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        DocumentModelList docList = wrapDoc.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        commonList.setItemsInPage(docList.size());
        // set the total result size
        commonList.setTotalItems(docList.totalSize());

        return (TL) commonList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractAllParts(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {

        DocumentModel docModel = wrapDoc.getWrappedObject();
        String[] schemas = docModel.getDeclaredSchemas();
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        for (String schema : schemas) {
            ObjectPartType partMeta = partsMetaMap.get(schema);
            if (partMeta == null) {
                continue; // unknown part, ignore
            }
            Map<String, Object> unQObjectProperties = extractPart(docModel, schema, partMeta);
            addOutputPart(unQObjectProperties, schema, partMeta);
        }
        addAccountPermissionsPart();
    }
    
    private void addAccountPermissionsPart() throws Exception {
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        String currentServiceName = ctx.getServiceName();
        String workflowSubResource = "/";
        JaxRsContext jaxRsContext = ctx.getJaxRsContext();
        if (jaxRsContext != null) {
        	String resourceName = SecurityUtils.getResourceName(jaxRsContext.getUriInfo());
        	workflowSubResource = workflowSubResource + resourceName + WorkflowClient.SERVICE_PATH + "/";
        } else {
        	workflowSubResource = workflowSubResource + currentServiceName + WorkflowClient.SERVICE_AUTHZ_SUFFIX;
        }
        AccountPermission accountPermission = JpaStorageUtils.getAccountPermissions(JpaStorageUtils.CS_CURRENT_USER,
        		currentServiceName, workflowSubResource);
        PayloadOutputPart accountPermissionPart = new PayloadOutputPart("account_permission", accountPermission);
        ctx.addOutputPart(accountPermissionPart);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillAllParts(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {

        //TODO filling extension parts should be dynamic
        //Nuxeo APIs lack to support stream/byte[] input, get/setting properties is
        //not an ideal way of populating objects.
        DocumentModel docModel = wrapDoc.getWrappedObject();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        PoxPayloadIn input = ctx.getInput();
        if (input.getParts().isEmpty()) {
            String msg = "No payload found!";
            logger.error(msg + "Ctx=" + getServiceContext().toString());
            throw new BadRequestException(msg);
        }

        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();

        //iterate over parts received and fill those parts
        List<PayloadInputPart> inputParts = input.getParts();
        for (PayloadInputPart part : inputParts) {

            String partLabel = part.getLabel();
            if (partLabel == null) {
                String msg = "Part label is missing or empty!";
                logger.error(msg + "Ctx=" + getServiceContext().toString());
                throw new BadRequestException(msg);
            }

            //skip if the part is not in metadata
            ObjectPartType partMeta = partsMetaMap.get(partLabel);
            if (partMeta == null) {
                continue;
            }
            fillPart(part, docModel, partMeta, action, ctx);
        }//rof

    }

    /**
     * fillPart fills an XML part into given document model
     * @param part to fill
     * @param docModel for the given object
     * @param partMeta metadata for the object to fill
     * @throws Exception
     */
    protected void fillPart(PayloadInputPart part, DocumentModel docModel,
            ObjectPartType partMeta, Action action, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception {
        //check if this is an xml part
        if (part.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
//                Document document = DocumentUtils.parseDocument(payload, partMeta,
//                        false /*don't validate*/);
        	Element element = part.getElementBody();
            Map<String, Object> objectProps = DocumentUtils.parseProperties(partMeta, element, ctx);
                if (action == Action.UPDATE) {
                    this.filterReadOnlyPropertiesForPart(objectProps, partMeta);
                }
                docModel.setProperties(partMeta.getLabel(), objectProps);
            }
        }

    /**
     * Filters out read only properties, so they cannot be set on update.
     * TODO: add configuration support to do this generally
     * @param objectProps the properties parsed from the update payload
     * @param partMeta metadata for the object to fill
     */
    public void filterReadOnlyPropertiesForPart(
            Map<String, Object> objectProps, ObjectPartType partMeta) {
        // Currently a no-op, but can be overridden in Doc handlers.
    }

    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema)
            throws Exception {
        return extractPart(docModel, schema, (Map<String, Object>)null);
    }
    
    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    @Deprecated
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        return extractPart(docModel, schema, partMeta, null);
    }    

    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    protected Map<String, Object> extractPart(
            DocumentModel docModel, 
            String schema,
            Map<String, Object> addToMap)
            throws Exception {
        Map<String, Object> result = null;

        Map<String, Object> objectProps = docModel.getProperties(schema);
        if (objectProps != null) {
	        //unqualify properties before sending the doc over the wire (to save bandwidh)
	        //FIXME: is there a better way to avoid duplication of a Map/Collection?
	        Map<String, Object> unQObjectProperties =
	                (addToMap != null) ? addToMap : (new HashMap<String, Object>());
	        Set<Entry<String, Object>> qualifiedEntries = objectProps.entrySet();
	        for (Entry<String, Object> entry : qualifiedEntries) {
	            String unqProp = getUnQProperty(entry.getKey());
	            unQObjectProperties.put(unqProp, entry.getValue());
	        }
	        result = unQObjectProperties;
        }

        return result;
    }
    
    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    @Deprecated
    protected Map<String, Object> extractPart(
            DocumentModel docModel, String schema, ObjectPartType partMeta,
            Map<String, Object> addToMap)
            throws Exception {
        Map<String, Object> result = null;

        result = this.extractPart(docModel, schema, addToMap);

        return result;
    }
    

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getAuthorityRefs(org.collectionspace.services.common.document.DocumentWrapper, java.util.List)
     */
    @Override
    public AuthorityRefList getAuthorityRefs(
            DocumentWrapper<DocumentModel> docWrapper,
            List<String> authRefFieldNames) throws PropertyException {

        AuthorityRefList authRefList = new AuthorityRefList();
        AbstractCommonList commonList = (AbstractCommonList) authRefList;
        
        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        
        List<AuthorityRefList.AuthorityRefItem> list = authRefList.getAuthorityRefItem();
        DocumentModel docModel = docWrapper.getWrappedObject();

        try {
        	int iFirstToUse = (int)(pageSize*pageNum);
        	int nFoundInPage = 0;
        	int nFoundTotal = 0;
            for (String authRefFieldName : authRefFieldNames) {

                // FIXME: Can use the schema to validate field existence,
                // to help avoid encountering PropertyExceptions.
                String schemaName = DocumentUtils.getSchemaNamePart(authRefFieldName);
                Schema schema = DocumentUtils.getSchemaFromName(schemaName);

                String descendantAuthRefFieldName = DocumentUtils.getDescendantAuthRefFieldName(authRefFieldName);
                if (descendantAuthRefFieldName != null && !descendantAuthRefFieldName.trim().isEmpty()) {
                    authRefFieldName = DocumentUtils.getAncestorAuthRefFieldName(authRefFieldName);
                }

                String xpath = "//" + authRefFieldName;
                Property prop = docModel.getProperty(xpath);
                if (prop == null) {
                    continue;
                }

                // If this is a single scalar field, with no children,
                // add an item with its values to the authRefs list.
                if (DocumentUtils.isSimpleType(prop)) {
                	String refName = prop.getValue(String.class);
                    if (refName == null) {
                        continue;
                    }
                    refName = refName.trim();
                    if (refName.isEmpty()) {
                        continue;
                    }
                	if((nFoundTotal < iFirstToUse)
                		|| (nFoundInPage >= pageSize)) {
                		nFoundTotal++;
                		continue;
                	}
            		nFoundTotal++;
            		nFoundInPage++;
            		appendToAuthRefsList(refName, schemaName, authRefFieldName, list);

                    // Otherwise, if this field has children, cycle through each child.
                    //
                    // Whenever we find instances of the descendant field among
                    // these children, add an item with its values to the authRefs list.
                    //
                    // FIXME: When we increase maximum repeatability depth, that is, the depth
                    // between ancestor and descendant, we'll need to use recursion here,
                    // rather than making fixed assumptions about hierarchical depth.
                } else if ((DocumentUtils.isListType(prop) || DocumentUtils.isComplexType(prop))
                        && prop.size() > 0) {
                    
                    Collection<Property> childProp = prop.getChildren();
                    for (Property cProp : childProp) {
                        if (DocumentUtils.isSimpleType(cProp) && cProp.getName().equals(descendantAuthRefFieldName)) {
                        	String refName = cProp.getValue(String.class);
                            if (refName == null) {
                                continue;
                            }
                            refName = refName.trim();
                            if (refName.isEmpty()) {
                                continue;
                            }
                        	if((nFoundTotal < iFirstToUse)
                            		|| (nFoundInPage >= pageSize)) {
                        		nFoundTotal++;
                        		continue;
                        	}
                    		nFoundTotal++;
                    		nFoundInPage++;
                            appendToAuthRefsList(refName, schemaName, descendantAuthRefFieldName, list);
                        } else if ((DocumentUtils.isListType(cProp) || DocumentUtils.isComplexType(cProp))
                            && prop.size() > 0) {
                            Collection<Property> grandChildProp = cProp.getChildren();
                            for (Property gProp : grandChildProp) {
                                if (DocumentUtils.isSimpleType(gProp) && gProp.getName().equals(descendantAuthRefFieldName)) {
                                	String refName = gProp.getValue(String.class);
                                    if (refName == null) {
                                        continue;
                                    }
                                    refName = refName.trim();
                                    if (refName.isEmpty()) {
                                        continue;
                                    }
                                	if((nFoundTotal < iFirstToUse)
                                    		|| (nFoundInPage >= pageSize)) {
                                		nFoundTotal++;
                                		continue;
                                	}
                            		nFoundTotal++;
                            		nFoundInPage++;
                                    appendToAuthRefsList(refName, schemaName, descendantAuthRefFieldName, list);
                                }
                            }
                        }
                    }
                }
            }
            // Set num of items in list. this is useful to our testing framework.
            commonList.setItemsInPage(nFoundInPage);
            // set the total result size
            commonList.setTotalItems(nFoundTotal);
            
        } catch (PropertyException pe) {
            String msg = "Attempted to retrieve value for invalid or missing authority field. "
                    + "Check authority field properties in tenant bindings.";
            logger.warn(msg, pe);
            throw pe;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Failed to retrieve authority references").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        return authRefList;
    }

    private void appendToAuthRefsList(String refName, String schemaName,
            String fieldName, List<AuthorityRefList.AuthorityRefItem> list)
            throws Exception {
        if (DocumentUtils.getSchemaNamePart(fieldName).isEmpty()) {
            fieldName = DocumentUtils.appendSchemaName(schemaName, fieldName);
        }
        list.add(authorityRefListItem(fieldName, refName));
    }

    private AuthorityRefList.AuthorityRefItem authorityRefListItem(String authRefFieldName, String refName) {

        AuthorityRefList.AuthorityRefItem ilistItem = new AuthorityRefList.AuthorityRefItem();
        try {
            RefNameUtils.AuthorityTermInfo termInfo = RefNameUtils.parseAuthorityTermInfo(refName);
            ilistItem.setRefName(refName);
            ilistItem.setAuthDisplayName(termInfo.inAuthority.displayName);
            ilistItem.setItemDisplayName(termInfo.displayName);
            ilistItem.setSourceField(authRefFieldName);
            ilistItem.setUri(termInfo.getRelativeUri());
        } catch (Exception e) {
            // Do nothing upon encountering an Exception here.
        }
        return ilistItem;
    }

    /**
     * Returns the primary value from a list of values.
     *
     * Assumes that the first value is the primary value.
     * This assumption may change when and if the primary value
     * is identified explicitly.
     *
     * @param values a list of values.
     * @param propertyName the name of a property through
     *     which the value can be extracted.
     * @return the primary value.
    protected String primaryValueFromMultivalue(List<Object> values, String propertyName) {
        String primaryValue = "";
        if (values == null || values.size() == 0) {
            return primaryValue;
        }
        Object value = values.get(0);
        if (value instanceof String) {
            if (value != null) {
                primaryValue = (String) value;
            }
       // Multivalue group of fields
       } else if (value instanceof Map) {
            if (value != null) {
                Map map = (Map) value;
                if (map.values().size() > 0) {
                    if (map.get(propertyName) != null) {
                      primaryValue = (String) map.get(propertyName);
                    }
                }
            }
       } else {
            logger.warn("Unexpected type for property " + propertyName
                    + " in multivalue list: not String or Map.");
       }
       return primaryValue;
    }
     */

    /**
     * Gets a simple property from the document.
     *
     * For completeness, as this duplicates DocumentModel method. 
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param propertyName The simple scalar property type
     * @return property value as String
     */
    protected String getSimpleStringProperty(DocumentModel docModel, String schema, String propName) {
    	String xpath = "/"+schema+":"+propName;
    	try {
	    	return (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Not a simple String property?"
    				+pe.getLocalizedMessage());
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a scalar String property?"
    				+cce.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    }

    /**
     * Gets first of a repeating list of scalar values, as a String, from the document.
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param listName The name of the scalar list property
     * @return first value in list, as a String, or empty string if the list is empty
     */
    protected String getFirstRepeatingStringProperty(
    		DocumentModel docModel, String schema, String listName) {
    	String xpath = "/"+schema+":"+listName+"/[0]";
    	try {
	    	return (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Not a repeating scalar?"
    				+pe.getLocalizedMessage());
    	} catch(IndexOutOfBoundsException ioobe) {
    		// Nuxeo sometimes handles missing sub, and sometimes does not. Odd.
    		return "";	// gracefully handle missing elements
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a repeating String property?"
    				+cce.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    }
   

    /**
     * Gets first of a repeating list of scalar values, as a String, from the document.
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param listName The name of the scalar list property
     * @return first value in list, as a String, or empty string if the list is empty
     */
    protected String getStringValueInPrimaryRepeatingComplexProperty(
    		DocumentModel docModel, String schema, String complexPropertyName, String fieldName) {
    	String xpath = "/"+schema+":"+complexPropertyName+"/[0]/"+fieldName;
    	try {
	    	return (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Bad propertyNames?"
    				+pe.getLocalizedMessage());
    	} catch(IndexOutOfBoundsException ioobe) {
    		// Nuxeo sometimes handles missing sub, and sometimes does not. Odd.
    		return "";	// gracefully handle missing elements
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a String property?"
    				+cce.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    }
   
    /**
     * Gets XPath value from schema. Note that only "/" and "[n]" are
     * supported for xpath. Can omit grouping elements for repeating complex types, 
     * e.g., "fieldList/[0]" can be used as shorthand for "fieldList/field[0]" and
     * "fieldGroupList/[0]/field" can be used as shorthand for "fieldGroupList/fieldGroup[0]/field".
     * If there are no entries for a list of scalars or for a list of complex types, 
     * a 0 index expression (e.g., "fieldGroupList/[0]/field") will safely return an empty
     * string. A non-zero index will throw an IndexOutOfBoundsException if there are not
     * that many elements in the list. 
     * N.B.: This does not follow the XPath spec - indices are 0-based, not 1-based.
     *
     * @param docModel The document model to get info from
     * @param schema The name of the schema (part)
     * @param xpath The XPath expression (without schema prefix)
     * @return value the indicated property value as a String
     */
    protected static String getXPathStringValue(DocumentModel docModel, String schema, String xpath) {
    	xpath = schema+":"+xpath;
    	try {
	    	return (String)docModel.getPropertyValue(xpath);
    	} catch(PropertyException pe) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}. Bad XPath spec?"
    				+pe.getLocalizedMessage());
    	} catch(ClassCastException cce) {
    		throw new RuntimeException("Problem retrieving property {"+xpath+"} as String. Not a String property?"
    				+cce.getLocalizedMessage());
    	} catch(IndexOutOfBoundsException ioobe) {
    		// Nuxeo seems to handle foo/[0]/bar when it is missing,
    		// but not foo/bar[0] (for repeating scalars).
    		if(xpath.endsWith("[0]")) { 		// gracefully handle missing elements
    			return "";
    		} else {
    			String msg = ioobe.getMessage();
    			if(msg!=null && msg.equals("Index: 0, Size: 0")) {
    				// Some other variant on a missing sub-field; quietly absorb.
    				return "";
    			} // Otherwise, e.g., for true OOB indices, propagate the exception.
    		}
    		throw new RuntimeException("Problem retrieving property {"+xpath+"}:"
    				+ioobe.getLocalizedMessage());
    	} catch(Exception e) {
    		throw new RuntimeException("Unknown problem retrieving property {"+xpath+"}."
    				+e.getLocalizedMessage());
    	}
    }
   
}
