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
import org.openflexo.fme.model.action.DeclareInstanceOfExistingConceptFromDiagramElement;
import org.openflexo.fme.model.action.DeclareInstanceOfExistingConceptFromDiagramElement.GRStrategy;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.controller.FlexoController;

public class DeclareInstanceOfExistingConceptFromDiagramElementWizard
		extends FlexoActionWizard<DeclareInstanceOfExistingConceptFromDiagramElement> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(DeclareInstanceOfExistingConceptFromDiagramElementWizard.class.getPackage().getName());

	private final ConfigureNewInstanceOfExistingConceptFromDiagramElement configureNewInstanceOfExistingConcept;

	private static final Dimension DIMENSIONS = new Dimension(600, 400);

	public DeclareInstanceOfExistingConceptFromDiagramElementWizard(DeclareInstanceOfExistingConceptFromDiagramElement action,
			FlexoController controller) {
		super(action, controller);
		addStep(configureNewInstanceOfExistingConcept = new ConfigureNewInstanceOfExistingConceptFromDiagramElement());
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("declare_instance_of_existing_concept");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public ConfigureNewInstanceOfExistingConceptFromDiagramElement getConfigureNewConceptFromNoneConcept() {
		return configureNewInstanceOfExistingConcept;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ConfigureNewInstanceOfExistingConceptFromDiagramElement.fib")
	public class ConfigureNewInstanceOfExistingConceptFromDiagramElement extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public DeclareInstanceOfExistingConceptFromDiagramElement getAction() {
			return DeclareInstanceOfExistingConceptFromDiagramElementWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_new_instance");
		}

		@Override
		public boolean isValid() {

			if (getConcept() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_choose_a_concept"), IssueMessageType.ERROR);
				return false;
			}

			if (getConcept().getContainerFlexoConcept() != null) {
				if (getContainer() == null) {
					setIssueMessage(getAction().getLocales().localizedForKey("please_select_a_container_for_new_concept_instance"),
							IssueMessageType.ERROR);
					return false;
				}
				if (!getConcept().getContainerFlexoConcept().isAssignableFrom(getContainer().getFlexoConcept())) {
					setIssueMessage(getAction().getLocales().localizedForKey("container_is_not_of_right_type"), IssueMessageType.ERROR);
					return false;
				}
			}

			if (getGrStrategy() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_choose_a_strategy_for_graphical_representation"),
						IssueMessageType.ERROR);
				return false;
			}

			return true;

		}

		public FlexoConcept getConcept() {
			return getAction().getConcept();
		}

		public void setConcept(FlexoConcept concept) {
			if (concept != getConcept()) {
				FlexoConcept oldValue = getConcept();
				getAction().setConcept(concept);
				getPropertyChangeSupport().firePropertyChange("concept", oldValue, concept);
				getPropertyChangeSupport().firePropertyChange("expectedContainerType", null, getExpectedContainerType());
				checkValidity();
			}
		}

		public GRStrategy getGrStrategy() {
			return getAction().getGrStrategy();
		}

		public void setGrStrategy(GRStrategy strategy) {
			if (strategy != getGrStrategy()) {
				GRStrategy oldValue = getGrStrategy();
				getAction().setGrStrategy(strategy);
				getPropertyChangeSupport().firePropertyChange("grStrategy", oldValue, strategy);
				checkValidity();
			}
		}

		public FlexoConceptInstance getContainer() {
			return getAction().getContainer();
		}

		public void setContainer(FlexoConceptInstance container) {
			if ((container == null && getContainer() != null) || (container != null && !container.equals(getContainer()))) {
				FlexoConceptInstance oldValue = getContainer();
				getAction().setContainer(container);
				getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
				checkValidity();
			}
		}

		public FlexoConceptInstanceType getExpectedContainerType() {
			return getConcept().getContainerFlexoConcept().getInstanceType();
		}

	}

}
