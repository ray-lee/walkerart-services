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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.LoaninClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanin.LenderGroup;
import org.collectionspace.services.loanin.LenderGroupList;
import org.collectionspace.services.loanin.LoansinCommon;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoaninAuthRefsTest, carries out Authority References tests against a
 * deployed and running Loanin (aka Loans In) Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class LoaninAuthRefsTest extends BaseServiceTest {

    private final String CLASS_NAME = LoaninAuthRefsTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    
    // Instance variables specific to this test.
    final String SERVICE_NAME = "loansin";
    final String SERVICE_PATH_COMPONENT = "loansin";
    final String PERSON_AUTHORITY_NAME = "TestPersonAuth";
    private String knownResourceId = null;
    private List<String> loaninIdsCreated = new ArrayList<String>();
    private List<String> personIdsCreated = new ArrayList<String>();
    private String personAuthCSID = null;
    private String lenderRefName = null;
    private String lendersAuthorizerRefName = null;
    private String lendersContactRefName = null;
    private String loanInContactRefName = null;
    private String borrowersAuthorizerRefName = null;
    // FIXME: Value changed from 5 to 2 when repeatable / multivalue 'lenders'
    // group was added to tenant-bindings.xml
    private final int NUM_AUTH_REFS_EXPECTED = 2;

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
	protected AbstractCommonList getAbstractCommonList(
			ClientResponse<AbstractCommonList> response) {
    	throw new UnsupportedOperationException(); //method not supported (or needed) in this test class
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class)
    public void createWithAuthRefs(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        testSetup(STATUS_CREATED, ServiceRequestType.CREATE);

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        
        // Create all the person refs and entities
        createPersonRefs();

        // Create a new Loans In resource.
        //
        // One or more fields in this resource will be PersonAuthority
        // references, and will refer to Person resources by their refNames.
        LoaninClient loaninClient = new LoaninClient();
        PoxPayloadOut multipart = createLoaninInstance(
                "loanInNumber-" + identifier,
                "returnDate-" + identifier,
		lenderRefName,
                lendersAuthorizerRefName,
                lendersContactRefName,
                loanInContactRefName,
                borrowersAuthorizerRefName);
        ClientResponse<Response> response = loaninClient.create(multipart);
        int statusCode = response.getStatus();
        try {
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
	            knownResourceId = extractId(response);
	            if (logger.isDebugEnabled()) {
	                logger.debug(testName + ": knownResourceId=" + knownResourceId);
	            }
	        }
	        
	        // Store the IDs from every resource created by tests,
	        // so they can be deleted after tests have been run.
	        loaninIdsCreated.add(extractId(response));
        } finally {
        	response.releaseConnection();
        }
    }
    
    protected void createPersonRefs(){

        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Create a temporary PersonAuthority resource, and its corresponding
        // refName by which it can be identified.
    	PoxPayloadOut multipart = PersonAuthorityClientUtils.createPersonAuthorityInstance(
    	    PERSON_AUTHORITY_NAME, PERSON_AUTHORITY_NAME, personAuthClient.getCommonPartName());
        ClientResponse<Response> res = personAuthClient.create(multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
        personAuthCSID = extractId(res);

        String authRefName = PersonAuthorityClientUtils.getAuthorityRefName(personAuthCSID, null);
        
        // Create temporary Person resources, and their corresponding refNames
        // by which they can be identified.
       	String csid = createPerson("Linus", "Lender", "linusLender", authRefName);
        personIdsCreated.add(csid);
        lenderRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

       	csid = createPerson("Art", "Lendersauthorizor", "artLendersauthorizor", authRefName);
        personIdsCreated.add(csid);
        lendersAuthorizerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Larry", "Lenderscontact", "larryLenderscontact", authRefName);
        personIdsCreated.add(csid);
        lendersContactRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);
        
        csid = createPerson("Carrie", "Loanincontact", "carrieLoanincontact", authRefName);
        personIdsCreated.add(csid);
        loanInContactRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        csid = createPerson("Bonnie", "Borrowersauthorizer", "bonnieBorrowersauthorizer", authRefName);
        personIdsCreated.add(csid);
        borrowersAuthorizerRefName = PersonAuthorityClientUtils.getPersonRefName(personAuthCSID, csid, null);

        // FIXME: Add instance(s) of 'lenders' field when we can work with
        // repeatable / multivalued authority reference fields.  Be sure to
        // change the NUM_AUTH_REFS_EXPECTED constant accordingly, or otherwise
        // revise check for numbers of authority fields expected in readAndCheckAuthRefs.
    }
    
    protected String createPerson(String firstName, String surName, String shortId, String authRefName ) {
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        Map<String, String> personInfo = new HashMap<String,String>();
        personInfo.put(PersonJAXBSchema.FORE_NAME, firstName);
        personInfo.put(PersonJAXBSchema.SUR_NAME, surName);
        personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortId);
    	PoxPayloadOut multipart =
    		PersonAuthorityClientUtils.createPersonInstance(personAuthCSID, 
    				authRefName, personInfo, personAuthClient.getItemCommonPartName());
        ClientResponse<Response> res = personAuthClient.createItem(personAuthCSID, multipart);
        int statusCode = res.getStatus();

        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, STATUS_CREATED);
    	return extractId(res);
    }

    // Success outcomes
    @Test(dataProvider="testName", dataProviderClass=AbstractServiceTestImpl.class,
        dependsOnMethods = {"createWithAuthRefs"})
    public void readAndCheckAuthRefs(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        testSetup(STATUS_OK, ServiceRequestType.READ);

        // Submit the request to the service and store the response.
        LoaninClient loaninClient = new LoaninClient();
        ClientResponse<String> res = loaninClient.read(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        if(logger.isDebugEnabled()){
            logger.debug(testName + ".read: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Extract the common part from the response.
        PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
        LoansinCommon loaninCommon = (LoansinCommon) extractPart(input,
            loaninClient.getCommonPartName(), LoansinCommon.class);
        Assert.assertNotNull(loaninCommon);
        if(logger.isDebugEnabled()){
            logger.debug(objectAsXmlString(loaninCommon, LoansinCommon.class));
        }

        // Check a couple of fields
        // Assert.assertEquals(loaninCommon.getLender(), lenderRefName);
        // Assert.assertEquals(loaninCommon.getLendersAuthorizer(), lendersAuthorizerRefName);
        // Assert.assertEquals(loaninCommon.getLendersContact(), lendersContactRefName);
        Assert.assertEquals(loaninCommon.getLoanInContact(), loanInContactRefName);
        Assert.assertEquals(loaninCommon.getBorrowersAuthorizer(), borrowersAuthorizerRefName);
        
        // Get the auth refs and check them
        ClientResponse<AuthorityRefList> res2 =
           loaninClient.getAuthorityRefs(knownResourceId);
        statusCode = res2.getStatus();

        if(logger.isDebugEnabled()){
            logger.debug(testName + ".getAuthorityRefs: status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        AuthorityRefList list = res2.getEntity();
        
        List<AuthorityRefList.AuthorityRefItem> items = list.getAuthorityRefItem();
        int numAuthRefsFound = items.size();
        if(logger.isDebugEnabled()){
            logger.debug("Expected " + NUM_AUTH_REFS_EXPECTED +
                " authority references, found " + numAuthRefsFound);
        }

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = true;
        if(iterateThroughList && logger.isDebugEnabled()){
            int i = 0;
            for(AuthorityRefList.AuthorityRefItem item : items){
                logger.debug(testName + ": list-item[" + i + "] Field:" +
                		item.getSourceField() + "= " +
                        item.getAuthDisplayName() +
                        item.getItemDisplayName());
                logger.debug(testName + ": list-item[" + i + "] refName=" +
                        item.getRefName());
                logger.debug(testName + ": list-item[" + i + "] URI=" +
                        item.getUri());
                i++;
            }
        }

        Assert.assertEquals(numAuthRefsFound, NUM_AUTH_REFS_EXPECTED,
            "Did not find all expected authority references! " +
            "Expected " + NUM_AUTH_REFS_EXPECTED + ", found " + numAuthRefsFound);

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
    @AfterClass(alwaysRun=true)
    public void cleanUp() {
        String noTest = System.getProperty("noTestCleanup");
    	if(Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
    	}
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }
        PersonAuthorityClient personAuthClient = new PersonAuthorityClient();
        // Delete Person resource(s) (before PersonAuthority resources).
        
        for (String resourceId : personIdsCreated) {
            // Note: Any non-success responses are ignored and not reported.
        	ClientResponse<Response> response = 
        		personAuthClient.deleteItem(personAuthCSID, resourceId); // alternative to personAuthClient.deleteItem().releaseConnection();
        	response.releaseConnection();
        }
        
        // Delete PersonAuthority resource(s).
        // Note: Any non-success response is ignored and not reported.
        if (personAuthCSID != null) {
        	personAuthClient.delete(personAuthCSID);
	        // Delete Loans In resource(s).
        	LoaninClient loaninClient = new LoaninClient();
        	ClientResponse<Response> response = null;
	        for (String resourceId : loaninIdsCreated) {
	            // Note: Any non-success responses are ignored and not reported.
	            response = loaninClient.delete(resourceId); // alternative to loaninClient.delete(resourceId).releaseConnection();
	            response.releaseConnection();
	        }
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }


   private PoxPayloadOut createLoaninInstance(String loaninNumber,
    		String returnDate,
                String lender,
                String lendersAuthorizer,
                String lendersContact,
                String loaninContact,
                String borrowersAuthorizer) {
        LoansinCommon loaninCommon = new LoansinCommon();
        loaninCommon.setLoanInNumber(loaninNumber);
        loaninCommon.setLoanInNumber(returnDate);
        LenderGroupList lenderGroupList =  new LenderGroupList();
        LenderGroup lenderGroup = new LenderGroup();
        lenderGroup.setLender(lender);
        lenderGroup.setLendersAuthorizer(lendersAuthorizer);
        lenderGroup.setLendersContact(lendersContact);
        lenderGroupList.getLenderGroup().add(lenderGroup);
        loaninCommon.setLenderGroupList(lenderGroupList);
        loaninCommon.setLoanInContact(loaninContact);
        loaninCommon.setBorrowersAuthorizer(borrowersAuthorizer);

        PoxPayloadOut multipart = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart commonPart =
            multipart.addPart(loaninCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new LoaninClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, loanin common");
            logger.debug(objectAsXmlString(loaninCommon, LoansinCommon.class));
        }

        return multipart;
    }
}
