/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.client.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.MediaClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.AbstractCommonListUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.media.LanguageList;
import org.collectionspace.services.media.MediaCommon;
import org.collectionspace.services.media.SubjectList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MediaServiceTest, carries out tests against a deployed and running Media Service. <p/>
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class MediaServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = MediaServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(MediaServiceTest.class);
    private String knownResourceId = null;

    private boolean mediaCleanup = true;
    
    /**
     * Sets up create tests.
     */
    @Override
	protected void setupCreate() {
        super.setupCreate();
        String noMediaCleanup = System.getProperty(NO_MEDIA_CLEANUP);
    	if(Boolean.TRUE.toString().equalsIgnoreCase(noMediaCleanup)) {
    		//
    		// Don't delete the blobs that we created during the test cycle
    		//
            this.mediaCleanup = false;
    	}
    }
    
    private boolean isMediaCleanup() {
    	return mediaCleanup;
    }

    
    @Override
	public String getServicePathComponent() {
		return MediaClient.SERVICE_PATH_COMPONENT;
	}

	@Override
	protected String getServiceName() {
		return MediaClient.SERVICE_NAME;
	}
    
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new MediaClient();
    }

    @Override
    protected AbstractCommonList getAbstractCommonList(ClientResponse<AbstractCommonList> response) {
        return response.getEntity(AbstractCommonList.class);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupCreate();
        MediaClient client = new MediaClient();
        PoxPayloadOut multipart = createMediaInstance(createIdentifier());
        ClientResponse<Response> res = client.create(multipart);
        assertStatusCode(res, testName);
        if (knownResourceId == null) {
            knownResourceId = extractId(res);  // Store the ID returned from the first resource created for additional tests below.
            logger.debug(testName + ": knownResourceId=" + knownResourceId);
        }
        allResourceIdsCreated.add(extractId(res)); // Store the IDs from every resource created by tests so they can be deleted after tests have been run.
    }

    /**
     * Looks in the .../src/test/resources/blobs directory for files from which to create Blob
     * instances.
     *
     * @param testName the test name
     * @param fromUri - if 'true' then send the service a URI from which to create the blob.
     * @param fromUri - if 'false' then send the service a multipart/form-data POST from which to create the blob.
     * @throws Exception the exception
     */    
    public void createBlob(String testName, boolean fromUri) throws Exception {
        setupCreate();
        MediaClient client = new MediaClient();
        PoxPayloadOut multipart = createMediaInstance(createIdentifier());
        ClientResponse<Response> mediaRes = client.create(multipart);
        assertStatusCode(mediaRes, testName);
        String mediaCsid = extractId(mediaRes);
        
        String currentDir = this.getResourceDir();
        String blobsDirPath = currentDir + File.separator + BLOBS_DIR;
        File blobsDir = new File(blobsDirPath);
        if (blobsDir != null && blobsDir.exists()) {
	        File[] children = blobsDir.listFiles();
	        if (children != null && children.length > 0) {
	        	File blobFile = null;
	        	//
	        	// Since Media records can have only a single associated blob,
	        	// we'll stop after we find a valid candidate
	        	//
	        	for (File child : children) {
	        		if (isBlobbable(child) == true) {
	        			blobFile = child;
	        			break;
	        		}
	        	}
	        	//
	        	// If we found a good blob candidate file, then try to create the blob record
	        	//
	        	if (blobFile != null) {
	        		ClientResponse<Response> res = null;
	        		String mimeType = this.getMimeType(blobFile);
	        		logger.debug("Processing file URI: " + blobFile.getAbsolutePath());
	        		logger.debug("MIME type is: " + mimeType);
	        		if (fromUri == true) {
	        			URL childUrl = blobFile.toURI().toURL();
	        			res = client.createBlobFromUri(mediaCsid, childUrl.toString());
	        		} else {
			            MultipartFormDataOutput formData = new MultipartFormDataOutput();
			            OutputPart outputPart = formData.addFormData("file", blobFile, MediaType.valueOf(mimeType));
			            res = client.createBlobFromFormData(mediaCsid, formData);
	        		}
		            assertStatusCode(res, testName);
		            if (isMediaCleanup() == true) {
		            	allResourceIdsCreated.add(extractId(res));
		            	allResourceIdsCreated.add(mediaCsid);
		            }
	        	} else {
	        		logger.debug("Directory: " + blobsDirPath + " contains no readable files.");
	        	}
	        } else {
	        	logger.debug("Directory: " + blobsDirPath + " is empty or cannot be read.");
	        }
        } else {
        	logger.debug("Directory: " + blobsDirPath + " is missing or cannot be read.");
        }        
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, 
    		dependsOnMethods = {"create"})
    public void createWithBlobUri(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        createBlob(testName, true /*with URI*/);
    }
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, 
    		dependsOnMethods = {"createWithBlobUri"})
    public void createWithBlobPost(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        createBlob(testName, false /*with POST*/);
    }
    
