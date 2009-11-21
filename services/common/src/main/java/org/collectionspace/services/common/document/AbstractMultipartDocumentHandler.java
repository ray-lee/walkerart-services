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
package org.collectionspace.services.common.document;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractMultipartDocumentHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AbstractMultipartDocumentHandler<T, TL, WT, WTL>
        extends AbstractDocumentHandler<T, TL, WT, WTL>
        implements MultipartDocumentHandler<T, TL, WT, WTL> {

    private final Logger logger = LoggerFactory.getLogger(AbstractMultipartDocumentHandler.class);

    public AbstractMultipartDocumentHandler() {
    }

    @Override
    public abstract void handleCreate(DocumentWrapper<WT> wrapDoc) throws Exception;

    @Override
    public abstract void handleUpdate(DocumentWrapper<WT> wrapDoc) throws Exception;

    @Override
    public abstract void handleGet(DocumentWrapper<WT> wrapDoc) throws Exception;

    @Override
    public abstract void handleGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception;

    @Override
    public abstract void extractAllParts(DocumentWrapper<WT> wrapDoc)
            throws Exception;

    @Override
    public abstract void fillAllParts(DocumentWrapper<WT> wrapDoc)
            throws Exception;

    @Override
    public abstract T extractCommonPart(DocumentWrapper<WT> wrapDoc)
            throws Exception;

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper<WT> wrapDoc)
            throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper<WTL> wrapDoc)
            throws Exception;

    @Override
    public abstract T getCommonPart();

    @Override
    public abstract void setCommonPart(T obj);

    @Override
    public abstract TL getCommonPartList();

    @Override
    public abstract void setCommonPartList(TL obj);

    @Override
    public abstract String getQProperty(String prop);

}
