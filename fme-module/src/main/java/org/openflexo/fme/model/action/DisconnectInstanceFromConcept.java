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
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;

/**
 * This action disconnects an instance of a concept from that concept: the shape stays in the diagram, but is a plain graphical element
 * again (an instance of the NoneGR), carrying the label of the instance. It is the opposite of {@link DeclareInstanceOfExistingConcept}.
 * <br>
 * The instance of the conceptual concept, and the values of its properties, are deleted.
 * 
 * <p>
 * Focused object is here a {@link FlexoConceptInstance} of a GR concept standing for a shape.
 * 
 * @author sylvain
 * 
 */
public class DisconnectInstanceFromConcept extends AbstractInstantiateConcept<DisconnectInstanceFromConcept> {

	private static final Logger logger = Logger.getLogger(DisconnectInstanceFromConcept.class.getPackage().getName());

	public static FlexoActionFactory<DisconnectInstanceFromConcept, FlexoConceptInstance, FlexoObject> actionType = new FlexoActionFactory<DisconnectInstanceFromConcept, FlexoConceptInstance, FlexoObject>(
			"disconnect_instance_from_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DisconnectInstanceFromConcept makeNewAction(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new DisconnectInstanceFromConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return conceptInstanceOf(object) != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return conceptInstanceOf(object) != null && getIssue(object) == null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DisconnectInstanceFromConcept.actionType, FlexoConceptInstance.class);
	}

	private DisconnectInstanceFromConcept(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * The instance of the conceptual concept supplied instance of a GR concept stands for.
	 * 
	 * @return null when supplied instance is not the one of the GR concept of a shape: the NoneGR, a relationship...
	 */
	public static FlexoConceptInstance conceptInstanceOf(FlexoConceptInstance grInstance) {
		if (grInstance == null || grInstance.getFlexoConcept() == null) {
			return null;
		}
		FlexoConcept grConcept = grInstance.getFlexoConcept();
		FlexoConceptInstanceRole conceptRole = FMEFreeModel.conceptRole(grConcept);
		if (conceptRole == null || !(grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME) instanceof ShapeRole)) {
			return null;
		}
		if (conceptRole.getFlexoConceptType() == null || FMEConceptualModel.isRelationship(conceptRole.getFlexoConceptType())) {
			return null;
		}
		return grInstance.getFlexoActor(conceptRole.getRoleName());
	}

	/**
	 * Localization key of what prevents to disconnect supplied instance, or null when it can be disconnected: it contains other instances,
	 * or it is an end of a relationship
	 */
	public static String getIssue(FlexoConceptInstance grInstance) {

		FlexoConceptInstance conceptInstance = conceptInstanceOf(grInstance);
		if (conceptInstance == null) {
			return "cannot_disconnect_that_instance";
		}
		if (!conceptInstance.getEmbeddedFlexoConceptInstances().isEmpty()) {
			return "cannot_disconnect_an_instance_containing_other_instances";
		}
		if (conceptInstance.getVirtualModelInstance() != null) {
			for (FlexoConceptInstance other : conceptInstance.getVirtualModelInstance().getFlexoConceptInstances()) {
				List<FlexoConceptInstanceRole> ends = FMEConceptualModel.relationEnds(other.getFlexoConcept());
				if (ends != null) {
					for (FlexoConceptInstanceRole end : ends) {
						if (other.getFlexoActor(end.getRoleName()) == conceptInstance) {
							return "cannot_disconnect_an_instance_related_to_another_one";
						}
					}
				}
			}
		}
		return null;
	}

	public String getIssue() {
		return getIssue(getFocusedObject());
	}

	/**
	 * Names of the properties of the conceptual instance, other than the one giving its label, which are set and will be lost
	 */
	public List<String> getLostPropertyNames() {
		List<String> returned = new ArrayList<>();
		FlexoConceptInstance conceptInstance = conceptInstanceOf(getFocusedObject());
		if (conceptInstance != null) {
			String labelProperty = FMEConceptualModel.labelPropertyName(conceptInstance.getFlexoConcept());
			for (FlexoProperty<?> property : conceptInstance.getFlexoConcept().getAccessibleProperties()) {
				if (!property.getPropertyName().equals(labelProperty)
						&& conceptInstance.getFlexoPropertyValue(property.getPropertyName()) != null) {
					returned.add(property.getPropertyName());
				}
			}
		}
		return returned;
	}

	@Override
	public boolean isValid() {
		return getIssue() == null;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FMEDiagramFreeModelInstance freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException("FlexoConceptInstance does not belong to any FreeModel");
		}

		FlexoConceptInstance grInstance = getFocusedObject();
		FlexoConceptInstance conceptInstance = conceptInstanceOf(grInstance);
		if (conceptInstance == null || getIssue() != null) {
			throw new InvalidArgumentException("Cannot disconnect " + grInstance + ": " + getIssue());
		}

		logger.info("Disconnect " + grInstance + " from " + conceptInstance.getFlexoConcept());

		FlexoConcept grConcept = grInstance.getFlexoConcept();
		String conceptRoleName = FMEFreeModel.conceptRoleName(grConcept);

		// The shape stays as it is, it is the label of the instance it keeps
		String labelProperty = FMEConceptualModel.labelPropertyName(conceptInstance.getFlexoConcept());
		String label = labelProperty != null ? (String) conceptInstance.getFlexoPropertyValue(labelProperty) : null;

		DiagramShape shapeElement = grInstance
				.getFlexoActor((ShapeRole) grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME));

		FlexoConcept noneConcept = getFMEFreeModel().getNoneFlexoConcept(getEditor(), this);

		// The opposite of what DeclareInstanceOfExistingConcept does, bypassing the DropScheme as well
		grInstance.setFlexoPropertyValue(conceptRoleName, null);
		grInstance.setFlexoConcept(noneConcept);
		if (label != null) {
			grInstance.setFlexoPropertyValue(FMEFreeModel.NAME_ROLE_NAME, label);
		}

		// Nothing represents the conceptual instance any longer
		conceptInstance.delete();

		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, noneConcept);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, noneConcept);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, grInstance);

		// The drawing has to discover that the shape is now the one of the NoneGR
		if (shapeElement != null && shapeElement.getParent() != null) {
			shapeElement.getParent().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null,
					shapeElement.getParent());
		}
	}

}
