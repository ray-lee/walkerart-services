/**
 * 
 */
package org.collectionspace.services;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;

/**
 * @author pschmitz
 *
 */
public interface OrganizationJAXBSchema extends AuthorityJAXBSchema {
	final static String ORGANIZATIONS_COMMON="organizations_common";	
	final static String DISPLAY_NAME_COMPUTED = "displayNameComputed";
	final static String SHORT_NAME = "shortName";
	final static String LONG_NAME = "longName";
	final static String NAME_ADDITIONS = "nameAdditions";
	final static String CONTACT_NAME = "contactName";
	final static String FOUNDING_DATE = "foundingDate";
	final static String DISSOLUTION_DATE = "dissolutionDate";
	final static String FOUNDING_PLACE = "foundingPlace";
    final static String GROUP = "group";
	final static String FUNCTION = "function";
    final static String SUB_BODY = "subBody";
	final static String HISTORY = "history";
}


