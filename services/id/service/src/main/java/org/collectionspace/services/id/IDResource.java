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
  
package org.collectionspace.services.id;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// May at some point instead use
// org.jboss.resteasy.spi.NotFoundException
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDResource
 *
 * Resource class to handle requests to the ID Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
// Set the base path component for URLs that access this service.
@Path("/idgenerators")
// Identify the default MIME media types consumed and produced by this service.
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class IDResource {

    final Logger logger = LoggerFactory.getLogger(IDResource.class);

    final static IDService service = new IDServiceJdbcImpl();

    //////////////////////////////////////////////////////////////////////
    /**
    * Constructor (no argument).
    */
    public IDResource() {
        // do nothing
    }

    //////////////////////////////////////////////////////////////////////
    /**
    * Generates and returns a new ID, from the specified ID generator.
    *
    * @param  csid  An identifier for an ID generator.
    *
    * @return  A new ID created ("generated") by the specified ID generator.
    */
    @POST
    @Path("/{csid}/ids")
    public Response newID(@PathParam("csid") String csid) {
    
        logger.debug("> in newID(String)");
    
        // @TODO The JavaDoc description reflects an as-yet-to-be-carried out
        // refactoring, in which the highest object type in the ID service
        // is that of an IDGenerator, some or all of which may be composed
        // of IDParts.  Some IDGenerators generate IDs based on patterns,
        // which may be composed in part of incrementing numeric or alphabetic
        // components, while others may not (e.g. UUIDs, web services-based
        // responses).
        
        // @TODO We're currently using simple integer IDs to identify ID generators
        // in this initial iteration.
        //
        // To uniquely identify ID generators in production, we'll need to handle
        // both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
        // other form of identifier to be determined, such as URLs or URNs.
        
        // @TODO We're currently returning IDs in plain text.  Identify whether
        // there is a requirement to return an XML representation, and/or any
        // other representations.
          
        // Unless the 'response' variable is explicitly initialized here,
        // the compiler gives the error: "variable response might not have
        // been initialized."
        Response response = null;
        response = response.ok().build();
        String newId = "";
        
        try {
        
            // Obtain a new ID from the specified ID generator,
            // and return it in the entity body of the response.
            newId = service.createID(csid);
                
            if (newId == null || newId.equals("")) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("ID Service returned null or empty ID")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
                return response;
            }
                    
            response = Response.status(Response.Status.CREATED)
              .entity(newId).type(MediaType.TEXT_PLAIN).build();
                
        // @TODO Return an XML-based error results format with the
        // responses below.
        
        // @TODO An IllegalStateException often indicates an overflow
        // of an IDPart.  Consider whether returning a 400 Bad Request
        // status code is still warranted, or whether returning some other
        // status would be more appropriate.
        
        } catch (DocumentNotFoundException dnfe) {
            response = Response.status(Response.Status.NOT_FOUND)
              .entity(dnfe.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (BadRequestException bre) {
            response = Response.status(Response.Status.BAD_REQUEST)
              .entity(bre.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (IllegalStateException ise) {
          response = Response.status(Response.Status.BAD_REQUEST)
              .entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();
        
        } catch (IllegalArgumentException iae) {
            response = Response.status(Response.Status.BAD_REQUEST)
              .entity(iae.getMessage()).type(MediaType.TEXT_PLAIN).build();
        
        // This is guard code that should never be reached.
        } catch (Exception e) {
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
        
        return response;
     
    }

    //////////////////////////////////////////////////////////////////////
    /**
    * Creates a new ID generator instance.
    *
    * @param  generatorRepresentation 
    *         A representation of an ID generator instance.
    */
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_XML)
    public Response createIDGenerator() {
    
        logger.debug("> in createIDGenerator(String)");
        
        // @TODO Implement this stubbed method

        // @TODO Replace this placeholder code.
        Response response = Response.status(Response.Status.CREATED)
              .entity("").type(MediaType.TEXT_PLAIN).build();

/*      
        // @TODO Replace this placeholder code.
        // Return a URL for the newly-created resource in the
        // Location header
        String csid = "TEST-1";
        String url = "/idgenerators/" + csid;
        List locationList = Collections.singletonList(url);
        response.getMetadata()
            .get("Location")
            .putSingle("Location", locationList);
*/

        return response;
        
    }

    //////////////////////////////////////////////////////////////////////
    /**
    * Returns a representation of a single ID generator instance resource.
    *
    * @param    csid  An identifier for an ID generator instance.
    *
    * @return  A representation of an ID generator instance resource.
    */
    @GET
    @Path("/{csid}")
    @Produces(MediaType.APPLICATION_XML)
    public Response readIDGenerator(@PathParam("csid") String csid) {
    
        logger.debug("> in readIDGenerator(String)");

        Response response = null;
        response = response.ok().build();
        String resourceRepresentation = "";
        
        try {
        
            resourceRepresentation = service.readIDGenerator(csid);
            
            if (resourceRepresentation == null ||
                resourceRepresentation.equals("")) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("ID Service returned null or empty ID Generator")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
                return response;
            }
                    
            response = Response.status(Response.Status.OK)
              .entity(resourceRepresentation)
              .type(MediaType.APPLICATION_XML)
              .build();
                
        // @TODO Return an XML-based error results format with the
        // responses below.
        
        } catch (DocumentNotFoundException dnfe) {
            response = Response.status(Response.Status.NOT_FOUND)
              .entity(dnfe.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (IllegalStateException ise) {
          response = Response.status(Response.Status.BAD_REQUEST)
              .entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();
        
        } catch (IllegalArgumentException iae) {
            response = Response.status(Response.Status.BAD_REQUEST)
              .entity(iae.getMessage()).type(MediaType.TEXT_PLAIN).build();
        
        // This is guard code that should never be reached.
        } catch (Exception e) {
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
        
        return response;

    }

    //////////////////////////////////////////////////////////////////////
    /**
    * Placeholder for retrieving a list of available ID Generator
    * instance resources.
    * 
    * Required to facilitate a HEAD method test in ServiceLayerTest.
    *
    * @return  A list of representations of ID generator instance resources.
    */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_XML)
    public Response readIDGeneratorsList() {
            
        logger.debug("> in readIDGeneratorsList()");

        Response response = null;
        response = response.ok().build();
        String resourceRepresentation = "";

        // @TODO Replace these placeholders/expedients
        // with a schema-defined list format.
        final String LIST_ROOT_START = "<list>";
        final String LIST_ROOT_END = "</list>";
        final String LIST_ITEM_START = "<item>";
        final String LIST_ITEM_END = "</item>";

        try {

            List<String> generators = service.readIDGeneratorsList();

            // @TODO Replace this placeholder/expedient, as per above
            StringBuffer sb = new StringBuffer("");
            sb.append(LIST_ROOT_START);
            for (String generator : generators) {
                sb.append(LIST_ITEM_START);
                sb.append(generator);
                sb.append(LIST_ITEM_END);
            }
            sb.append(LIST_ROOT_END);

            resourceRepresentation = sb.toString();

            response = Response.status(Response.Status.OK)
              .entity(resourceRepresentation)
              .type(MediaType.APPLICATION_XML)
              .build();

        // @TODO Return an XML-based error results format with the
        // responses below.

         } catch (IllegalStateException ise) {
          response = Response.status(Response.Status.BAD_REQUEST)
              .entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();

        // This is guard code that should never be reached.
        } catch (Exception e) {
          response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        return response;
    }


}
