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

import org.openflexo.connie.DataBinding;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.model.action.CreateFMEFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.technologyadapter.powerpoint.BasicPowerpointModelSlot;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;

/**
 * Creates the {@link FMEPowerpointFreeModel} of a {@link FreeModellingProjectNature} (there is one per project, named
 * {@link FMEPowerpointFreeModel#DEFAULT_NAME} by default): its virtual model gets a model slot for the slideshow, connected by its
 * creation scheme to the slideshow given as parameter
 * 
 * @author sylvain
 * 
 */
public class CreateFMEPowerpointFreeModel extends CreateFMEFreeModel<CreateFMEPowerpointFreeModel> {

	public static FlexoActionFactory<CreateFMEPowerpointFreeModel, FreeModellingProjectNature, FlexoObject> actionType = new FlexoActionFactory<CreateFMEPowerpointFreeModel, FreeModellingProjectNature, FlexoObject>(
			"create_powerpoint_free_model", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFMEPowerpointFreeModel makeNewAction(FreeModellingProjectNature focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateFMEPowerpointFreeModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FreeModellingProjectNature object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		/**
		 * One PowerPoint free model per project
		 */
		@Override
		public boolean isEnabledForSelection(FreeModellingProjectNature object, Vector<FlexoObject> globalSelection) {
			return FMEPowerpointFreeModel.getFreeModel(object) == null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFMEPowerpointFreeModel.actionType, FreeModellingProjectNature.class);
	}

	private CreateFMEPowerpointFreeModel(FreeModellingProjectNature focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		setFreeModelName(FMEPowerpointFreeModel.DEFAULT_NAME);
	}

	@Override
	protected FMEPowerpointFreeModel createNewFreeModel(String metaModelName, FMEConceptualModel conceptualModel) {

		VirtualModel newVirtualModel = createVirtualModel(metaModelName, conceptualModel.getAccessedVirtualModelResource());

		if (newVirtualModel == null) {
			return null;
		}

		// The model slot of the slideshow
		CreateModelSlot createMS = CreateModelSlot.actionType.makeNewEmbeddedAction(newVirtualModel, null, this);
		createMS.setTechnologyAdapter(
				getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(PowerpointTechnologyAdapter.class));
		createMS.setModelSlotClass(BasicPowerpointModelSlot.class);
		createMS.setModelSlotName(FMEPowerpointFreeModel.SLIDESHOW_MODEL_SLOT_NAME);
		createMS.doAction();

		// The creation scheme connects it to the slideshow given as parameter
		CreationScheme creationScheme = newVirtualModel.getCreationSchemes().get(0);
		CreateGenericBehaviourParameter createSlideshowParameter = CreateGenericBehaviourParameter.actionType
				.makeNewEmbeddedAction(creationScheme, null, this);
		createSlideshowParameter.setParameterName(FMEPowerpointFreeModel.SLIDESHOW_MODEL_SLOT_NAME);
		createSlideshowParameter.setParameterType(PowerpointSlideshow.class);
		createSlideshowParameter.doAction();

		CreateEditionAction assignSlideshowAction = CreateEditionAction.actionType.makeNewEmbeddedAction(creationScheme.getControlGraph(),
				null, this);
		assignSlideshowAction.setEditionActionClass(ExpressionAction.class);
		assignSlideshowAction.setAssignation(new DataBinding<>(FMEPowerpointFreeModel.SLIDESHOW_MODEL_SLOT_NAME));
		assignSlideshowAction.doAction();
		ExpressionAction<?> assignSlideshow = (ExpressionAction<?>) assignSlideshowAction.getBaseEditionAction();
		assignSlideshow.setExpression(new DataBinding<>("parameters." + FMEPowerpointFreeModel.SLIDESHOW_MODEL_SLOT_NAME));

		// Save
		try {
			newVirtualModel.getResource().save();
		} catch (SaveResourceException e) {
			e.printStackTrace();
		}

		FMEPowerpointFreeModel newFreeModel = getFocusedObject().getOwner().getModelFactory().newInstance(FMEPowerpointFreeModel.class);
		newFreeModel.setAccessedVirtualModel(newVirtualModel);
		getFocusedObject().addToFreeModels(newFreeModel);

		return newFreeModel;
	}

	@Override
	public FMEPowerpointFreeModel getNewFreeModel() {
		return (FMEPowerpointFreeModel) super.getNewFreeModel();
	}

}
