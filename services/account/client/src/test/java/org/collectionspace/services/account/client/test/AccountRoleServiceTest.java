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
package org.collectionspace.services.account.client.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import javax.ws.rs.core.Response;

//import org.collectionspace.services.authorization.AccountRolesList;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.client.AccountRoleClient;
import org.collectionspace.services.client.AccountRoleFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.RoleFactory;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;


import org.testng.Assert;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * AccountServiceTest, carries out tests against a
 * deployed and running Account, Role and AccountRole Services.
 * 
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class AccountRoleServiceTest extends AbstractServiceTestImpl {

    /** The Constant logger. */
    private final static String CLASS_NAME = AccountRoleServiceTest.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(CLASS_NAME);
    // Instance variables specific to this test.
    /** The known resource id. */
    private String knownResourceId = null;
    private String prebuiltAdminCSID = null;
    private String prebuiltAdminUserId = "admin@core.collectionspace.org";
    /** The all resource ids created. */
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    /** The acc values. */
    private Hashtable<String, AccountValue> accValues = new Hashtable<String, AccountValue>();
    /** The role values. */
    private Hashtable<String, RoleValue> roleValues = new Hashtable<String, RoleValue>();
    /*
     * This method is called only by the parent class, AbstractServiceTestImpl
     */

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        return new AccountRoleClient().getServicePathComponent();
    }

    /**
     * Seed data.
     */
    @BeforeClass(alwaysRun = true)
    public void seedData() {
        String userId = "acc-role-user1";
        String accId = createAccount(userId, "acc-role-user1-test@cspace.org");
        AccountValue ava = new AccountValue();
        ava.setScreenName(userId);
        ava.setUserId(userId);
        ava.setAccountId(accId);
        accValues.put(ava.getScreenName(), ava);

        String userId2 = "acc-role-user2";
        String coAccId = createAccount(userId2, "acc-role-user2-test@cspace.org");
        AccountValue avc = new AccountValue();
        avc.setScreenName(userId2);
        avc.setUserId(userId2);
        avc.setAccountId(coAccId);
        accValues.put(avc.getScreenName(), avc);

        String rn1 = "ROLE_CO1";
        String r1RoleId = createRole(rn1);
        RoleValue rv1 = new RoleValue();
        rv1.setRoleId(r1RoleId);
        rv1.setRoleName(rn1);
        roleValues.put(rv1.getRoleName(), rv1);

        String rn2 = "ROLE_CO2";
        String r2RoleId = createRole(rn2);
        RoleValue rv2 = new RoleValue();
        rv2.setRoleId(r2RoleId);
        rv2.setRoleName(rn2);
        roleValues.put(rv2.getRoleName(), rv2);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new AccountRoleClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getAbstractCommonList(
            ClientResponse<AbstractCommonList> response) {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readPaginatedList(java.lang.String)
     */
    @Test(dataProvider = "testName")
    @Override
    public void readPaginatedList(String testName) throws Exception {
        //FIXME: http://issues.collectionspace.org/browse/CSPACE-1697
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
            testBanner(testName, CLASS_NAME);
        }
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        AccountValue av = accValues.get("acc-role-user1");
        AccountRole accRole = createAccountRoleInstance(av,
                roleValues.values(), true, true);
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<Response> res = client.create(av.getAccountId(), accRole);
        try {
            int statusCode = res.getStatus();

            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Store the ID returned from this create operation
            // for additional tests below.
            //this is is not important in case of this relationship
            knownResourceId = extractId(res);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        } finally {
            res.releaseConnection();
        }
    }

    //to not cause uniqueness violation for accRole, createList is removed
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    @Override
    public void createList(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
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
            testBanner(testName, CLASS_NAME);
        }

        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<AccountRole> res = client.read(
                accValues.get("acc-role-user1").getAccountId());
        int statusCode = res.getStatus();
        try {
            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            AccountRole output = res.getEntity();
            Assert.assertNotNull(output);
        } finally {
            res.releaseConnection();
        }
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void readNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            testBanner(testName, CLASS_NAME);
        }

        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<AccountRole> res = client.read(this.NON_EXISTENT_ID);
        int statusCode = res.getStatus();
        try {
            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
            res.releaseConnection();
        }
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void readNoRelationship(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            testBanner(testName, CLASS_NAME);
        }

        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<AccountRole> res = client.read(
                accValues.get("acc-role-user2").getAccountId());
        int statusCode = res.getStatus();
        try {
            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());
            AccountRole output = res.getEntity();

            String sOutput = objectAsXmlString(output, AccountRole.class);
            if(logger.isDebugEnabled()) {
                logger.debug(testName + " received " + sOutput);
            }
        } finally {
            res.releaseConnection();
        }
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
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
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
    dependsOnMethods = {"read", "readList", "readNonExistent"})
    public void update(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"readNonExistent", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
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
    dependsOnMethods = {"read"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            testBanner(testName, CLASS_NAME);
        }

        // Perform setup.
        setupDelete();
        
        //
        // Lookup a know account, and delete all of its role relationships
        //
        AccountRoleClient client = new AccountRoleClient();
        ClientResponse<AccountRole> readResponse = client.read(
                accValues.get("acc-role-user1").getAccountId());
        AccountRole toDelete = null;
        try {
        	toDelete = readResponse.getEntity();
        } finally {
        	readResponse.releaseConnection();
        }

        ClientResponse<Response> res = client.delete(
                toDelete.getAccounts().get(0).getAccountId(), toDelete);
        try {
            int statusCode = res.getStatus();
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
            res.releaseConnection();
        }
        
        //
        // recreate 'acc-role-user1' account and roles
        //
        create(testName);
        setupDelete();
        
        //
        // Lookup a know account, and delete all of its role relationships
        //
        readResponse = client.read(
                accValues.get("acc-role-user1").getAccountId());
        toDelete = null;
        try {
        	toDelete = readResponse.getEntity();
        } finally {
        	readResponse.releaseConnection();
        }

        res = client.delete(toDelete.getAccounts().get(0).getAccountId());
        try {
            int statusCode = res.getStatus();
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
            res.releaseConnection();
        }
        
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    	    dependsOnMethods = {"read"})
	public void deleteLockedAccount(String testName) throws Exception {

    	if (logger.isDebugEnabled()) {
    		testBanner(testName, CLASS_NAME);
    	}
    	
    	findPrebuiltAdminAccount();

    	// Perform setup.
        EXPECTED_STATUS_CODE = Response.Status.FORBIDDEN.getStatusCode();
        REQUEST_TYPE = ServiceRequestType.DELETE;
        testSetup(EXPECTED_STATUS_CODE, REQUEST_TYPE);

    	AccountRoleClient client = new AccountRoleClient();
    	ClientResponse<Response> res = client.delete(prebuiltAdminCSID);
    	try {
    		int statusCode = res.getStatus();
    		Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
    				invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    		Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    	} finally {
    		res.releaseConnection();
    	}
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void deleteNonExistent(String testName) throws Exception {
        //ignoring this test as the service side returns 200 now even if it does
        //not find a record in the db
    }
    
    // ---------------------------------------------------------------
    // Search tests
    // ---------------------------------------------------------------
    
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void searchWorkflowDeleted(String testName) throws Exception {
        // Fixme: null test for now, overriding test in base class
    }    

    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------
    /**
     * Tests the code for manually submitting data that is used by several
     * of the methods above.
     * @throws Exception 
     */
    @Test(dependsOnMethods = {"create"})
    public void testSubmitRequest() throws Exception {  //FIXME:  REM - This is not testing a submit /accounts/*/accountroles, but instead just to /accounts

        // Expected status code: 200 OK
        final int EXPECTED_STATUS = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(accValues.get("acc-role-user1").getAccountId());
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
     * Creates the account role instance.
     *
     * @param av the av
     * @param rvs the rvs
     * @param usePermId the use perm id
     * @param useRoleId the use role id
     * @return the account role
     */
    static public AccountRole createAccountRoleInstance(AccountValue pv,
            Collection<RoleValue> rvs,
            boolean usePermId,
            boolean useRoleId) {

        AccountRole accRole = AccountRoleFactory.createAccountRoleInstance(
                pv, rvs, usePermId, useRoleId);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, accRole common");
            logger.debug(objectAsXmlString(accRole, AccountRole.class));
        }
        return accRole;
    }

    /**
     * Clean up.
     */
    @AfterClass(alwaysRun = true)
    @Override
    public void cleanUp() {

        setupDelete();

        String noTest = System.getProperty("noTestCleanup");
        if (Boolean.TRUE.toString().equalsIgnoreCase(noTest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping Cleanup phase ...");
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up temporary resources created for testing ...");
        }


        for (AccountValue pv : accValues.values()) {
            deleteAccount(pv.getAccountId());
        }

        for (RoleValue rv : roleValues.values()) {
            deleteRole(rv.getRoleId());
        }
    }

    /**
     * Creates the account.
     *
     * @param userName the user name
     * @param email the email
     * @return the string
     */
    private String createAccount(String userName, String email) {

        if (logger.isDebugEnabled()) {
            testBanner("createAccount");
        }

        setupCreate();

        AccountClient accClient = new AccountClient();
        AccountsCommon account = AccountFactory.createAccountInstance(
                userName, userName, userName, email, accClient.getTenantId(),
                true, false, true, true);
        ClientResponse<Response> res = accClient.create(account);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("createAccount: userName=" + userName
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
    }

    private void findPrebuiltAdminAccount() {
    	// Search for the prebuilt admin user and then hold its CSID
    	if(prebuiltAdminCSID == null) {
            setupReadList();
            AccountClient client = new AccountClient();
            ClientResponse<AccountsCommonList> res =
                    client.readSearchList(null, this.prebuiltAdminUserId, null);
            AccountsCommonList list = res.getEntity();
            int statusCode = res.getStatus();

            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
            List<AccountsCommonList.AccountListItem> items = list.getAccountListItem();
            Assert.assertEquals(1, items.size(), "Found more than one Admin account!");
            AccountsCommonList.AccountListItem item = items.get(0);
            prebuiltAdminCSID = item.getCsid();
            if (logger.isDebugEnabled()) {
                logger.debug("Found Admin Account with ID: " + prebuiltAdminCSID);
            }
    	}
    }
    
    /**
     * Delete account.
     *
     * @param accId the acc id
     */
    private void deleteAccount(String accId) {

        if (logger.isDebugEnabled()) {
            testBanner("deleteAccount");
        }

        setupDelete();

        AccountClient accClient = new AccountClient();
        ClientResponse<Response> res = accClient.delete(accId);
        int statusCode = res.getStatus();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("deleteAccount: delete account id="
                        + accId + " status=" + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Creates the role.
     *
     * @param roleName the role name
     * @return the string
     */
    private String createRole(String roleName) {
        if (logger.isDebugEnabled()) {
            testBanner("createRole");
        }
        setupCreate();
        RoleClient roleClient = new RoleClient();

        Role role = RoleFactory.createRoleInstance(roleName,
        		roleName, //the display name
                "role for " + roleName, true);
        ClientResponse<Response> res = roleClient.create(role);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug("createRole: name=" + roleName
                    + " status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        res.releaseConnection();
        return extractId(res);
    }

    /**
     * Delete role.
     *
     * @param roleId the role id
     */
    private void deleteRole(String roleId) {
        if (logger.isDebugEnabled()) {
            testBanner("deleteRole");
        }
        setupDelete();
        RoleClient roleClient = new RoleClient();
        ClientResponse<Response> res = roleClient.delete(roleId);
        int statusCode = res.getStatus();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("deleteRole: delete role id=" + roleId
                        + " status=" + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
            res.releaseConnection();
        }
    }

	@Override
	protected String getServiceName() {
		// AccountRoles service is a sub-service of the Account service, so we return Account's service name
		return AccountClient.SERVICE_NAME;
	}
}
