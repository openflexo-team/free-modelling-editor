/**
 * 
 * Copyright (c) 2026, Openflexo
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

import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FMEInspectorGenerator;
import org.openflexo.fme.model.FMENames;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;

/**
 * Makes a new property emerge from a link between two elements of a free model instance, each standing for an instance of a concept: a
 * property of the concept at the start of the link, typed by the concept at its end, is created, and its value, for the conceptual
 * instance at the start, is the conceptual instance at the end.<br>
 * 
 * The technology gives the ends of the link and what represents such a link from then on
 * ({@link #buildLinkRepresentation(String)}: a GR concept drawing it as a connector, for instance)
 * 
 * @param <A>
 *            type of the action
 * @param <T>
 *            type of the focused link
 * @param <FM>
 *            type of the free model
 * @param <FMI>
 *            type of the free model instance
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCreateNewFMEPropertyFromLink<A extends AbstractCreateNewFMEPropertyFromLink<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractLinkAction<A, T, FM, FMI> {

	private String propertyName;
	private String description;

	protected AbstractCreateNewFMEPropertyFromLink(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		if (getFromFlexoConceptInstance() == null) {
			throw new InvalidArgumentException(getFocusedObject() + " does not start from an instance of a concept");
		}

		CreateFlexoConceptInstanceRole createPropertyAction = CreateFlexoConceptInstanceRole.actionType
				.makeNewEmbeddedAction(getFromFlexoConcept(), null, this);
		createPropertyAction.setRoleName(getCreatedPropertyName());
		createPropertyAction.setFlexoConceptInstanceType(getToFlexoConcept());
		createPropertyAction.setVirtualModelInstance(new DataBinding<>("container"));
		createPropertyAction.setDescription(getDescription());
		createPropertyAction.doAction();

		if (getFromFlexoConceptGR() != null) {
			// The source's inspector derives entirely to the conceptual concept's own (@Inspector(derived=...));
			// completing THAT one - not regenerating it: its author may have edited it - is what makes the new property show
			FMEInspectorGenerator.addPropertyToConceptualInspector(getFromFlexoConcept(),
					getFromFlexoConcept().getAccessibleProperty(getCreatedPropertyName()));
		}

		buildLinkRepresentation(getCreatedPropertyName());

		getFromFlexoConceptInstance().setFlexoPropertyValue(getCreatedPropertyName(), getToFlexoConceptInstance());
	}

	/**
	 * Called once the property is created: builds what represents such a link in the free model from then on, if anything
	 * 
	 * @param createdPropertyName
	 *            name of the new property
	 */
	protected abstract void buildLinkRepresentation(String createdPropertyName) throws FlexoException;

	/**
	 * The name the property is created with: {@link #getPropertyName()}, as typed, normalized into a name FML accepts
	 */
	public String getCreatedPropertyName() {
		return FMENames.propertyName(getPropertyName());
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

	@Override
	public boolean isValid() {

		if (getCreatedPropertyName() == null) {
			return false;
		}

		if (getFromFlexoConcept() == null) {
			return false;
		}

		return true;
	}

}
