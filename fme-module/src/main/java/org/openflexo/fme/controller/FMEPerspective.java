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

package org.openflexo.fme.controller;

import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.FMEModule;
import org.openflexo.fme.controller.editor.FreeModelDiagramEditor;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.view.ConvertToFMEProjectView;
import org.openflexo.fme.view.FMEDiagramFreeModelModuleView;
import org.openflexo.fme.view.FMEFreeModelModuleView;
import org.openflexo.fme.view.FMEProjectNatureModuleView;
import org.openflexo.fme.view.FMEWelcomePanelModuleView;
import org.openflexo.fme.widget.FIBConceptBrowser;
import org.openflexo.fme.widget.FIBFreeModellingProjectBrowser;
import org.openflexo.fme.widget.FIBRepresentedConceptBrowser;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.inspector.FIBFlexoConceptInstanceInspectorPanel;
import org.openflexo.module.FlexoModule.WelcomePanel;
import org.openflexo.pamela.undo.CompoundEdit;
import javax.swing.SwingUtilities;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.inspector.ModuleInspectorController.EmptySelectionActivated;
import org.openflexo.inspector.ModuleInspectorController.InspectedObjectChanged;
import org.openflexo.inspector.ModuleInspectorController.MultipleSelectionActivated;
import org.openflexo.swing.FlexoCollabsiblePanel;
import org.openflexo.swing.FlexoCollabsiblePanelGroup;
import org.openflexo.technologyadapter.diagram.controller.DiagramTechnologyAdapterController;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;
import org.openflexo.view.controller.model.NaturePerspective;

public class FMEPerspective extends NaturePerspective<FreeModellingProjectNature> {

	protected static final Logger logger = Logger.getLogger(FMEPerspective.class.getPackage().getName());

	private FIBFreeModellingProjectBrowser freeModellingProjectBrowser = null;
	private final FIBRepresentedConceptBrowser representedConceptBrowser;
	private final FIBConceptBrowser conceptBrowser;

	private FlexoCollabsiblePanelGroup inspectorPanelGroup;
	private FlexoCollabsiblePanel representedConceptPanel;
	private FIBFlexoConceptInstanceInspectorPanel inspectorPanel;

	/**
	 * Default constructor taking controller as argument
	 */
	public FMEPerspective(FMEController controller) {
		super(controller);

		freeModellingProjectBrowser = new FIBFreeModellingProjectBrowser(controller.getProject(), controller);

		setTopLeftView(freeModellingProjectBrowser);

		representedConceptBrowser = new FIBRepresentedConceptBrowser(null, controller);
		conceptBrowser = new FIBConceptBrowser(null, controller);

		inspectorPanel = new FIBFlexoConceptInstanceInspectorPanel(getController().getModuleInspectorController());
		inspectorPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

	}

	@Override
	public String getName() {
		return "free_modelling_perspective";
	}

	public FlexoCollabsiblePanelGroup getInspectorPanelGroup() {
		if (inspectorPanelGroup == null) {
			inspectorPanelGroup = getDiagramTechnologyAdapterController(getController()).getInspectors().getPanelGroup();
			representedConceptPanel = inspectorPanelGroup.insertContentsAtIndex(
					getController().getModuleLocales().localizedForKey("represented_concept"), inspectorPanel, 0);
			getController().getModuleInspectorController().addObserver((o, notification) -> updateInspectorTitle(notification));
		}
		return inspectorPanelGroup;
	}

	/**
	 * The title of the panel of the inspector follows what is inspected: the name of the concept when it is an instance of a concept, the
	 * type of the object otherwise (and what it was named at first when nothing, or several objects, are selected)
	 */
	private void updateInspectorTitle(Object notification) {
		String title = null;
		if (notification instanceof EmptySelectionActivated || notification instanceof MultipleSelectionActivated) {
			title = getController().getModuleLocales().localizedForKey("represented_concept");
		}
		else if (notification instanceof InspectedObjectChanged) {
			title = titleFor(((InspectedObjectChanged) notification).getInspectedObject());
		}
		if (title != null) {
			final String newTitle = title;
			SwingUtilities.invokeLater(() -> {
				if (representedConceptPanel != null) {
					representedConceptPanel.setTitle(newTitle);
				}
			});
		}
	}

