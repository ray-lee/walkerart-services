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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.authentication.client.test;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.account.AccountTenant;
import org.collectionspace.services.client.AccountClient;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.Status;
import org.collectionspace.services.client.AccountFactory;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.test.AbstractServiceTestImpl;
import org.collectionspace.services.client.test.BaseServiceTest;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthenticationServiceTest uses CollectionObject service to test
 * authentication
 * 
 * $LastChangedRevision: 434 $ $LastChangedDate: 2009-07-28 14:34:15 -0700 (Tue,
 * 28 Jul 2009) $
 */
public class AuthenticationServiceTest extends AbstractServiceTestImpl {

    /** The known resource id. */
    private String knownResourceId = null;
    private String barneyAccountId = null; //active
    private String georgeAccountId = null; //inactive
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(AuthenticationServiceTest.class);

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#getServicePathComponent()
     */
    @Override
    protected String getServicePathComponent() {
        // no need to return anything but null since no auth resources are
        // accessed
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new AccountClient();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getAbstractCommonList(org.jboss.resteasy.client.ClientResponse)
     */
    @Override
    protected AbstractCommonList getAbstractCommonList(
            ClientResponse<AbstractCommonList> response) {
        throw new UnsupportedOperationException(); //Since this test does not support lists, this method is not needed.
    }

    @Test(dataProvider = "testName")
    @Override
    public void readPaginatedList(String testName) throws Exception {
        // Test not supported.
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createActiveAccount(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);
        AccountClient accountClient = new AccountClient();
        accountClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        accountClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "test");
        accountClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "test");

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("barney", "barney08", "barney@dinoland.com", false);
        ClientResponse<Response> res = accountClient.create(account);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": barney status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation
        // for additional tests below.
        barneyAccountId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": barneyAccountId=" + barneyAccountId);
        }

    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class)
    public void createInactiveAccount(String testName) throws Exception {
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate(testName);
        AccountClient accountClient = new AccountClient();
        accountClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        accountClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "test");
        accountClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "test");

        // Submit the request to the service and store the response.
        AccountsCommon account =
                createAccountInstance("george", "george08", "george@curiousland.com", false);
        ClientResponse<Response> res = accountClient.create(account);
        int statusCode = res.getStatus();

        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": george status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation
        // for additional tests below.
        georgeAccountId = extractId(res);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": georgeAccountId=" + georgeAccountId);
        }

        //deactivate
        setupUpdate(testName);
        account.setStatus(Status.INACTIVE);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ":updated object");
            logger.debug(objectAsXmlString(account,
                    AccountsCommon.class));
        }

        // Submit the request to the service and store the response.
        ClientResponse<AccountsCommon> res1 = accountClient.update(georgeAccountId, account);
        statusCode = res1.getStatus();
        // Check the status code of the response: does it match the expected response(s)?
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#create()
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"createActiveAccount"})
    @Override
    public void create(String testName) {
        setupCreate(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "barney");
        collectionObjectClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "barney08");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error("create: caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug("create: status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(),
                Response.Status.CREATED.getStatusCode(), "expected "
                + Response.Status.CREATED.getStatusCode());

        // Store the ID returned from this create operation for additional tests
        // below.
        knownResourceId = extractId(res);
    }

    @Test(dataProvider = "testName", dependsOnMethods = {"createInactiveAccount"})
    public void createWithInactiveAccount(String testName) {
        banner(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "george");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY,
                "george08");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error(testName + ": caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(),
                Response.Status.FORBIDDEN.getStatusCode(), "expected "
                + Response.Status.FORBIDDEN.getStatusCode());
    }

    /**
     * Creates the collection object instance without password.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithoutPassword(String testName) {
        banner(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "test");
        collectionObjectClient.removeProperty(CollectionSpaceClient.PASSWORD_PROPERTY);
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error(testName + ": caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /**
     * Creates the collection object with unknown user
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithUnknownUser(String testName) {
        banner(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "foo");
        collectionObjectClient.setProperty(CollectionSpaceClient.PASSWORD_PROPERTY,
                "bar");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error(testName + ": caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /**
     * Creates the collection object instance with incorrect password.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithIncorrectPassword(String testName) {
        banner(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "test");
        collectionObjectClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error(testName + ": caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /**
     * Creates the collection object instance with incorrect user password.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithIncorrectUserPassword(String testName) {
        banner(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "foo");
        collectionObjectClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "bar");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error(testName + ": caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = "
                    + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /**
     * Creates the collection object instance with incorrect user password.
     */
    @Test(dataProvider = "testName", dependsOnMethods = {"createActiveAccount"})
    public void createWithoutTenant(String testName) {
        banner(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        String identifier = BaseServiceTest.createIdentifier();
        MultipartOutput multipart = createCollectionObjectInstance(
                collectionObjectClient.getCommonPartName(), identifier);

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "babybop");
        collectionObjectClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "babybop09");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error(testName + ": caught " + e.getMessage());
            return;
        }
        ClientResponse<Response> res = collectionObjectClient.create(multipart);
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": status = "
                    + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode(), "expected " + Response.Status.UNAUTHORIZED.getStatusCode());
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#delete()
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create"})
    public void delete(String testName) {
        setupDelete(testName);
        CollectionObjectClient collectionObjectClient = new CollectionObjectClient();
        collectionObjectClient = new CollectionObjectClient();

        collectionObjectClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        collectionObjectClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "test");
        collectionObjectClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        try {
            collectionObjectClient.setupHttpClient();
            collectionObjectClient.setProxy();
        } catch (Exception e) {
            logger.error("deleteCollectionObject: caught " + e.getMessage());
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Calling deleteCollectionObject:" + knownResourceId);
        }
        ClientResponse<Response> res = collectionObjectClient.delete(knownResourceId);
        if (logger.isDebugEnabled()) {
            logger.debug("deleteCollectionObject: status = " + res.getStatus());
        }
        Assert.assertEquals(res.getStatus(),
                Response.Status.OK.getStatusCode(), "expected " + Response.Status.OK.getStatusCode());
    }

    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"create", "createWithInactiveAccount"})
    public void deleteAccounts(String testName) throws Exception {

        // Perform setup.
        setupDelete(testName);
        AccountClient accountClient = new AccountClient();

        accountClient.setProperty(CollectionSpaceClient.AUTH_PROPERTY,
                "true");
        accountClient.setProperty(CollectionSpaceClient.USER_PROPERTY,
                "test");
        accountClient.setProperty(
                CollectionSpaceClient.PASSWORD_PROPERTY, "test");
        // Submit the request to the service and store the response.
        ClientResponse<Response> res = accountClient.delete(barneyAccountId);
        int statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": barney status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));

        res = accountClient.delete(georgeAccountId);
        statusCode = res.getStatus();
        if (logger.isDebugEnabled()) {
            logger.debug(testName + ": george status = " + statusCode);
        }
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param identifier the identifier
     *
     * @return the multipart output
     */
    private MultipartOutput createCollectionObjectInstance(
            String commonPartName, String identifier) {
        return createCollectionObjectInstance(commonPartName, "objectNumber-" + identifier, "objectName-" + identifier);
    }

    /**
     * Creates the collection object instance.
     *
     * @param commonPartName the common part name
     * @param objectNumber the object number
     * @param objectName the object name
     *
     * @return the multipart output
     */
    private MultipartOutput createCollectionObjectInstance(
            String commonPartName, String objectNumber, String objectName) {
        CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();

        collectionObject.setObjectNumber(objectNumber);
        collectionObject.setObjectName(objectName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(collectionObject,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", commonPartName);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, collectionobject common ",
                    collectionObject, CollectionobjectsCommon.class);
        }
        return multipart;
    }

    private AccountsCommon createAccountInstance(String screenName,
            String passwd, String email, boolean invalidTenant) {

        AccountsCommon account = AccountFactory.createAccountInstance(screenName,
                screenName, passwd, email,
                true, true, invalidTenant, true, true);

        List<AccountTenant> atl = account.getTenants();

        //disable 2nd tenant till tenant identification is in effect
        //on the service side for 1-n user-tenants
//        AccountsCommon.Tenant at2 = new AccountsCommon.Tenant();
//        at2.setId(UUID.randomUUID().toString());
//        at2.setName("collectionspace.org");
//        atl.add(at2);
//        account.setTenants(atl);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, account common");
            logger.debug(objectAsXmlString(account,
                    AccountsCommon.class));
        }
        return account;

    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#createList()
     */
    @Override
    public void createList(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#createWithEmptyEntityBody()
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#createWithMalformedXml()
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#createWithWrongXmlSchema()
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#read()
     */
    @Override
    public void read(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#readNonExistent()
     */
    @Override
    public void readNonExistent(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#readList()
     */
    @Override
    public void readList(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#update()
     */
    @Override
    public void update(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#updateWithEmptyEntityBody()
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#updateWithMalformedXml()
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#updateWithWrongXmlSchema()
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#updateNonExistent()
     */
    @Override
    public void updateNonExistent(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTest#deleteNonExistent()
     */
    @Override
    public void deleteNonExistent(String testName) throws Exception {
        //FIXME: Should this test really be empty?  If so, please comment accordingly.
    }
}
