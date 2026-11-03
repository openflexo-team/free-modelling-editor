/**
 * 
 * Copyright (c) 2026, Openflexo
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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.AbstractDeclareInstanceOfExistingConcept;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.rm.Resource;
import org.openflexo.view.controller.FlexoController;

/**
 * Wizard of the actions declaring an element as an instance of an existing concept ({@link AbstractDeclareInstanceOfExistingConcept}),
 * whatever the technology: the concept, a new instance (its name, its container) or an existing one.<br>
 * A technology which has more to choose (how the representation of the element and the one of its concept relate, for a diagram) gives
 * the component showing it, which is shown below, with the action as data
 * 
 * @author sylvain
 *
 */
public class DeclareInstanceOfExistingConceptWizard extends FlexoActionWizard<AbstractDeclareInstanceOfExistingConcept<?, ?, ?, ?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeclareInstanceOfExistingConceptWizard.class.getPackage().getName());

	private static final Dimension DIMENSIONS = new Dimension(600, 400);

	private final ConfigureInstanceOfExistingConcept configureInstanceOfExistingConcept;
	private final Resource representationOptions;

	/**
	 * @param action
	 *            the action to configure
	 * @param representationOptions
	 *            the component showing what the technology has more to choose, its data being the action, or null
	 * @param controller
	 */
	public DeclareInstanceOfExistingConceptWizard(AbstractDeclareInstanceOfExistingConcept<?, ?, ?, ?> action, Resource representationOptions,
			FlexoController controller) {
		super(action, controller);
		this.representationOptions = representationOptions;
		addStep(configureInstanceOfExistingConcept = new ConfigureInstanceOfExistingConcept());
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

	public ConfigureInstanceOfExistingConcept getConfigureInstanceOfExistingConcept() {
		return configureInstanceOfExistingConcept;
	}

	/**
	 * This step chooses the concept, and whether the element stands for a new instance of it or for an existing one
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ConfigureInstanceOfExistingConcept.fib")
	public class ConfigureInstanceOfExistingConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AbstractDeclareInstanceOfExistingConcept<?, ?, ?, ?> getAction() {
			return DeclareInstanceOfExistingConceptWizard.this.getAction();
		}

		public FMEFreeModel getFreeModel() {
			return getAction().getFMEFreeModel();
		}

		/**
		 * The component showing what the technology has more to choose, its data being the action, or null
		 */
		public Resource getRepresentationOptions() {
			return representationOptions;
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_new_instance");
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

		public FlexoConcept getConcept() {
			return getAction().getConcept();
		}

		public void setConcept(FlexoConcept concept) {
			if (concept != getConcept()) {
				FlexoConcept oldValue = getConcept();
				getAction().setConcept(concept);
				getPropertyChangeSupport().firePropertyChange("concept", oldValue, concept);
				getPropertyChangeSupport().firePropertyChange("expectedContainerType", null, getExpectedContainerType());
				getPropertyChangeSupport().firePropertyChange("availableInstances", null, getAvailableInstances());
				getPropertyChangeSupport().firePropertyChange("existingInstance", null, getExistingInstance());
				checkValidity();
			}
		}

		/**
		 * Whether the element stands for an instance of the concept which already exists in the sample data
		 */
		public boolean getRepresentExistingInstance() {
			return !getAction().isCreateNewInstance();
		}

		public void setRepresentExistingInstance(boolean representExistingInstance) {
			if (representExistingInstance != getRepresentExistingInstance()) {
				getAction().setCreateNewInstance(!representExistingInstance);
				getPropertyChangeSupport().firePropertyChange("representExistingInstance", !representExistingInstance,
						representExistingInstance);
				checkValidity();
			}
		}

		public List<FlexoConceptInstance> getAvailableInstances() {
			return getAction().getAvailableInstances();
		}

		public FlexoConceptInstance getExistingInstance() {
			return getAction().getExistingInstance();
		}

		public void setExistingInstance(FlexoConceptInstance existingInstance) {
			if (existingInstance != getExistingInstance()) {
				FlexoConceptInstance oldValue = getExistingInstance();
				getAction().setExistingInstance(existingInstance);
				getPropertyChangeSupport().firePropertyChange("existingInstance", oldValue, existingInstance);
				checkValidity();
			}
		}

		public String getNewInstanceName() {
			return getAction().getNewInstanceName();
		}

		public void setNewInstanceName(String newInstanceName) {
			if ((newInstanceName == null && getNewInstanceName() != null)
					|| (newInstanceName != null && !newInstanceName.equals(getNewInstanceName()))) {
				String oldValue = getNewInstanceName();
				getAction().setNewInstanceName(newInstanceName);
				getPropertyChangeSupport().firePropertyChange("newInstanceName", oldValue, newInstanceName);
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
			if (getConcept() != null && getConcept().getContainerFlexoConcept() != null) {
				return getConcept().getContainerFlexoConcept().getInstanceType();
			}
			return null;
		}

	}

}
