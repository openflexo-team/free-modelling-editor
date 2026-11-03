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
package org.openflexo.fme.model.action;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;

/**
 * Declares an element of a free model instance to be an instance of an existing concept of the conceptual model<br>
 * 
 * The focused object is either an instance of a GR concept standing for the element (the NoneGR of a diagram shape for instance), or the
 * element itself when nothing stands for it yet (a PowerPoint shape, a diagram element not yet in the free model instance).<br>
 * 
 * The free modelling part is the same for every technology: the conceptual instance is either a new instance of the concept, created in
 * the sample data, or an instance of the concept which already exists there (it may already be represented elsewhere, in another free
 * model instance for example); the GR concept of the concept is retrieved (or created), and the instance standing for the element becomes
 * an instance of that GR concept, pointing to the conceptual instance.<br>
 * What happens to the representation element itself (a shape, its graphical representation, a palette...) is the part of the technology,
 * given by {@link #retrieveRepresentationInstance(FlexoConcept)}, {@link #getDefaultInstanceName()} and the hooks
 * {@link #prepareRepresentation(FlexoConceptInstance, FlexoConcept, boolean)},
 * {@link #completeRepresentation(FlexoConceptInstance, FlexoConcept, boolean)} and {@link #refreshRepresentation(FlexoConceptInstance)}
 * 
 * @param <A>
 *            type of the action
 * @param <T>
 *            type of the focused object
 * @param <FM>
 *            type of the free model
 * @param <FMI>
 *            type of the free model instance
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractDeclareInstanceOfExistingConcept<A extends AbstractDeclareInstanceOfExistingConcept<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractFreeModelInstanceAction<A, T, FM, FMI> {

	private static final Logger logger = Logger.getLogger(AbstractDeclareInstanceOfExistingConcept.class.getPackage().getName());

	private FlexoConcept concept;
	private FlexoConceptInstance container;
	private boolean createNewInstance = true;
	private FlexoConceptInstance existingInstance;
	private String newInstanceName;

	private FlexoConceptInstance conceptInstance;
	private FlexoConceptInstance grInstance;

	protected AbstractDeclareInstanceOfExistingConcept(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * Return concept (at conceptual level): the first concept of the conceptual model until one is chosen
	 * 
	 * @return
	 */
	public FlexoConcept getConcept() {
		if (concept == null && getFMEFreeModel() != null
				&& getFMEFreeModel().getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().size() > 0) {
			return getFMEFreeModel().getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().get(0);
		}
		return concept;
	}

	public void setConcept(FlexoConcept concept) {
		boolean wasValid = isValid();
		this.concept = concept;
		if (existingInstance != null && (concept == null || !concept.isAssignableFrom(existingInstance.getFlexoConcept()))) {
			setExistingInstance(null);
		}
		getPropertyChangeSupport().firePropertyChange("concept", null, concept);
		getPropertyChangeSupport().firePropertyChange("hasGRConcept", !hasGRConcept(), hasGRConcept());
		getPropertyChangeSupport().firePropertyChange("availableInstances", null, getAvailableInstances());
		conceptChanged();
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	/**
	 * Called when the concept was changed, for what depends on it in the technology
	 */
	protected void conceptChanged() {
	}

	public boolean hasGRConcept() {
		return getGRConcept(false) != null;
	}

	public FlexoConcept getGRConcept(boolean createWhenUnexistant) {
		FlexoConceptInstance containerGRInstance = getContainerGRInstance();
		return getFMEFreeModel().getGRFlexoConcept(getConcept(), containerGRInstance != null ? containerGRInstance.getFlexoConcept() : null,
				getEditor(), this, createWhenUnexistant);
	}

	/**
	 * The instance of a GR concept standing for the element containing the focused one, if any (a shape containing a shape): it gives the
	 * default container of the new conceptual instance, and the GR concept of the concept, when it has to be created, is dropped into its
	 * GR concept
	 */
	protected FlexoConceptInstance getContainerGRInstance() {
		return null;
	}

	/**
	 * The instance of the sample data containing the new conceptual instance (when the concept has a container concept): by default the
	 * one the element containing the focused one stands for
	 */
	public FlexoConceptInstance getContainer() {
		if (container == null) {
			return FMEFreeModel.representedInstance(getContainerGRInstance());
		}
		return container;
	}

	public void setContainer(FlexoConceptInstance container) {
		if ((container == null && this.container != null) || (container != null && !container.equals(this.container))) {
			boolean wasValid = isValid();
			FlexoConceptInstance oldValue = getContainer();
			this.container = container;
			getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
			getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
		}
	}

	/**
	 * Whether the element stands for a new instance of the concept (the default), or for an instance of the sample data which already
	 * exists ({@link #getExistingInstance()})
	 */
	public boolean isCreateNewInstance() {
		return createNewInstance;
	}

	public void setCreateNewInstance(boolean createNewInstance) {
		if (createNewInstance != this.createNewInstance) {
			boolean wasValid = isValid();
			this.createNewInstance = createNewInstance;
			getPropertyChangeSupport().firePropertyChange("createNewInstance", !createNewInstance, createNewInstance);
			getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
		}
	}

	/**
	 * The instance of the sample data the element stands for, when no new instance is created
	 */
	public FlexoConceptInstance getExistingInstance() {
		return existingInstance;
	}

	public void setExistingInstance(FlexoConceptInstance existingInstance) {
		if (existingInstance != this.existingInstance) {
			boolean wasValid = isValid();
			FlexoConceptInstance oldValue = this.existingInstance;
			this.existingInstance = existingInstance;
			getPropertyChangeSupport().firePropertyChange("existingInstance", oldValue, existingInstance);
			getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
		}
	}

	/**
	 * The instances of the concept in the sample data, which the element may stand for instead of a new one
	 */
	public List<FlexoConceptInstance> getAvailableInstances() {
		if (getConcept() == null || getFMEFreeModel() == null || getFMEFreeModel().getSampleData() == null
				|| getFMEFreeModel().getSampleData().getAccessedVirtualModelInstance() == null) {
			return Collections.emptyList();
		}
		return getFMEFreeModel().getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances(getConcept());
	}

	/**
	 * The name of the new conceptual instance, the first parameter of the creation scheme of the concept: the one given by the
	 * representation element ({@link #getDefaultInstanceName()}) unless set
	 */
	public String getNewInstanceName() {
		if (newInstanceName == null) {
			return getDefaultInstanceName();
		}
		return newInstanceName;
	}

	public void setNewInstanceName(String newInstanceName) {
		String oldValue = getNewInstanceName();
		this.newInstanceName = newInstanceName;
		getPropertyChangeSupport().firePropertyChange("newInstanceName", oldValue, newInstanceName);
	}

	/**
	 * The name the representation element gives to a new conceptual instance (the label of a shape, the text of a PowerPoint shape...)
	 */
	protected abstract String getDefaultInstanceName();

	/**
	 * The conceptual instance the element stands for, once the action is done
	 */
	public FlexoConceptInstance getConceptInstance() {
		return conceptInstance;
	}

	/**
	 * The instance of the GR concept standing for the element, once the action is done
	 */
	public FlexoConceptInstance getGRInstance() {
		return grInstance;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		logger.info("Declare " + getFocusedObject() + " as instance of existing concept " + getConcept());

		FMI freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException(getFocusedObject() + " does not belong to any FreeModel");
		}
		if (getIssue() != null) {
			throw new InvalidArgumentException("Cannot declare " + getFocusedObject() + ": " + getIssue());
		}

		conceptInstance = isCreateNewInstance() ? instantiateConcept(getNewInstanceName()) : getExistingInstance();

		boolean grConceptWasExisting = hasGRConcept();

		// Create or retrieve the GR concept
		FlexoConcept grConcept = getGRConcept(true);

		grInstance = retrieveRepresentationInstance(grConcept);

		prepareRepresentation(grInstance, grConcept, grConceptWasExisting);

		// We will here bypass the classical DropScheme
		grInstance.setFlexoConcept(grConcept);
		grInstance.setFlexoPropertyValue(FMEFreeModel.conceptRoleName(grConcept), conceptInstance);

		completeRepresentation(grInstance, grConcept, grConceptWasExisting);

		// We should notify the creation of a new FlexoConcept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, getConcept());
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, getConcept());
		// This is used to notify the adding of a new instance of a flexo concept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, grInstance);

		refreshRepresentation(grInstance);
	}

	/**
	 * Instantiates the concept in the sample data, in the container when one was given
	 * 
	 * @param name
	 *            name of the new conceptual instance, the first parameter of the creation scheme of the concept
	 * @return the new conceptual instance
	 */
	protected FlexoConceptInstance instantiateConcept(String name) {
		CreateFlexoConceptInstance instantiateConcept = CreateFlexoConceptInstance.actionType
				.makeNewEmbeddedAction(getFMEFreeModel().getSampleData().getAccessedVirtualModelInstance(), null, this);
		instantiateConcept.setFlexoConcept(getConcept());
		instantiateConcept.setContainer(getContainer());
		CreationScheme cs = getConcept().getCreationSchemes().get(0);
		instantiateConcept.setCreationScheme(cs);
		instantiateConcept.setParameterValue(cs.getParameters().get(0), name);
		instantiateConcept.doAction();
		return instantiateConcept.getNewFlexoConceptInstance();
	}

	/**
	 * The instance standing for the element: the focused instance itself, or an instance created for the focused element.<br>
	 * It may still be an instance of another GR concept (the NoneGR), or already be an instance of supplied GR concept
	 * 
	 * @param grConcept
	 *            the GR concept of the concept
	 */
	protected abstract FlexoConceptInstance retrieveRepresentationInstance(FlexoConcept grConcept) throws FlexoException;

	/**
	 * Called once the GR concept is known, before the instance standing for the element is made an instance of it
	 * 
	 * @param grInstance
	 *            the instance standing for the element
	 * @param grConcept
	 *            the GR concept of the concept
	 * @param grConceptWasExisting
	 *            whether the GR concept existed before this action (it was created otherwise)
	 */
	protected abstract void prepareRepresentation(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting)
			throws FlexoException;

	/**
	 * Called once the instance standing for the element is an instance of the GR concept, pointing to the conceptual instance
	 * 
	 * @param grInstance
	 *            the instance standing for the element
	 * @param grConcept
	 *            the GR concept of the concept
	 * @param grConceptWasExisting
	 *            whether the GR concept existed before this action (it was created otherwise)
	 */
	protected abstract void completeRepresentation(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting)
			throws FlexoException;

	/**
	 * Called last, once the free model instance has notified the new instance: brings the view of the representation up to date
	 * 
	 * @param grInstance
	 *            the instance standing for the element
	 */
	protected abstract void refreshRepresentation(FlexoConceptInstance grInstance);

	/**
	 * Localization key of what prevents the action, or null when it can be done
	 */
	public String getIssue() {
		if (getConcept() == null) {
			return "please_choose_a_concept";
		}
		if (!isCreateNewInstance()) {
			if (getExistingInstance() == null) {
				return "please_choose_an_instance";
			}
			if (!getConcept().isAssignableFrom(getExistingInstance().getFlexoConcept())) {
				return "that_instance_is_not_an_instance_of_that_concept";
			}
			return null;
		}
		if (getConcept().getCreationSchemes().isEmpty()) {
			// A concept created by generalizing others: never instantiated by itself
			return "that_concept_cannot_be_instantiated";
		}
		if (getConcept().getContainerFlexoConcept() != null) {
			if (getContainer() == null) {
				return "please_select_a_container_for_new_concept_instance";
			}
			if (!getConcept().getContainerFlexoConcept().isAssignableFrom(getContainer().getFlexoConcept())) {
				return "container_is_not_of_right_type";
			}
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return getIssue() == null;
	}

}