	private String titleFor(Object inspectedObject) {
		if (inspectedObject == null) {
			return getController().getModuleLocales().localizedForKey("represented_concept");
		}
		if (inspectedObject instanceof FlexoConceptInstance && !(inspectedObject instanceof VirtualModelInstance)
				&& ((FlexoConceptInstance) inspectedObject).getFlexoConcept() != null) {
			return conceptName(((FlexoConceptInstance) inspectedObject).getFlexoConcept());
		}
		if (inspectedObject instanceof FlexoObject && ((FlexoObject) inspectedObject).getImplementedInterface() != null) {
			return ((FlexoObject) inspectedObject).getImplementedInterface().getSimpleName();
		}
		return inspectedObject.getClass().getSimpleName();
	}

	/**
	 * The name of the concept an instance of supplied concept is an instance of, as the browsers show it: the one of the conceptual concept
	 * when it is an instance of a GR concept, "unclassified" for what is not an instance of any concept
	 */
	private String conceptName(FlexoConcept concept) {
		if (concept.getName().equals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME)) {
			return getController().getModuleLocales().localizedForKey("unclassified");
		}
		FlexoConceptInstanceRole conceptRole = FMEFreeModel.conceptRole(concept);
		if (conceptRole != null && conceptRole.getFlexoConceptType() != null) {
			return conceptRole.getFlexoConceptType().getName();
		}
		return concept.getName();
	}

	public DiagramTechnologyAdapterController getDiagramTechnologyAdapterController(FlexoController controller) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		return tacService.getTechnologyAdapterController(DiagramTechnologyAdapterController.class);
	}

	@Override
	public Class<FreeModellingProjectNature> getNatureClass() {
		return FreeModellingProjectNature.class;
	}

	public ModuleView<?> getCurrentModuleView(FlexoController controller) {
		return controller.getCurrentModuleView();
	}

	/**
	 * Overrides getIcon
	 * 
	 * @see org.openflexo.view.controller.model.FlexoPerspective#getActiveIcon()
	 */
	@Override
	public ImageIcon getActiveIcon() {
		return FMEIconLibrary.FME_SMALL_ICON;
	}

	public void focusOnFreeModel(FMEFreeModelInstance freeModel) {
		logger.info("focusOnFreeModel " + freeModel);

		representedConceptBrowser.setFreeModel(freeModel);
		conceptBrowser.setFreeModel(freeModel);

		setMiddleLeftView(representedConceptBrowser);
		setBottomLeftView(conceptBrowser);

		// setMiddleRightView(inspectorPanel);

	}

	@Override
	public void focusOnObject(FlexoObject object) {
		if (object instanceof FMEFreeModelInstance) {
			focusOnFreeModel((FMEFreeModelInstance) object);
		}
	};

	@Override
	public void objectWasClicked(Object object, FlexoController controller) {
		// logger.info("ViewPointPerspective: object was clicked: " + object);
		if (object == null) {
			return;
		}
	}

	@Override
	public void objectWasRightClicked(Object object, FlexoController controller) {
		// logger.info("ViewPointPerspective: object was right-clicked: " + object);
	}

	@Override
	public void objectWasDoubleClicked(Object object, FlexoController controller) {
		if (object instanceof FMEFreeModelInstance) {
			controller.selectAndFocusObject((FMEFreeModelInstance) object);
		}
		else {
			super.objectWasDoubleClicked(object, controller);
		}
	}

	public void setProject(FlexoProject<?> project) {
		freeModellingProjectBrowser.setRootObject(project);
	}

	@Override
	public boolean isRepresentableInModuleView(FlexoObject object) {
		if (object instanceof FreeModellingProjectNature) {
			return true;
		}
		if (object instanceof FMEFreeModel) {
			return true;
		}
		if (object instanceof FMEFreeModelInstance) {
			return true;
		}
		return super.isRepresentableInModuleView(object);
	}

	@Override
	public FlexoObject getRepresentableMasterObject(FlexoObject object) {
		if (object instanceof FreeModellingProjectNature) {
			return object;
		}
		if (object instanceof FMEFreeModel) {
			return object;
		}
		if (object instanceof FMEFreeModelInstance) {
			return object;
		}
		return super.getRepresentableMasterObject(object);
	}

	@Override
	public String getWindowTitleforObject(FlexoObject object, FlexoController controller) {
		if (object instanceof WelcomePanel) {
			return "Welcome";
		}
		if (object instanceof FlexoProject) {
			return ((FlexoProject<?>) object).getName();
		}
		if (object instanceof FreeModellingProjectNature) {
			return ((FreeModellingProjectNature) object).getOwner().getName();
		}
		if (object instanceof FMEFreeModel) {
			return ((FMEFreeModel) object).getName();
		}
		if (object instanceof FMEFreeModelInstance) {
			return ((FMEFreeModelInstance) object).getName();
		}
		if (object != null) {
			return object.toString();
		}
		return "null";
	}

	public void closeFreeModelBrowsers() {
		setMiddleLeftView(null);
		setBottomLeftView(null);
	}

	@Override
	public FlexoObject getDefaultObject(FlexoObject proposedObject) {
		if (proposedObject instanceof FlexoProject && ((FlexoProject<?>) proposedObject).hasNature(FreeModellingProjectNature.class)) {
			return ((FlexoProject<?>) proposedObject).getNature(FreeModellingProjectNature.class);
		}

		return super.getDefaultObject(proposedObject);
	}

	@Override
	public ModuleView<?> createModuleViewForMasterObject(FlexoObject object) {
		if (object instanceof WelcomePanel) {
			return new FMEWelcomePanelModuleView((WelcomePanel<FMEModule>) object, getController(), this);
		}
		if (object instanceof FlexoProject) {
			return new ConvertToFMEProjectView((FlexoProject<?>) object, getController(), this);
		}
		if (object instanceof FreeModellingProjectNature) {
			return new FMEProjectNatureModuleView((FreeModellingProjectNature) object, getController(), this);
		}
		if (object instanceof FMEFreeModel) {
			return new FMEFreeModelModuleView((FMEFreeModel) object, getController(), this);
		}
		if (object instanceof FMEFreeModelInstance && ((FMEFreeModelInstance) object).getFreeModel() != null) {
			// A free model created before the inspectors of its concepts were generated gets them when opened
			((FMEFreeModelInstance) object).getFreeModel().generateMissingInspectors();
		}
		if (object instanceof FMEDiagramFreeModelInstance) {
			// Initialization of Diagram representation may rise PAMELA edits
			// The goal is here to embed all those edits in a special edit record
			// Which is to be discarded as undoable action at the end of this initialization
			CompoundEdit edit = null;
			if (getController().getEditor().getUndoManager() != null) {
				edit = getController().getEditor().getUndoManager().startRecording("Initialize free diagram");
			}
			DiagramTechnologyAdapterController diagramTAC = getController().getApplicationContext().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(DiagramTechnologyAdapterController.class);
			FreeModelDiagramEditor editor = new FreeModelDiagramEditor((FMEDiagramFreeModelInstance) object, false, getController(),
					diagramTAC.getToolFactory());
			if (edit != null) {
				getController().getEditor().getUndoManager().stopRecording(edit);
				// Make this edit not-undoable
				edit.die();
			}
			return new FMEDiagramFreeModelModuleView(editor, this);
		}
		return super.createModuleViewForMasterObject(object);
	}
}
