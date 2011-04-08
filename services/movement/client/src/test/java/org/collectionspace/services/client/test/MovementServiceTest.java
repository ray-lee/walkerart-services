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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.MovementsCommon;
import org.collectionspace.services.movement.MovementsCommonList;
import org.collectionspace.services.movement.MovementMethodsList;

import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MovementServiceTest, carries out tests against a
 * deployed and running Movement Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class MovementServiceTest extends AbstractServiceTestImpl {

   /** The logger. */
    private final String CLASS_NAME = MovementServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    final String SERVICE_NAME = "movements";
    final String SERVICE_PATH_COMPONENT = "movements";

    // Instance variables specific to this test.
    private String knownResourceId = null;
    private final static String TIMESTAMP_UTC = GregorianCalendarDateTimeUtils.timestampUTC();
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new MovementClient();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response) {
        return response.getEntity(MovementsCommonList.class);
    }
 
    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void create(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        String identifier = createIdentifier();
        PoxPayloadOut multipart = createMovementInstance(identifier);
        ClientResponse<Response> res = client.create(multipart);

        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Specifically:
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null){
            knownResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(extractId(res));
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create"})
    public void createList(String testName) throws Exception {
        for(int i = 0; i < 3; i++){
            create(testName);
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    	//Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    	//Should this really be empty?
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    	//Should this really be empty?
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
            logger.debug("createWithEmptyEntityBody url=" + url +
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
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#read(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create"})
    public void read(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<String> res = client.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Get the common part of the response and verify that it is not null.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        MovementsCommon movementCommon = null;
        if (payloadInputPart != null) {
        	movementCommon = (MovementsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(movementCommon);

        // Check selected fields.

        // Check the values of one or more date/time fields.
        if (logger.isDebugEnabled()) {
            logger.debug("locationDate=" + movementCommon.getLocationDate());
            logger.debug("TIMESTAMP_UTC=" + TIMESTAMP_UTC);
        }
        Assert.assertTrue(movementCommon.getLocationDate().equals(TIMESTAMP_UTC));
        Assert.assertTrue(movementCommon.getPlannedRemovalDate().equals(TIMESTAMP_UTC));
        Assert.assertNull(movementCommon.getRemovalDate());
        
        // Check the values of fields containing Unicode UTF-8 (non-Latin-1) characters.
       if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + getUTF8DataFragment() + "\n"
                    + "UTF-8 data received=" + movementCommon.getMovementNote());
    }
        Assert.assertEquals(movementCommon.getMovementNote(), getUTF8DataFragment(),
                "UTF-8 data retrieved '" + movementCommon.getMovementNote()
                + "' does not match expected data '" + getUTF8DataFragment());

    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"read"})
    public void readNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createList", "read"})
    public void readList(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<MovementsCommonList> res = client.readList();
        MovementsCommonList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){
            List<MovementsCommonList.MovementListItem> items =
                    list.getMovementListItem();
            int i = 0;
            for(MovementsCommonList.MovementListItem item : items){
                logger.debug(testName + ": list-item[" + i + "] csid=" +
                        item.getCsid());
                logger.debug(testName + ": list-item[" + i + "] movementReferenceNumber=" +
                        item.getMovementReferenceNumber());
                logger.debug(testName + ": list-item[" + i + "] locationDate=" +
                        item.getLocationDate());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"read"})
    public void update(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        MovementClient client = new MovementClient();
        ClientResponse<String> res = client.read(knownResourceId);
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": read status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);
        if(logger.isDebugEnabled()){
            logger.debug("got object to update with ID: " + knownResourceId);
        }

        // Extract the common part from the response.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
        MovementsCommon movementCommon = null;
        if (payloadInputPart != null) {
        	movementCommon = (MovementsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(movementCommon);

        // Update its content.
        movementCommon.setMovementReferenceNumber("updated-" + movementCommon.getMovementReferenceNumber());
        movementCommon.setMovementNote("updated movement note-" + movementCommon.getMovementNote());
        movementCommon.setNormalLocation(""); // Test deletion of existing string value

        String currentTimestamp = GregorianCalendarDateTimeUtils.timestampUTC();
        movementCommon.setPlannedRemovalDate(""); // Test deletion of existing date or date/time value
        movementCommon.setRemovalDate(currentTimestamp);

        if(logger.isDebugEnabled()){
            logger.debug("to be updated object");
            logger.debug(objectAsXmlString(movementCommon, MovementsCommon.class));
        }

        // Submit the request to the service and store the response.
        PoxPayloadOut output = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart = output.addPart(movementCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(client.getCommonPartName());
        res = client.update(knownResourceId, output);

        // Check the status code of the response: does it match the expected response(s)?
        int statusCode = res.getStatus();
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

       // Extract the updated common part from the response.
        input = new PoxPayloadIn(res.getEntity());
        payloadInputPart = input.getPart(client.getCommonPartName());
        MovementsCommon updatedMovementCommon = null;
        if (payloadInputPart != null) {
        	updatedMovementCommon = (MovementsCommon) payloadInputPart.getBody();
        }
        Assert.assertNotNull(movementCommon);
        if(logger.isDebugEnabled()){
            logger.debug("updated object");
            logger.debug(objectAsXmlString(updatedMovementCommon, MovementsCommon.class));
        }

        // Check selected fields in the updated common part.
        Assert.assertEquals(updatedMovementCommon.getNormalLocation(),
            movementCommon.getNormalLocation(), "Data in updated object did not match submitted data.");
        if(logger.isDebugEnabled()){
            logger.debug("Normal location after update=|" + updatedMovementCommon.getNormalLocation() + "|");
        }

        Assert.assertEquals(updatedMovementCommon.getMovementReferenceNumber(),
            movementCommon.getMovementReferenceNumber(),
            "Data in updated object did not match submitted data.");
        Assert.assertEquals(updatedMovementCommon.getMovementNote(),
            movementCommon.getMovementNote(),
            "Data in updated object did not match submitted data.");
        Assert.assertNull(updatedMovementCommon.getPlannedRemovalDate());
        Assert.assertEquals(updatedMovementCommon.getRemovalDate(),
            movementCommon.getRemovalDate(),
            "Data in updated object did not match submitted data.");

        if(logger.isDebugEnabled()){
            logger.debug("UTF-8 data sent=" + movementCommon.getMovementNote() + "\n"
                    + "UTF-8 data received=" + updatedMovementCommon.getMovementNote());
    }
        Assert.assertTrue(updatedMovementCommon.getMovementNote().contains(getUTF8DataFragment()),
                "UTF-8 data retrieved '" + updatedMovementCommon.getMovementNote()
                + "' does not contain expected data '" + getUTF8DataFragment());
        Assert.assertEquals(updatedMovementCommon.getMovementNote(),
                movementCommon.getMovementNote(),
                "Data in updated object did not match submitted data.");

    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception{
    	//Should this really be empty?
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    	//Should this really be empty?
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    	//Should this really be empty?
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
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
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
        MovementClient client = new MovementClient();
        PoxPayloadOut multipart = createMovementInstance(NON_EXISTENT_ID);
        ClientResponse<String> res = client.update(NON_EXISTENT_ID, multipart);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */

    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"create", "readList", "testSubmitRequest", "update"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        /*
        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
     *
     */
    }


    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        MovementClient client = new MovementClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
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
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug("testSubmitRequest: url=" + url +
                " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    /**
     * Creates the movement instance.
     *
     * @param identifier the identifier
     * @return the multipart output
     */
    private PoxPayloadOut createMovementInstance(String identifier) {
        return createInstance("movementReferenceNumber-" + identifier);
    }

    /**
     * Creates an instance of a Movement record for testing.
     *
     * @param movementReferenceNumber A movement reference number.
     * @return Multipart output suitable for use as a payload
     *     in a create or update request.
     */
    @Override
    public PoxPayloadOut createInstance(String movementReferenceNumber) {
        MovementsCommon movementCommon = new MovementsCommon();
        // FIXME: Values of currentLocation, normalLocation,
        // and movementContact should be refNames.
        movementCommon.setCurrentLocation("currentLocation value");
        movementCommon.setCurrentLocationFitness("currentLocationFitness value");
        movementCommon.setCurrentLocationNote("currentLocationNote value");
        movementCommon.setLocationDate(TIMESTAMP_UTC);
        movementCommon.setNormalLocation("normalLocation value");
        movementCommon.setMovementContact("movementContact value");
        MovementMethodsList movementMethodsList = new MovementMethodsList();
        List<String> methods = movementMethodsList.getMovementMethod();
        // @TODO Use properly formatted refNames for representative movement
        // methods in this example record. The values below are placeholders.
        String identifier = createIdentifier();
        methods.add("First Movement Method-" + identifier);
        methods.add("Second Movement Method-" + identifier);
        movementCommon.setMovementMethods(movementMethodsList);
        movementCommon.setMovementNote(getUTF8DataFragment());
        movementCommon.setMovementReferenceNumber(movementReferenceNumber);
        movementCommon.setPlannedRemovalDate(TIMESTAMP_UTC);
        movementCommon.setRemovalDate(""); // Test empty date value
        movementCommon.setReasonForMove("reasonForMove value");

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(movementCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new MovementClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, movement common");
            logger.debug(objectAsXmlString(movementCommon, MovementsCommon.class));
        }

        return multipart;
    }

    // FIXME Should be moved to a common class, as these are general utilities.
    // FIXME Should be refactored to become a convenience variant of a
    // general method to return a current datestamp or timestamp in any
    // provided time zone.

   /**
    * Returns an ISO 8601 formatted timestamp of the
    * current time instance in the UTC time zone.
    */
    public String datestampUTC() {
        final String ISO_8601_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
        final DateFormat ISO_8601_DATE_FORMAT =
            new SimpleDateFormat(ISO_8601_DATE_FORMAT_PATTERN);

        final String UTC_TIMEZONE_IDENTIFIER = "UTC";
        final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone(UTC_TIMEZONE_IDENTIFIER);

        Date timestamp = new Date();
        return formatDate(timestamp, UTC_TIMEZONE, ISO_8601_DATE_FORMAT);
    }

   /**
    * Returns an ISO 8601 formatted timestamp of the
    * current time instance in the UTC time zone.
    */
    public String timestampUTC() {
        final String ISO_8601_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        final DateFormat ISO_8601_FORMAT =
            new SimpleDateFormat(ISO_8601_FORMAT_PATTERN);

        final String UTC_TIMEZONE_IDENTIFIER = "UTC";
        final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone(UTC_TIMEZONE_IDENTIFIER);

        Date timestamp = new Date();
        return formatDate(timestamp, UTC_TIMEZONE, ISO_8601_FORMAT);
    }

   /**
    * Formats a provided date using a provided date formatter,
    * in the default system time zone.
    *
    * @param date  A date to format.
    * @param df    A date formatter to apply.
    * @return      A formatted date string.
    */
    public String formatDate(Date date, DateFormat df) {
        return formatDate(date, TimeZone.getDefault(), df);
    }

    // FIXME Add error handling.

   /**
    * Formats a provided date using a provided date formatter,
    * in a provided time zone.
    *
    * @param date  A date to format.
    * @param tz    The time zone qualifier for the date to format.
    * @param df    A date formatter to apply.
    *
    * @return      A formatted date string.
    */
    public String formatDate(Date date, TimeZone tz, DateFormat df) {
        df.setTimeZone(tz);
        return df.format(date);
    }

}
