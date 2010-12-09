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
package org.collectionspace.services.blob.nuxeo;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.BlobJAXBSchema;

import org.collectionspace.services.common.blob.BlobInput;
import org.collectionspace.services.common.blob.BlobOutput;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoImageUtils;
import org.collectionspace.services.common.DocHandlerBase;

import org.collectionspace.services.blob.BlobsCommonList;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.blob.BlobsCommonList.BlobListItem;

import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.ClientException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BlobDocumentModelHandler.
 */
public class BlobDocumentModelHandler
extends DocHandlerBase<BlobsCommon, AbstractCommonList> {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(BlobDocumentModelHandler.class);

	public final String getNuxeoSchemaName(){
		return "blobs";
	}

	public String getSummaryFields(AbstractCommonList commonList){
		return "name|mimeType|encoding|length|uri|csid";
	}

	public AbstractCommonList createAbstractCommonListImpl(){
		return new BlobsCommonList();
	}

	public List createItemsList(AbstractCommonList commonList){
		List list = ((BlobsCommonList)commonList).getBlobListItem();
		return list;
	}
	
	private String getDerivativePathBase(DocumentModel docModel) {
		return getServiceContextPath() + docModel.getName() + "/" +
			BlobInput.URI_DERIVATIVES_PATH + "/";
	}

	public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
		BlobListItem item = new BlobListItem();
		item.setEncoding((String)
				docModel.getProperty(label, BlobJAXBSchema.encoding));
		item.setMimeType((String)
				docModel.getProperty(label, BlobJAXBSchema.mimeType));
		//String theData = (String)
		docModel.getProperty(label, BlobJAXBSchema.data);
		item.setName((String)
				docModel.getProperty(label, BlobJAXBSchema.name));
		item.setLength((String)
				docModel.getProperty(label, BlobJAXBSchema.length));
		item.setUri(getServiceContextPath() + id);
		item.setCsid(id);
		return item;
	}

	private BlobsCommon getCommonPartProperties(DocumentModel docModel) throws Exception {
		String label = getServiceContext().getCommonPartLabel();
		BlobsCommon result = new BlobsCommon();
		
		result.setData((String)	
				docModel.getProperty(label, BlobJAXBSchema.data));
		result.setDigest((String)
				docModel.getProperty(label, BlobJAXBSchema.digest));
		result.setEncoding((String)
				docModel.getProperty(label, BlobJAXBSchema.encoding));
		result.setLength((String)
				docModel.getProperty(label, BlobJAXBSchema.length));
		result.setMimeType((String)
				docModel.getProperty(label, BlobJAXBSchema.mimeType));
		result.setName((String)
				docModel.getProperty(label, BlobJAXBSchema.name));
		result.setRepositoryId((String)
				docModel.getProperty(label, BlobJAXBSchema.repositoryId));
		result.setUri(getServiceContextPath() + docModel.getName() + "/" +
				BlobInput.URI_CONTENT_PATH);
		
		return result;
	}
	
	private void setCommonPartProperties(DocumentModel documentModel,
			BlobsCommon blobsCommon) throws ClientException {
		String label = getServiceContext().getCommonPartLabel();
		documentModel.setProperty(label, BlobJAXBSchema.data, blobsCommon.getData());
		documentModel.setProperty(label, BlobJAXBSchema.digest,	blobsCommon.getDigest());
		documentModel.setProperty(label, BlobJAXBSchema.encoding, blobsCommon.getEncoding());
		documentModel.setProperty(label, BlobJAXBSchema.length, blobsCommon.getLength());
		documentModel.setProperty(label, BlobJAXBSchema.mimeType, blobsCommon.getMimeType());
		documentModel.setProperty(label, BlobJAXBSchema.name, blobsCommon.getName());
		documentModel.setProperty(label, BlobJAXBSchema.uri, blobsCommon.getUri());
		documentModel.setProperty(label, BlobJAXBSchema.repositoryId, blobsCommon.getRepositoryId());
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractAllParts(org.collectionspace.services.common.document.DocumentWrapper)
	 */
	@Override
	public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
			throws Exception {
		ServiceContext ctx = this.getServiceContext();
		RepositoryInstance repoSession = this.getRepositorySession();
		DocumentModel docModel = wrapDoc.getWrappedObject();
		BlobsCommon blobsCommon = this.getCommonPartProperties(docModel);		
		String blobRepositoryId = blobsCommon.getRepositoryId(); //cache the value to pass to the blob retriever
		
		if (ctx.getProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY) != null) {
			BlobsCommonList blobsCommonList = NuxeoImageUtils.getBlobDerivatives(
					repoSession, blobRepositoryId, getDerivativePathBase(docModel));
			ctx.setProperty(BlobInput.BLOB_DERIVATIVE_LIST_KEY, blobsCommonList);
			return;  //FIXME: Don't like this exit point.  Perhaps derivatives should be a sub-resource?
		}		

		String derivativeTerm = (String)ctx.getProperty(BlobInput.BLOB_DERIVATIVE_TERM_KEY);
		Boolean getContentFlag = ctx.getProperty(BlobInput.BLOB_CONTENT_KEY) != null ? true : false;
		BlobOutput blobOutput = NuxeoImageUtils.getBlobOutput(ctx, repoSession,
				blobRepositoryId, derivativeTerm, getContentFlag);
		if (getContentFlag == true) {
			ctx.setProperty(BlobInput.BLOB_CONTENT_KEY, blobOutput.getBlobInputStream());
		}

		if (derivativeTerm != null) {
			// reset 'blobsCommon' if we have a derivative request
			blobsCommon = blobOutput.getBlobsCommon();
			blobsCommon.setUri(getDerivativePathBase(docModel) +
					derivativeTerm + "/" + BlobInput.URI_CONTENT_PATH);
		}
		
		blobsCommon.setRepositoryId(null); //hide the repository id from the GET results payload since it is private
		this.setCommonPartProperties(docModel, blobsCommon);
		// finish extracting the other parts by calling the parent
		super.extractAllParts(wrapDoc);
	}

	@Override
	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		ServiceContext ctx = this.getServiceContext();
		BlobInput blobInput = (BlobInput)ctx.getProperty(BlobInput.class.getName());
		if (blobInput == null) {    		
			super.fillAllParts(wrapDoc, action);
		} else {
			//
			// If blobInput is set then we just received a multipart/form-data file post
			//
			DocumentModel documentModel = wrapDoc.getWrappedObject();
			RepositoryInstance repoSession = this.getRepositorySession();    	
			BlobsCommon blobsCommon = NuxeoImageUtils.createPicture(ctx, repoSession, blobInput);
			this.setCommonPartProperties(documentModel, blobsCommon);
		}
	}    
}

