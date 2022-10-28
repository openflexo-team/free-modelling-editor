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

package org.openflexo.fme.controller.action;

import java.util.logging.Logger;

import org.openflexo.components.wizard.Wizard;
import org.openflexo.components.wizard.WizardDialog;
import org.openflexo.fme.model.action.InstantiateNewFMEProperty;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;

public class InstantiateNewFMEPropertyInitializer extends ActionInitializer<InstantiateNewFMEProperty, FlexoConceptInstance, FlexoObject> {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public InstantiateNewFMEPropertyInitializer(ControllerActionInitializer actionInitializer) {
		super(InstantiateNewFMEProperty.actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<InstantiateNewFMEProperty, FlexoConceptInstance, FlexoObject> getDefaultInitializer() {
		return (e, action) -> {
			Wizard wizard = new InstantiateNewFMEPropertyWizard(action, getController());
			WizardDialog dialog = new WizardDialog(wizard, getController());
			dialog.showDialog();
			if (dialog.getStatus() != Status.VALIDATED) {
				// Operation cancelled
				return false;
			}
			return true;
			// return instanciateAndShowDialog(action, FMECst.CREATE_NEW_CONCEPT_DIALOG_FIB);
		};
	}

	@Override
	protected FlexoActionRunnable<InstantiateNewFMEProperty, FlexoConceptInstance, FlexoObject> getDefaultFinalizer() {
		return (e, action) -> {
			logger.info("CreateNewConcept finalizer");
			getController().selectAndFocusObject(action.getFocusedObject());
			return true;
		};
	}
}
