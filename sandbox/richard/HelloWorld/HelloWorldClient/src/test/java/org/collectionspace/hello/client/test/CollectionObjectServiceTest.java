package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.hello.CollectionObject;
import org.collectionspace.hello.CollectionObjectList;
import org.collectionspace.hello.PersonNuxeo;
import org.collectionspace.hello.client.CollectionObjectClient;

/**
 * A PersonNuxeoServiceTest.
 * 
 * @version $Revision:$
 */
public class CollectionObjectServiceTest {

    private CollectionObjectClient collectionObjectClient = CollectionObjectClient.getInstance();
    private String updateId = "";
    private String deleteId = "";

    @Test
    public void createCollectionObject() {
    	long identifier = this.createIdentifier();
    	
    	CollectionObject collectionObject = createCollectionObject(identifier);
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        
        //store updateId locally for "update" test
        updateId = extractId(res);        
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void updatePerson() {
    	ClientResponse<CollectionObject> res = collectionObjectClient.getCollectionObject(updateId);
        CollectionObject collectionObject = res.getEntity();
        verbose("got collectionobject to update: " + updateId,
        		collectionObject, CollectionObject.class);
        
        collectionObject.setCsid("updated-" + updateId);
        collectionObject.setIdentifier("updated-" + collectionObject.getIdentifier());
        collectionObject.setDescription("updated-" + collectionObject.getDescription());
        
        res = collectionObjectClient.updateCollectionObject(updateId, collectionObject);
        CollectionObject updatedCollectionObject = res.getEntity();
        Assert.assertEquals(updatedCollectionObject.getDescription(), "updated-" + collectionObject.getDescription());

        verbose("updated collectionObject", updatedCollectionObject, CollectionObject.class);
        
        return;
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void createCollection() {
    	for (int i = 0; i < 3; i++) {
    		this.createCollectionObject();
    	}
    }
    
    @Test(dependsOnMethods = {"createCollection"})
    public void getCollectionObjectList() {
        //the resource method is expected to return at least an empty list
        CollectionObjectList coList = collectionObjectClient.getCollectionObjectList().getEntity();
        List<CollectionObjectList.CollectionObjectListItem> coItemList = coList.getCollectionObjectListItem();
        int i = 0;
        for(CollectionObjectList.CollectionObjectListItem pli : coItemList) {
            verbose("getCollectionObjectList: list-item[" + i + "] csid=" + pli.getCsid());
            verbose("getCollectionObjectList: list-item[" + i + "] identifier=" + pli.getIdentifier());
            verbose("getCollectionObjectList: list-item[" + i + "] URI=" + pli.getUri());
            i++;
        }
    }

    @Test(dependsOnMethods = {"updateCollectionObject"})
    public void deleteCollectionObject() {
        ClientResponse<Response> res = collectionObjectClient.deleteCollectionObject(deleteId);
        verbose("deleteCollectionObject: csid=" + deleteId);
        verbose("deleteCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    private CollectionObject createCollectionObject(String csid, String identifier, String description) {
    	CollectionObject collectionObject = new CollectionObject();
    	
    	collectionObject.setCsid(csid);
    	collectionObject.setIdentifier(identifier);
    	collectionObject.setDescription(description);

        return collectionObject;
    }

    private CollectionObject createCollectionObject(long identifier) {
    	CollectionObject collectionObject = createCollectionObject("csid-" + identifier,
    			"did-" + identifier, "description-" + identifier);    	

        return collectionObject;
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("id=" + id);
        return id;
    }

    private void verbose(String msg) {
        System.out.println("PersonServiceTest : " + msg);
    }

    private void verbose(String msg, Object o, Class clazz) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void verboseMap(MultivaluedMap map) {
        for(Object entry : map.entrySet()){
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            verbose("    name=" + mentry.getKey() + " value=" + mentry.getValue());
        }
    }
    
    private long createIdentifier() {
    	long identifier = System.currentTimeMillis();
    	return identifier;
    }
}
