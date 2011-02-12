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
package org.collectionspace.services.nuxeo.client.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.jaxb.AbstractCommonList;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jvnet.jaxb2_commons.lang.ToString;
import org.jvnet.jaxb2_commons.lang.builder.JAXBToStringBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlSeeAlso;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * This class allows us to generically represent and marshall a set of list
 * results for any object. The base information is provided by the
 * AbstractCommonList.xsd and associated class, defining the list header.
 * A array of itemInfo objects define the results-specific info, where
 * each itemInfo is a map of Strings that represent the fields returned for
 * each item.
 *
 * @author pschmitz
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
@XmlAccessorType(XmlAccessType.NONE)
// We use the same root as the superclass, so unmarshalling will work (more or less)
@XmlRootElement(name = "abstract-common-list")
public class CommonList extends AbstractCommonList {
	
	/** The logger. */
	//private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@XmlTransient
	private DocumentBuilderFactory factory;
	@XmlTransient
	private DocumentBuilder parser;
	@XmlTransient
	private Document doc;
    
    public CommonList()
    	throws javax.xml.parsers.ParserConfigurationException {
    	super();
    	factory = DocumentBuilderFactory.newInstance();
        //Get the DocumentBuilder
        parser = factory.newDocumentBuilder();
        //Create blank DOM Document
        doc = parser.newDocument();
    }
        
	@XmlTransient
	private String fieldKeys[] = null;
	
	//Add methods to add new items, and to set the fieldKeys. Could make
	//them an array of strings rather than arraylist.
	
	/**
	 * @return the current set of fieldKeys.
	 */
	public String[] getFieldKeys() {
		return fieldKeys;
	}
	
	/**
	 * Sets the keys to assume when fetching fields from the itemInfo maps.
	 * As a side-effect, will build and set super.fieldsReturned.
	 * This MUST be called before attempting to add items (with addItem).
	 * 
	 * @param fieldKeys	the keys to use
	 * 
	 */
	public void setFieldsReturned(String[] fieldKeys) {
		this.fieldKeys = fieldKeys;
		String fieldsImploded = implode(fieldKeys, "|");
		setFieldsReturned(fieldsImploded);
	}

	// TODO This should be in common, but then we have mutual dependencies. Sigh.
	private String implode(String strings[], String sep) {
		String implodedString;
		if (strings.length==0) {
			implodedString = "";
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append(strings[0]);
			for (int i=1;i<strings.length;i++) {
				if(strings[i]!=null&& !strings[i].trim().isEmpty()) {
					sb.append(sep);
					sb.append(strings[i]);
				}
			}
			implodedString = sb.toString();
		}
		return implodedString;
	}
	
	/**
	 * Adds an item to the results list. Each item should have fields
	 * associated to keys defined in the fieldKeys. 
	 * Caller must call setFieldsReturned() before calling this.
	 * 
	 * @param itemInfo
	 * @throws RuntimeException if this is called before fieldKeys has been set.
	 */
	public void addItem(HashMap<String,String> itemInfo) {
		if(fieldKeys==null) {
			throw new RuntimeException("CommonList.addItem: Cannot add items before fieldKeys are set.");
		}
		List<AbstractCommonList.ListItem> itemsList = getListItem();
		AbstractCommonList.ListItem listItem = new AbstractCommonList.ListItem();
		itemsList.add(listItem);
		List<Element> anyList = listItem.getAny();
		for(String key:fieldKeys) {
			Element el = doc.createElement(key);
			el.setTextContent(itemInfo.get(key));
			anyList.add(el);
		}
	}

}


