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

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
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
			return FMEFreeModel.conceptRole(object.getFlexoConcept()) != null;
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
	private Integer integerValue;
	private Double floatValue;
	private Date dateValue;
	private String enumerationValue;
	private FlexoConceptInstance referenceValue;

	// Default value, assigned to all other instances of the concept in the current free model
	private String defaultStringValue;
	private Boolean defaultBooleanValue;
	private Integer defaultIntegerValue;
	private Double defaultFloatValue;
	private Date defaultDateValue;
	private String defaultEnumerationValue;
	private FlexoConceptInstance defaultReferenceValue;

	private CreateNewFMEProperty createPropertyAction;

	private InstantiateNewFMEProperty(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		createPropertyAction = CreateNewFMEProperty.actionType.makeNewEmbeddedAction(focusedObject.getFlexoConcept(), null, this);

	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		if (createPropertyAction.getFMEType() != null) {
			createPropertyAction.doAction();

			String propertyName = createPropertyAction.getCreatedPropertyName();
			FlexoConceptInstance currentInstance = getConceptInstance();

			// Default value of the role: it is the one given to the instances created from now on
			String defaultExpression = getDefaultValueExpression();
			if (defaultExpression != null) {
				FlexoProperty<?> property = createPropertyAction.getConcept().getAccessibleProperty(propertyName);
				if (property instanceof FlexoRole) {
					((FlexoRole) property).setDefaultValue(new DataBinding<>(defaultExpression));
				}
			}

			// Default value: all other instances of the concept in the current free model
			Object defaultValue = getDefaultValue();
			if (defaultValue != null && currentInstance != null && currentInstance.getFlexoConcept() != null
					&& currentInstance.getVirtualModelInstance() != null) {
				for (FlexoConceptInstance instance : new ArrayList<>(
						currentInstance.getVirtualModelInstance().getFlexoConceptInstances(createPropertyAction.getConcept()))) {
					if (instance != currentInstance) {
						instance.setFlexoPropertyValue(propertyName, defaultValue);
					}
				}
			}

			// Value for this instance
			currentInstance.setFlexoPropertyValue(propertyName, getValue());
		}

	}

	/**
	 * Value to assign to the current instance, according to the type of the new property
	 */
	private Object getValue() {
		switch (createPropertyAction.getFMEType()) {
			case String:
				return getStringValue();
			case Boolean:
				return getBooleanValue();
			case Integer:
				return getIntegerValue();
			case Float:
				return getFloatValue();
			case Date:
				return getDateValue();
			case Enumeration:
				return enumInstance(getEnumerationValue());
			case Reference:
				return getReferenceValue();
			default:
				return null;
		}
	}

	/**
	 * Default value to assign to other instances, according to the type of the new property (null when none is defined)
	 */
	private Object getDefaultValue() {
		switch (createPropertyAction.getFMEType()) {
			case String:
				return getDefaultStringValue();
			case Boolean:
				return getDefaultBooleanValue();
			case Integer:
				return getDefaultIntegerValue();
			case Float:
				return getDefaultFloatValue();
			case Date:
				return getDefaultDateValue();
			case Enumeration:
				return enumInstance(getDefaultEnumerationValue());
			case Reference:
				return getDefaultReferenceValue();
			default:
				return null;
		}
	}

	/**
	 * The default value, as the expression stored in the default value of the role (null when none is defined, or when the type has no
	 * literal)
	 */
	private String getDefaultValueExpression() {
		switch (createPropertyAction.getFMEType()) {
			case String:
				// A Connie string literal does not unescape a quote or a backslash: no expression for such a string (the other instances
				// get it anyway)
				String text = getDefaultStringValue();
				return text != null && text.indexOf('"') < 0 && text.indexOf('\\') < 0 ? "\"" + text + "\"" : null;
			case Boolean:
				return getDefaultBooleanValue() != null ? getDefaultBooleanValue().toString() : null;
			case Integer:
				return getDefaultIntegerValue() != null ? getDefaultIntegerValue().toString() : null;
			case Float:
				return getDefaultFloatValue() != null ? getDefaultFloatValue().toString() : null;
			default:
				return null;
		}
	}

	private Object enumInstance(String enumValueName) {
		return enumValueName != null ? createPropertyAction.getNewEnum().getInstance(enumValueName) : null;
	}

	public FlexoConceptInstance getConceptInstance() {
		if (getFocusedObject() != null) {
			try {
				return getFocusedObject().execute(FMEFreeModel.conceptRoleName(getFocusedObject().getFlexoConcept()));
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

	public Integer getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Integer integerValue) {
		if ((integerValue == null && this.integerValue != null) || (integerValue != null && !integerValue.equals(this.integerValue))) {
			Integer oldValue = this.integerValue;
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

	public String getDefaultStringValue() {
		return defaultStringValue;
	}

	public void setDefaultStringValue(String defaultStringValue) {
		if ((defaultStringValue == null && this.defaultStringValue != null) || (defaultStringValue != null && !defaultStringValue.equals(this.defaultStringValue))) {
			String oldValue = this.defaultStringValue;
			this.defaultStringValue = defaultStringValue;
			getPropertyChangeSupport().firePropertyChange("defaultStringValue", oldValue, defaultStringValue);
		}
	}

	public Boolean getDefaultBooleanValue() {
		return defaultBooleanValue;
	}

	public void setDefaultBooleanValue(Boolean defaultBooleanValue) {
		if ((defaultBooleanValue == null && this.defaultBooleanValue != null) || (defaultBooleanValue != null && !defaultBooleanValue.equals(this.defaultBooleanValue))) {
			Boolean oldValue = this.defaultBooleanValue;
			this.defaultBooleanValue = defaultBooleanValue;
			getPropertyChangeSupport().firePropertyChange("defaultBooleanValue", oldValue, defaultBooleanValue);
		}
	}

	public Integer getDefaultIntegerValue() {
		return defaultIntegerValue;
	}

	public void setDefaultIntegerValue(Integer defaultIntegerValue) {
		if ((defaultIntegerValue == null && this.defaultIntegerValue != null) || (defaultIntegerValue != null && !defaultIntegerValue.equals(this.defaultIntegerValue))) {
			Integer oldValue = this.defaultIntegerValue;
			this.defaultIntegerValue = defaultIntegerValue;
			getPropertyChangeSupport().firePropertyChange("defaultIntegerValue", oldValue, defaultIntegerValue);
		}
	}

	public Double getDefaultFloatValue() {
		return defaultFloatValue;
	}

	public void setDefaultFloatValue(Double defaultFloatValue) {
		if ((defaultFloatValue == null && this.defaultFloatValue != null) || (defaultFloatValue != null && !defaultFloatValue.equals(this.defaultFloatValue))) {
			Double oldValue = this.defaultFloatValue;
			this.defaultFloatValue = defaultFloatValue;
			getPropertyChangeSupport().firePropertyChange("defaultFloatValue", oldValue, defaultFloatValue);
		}
	}

	public Date getDefaultDateValue() {
		return defaultDateValue;
	}

	public void setDefaultDateValue(Date defaultDateValue) {
		if ((defaultDateValue == null && this.defaultDateValue != null) || (defaultDateValue != null && !defaultDateValue.equals(this.defaultDateValue))) {
			Date oldValue = this.defaultDateValue;
			this.defaultDateValue = defaultDateValue;
			getPropertyChangeSupport().firePropertyChange("defaultDateValue", oldValue, defaultDateValue);
		}
	}

	public String getDefaultEnumerationValue() {
		return defaultEnumerationValue;
	}

	public void setDefaultEnumerationValue(String defaultEnumerationValue) {
		if ((defaultEnumerationValue == null && this.defaultEnumerationValue != null) || (defaultEnumerationValue != null && !defaultEnumerationValue.equals(this.defaultEnumerationValue))) {
			String oldValue = this.defaultEnumerationValue;
			this.defaultEnumerationValue = defaultEnumerationValue;
			getPropertyChangeSupport().firePropertyChange("defaultEnumerationValue", oldValue, defaultEnumerationValue);
		}
	}

	public FlexoConceptInstance getDefaultReferenceValue() {
		return defaultReferenceValue;
	}

	public void setDefaultReferenceValue(FlexoConceptInstance defaultReferenceValue) {
		if ((defaultReferenceValue == null && this.defaultReferenceValue != null) || (defaultReferenceValue != null && !defaultReferenceValue.equals(this.defaultReferenceValue))) {
			FlexoConceptInstance oldValue = this.defaultReferenceValue;
			this.defaultReferenceValue = defaultReferenceValue;
			getPropertyChangeSupport().firePropertyChange("defaultReferenceValue", oldValue, defaultReferenceValue);
		}
	}
}
