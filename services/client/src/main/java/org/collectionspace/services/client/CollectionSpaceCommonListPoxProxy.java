package org.collectionspace.services.client;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

public interface CollectionSpaceCommonListPoxProxy extends CollectionSpacePoxProxy<AbstractCommonList> {
    @GET
    ClientResponse<AbstractCommonList> readList();
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> keywordSearchIncludeDeleted(
    	    @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOW_QUERY_NONDELETED) String includeDeleted);

}
