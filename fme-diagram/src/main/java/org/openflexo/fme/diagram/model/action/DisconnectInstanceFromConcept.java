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

package org.openflexo.fme.diagram.model.action;

import java.util.Vector;

import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.AbstractDisconnectInstanceFromConcept;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * This action disconnects an instance of a concept from that concept: the shape stays in the diagram, but is a plain graphical element
 * again (an instance of the NoneGR), carrying the label of the instance. It is the opposite of {@link DeclareInstanceOfExistingConcept}.
 * <br>
 * The instance of the conceptual concept, and the values of its properties, are deleted when nothing else represents it (see
 * {@link AbstractDisconnectInstanceFromConcept}).
 * 
 * <p>
 * Focused object is here a {@link FlexoConceptInstance} of a GR concept standing for a shape.
 * 
 * @author sylvain
 * 
 */
public class DisconnectInstanceFromConcept extends
		AbstractDisconnectInstanceFromConcept<DisconnectInstanceFromConcept, FlexoConceptInstance, FMEDiagramFreeModel, FMEDiagramFreeModelInstance> {

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
		if (DiagramRepresentations.shapeOf(grInstance) == null) {
			return null;
		}
		return AbstractDisconnectInstanceFromConcept.conceptInstanceOf(grInstance);
	}

	/**
	 * Localization key of what prevents to disconnect supplied instance, or null when it can be disconnected
	 */
	public static String getIssue(FlexoConceptInstance grInstance) {
		if (conceptInstanceOf(grInstance) == null) {
			return "cannot_disconnect_that_instance";
		}
		return AbstractDisconnectInstanceFromConcept.getIssue(grInstance);
	}

	@Override
	public String getIssue() {
		return getIssue(getGRInstance());
	}

	/**
	 * The shape stays as it is: it becomes an instance of the NoneGR again, keeping the label of the instance
	 */
	@Override
	protected void releaseRepresentation(FlexoConceptInstance grInstance, FlexoConceptInstance conceptInstance) {

		FlexoConcept grConcept = grInstance.getFlexoConcept();
		String conceptRoleName = FMEFreeModel.conceptRoleName(grConcept);

		// The shape stays as it is, it is the label of the instance it keeps
		String labelProperty = FMEConceptualModel.labelPropertyName(conceptInstance.getFlexoConcept());
		String label = labelProperty != null ? (String) conceptInstance.getFlexoPropertyValue(labelProperty) : null;

		FlexoConcept noneConcept = getFMEFreeModel().getNoneFlexoConcept(getEditor(), this);

		// The opposite of what DeclareInstanceOfExistingConcept does, bypassing the DropScheme as well
		grInstance.setFlexoPropertyValue(conceptRoleName, null);
		grInstance.setFlexoConcept(noneConcept);
		if (label != null) {
			grInstance.setFlexoPropertyValue(FMEFreeModel.NAME_ROLE_NAME, label);
		}
	}

	/**
	 * The drawing has to discover that the shape is now the one of the NoneGR
	 */
	@Override
	protected void refreshRepresentation(FlexoConceptInstance grInstance) {
		DiagramRepresentations.invalidate(DiagramRepresentations.shapeOf(grInstance));
	}

}
