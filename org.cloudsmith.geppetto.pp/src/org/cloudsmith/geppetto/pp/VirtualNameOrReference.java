/**
 * Copyright (c) 2011 Cloudsmith Inc. and other contributors, as listed below.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Cloudsmith
 * 
 */
package org.cloudsmith.geppetto.pp;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Virtual Name Or Reference</b></em>'.
 * <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.cloudsmith.geppetto.pp.VirtualNameOrReference#getValue <em>Value</em>}</li>
 * <li>{@link org.cloudsmith.geppetto.pp.VirtualNameOrReference#isExported <em>Exported</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.cloudsmith.geppetto.pp.PPPackage#getVirtualNameOrReference()
 * @model
 * @generated
 */
public interface VirtualNameOrReference extends LiteralExpression {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see org.cloudsmith.geppetto.pp.PPPackage#getVirtualNameOrReference_Value()
	 * @model
	 * @generated
	 */
	String getValue();

	/**
	 * Returns the value of the '<em><b>Exported</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Exported</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Exported</em>' attribute.
	 * @see #setExported(boolean)
	 * @see org.cloudsmith.geppetto.pp.PPPackage#getVirtualNameOrReference_Exported()
	 * @model
	 * @generated
	 */
	boolean isExported();

	/**
	 * Sets the value of the '{@link org.cloudsmith.geppetto.pp.VirtualNameOrReference#isExported <em>Exported</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Exported</em>' attribute.
	 * @see #isExported()
	 * @generated
	 */
	void setExported(boolean value);

	/**
	 * Sets the value of the '{@link org.cloudsmith.geppetto.pp.VirtualNameOrReference#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

} // VirtualNameOrReference
