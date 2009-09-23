/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.acquisition.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.AcquisitionJAXBSchema;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.acquisition.AcquisitionsCommon;
import org.collectionspace.services.acquisition.AcquisitionsCommonList;
import org.collectionspace.services.acquisition.AcquisitionsCommonList.AcquisitionListItem;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcquisitionDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class AcquisitionDocumentModelHandler
        extends DocumentModelHandler<AcquisitionsCommon, AcquisitionsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(AcquisitionDocumentModelHandler.class);
    /**
     * acquisition is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private AcquisitionsCommon acquisition;
    /**
     * acquisitionList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private AcquisitionsCommonList acquisitionList;

    @Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

    /**
     * getCommonPart get associated acquisition
     * @return
     */
    @Override
    public AcquisitionsCommon getCommonPart() {
        return acquisition;
    }

    /**
     * setCommonPart set associated acquisition
     * @param acquisition
     */
    @Override
    public void setCommonPart(AcquisitionsCommon acquisition) {
        this.acquisition = acquisition;
    }

    /**
     * getAcquisitionList get associated acquisition (for index/GET_ALL)
     * @return
     */
    @Override
    public AcquisitionsCommonList getCommonPartList() {
        return acquisitionList;
    }

    @Override
    public void setCommonPartList(AcquisitionsCommonList acquisitionList) {
        this.acquisitionList = acquisitionList;
    }

    @Override
    public AcquisitionsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(AcquisitionsCommon acquisitionObject, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public AcquisitionsCommonList extractCommonPartList(DocumentWrapper wrapDoc) throws Exception {
        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();

        AcquisitionsCommonList coList = new AcquisitionsCommonList();
        List<AcquisitionsCommonList.AcquisitionListItem> list = coList.getAcquisitionListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            AcquisitionListItem listItem = new AcquisitionListItem();
            listItem.setAccessiondate((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    AcquisitionJAXBSchema.ACCESSIONDATE));
            //need fully qualified context for URI
            String id = docModel.getId();
            listItem.setUri(getServiceContextPath() + id);
            listItem.setCsid(id);
            list.add(listItem);
        }

        return coList;
    }

    @Override
    public void fillAllParts(DocumentWrapper wrapDoc) throws Exception {

        super.fillAllParts(wrapDoc);
        fillDublinCoreObject(wrapDoc); //dublincore might not be needed in future
    }

    private void fillDublinCoreObject(DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding
        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", AcquisitionConstants.NUXEO_DC_TITLE);
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getDocumentType()
     */
    public String getDocumentType() {
        return AcquisitionConstants.NUXEO_DOCTYPE;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return AcquisitionConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

