/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *\
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

import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.collectionspace.services.client.AbstractServiceClientImpl;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionObjectFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.BriefDescriptionList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionobjectsNaturalhistory;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.collectionobject.ResponsibleDepartmentList;
import org.collectionspace.services.collectionobject.DimensionGroup;
import org.collectionspace.services.collectionobject.DimensionList;
import org.collectionspace.services.collectionobject.ObjectNameGroup;
import org.collectionspace.services.collectionobject.ObjectNameList;
import org.collectionspace.services.collectionobject.OtherNumber;
import org.collectionspace.services.collectionobject.OtherNumberList;
import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;

import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectServiceTest, carries out tests against a
 * deployed and running CollectionObject Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class CollectionObjectServiceTest extends AbstractServiceTestImpl {

    /** The logger. */
    private final String CLASS_NAME = CollectionObjectServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    // Instance variables specific to this test.
    /** The known resource id. */
    private String knownResourceId = null;

    private final String OBJECT_NAME_VALUE = "an object name";
    private final String UPDATED_MEASURED_PART_VALUE = "updated measured part value";
    private final String UTF8_DATA_SAMPLE = "Audiorecording album cover signed by Lech "
            + "Wa" + '\u0142' + '\u0119' + "sa";

//    /* (non-Javadoc)
//     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
//     */
//    @Override
//    protected String getServicePathComponent() {
//        return new CollectionObjectClient().getServicePathComponent();
//    }

	@Override
	protected String getServiceName() {
		return CollectionObjectClient.SERVICE_NAME;
	}
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new CollectionObjectClient();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response) {
        return response.getEntity(CollectionobjectsCommonList.class);
    }
 
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
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
        CollectionObjectClient client = new CollectionObjectClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart =
                createCollectionObjectInstance(client.getCommonPartName(), identifier);
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
            knownResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }

        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(extractId(res));
    }


    /*
     * Tests to diagnose and verify the fixed status of CSPACE-1026,
     * "Whitespace at certain points in payload cause failure"
     */
    /**
     * Creates the from xml cambridge.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlCambridge(String testName) throws Exception {
        String newId =
            createFromXmlFile(testName, "./test-data/testCambridge.xml", true);
        testSubmitRequest(newId);
    }

   /*
    * Tests to diagnose and fix CSPACE-2242.
    *
    * This is a bug identified in release 0.8 in which value instances of a
    * repeatable field are not stored when the first value instance of that
    * field is blank.
    */

    // Verify that record creation occurs successfully when the first value instance
    // of a single, repeatable String scalar field is non-blank.
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"}, groups = {"cspace2242group"})
    public void createFromXmlNonBlankFirstValueInstance(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        String newId =
            createFromXmlFile(testName, "./test-data/cspace-2242-first-value-instance-nonblank.xml", true);
        CollectionobjectsCommon collectionObject = readCollectionObjectCommonPart(newId);
        // Verify that at least one value instance of the repeatable field was successfully persisted.
        BriefDescriptionList descriptionList = collectionObject.getBriefDescriptions();
        List<String> descriptions = descriptionList.getBriefDescription();
        Assert.assertTrue(descriptions.size() > 0);
    }

    // Verify that record creation occurs successfully when the first value instance
    // of a single, repeatable String scalar field is blank.
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"}, groups = {"cspace2242group"})
    public void createFromXmlBlankFirstValueInstance(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        String newId =
            createFromXmlFile(testName, "./test-data/cspace-2242-first-value-instance-blank.xml", true);
        CollectionobjectsCommon collectionObject = readCollectionObjectCommonPart(newId);
        // Verify that at least one value instance of the repeatable field was successfully persisted.
        BriefDescriptionList descriptionList = collectionObject.getBriefDescriptions();
        List<String> descriptions = descriptionList.getBriefDescription();
        Assert.assertTrue(descriptions.size() > 0);
    }

     // Verify that values are preserved when enclosed in double quote marks.
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"}, groups = {"cspace3237group"})
    public void doubleQuotesEnclosingFieldContents(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        String newId =
            createFromXmlFile(testName, "./test-data/cspace-3237-double-quotes.xml", true);
        CollectionobjectsCommon collectionObject = readCollectionObjectCommonPart(newId);

        Assert.assertTrue(collectionObject.getDistinguishingFeatures().matches("^\\\".+?\\\"$"));

        BriefDescriptionList descriptionList = collectionObject.getBriefDescriptions();
        List<String> descriptions = descriptionList.getBriefDescription();
        Assert.assertTrue(descriptions.size() > 0);
        Assert.assertNotNull(descriptions.get(0));
        Assert.assertTrue(descriptions.get(0).matches("^\\\".+?\\\"$"));

        if (logger.isDebugEnabled()) {
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }
    }

    /**
     * Creates the from xml rfw s1.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS1(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            //createFromXmlFile(testName, "./target/test-classes/test-data/repfield_whitesp1.xml", false);
        	createFromXmlFile(testName, testDataDir + "/repfield_whitesp1.xml", false);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s2.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS2(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            //createFromXmlFile(testName, "./target/test-classes/test-data/repfield_whitesp2.xml", false);
        	createFromXmlFile(testName, testDataDir + "/repfield_whitesp2.xml", false);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s3.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS3(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            //createFromXmlFile(testName, "./target/test-classes/test-data/repfield_whitesp3.xml", false);
        	createFromXmlFile(testName, testDataDir + "/repfield_whitesp3.xml", false);
        testSubmitRequest(newId);
    }

    /**
     * Creates the from xml rfw s4.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createFromXmlRFWS4(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
        String newId =
            createFromXmlFile(testName, testDataDir + "/repfield_whitesp4.xml", false);
        testSubmitRequest(newId);
    }

    /*
     * Tests to diagnose and verify the fixed status of CSPACE-1248,
     * "Wedged records created!" (i.e. records with child repeatable
     * fields, which contain null values, can be successfully created
     * but an error occurs on trying to retrieve those records).
     */

    /**
     * Creates a CollectionObject resource with a null value repeatable field.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithNullValueRepeatableField(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
    	String newId =
            createFromXmlFile(testName, testDataDir + "/repfield_null1.xml", false);
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully created record with null value repeatable field.");
            logger.debug("Attempting to retrieve just-created record ...");
        }
        testSubmitRequest(newId);
    }

    /**
     * Creates a CollectionObject resource, one of whose fields contains
     * non-Latin 1 Unicode UTF-8 characters.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "testSubmitRequest"}, groups={"utf8-create"})
    public void createWithUTF8Data(String testName) throws Exception {
    	String testDataDir = System.getProperty("test-data.fileName");
    	String newId =
            createFromXmlFile(testName, testDataDir + "/cspace-2779-utf-8-create.xml", false);
        if (logger.isDebugEnabled()) {
            logger.debug("Created record with UTF-8 chars in payload.");
            logger.debug("Attempting to retrieve just-created record ...");
        }
        CollectionobjectsCommon collectionObject = readCollectionObjectCommonPart(newId);
        String distinguishingFeatures = collectionObject.getDistinguishingFeatures();
        if (logger.isDebugEnabled()) {
            logger.debug("Sent distinguishingFeatures: " + UTF8_DATA_SAMPLE);
            logger.debug("Received distinguishingFeatures: " + distinguishingFeatures);
        }
        Assert.assertTrue(distinguishingFeatures.equals(UTF8_DATA_SAMPLE));
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#createList()
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
    	this.createPaginatedList(testName, DEFAULT_LIST_SIZE);
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithEmptyEntityBody(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

   /**
    * Test how the service handles XML that is not well formed,
    * when sent in the payload of a Create request.
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
    */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithMalformedXml(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithWrongXmlSchema(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }


/*
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTest.class,
    dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithEmptyEntityBody(String testName) throwsException {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupCreateWithEmptyEntityBody();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getServiceRootURL();
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

   /**
    * Test how the service handles, in a Create request, payloads
    * containing null values (or, in the case of String fields,
    * empty String values) in one or more fields which must be
    * present and are required to contain non-empty values.
    *
    * This is a test of code and/or configuration in the service's
    * validation routine(s).
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
    * @throws Exception 
    */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createWithRequiredValuesNullOrEmpty(String testName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        setupCreate();

        // Build a payload with invalid content, by omitting a
        // field (objectNumber) which must be present, and in which
        // a non-empty value is required, as enforced by the service's
        // validation routine(s).
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

        TitleGroupList titleGroupList = new TitleGroupList();
        List<TitleGroup> titleGroups = titleGroupList.getTitleGroup();
        TitleGroup titleGroup = new TitleGroup();
        titleGroup.setTitle("a title");
        titleGroups.add(titleGroup);
        collectionObject.setTitleGroupList(titleGroupList);

        ObjectNameList objNameList = new ObjectNameList();
        List<ObjectNameGroup> objNameGroups = objNameList.getObjectNameGroup();
        ObjectNameGroup objectNameGroup = new ObjectNameGroup();
        objectNameGroup.setObjectName("an object name");
        objNameGroups.add(objectNameGroup);
        collectionObject.setObjectNameList(objNameList);

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        PoxPayloadOut multipart =
                createCollectionObjectInstance(client.getCommonPartName(), collectionObject, null);
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        // Read the response and verify that the create attempt failed.
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

        // FIXME: Consider splitting off the following into its own test method.
        
        // Build a payload with invalid content, by setting a value to the
        // empty String, in a field (objectNumber) that requires a non-empty
        // value, as enforced by the service's validation routine(s).
        collectionObject = new CollectionobjectsCommon();
        collectionObject.setObjectNumber("");
        collectionObject.setDistinguishingFeatures("Distinguishing features.");

        objNameList = new ObjectNameList();
        objNameGroups = objNameList.getObjectNameGroup();
        objectNameGroup = new ObjectNameGroup();
        objectNameGroup.setObjectName(OBJECT_NAME_VALUE);
        objNameGroups.add(objectNameGroup);
        collectionObject.setObjectNameList(objNameList);

        // Submit the request to the service and store the response.
        multipart =
            createCollectionObjectInstance(client.getCommonPartName(), collectionObject, null);
        res = client.create(multipart);
        statusCode = res.getStatus();

        // Read the response and verify that the create attempt failed.
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

    }


    // ---------------------------------------------------------------
    // CRUD tests : READ tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
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
        CollectionObjectClient client = new CollectionObjectClient();
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

        // Extract the common part.
        CollectionobjectsCommon collectionobjectCommon = extractCommonPartValue(testName, res);

        // Verify the number and contents of values in repeatable fields,
        // as created in the instance record used for testing.
        DimensionList dimensionList = collectionobjectCommon.getDimensions();
        Assert.assertNotNull(dimensionList);
        List<DimensionGroup> dimensionsGroups = dimensionList.getDimensionGroup();
        Assert.assertNotNull(dimensionsGroups);
        Assert.assertTrue(dimensionsGroups.size() > 0);
        Assert.assertNotNull(dimensionsGroups.get(0));
        Assert.assertNotNull(dimensionsGroups.get(0).getMeasuredPart());

        /* No longer part of the "default" domain service tests for the CollectionObject record.
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": Reading Natural History part ...");
        }

        // Currently checking only that the natural history part is non-null;
        // can add specific field-level checks as warranted.
        Object conh = extractPartValue(testName, res, getNHPartName());
        Assert.assertNotNull(conh);
        */
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
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

    
    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */
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
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<CollectionobjectsCommonList> res = client.readList();
        CollectionobjectsCommonList list = res.getEntity();
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
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<CollectionobjectsCommonList.CollectionObjectListItem> items =
                    list.getCollectionObjectListItem();
            int i = 0;

            for (CollectionobjectsCommonList.CollectionObjectListItem item : items) {
                logger.debug(testName + ": list-item[" + i + "] csid="
                        + item.getCsid());
                logger.debug(testName + ": list-item[" + i + "] objectNumber="
                        + item.getObjectNumber());
                logger.debug(testName + ": list-item[" + i + "] URI="
                        + item.getUri());
                i++;

            }
        }
    }

    // Failure outcomes
    // None at present.
    // ---------------------------------------------------------------
    // CRUD tests : UPDATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#update(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        // Read an existing resource that will be updated.
        ClientResponse<String> res = updateRetrieve(testName, knownResourceId);

        // Extract its common part.
        CollectionobjectsCommon collectionObjectCommon = extractCommonPartValue(testName, res);

        // Change the content of one or more fields in the common part.

        collectionObjectCommon.setObjectNumber("updated-" + collectionObjectCommon.getObjectNumber());

        // Change the object name in the first value instance in the
        // object name repeatable group.
        ObjectNameList objNameList = collectionObjectCommon.getObjectNameList();
        List<ObjectNameGroup> objNameGroups = objNameList.getObjectNameGroup();
        Assert.assertNotNull(objNameGroups);
        Assert.assertTrue(objNameGroups.size() >= 1);
        String objectName = objNameGroups.get(0).getObjectName();
        Assert.assertEquals(objectName, OBJECT_NAME_VALUE);
        String updatedObjectName = "updated-" + objectName;
        objNameGroups.get(0).setObjectName(updatedObjectName);
        collectionObjectCommon.setObjectNameList(objNameList);

        // Replace the existing value instances in the dimensions repeatable group
        // with entirely new value instances, also changing the number of such instances.
        DimensionList dimensionList = collectionObjectCommon.getDimensions();
        Assert.assertNotNull(dimensionList);
        List<DimensionGroup> dimensionGroups = dimensionList.getDimensionGroup();
        Assert.assertNotNull(dimensionGroups);
        int originalDimensionGroupSize = dimensionGroups.size();
        Assert.assertTrue(originalDimensionGroupSize >= 1);

        DimensionGroup updatedDimensionGroup = new DimensionGroup();
        updatedDimensionGroup.setMeasuredPart(UPDATED_MEASURED_PART_VALUE);
        dimensionGroups.clear();
        dimensionGroups.add(updatedDimensionGroup);
        int updatedDimensionGroupSize = dimensionGroups.size();
        Assert.assertTrue(updatedDimensionGroupSize >= 1);
        Assert.assertTrue(updatedDimensionGroupSize != originalDimensionGroupSize);
        collectionObjectCommon.setDimensions(dimensionList);

        if (logger.isDebugEnabled()) {
            logger.debug("sparse update that will be sent in update request:");
            logger.debug(objectAsXmlString(collectionObjectCommon,
                    CollectionobjectsCommon.class));
        }

        // Send the changed resource to be updated and read the updated resource
        // from the response.
        res = updateSend(testName, knownResourceId, collectionObjectCommon);

        // Extract its common part.
        CollectionobjectsCommon updatedCollectionobjectCommon = extractCommonPartValue(testName, res);

        // Read the updated common part and verify that the resource was correctly updated.
        objNameList = updatedCollectionobjectCommon.getObjectNameList();
        Assert.assertNotNull(objNameList);
        objNameGroups = objNameList.getObjectNameGroup();
        Assert.assertNotNull(objNameGroups);
        Assert.assertTrue(objNameGroups.size() >= 1);
        Assert.assertEquals(updatedObjectName,
                objNameGroups.get(0).getObjectName(),
                "Data in updated object did not match submitted data.");
        
        dimensionList = updatedCollectionobjectCommon.getDimensions();
        Assert.assertNotNull(dimensionList);
        dimensionGroups = dimensionList.getDimensionGroup();
        Assert.assertNotNull(dimensionGroups);
        Assert.assertTrue(dimensionGroups.size() == updatedDimensionGroupSize);
        Assert.assertEquals(UPDATED_MEASURED_PART_VALUE,
                dimensionGroups.get(0).getMeasuredPart(),
                "Data in updated object did not match submitted data.");

    }

    /**
     * Update retrieve.
     *
     * @param testName the test name
     * @param id the id
     * @return the client response
     */
    private ClientResponse<String> updateRetrieve(String testName, String id) {
        setupRead();
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<String> res = client.read(knownResourceId);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        if(logger.isDebugEnabled()){
            logger.debug("got object to update with ID: " + knownResourceId);
        }
        return res;
    }

    /**
     * Update send.
     *
     * @param testName the test name
     * @param id the id
     * @return the client response
     */
    private ClientResponse<String> updateSend(String testName, String id,
            CollectionobjectsCommon collectionObjectCommon) {
        setupUpdate();
        PoxPayloadOut output = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(collectionObjectCommon, MediaType.APPLICATION_XML_TYPE);
        CollectionObjectClient client = new CollectionObjectClient();
        commonPart.setLabel(client.getCommonPartName());
        ClientResponse<String> res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();
        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": read status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        return res;
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

   /**
    * Test how the service handles XML that is not well formed,
    * when sent in the payload of an Update request.
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
    */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithMalformedXml(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    	//FIXME: Should this test really be empty?
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
    public void updateWithMalformedXml() throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateWithMalformedXml();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        final String entity = MALFORMED_XML_DATA;
        String mediaType = MediaType.APPLICATION_XML;
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
        logger.debug(testName + ": url=" + url +
        " status=" + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
        invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
*/

    /* (non-Javadoc)
 * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
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
        //
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in updateCollectionObject(), below.
        CollectionObjectClient client = new CollectionObjectClient();
        PoxPayloadOut multipart =
                createCollectionObjectInstance(client.getCommonPartName(), NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
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

   /**
    * Test how the service handles, in an Update request, payloads
    * containing null values (or, in the case of String fields,
    * empty String values) in one or more fields in which non-empty
    * values are required.
    *
    * This is a test of code and/or configuration in the service's
    * validation routine(s).
    *
    * @param testName  The name of this test method.  This name is supplied
    *     automatically, via reflection, by a TestNG 'data provider' in
    *     a base class.
 * @throws Exception 
    */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"read"})
    public void updateWithRequiredValuesNullOrEmpty(String testName) throws Exception {
  
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        // Read an existing record for updating.
        ClientResponse<String> res = updateRetrieve(testName, knownResourceId);

        // Extract its common part.
        CollectionobjectsCommon collectionObjectCommon = extractCommonPartValue(testName, res);

        // Update the common part with invalid content, by setting a value to
        // the empty String, in a field that requires a non-empty value,
        // as enforced by the service's validation routine(s).
        collectionObjectCommon.setObjectNumber("");

        if (logger.isDebugEnabled()) {
            logger.debug(testName + " updated object");
            logger.debug(objectAsXmlString(collectionObjectCommon,
                    CollectionobjectsCommon.class));
        }

        // Submit the request to the service and store the response.
        setupUpdate();
        PoxPayloadOut output = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = output.addPart(collectionObjectCommon, MediaType.APPLICATION_XML_TYPE);
        CollectionObjectClient client = new CollectionObjectClient();
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);
        int statusCode = res.getStatus();

        // Read the response and verify that the update attempt failed.
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, Response.Status.BAD_REQUEST.getStatusCode());

    }

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
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

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
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
        CollectionObjectClient client = new CollectionObjectClient();
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

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     * @throws Exception 
     */

    @Test(dependsOnMethods = {"create", "read"})
    public void testSubmitRequest() throws Exception {
        testSubmitRequest(knownResourceId);
    }

    /**
     * Test submit request.
     *
     * @param resourceId the resource id
     * @throws Exception the exception
     */
    private void testSubmitRequest(String resourceId) throws Exception {

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(resourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug("testSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createCollectionObjectInstance(String commonPartName,
            String identifier) {
        return createCollectionObjectInstance(commonPartName,
                "objectNumber-" + identifier,
                "objectName-" + identifier);
    }
    
    @Override
    protected PoxPayloadOut createInstance(String identifier) {
    	String commonPartName = CollectionObjectClient.SERVICE_COMMON_PART_NAME;
    	return createCollectionObjectInstance(commonPartName, identifier);
    }

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param objectNumber the object number
     * @param objectName the object name
     * @return the multipart output
     */
    private PoxPayloadOut createCollectionObjectInstance(String commonPartName,
            String objectNumber, String objectName) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

        //REM
        OtherNumber remNumber = new OtherNumber();
        remNumber.setNumberType("remNumber");
        remNumber.setNumberValue("2271966-" + System.currentTimeMillis());
        collectionObject.setRemNumber(remNumber);
        
        // Scalar fields
        collectionObject.setObjectNumber(objectNumber);
        collectionObject.setAge(""); //test for null string
        
        // FIXME this can be removed when the repeatable other number list
        // is supported by the application layers
        collectionObject.setOtherNumber("urn:org.walkerart.id:123");

        // Repeatable structured groups

        TitleGroupList titleGroupList = new TitleGroupList();
        List<TitleGroup> titleGroups = titleGroupList.getTitleGroup();
        TitleGroup titleGroup = new TitleGroup();
        titleGroup.setTitle("a title");
        titleGroups.add(titleGroup);
        collectionObject.setTitleGroupList(titleGroupList);

        ObjectNameList objNameList = new ObjectNameList();
        List<ObjectNameGroup> objNameGroups = objNameList.getObjectNameGroup();
        ObjectNameGroup objectNameGroup = new ObjectNameGroup();
        objectNameGroup.setObjectName(OBJECT_NAME_VALUE);
        objNameGroups.add(objectNameGroup);
        collectionObject.setObjectNameList(objNameList);

        DimensionList dimensionList = new DimensionList();
        List<DimensionGroup> dimensionGroups = dimensionList.getDimensionGroup();
        DimensionGroup dimensionGroup1 = new DimensionGroup();
        dimensionGroup1.setMeasuredPart("head");
        dimensionGroup1.setDimension("length");
        dimensionGroup1.setValue("30");
        dimensionGroup1.setMeasurementUnit("cm");
        DimensionGroup dimensionGroup2 = new DimensionGroup();
        dimensionGroup2.setMeasuredPart("leg");
        dimensionGroup2.setDimension("width");
        dimensionGroup2.setValue("2.57");
        dimensionGroup2.setMeasurementUnit("m");
        dimensionGroup2.setValueQualifier("");  // test null string
        dimensionGroups.add(dimensionGroup1);
        dimensionGroups.add(dimensionGroup2);
        collectionObject.setDimensions(dimensionList);

        // Repeatable scalar fields
        
        BriefDescriptionList descriptionList = new BriefDescriptionList();
        List<String> descriptions = descriptionList.getBriefDescription();
        descriptions.add("Papier mache bird cow mask with horns, "
                + "painted red with black and yellow spots. "
                + "Puerto Rico. ca. 8&quot; high, 6&quot; wide, projects 10&quot; (with horns).");
        descriptions.add("Acrylic rabbit mask with wings, "
                + "painted red with green and aquamarine spots. "
                + "Puerto Rico. ca. 8&quot; high, 6&quot; wide, projects 10&quot; (with wings).");
        collectionObject.setBriefDescriptions(descriptionList);

        ResponsibleDepartmentList deptList = new ResponsibleDepartmentList();
        List<String> depts = deptList.getResponsibleDepartment();
        // @TODO Use properly formatted refNames for representative departments
        // in this example test record. The following are mere placeholders.
        depts.add("urn:org.collectionspace.services.department:Registrar");
        depts.add("urn:org.walkerart.department:Fine Art");
        collectionObject.setResponsibleDepartments(deptList);

        OtherNumberList otherNumList = new OtherNumberList();
        List<OtherNumber> otherNumbers = otherNumList.getOtherNumber();
        OtherNumber otherNumber1 = new OtherNumber();        
        otherNumber1.setNumberValue("101." + objectName);
        otherNumber1.setNumberType("integer");
        otherNumbers.add(otherNumber1);
        OtherNumber otherNumber2 = new OtherNumber();
        otherNumber2.setNumberValue("101.502.23.456." + objectName);
        otherNumber2.setNumberType("ipaddress");
        otherNumbers.add(otherNumber2);
        collectionObject.setOtherNumberList(otherNumList);

        // Add instances of fields from an extension schema

        CollectionobjectsNaturalhistory conh = new CollectionobjectsNaturalhistory();
      // Laramie20110524 removed for build:   conh.setNhString("test-string");
      // Laramie20110524 removed for build:   conh.setNhInt(999);
      // Laramie20110524 removed for build:   conh.setNhLong(9999);

        PoxPayloadOut multipart = createCollectionObjectInstance(commonPartName, collectionObject, conh);
        return multipart;
    }

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param collectionObject the collection object
     * @param conh the conh
     * @return the multipart output
     */
    private PoxPayloadOut createCollectionObjectInstance(String commonPartName,
            CollectionobjectsCommon collectionObject, CollectionobjectsNaturalhistory conh) {

        PoxPayloadOut multipart = CollectionObjectFactory.createCollectionObjectInstance(
                commonPartName, collectionObject, getNHPartName(), conh);
        if (logger.isDebugEnabled()) {
            logger.debug("to be created, collectionobject common");
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }

        if (conh != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("to be created, collectionobject nhistory");
                logger.debug(objectAsXmlString(conh,
                        CollectionobjectsNaturalhistory.class));
            }
        }
        return multipart;

    }

    /**
     * createCollectionObjectInstanceFromXml uses JAXB unmarshaller to retrieve
     * collectionobject from given file
     * @param commonPartName
     * @param commonPartFileName
     * @return
     * @throws Exception
     */
    private PoxPayloadOut createCollectionObjectInstanceFromXml(String testName, String commonPartName,
            String commonPartFileName) throws Exception {

        CollectionobjectsCommon collectionObject =
                (CollectionobjectsCommon) getObjectFromFile(CollectionobjectsCommon.class,
                commonPartFileName);
        PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(collectionObject,
                MediaType.APPLICATION_XML_TYPE);
        CollectionObjectClient client = new CollectionObjectClient();
        commonPart.setLabel(client.getCommonPartName());

        if (logger.isDebugEnabled()) {
            logger.debug(testName + " to be created, collectionobject common");
            logger.debug(objectAsXmlString(collectionObject,
                    CollectionobjectsCommon.class));
        }
        return multipart;

    }

    /**
     * createCollectionObjectInstanceFromRawXml uses stringified collectionobject
     * retrieve from given file
     * @param commonPartName
     * @param commonPartFileName
     * @return
     * @throws Exception
     */
    private PoxPayloadOut createCollectionObjectInstanceFromRawXml(String testName, String commonPartName,
            String commonPartFileName) throws Exception {

    	PoxPayloadOut multipart = new PoxPayloadOut(CollectionObjectClient.SERVICE_PAYLOAD_NAME);
        String stringObject = getXmlDocumentAsString(commonPartFileName);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + " to be created, collectionobject common " + "\n" + stringObject);
        }
        PayloadOutputPart commonPart = multipart.addPart(commonPartName, stringObject);