//    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update"})
//    public void updateWithBlob(String testName) throws Exception {
//        logger.debug(testBanner(testName, CLASS_NAME));
//        setupCreate();
//        MediaClient client = new MediaClient();
//        PoxPayloadOut multipart = createMediaInstance(createIdentifier());
//        ClientResponse<Response> res = client.create(multipart);
//        assertStatusCode(res, testName);
//        String csid = extractId(res);
//        
//        
//        allResourceIdsCreated.add(extractId(res)); // Store the IDs from every resource created by tests so they can be deleted after tests have been run.
//    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        for (int i = 0; i < 3; i++) {
            create(testName);
        }
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupRead();
        MediaClient client = new MediaClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        MediaCommon media = (MediaCommon) extractPart(input, client.getCommonPartName(), MediaCommon.class);
        Assert.assertNotNull(media);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadList();
        MediaClient client = new MediaClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = res.getEntity();
        assertStatusCode(res, testName);
        // Optionally output additional data about list members for debugging.
        if(logger.isTraceEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdate();
        MediaClient client = new MediaClient();
        ClientResponse<String> res = client.read(knownResourceId);
        assertStatusCode(res, testName);
        logger.debug("got object to update with ID: " + knownResourceId);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());;
        MediaCommon media = (MediaCommon) extractPart(input, client.getCommonPartName(), MediaCommon.class);
        Assert.assertNotNull(media);

        media.setTitle("updated-" + media.getTitle());
        logger.debug("Object to be updated:"+objectAsXmlString(media, MediaCommon.class));
        PoxPayloadOut output = new PoxPayloadOut(MediaClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(media, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        assertStatusCode(res, testName);
        input = new PoxPayloadIn(res.getEntity());
        MediaCommon updatedMedia = (MediaCommon) extractPart(input, client.getCommonPartName(), MediaCommon.class);
        Assert.assertNotNull(updatedMedia);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupUpdateNonExistent();
        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        MediaClient client = new MediaClient();
        PoxPayloadOut multipart = createMediaInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        assertStatusCode(res, testName);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDelete();
        MediaClient client = new MediaClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        assertStatusCode(res, testName);
    }

    // ---------------------------------------------------------------
    // Failure outcome tests : means we expect response to fail, but test to succeed
    // ---------------------------------------------------------------

    // Failure outcome
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupReadNonExistent();
        MediaClient client = new MediaClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcome
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {
        logger.debug(testBanner(testName, CLASS_NAME));
        setupDeleteNonExistent();
        MediaClient client = new MediaClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        assertStatusCode(res, testName);
    }

    // Failure outcomes
    // Placeholders until the tests below can be implemented. See Issue CSPACE-401.

    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode(); // Expected status code: 200 OK
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);
        logger.debug("testSubmitRequest: url=" + url + " status=" + statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS);
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	return createMediaInstance(identifier);
    }
    
    
    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    private PoxPayloadOut createMediaInstance(String title) {
        String identifier = "media.title-" + title;
        MediaCommon media = new MediaCommon();
        media.setTitle(identifier);
        media.setContributor("Joe-bob briggs");
        media.setCoverage("Lots of stuff");
        media.setPublisher("Ludicrum Enterprises");
        SubjectList subjects = new SubjectList();
        List<String> subjList = subjects.getSubject();
        subjList.add("Pints of blood");
        subjList.add("Much skin");
        media.setSubjectList(subjects);
        LanguageList languages = new LanguageList();
        List<String> langList = languages.getLanguage();
        langList.add("English");
        langList.add("German");
        media.setLanguageList(languages);
        PoxPayloadOut multipart = new PoxPayloadOut(MediaClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(media, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new MediaClient().getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, media common");
            logger.debug(objectAsXmlString(media, MediaCommon.class));
        }

        return multipart;
    }
}
