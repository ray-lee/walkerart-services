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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.vocabulary.nuxeo;

import java.util.regex.Pattern;

import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class VocabularyItemValidatorHandler implements ValidatorHandler {

    final Logger logger = LoggerFactory.getLogger(VocabularyItemValidatorHandler.class);
    private static final Pattern shortIdBadPattern = Pattern.compile("[\\W]"); //.matcher(input).matches()

    @Override
    public void validate(Action action, ServiceContext ctx)
            throws InvalidDocumentException {
        if(logger.isDebugEnabled()) {
            logger.debug("validate() action=" + action.name());
        }
        try {
            MultipartServiceContext mctx = (MultipartServiceContext) ctx;
            VocabularyitemsCommon vocabItem = (VocabularyitemsCommon) mctx.getInputPart(mctx.getCommonPartLabel(),
                    VocabularyitemsCommon.class);
            String msg = "";
            boolean invalid = false;
			String displayName = vocabItem.getDisplayName();
            if((displayName==null)||(displayName.length()<2)) {
                invalid = true;
                msg += "displayName must be non-null, and at least 2 chars long!";
            }
			String shortId = vocabItem.getShortIdentifier();
            if(shortId==null){
                invalid = true;
                msg += "shortIdentifier must be non-null";
            } else if(shortIdBadPattern.matcher(shortId).find()) {
                invalid = true;
                msg += "shortIdentifier must only contain standard word characters";
            }
            /*
            if(action.equals(Action.CREATE)) {
                //create specific validation here
            } else if(action.equals(Action.UPDATE)) {
                //update specific validation here
            }
            */

            if (invalid) {
                logger.error(msg);
                throw new InvalidDocumentException(msg);
            }
        } catch (InvalidDocumentException ide) {
            throw ide;
        } catch (Exception e) {
            throw new InvalidDocumentException(e);
        }
    }
}
