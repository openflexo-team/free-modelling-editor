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

import java.awt.Image;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.CreateNewConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateNewConceptWizard extends FlexoActionWizard<CreateNewConcept> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateNewConceptWizard.class.getPackage().getName());

	private final ConfigureNewConcept configureNewConcept;

	public CreateNewConceptWizard(CreateNewConcept action, FlexoController controller) {
		super(action, controller);
		addStep(configureNewConcept = new ConfigureNewConcept());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_new_concept");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public ConfigureNewConcept getConfigureNewConcept() {
		return configureNewConcept;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ConfigureNewConcept.fib")
	public class ConfigureNewConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateNewConcept getAction() {
			return CreateNewConceptWizard.this.getAction();
		}

		public FMEFreeModel getFreeModel() {
			return getAction().getFocusedObject();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_new_concept");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getNewConceptName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_concept_name_defined"), IssueMessageType.ERROR);
				return false;
			}

			if (getFreeModel().getAccessedVirtualModel().getFlexoConcept(getNewConceptName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("a_concept_with_that_name_already_exists"),
						IssueMessageType.ERROR);
				return false;
			}

			return true;

		}

		public String getNewConceptName() {
			return getAction().getNewConceptName();
		}

		public void setNewConceptName(String newConceptName) {
			if (!newConceptName.equals(getNewConceptName())) {
				String oldValue = getNewConceptName();
				getAction().setNewConceptName(newConceptName);
				getPropertyChangeSupport().firePropertyChange("newConceptName", oldValue, newConceptName);
				checkValidity();
			}
		}

		public String getNewConceptDescription() {
			return getAction().getNewConceptDescription();
		}

		public void setNewConceptDescription(String newConceptDescription) {
			if (!newConceptDescription.equals(getNewConceptDescription())) {
				String oldValue = getNewConceptDescription();
				getAction().setNewConceptDescription(newConceptDescription);
				getPropertyChangeSupport().firePropertyChange("newConceptDescription", oldValue, newConceptDescription);
				checkValidity();
			}
		}

	}

}
