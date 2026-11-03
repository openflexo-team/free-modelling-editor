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

package org.openflexo.fme.diagram;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.FMETechnologyExtension;
import org.openflexo.fme.controller.FMEController;
import org.openflexo.fme.controller.FMEPerspective;
import org.openflexo.fme.controller.action.CreateNewConceptFromElementInitializer;
import org.openflexo.fme.controller.action.CreateNewConceptFromLinkInitializer;
import org.openflexo.fme.controller.action.CreateNewFMEPropertyFromLinkInitializer;
import org.openflexo.fme.controller.action.DeclareInstanceOfExistingConceptInitializer;
import org.openflexo.fme.controller.action.DisconnectInstanceFromConceptInitializer;
import org.openflexo.fme.controller.action.FMEControllerActionInitializer;
import org.openflexo.fme.diagram.controller.action.CreateFMEDiagramFreeModelInitializer;
import org.openflexo.fme.diagram.controller.action.DropFreeShapeInitializer;
import org.openflexo.fme.diagram.controller.action.InstantiateFMEDiagramFreeModelInitializer;
import org.openflexo.fme.diagram.controller.editor.FreeModelDiagramEditor;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.diagram.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.action.CreateNewConceptFromDiagramConnector;
import org.openflexo.fme.diagram.model.action.CreateNewConceptFromDiagramElement;
import org.openflexo.fme.diagram.model.action.CreateNewConceptFromNoneConcept;
import org.openflexo.fme.diagram.model.action.CreateNewFMEPropertyFromDiagramConnector;
import org.openflexo.fme.diagram.model.action.DeclareInstanceOfExistingConcept;
import org.openflexo.fme.diagram.model.action.DeclareInstanceOfExistingConceptFromDiagramElement;
import org.openflexo.fme.diagram.model.action.DisconnectInstanceFromConcept;
import org.openflexo.fme.diagram.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.fme.diagram.view.FMEDiagramFreeModelModuleView;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.nature.NatureObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.icon.IconFactory;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.swing.FlexoCollabsiblePanelGroup;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.controller.DiagramTechnologyAdapterController;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;

/**
 * The Diagram technology of the FreeModellingEditor: concepts are represented by the shapes and connectors of a diagram
 * 
 * @author sylvain
 * 
 */
public class DiagramFMEExtension implements FMETechnologyExtension {

	public static final String IDENTIFIER = "diagram";

	/**
	 * What a Diagram user chooses more when declaring a shape as an instance of an existing concept: the strategy of representation
	 */
	public static final Resource SHAPE_REPRESENTATION_OPTIONS = ResourceLocator.locateResource("Fib/Wizard/ShapeRepresentationOptions.fib");

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Class<? extends TechnologyAdapter<?>> getTechnologyAdapterClass() {
		return DiagramTechnologyAdapter.class;
	}

	@Override
	public List<Class<?>> getModelEntities() {
		return Arrays.asList(FMEDiagramFreeModel.class, FMEDiagramFreeModelInstance.class);
	}

	@Override
	public Class<? extends FMEFreeModel> getFreeModelClass() {
		return FMEDiagramFreeModel.class;
	}

	@Override
	public Class<? extends FMEFreeModelInstance> getFreeModelInstanceClass() {
		return FMEDiagramFreeModelInstance.class;
	}

	@Override
	public FlexoActionFactory<CreateFMEDiagramFreeModel, FreeModellingProjectNature, FlexoObject> getCreateFreeModelActionType() {
		return CreateFMEDiagramFreeModel.actionType;
	}

	@Override
	public FlexoActionFactory<InstantiateFMEDiagramFreeModel, NatureObject<FreeModellingProjectNature>, FlexoObject> getInstantiateFreeModelActionType() {
		return InstantiateFMEDiagramFreeModel.actionType;
	}

	@Override
	public ImageIcon getIcon() {
		return FMEIconLibrary.DIAGRAM_ICON;
	}

