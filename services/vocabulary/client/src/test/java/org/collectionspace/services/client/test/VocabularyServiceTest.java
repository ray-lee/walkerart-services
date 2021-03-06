/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.common.AbstractCommonListUtils;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.client.VocabularyClientUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * VocabularyServiceTest, carries out tests against a
 * deployed and running Vocabulary Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class VocabularyServiceTest extends AbstractServiceTestImpl {

    private final String CLASS_NAME = VocabularyServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    final String SERVICE_PATH_COMPONENT = VocabularyClient.SERVICE_PATH_COMPONENT;//"vocabularies";
    final String SERVICE_PAYLOAD_NAME = VocabularyClient.SERVICE_PAYLOAD_NAME;
    final String SERVICE_ITEM_PAYLOAD_NAME = VocabularyClient.SERVICE_ITEM_PAYLOAD_NAME;
    private String knownResourceId = null;
    private String knownResourceShortIdentifer = null;
    private String knownResourceRefName = null;
    private String knownResourceFullRefName = null;
    private String knownItemResourceId = null;
    private int nItemsToCreateInList = 5;
//    private List<String> allResourceIdsCreated = new ArrayList<String>();
//    private Map<String, String> allResourceItemIdsCreated =
//            new HashMap<String, String>();

    protected void setKnownResource(String id, String shortIdentifer,
            String refName, String fullRefName) {
        knownResourceId = id;
        knownResourceShortIdentifer = shortIdentifer;
        knownResourceRefName = refName;
        knownResourceFullRefName = fullRefName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new VocabularyClient();
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	VocabularyClient client = new VocabularyClient();
        String displayName = "displayName-" + identifier;
        PoxPayloadOut multipart = VocabularyClientUtils.createEnumerationInstance(
                displayName, identifier, client.getCommonPartName());
        return multipart;
    }    
    
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        String identifier = createIdentifier();
        String displayName = "displayName-" + identifier;
        PoxPayloadOut multipart = VocabularyClientUtils.createEnumerationInstance(
                displayName, identifier, client.getCommonPartName());
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null) {
            setKnownResource(extractId(res), identifier,
                    VocabularyClientUtils.createVocabularyRefName(identifier, null),
                    VocabularyClientUtils.createVocabularyRefName(identifier, displayName));
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(extractId(res));

    }

    @Override
    protected PoxPayloadOut createItemInstance(String parentCsid, String identifier) {
    	String headerLabel = new VocabularyClient().getItemCommonPartName();
        HashMap<String, String> vocabItemInfo = new HashMap<String, String>();
        String shortId = createIdentifier();
        vocabItemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, shortId);
        vocabItemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "display-" + shortId);

    	return VocabularyClientUtils.createVocabularyItemInstance(identifier, vocabItemInfo, headerLabel);
    }    
    
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createItem(String testName) {

        if (null != testName && logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupCreate();

        VocabularyClient client = new VocabularyClient();
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        String shortId = createIdentifier();
        itemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, shortId);
        itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "display-" + shortId);
        String newID = VocabularyClientUtils.createItemInVocabulary(knownResourceId,
                knownResourceRefName, itemInfo, client);

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            knownItemResourceId = newID;
            if (null != testName && logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + knownItemResourceId);
            }
        }
        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allResourceItemIdsCreated.put(newID, knownResourceId);
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create", "createItem", "readItem"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < 3; i++) {
            // Force create to reset the known resource info
            setKnownResource(null, null, null, null);
            knownItemResourceId = null;
            create(testName);
            // Add nItemsToCreateInList items to each vocab
            for (int j = 0; j < nItemsToCreateInList; j++) {
                createItem(null);
            }
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createWithBadShortId(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_BAD_REQUEST, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        PoxPayloadOut multipart = VocabularyClientUtils.createEnumerationInstance(
                "Vocab with Bad Short Id", "Bad Short Id!", client.getCommonPartName());
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createItem"})
    public void createItemWithBadShortId(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        setupCreateWithMalformedXml();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        itemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, "Bad Item Short Id!");
        itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "Bad Item!");
        PoxPayloadOut multipart =
                VocabularyClientUtils.createVocabularyItemInstance(knownResourceRefName,
                itemInfo, client.getCommonPartItemName());
        ClientResponse<Response> res = client.createItem(knownResourceId, multipart);

        int statusCode = res.getStatus();

        if (!REQUEST_TYPE.isValidStatusCode(statusCode)) {
            throw new RuntimeException("Could not create Item: \"" + itemInfo.get(AuthorityItemJAXBSchema.DISPLAY_NAME)
                    + "\" in personAuthority: \"" + knownResourceRefName
                    + "\" " + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        }
        if (statusCode != EXPECTED_STATUS_CODE) {
            throw new RuntimeException("Unexpected Status when creating Item: \"" + itemInfo.get(AuthorityItemJAXBSchema.DISPLAY_NAME)
                    + "\" in personAuthority: \"" + knownResourceRefName + "\", Status:" + statusCode);
        }
    }

    /*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupCreateWithEmptyEntityBody(testName, CLASS_NAME);

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = "";
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()) {
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupCreateWithMalformedXml();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA; // Constant from base class.
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithWrongXmlSchema(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupCreateWithWrongXmlSchema();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getServiceRootURL();
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
     */
    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
                client.getCommonPartName(), VocabulariesCommon.class);

        Assert.assertNotNull(vocabulary);
        Assert.assertEquals(vocabulary.getRefName(), knownResourceFullRefName);
    }

    /**
     * Read by name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void readByName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.readByName(knownResourceShortIdentifer);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
                client.getCommonPartName(), VocabulariesCommon.class);

        Assert.assertNotNull(vocabulary);
    }

    /*
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"read"})
    public void readByName(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupRead();

    // Submit the request to the service and store the response.
    ClientResponse<PoxPayloadIn> res = client.read(knownResourceId);
    int statusCode = res.getStatus();

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": status = " + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    //FIXME: remove the following try catch once Aron fixes signatures
    try {
    PoxPayloadIn input = (PoxPayloadIn) res.getEntity();
    VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
    client.getCommonPartName(), VocabulariesCommon.class);
    Assert.assertNotNull(vocabulary);
    } catch (Exception e) {
    throw new RuntimeException(e);
    }
    }
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createItem", "read"})
    public void readItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Check whether we've received a vocabulary item.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabularyitemsCommon vocabularyItem = (VocabularyitemsCommon) extractPart(input,
                client.getCommonPartItemName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(vocabularyItem);
        Assert.assertEquals(vocabularyItem.getInAuthority(), knownResourceId);
    }

    // Failure outcomes
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"updateItem"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        testSetup(STATUS_BAD_REQUEST, ServiceRequestType.UPDATE);
        // setupUpdateWithWrongXmlSchema(testName);

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());

        // Check whether Person has expected displayName.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabularyitemsCommon vitem = (VocabularyitemsCommon) extractPart(input,
                client.getCommonPartItemName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(vitem);
        // Try to Update with null displayName
        vitem.setDisplayName(null);

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(vitem, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartItemName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE,
                "Expecting invalid message because of null displayName.");

        // Now try to Update with 1-char displayName (too short)
        vitem.setDisplayName("a");

        // Submit the updated resource to the service and store the response.
        output = new PoxPayloadOut(SERVICE_ITEM_PAYLOAD_NAME);
        commonPart = output.addPart(vitem, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartItemName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE,
                "Expecting invalid message because of 1-char displayName.");
    }

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"readItem", "readNonExistent"})
    public void readItemNonExistent(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        AbstractCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        if(logger.isTraceEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "readItem"})
    public void readItemList(String testName) {
        readItemListInt(knownResourceId, null, testName);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createList", "readItem"})
    public void readItemListByName(String testName) {
        readItemListInt(null, knownResourceShortIdentifer, testName);
    }

    private void readItemListInt(String vcsid, String shortId, String testName) {

        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<AbstractCommonList> res = null;
        if (vcsid != null) {
            res = client.readItemList(vcsid, null, null);
        } else if (shortId != null) {
            res = client.readItemListForNamedAuthority(shortId, null, null);
        } else {
            Assert.fail("Internal Error: readItemList both vcsid and shortId are null!");
        }
        AbstractCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("  " + testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        List<AbstractCommonList.ListItem> items = list.getListItem();
        int nItemsReturned = items.size();
        long nItemsTotal = list.getTotalItems();
        if (logger.isDebugEnabled()) {
            logger.debug("  " + testName + ": Expected "
                    + nItemsToCreateInList + " items; got: " + nItemsReturned + " of: " + nItemsTotal);
        }
        Assert.assertEquals(nItemsTotal, nItemsToCreateInList);

        if(logger.isTraceEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
        }
    }

    // Failure outcomes
    // None at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res =
                client.read(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug("got Vocabulary to update with ID: " + knownResourceId);
        }
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabulariesCommon vocabulary = (VocabulariesCommon) extractPart(input,
                client.getCommonPartName(), VocabulariesCommon.class);
        Assert.assertNotNull(vocabulary);

        // Update the contents of this resource.
        vocabulary.setDisplayName("updated-" + vocabulary.getDisplayName());
        vocabulary.setVocabType("updated-" + vocabulary.getVocabType());
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated Vocabulary");
            logger.debug(objectAsXmlString(vocabulary, VocabulariesCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(SERVICE_PAYLOAD_NAME);

        PayloadOutputPart commonPart = output.addPart(vocabulary, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("update: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = new PoxPayloadIn(res.getEntity());
        VocabulariesCommon updatedVocabulary =
                (VocabulariesCommon) extractPart(input,
                client.getCommonPartName(), VocabulariesCommon.class);
        Assert.assertNotNull(updatedVocabulary);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedVocabulary.getDisplayName(),
                vocabulary.getDisplayName(),
                "Data in updated object did not match submitted data.");
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"readItem", "update", "verifyIgnoredUpdateWithInAuthority"})
    public void updateItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res =
                client.readItem(knownResourceId, knownItemResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

        if (logger.isDebugEnabled()) {
            logger.debug("got VocabularyItem to update with ID: "
                    + knownItemResourceId
                    + " in Vocab: " + knownResourceId);
        }
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabularyitemsCommon vocabularyItem = (VocabularyitemsCommon) extractPart(input,
                client.getCommonPartItemName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(vocabularyItem);

        // Update the contents of this resource.
        vocabularyItem.setDisplayName("updated-" + vocabularyItem.getDisplayName());
        if (logger.isDebugEnabled()) {
            logger.debug("to be updated VocabularyItem");
            logger.debug(objectAsXmlString(vocabularyItem,
                    VocabularyitemsCommon.class));
        }

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(vocabularyItem, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartItemName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("updateItem: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that its contents exist.
        input = new PoxPayloadIn(res.getEntity());
        VocabularyitemsCommon updatedVocabularyItem =
                (VocabularyitemsCommon) extractPart(input,
                client.getCommonPartItemName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(updatedVocabularyItem);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedVocabularyItem.getDisplayName(),
                vocabularyItem.getDisplayName(),
                "Data in updated VocabularyItem did not match submitted data.");
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"readItem"})
    public void verifyIgnoredUpdateWithInAuthority(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " read Vocab:" + knownResourceId + "/Item:"
                    + knownItemResourceId + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());

        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        VocabularyitemsCommon vitem = (VocabularyitemsCommon) extractPart(input,
                client.getCommonPartItemName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(vitem);
        // Try to Update with new parent vocab (use self, for test).
        Assert.assertEquals(vitem.getInAuthority(),
                knownResourceId,
                "VocabularyItem inAuthority does not match knownResourceId.");
        vitem.setInAuthority(knownItemResourceId);

        // Submit the updated resource to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(vitem, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartItemName());
        res = client.updateItem(knownResourceId, knownItemResourceId, output);
        statusCode = res.getStatus();

        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Retrieve the updated resource and verify that the parent did not change
        res = client.readItem(knownResourceId, knownItemResourceId);
        input = new PoxPayloadIn(res.getEntity());
        VocabularyitemsCommon updatedVocabularyItem =
                (VocabularyitemsCommon) extractPart(input,
                client.getCommonPartItemName(), VocabularyitemsCommon.class);
        Assert.assertNotNull(updatedVocabularyItem);

        // Verify that the updated resource received the correct data.
        Assert.assertEquals(updatedVocabularyItem.getInAuthority(),
                knownResourceId,
                "VocabularyItem allowed update to the parent (inAuthority).");
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }

    /*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithEmptyEntityBody(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupUpdateWithEmptyEntityBody();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = "";
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithMalformedXml(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupUpdateWithMalformedXml();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = MALFORMED_XML_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug(testName + ": url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema(String testName) throws Exception {

    if (logger.isDebugEnabled()) {
    logger.debug(testBanner(testName, CLASS_NAME));
    }
    // Perform setup.
    setupUpdateWithWrongXmlSchema();

    // Submit the request to the service and store the response.
    String method = REQUEST_TYPE.httpMethodName();
    String url = getResourceURL(knownResourceId);
    String mediaType = MediaType.APPLICATION_XML;
    final String entity = WRONG_XML_SCHEMA_DATA;
    int statusCode = submitRequest(method, url, mediaType, entity);

    // Check the status code of the response: does it match
    // the expected response(s)?
    if(logger.isDebugEnabled()){
    logger.debug("updateWithWrongXmlSchema: url=" + url +
    " status=" + statusCode);
    }
    Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        VocabularyClient client = new VocabularyClient();
        String displayName = "displayName-" + NON_EXISTENT_ID;
        PoxPayloadOut multipart = VocabularyClientUtils.createEnumerationInstance(
                displayName, NON_EXISTENT_ID, client.getCommonPartName());
        ClientResponse<String> res =
                client.update(NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"updateItem", "testItemSubmitRequest"})
    public void updateNonExistentItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        VocabularyClient client = new VocabularyClient();
        HashMap<String, String> itemInfo = new HashMap<String, String>();
        itemInfo.put(AuthorityItemJAXBSchema.SHORT_IDENTIFIER, "nonex");
        itemInfo.put(AuthorityItemJAXBSchema.DISPLAY_NAME, "display-nonex");
        PoxPayloadOut multipart =
                VocabularyClientUtils.createVocabularyItemInstance(
                VocabularyClientUtils.createVocabularyRefName(NON_EXISTENT_ID, null),
                itemInfo, client.getCommonPartItemName());
        ClientResponse<String> res =
                client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update", "deleteItem"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createItem", "readItemList", "testItemSubmitRequest",
        "updateItem", "verifyIllegalItemDisplayName", "verifyIgnoredUpdateWithInAuthority"})
    public void deleteItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<Response> res = client.deleteItem(knownResourceId, knownItemResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("delete: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // Failure outcomes
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        VocabularyClient client = new VocabularyClient();
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     */
    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() {

        // Expected status code: 200 OK
        setupRead();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }

    @Test(dependsOnMethods = {"createItem", "readItem", "testSubmitRequest"})
    public void testItemSubmitRequest() {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getItemResourceURL(knownResourceId, knownItemResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testItemSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    // ---------------------------------------------------------------
    // Cleanup of resources created during testing
    // ---------------------------------------------------------------
    /**
     * Deletes all resources created by tests, after all tests have been run.
     *
     * This cleanup method will always be run, even if one or more tests fail.
     * For this reason, it attempts to remove all resources created
     * at any point during testing, even if some of those resources
     * may be expected to be deleted by certain tests.
     */
//    @AfterClass(alwaysRun = true)
//    public void cleanUp() {
//        String noTest = System.getProperty("noTestCleanup");
//        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Skipping Cleanup phase ...");
//            }
//            return;
//        }
//        if (logger.isDebugEnabled()) {
//            logger.debug("Cleaning up temporary resources created for testing ...");
//        }
//        VocabularyClient client = new VocabularyClient();
//        String vocabularyResourceId;
//        String vocabularyItemResourceId;
//        // Clean up vocabulary item resources.
//        for (Map.Entry<String, String> entry : allResourceItemIdsCreated.entrySet()) {
//            vocabularyItemResourceId = entry.getKey();
//            vocabularyResourceId = entry.getValue();
//            // Note: Any non-success responses are ignored and not reported.
//            client.deleteItem(vocabularyResourceId, vocabularyItemResourceId).releaseConnection();
//        }
//        // Clean up vocabulary resources.
//        for (String resourceId : allResourceIdsCreated) {
//            // Note: Any non-success responses are ignored and not reported.
//            client.delete(resourceId).releaseConnection();
//        }
//
//    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    public String getServicePathItemsComponent() {
        return AuthorityClient.ITEMS;
    }

    /**
     * Returns the root URL for a service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning vocabulary, followed by the 
     * path component for the items.
     *
     * @return The root URL for a service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getServicePathItemsComponent();
    }

    /**
     * Returns the URL of a specific resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  resourceIdentifier  An identifier (such as a UUID) for a resource.
     *
     * @return The URL of a specific resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String resourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + resourceIdentifier;
    }

    @Override
    protected String getServiceName() {
        return VocabularyClient.SERVICE_NAME;
    }
}
