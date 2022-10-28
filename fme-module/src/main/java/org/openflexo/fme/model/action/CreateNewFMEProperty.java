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
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreateFlexoEnum;
import org.openflexo.foundation.fml.action.CreateFlexoEnumValue;
import org.openflexo.foundation.fml.action.CreateInspectorEntry;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.toolbox.StringUtils;

/**
 * This action is used to create a new primitive property for a concept<br>
 * Focused object is the GR concept
 * 
 * @author sylvain
 * 
 */
public class CreateNewFMEProperty extends FMEAction<CreateNewFMEProperty, FlexoConcept, FlexoObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateNewFMEProperty.class.getPackage().getName());

	public static FlexoActionFactory<CreateNewFMEProperty, FlexoConcept, FlexoObject> actionType = new FlexoActionFactory<CreateNewFMEProperty, FlexoConcept, FlexoObject>(
			"create_new_property", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewFMEProperty makeNewAction(FlexoConcept focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new CreateNewFMEProperty(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConcept object, Vector<FlexoObject> globalSelection) {
			return object.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME) != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConcept object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateNewFMEProperty.actionType, FlexoConcept.class);
	}

	private String propertyName;
	private String description;
	private String enumValues;
	private FlexoConcept referenceType;
	private FMEType fmeType = FMEType.String;

	private FlexoEnum newEnum;

	// private FlexoConceptInstance newFlexoConceptInstance;

	private CreateNewFMEProperty(FlexoConcept focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		System.out.println("OK on cree la propriete dans " + getConcept());
		System.out.println("Pour " + getGRConcept());
		System.out.println("type " + getFMEType());

		if (getConcept() != null && getFMEType() != null) {
			if (getFMEType().getPrimitiveType() != null) {
				CreatePrimitiveRole createPropertyAction = CreatePrimitiveRole.actionType.makeNewEmbeddedAction(getConcept(), null, this);
				createPropertyAction.setRoleName(getPropertyName());
				createPropertyAction.setPrimitiveType(getFMEType().getPrimitiveType());
				createPropertyAction.setDescription(getDescription());
				createPropertyAction.doAction();
			}
			else if (getFMEType() == FMEType.Enumeration) {

				CreateFlexoEnum createEnumAction = CreateFlexoEnum.actionType
						.makeNewEmbeddedAction(getConcept().getDeclaringCompilationUnit().getVirtualModel(), null, this);
				createEnumAction.setNewFlexoEnumName(getPropertyName().substring(0, 1).toUpperCase() + getPropertyName().substring(1));
				createEnumAction.setNewFlexoEnumDescription(getDescription());
				createEnumAction.doAction();
				newEnum = createEnumAction.getNewFlexoConcept();

				StringTokenizer st = new StringTokenizer(getEnumValues(), ",");
				while (st.hasMoreTokens()) {
					String next = st.nextToken();
					CreateFlexoEnumValue createEnumValue = CreateFlexoEnumValue.actionType.makeNewEmbeddedAction(newEnum, null, this);
					createEnumValue.setValueName(next);
					createEnumValue.doAction();
				}

				CreateFlexoConceptInstanceRole createPropertyAction = CreateFlexoConceptInstanceRole.actionType
						.makeNewEmbeddedAction(getConcept(), null, this);
				createPropertyAction.setRoleName(getPropertyName());
				createPropertyAction.setFlexoConceptInstanceType(newEnum);
				createPropertyAction.setVirtualModelInstance(new DataBinding<>("container"));
				createPropertyAction.setDescription(getDescription());
				createPropertyAction.doAction();
			}
			else if (getFMEType() == FMEType.Reference) {
				CreateFlexoConceptInstanceRole createPropertyAction = CreateFlexoConceptInstanceRole.actionType
						.makeNewEmbeddedAction(getConcept(), null, this);
				createPropertyAction.setRoleName(getPropertyName());
				createPropertyAction.setFlexoConceptInstanceType(getReferenceType());
				createPropertyAction.setVirtualModelInstance(new DataBinding<>("container"));
				createPropertyAction.setDescription(getDescription());
				createPropertyAction.doAction();
			}

			if (getGRConcept() != null) {
				CreateInspectorEntry createInspectorEntry = CreateInspectorEntry.actionType
						.makeNewEmbeddedAction(getGRConcept().getInspector(), null, this);
				createInspectorEntry.setEntryName(getPropertyName());
				createInspectorEntry.setEntryType(getFMEType().getType());
				createInspectorEntry.setData(new DataBinding<>(FMEFreeModel.CONCEPT_ROLE_NAME + "." + propertyName));
				createInspectorEntry.setIndex(getGRConcept().getInspector().getEntries().size() - 1);
				createInspectorEntry.doAction();
			}

		}

	}

	/**
	 * Return graphical representation concept
	 * 
	 * @return
	 */
	public FlexoConcept getGRConcept() {
		return getFocusedObject();
	}

	/**
	 * Return concept (conceptual model level) where to create the property<br>
	 * 
	 * @return
	 */
	public FlexoConcept getConcept() {
		if (getGRConcept() != null) {
			FlexoProperty<?> p = getGRConcept().getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				return fciRole.getFlexoConceptType();
			}
		}
		return null;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		if ((propertyName == null && getPropertyName() != null) || (propertyName != null && !propertyName.equals(getPropertyName()))) {
			String oldValue = getPropertyName();
			this.propertyName = propertyName;
			getPropertyChangeSupport().firePropertyChange("propertyName", oldValue, propertyName);
		}
	}

	public FMEType getFMEType() {
		return fmeType;
	}

	public void setFMEType(FMEType fmeType) {
		this.fmeType = fmeType;
		getPropertyChangeSupport().firePropertyChange("FMEType", null, fmeType);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if ((description == null && this.description != null) || (description != null && !description.equals(this.description))) {
			String oldValue = this.description;
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
		}
	}

	public String getEnumValues() {
		return enumValues;
	}

	public void setEnumValues(String enumValues) {
		if ((enumValues == null && this.enumValues != null) || (enumValues != null && !enumValues.equals(this.enumValues))) {
			String oldValue = this.enumValues;
			this.enumValues = enumValues;
			computeEnumValues();
			getPropertyChangeSupport().firePropertyChange("enumValues", oldValue, enumValues);
			getPropertyChangeSupport().firePropertyChange("enumValuesAsList", null, getEnumValuesAsList());
		}
	}

	private List<String> enumValuesAsList = new ArrayList<>();

	public List<String> getEnumValuesAsList() {
		if (enumValuesAsList == null) {
			computeEnumValues();
		}
		return enumValuesAsList;
	}

	private void computeEnumValues() {
		enumValuesAsList.clear();
		StringTokenizer st = new StringTokenizer(getEnumValues(), ",");
		while (st.hasMoreTokens()) {
			enumValuesAsList.add(st.nextToken());
		}

	}

	public FlexoConcept getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(FlexoConcept referenceType) {
		if ((referenceType == null && this.referenceType != null) || (referenceType != null && !referenceType.equals(this.referenceType))) {
			FlexoConcept oldValue = this.referenceType;
			this.referenceType = referenceType;
			getPropertyChangeSupport().firePropertyChange("referenceType", oldValue, referenceType);
		}
	}

	public FlexoEnum getNewEnum() {
		return newEnum;
	}

	@Override
	public boolean isValid() {

		if (StringUtils.isEmpty(getPropertyName())) {
			return false;
		}

		if (getFMEType() == null) {
			return false;
		}

		if (getFMEType() == FMEType.Enumeration && StringUtils.isEmpty(getEnumValues())) {
			return false;
		}

		if (getFMEType() == FMEType.Reference && getReferenceType() == null) {
			return false;
		}

		return true;
	}

}
