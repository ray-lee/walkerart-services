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

import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;
import org.collectionspace.services.relation.RelationshipType;

import org.jboss.resteasy.client.ClientResponse;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * RelationServiceTest, carries out tests against a
 * deployed and running Relation Service.
 * 
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class RelationServiceTest extends AbstractServiceTest {

    private RelationClient client = new RelationClient();
    final String SERVICE_PATH_COMPONENT = "relations";
    private String knownResourceId = null; 

    // ---------------------------------------------------------------
    // CRUD tests : CREATE tests
    // ---------------------------------------------------------------

    // Success outcomes
    
    @Override
    @Test
    public void create() {

        // Perform setup, such as initializing the type of service request
        // (e.g. CREATE, DELETE), its valid and expected status codes, and
        // its associated HTTP method name (e.g. POST, DELETE).
        setupCreate();

        // Submit the request to the service and store the response.
        String identifier = createIdentifier();
        Relation relation = createRelationInstance(identifier);
        ClientResponse<Response> res = client.create(relation);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        //
        // Does it fall within the set of valid status codes?
        // Does it exactly match the expected status code?
        verbose("create: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Store the ID returned from this create operation for
        // additional tests below.
        knownResourceId = extractId(res);
    }

    @Override
    @Test(dependsOnMethods = {"create"})
    public void createList() {
        for(int i = 0; i < 3; i++){
            create();
        }
    }

    // Failure outcomes

    // Placeholders until the two tests below can be uncommented.
    // See Issue CSPACE-401.
    public void createWithMalformedXml() {}
    public void createWithWrongXmlSchema() {}

/*
    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithMalformedXml() {
    
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
        verbose("createWithMalformedXml url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dependsOnMethods = {"create", "testSubmitRequest"})
    public void createWithWrongXmlSchema() {
    
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
        verbose("createWithWrongSchema url=" + url + " status=" + statusCode);
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
    @Test(dependsOnMethods = {"create"})
    public void read() {
    
        // Perform setup.
        setupRead();

        // Submit the request to the service and store the response.
        ClientResponse<Relation> res = client.read(knownResourceId);
        int statusCode = res.getStatus();
            
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("read: status = " + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Verify that the resource identifier 
		Relation returnedRelation = res.getEntity();
		Assert.assertEquals(returnedRelation.getCsid(), knownResourceId);

    }

    // Failure outcomes
    
    @Override
    @Test(dependsOnMethods = {"read"})
    public void readNonExistent() {

        // Perform setup.
        setupReadNonExistent();
        
        // Submit the request to the service and store the response.
        ClientResponse<Relation> res = client.read(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("readNonExistent: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }


    // ---------------------------------------------------------------
    // CRUD tests : READ_LIST tests
    // ---------------------------------------------------------------

    // Success outcomes

    @Override
    @Test(dependsOnMethods = {"createList"})
    public void readList() {
    
        // Perform setup.
        setupReadList();

        // Submit the request to the service and store the response.
        ClientResponse<RelationList> res = client.readList();
        RelationList list = res.getEntity();
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("readList: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

        // Optionally output additional data about list members for debugging.
        boolean iterateThroughList = false;
        if (iterateThroughList && logger.isDebugEnabled()) {
            List<RelationList.RelationListItem> items =
                list.getRelationListItem();
            int i = 0;
            for(RelationList.RelationListItem item : items){
                verbose("readList: list-item[" + i + "] csid=" +
                    item.getCsid());
                verbose("readList: list-item[" + i + "] URI=" +
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

    @Override
    @Test(dependsOnMethods = {"create"})
    public void update() {
    
        // Perform setup.
        setupUpdate();

        // Retrieve an existing resource that we can update.
        ClientResponse<Relation> res = client.read(knownResourceId);
        verbose("read: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), EXPECTED_STATUS_CODE);
        Relation relation = res.getEntity();
        verbose("Got object to update with ID: " + knownResourceId,
                relation, Relation.class);

        // Update the content of this resource.
        relation.setDocumentId1("updated-" + relation.getDocumentId1());
        relation.setDocumentType1("updated-" + relation.getDocumentType1());
        relation.setDocumentId2("updated-" + relation.getDocumentId2());
        relation.setDocumentType2("updated-" + relation.getDocumentType2());

        // Submit the request to the service and store the response.
        res = client.update(knownResourceId, relation);
        int statusCode = res.getStatus();
        Relation updatedObject = res.getEntity();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("update: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
        
        // Check the contents of the response: does it match
        // what was submitted?
        verbose("update: ", updatedObject, Relation.class);
        final String msg =
          "Data in updated object did not match submitted data.";
        Assert.assertEquals(
          updatedObject.getDocumentId1(), relation.getDocumentId1(), msg);
        Assert.assertEquals(
          updatedObject.getDocumentType1(), relation.getDocumentType1(), msg);
        Assert.assertEquals(
          updatedObject.getDocumentId2(), relation.getDocumentId2(), msg);
        Assert.assertEquals(
          updatedObject.getDocumentType2(), relation.getDocumentType2(), msg);

    }
    
    // Failure outcomes

    // Placeholders until the two tests below can be uncommented.
    // See Issue CSPACE-401.
    public void updateWithMalformedXml() {}
    public void updateWithWrongXmlSchema() {}

/*
    @Override
    @Test(dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithMalformedXml() {

        // Perform setup.
        setupUpdateWithMalformedXml();

        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = MALFORMED_XML_DATA; // Constant from abstract base class.
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateWithMalformedXml: url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    @Override
    @Test(dependsOnMethods = {"create", "update", "testSubmitRequest"})
    public void updateWithWrongXmlSchema() {
    
        // Perform setup.
        setupUpdateWithWrongXmlSchema();
        
        // Submit the request to the service and store the response.
        String method = REQUEST_TYPE.httpMethodName();
        String url = getResourceURL(knownResourceId);
        String mediaType = MediaType.APPLICATION_XML;
        final String entity = WRONG_XML_SCHEMA_DATA; // Constant from abstract base class.
        int statusCode = submitRequest(method, url, mediaType, entity);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateWithWrongSchema: url=" + url + " status=" + statusCode);
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }
*/


    @Override
    @Test(dependsOnMethods = {"update", "testSubmitRequest"})
    public void updateNonExistent() {

        // Perform setup.
        setupUpdateNonExistent();

        // Submit the request to the service and store the response.
        // Note: The ID used in this 'create' call may be arbitrary.
        // The only relevant ID may be the one used in update(), below.
        Relation relation = createRelationInstance(NON_EXISTENT_ID);
        ClientResponse<Relation> res =
          client.update(NON_EXISTENT_ID, relation);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("updateNonExistent: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }


    // ---------------------------------------------------------------
    // CRUD tests : DELETE tests
    // ---------------------------------------------------------------

    // Success outcomes

    @Override
    @Test(dependsOnMethods = {"create", "read", "update"})
    public void delete() {

        // Perform setup.
        setupDelete();

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(knownResourceId);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("delete: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }

    // Failure outcomes

    @Override
    @Test(dependsOnMethods = {"delete"})
    public void deleteNonExistent() {

        // Perform setup.
        setupDeleteNonExistent();

        // Submit the request to the service and store the response.
        ClientResponse<Response> res = client.delete(NON_EXISTENT_ID);
        int statusCode = res.getStatus();

        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("deleteNonExistent: status = " + res.getStatus());
        Assert.assertTrue(REQUEST_TYPE.isValidStatusCode(statusCode),
            invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);
    }


    // ---------------------------------------------------------------
    // RELATE_OBJECT tests
    // ---------------------------------------------------------------
    
    @Test(dependsOnMethods = {"create"})
    public void relateObjects() {
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
        final int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();

        // Submit the request to the service and store the response.
        String method = ServiceRequestType.READ.httpMethodName();
        String url = getResourceURL(knownResourceId);
        int statusCode = submitRequest(method, url);
        
        // Check the status code of the response: does it match
        // the expected response(s)?
        verbose("testSubmitRequest: url=" + url + " status=" + statusCode);
        Assert.assertEquals(statusCode, EXPECTED_STATUS_CODE);

    }		


    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    private Relation createRelationInstance(String identifier) {
        String result = null;
        Relation relation = new Relation();
        fillRelation(relation, identifier);
        return relation;
    }
    
    /**
     * Fills the relation.
     * 
     * @param identifier the identifier
     * 
     * @return the relation
     */
    private void fillRelation(Relation relation, String identifier) {
        fillRelation(relation, "Subject-" + identifier,
            "SubjectType-" + identifier + "-type",
            "Object-" + identifier,
            "ObjectType-" + identifier + "-type",
            RelationshipType.COLLECTIONOBJECT_INTAKE);
    }

    /**
     * Fills the relation.
     * 
     * @param documentId1 the document id1
     * @param documentType1 the document type1
     * @param documentId2 the document id2
     * @param documentType2 the document type2
     * @param rt the rt
     * 
     * @return the relation
     */
    private void fillRelation(Relation relation,
        String documentId1, String documentType1,
        String documentId2, String documentType2,
        RelationshipType rt)
    {
        relation.setDocumentId1(documentId1);
        relation.setDocumentType1(documentType1);
        relation.setDocumentId2(documentId2);
        relation.setDocumentType2(documentType2);
        
        relation.setRelationshipType(rt);
    }

}
