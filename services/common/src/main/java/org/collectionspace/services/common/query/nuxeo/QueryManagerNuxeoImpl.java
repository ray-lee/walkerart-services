/**	
 * QueryManagerNuxeoImpl.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.query.nuxeo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;

import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnector;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocableUtils;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;

public class QueryManagerNuxeoImpl implements IQueryManager {
	
	private static String ECM_FULLTEXT_LIKE = 
		"ecm:fulltext" + SEARCH_TERM_SEPARATOR + IQueryManager.SEARCH_LIKE;
	private static String SEARCH_LIKE_FORM = null;

	private final Logger logger = LoggerFactory
			.getLogger(QueryManagerNuxeoImpl.class);
	
	// Consider that letters, letter-markers, numbers, '_' and apostrophe are words  
	private static Pattern nonWordChars = Pattern.compile("[^\\p{L}\\p{M}\\p{N}_']");
	private static Pattern unescapedDblQuotes = Pattern.compile("(?<!\\\\)\"");
	private static Pattern unescapedSingleQuote = Pattern.compile("(?<!\\\\)'");
	private static Pattern kwdSearchProblemChars = Pattern.compile("[\\:\\(\\)]");
	private static Pattern kwdSearchHyphen = Pattern.compile(" - ");
	
	private static String getLikeForm() {
		if(SEARCH_LIKE_FORM == null) {
			try {
				DatabaseProductType type = JDBCTools.getDatabaseProductType();
				if(type == DatabaseProductType.MYSQL) {
					SEARCH_LIKE_FORM = IQueryManager.SEARCH_LIKE;
				} else if(type == DatabaseProductType.POSTGRESQL) {
					SEARCH_LIKE_FORM = IQueryManager.SEARCH_ILIKE;
				}
			} catch (Exception e) {
				SEARCH_LIKE_FORM = IQueryManager.SEARCH_LIKE;
			}
		}
		return SEARCH_LIKE_FORM;
	}

	//TODO: This is currently just an example fixed query.  This should eventually be
	// removed or replaced with a more generic method.
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.query.IQueryManager#execQuery(java.lang.String)
	 */
	public void execQuery(String queryString) {
		NuxeoClient client = null;
		try {
			client = NuxeoConnector.getInstance().getClient();
			RepositoryInstance repoSession = client.openRepository();
			
			DocumentModelList docModelList = repoSession.query("SELECT * FROM Relation WHERE relation:relationtype.documentId1='updated-Subject-1'");
//			DocumentModelList docModelList = repoSession.query("SELECT * FROM Relation");
//			DocumentModelList docModelList = repoSession.query("SELECT * FROM CollectionObject WHERE collectionobject:objectNumber='objectNumber-1251305545865'");
			for (DocumentModel docModel : docModelList) {
				System.out.println("--------------------------------------------");
				System.out.println(docModel.getPathAsString());
				System.out.println(docModel.getName());
				System.out.println(docModel.getPropertyValue("dc:title"));
//				System.out.println("documentId1=" + docModel.getProperty("relation", "relationtype/documentId1").toString());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.query.IQueryManager#createWhereClauseFromKeywords(java.lang.String)
	 */
	// TODO handle keywords containing escaped punctuation chars, then we need to qualify the
	// search by matching on the fulltext.simpletext field.
	// TODO handle keywords containing unescaped double quotes by matching the phrase
	// against the fulltext.simpletext field.
	// Both these require using JDBC, since we cannot get to the fulltext table in NXQL
	public String createWhereClauseFromKeywords(String keywords) {
		String result = null;
		StringBuffer fullTextWhereClause = new StringBuffer(SEARCH_GROUP_OPEN);
		//StringBuffer phraseWhereClause = new StringBuffer(SEARCH_GROUP_OPEN);
		boolean phrasesToAdd = false;
		// Split on unescaped double quotes to handle phrases
		String[] phrases = unescapedDblQuotes.split(keywords.trim());
		boolean first = true;
		for(String phrase : phrases ) {
			String trimmed = phrase.trim();
			// Ignore empty strings from match, or goofy input
			if(trimmed.isEmpty())
				continue;
			// Add the phrase to the string to pass in for full text matching.
			// Note that we can pass in a set of words and it will do the OR for us.
			if(first) {
				fullTextWhereClause.append(ECM_FULLTEXT_LIKE +"'");
				first = false;
			} else {
				fullTextWhereClause.append(SEARCH_TERM_SEPARATOR);
			}
			// ignore the special chars except single quote here - can't hurt
			// TODO this should become a special function that strips things the
			// fulltext will ignore, including non-word chars and too-short words,
			// and escaping single quotes. Can return a boolean for anything stripped,
			// which triggers the back-up search. We can think about whether stripping
			// short words not in a quoted phrase should trigger the backup.
			trimmed = unescapedSingleQuote.matcher(trimmed).replaceAll("\\\\'");
			// If there are non-word chars in the phrase, we need to match the
			// phrase exactly against the fulltext table for this object
			//if(nonWordChars.matcher(trimmed).matches()) {
			//}
			// Replace problem chars with spaces. Patches CSPACE-4147, CSPACE-4106 
			trimmed = kwdSearchProblemChars.matcher(trimmed).replaceAll(" ");
			trimmed = kwdSearchHyphen.matcher(trimmed).replaceAll(" ");

			fullTextWhereClause.append(trimmed);
			if (logger.isTraceEnabled() == true) {
				logger.trace("Current built whereClause is: " + fullTextWhereClause.toString());
			}
		}
		if(first) {
			throw new RuntimeException("No usable keywords specified in string:["
					+keywords+"]");
		}
		fullTextWhereClause.append("'"+SEARCH_GROUP_CLOSE);
		
		result = fullTextWhereClause.toString();
	    if (logger.isDebugEnabled()) {
	    	logger.debug("Final built WHERE clause is: " + result);
	    }
	    
	    return result;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.query.IQueryManager#createWhereClauseFromKeywords(java.lang.String)
	 */
	// TODO handle keywords containing escaped punctuation chars, then we need to qualify the
	// search by matching on the fulltext.simpletext field.
	// TODO handle keywords containing unescaped double quotes by matching the phrase
	// against the fulltext.simpletext field.
	// Both these require using JDBC, since we cannot get to the fulltext table in NXQL
	public String createWhereClauseForPartialMatch(String field, String partialTerm) {
		String trimmed = (partialTerm == null)?"":partialTerm.trim(); 
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No partialTerm specified.");
		}
		if (field==null || field.isEmpty()) {
			throw new RuntimeException("No match field specified.");
		}
		String ptClause = field
			+ getLikeForm()
			+ "'%" + unescapedSingleQuote.matcher(trimmed).replaceAll("\\\\'") + "%'";
		return ptClause;
	}

	/**
	 * Creates a filtering where clause from docType, for invocables.
	 * 
	 * @param docType the docType
	 * 
	 * @return the string
	 */
	public String createWhereClauseForInvocableByDocType(String schema, String docType) {
		String trimmed = (docType == null)?"":docType.trim(); 
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No docType specified.");
		}
		if (schema==null || schema.isEmpty()) {
			throw new RuntimeException("No match schema specified.");
		}
		String wClause = schema+":"+InvocableJAXBSchema.FOR_DOC_TYPES + " = '" + trimmed + "'";
		return wClause;
	}
	
	/**
	 * Creates a filtering where clause from invocation mode, for invocables.
	 * 
	 * @param mode the mode
	 * 
	 * @return the string
	 */
	public String createWhereClauseForInvocableByMode(String schema, String mode) {
		String trimmed = (mode == null)?"":mode.trim(); 
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No docType specified.");
		}
		if (schema==null || schema.isEmpty()) {
			throw new RuntimeException("No match schema specified.");
		}
		String wClause = 
			InvocableUtils.getPropertyNameForInvocationMode(schema, trimmed)
			+ " != 0";
		return wClause;
	}
	
	
	/**
	 * @param input
	 * @return true if there were any chars filtered, that will require a backup
	 *  qualifying search on the actual text.
	 */
	private boolean filterForFullText(String input) {
		boolean fFilteredChars = false;
		
		return fFilteredChars;
	}
}
