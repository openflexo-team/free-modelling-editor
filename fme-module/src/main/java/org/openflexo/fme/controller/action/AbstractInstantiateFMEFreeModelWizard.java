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
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.model.action.InstantiateFMEFreeModel;
import org.openflexo.fme.model.action.InstantiateFMEFreeModel.FreeModelChoice;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public abstract class AbstractInstantiateFMEFreeModelWizard<A extends InstantiateFMEFreeModel<A, FM>, FM extends FMEFreeModel>
		extends FlexoActionWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractInstantiateFMEFreeModelWizard.class.getPackage().getName());

	private ChooseFreeModel chooseFreeModel;
	private DescribeFreeModelInstance describeFreeModelInstance;

	private static final Dimension DIMENSIONS = new Dimension(750, 500);

	public AbstractInstantiateFMEFreeModelWizard(A action, FlexoController controller) {
		super(action, controller);
		if (!(action.getFocusedObject() instanceof FMEFreeModel)) {
			addStep(chooseFreeModel = makeChooseFreeModel());
		}
		addStep(describeFreeModelInstance = makeDescribeFreeModelInstance());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	public abstract ChooseFreeModel makeChooseFreeModel();

	public ChooseFreeModel getChooseFreeModel() {
		return chooseFreeModel;
	}

	public abstract DescribeFreeModelInstance makeDescribeFreeModelInstance();

	public DescribeFreeModelInstance getDescribeFreeModelInstance() {
		return describeFreeModelInstance;
	}

	public abstract class ChooseFreeModel extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractInstantiateFMEFreeModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_free_model_to_instantiate_or_create_new_one");
		}

		public FreeModellingProjectNature getFreeModellingProjectNature() {
			return getAction().getFocusedObject().getNature();
		}

		@Override
		public boolean isValid() {

			if (getFreeModelChoice() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_choose_an_option"), IssueMessageType.ERROR);
				return false;
			}

			switch (getFreeModelChoice()) {
				case CreateNewFreeModel:
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
					break;
				case SelectExistingFreeModel:
					if (getExistingFreeModel() == null) {
						setIssueMessage(getAction().getLocales().localizedForKey("please_select_a_free_model"), IssueMessageType.ERROR);
						return false;
					}
				default:
					break;
			}

			return true;
		}

		public FreeModelChoice getFreeModelChoice() {
			return getAction().getFreeModelChoice();
		}

		public void setFreeModelChoice(FreeModelChoice freeModelChoice) {
			if (freeModelChoice != getFreeModelChoice()) {
				FreeModelChoice oldValue = getFreeModelChoice();
				getAction().setFreeModelChoice(freeModelChoice);
				getPropertyChangeSupport().firePropertyChange("freeModelChoice", oldValue, freeModelChoice);
				checkValidity();
			}
		}

		public FM getExistingFreeModel() {
			return getAction().getExistingFreeModel();
		}

		public void setExistingFreeModel(FM existingFreeModel) {
			if ((existingFreeModel == null && getExistingFreeModel() != null)
					|| (existingFreeModel != null && !existingFreeModel.equals(getExistingFreeModel()))) {
				FM oldValue = getExistingFreeModel();
				getAction().setExistingFreeModel(existingFreeModel);
				getPropertyChangeSupport().firePropertyChange("existingFreeModel", oldValue, existingFreeModel);
				checkValidity();
			}
		}

		public String getFreeModelName() {
			if (getAction().getCreateFreeModelAction() != null) {
				return getAction().getCreateFreeModelAction().getFreeModelName();
			}
			return null;
		}

		public void setFreeModelName(String newFreeModelName) {
			if (getAction().getCreateFreeModelAction() != null) {
				if (!newFreeModelName.equals(getFreeModelName())) {
					String oldValue = getFreeModelName();
					getAction().getCreateFreeModelAction().setFreeModelName(newFreeModelName);
					getPropertyChangeSupport().firePropertyChange("newFreeModelName", oldValue, newFreeModelName);
					checkValidity();
				}
			}
		}

		public String getFreeModelDescription() {
			return getAction().getCreateFreeModelAction().getFreeModelDescription();
		}

		public void setFreeModelDescription(String newFreeModelDescription) {
			if (getAction().getCreateFreeModelAction() != null) {
				if (!newFreeModelDescription.equals(getFreeModelDescription())) {
					String oldValue = getFreeModelDescription();
					getAction().getCreateFreeModelAction().setFreeModelDescription(newFreeModelDescription);
					getPropertyChangeSupport().firePropertyChange("newFreeModelDescription", oldValue, newFreeModelDescription);
					checkValidity();
				}
			}
		}

		/*@Override
		public boolean isTransitionalStep() {
			return true;
		}
		
		@Override
		public void performTransition() {
			super.performTransition();
			if (getFreeModelChoice() == FreeModelChoice.CreateNewFreeModel) {
			}
			else if (getFreeModelChoice() == FreeModelChoice.SelectExistingFreeModel) {
				addStep(useExistingDiagramSpecification = new UseExistingDiagramSpecification());
			}
		}
		
		@Override
		public void discardTransition() {
			super.discardTransition();
		
			if (getChoice() == DiagramSpecificationChoice.CreateNewDiagramSpecification) {
				removeStep(createNewDiagramSpecification);
			}
			else if (getChoice() == DiagramSpecificationChoice.UseExistingDiagramSpecification) {
				removeStep(useExistingDiagramSpecification);
			}
		}*/

	}

	public abstract class DescribeFreeModelInstance extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public A getAction() {
			return AbstractInstantiateFMEFreeModelWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_free_model_instance_beeing_created");
		}

		public FreeModellingProjectNature getFreeModellingProjectNature() {
			return getAction().getFocusedObject().getNature();
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getFreeModelInstanceName())) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_free_model_name_defined"), IssueMessageType.ERROR);
				return false;
			}

			/*if (getAction().getFreeModel() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("no_free_model_defined"), IssueMessageType.ERROR);
				return false;
			}*/

			if (getAction().getFreeModel() != null && getAction().getFreeModel().getFreeModelInstance(getFreeModelInstanceName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey("a_free_model_instance_with_that_name_already_exists"),
						IssueMessageType.ERROR);
				return false;
			}

			if (StringUtils.isEmpty(getFreeModelInstanceDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_free_model_instance"),
						IssueMessageType.WARNING);
			}

			return true;
		}

		public String getFreeModelInstanceName() {
			return getAction().getFreeModelInstanceName();
		}

		public void setFreeModelInstanceName(String newFreeModelInstanceName) {
			if (!newFreeModelInstanceName.equals(getFreeModelInstanceName())) {
				String oldValue = getFreeModelInstanceName();
				getAction().setFreeModelInstanceName(newFreeModelInstanceName);
				getPropertyChangeSupport().firePropertyChange("newFreeModelInstanceName", oldValue, newFreeModelInstanceName);
				checkValidity();
			}

		}

		public String getFreeModelInstanceDescription() {
			return getAction().getFreeModelInstanceDescription();
		}

		public void setFreeModelInstanceDescription(String newFreeModelInstanceDescription) {
			if (!newFreeModelInstanceDescription.equals(getFreeModelInstanceDescription())) {
				String oldValue = getFreeModelInstanceDescription();
				getAction().setFreeModelInstanceDescription(newFreeModelInstanceDescription);
				getPropertyChangeSupport().firePropertyChange("newFreeModelInstanceDescription", oldValue, newFreeModelInstanceDescription);
				checkValidity();
			}
		}

	}

}
