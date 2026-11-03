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
import org.openflexo.fme.model.FMENames;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.toolbox.StringUtils;

/**
 * Makes a new concept of the conceptual model emerge from an element of a free model instance: the concept and its GR concept are
 * created, the concept is instantiated in the sample data, and the element stands for that new instance<br>
 * 
 * The focused object is either an instance of a GR concept standing for the element (the NoneGR of a diagram shape for instance), or the
 * element itself when nothing stands for it yet (a PowerPoint shape).<br>
 * 
 * The technology gives the instance standing for the element ({@link #retrieveRepresentationInstance(FlexoConcept)}), the name of the
 * new instance ({@link #getDefaultInstanceName()}), the element containing the focused one if any ({@link #getContainerGRInstance()}),
 * and what happens to the representation ({@link #configureRepresentation(FlexoConceptInstance, FlexoConcept, FlexoConcept)},
 * {@link #refreshRepresentation(FlexoConceptInstance)})
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
public abstract class AbstractCreateNewConceptFromElement<A extends AbstractCreateNewConceptFromElement<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractFreeModelInstanceAction<A, T, FM, FMI> {

	private static final Logger logger = Logger.getLogger(AbstractCreateNewConceptFromElement.class.getPackage().getName());

	private String newConceptName;
	private String newConceptDescription;
	private FlexoConcept containerConcept;

	private FlexoConcept newFlexoConcept;
	private FlexoConcept newGRFlexoConcept;
	private FlexoConceptInstance grInstance;

	private NewConceptStructure structure;

	protected AbstractCreateNewConceptFromElement(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The properties of the concept to create, and the one labelling its instances: <code>name</code> and <code>description</code> unless
	 * configured
	 */
	public NewConceptStructure getStructure() {
		if (structure == null) {
			structure = new NewConceptStructure();
			structure.setContextIssue(() -> getFMEFreeModel() != null && getFMEFreeModel().getAccessedVirtualModel()
					.getAccessibleProperty(structure.getConceptRoleName()) != null ? "concept_role_name_already_used" : null);
			structure.getPropertyChangeSupport().addPropertyChangeListener(evt -> {
				boolean wasValid = isValid();
				getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
			});
		}
		return structure;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FMI freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException(getFocusedObject() + " does not belong to any FreeModel");
		}

		String actualName = getDefaultInstanceName();
		FlexoConceptInstance containerGRInstance = getContainerGRInstance();

		// Now we create the new conceptual concept
		CreateNewConcept createNewConcept = CreateNewConcept.actionType.makeNewEmbeddedAction(getFMEFreeModel(), null, this);
		createNewConcept.setNewConceptName(getNewConceptName());
		createNewConcept.setNewConceptDescription(getNewConceptDescription());
		// The structure edited by the user is the one of this action: it is what the embedded action creates
		createNewConcept.setStructure(getStructure());
		createNewConcept.setContainerConcept(getContainerConcept());
		createNewConcept.setContainerGRConcept(containerGRInstance != null ? containerGRInstance.getFlexoConcept() : null);
		createNewConcept.doAction();
		newFlexoConcept = createNewConcept.getNewFlexoConcept();
		newGRFlexoConcept = createNewConcept.getNewGRFlexoConcept();

		// Now we instantiate that concept
		CreateFlexoConceptInstance instantiateConcept = CreateFlexoConceptInstance.actionType
				.makeNewEmbeddedAction(getFMEFreeModel().getSampleData().getAccessedVirtualModelInstance(), null, this);
		instantiateConcept.setFlexoConcept(newFlexoConcept);
		FlexoConceptInstance containerFCI = FMEFreeModel.representedInstance(containerGRInstance);
		if (containerFCI != null) {
			instantiateConcept.setContainer(containerFCI);
		}
		CreationScheme cs = newFlexoConcept.getCreationSchemes().get(0);
		instantiateConcept.setCreationScheme(cs);
		instantiateConcept.setParameterValue(cs.getParameters().get(0), actualName);
		instantiateConcept.doAction();
		FlexoConceptInstance conceptInstance = instantiateConcept.getNewFlexoConceptInstance();

		grInstance = retrieveRepresentationInstance(newGRFlexoConcept);

		configureRepresentation(grInstance, newFlexoConcept, newGRFlexoConcept);

		// We will here bypass the classical DropScheme
		grInstance.setFlexoConcept(newGRFlexoConcept);
		grInstance.setFlexoPropertyValue(FMEFreeModel.conceptRoleName(newGRFlexoConcept), conceptInstance);

		// We should notify the creation of a new FlexoConcept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, newFlexoConcept);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, newFlexoConcept);

		// This is used to notify the adding of a new instance of a flexo concept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, grInstance);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getEmbeddedInstances(FlexoConceptInstance)", null, grInstance);

		refreshRepresentation(grInstance);
	}

	/**
	 * The name the representation element gives to the new conceptual instance (the label of a shape, the text of a PowerPoint shape...)
	 */
	protected abstract String getDefaultInstanceName();

	/**
	 * The instance of a GR concept standing for the element containing the focused one, if any (a shape containing a shape): the new
	 * conceptual instance is created in the instance it stands for, and the new GR concept is dropped into its GR concept
	 */
	protected FlexoConceptInstance getContainerGRInstance() {
		return null;
	}

	/**
	 * The instance standing for the element: the focused instance itself, or an instance created for the focused element.<br>
	 * It may still be an instance of another GR concept (the NoneGR), or already be an instance of supplied GR concept
	 * 
	 * @param grConcept
	 *            the new GR concept
	 */
	protected abstract FlexoConceptInstance retrieveRepresentationInstance(FlexoConcept grConcept) throws FlexoException;

	/**
	 * Called once the concept, its GR concept and its instance are created, before the instance standing for the element is made an
	 * instance of the new GR concept: what the new GR concept takes from the element (a shape, a palette entry...)
	 */
	protected abstract void configureRepresentation(FlexoConceptInstance grInstance, FlexoConcept newConcept, FlexoConcept newGRConcept)
			throws FlexoException;

	/**
	 * Called last, once the free model instance has notified the new instance: brings the view of the representation up to date
	 */
	protected abstract void refreshRepresentation(FlexoConceptInstance grInstance);

	public String getNewConceptName() {
		return newConceptName;
	}

	public void setNewConceptName(String newConceptName) {
		boolean wasValid = isValid();
		this.newConceptName = newConceptName;
		getPropertyChangeSupport().firePropertyChange("newConceptName", null, newConceptName);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public String getNewConceptDescription() {
		return newConceptDescription;
	}

	public void setNewConceptDescription(String newConceptDescription) {
		boolean wasValid = isValid();
		this.newConceptDescription = newConceptDescription;
		getPropertyChangeSupport().firePropertyChange("newConceptDescription", null, newConceptDescription);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public FlexoConcept getNewFlexoConcept() {
		return newFlexoConcept;
	}

	public FlexoConcept getNewGRFlexoConcept() {
		return newGRFlexoConcept;
	}

	/**
	 * The instance of the new GR concept standing for the element, once the action is done
	 */
	public FlexoConceptInstance getGRInstance() {
		return grInstance;
	}

	/**
	 * The concept containing the new one: by default the one the element containing the focused one stands for
	 */
	public FlexoConcept getContainerConcept() {
		if (containerConcept == null) {
			FlexoConceptInstance containerGRInstance = getContainerGRInstance();
			FlexoConceptInstanceRole role = containerGRInstance != null ? FMEFreeModel.conceptRole(containerGRInstance.getFlexoConcept())
					: null;
			return role != null ? role.getFlexoConceptType() : null;
		}
		return containerConcept;
	}

	public void setContainerConcept(FlexoConcept containerConcept) {
		if (containerConcept != this.containerConcept) {
			FlexoConcept oldValue = this.containerConcept;
			this.containerConcept = containerConcept;
			getPropertyChangeSupport().firePropertyChange("containerConcept", oldValue, containerConcept);
		}
	}

	@Override
	public boolean isValid() {

		if (StringUtils.isEmpty(newConceptName) || !FMENames.isValidConceptName(newConceptName)) {
			return false;
		}

		if (getFMEFreeModel() != null && getFMEFreeModel().isConceptNameUsed(newConceptName)) {
			return false;
		}

		return getStructure().isValid();
	}

}