	@Override
	public ImageIcon getBigIcon() {
		return FMEIconLibrary.DIAGRAM_BIG_ICON;
	}

	@Override
	public ImageIcon getIconForObject(Object object) {
		if (object instanceof FMEDiagramFreeModelInstance) {
			return FMEIconLibrary.DIAGRAM_ICON;
		}
		if (object instanceof FMEDiagramFreeModel) {
			return IconFactory.getImageIcon(FMEIconLibrary.DIAGRAM_ICON, FMEIconLibrary.FME_MARKER);
		}
		return null;
	}

	@Override
	public void initializeActions(FMEControllerActionInitializer actionInitializer) {
		new CreateFMEDiagramFreeModelInitializer(actionInitializer);
		new InstantiateFMEDiagramFreeModelInitializer(actionInitializer);

		new DropFreeShapeInitializer(actionInitializer);
		new CreateNewConceptFromElementInitializer<>(CreateNewConceptFromNoneConcept.actionType, actionInitializer);
		new DeclareInstanceOfExistingConceptInitializer<>(DeclareInstanceOfExistingConcept.actionType, SHAPE_REPRESENTATION_OPTIONS,
				actionInitializer);
		new DisconnectInstanceFromConceptInitializer<>(DisconnectInstanceFromConcept.actionType, actionInitializer);

		// Actions applied on Diagram Elements(not currently associated with flexo concept)
		new CreateNewConceptFromElementInitializer<>(CreateNewConceptFromDiagramElement.actionType, actionInitializer) {
			@Override
			protected Icon getEnabledIcon(FlexoActionFactory<CreateNewConceptFromDiagramElement, DiagramElement<?>, FlexoObject> actionType) {
				return FMEIconLibrary.FME_SMALL_ICON;
			}
		};
		new DeclareInstanceOfExistingConceptInitializer<>(DeclareInstanceOfExistingConceptFromDiagramElement.actionType,
				SHAPE_REPRESENTATION_OPTIONS, actionInitializer) {
			@Override
			protected Icon getEnabledIcon(
					FlexoActionFactory<DeclareInstanceOfExistingConceptFromDiagramElement, DiagramElement<?>, FlexoObject> actionType) {
				return FMEIconLibrary.FME_SMALL_ICON;
			}
		};
		new CreateNewConceptFromLinkInitializer<>(CreateNewConceptFromDiagramConnector.actionType, actionInitializer);
		new CreateNewFMEPropertyFromLinkInitializer<>(CreateNewFMEPropertyFromDiagramConnector.actionType, actionInitializer);
	}

	@Override
	public ModuleView<?> createModuleView(FMEFreeModelInstance freeModelInstance, FMEController controller, FMEPerspective perspective) {
		// Initialization of Diagram representation may rise PAMELA edits
		// The goal is here to embed all those edits in a special edit record
		// Which is to be discarded as undoable action at the end of this initialization
		CompoundEdit edit = null;
		if (controller.getEditor().getUndoManager() != null) {
			edit = controller.getEditor().getUndoManager().startRecording("Initialize free diagram");
		}
		FreeModelDiagramEditor editor = new FreeModelDiagramEditor((FMEDiagramFreeModelInstance) freeModelInstance, false, controller,
				getDiagramTechnologyAdapterController(controller).getToolFactory());
		if (edit != null) {
			controller.getEditor().getUndoManager().stopRecording(edit);
			// Make this edit not-undoable
			edit.die();
		}
		return new FMEDiagramFreeModelModuleView(editor, perspective);
	}

	@Override
	public FlexoCollabsiblePanelGroup getInspectorPanelGroup(FlexoController controller) {
		return getDiagramTechnologyAdapterController(controller).getInspectors().getPanelGroup();
	}

	private static DiagramTechnologyAdapterController getDiagramTechnologyAdapterController(FlexoController controller) {
		return controller.getApplicationContext().getTechnologyAdapterControllerService()
				.getTechnologyAdapterController(DiagramTechnologyAdapterController.class);
	}

}
