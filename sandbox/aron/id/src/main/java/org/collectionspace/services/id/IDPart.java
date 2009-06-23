 /*	
 * IDPart
 *
 * <p>Models a part of an identifier (ID).</p>
 *
 * <p>Some representative examples of data that can be
 * managed within IDParts include:</p>
 *
 * <ul>
 *   <li>Incrementing numeric or alphabetic values</li>
 *   <li>Date values</li>
 *   <li>Static separators</li>
 * </ul>
 *
 * Copyright 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * @author $Author$
 * @version $Revision$
 * $Date$
 */

// @TODO: Add Javadoc comments

package org.collectionspace.services.id;

public abstract class IDPart {

	// A generator for the types of IDs that are generated by this part.
	// This generator is passed in at construction time.
	protected IDGenerator generator;
	
	// Constructor
	public IDPart(IDGenerator idGenerator) throws IllegalArgumentException {
		this.generator = idGenerator;
	}

	// Resets the ID to its initial value.
	public synchronized void reset() {
		generator.reset();
	}

	// Returns the initial value of this ID.
	public synchronized String getInitialID() {
		return generator.getInitialID();
	}

	// Returns the current value of this ID.
	public synchronized String getCurrentID() {
		return generator.getCurrentID();
	}

	// Returns the next value of this ID.
	public synchronized String getNextID() {
		try {
			return generator.getNextID();
		} catch (IllegalStateException e) {
			throw e;
		}
	}

	// public boolean validate() {};
 
}
