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

import org.collectionspace.services.TaxonJAXBSchema;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.client.TaxonomyAuthorityClientUtils;
import org.collectionspace.services.common.AbstractCommonListUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.taxonomy.TaxonAuthorGroup;
import org.collectionspace.services.taxonomy.TaxonAuthorGroupList;
import org.collectionspace.services.taxonomy.TaxonCitationList;
import org.collectionspace.services.taxonomy.TaxonomyauthorityCommon;
import org.collectionspace.services.taxonomy.TaxonCommon;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * TaxonomyAuthorityServiceTest, carries out tests against a
 * deployed and running TaxonomyAuthority Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class TaxonomyAuthorityServiceTest extends AbstractServiceTestImpl { //FIXME: Test classes for Vocab, Person, Org, and Taxonomy should have a base class!

    /** The logger. */
    private final String CLASS_NAME = TaxonomyAuthorityServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(TaxonomyAuthorityServiceTest.class);
    private final String REFNAME = "refName";
    private final String DISPLAYNAME = "displayName";

    @Override
    public String getServicePathComponent() {
        return TaxonomyAuthorityClient.SERVICE_PATH_COMPONENT;
    }

    @Override
    protected String getServiceName() {
        return TaxonomyAuthorityClient.SERVICE_NAME;
    }

    public String getItemServicePathComponent() {
        return AuthorityClient.ITEMS;
    }
    final String TEST_SHORTID = "CentauruspleurexanthemusGreen1832";
    final String TEST_TERM_STATUS = "accepted";
    final String TEST_TAXON_FULL_NAME = "Centaurus pleurexanthemus Green 1832";
    // TODO Re-implement the Taxon Rank field so as to provide an orderable
    // ranking value, as well as a display name.
    final String TEST_TAXON_RANK = "species";
    final String TEST_TAXON_AUTHOR = "J. Green";
    final String TEST_TAXON_AUTHOR_TYPE = "ascribed";
    final String TEST_TAXON_CITATION = "A Monograph of the Trilobites of North America";
    final String TEST_TAXON_CURRENCY = "current";
    final String TEST_TAXON_YEAR = "1832";
    final String TEST_TAXONOMIC_STATUS = "valid";
    final String TEST_TAXON_IS_NAMED_HYBRID = "false";
    final TaxonAuthorGroupList NULL_TAXON_AUTHOR_GROUP_LIST = null;
    final TaxonCitationList NULL_TAXON_CITATION_LIST = null;
    /** The known resource id. */
    private String knownResourceId = null;
    private String knownResourceShortIdentifer = null;
    private String knownResourceRefName = null;
    private String knownTaxonomyTypeRefName = null;
    private String knownItemResourceId = null;
    private String knownItemResourceShortIdentifer = null;
    /** The n items to create in list. */
    private int nItemsToCreateInList = 3;
    /** The all resource ids created. */
    private List<String> allResourceIdsCreated = new ArrayList<String>();
    /** The all item resource ids created. */
    private Map<String, String> allItemResourceIdsCreated =
            new HashMap<String, String>();

    protected void setKnownResource(String id, String shortIdentifer,
            String refName) {
        knownResourceId = id;
        knownResourceShortIdentifer = shortIdentifer;
        knownResourceRefName = refName;
    }

    protected void setKnownItemResource(String id, String shortIdentifer) {
        knownItemResourceId = id;
        knownItemResourceShortIdentifer = shortIdentifer;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
        return new TaxonomyAuthorityClient();
    }

    @Override
    protected PoxPayloadOut createInstance(String identifier) {
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        String shortId = identifier;
        String displayName = "displayName-" + shortId;
        String baseRefName =
                TaxonomyAuthorityClientUtils.createTaxonomyAuthRefName(shortId, null);
        PoxPayloadOut multipart =
                TaxonomyAuthorityClientUtils.createTaxonomyAuthorityInstance(
                displayName, shortId, client.getCommonPartName());
        return multipart;
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.ServiceTest#create(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"create"})
    public void create(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        String shortId = createIdentifier();
        String displayName = "displayName-" + shortId;
        String baseRefName =
                TaxonomyAuthorityClientUtils.createTaxonomyAuthRefName(shortId, null);

        PoxPayloadOut multipart =
                TaxonomyAuthorityClientUtils.createTaxonomyAuthorityInstance(
                displayName, shortId, client.getCommonPartName());
        String newID = null;
        ClientResponse<Response> res = client.create(multipart);
        try {
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
            Assert.assertTrue(this.REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(this.REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, this.EXPECTED_STATUS_CODE);

            newID = TaxonomyAuthorityClientUtils.extractId(res);
        } finally {
            res.releaseConnection();
        }
        // Store the ID returned from the first resource created
        // for additional tests below.
        if (knownResourceId == null) {
            setKnownResource(newID, shortId, baseRefName);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownResourceId=" + knownResourceId);
            }
        }
        // Store the IDs from every resource created by tests,
        // so they can be deleted after tests have been run.
        allResourceIdsCreated.add(newID);
    }

    /**
     * Creates the item.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"create"}, dependsOnMethods = {"create"})
    public void createItem(String testName) {
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        setupCreate();
        createItemInAuthority(knownResourceId, knownResourceRefName);
    }

    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param authRefName the auth ref name
     * @return the string
     */
    private String createItemInAuthority(String vcsid, String authRefName) {

        final String testName = "createItemInAuthority(" + vcsid + "," + authRefName + ")";
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        Map<String, String> taxonMap = new HashMap<String, String>();

        // Fields present in all authority records.
        taxonMap.put(TaxonJAXBSchema.SHORT_IDENTIFIER, TEST_SHORTID);
        // TODO Make term status be controlled vocab.
        taxonMap.put(TaxonJAXBSchema.TERM_STATUS, TEST_TERM_STATUS);

        // Fields specific to this specific authority record type.
        taxonMap.put(TaxonJAXBSchema.NAME, TEST_TAXON_FULL_NAME);
        taxonMap.put(TaxonJAXBSchema.TAXON_RANK, TEST_TAXON_RANK);
        taxonMap.put(TaxonJAXBSchema.TAXON_CURRENCY, TEST_TAXON_CURRENCY);
        taxonMap.put(TaxonJAXBSchema.TAXON_YEAR, TEST_TAXON_YEAR);
        taxonMap.put(TaxonJAXBSchema.TAXONOMIC_STATUS, TEST_TAXONOMIC_STATUS);
        taxonMap.put(TaxonJAXBSchema.TAXON_IS_NAMED_HYBRID, TEST_TAXON_IS_NAMED_HYBRID);

        TaxonCitationList taxonCitationList = new TaxonCitationList();
        List<String> taxonCitations = taxonCitationList.getTaxonCitation();
        taxonCitations.add(TEST_TAXON_CITATION);

        TaxonAuthorGroupList taxonAuthorGroupList = new TaxonAuthorGroupList();
        List<TaxonAuthorGroup> taxonAuthorGroups = taxonAuthorGroupList.getTaxonAuthorGroup();
        TaxonAuthorGroup taxonAuthorGroup = new TaxonAuthorGroup();
        taxonAuthorGroup.setTaxonAuthor(TEST_TAXON_AUTHOR);
        taxonAuthorGroup.setTaxonAuthorType(TEST_TAXON_AUTHOR_TYPE);
        taxonAuthorGroups.add(taxonAuthorGroup);

        // FIXME: Add additional fields in the Taxon record here,
        // including at least one each of:
        // * a Boolean field (when implemented)
        // * an authref field (when implemented)

        String newID = TaxonomyAuthorityClientUtils.createItemInAuthority(vcsid,
                authRefName, taxonMap, taxonAuthorGroupList, taxonCitationList, client);

        // Store the ID returned from the first item resource created
        // for additional tests below.
        if (knownItemResourceId == null) {
            setKnownItemResource(newID, TEST_SHORTID);
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": knownItemResourceId=" + newID + " inAuthority=" + vcsid);
            }
        }

        // Store the IDs from any item resources created
        // by tests, along with the IDs of their parents, so these items
        // can be deleted after all tests have been run.
        allItemResourceIdsCreated.put(newID, vcsid);

        return newID;
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void createWithEmptyEntityBody(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithMalformedXml(java.lang.String)
     */
    @Override
    public void createWithMalformedXml(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void createWithWrongXmlSchema(String testName) throws Exception {
    }

    // ---------------------------------------------------------------
    // CRUD tests : CREATE LIST tests
    // ---------------------------------------------------------------
    // Success outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#createList(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"createList"}, dependsOnGroups = {"create"})
    public void createList(String testName) throws Exception {
        for (int i = 0; i < nItemsToCreateInList; i++) {
            create(testName);
        }
    }

    /**
     * Creates the item list.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"createList"}, dependsOnMethods = {"createList"})
    public void createItemList(String testName) throws Exception {
        // Add items to the initially-created, known parent record.
        for (int j = 0; j < nItemsToCreateInList; j++) {
            createItem(testName);
        }
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
    groups = {"read"}, dependsOnGroups = {"create"})
    public void read(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        String newID = null;
        ClientResponse<String> res = client.read(knownResourceId);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
            //FIXME: remove the following try catch once Aron fixes signatures
            try {
                PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
                TaxonomyauthorityCommon taxonomyAuthority =
                        (TaxonomyauthorityCommon) extractPart(input,
                        client.getCommonPartName(), TaxonomyauthorityCommon.class);
                Assert.assertNotNull(taxonomyAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Read by name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"read"}, dependsOnGroups = {"create"})
    public void readByName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName + "(" + knownResourceShortIdentifer + ")", CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readByName(knownResourceShortIdentifer);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
            //FIXME: remove the following try catch once Aron fixes signatures
            try {
                PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
                TaxonomyauthorityCommon taxonomyAuthority = (TaxonomyauthorityCommon) extractPart(input,
                        client.getCommonPartName(), TaxonomyauthorityCommon.class);
                Assert.assertNotNull(taxonomyAuthority);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Read item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"read"}, dependsOnMethods = {"read"})
    public void readItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Check whether we've received an authority item record.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            TaxonCommon taxon = (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(taxon);
            boolean showFull = true;
            if (showFull && logger.isDebugEnabled()) {
                logger.debug(testName + ": returned payload:");
                logger.debug(objectAsXmlString(taxon, TaxonCommon.class));
            }

            // Check that this authority item record has the correct identifier
            // pointing to its parent authority record.
            Assert.assertNotNull(taxon.getInAuthority(), "inAuthority field value is unexpectedly null.");
            Assert.assertEquals(taxon.getInAuthority(), knownResourceId,
                    "Value of item's inAuthority field does not contain the correct identifier "
                    + "pointing to its parent authority record.");

            // Check individual fields in the authority item record.
            Assert.assertNotNull(taxon.getTaxonFullName(), "Field value is unexpectedly null.");
            Assert.assertEquals(taxon.getTaxonFullName(), TEST_TAXON_FULL_NAME,
                    "Field value " + taxon.getTaxonFullName()
                    + "does not match expected value " + TEST_TAXON_FULL_NAME);
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Verify item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"readItem", "updateItem"})
    public void verifyItemDisplayName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Check whether taxon has expected displayName.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            TaxonCommon taxon = (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(taxon);
            String displayName = taxon.getDisplayName();
            // Make sure displayName matches computed form
            String expectedDisplayName =
                    TaxonomyAuthorityClientUtils.prepareDefaultDisplayName(TEST_TAXON_FULL_NAME);
            Assert.assertNotNull(displayName, expectedDisplayName);

            // Update the shortName and verify the computed name is updated.
            taxon.setCsid(null);
            taxon.setDisplayNameComputed(true);
            taxon.setTaxonFullName("updated-" + TEST_TAXON_FULL_NAME);
            expectedDisplayName =
                    TaxonomyAuthorityClientUtils.prepareDefaultDisplayName("updated-" + TEST_TAXON_FULL_NAME);

            // Submit the updated resource to the service and store the response.
            PoxPayloadOut output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
            PayloadOutputPart commonPart = output.addPart(taxon, MediaType.APPLICATION_XML_TYPE);
            commonPart.setLabel(client.getItemCommonPartName());
            res.releaseConnection();
            res = client.updateItem(knownResourceId, knownItemResourceId, output);
            statusCode = res.getStatus();

            // Check the status code of the response: does it match the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug("updateItem: status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
            TaxonCommon updatedTaxon =
                    (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(updatedTaxon);

            // Verify that the updated resource received the correct data.
            Assert.assertEquals(updatedTaxon.getTaxonFullName(), taxon.getTaxonFullName(),
                    "Updated ForeName in Taxonomy did not match submitted data.");
            // Verify that the updated resource computes the right displayName.
            Assert.assertEquals(updatedTaxon.getDisplayName(), expectedDisplayName,
                    "Updated ForeName in Taxonomy not reflected in computed DisplayName.");

            // Now Update the displayName, not computed and verify the computed name is overriden.
            taxon.setDisplayNameComputed(false);
            expectedDisplayName = "TestName";
            taxon.setDisplayName(expectedDisplayName);

            // Submit the updated resource to the service and store the response.
            output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
            commonPart = output.addPart(taxon, MediaType.APPLICATION_XML_TYPE);
            commonPart.setLabel(client.getItemCommonPartName());
            res.releaseConnection();
            res = client.updateItem(knownResourceId, knownItemResourceId, output);
            statusCode = res.getStatus();

            // Check the status code of the response: does it match the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug("updateItem: status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
            updatedTaxon =
                    (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(updatedTaxon);

            // Verify that the updated resource received the correct data.
            Assert.assertEquals(updatedTaxon.isDisplayNameComputed(), false,
                    "Updated displayNameComputed in Taxonomy did not match submitted data.");
            // Verify that the updated resource computes the right displayName.
            Assert.assertEquals(updatedTaxon.getDisplayName(),
                    expectedDisplayName,
                    "Updated DisplayName (not computed) in Taxonomy not stored.");
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Verify illegal item display name.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    dependsOnMethods = {"verifyItemDisplayName"})
    public void verifyIllegalItemDisplayName(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        // FIXME: create a setup configuration for this operation.
        setupUpdateWithWrongXmlSchema();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, knownItemResourceId);
        try {
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match
            // the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, Response.Status.OK.getStatusCode());

            // Check whether Taxonomy has expected displayName.
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            TaxonCommon taxon = (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(taxon);
            // Try to Update with computed false and no displayName
            taxon.setDisplayNameComputed(false);
            taxon.setDisplayName(null);

            // Submit the updated resource to the service and store the response.
            PoxPayloadOut output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
            PayloadOutputPart commonPart = output.addPart(taxon, MediaType.APPLICATION_XML_TYPE);
            commonPart.setLabel(client.getItemCommonPartName());
            res.releaseConnection();
            res = client.updateItem(knownResourceId, knownItemResourceId, output);
            statusCode = res.getStatus();

            // Check the status code of the response: does it match the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug("updateItem: status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        } finally {
            res.releaseConnection();
        }
    }

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"read"}, dependsOnMethods = {"read"})
    public void readNonExistent(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.read(NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

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

    /**
     * Read item non existent.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"read"}, dependsOnMethods = {"readItem"})
    public void readItemNonExistent(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadNonExistent();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.readItem(knownResourceId, NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

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

    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------
    // Success outcomes

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#readList(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"readList"}, dependsOnGroups = {"createList", "read"})
    public void readList(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<AbstractCommonList> res = client.readList();
        try {
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
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Read item list.
     */
    @Test(groups = {"readList"}, dependsOnMethods = {"readList"})
    public void readItemList() {
        String testName = "readItemList";
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        readItemList(knownResourceId, null);
    }

    /**
     * Read item list by authority name.
     */
    @Test(groups = {"readList"}, dependsOnMethods = {"readItemList"})
    public void readItemListByAuthorityName() {
        String testName = "readItemListByAuthorityName";
        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        readItemList(null, knownResourceShortIdentifer);
    }

    /**
     * Read item list.
     *
     * @param vcsid the vcsid
     * @param name the name
     */
    private void readItemList(String vcsid, String shortId) {

        String testName = "readItemList";

        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<AbstractCommonList> res = null;
        if (vcsid != null) {
            res = client.readItemList(vcsid, null, null);
        } else if (shortId != null) {
            res = client.readItemListForNamedAuthority(shortId, null, null);
        } else {
            Assert.fail("readItemList passed null csid and name!");
        }
        try {
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

            List<AbstractCommonList.ListItem> items =
                    list.getListItem();
            int nItemsReturned = items.size();
            // There will be one item created, associated with a
            // known parent resource, by the createItem test.
            //
            // In addition, there will be 'nItemsToCreateInList'
            // additional items created by the createItemList test,
            // all associated with the same parent resource.
            int nExpectedItems = nItemsToCreateInList + 1;
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": Expected "
                        + nExpectedItems + " items; got: " + nItemsReturned);
            }
            Assert.assertEquals(nItemsReturned, nExpectedItems);

            for (AbstractCommonList.ListItem item : items) {
            	String value = 
            		AbstractCommonListUtils.ListItemGetElementValue(item, REFNAME);
                Assert.assertTrue((null != value), "Item refName is null!");
            	value = 
            		AbstractCommonListUtils.ListItemGetElementValue(item, DISPLAYNAME);
                Assert.assertTrue((null != value), "Item displayName is null!");
            }
            if(logger.isTraceEnabled()){
            	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, logger, testName);
            }
        } finally {
            res.releaseConnection();
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
    groups = {"update"}, dependsOnGroups = {"read", "readList"})
    public void update(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res = client.read(knownResourceId);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": read status = " + res.getStatus());
            }
            Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

            if (logger.isDebugEnabled()) {
                logger.debug("got TaxonomyAuthority to update with ID: " + knownResourceId);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            TaxonomyauthorityCommon taxonomyAuthority = (TaxonomyauthorityCommon) extractPart(input,
                    client.getCommonPartName(), TaxonomyauthorityCommon.class);
            Assert.assertNotNull(taxonomyAuthority);

            // Update the contents of this resource.
            taxonomyAuthority.setDisplayName("updated-" + taxonomyAuthority.getDisplayName());
            taxonomyAuthority.setVocabType("updated-" + taxonomyAuthority.getVocabType());
            if (logger.isDebugEnabled()) {
                logger.debug("to be updated TaxonomyAuthority");
                logger.debug(objectAsXmlString(taxonomyAuthority, TaxonomyauthorityCommon.class));
            }

            // Submit the updated resource to the service and store the response.
            PoxPayloadOut output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_PAYLOAD_NAME);
            PayloadOutputPart commonPart = output.addPart(taxonomyAuthority, MediaType.APPLICATION_XML_TYPE);
            commonPart.setLabel(client.getCommonPartName());
            res.releaseConnection();
            res = client.update(knownResourceId, output);
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
            TaxonomyauthorityCommon updatedTaxonomyAuthority =
                    (TaxonomyauthorityCommon) extractPart(input,
                    client.getCommonPartName(), TaxonomyauthorityCommon.class);
            Assert.assertNotNull(updatedTaxonomyAuthority);

            // Verify that the updated resource received the correct data.
            Assert.assertEquals(updatedTaxonomyAuthority.getDisplayName(),
                    taxonomyAuthority.getDisplayName(),
                    "Data in updated object did not match submitted data.");
        } finally {
            res.releaseConnection();
        }
    }

    /**
     * Update item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"update"}, dependsOnMethods = {"update"})
    public void updateItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdate();

        // Retrieve the contents of a resource to update.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<String> res =
                client.readItem(knownResourceId, knownItemResourceId);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": read status = " + res.getStatus());
            }
            Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);

            if (logger.isDebugEnabled()) {
                logger.debug("got Taxonomy to update with ID: "
                        + knownItemResourceId
                        + " in TaxonomyAuthority: " + knownResourceId);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            TaxonCommon taxon = (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(taxon);

            // Update the contents of this resource.
            taxon.setCsid(null);
            taxon.setTaxonFullName("updated-" + taxon.getTaxonFullName());
            if (logger.isDebugEnabled()) {
                logger.debug("to be updated Taxonomy");
                logger.debug(objectAsXmlString(taxon,
                        TaxonCommon.class));
            }

            // Submit the updated resource to the service and store the response.
            PoxPayloadOut output = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
            PayloadOutputPart commonPart = output.addPart(taxon, MediaType.APPLICATION_XML_TYPE);
            commonPart.setLabel(client.getItemCommonPartName());
            res.releaseConnection();
            res = client.updateItem(knownResourceId, knownItemResourceId, output);
            int statusCode = res.getStatus();

            // Check the status code of the response: does it match the expected response(s)?
            if (logger.isDebugEnabled()) {
                logger.debug(testName + ": status = " + statusCode);
            }
            Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
                    invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

            // Retrieve the updated resource and verify that its contents exist.
            input = new PoxPayloadIn(res.getEntity());
            TaxonCommon updatedTaxon =
                    (TaxonCommon) extractPart(input,
                    client.getItemCommonPartName(), TaxonCommon.class);
            Assert.assertNotNull(updatedTaxon);

            // Verify that the updated resource received the correct data.
            Assert.assertEquals(updatedTaxon.getTaxonFullName(), taxon.getTaxonFullName(),
                    "Data in updated Taxon did not match submitted data.");
        } finally {
            res.releaseConnection();
        }
    }

    // Failure outcomes
    // Placeholders until the three tests below can be uncommented.
    // See Issue CSPACE-401.
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithEmptyEntityBody(java.lang.String)
     */
    @Override
    public void updateWithEmptyEntityBody(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithMalformedXml(java.lang.String)
     */
    @Override
    public void updateWithMalformedXml(String testName) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateWithWrongXmlSchema(java.lang.String)
     */
    @Override
    public void updateWithWrongXmlSchema(String testName) throws Exception {
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#updateNonExistent(java.lang.String)
     */
    @Override
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"update"}, dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID(s) used when creating the request payload may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        String displayName = "displayName-NON_EXISTENT_ID";
        PoxPayloadOut multipart = TaxonomyAuthorityClientUtils.createTaxonomyAuthorityInstance(
                displayName, "nonEx", client.getCommonPartName());
        ClientResponse<String> res =
                client.update(NON_EXISTENT_ID, multipart);
        try {
            int statusCode = res.getStatus();

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

    /**
     * Update non existent item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"update"}, dependsOnMethods = {"updateItem", "testItemSubmitRequest"})
    public void updateNonExistentItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        Map<String, String> nonexMap = new HashMap<String, String>();
        nonexMap.put(TaxonJAXBSchema.NAME, TEST_TAXON_FULL_NAME);
        nonexMap.put(TaxonJAXBSchema.SHORT_IDENTIFIER, "nonEx");
        nonexMap.put(TaxonJAXBSchema.TERM_STATUS, TEST_TERM_STATUS);
        PoxPayloadOut multipart =
                TaxonomyAuthorityClientUtils.createTaxonInstance(
                TaxonomyAuthorityClientUtils.createTaxonomyRefName(knownResourceRefName, "nonEx", "Non Existent"),
                nonexMap, NULL_TAXON_AUTHOR_GROUP_LIST, NULL_TAXON_CITATION_LIST,
                client.getItemCommonPartName());
        ClientResponse<String> res =
                client.updateItem(knownResourceId, NON_EXISTENT_ID, multipart);
        try {
            int statusCode = res.getStatus();

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

    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------
    // Success outcomes
    // Note: delete sub-resources in ascending hierarchical order,
    // before deleting their parents.
    /**
     * Delete item.
     *
     * @param testName the test name
     * @throws Exception the exception
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    //groups = {"delete"}, dependsOnGroups = {"create", "read", "readList", "readListByPartialTerm", "update"})
    groups = {"delete"}, dependsOnGroups = {"create", "read", "readList", "update"})
    public void deleteItem(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        if (logger.isDebugEnabled()) {
            logger.debug("parentcsid =" + knownResourceId
                    + " itemcsid = " + knownItemResourceId);
        }

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<Response> res =
                client.deleteItem(knownResourceId, knownItemResourceId);
        try {
            int statusCode = res.getStatus();

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

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#delete(java.lang.String)
     */
    @Override
    // @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    // groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void delete(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDelete();

        if (logger.isDebugEnabled()) {
            logger.debug("parentcsid =" + knownResourceId);
        }

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<Response> res = client.delete(knownResourceId);
        try {
            int statusCode = res.getStatus();

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

    // Failure outcomes
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.AbstractServiceTestImpl#deleteNonExistent(java.lang.String)
     */
    @Override
    // @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    // groups = {"delete"}, dependsOnMethods = {"delete"})
    public void deleteNonExistent(String testName) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

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

    /**
     * Delete non existent item.
     *
     * @param testName the test name
     */
    @Test(dataProvider = "testName", dataProviderClass = AbstractServiceTestImpl.class,
    groups = {"delete"}, dependsOnMethods = {"deleteItem"})
    public void deleteNonExistentItem(String testName) {

        if (logger.isDebugEnabled()) {
            logger.debug(testBanner(testName, CLASS_NAME));
        }
        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        ClientResponse<Response> res = client.deleteItem(knownResourceId, NON_EXISTENT_ID);
        try {
            int statusCode = res.getStatus();

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
        if (logger.isDebugEnabled()) {
            logger.debug("testSubmitRequest: url=" + url
                    + " status=" + statusCode);
        }
        Assert.assertEquals(statusCode, EXPECTED_STATUS);

    }

    /**
     * Test item submit request.
     */
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
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
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
        String parentResourceId;
        String itemResourceId;
        // Clean up contact resources.
        TaxonomyAuthorityClient client = new TaxonomyAuthorityClient();
        parentResourceId = knownResourceId;
        // Clean up item resources.
        for (Map.Entry<String, String> entry : allItemResourceIdsCreated.entrySet()) {
            itemResourceId = entry.getKey();
            parentResourceId = entry.getValue();
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res =
                    client.deleteItem(parentResourceId, itemResourceId);
            res.releaseConnection();
        }
        // Clean up parent resources.
        for (String resourceId : allResourceIdsCreated) {
            // Note: Any non-success responses from the delete operation
            // below are ignored and not reported.
            ClientResponse<Response> res = client.delete(resourceId);
            res.releaseConnection();
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getServicePathComponent()
     */
    /**
     * Returns the root URL for the item service.
     *
     * This URL consists of a base URL for all services, followed by
     * a path component for the owning parent, followed by the
     * path component for the items.
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @return The root URL for the item service.
     */
    protected String getItemServiceRootURL(String parentResourceIdentifier) {
        return getResourceURL(parentResourceIdentifier) + "/" + getItemServicePathComponent();
    }

    /**
     * Returns the URL of a specific item resource managed by a service, and
     * designated by an identifier (such as a universally unique ID, or UUID).
     *
     * @param  parentResourceIdentifier  An identifier (such as a UUID) for the
     * parent authority resource of the relevant item resource.
     *
     * @param  itemResourceIdentifier  An identifier (such as a UUID) for an
     * item resource.
     *
     * @return The URL of a specific item resource managed by a service.
     */
    protected String getItemResourceURL(String parentResourceIdentifier, String itemResourceIdentifier) {
        return getItemServiceRootURL(parentResourceIdentifier) + "/" + itemResourceIdentifier;
    }
}
