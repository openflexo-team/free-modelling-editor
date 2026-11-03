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
 * Makes a new relational concept emerge from a link between two elements of a free model instance, each standing for an instance of a
 * concept: the relational concept (linking both concepts) and its GR concept are created, the relational concept is instantiated in the
 * sample data between both conceptual instances, and the link stands for that new instance.<br>
 * 
 * The relational concept itself is configured on {@link #getRelationalConceptAction()} (its name, the names of its ends, its
 * properties...), which this action runs.<br>
 * 
 * The technology gives the ends of the link, the instance standing for the link
 * ({@link #createRepresentationInstance(FlexoConcept)}) and what happens to its representation
 * ({@link #configureRepresentation(FlexoConcept)}, {@link #refreshRepresentation(FlexoConceptInstance)})
 * 
 * @param <A>
 *            type of the action
 * @param <T>
 *            type of the focused link
 * @param <FM>
 *            type of the free model
 * @param <FMI>
 *            type of the free model instance
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCreateNewConceptFromLink<A extends AbstractCreateNewConceptFromLink<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractLinkAction<A, T, FM, FMI> {

	private CreateNewRelationalConcept relationalConceptAction;
	private FlexoConceptInstance newFlexoConceptInstance;

	protected AbstractCreateNewConceptFromLink(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The action creating the relational concept and its GR concept, between the concepts both ends of the link stand for: this is where
	 * the relational concept is configured
	 */
	public CreateNewRelationalConcept getRelationalConceptAction() {
		if (relationalConceptAction == null && getFMEFreeModel() != null) {
			relationalConceptAction = CreateNewRelationalConcept.actionType.makeNewEmbeddedAction(getFMEFreeModel(), null, this);
			relationalConceptAction.setFromConcept(getFromFlexoConcept());
			relationalConceptAction.setToConcept(getToFlexoConcept());
			relationalConceptAction.setFromGRConcept(getFromFlexoConceptGR());
			relationalConceptAction.setToGRConcept(getToFlexoConceptGR());
		}
		return relationalConceptAction;
	}

	@Override
	public boolean isValid() {
		return linksTwoInstances() && getRelationalConceptAction() != null && getRelationalConceptAction().isValid();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FMI freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException(getFocusedObject() + " does not belong to any FreeModel");
		}
		if (!linksTwoInstances()) {
			throw new InvalidArgumentException(getFocusedObject() + " does not link two instances of concepts");
		}

		// Now we create the new conceptual concept
		CreateNewRelationalConcept createNewConcept = getRelationalConceptAction();
		createNewConcept.doAction();

		if (!createNewConcept.hasActionExecutionSucceeded()) {
			throw new FlexoException("Could not create the relational concept");
		}

		FlexoConcept newFlexoConcept = createNewConcept.getNewFlexoConcept();
		FlexoConcept newGRFlexoConcept = createNewConcept.getNewGRFlexoConcept();

		configureRepresentation(newGRFlexoConcept);

		// Now we instantiate that concept, between both conceptual instances
		CreateFlexoConceptInstance instantiateConcept = CreateFlexoConceptInstance.actionType
				.makeNewEmbeddedAction(getFMEFreeModel().getSampleData().getAccessedVirtualModelInstance(), null, this);
		instantiateConcept.setFlexoConcept(newFlexoConcept);
		CreationScheme cs = newFlexoConcept.getCreationSchemes().get(0);
		instantiateConcept.setCreationScheme(cs);
		instantiateConcept.setParameterValue(cs.getParameters().get(0), getFromFlexoConceptInstance());
		instantiateConcept.setParameterValue(cs.getParameters().get(1), getToFlexoConceptInstance());
		instantiateConcept.doAction();
		FlexoConceptInstance conceptInstance = instantiateConcept.getNewFlexoConceptInstance();

		newFlexoConceptInstance = createRepresentationInstance(newGRFlexoConcept);
		newFlexoConceptInstance.setFlexoActor(conceptInstance, FMEFreeModel.conceptRole(newGRFlexoConcept));

		// We should notify the creation of a new FlexoConcept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, newFlexoConcept);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, newFlexoConcept);

		// This is used to notify the adding of a new instance of a flexo concept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, newFlexoConceptInstance);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getEmbeddedInstances(FlexoConceptInstance)", null,
				newFlexoConceptInstance);

		refreshRepresentation(newFlexoConceptInstance);
	}

	/**
	 * Called once the relational concept and its GR concept are created: what the new GR concept takes from the link (the graphical
	 * representation of a connector...)
	 */
	protected abstract void configureRepresentation(FlexoConcept newGRConcept) throws FlexoException;

	/**
	 * Creates the instance of the new GR concept standing for the link, in the free model instance, its representation role pointing to
	 * the focused link. The template then makes it point to the new conceptual instance.
	 */
	protected abstract FlexoConceptInstance createRepresentationInstance(FlexoConcept newGRConcept) throws FlexoException;

	/**
	 * Called last: brings the view of the representation up to date
	 */
	protected abstract void refreshRepresentation(FlexoConceptInstance grInstance);

	/**
	 * The instance of the new GR concept standing for the link, once the action is done
	 */
	public FlexoConceptInstance getNewFlexoConceptInstance() {
		return newFlexoConceptInstance;
	}

}
