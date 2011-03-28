/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Copyright © 2009 Regents of the University of California
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.collectionspace.services.client;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.movement.MovementsCommonList;

/**
 * MovementProxy.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
@Path("/movements/")
@Produces("application/xml")
@Consumes("application/xml")
public interface MovementProxy extends CollectionSpacePoxProxy {

    // List
    @GET
    @Produces({"application/xml"})
    ClientResponse<MovementsCommonList> readList();
    
    // Sorted list
    @GET
    @Produces({"application/xml"})
    ClientResponse<MovementsCommonList> readListSortedBy(
        @QueryParam(IClientQueryParams.SORT_BY_PARAM) String sortFieldName);

    @GET
    @Produces({"application/xml"})
    ClientResponse<MovementsCommonList> keywordSearchSortedBy(
        @QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
        @QueryParam(IClientQueryParams.SORT_BY_PARAM) String sortFieldName);
}
