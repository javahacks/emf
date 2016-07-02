package com.javahacks.emf.mt.model;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/** 
* @model
*/
public interface Model extends EObject {

	/**
	* @model containment="true"
	*/
	public EList<Signal> getSignals();
	
}
