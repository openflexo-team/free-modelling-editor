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

import org.openflexo.fme.model.action.AbstractCreateNewConceptFromElement;
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
 * Makes a new concept emerge from a shape of an annotated slideshow: the concept and its GR concept are created, the concept is
 * instantiated in the sample data, named after the shape, and the shape stands for that instance. The shape itself is not modified.
 * 
 * @author sylvain
 * 
 */
public class CreateNewConceptFromShape extends
		AbstractCreateNewConceptFromElement<CreateNewConceptFromShape, PowerpointShape, FMEPowerpointFreeModel, FMEPowerpointFreeModelInstance> {

	public static FlexoActionFactory<CreateNewConceptFromShape, PowerpointShape, FlexoObject> actionType = new FlexoActionFactory<CreateNewConceptFromShape, PowerpointShape, FlexoObject>(
			"create_new_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewConceptFromShape makeNewAction(PowerpointShape focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewConceptFromShape(focusedObject, globalSelection, editor);
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
		FlexoObjectImpl.addActionForClass(CreateNewConceptFromShape.actionType, PowerpointShape.class);
	}

	private CreateNewConceptFromShape(PowerpointShape focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
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
	public boolean isValid() {
		if (getFMEFreeModelInstance() == null || getFMEFreeModelInstance().getGRInstance(getFocusedObject()) != null) {
			return false;
		}
		return super.isValid();
	}

	/**
	 * A new instance of the new GR concept, standing for the shape
	 */
	@Override
	protected FlexoConceptInstance retrieveRepresentationInstance(FlexoConcept grConcept) throws FlexoException {
		return PowerpointAnnotations.createGRInstance(getFMEFreeModelInstance(), getFocusedObject(), grConcept);
	}

	/**
	 * The shape is not modified, and a slideshow has no palette
	 */
	@Override
	protected void configureRepresentation(FlexoConceptInstance grInstance, FlexoConcept newConcept, FlexoConcept newGRConcept) {
	}

	/**
	 * The overlay of the slideshow follows the notifications of the free model instance
	 */
	@Override
	protected void refreshRepresentation(FlexoConceptInstance grInstance) {
	}

}
