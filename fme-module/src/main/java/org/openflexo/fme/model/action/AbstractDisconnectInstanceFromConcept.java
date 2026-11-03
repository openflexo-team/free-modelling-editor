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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * Disconnects an element of a free model instance from the concept it is an instance of: the element stays as it is in its artefact, but
 * no longer stands for an instance of the conceptual model. It is the opposite of {@link AbstractDeclareInstanceOfExistingConcept}.<br>
 * 
 * The conceptual instance is deleted, with the values of its properties, only when nothing represents it any longer: neither another
 * element of this free model instance, nor an element of another free model instance (of another technology, for instance). Otherwise
 * it is kept as it is.<br>
 * 
 * The focused object is either the instance of the GR concept standing for the element, or the element itself. The technology says what
 * becomes of the instance standing for the element ({@link #releaseRepresentation(FlexoConceptInstance, FlexoConceptInstance)}: back to
 * the NoneGR, deleted...)
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
public abstract class AbstractDisconnectInstanceFromConcept<A extends AbstractDisconnectInstanceFromConcept<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractFreeModelInstanceAction<A, T, FM, FMI> {

	private static final Logger logger = Logger.getLogger(AbstractDisconnectInstanceFromConcept.class.getPackage().getName());

	private boolean conceptInstanceDeleted;

	protected AbstractDisconnectInstanceFromConcept(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The instance of the conceptual concept supplied instance of a GR concept stands for
	 * 
	 * @return null when supplied instance stands for no conceptual instance (the NoneGR), or for a relationship
	 */
	public static FlexoConceptInstance conceptInstanceOf(FlexoConceptInstance grInstance) {
		FlexoConceptInstance conceptInstance = FMEFreeModel.representedInstance(grInstance);
		if (conceptInstance == null || conceptInstance.getFlexoConcept() == null
				|| FMEConceptualModel.isRelationship(conceptInstance.getFlexoConcept())) {
			return null;
		}
		return conceptInstance;
	}

	/**
	 * The other instances of GR concepts, in all the free model instances of the project, standing for the same conceptual instance as
	 * supplied one
	 */
	public static List<FlexoConceptInstance> getOtherRepresentations(FlexoConceptInstance grInstance) {
		List<FlexoConceptInstance> returned = new ArrayList<>();
		FreeModellingProjectNature nature = natureOf(grInstance);
		FlexoConceptInstance conceptInstance = conceptInstanceOf(grInstance);
		if (nature != null && conceptInstance != null) {
			for (FlexoConceptInstance representation : nature.getRepresentations(conceptInstance)) {
				if (representation != grInstance) {
					returned.add(representation);
				}
			}
		}
		return returned;
	}

	/**
	 * Whether disconnecting supplied instance deletes the conceptual instance: nothing else represents it
	 */
	public static boolean willDeleteConceptInstance(FlexoConceptInstance grInstance) {
		return conceptInstanceOf(grInstance) != null && getOtherRepresentations(grInstance).isEmpty();
	}

	/**
	 * Localization key of what prevents to disconnect supplied instance, or null when it can be disconnected.<br>
	 * When the conceptual instance is to be deleted, it must neither contain other instances, nor be an end of a relationship
	 */
	public static String getIssue(FlexoConceptInstance grInstance) {

		FlexoConceptInstance conceptInstance = conceptInstanceOf(grInstance);
		if (conceptInstance == null) {
			return "cannot_disconnect_that_instance";
		}
		if (!willDeleteConceptInstance(grInstance)) {
			// The conceptual instance is kept: nothing else changes
			return null;
		}
		if (!conceptInstance.getEmbeddedFlexoConceptInstances().isEmpty()) {
			return "cannot_disconnect_an_instance_containing_other_instances";
		}
		if (conceptInstance.getVirtualModelInstance() != null) {
			for (FlexoConceptInstance other : conceptInstance.getVirtualModelInstance().getFlexoConceptInstances()) {
				List<FlexoConceptInstanceRole> ends = FMEConceptualModel.relationEnds(other.getFlexoConcept());
				if (ends != null) {
					for (FlexoConceptInstanceRole end : ends) {
						if (other.getFlexoActor(end.getRoleName()) == conceptInstance) {
							return "cannot_disconnect_an_instance_related_to_another_one";
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * The instance of the GR concept to disconnect: the focused one, or the one standing for the focused element
	 */
	public FlexoConceptInstance getGRInstance() {
		if (getFocusedObject() instanceof FlexoConceptInstance) {
			return (FlexoConceptInstance) getFocusedObject();
		}
		FMI freeModelInstance = getFMEFreeModelInstance();
		return freeModelInstance != null ? freeModelInstance.getGRInstance(getFocusedObject()) : null;
	}

	public String getIssue() {
		return getIssue(getGRInstance());
	}

	/**
	 * Whether this action deletes the conceptual instance: nothing else represents it
	 */
	public boolean willDeleteConceptInstance() {
		return willDeleteConceptInstance(getGRInstance());
	}

	/**
	 * Whether the conceptual instance was deleted, once the action is done
	 */
	public boolean isConceptInstanceDeleted() {
		return conceptInstanceDeleted;
	}

	/**
	 * Names of the properties of the conceptual instance, other than the one giving its label, which are set and will be lost: none when
	 * the conceptual instance is kept
	 */
	public List<String> getLostPropertyNames() {
		List<String> returned = new ArrayList<>();
		if (!willDeleteConceptInstance()) {
			return returned;
		}
		FlexoConceptInstance conceptInstance = conceptInstanceOf(getGRInstance());
		String labelProperty = FMEConceptualModel.labelPropertyName(conceptInstance.getFlexoConcept());
		for (FlexoProperty<?> property : conceptInstance.getFlexoConcept().getAccessibleProperties()) {
			if (!property.getPropertyName().equals(labelProperty) && conceptInstance.getFlexoPropertyValue(property.getPropertyName()) != null) {
				returned.add(property.getPropertyName());
			}
		}
		return returned;
	}

	@Override
	public boolean isValid() {
		return getIssue() == null;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FMI freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException(getFocusedObject() + " does not belong to any FreeModel");
		}

		FlexoConceptInstance grInstance = getGRInstance();
		FlexoConceptInstance conceptInstance = conceptInstanceOf(grInstance);
		if (conceptInstance == null || getIssue() != null) {
			throw new InvalidArgumentException("Cannot disconnect " + grInstance + ": " + getIssue());
		}

		logger.info("Disconnect " + grInstance + " from " + conceptInstance.getFlexoConcept());

		boolean deleteConceptInstance = willDeleteConceptInstance();

		releaseRepresentation(grInstance, conceptInstance);

		if (deleteConceptInstance) {
			// Nothing represents the conceptual instance any longer
			conceptInstance.delete();
			conceptInstanceDeleted = true;
		}

		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, conceptInstance.getFlexoConcept());
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, conceptInstance.getFlexoConcept());
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, grInstance);

		refreshRepresentation(grInstance);
	}

	/**
	 * Makes supplied instance stop standing for the conceptual instance (an instance of the NoneGR again, or deleted). The element of the
	 * artefact stays as it is, and the conceptual instance is not deleted here.
	 * 
	 * @param grInstance
	 *            the instance standing for the element
	 * @param conceptInstance
	 *            the conceptual instance it stood for
	 */
	protected abstract void releaseRepresentation(FlexoConceptInstance grInstance, FlexoConceptInstance conceptInstance)
			throws FlexoException;

	/**
	 * Called last: brings the view of the representation up to date
	 */
	protected abstract void refreshRepresentation(FlexoConceptInstance grInstance);

}
