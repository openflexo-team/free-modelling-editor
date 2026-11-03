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

import org.openflexo.fme.model.action.AbstractDeclareInstanceOfExistingConcept;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.fme.pptx.model.PowerpointAnnotations;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;

/**
 * Declares a shape of an annotated slideshow as an instance of an existing concept: a new instance of the concept, or one already in the
 * sample data. The instance of the GR concept standing for the shape is created; the shape itself is not modified.
 * 
 * @author sylvain
 * 
 */
public class DeclareShapeAsInstanceOfExistingConcept extends
		AbstractDeclareInstanceOfExistingConcept<DeclareShapeAsInstanceOfExistingConcept, PowerpointShape, FMEPowerpointFreeModel, FMEPowerpointFreeModelInstance> {

	public static FlexoActionFactory<DeclareShapeAsInstanceOfExistingConcept, PowerpointShape, FlexoObject> actionType = new FlexoActionFactory<DeclareShapeAsInstanceOfExistingConcept, PowerpointShape, FlexoObject>(
			"declare_instance_of_existing_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeclareShapeAsInstanceOfExistingConcept makeNewAction(PowerpointShape focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new DeclareShapeAsInstanceOfExistingConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(PowerpointShape object, Vector<FlexoObject> globalSelection) {
			return !PowerpointAnnotations.getFreeModelInstances(object).isEmpty();
		}

		@Override
		public boolean isEnabledForSelection(PowerpointShape object, Vector<FlexoObject> globalSelection) {
			return PowerpointAnnotations.isAnnotatable(object);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeclareShapeAsInstanceOfExistingConcept.actionType, PowerpointShape.class);
	}

	private DeclareShapeAsInstanceOfExistingConcept(PowerpointShape focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * The text of the shape, or its PowerPoint name
	 */
	@Override
	protected String getDefaultInstanceName() {
		return PowerpointAnnotations.defaultInstanceName(getFocusedObject());
	}

	@Override
	public String getIssue() {
		if (getFMEFreeModelInstance() != null && getFMEFreeModelInstance().getGRInstance(getFocusedObject()) != null) {
			return "that_shape_is_already_annotated";
		}
		return super.getIssue();
	}

	/**
	 * A new instance of the GR concept, standing for the shape
	 */
	@Override
	protected FlexoConceptInstance retrieveRepresentationInstance(FlexoConcept grConcept) throws FlexoException {
		return PowerpointAnnotations.createGRInstance(getFMEFreeModelInstance(), getFocusedObject(), grConcept);
	}

	/**
	 * The shape is not modified
	 */
	@Override
	protected void prepareRepresentation(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting) {
	}

	@Override
	protected void completeRepresentation(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting) {
	}

	/**
	 * The overlay of the slideshow follows the notifications of the free model instance
	 */
	@Override
	protected void refreshRepresentation(FlexoConceptInstance grInstance) {
	}

}
