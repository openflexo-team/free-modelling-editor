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
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.model.action.CreateFMEFreeModel;
import org.openflexo.fme.model.action.CreateFMEFreeModel.ConceptualModelChoice;
import org.openflexo.fme.model.action.CreateFMEFreeModel.SampleDataChoice;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractCreateFMEFreeModelWizard<A extends CreateFMEFreeModel<A>> extends FlexoActionWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFMEFreeModelWizard.class.getPackage().getName());

	private final DescribeFreeModel describeFreeModel;

	private static final Dimension DIMENSIONS = new Dimension(750, 500);

	private final ConfigureFreeModelConceptualModel configureConceptualModel;
	private final ConfigureFreeModelSampleData configureSampleData;

	public AbstractCreateFMEFreeModelWizard(A action, FlexoController controller) {
		super(action, controller);
		addStep(describeFreeModel = makeDescribeFreeModel());
		addStep(configureConceptualModel = new ConfigureFreeModelConceptualModel());
		addStep(configureSampleData = new ConfigureFreeModelSampleData());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	public abstract DescribeFreeModel makeDescribeFreeModel();

	public DescribeFreeModel getDescribeFreeModel() {
		return describeFreeModel;
	}

	public ConfigureFreeModelConceptualModel getConfigureConceptualModel() {
		return configureConceptualModel;
	}

	public ConfigureFreeModelSampleData getConfigureSampleData() {
		return configureSampleData;
	}

	public abstract class DescribeFreeModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractCreateFMEFreeModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_free_model");
		}

		public FreeModellingProjectNature getFreeModellingProjectNature() {
			return getAction().getFocusedObject();
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getFreeModelName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_free_model_name_defined"), IssueMessageType.ERROR);
				return false;
			}

			else if (getFreeModellingProjectNature().getFreeModel(getFreeModelName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("a_free_model_with_that_name_already_exists"),
						IssueMessageType.ERROR);
				return false;
			}

			else if (StringUtils.isEmpty(getFreeModelDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_free_model"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public String getFreeModelName() {
			return getAction().getFreeModelName();
		}

		public void setFreeModelName(String newFreeModelName) {
			if (!newFreeModelName.equals(getFreeModelName())) {
				String oldValue = getFreeModelName();
				getAction().setFreeModelName(newFreeModelName);
				getPropertyChangeSupport().firePropertyChange("newFreeModelName", oldValue, newFreeModelName);
				checkValidity();
			}
		}

		public String getFreeModelDescription() {
			return getAction().getFreeModelDescription();
		}

		public void setFreeModelDescription(String newFreeModelDescription) {
			if (!newFreeModelDescription.equals(getFreeModelDescription())) {
				String oldValue = getFreeModelDescription();
				getAction().setFreeModelDescription(newFreeModelDescription);
				getPropertyChangeSupport().firePropertyChange("newFreeModelDescription", oldValue, newFreeModelDescription);
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
	@FIBPanel("Fib/Wizard/ConfigureFreeModelConceptualModel.fib")
	public class ConfigureFreeModelConceptualModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractCreateFMEFreeModelWizard.this.getAction();
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
				case CreateNewTopLevelVirtualModel:
					if (StringUtils.isEmpty(getNewConceptualModelName())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_conceptual_model_name_defined"),
								IssueMessageType.ERROR);
						return false;
					}
					break;
				case CreateContainedVirtualModel:
					if (StringUtils.isEmpty(getNewConceptualModelName())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_conceptual_model_name_defined"),
								IssueMessageType.ERROR);
						return false;
					}
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
	@FIBPanel("Fib/Wizard/ConfigureFreeModelSampleData.fib")
	public class ConfigureFreeModelSampleData extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractCreateFMEFreeModelWizard.this.getAction();
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

			if (getConfigureConceptualModel().getConceptualModelChoice() == ConceptualModelChoice.UseGeneralConceptualModel) {
				if (getSampleDataChoice() != SampleDataChoice.UseGeneralSampleData) {
					setIssueMessage(getAction().getLocales().localizedForKey(
							"you_cannot_specialize_sample_data_if_conceptual_model_is_not_specialized"), IssueMessageType.ERROR);
					return false;
				}
			}

			switch (getSampleDataChoice()) {
				case UseGeneralSampleData:
					if (getConfigureConceptualModel().getConceptualModelChoice() != ConceptualModelChoice.UseGeneralConceptualModel) {
						setIssueMessage(getAction().getLocales().localizedForKey(
								"you_cannot_use_general_sample_data_as_you_specialized_conceptual_model"), IssueMessageType.ERROR);
						return false;
					}
				case CreateNewVirtualModelInstance:
					if (StringUtils.isEmpty(getSampleDataName())) {
						setIssueMessage(getAction().getLocales().localizedForKey("no_virtual_model_instance_name_defined"),
								IssueMessageType.ERROR);
						return false;
					}
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

		public FlexoConceptInstanceType getExpectedType() {
			if (getConfigureConceptualModel().getConceptualModelChoice() == ConceptualModelChoice.SelectExistingVirtualModel) {
				return getConfigureConceptualModel().getExistingConceptualModelResource().getCompilationUnit().getVirtualModel()
						.getInstanceType();
			}
			return null;
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
