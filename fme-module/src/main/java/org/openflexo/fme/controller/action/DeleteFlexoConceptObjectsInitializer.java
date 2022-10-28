/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fme.controller.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

public class DeleteFlexoConceptObjectsInitializer
		extends ActionInitializer<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> {
	public DeleteFlexoConceptObjectsInitializer(ControllerActionInitializer actionInitializer) {
		super(DeleteFlexoConceptObjects.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> getDefaultInitializer() {
		return (e, action) -> {
			if (action == null)
				return false;
			if (action.getEditor() == null)
				return false;
			FlexoProject<?> project = action.getEditor().getProject();
			if (project == null)
				return false;

			if (!(project.hasNature(FreeModellingProjectNature.class))) {
				return false;
			}

			if (action.getGlobalSelection().size() >= 1) {
				if (action.getFocusedObject() instanceof FlexoConcept) {
					FlexoConcept concept = (FlexoConcept) action.getFocusedObject();
					VirtualModel vm = concept.getOwner();
					FreeModellingProjectNature fmeNature = project.getNature(FreeModellingProjectNature.class);
					FMEFreeModel freeModel = fmeNature.getFreeModel(vm);
					if (freeModel == null) {
						// this concept belongs to a VirtualModel which is not a FreeModel
						// abort
						return false;
					}
					if (concept == freeModel.getNoneFlexoConcept(getEditor(), action)) {
						FlexoController.showError(getModuleLocales(action).localizedForKey("none_concept_cannot_be_deleted"));
						return false;
					}
					// Lets see if there are instances of this concept in the project
					List<FlexoConceptInstance> fciToBeDeleted = new ArrayList<>();
					for (FMEFreeModelInstance freeModelInstance : freeModel.getFreeModelInstances()) {
						fciToBeDeleted.addAll(freeModelInstance.getInstances(concept));
					}

					if (fciToBeDeleted.size() > 0) {
						if (FlexoController.confirm(getModuleLocales(action).localizedForKeyWithParams(
								"would_you_really_like_to_delete_this_concept_and_all_existing_instances_of_that_concept_(($0)_instances_found)_?",
								fciToBeDeleted.size()))) {
							for (FlexoConceptInstance fci : fciToBeDeleted) {
								fci.delete();
							}
							return true;
						}
						return false;
					}
					return FlexoController
							.confirm(getModuleLocales(action).localizedForKey("would_you_really_like_to_delete_this_concept_?"));
				}
			}

			return false;
		};
	}

	@Override
	protected FlexoActionRunnable<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> getDefaultFinalizer() {
		return (e, action) -> {
			/*if (action.getGlobalSelection().size() >= 1) {
				if (action.getFocusedObject() instanceof FlexoConcept) {
					// FlexoConcept concept = (FlexoConcept) action.getFocusedObject();
					FreeModellingProjectNature fmeNature = action.getServiceManager().getProjectNatureService()
							.getProjectNature(FreeModellingProjectNature.class);
					FreeModellingProject fmeProject = getProject().asNature(fmeNature);
					// This concept has been deleted
					for (FMEFreeModelInstance freeModel : fmeProject.getFreeModels()) {
						freeModel.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null,
								freeModel.getUsedFlexoConcepts());
					}
					return true;
				}
			}*/
			return true;
		};
	}

	@Override
	protected Icon getEnabledIcon(FlexoActionFactory<DeleteFlexoConceptObjects, FlexoConceptObject, FlexoConceptObject> actionType) {
		return IconLibrary.DELETE_ICON;
	}

	@Override
	protected KeyStroke getShortcut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	}
}
