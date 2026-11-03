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
package org.openflexo.fme.pptx.model.action;

import java.util.Vector;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.AbstractDisconnectInstanceFromConcept;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.fme.pptx.model.PowerpointAnnotations;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.powerpoint.fml.PowerpointShapeRole;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;

/**
 * Removes the annotation of a shape: the shape is a plain PowerPoint shape again, the instance of the GR concept standing for it is
 * deleted. The conceptual instance is deleted only when nothing else represents it (see {@link AbstractDisconnectInstanceFromConcept}).
 * The shape itself is not modified.
 * 
 * @author sylvain
 * 
 */
public class DisconnectShapeFromConcept extends
		AbstractDisconnectInstanceFromConcept<DisconnectShapeFromConcept, PowerpointShape, FMEPowerpointFreeModel, FMEPowerpointFreeModelInstance> {

	public static FlexoActionFactory<DisconnectShapeFromConcept, PowerpointShape, FlexoObject> actionType = new FlexoActionFactory<DisconnectShapeFromConcept, PowerpointShape, FlexoObject>(
			"disconnect_instance_from_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DisconnectShapeFromConcept makeNewAction(PowerpointShape focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new DisconnectShapeFromConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(PowerpointShape object, Vector<FlexoObject> globalSelection) {
			return PowerpointAnnotations.isAnnotated(object);
		}

		@Override
		public boolean isEnabledForSelection(PowerpointShape object, Vector<FlexoObject> globalSelection) {
			return PowerpointAnnotations.isAnnotated(object) && getIssue(PowerpointAnnotations.getGRInstance(object)) == null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DisconnectShapeFromConcept.actionType, PowerpointShape.class);
	}

	private DisconnectShapeFromConcept(PowerpointShape focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * The instance standing for the shape is deleted, without its deletion scheme (which would delete the conceptual instance): the shape
	 * and the conceptual instance are detached first, while the instance still belongs to its virtual model instance (the reference to the
	 * shape needs its model slot), and the shape is left alone
	 */
	@Override
	protected void releaseRepresentation(FlexoConceptInstance grInstance, FlexoConceptInstance conceptInstance) {
		FlexoConcept grConcept = grInstance.getFlexoConcept();
		grInstance.setFlexoActor(null, (PowerpointShapeRole) grConcept.getAccessibleProperty(FMEPowerpointFreeModel.SHAPE_ROLE_NAME));
		grInstance.setFlexoPropertyValue(FMEFreeModel.conceptRoleName(grConcept), null);
		grInstance.performCoreDeletion();
	}

	/**
	 * The overlay of the slideshow follows the notifications of the free model instance
	 */
	@Override
	protected void refreshRepresentation(FlexoConceptInstance grInstance) {
	}

}
