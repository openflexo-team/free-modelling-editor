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

import javax.swing.ImageIcon;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.FMEModule;
import org.openflexo.fme.controller.action.FMEControllerActionInitializer;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FMESampleData;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.view.ConvertToFMEProjectView;
import org.openflexo.fme.view.FMEWelcomePanelModuleView;
import org.openflexo.fme.view.menu.FMEMenuBar;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.validation.FlexoValidationModel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.module.FlexoModule.WelcomePanel;
import org.openflexo.selection.MouseSelectionManager;
import org.openflexo.view.FlexoMainPane;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;
import org.openflexo.view.menu.FlexoMenuBar;

/**
 * This is the controller of ViewPointModeller module
 * 
 * @author sylvain
 */
public class FMEController extends FlexoController {

	private static final Logger logger = Logger.getLogger(FMEController.class.getPackage().getName());

	public FMEPerspective FREE_MODELLING_PERSPECTIVE;

	/**
	 * Default constructor
	 */
	public FMEController(FMEModule module) {
		super(module);
	}

	@Override
	protected void initializePerspectives() {
		addToPerspectives(FREE_MODELLING_PERSPECTIVE = new FMEPerspective(this));
	}

	@Override
	public FlexoPerspective getDefaultPerspective() {
		return FREE_MODELLING_PERSPECTIVE;
	}

	@Override
	protected MouseSelectionManager createSelectionManager() {
		return new FMESelectionManager(this);
	}

	@Override
	public ControllerActionInitializer createControllerActionInitializer() {
		return new FMEControllerActionInitializer(this);
	}

	/**
	 * Creates a new instance of MenuBar for the module this controller refers to
	 * 
	 * @return
	 */
	@Override
	protected FlexoMenuBar createNewMenuBar() {
		return new FMEMenuBar(this);
	}

	/**
	 * Init inspectors
	 */
	@Override
	public void initInspectors() {
		super.initInspectors();
	}

	@Override
	protected FlexoMainPane createMainPane() {
		return new FlexoMainPane(this);
	}

	@Override
	public FlexoObject getDefaultObjectToSelect(FlexoProject<?> project) {
		if (project != null && project.hasNature(FreeModellingProjectNature.class)) {
			return project.getNature(FreeModellingProjectNature.class);
		}
		return project;
	}

	/**
	 * Select the view representing supplied object, if this view exists. Try all to really display supplied object, even if required view
	 * is not the current displayed view
	 * 
	 * @param object
	 *            : the object to focus on
	 */
	@Override
	public void selectAndFocusObject(FlexoObject object) {
		if (object != null) {
			logger.info("selectAndFocusObject " + object + "of " + object.getClass().getSimpleName());
			if (object instanceof FlexoProject) {
				setCurrentEditedObject(object);
			}
			else if (object instanceof FreeModellingProjectNature) {
				setCurrentEditedObject(object);
			}
			if (object instanceof FMEFreeModel) {
				setCurrentEditedObject(object);
			}
			if (object instanceof FMEFreeModelInstance) {
				setCurrentEditedObject(object);
			}
			if (getCurrentPerspective() == FREE_MODELLING_PERSPECTIVE) {
				if (object instanceof FMEFreeModelInstance) {
					FREE_MODELLING_PERSPECTIVE.focusOnFreeModel((FMEFreeModelInstance) object);
				}
			}
			getSelectionManager().setSelectedObject(object);
		}
		else {
			logger.warning("Cannot set focus on a NULL object");
		}
	}

	public VirtualModel getCurrentVirtualModel() {
		if (getCurrentDisplayedObjectAsModuleView() instanceof FMLObject) {
			return ((FMLObject) getCurrentDisplayedObjectAsModuleView()).getDeclaringCompilationUnit().getVirtualModel();
		}
		return null;
	}

	@Override
	public FlexoValidationModel getValidationModelForObject(FlexoObject object) {
		if (object instanceof FMLObject) {
			return getApplicationContext().getVirtualModelLibrary().getFMLValidationModel();
		}
		return super.getValidationModelForObject(object);
	}

	@Override
	public void updateEditor(FlexoEditor from, FlexoEditor to) {
		super.updateEditor(from, to);
		FlexoProject<?> project = (to != null ? to.getProject() : null);
		FREE_MODELLING_PERSPECTIVE.setProject(project);
	}

	@Override
	public ImageIcon iconForObject(Object object) {
		if (object instanceof FreeModellingProjectNature) {
			return IconFactory.getImageIcon(IconLibrary.OPENFLEXO_NOTEXT_16, FMEIconLibrary.FME_MARKER);
		}
		else if (object instanceof FMEConceptualModel) {
			return FMLIconLibrary.VIRTUAL_MODEL_ICON;
		}
		else if (object instanceof FMESampleData) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		}
		else if (object instanceof FMEDiagramFreeModelInstance) {
			return FMEIconLibrary.DIAGRAM_ICON;
		}
		else if (object instanceof FMEDiagramFreeModel) {
			return IconFactory.getImageIcon(FMEIconLibrary.DIAGRAM_ICON, FMEIconLibrary.FME_MARKER);
		}
		return super.iconForObject(object);
	}

	@Override
	public ModuleView<?> makeWelcomePanel(WelcomePanel<?> welcomePanel, FlexoPerspective perspective) {
		return new FMEWelcomePanelModuleView((WelcomePanel<FMEModule>) welcomePanel, this, perspective);
	}

	@Override
	public ModuleView<?> makeDefaultProjectView(FlexoProject<?> project, FlexoPerspective perspective) {
		return new ConvertToFMEProjectView(project, this, perspective);
	}

}
