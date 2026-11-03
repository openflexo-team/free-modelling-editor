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
 * Declares an element of a free model instance, represented by an instance of one of its GR concepts (the NoneGR for instance), to be an
 * instance of an existing concept of the conceptual model<br>
 * 
 * The free modelling part is the same for every technology: the concept is instantiated in the sample data, its GR concept is retrieved
 * (or created), and the focused instance becomes an instance of that GR concept, pointing to the new conceptual instance. What happens to
 * the representation element itself (a shape, its graphical representation, a palette...) is the part of the technology, given by the
 * hooks {@link #prepareRepresentation(FlexoConceptInstance, FlexoConcept, boolean)},
 * {@link #completeRepresentation(FlexoConceptInstance, FlexoConcept, boolean)} and {@link #refreshRepresentation(FlexoConceptInstance)}
 * 
 * @param <A>
 *            type of the action
 * @param <FM>
 *            type of the free model
 * @param <FMI>
 *            type of the free model instance
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractDeclareInstanceOfExistingConcept<A extends AbstractDeclareInstanceOfExistingConcept<A, FM, FMI>, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractInstantiateConcept<A, FM, FMI> {

	private static final Logger logger = Logger.getLogger(AbstractDeclareInstanceOfExistingConcept.class.getPackage().getName());

	private FlexoConcept concept;
	private FlexoConceptInstance container;

	protected AbstractDeclareInstanceOfExistingConcept(FlexoActionFactory<A, FlexoConceptInstance, FlexoObject> actionFactory,
			FlexoConceptInstance focusedObject, List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * Return concept (at conceptual level)
	 * 
	 * @return
	 */
	public FlexoConcept getConcept() {
		if (concept == null && getFreeModellingProjectNature() != null
				&& getFMEFreeModel().getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().size() > 0) {
			return getFMEFreeModel().getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().get(0);
		}
		return concept;
	}

	public void setConcept(FlexoConcept concept) {
		boolean wasValid = isValid();
		this.concept = concept;
		getPropertyChangeSupport().firePropertyChange("concept", null, concept);
		getPropertyChangeSupport().firePropertyChange("hasGRConcept", !hasGRConcept(), hasGRConcept());
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
		return getFMEFreeModel().getGRFlexoConcept(getConcept(), null, getEditor(), this, createWhenUnexistant);
	}

	public FlexoConceptInstance getContainer() {
		return container;
	}

	public void setContainer(FlexoConceptInstance container) {
		if ((container == null && this.container != null) || (container != null && !container.equals(this.container))) {
			FlexoConceptInstance oldValue = this.container;
			this.container = container;
			getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
		}
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		logger.info("Declare " + getFocusedObject() + " as instance of existing concept " + getConcept());

		FMI freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException("FlexoConceptInstance does not belong to any FreeModel");
		}

		FlexoConceptInstance flexoConceptInstance = getFocusedObject();

		FlexoConceptInstance conceptInstance = instantiateConcept(flexoConceptInstance.getFlexoPropertyValue(FMEFreeModel.NAME_ROLE_NAME));

		boolean grConceptWasExisting = hasGRConcept();

		// Create or retrieve the GR concept
		FlexoConcept grConcept = getGRConcept(true);

		prepareRepresentation(flexoConceptInstance, grConcept, grConceptWasExisting);

		// We will here bypass the classical DropScheme
		flexoConceptInstance.setFlexoConcept(grConcept);
		flexoConceptInstance.setFlexoPropertyValue(FMEFreeModel.conceptRoleName(grConcept), conceptInstance);

		completeRepresentation(flexoConceptInstance, grConcept, grConceptWasExisting);

		// We should notify the creation of a new FlexoConcept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, getConcept());
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, getConcept());
		// This is used to notify the adding of a new instance of a flexo concept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, getFocusedObject());

		refreshRepresentation(flexoConceptInstance);
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
	 * Called once the GR concept is known, while the focused instance is still an instance of its former GR concept
	 * 
	 * @param flexoConceptInstance
	 *            the focused instance
	 * @param grConcept
	 *            the GR concept of the concept
	 * @param grConceptWasExisting
	 *            whether the GR concept existed before this action (it was created otherwise)
	 */
	protected abstract void prepareRepresentation(FlexoConceptInstance flexoConceptInstance, FlexoConcept grConcept,
			boolean grConceptWasExisting) throws FlexoException;

	/**
	 * Called once the focused instance is an instance of the GR concept, pointing to the new conceptual instance
	 * 
	 * @param flexoConceptInstance
	 *            the focused instance
	 * @param grConcept
	 *            the GR concept of the concept
	 * @param grConceptWasExisting
	 *            whether the GR concept existed before this action (it was created otherwise)
	 */
	protected abstract void completeRepresentation(FlexoConceptInstance flexoConceptInstance, FlexoConcept grConcept,
			boolean grConceptWasExisting) throws FlexoException;

	/**
	 * Called last, once the free model instance has notified the new instance: brings the view of the representation up to date
	 * 
	 * @param flexoConceptInstance
	 *            the focused instance
	 */
	protected abstract void refreshRepresentation(FlexoConceptInstance flexoConceptInstance);

	@Override
	public boolean isValid() {
		if (getConcept() == null) {
			return false;
		}
		return true;
	}

}
