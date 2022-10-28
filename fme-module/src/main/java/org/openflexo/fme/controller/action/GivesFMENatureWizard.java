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
import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.model.action.GivesFMENature;
import org.openflexo.fme.model.action.GivesFMENature.ConceptualModelChoice;
import org.openflexo.fme.model.action.GivesFMENature.SampleDataChoice;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class GivesFMENatureWizard extends FlexoActionWizard<GivesFMENature> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GivesFMENatureWizard.class.getPackage().getName());

	private final ConfigureConceptualModel configureConceptualModel;
	private final ConfigureSampleData configureSampleData;

	private static final Dimension DIMENSIONS = new Dimension(750, 500);

	public GivesFMENatureWizard(GivesFMENature action, FlexoController controller) {
		super(action, controller);
		addStep(configureConceptualModel = new ConfigureConceptualModel());
		addStep(configureSampleData = new ConfigureSampleData());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("gives_project_free_modelling_nature");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMEIconLibrary.FREE_MODEL_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	public ConfigureConceptualModel getConfigureConceptualModel() {
		return configureConceptualModel;
	}

	public ConfigureSampleData getConfigureSampleData() {
		return configureSampleData;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ConfigureConceptualModel.fib")
	public class ConfigureConceptualModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public GivesFMENature getAction() {
			return GivesFMENatureWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_conceptual_model");
		}

		@Override
		public boolean isValid() {

			if (getConceptualModelChoice() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_chose_an_option"), IssueMessageType.ERROR);
				return false;
			}

			switch (getConceptualModelChoice()) {
				case CreateNewVirtualModel:
					if (StringUtils.isEmpty(getNewConceptualModelName())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_conceptual_model_name_defined"),
								IssueMessageType.ERROR);
						return false;
					}
					/*if (StringUtils.isEmpty(getNewConceptualModelRelativePath())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_relative_path_defined"), IssueMessageType.ERROR);
						return false;
					}*/
					break;
				case SelectExistingVirtualModel:
					if (getExistingConceptualModelResource() == null) {
						setIssueMessage(getAction().getLocales().localizedForKey("please_select_a_virtual_model"), IssueMessageType.ERROR);
						return false;
					}
					break;
			}

			return true;

		}

		public ConceptualModelChoice getConceptualModelChoice() {
			return getAction().getConceptualModelChoice();
		}

		public void setConceptualModelChoice(ConceptualModelChoice conceptualModelChoice) {
			if (conceptualModelChoice != getConceptualModelChoice()) {
				ConceptualModelChoice oldValue = getConceptualModelChoice();
				getAction().setConceptualModelChoice(conceptualModelChoice);
				getPropertyChangeSupport().firePropertyChange("conceptualModelChoice", oldValue, conceptualModelChoice);
				checkValidity();
			}
		}

		public CompilationUnitResource getExistingConceptualModelResource() {
			return getAction().getExistingConceptualModelResource();
		}

		public void setExistingConceptualModelResource(CompilationUnitResource existingConceptualModelResource) {
			if (existingConceptualModelResource != getExistingConceptualModelResource()) {
				CompilationUnitResource oldValue = getExistingConceptualModelResource();
				getAction().setExistingConceptualModelResource(existingConceptualModelResource);
				getPropertyChangeSupport().firePropertyChange("existingConceptualModelResource", oldValue, existingConceptualModelResource);
				checkValidity();
			}
		}

		public String getNewConceptualModelName() {
			return getAction().getNewConceptualModelName();
		}

		public void setNewConceptualModelName(String newConceptualModelName) {
			if ((newConceptualModelName == null && getNewConceptualModelName() != null)
					|| (newConceptualModelName != null && !newConceptualModelName.equals(getNewConceptualModelName()))) {
				String oldValue = getNewConceptualModelName();
				getAction().setNewConceptualModelName(newConceptualModelName);
				getPropertyChangeSupport().firePropertyChange("newConceptualModelName", oldValue, newConceptualModelName);
				checkValidity();
			}
		}

		public String getNewConceptualModelRelativePath() {
			return getAction().getNewConceptualModelRelativePath();
		}

		public void setNewConceptualModelRelativePath(String newConceptualModelRelativePath) {
			if ((newConceptualModelRelativePath == null && getNewConceptualModelRelativePath() != null)
					|| (newConceptualModelRelativePath != null
							&& !newConceptualModelRelativePath.equals(getNewConceptualModelRelativePath()))) {
				String oldValue = getNewConceptualModelRelativePath();
				getAction().setNewConceptualModelRelativePath(newConceptualModelRelativePath);
				getPropertyChangeSupport().firePropertyChange("newConceptualModelRelativePath", oldValue, newConceptualModelRelativePath);
				checkValidity();
			}
		}

	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ConfigureSampleData.fib")
	public class ConfigureSampleData extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public GivesFMENature getAction() {
			return GivesFMENatureWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_sample_data");
		}

		@Override
		public boolean isValid() {

			if (getSampleDataChoice() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_chose_an_option"), IssueMessageType.ERROR);
				return false;
			}

			switch (getSampleDataChoice()) {
				case CreateNewVirtualModelInstance:
					if (StringUtils.isEmpty(getSampleDataName())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_virtual_model_instance_name_defined"),
								IssueMessageType.ERROR);
						return false;
					}
					/*if (StringUtils.isEmpty(getNewConceptualModelRelativePath())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_relative_path_defined"), IssueMessageType.ERROR);
						return false;
					}*/
					break;
				case SelectExistingVirtualModelInstance:
					if (getExistingSampleDataResource() == null) {
						setIssueMessage(getAction().getLocales().localizedForKey("please_select_a_virtual_model_instance"),
								IssueMessageType.ERROR);
						return false;
					}
					break;
			}

			return true;

		}

		public SampleDataChoice getSampleDataChoice() {
			return getAction().getSampleDataChoice();
		}

		public void setSampleDataChoice(SampleDataChoice sampleDataChoice) {
			if (sampleDataChoice != getSampleDataChoice()) {
				SampleDataChoice oldValue = getSampleDataChoice();
				getAction().setSampleDataChoice(sampleDataChoice);
				getPropertyChangeSupport().firePropertyChange("sampleDataChoice", oldValue, sampleDataChoice);
				checkValidity();
			}
		}

		public FMLRTVirtualModelInstanceResource getExistingSampleDataResource() {
			return getAction().getExistingSampleDataResource();
		}

		public void setExistingSampleDataResource(FMLRTVirtualModelInstanceResource existingSampleDataResource) {
			if (existingSampleDataResource != getExistingSampleDataResource()) {
				FMLRTVirtualModelInstanceResource oldValue = getExistingSampleDataResource();
				getAction().setExistingSampleDataResource(existingSampleDataResource);
				getPropertyChangeSupport().firePropertyChange("existingSampleDataResource", oldValue, existingSampleDataResource);
				checkValidity();
			}
		}

		public String getSampleDataName() {
			return getAction().getSampleDataName();
		}

		public void setSampleDataName(String sampleDataName) {
			if ((sampleDataName == null && getSampleDataName() != null)
					|| (sampleDataName != null && !sampleDataName.equals(getSampleDataName()))) {
				String oldValue = getSampleDataName();
				getAction().setSampleDataName(sampleDataName);
				getPropertyChangeSupport().firePropertyChange("sampleDataName", oldValue, sampleDataName);
				checkValidity();
			}
		}

		public String getSampleDataRelativePath() {
			return getAction().getSampleDataRelativePath();
		}

		public void setSampleDataRelativePath(String sampleDataRelativePath) {
			if ((sampleDataRelativePath == null && getSampleDataRelativePath() != null)
					|| (sampleDataRelativePath != null && !sampleDataRelativePath.equals(getSampleDataRelativePath()))) {
				String oldValue = getSampleDataRelativePath();
				getAction().setSampleDataRelativePath(sampleDataRelativePath);
				getPropertyChangeSupport().firePropertyChange("sampleDataRelativePath", oldValue, sampleDataRelativePath);
				checkValidity();
			}
		}

	}

}