//        commonPart.setLabel(commonPartName);

        return multipart;
    }

    /**
     * Gets the nH part name.
     *
     * @return the nH part name
     */
    private String getNHPartName() {
        return "collectionobjects_naturalhistory";
    }

    /**
     * Creates the from xml file.
     *
     * @param testName the test name
     * @param fileName the file name
     * @param useJaxb the use jaxb
     * @return the string
     * @throws Exception the exception
     */
    private String createFromXmlFile(String testName, String fileName, boolean useJaxb) throws Exception {
  
        // Perform setup.
        setupCreate();

        PoxPayloadOut multipart = null;

        CollectionObjectClient client = new CollectionObjectClient();
        if (useJaxb) {
            multipart = createCollectionObjectInstanceFromXml(testName,
                    client.getCommonPartName(), fileName);
        } else {
            multipart = createCollectionObjectInstanceFromRawXml(testName,
                    client.getCommonPartName(), fileName);
        }
        ClientResponse<Response> res = client.create(multipart);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        String newId = extractId(res);
        allResourceIdsCreated.add(newId);
        return newId;
    }

    // FIXME: This duplicates code in read(), and should be consolidated.
    // This is an expedient to support reading and verifying the contents
    // of resources that have been created from test data XML files.
    private CollectionobjectsCommon readCollectionObjectCommonPart(String csid)
        throws Exception {

        String testName = "readCollectionObjectCommonPart";

        setupRead();

        // Submit the request to the service and store the response.
        CollectionObjectClient client = new CollectionObjectClient();
        ClientResponse<String> res = client.read(csid);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Extract the common part.
        CollectionobjectsCommon collectionObject = extractCommonPartValue(testName, res);
        Assert.assertNotNull(collectionObject);

        return collectionObject;
     }

    private CollectionobjectsCommon extractCommonPartValue(String testName, ClientResponse<String> res)
        throws Exception {
        CollectionObjectClient client = new CollectionObjectClient();
        PayloadInputPart payloadInputPart = extractPart(testName, res, client.getCommonPartName());
        Object obj = null;
        if (payloadInputPart != null) {
        	obj = payloadInputPart.getBody();
        }
        Assert.assertNotNull(obj,
                testName + ": body of " + client.getCommonPartName() + " part was unexpectedly null.");
        CollectionobjectsCommon collectionobjectCommon = (CollectionobjectsCommon) obj;
        Assert.assertNotNull(collectionobjectCommon,
                testName + ": " + client.getCommonPartName() + " part was unexpectedly null.");
        return collectionobjectCommon;
    }

    private Object extractPartValue(String testName, ClientResponse<String> res, String partLabel)
        throws Exception {
        Object obj = null;
        PayloadInputPart payloadInputPart = extractPart(testName, res, partLabel);
        if (payloadInputPart != null) {
        	obj = payloadInputPart.getElementBody();
        }
        Assert.assertNotNull(obj,
                testName + ": value of part " + partLabel + " was unexpectedly null.");
        return obj;
    }

    private PayloadInputPart extractPart(String testName, ClientResponse<String> res, String partLabel)
        throws Exception {
        if (logger.isDebugEnabled()) {
           logger.debug(testName + ": Reading part " + partLabel + " ...");
        }
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(partLabel);
        Assert.assertNotNull(payloadInputPart,
                testName + ": part " + partLabel + " was unexpectedly null.");
        return payloadInputPart;
    }

	@Override
	protected String getServicePathComponent() {
		// TODO Auto-generated method stub
		return CollectionObjectClient.SERVICE_PATH_COMPONENT;
	}

}
