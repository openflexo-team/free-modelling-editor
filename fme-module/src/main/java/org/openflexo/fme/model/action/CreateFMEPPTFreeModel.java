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

import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.poi.hslf.model.Slide;
import org.openflexo.connie.DataBinding;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEPPTFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
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
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot;
import org.openflexo.technologyadapter.diagram.fml.action.CreateDiagramPalette;
import org.openflexo.technologyadapter.diagram.fml.action.CreateDiagramSpecification;
import org.openflexo.technologyadapter.diagram.fml.action.CreateExampleDiagram;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramSpecification;
import org.openflexo.technologyadapter.diagram.model.DiagramType;

/**
 * This action is used to create a new {@link FMEPPTFreeModel} in a {@link FreeModellingProjectNature}<br>
 * 
 * @author sylvain
 * 
 */
public class CreateFMEPPTFreeModel extends CreateFMEFreeModel<CreateFMEPPTFreeModel> {

	private static final Logger logger = Logger.getLogger(CreateFMEPPTFreeModel.class.getPackage().getName());

	public static FlexoActionFactory<CreateFMEPPTFreeModel, FreeModellingProjectNature, FlexoObject> actionType = new FlexoActionFactory<CreateFMEPPTFreeModel, FreeModellingProjectNature, FlexoObject>(
			"create_ppt_free_model", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFMEPPTFreeModel makeNewAction(FreeModellingProjectNature focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateFMEPPTFreeModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FreeModellingProjectNature object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FreeModellingProjectNature object, Vector<FlexoObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFMEPPTFreeModel.actionType, FreeModellingProjectNature.class);
	}

	private File pptFile;
	private Slide slide;

	private CreateFMEPPTFreeModel(FreeModellingProjectNature focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	private CreateFMEPPTFreeModel(FlexoActionFactory<CreateFMEPPTFreeModel, FreeModellingProjectNature, FlexoObject> actionType,
			FreeModellingProjectNature focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public File getFile() {
		return pptFile;
	}

	public void setFile(File pptFile) {
		if ((pptFile == null && this.pptFile != null) || (pptFile != null && !pptFile.equals(this.pptFile))) {
			File oldValue = this.pptFile;
			this.pptFile = pptFile;
			getPropertyChangeSupport().firePropertyChange("file", oldValue, pptFile);
		}
	}

	public Slide getSlide() {
		return slide;
	}

	public void setSlide(Slide slide) {
		if ((slide == null && this.slide != null) || (slide != null && !slide.equals(this.slide))) {
			Slide oldValue = this.slide;
			this.slide = slide;
			getPropertyChangeSupport().firePropertyChange("slide", oldValue, slide);
		}
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (getFile() == null || !getFile().exists()) {
			return false;
		}

		if (getSlide() == null) {
			return false;
		}

		return true;
	}

	@Override
	protected FMEDiagramFreeModel createNewFreeModel(String metaModelName, FMEConceptualModel conceptualModel) {

		// First we create the diagram specification
		// System.out.println("Creating DiagramSpecification...");

		CreateDiagramSpecification createDS = CreateDiagramSpecification.actionType
				.makeNewEmbeddedAction(null/*getDiagramSpecificationFolder()*/, null, this);
		createDS.setNewDiagramSpecificationName(metaModelName);
		createDS.doAction();
		DiagramSpecification diagramSpecification = createDS.getNewDiagramSpecification();
		// System.out.println("DiagramSpecification has been created: " + diagramSpecification);

		if (diagramSpecification == null) {
			return null;
		}

		CreateExampleDiagram createExampleDiagram = CreateExampleDiagram.actionType.makeNewEmbeddedAction(diagramSpecification, null, this);
		if (createExampleDiagram == null) {
			return null;
		}
		createExampleDiagram.setNewDiagramName("Default");
		createExampleDiagram.setNewDiagramTitle("Default example diagram");
		createExampleDiagram.doAction();

		CreateDiagramPalette createPalette = CreateDiagramPalette.actionType.makeNewEmbeddedAction(diagramSpecification, null, this);
		if (createPalette == null) {
			return null;
		}
		createPalette.setNewPaletteName(FMEDiagramFreeModel.PALETTE_NAME);
		createPalette.doAction();
		// System.out.println("Palette has been created: " + createPalette.getNewPalette());

		// Now we create the VirtualModel
		VirtualModel newVirtualModel = createVirtualModel(metaModelName, conceptualModel.getAccessedVirtualModelResource());

		if (newVirtualModel == null) {
			return null;
		}

		// Now we create the diagram model slot
		CreateModelSlot createMS = CreateModelSlot.actionType.makeNewEmbeddedAction(newVirtualModel, null, this);
		createMS.setTechnologyAdapter(
				getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(DiagramTechnologyAdapter.class));
		createMS.setModelSlotClass(TypedDiagramModelSlot.class);
		createMS.setModelSlotName(FMEDiagramFreeModel.DIAGRAM_MODEL_SLOT_NAME);
		createMS.setMmRes(diagramSpecification.getResource());
		createMS.doAction();

		// Complete the creation scheme
		CreationScheme creationScheme = newVirtualModel.getCreationSchemes().get(0);
		CreateGenericBehaviourParameter createDiagramParameter = CreateGenericBehaviourParameter.actionType
				.makeNewEmbeddedAction(creationScheme, null, this);
		createDiagramParameter.setParameterName("diagram");
		createDiagramParameter.setParameterType(DiagramType.getDiagramType(diagramSpecification));
		createDiagramParameter.doAction();

		CreateEditionAction assignDiagramAction = CreateEditionAction.actionType.makeNewEmbeddedAction(creationScheme.getControlGraph(),
				null, this);
		assignDiagramAction.setEditionActionClass(ExpressionAction.class);
		assignDiagramAction.setAssignation(new DataBinding<>(FMEDiagramFreeModel.DIAGRAM_MODEL_SLOT_NAME));
		assignDiagramAction.doAction();
		ExpressionAction<?> assignDiagram = (ExpressionAction<?>) assignDiagramAction.getBaseEditionAction();
		assignDiagram.setExpression(new DataBinding<>("parameters.diagram"));

		// Save
		try {
			newVirtualModel.getResource().save();
		} catch (SaveResourceException e1) {
			e1.printStackTrace();
		}

		FMEDiagramFreeModel newFreeModel = getFocusedObject().getOwner().getModelFactory().newInstance(FMEDiagramFreeModel.class);
		newFreeModel.setAccessedVirtualModel(newVirtualModel);
		getFocusedObject().addToFreeModels(newFreeModel);

		return newFreeModel;

	}

	@Override
	public FMEDiagramFreeModel getNewFreeModel() {
		return (FMEDiagramFreeModel) super.getNewFreeModel();
	}

}
