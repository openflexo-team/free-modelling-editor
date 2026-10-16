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

import java.awt.Dimension;
import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fme.model.action.RenameFMEConcept;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class RenameFMEConceptWizard extends FlexoActionWizard<RenameFMEConcept> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RenameFMEConceptWizard.class.getPackage().getName());

	private final ConfigureRenameConcept configureRenameConcept;

	public RenameFMEConceptWizard(RenameFMEConcept action, FlexoController controller) {
		super(action, controller);
		addStep(configureRenameConcept = new ConfigureRenameConcept());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("rename_concept");
	}

	@Override
	public Dimension getExtraSize() {
		// Room for the message of the step
		return new Dimension(0, 50);
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public ConfigureRenameConcept getConfigureRenameConcept() {
		return configureRenameConcept;
	}

	/**
	 * This step is used to enter the new name of the concept
	 */
	@FIBPanel("Fib/Wizard/ConfigureRenameConcept.fib")
	public class ConfigureRenameConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public RenameFMEConcept getAction() {
			return RenameFMEConceptWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_rename_concept");
		}

		@Override
		public boolean isValid() {
			String issue = getAction().getIssue();
			if (issue != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(issue), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		public String getNewConceptName() {
			return getAction().getNewConceptName();
		}

		public void setNewConceptName(String newConceptName) {
			if (newConceptName != null && !newConceptName.equals(getNewConceptName())) {
				String oldValue = getNewConceptName();
				getAction().setNewConceptName(newConceptName);
				getPropertyChangeSupport().firePropertyChange("newConceptName", oldValue, newConceptName);
				checkValidity();
			}
		}

	}

}
