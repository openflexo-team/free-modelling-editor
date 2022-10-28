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

import org.openflexo.fme.FMEModule;
import org.openflexo.fme.controller.editor.FreeModelDiagramEditor;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.view.FMEDiagramFreeModelModuleView;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Represents the controller of a FIBComponent in FME graphical context
 * 
 * 
 * @author sylvain
 * 
 * @param <T>
 */
public class FMEFIBController extends FlexoFIBController {

	private static final Logger logger = Logger.getLogger(FMEFIBController.class.getPackage().getName());

	public FMEFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
		// Default parent localizer is the main localizer
		setParentLocalizer(FlexoLocalization.getMainLocalizer());
	}

	public final LocalizedDelegate getLocales() {
		try {
			return getServiceManager().getModuleLoader().getModuleInstance(FMEModule.class).getLocales();
		} catch (ModuleLoadingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public FMEController getFlexoController() {
		return (FMEController) super.getFlexoController();
	}

	public FreeModelDiagramEditor getDiagramEditor() {
		if (getFlexoController().getCurrentModuleView() instanceof FMEDiagramFreeModelModuleView) {
			return ((FMEDiagramFreeModelModuleView) getFlexoController().getCurrentModuleView()).getEditor();
		}
		return null;
	}

	public FreeModellingProjectNature getFreeModellingProjectNature() {
		if (getEditor() != null) {
			FlexoProject<?> project = getEditor().getProject();
			if (project != null)
				return project.getNature(FreeModellingProjectNature.class);
		}
		return null;
	}

	public String getFlexoConceptName(FlexoConcept concept) {
		if (getFreeModellingProjectNature() != null) {
			if (concept.getName().equals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME)) {
				return getLocales().localizedForKey("unclassified");
			}
			FlexoConceptInstanceRole conceptRole = (FlexoConceptInstanceRole) concept.getAccessibleRole(FMEFreeModel.CONCEPT_ROLE_NAME);
			if (conceptRole != null && conceptRole.getFlexoConceptType() != null) {
				return conceptRole.getFlexoConceptType().getName();
			}
		}
		return concept.getName();
	}

	@Override
	protected ImageIcon retrieveIconForObject(Object object) {
		if (object instanceof FlexoConcept && ((FlexoConcept) object).getName().equals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME)) {
			return FMLIconLibrary.UNKNOWN_ICON;
		}
		return super.retrieveIconForObject(object);
	}

	public void createConcept(String name) {
		// getDiagramEditor().createNewConcept(name);
	}

	public void removeConcept(FlexoConcept concept) {
		// getDiagramEditor().removeConcept(concept);
	}

	public void renameConcept(FlexoConcept concept) {
		// getDiagramEditor().renameConcept(concept);
	}

	/*public Icon getIcon(FMEModelObject object) {
		if (object instanceof Concept) {
			return FMEIconLibrary.CONCEPT_ICON;
		} else if (object instanceof Instance) {
			return FMEIconLibrary.INSTANCE_ICON;
		}
		return null;
	}*/

}
