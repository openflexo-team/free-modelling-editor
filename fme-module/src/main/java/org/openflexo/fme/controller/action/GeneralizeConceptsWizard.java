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
import org.openflexo.fme.model.action.GeneralizeConcepts;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class GeneralizeConceptsWizard extends FlexoActionWizard<GeneralizeConcepts> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GeneralizeConceptsWizard.class.getPackage().getName());

	private final ConfigureGeneralizeConcepts configureGeneralizeConcepts;
	private final ChooseFactoredProperties chooseFactoredProperties;

	public GeneralizeConceptsWizard(GeneralizeConcepts action, FlexoController controller) {
		super(action, controller);
		addStep(configureGeneralizeConcepts = new ConfigureGeneralizeConcepts());
		addStep(chooseFactoredProperties = new ChooseFactoredProperties());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("generalize_concepts");
	}

	@Override
	public Dimension getExtraSize() {
		// Room for the message of the step, and for the table of properties
		return new Dimension(0, 150);
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public ConfigureGeneralizeConcepts getConfigureGeneralizeConcepts() {
		return configureGeneralizeConcepts;
	}

	public ChooseFactoredProperties getChooseFactoredProperties() {
		return chooseFactoredProperties;
	}

	/**
	 * This step is used to name the super concept and to tell whether it is abstract
	 */
	@FIBPanel("Fib/Wizard/ConfigureGeneralizeConcepts.fib")
	public class ConfigureGeneralizeConcepts extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public GeneralizeConcepts getAction() {
			return GeneralizeConceptsWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_generalize_concepts");
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

		public String getConceptsSummary() {
			return names(getAction().getConcepts());
		}

		public String getParentsSummary() {
			if (getAction().getCommonParents().isEmpty()) {
				return getAction().getLocales().localizedForKey("no_common_parent");
			}
			return names(getAction().getCommonParents());
		}

		private String names(Iterable<FlexoConcept> concepts) {
			StringBuilder returned = new StringBuilder();
			for (FlexoConcept concept : concepts) {
				if (returned.length() > 0) {
					returned.append(", ");
				}
				returned.append(concept.getName());
			}
			return returned.toString();
		}

		public boolean getNewConceptAbstract() {
			return getAction().isNewConceptAbstract();
		}

		public void setNewConceptAbstract(boolean newConceptAbstract) {
			if (newConceptAbstract != getNewConceptAbstract()) {
				getAction().setNewConceptAbstract(newConceptAbstract);
				getPropertyChangeSupport().firePropertyChange("newConceptAbstract", !newConceptAbstract, newConceptAbstract);
				checkValidity();
			}
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

		public String getNewConceptDescription() {
			return getAction().getNewConceptDescription();
		}

		public void setNewConceptDescription(String newConceptDescription) {
			if (newConceptDescription != null && !newConceptDescription.equals(getNewConceptDescription())) {
				String oldValue = getNewConceptDescription();
				getAction().setNewConceptDescription(newConceptDescription);
				getPropertyChangeSupport().firePropertyChange("newConceptDescription", oldValue, newConceptDescription);
				checkValidity();
			}
		}

	}

	/**
	 * This step is used to choose the properties to move to the super concept
	 */
	@FIBPanel("Fib/Wizard/ChooseFactoredProperties.fib")
	public class ChooseFactoredProperties extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public GeneralizeConcepts getAction() {
			return GeneralizeConceptsWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("choose_factored_properties");
		}

		/** Whether the concepts to generalize are relationships, which may share their ends */
		public boolean getEndsVisible() {
			return getAction().isRelationshipsSelection();
		}

		/** Whether they do: the ends are then proposed, otherwise the reason why they are not */
		public boolean getEndsPossible() {
			return getAction().getCommonEnds() != null;
		}

		public boolean getGeneralizeEnds() {
			return getAction().isGeneralizeEnds();
		}

		public void setGeneralizeEnds(boolean generalizeEnds) {
			if (generalizeEnds != getGeneralizeEnds()) {
				getAction().setGeneralizeEnds(generalizeEnds);
				getPropertyChangeSupport().firePropertyChange("generalizeEnds", !generalizeEnds, generalizeEnds);
				checkValidity();
			}
		}

		public String getEndsSummary() {
			if (getAction().getCommonEnds() != null) {
				return getAction().getCommonEnds().toString();
			}
			String issue = getAction().getEndsIssue();
			return issue != null ? getAction().getLocales().localizedForKey(issue) : null;
		}

		@Override
		public boolean isValid() {
			String issue = getAction().getPropertiesIssue();
			if (issue != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(issue), IssueMessageType.ERROR);
				return false;
			}
			return true;
		}

		/** The properties the concepts have in common by name, which cannot be moved, or null when there is none */
		public String getNotFactoredSummary() {
			if (getAction().getNotFactoredProperties().isEmpty()) {
				return null;
			}
			return String.join(", ", getAction().getNotFactoredProperties());
		}

	}

}
