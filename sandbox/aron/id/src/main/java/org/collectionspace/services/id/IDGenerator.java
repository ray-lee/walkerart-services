 /*	
 * IDGenerator
 *
 * Interface for a generator class that returns IDs.
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author: aron $
 * @version $Revision: 267 $
 * $Date: 2009-06-19 19:03:38 -0700 (Fri, 19 Jun 2009) $
 */
 
// @TODO: Consider making this class, or a class that implements
// this interface, abstract, in part because we're duplicating code
// in isValidID() in multiple Generator subclasses.
 
package org.collectionspace.services.id;

public interface IDGenerator {
    
	// Returns the initial value of the ID.
	public String getInitialID();

	// Gets the current value of an ID.
	public String getCurrentID();
	
	// Sets the current value of an ID.
	public void setCurrentID(String value);

	// Resets an ID to its initial value.
	public void resetID();

	// Returns the next ID in the sequence, and sets
	// the current value to that ID.
	public String nextID();

	// Validates an ID against a pattern of generated IDs.
	public boolean isValidID(String value);

	// Returns a String representation of the regular expression ("regex")
	// pattern used to validate instance values of generated IDs.  
	public String getRegex();
		
}