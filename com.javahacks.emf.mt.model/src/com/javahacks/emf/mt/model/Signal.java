package com.javahacks.emf.mt.model;

import org.eclipse.emf.ecore.EObject;

/**
* @model
*/
public interface Signal extends EObject {

	/**
	* @model
	*/
	public String getName();
	
	/**
	 * Sets the value of the '{@link com.javahacks.emf.mt.model.Signal#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	* @model
	*/
	public double getValue();

	/**
	 * Sets the value of the '{@link com.javahacks.emf.mt.model.Signal#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(double value);

	/**
	* @model
	*/
	public int getUpdates();

	/**
	 * Sets the value of the '{@link com.javahacks.emf.mt.model.Signal#getUpdates <em>Updates</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Updates</em>' attribute.
	 * @see #getUpdates()
	 * @generated
	 */
	void setUpdates(int value);
	
}
