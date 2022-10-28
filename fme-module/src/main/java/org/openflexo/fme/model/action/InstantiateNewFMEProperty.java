/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Freemodellingeditor, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.fme.model.action;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * This action is used to create a new primitive property for a concept instance<br>
 * Focused object is the GR concept instance
 * 
 * @author sylvain
 * 
 */
public class InstantiateNewFMEProperty extends FMEAction<InstantiateNewFMEProperty, FlexoConceptInstance, FlexoObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InstantiateNewFMEProperty.class.getPackage().getName());

	public static FlexoActionFactory<InstantiateNewFMEProperty, FlexoConceptInstance, FlexoObject> actionType = new FlexoActionFactory<InstantiateNewFMEProperty, FlexoConceptInstance, FlexoObject>(
			"create_new_property", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public InstantiateNewFMEProperty makeNewAction(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new InstantiateNewFMEProperty(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return object.getFlexoConcept().getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME) != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(InstantiateNewFMEProperty.actionType, FlexoConceptInstance.class);
	}

	private String stringValue;
	private Boolean booleanValue;
	private Long integerValue;
	private Double floatValue;
	private Date dateValue;
	private String enumerationValue;
	private FlexoConceptInstance referenceValue;

	private CreateNewFMEProperty createPropertyAction;

	private InstantiateNewFMEProperty(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		createPropertyAction = CreateNewFMEProperty.actionType.makeNewEmbeddedAction(focusedObject.getFlexoConcept(), null, this);

	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		if (createPropertyAction.getFMEType() != null) {
			createPropertyAction.doAction();

			switch (createPropertyAction.getFMEType()) {
				case String:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(), getStringValue());
					break;
				case Boolean:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(), getBooleanValue());
					break;
				case Integer:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(), getIntegerValue());
					break;
				case Float:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(), getFloatValue());
					break;
				case Date:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(), getDateValue());
					break;
				case Enumeration:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(),
							createPropertyAction.getNewEnum().getInstance(getEnumerationValue()));
					break;
				case Reference:
					getConceptInstance().setFlexoPropertyValue(createPropertyAction.getPropertyName(), getReferenceValue());
					break;

				default:
					break;
			}
		}

	}

	public FlexoConceptInstance getConceptInstance() {
		if (getFocusedObject() != null) {
			try {
				return getFocusedObject().execute(FMEFreeModel.CONCEPT_ROLE_NAME);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			} catch (InvalidBindingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean isValid() {

		if (!createPropertyAction.isValid()) {
			return false;
		}

		return true;
	}

	public CreateNewFMEProperty getCreatePropertyAction() {
		return createPropertyAction;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		if ((stringValue == null && this.stringValue != null) || (stringValue != null && !stringValue.equals(this.stringValue))) {
			String oldValue = this.stringValue;
			this.stringValue = stringValue;
			getPropertyChangeSupport().firePropertyChange("stringValue", oldValue, stringValue);
		}
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		if ((booleanValue == null && this.booleanValue != null) || (booleanValue != null && !booleanValue.equals(this.booleanValue))) {
			Boolean oldValue = this.booleanValue;
			this.booleanValue = booleanValue;
			getPropertyChangeSupport().firePropertyChange("booleanValue", oldValue, booleanValue);
		}
	}

	public Long getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Long integerValue) {
		if ((integerValue == null && this.integerValue != null) || (integerValue != null && !integerValue.equals(this.integerValue))) {
			Long oldValue = this.integerValue;
			this.integerValue = integerValue;
			getPropertyChangeSupport().firePropertyChange("integerValue", oldValue, integerValue);
		}
	}

	public Double getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(Double floatValue) {
		if ((floatValue == null && this.floatValue != null) || (floatValue != null && !floatValue.equals(this.floatValue))) {
			Double oldValue = this.floatValue;
			this.floatValue = floatValue;
			getPropertyChangeSupport().firePropertyChange("floatValue", oldValue, floatValue);
		}
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		if ((dateValue == null && this.dateValue != null) || (dateValue != null && !dateValue.equals(this.dateValue))) {
			Date oldValue = this.dateValue;
			this.dateValue = dateValue;
			getPropertyChangeSupport().firePropertyChange("dateValue", oldValue, dateValue);
		}
	}

	public String getEnumerationValue() {
		return enumerationValue;
	}

	public void setEnumerationValue(String enumerationValue) {
		if ((enumerationValue == null && this.enumerationValue != null)
				|| (enumerationValue != null && !enumerationValue.equals(this.enumerationValue))) {
			String oldValue = this.enumerationValue;
			this.enumerationValue = enumerationValue;
			getPropertyChangeSupport().firePropertyChange("enumerationValue", oldValue, enumerationValue);
		}
	}

	public FlexoConceptInstance getReferenceValue() {
		return referenceValue;
	}

	public void setReferenceValue(FlexoConceptInstance referenceValue) {
		if ((referenceValue == null && this.referenceValue != null)
				|| (referenceValue != null && !referenceValue.equals(this.referenceValue))) {
			FlexoConceptInstance oldValue = this.referenceValue;
			this.referenceValue = referenceValue;
			getPropertyChangeSupport().firePropertyChange("referenceValue", oldValue, referenceValue);
		}
	}
}
