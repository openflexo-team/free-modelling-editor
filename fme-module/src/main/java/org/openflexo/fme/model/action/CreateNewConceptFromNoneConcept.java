/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance.ObjectLookupResult;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.toolbox.StringUtils;

/**
 * This action is used to create a new conceptual concept<br>
 * 
 * Focused object is here a {@link FlexoConceptInstance} of the NoneGR
 * 
 * @author sylvain
 * 
 */
public class CreateNewConceptFromNoneConcept extends AbstractInstantiateConcept<CreateNewConceptFromNoneConcept> {

	private static final Logger logger = Logger.getLogger(CreateNewConceptFromNoneConcept.class.getPackage().getName());

	public static FlexoActionFactory<CreateNewConceptFromNoneConcept, FlexoConceptInstance, FlexoObject> actionType = new FlexoActionFactory<CreateNewConceptFromNoneConcept, FlexoConceptInstance, FlexoObject>(
			"create_new_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewConceptFromNoneConcept makeNewAction(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewConceptFromNoneConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return object.getFlexoConcept().getName().equals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateNewConceptFromNoneConcept.actionType, FlexoConceptInstance.class);
	}

	private String newConceptName;
	private String newConceptDescription;
	private FlexoConcept containerConcept;

	private FlexoConcept newFlexoConcept;
	private FlexoConcept newGRFlexoConcept;

	private CreateNewConceptFromNoneConcept(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FMEDiagramFreeModelInstance freeModelInstance = getFMEFreeModelInstance();
		if (freeModelInstance == null) {
			throw new InvalidArgumentException("FlexoConceptInstance does not belong to any FreeModel");
		}

		FlexoConceptInstance flexoConceptInstance = getFocusedObject();

		String actualName = flexoConceptInstance.getFlexoPropertyValue(FMEFreeModel.NAME_ROLE_NAME);

		// Retrieve shape property of None FC
		ShapeRole noneShapeRole = (ShapeRole) flexoConceptInstance.getFlexoConcept()
				.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		// Retrieve actual shape element
		DiagramShape shapeElement = flexoConceptInstance.getFlexoActor(noneShapeRole);

		// Now we create the new conceptual concept
		CreateNewConcept createNewConcept = CreateNewConcept.actionType.makeNewEmbeddedAction(getFMEFreeModel(), null, this);
		createNewConcept.setNewConceptName(getNewConceptName());
		createNewConcept.setNewConceptDescription(getNewConceptDescription());
		createNewConcept.setContainerConcept(getContainerConcept());
		createNewConcept.setContainerGRConcept(retrieveContainerGRFlexoConcept());
		createNewConcept.doAction();
		newFlexoConcept = createNewConcept.getNewFlexoConcept();
		newGRFlexoConcept = createNewConcept.getNewGRFlexoConcept();

		// Now we instantiate that concept
		CreateFlexoConceptInstance instantiateConcept = CreateFlexoConceptInstance.actionType
				.makeNewEmbeddedAction(getFMEFreeModel().getSampleData().getAccessedVirtualModelInstance(), null, this);
		instantiateConcept.setFlexoConcept(newFlexoConcept);
		FlexoConceptInstance containerFCI = retrieveContainerFlexoConceptInstance();
		if (containerFCI != null) {
			instantiateConcept.setContainer(containerFCI);
		}
		CreationScheme cs = newFlexoConcept.getCreationSchemes().get(0);
		instantiateConcept.setCreationScheme(cs);
		instantiateConcept.setParameterValue(cs.getParameters().get(0), actualName);
		instantiateConcept.doAction();
		FlexoConceptInstance conceptInstance = instantiateConcept.getNewFlexoConceptInstance();

		// Add entry in palette
		freeModelInstance.getFreeModel().createPaletteElementForConcept(newGRFlexoConcept, newFlexoConcept,
				shapeElement.getGraphicalRepresentation(), this);

		// Sets new concept GR with actual shape GR
		ShapeRole shapeRole = (ShapeRole) newGRFlexoConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		shapeRole.getGraphicalRepresentation().setsWith(shapeElement.getGraphicalRepresentation());
		shapeRole.getGraphicalRepresentation().setX(10);
		shapeRole.getGraphicalRepresentation().setY(10);

		// We will here bypass the classical DropScheme
		flexoConceptInstance.setFlexoConcept(newGRFlexoConcept);
		flexoConceptInstance.setFlexoPropertyValue(FMEFreeModel.CONCEPT_ROLE_NAME, conceptInstance);

		// We should notify the creation of a new FlexoConcept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, newFlexoConcept);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, newFlexoConcept);

		// This is used to notify the adding of a new instance of a flexo concept
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, getFocusedObject());
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getEmbeddedInstances(FlexoConceptInstance)", null,
				getFocusedObject());

		// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new shape is FML-controlled
		shapeElement.getParent().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, shapeElement.getParent());

	}

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

	@Override
	public boolean isValid() {

		if (StringUtils.isEmpty(newConceptName)) {
			return false;
		}

		if (getFocusedObject().getVirtualModelInstance().getVirtualModel().getFlexoConcept(newConceptName) != null) {
			return false;
		}

		return true;
	}

	private FlexoConceptInstance retrieveContainerFlexoConceptInstance() {

		DiagramShape shape = getFocusedObject().getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		if (shape.getParent() instanceof DiagramShape) {
			ObjectLookupResult lookup = getFocusedObject().getVirtualModelInstance().lookup(shape.getParent());
			if (lookup != null) {
				FlexoConceptInstance containerGRFCI = lookup.flexoConceptInstance;
				return containerGRFCI.getFlexoActor(FMEFreeModel.CONCEPT_ROLE_NAME);
			}
		}
		return null;
	}

	private FlexoConcept retrieveContainerGRFlexoConcept() {

		DiagramShape shape = getFocusedObject().getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		if (shape.getParent() instanceof DiagramShape) {
			ObjectLookupResult lookup = getFocusedObject().getVirtualModelInstance().lookup(shape.getParent());
			if (lookup != null) {
				return lookup.flexoConceptInstance.getFlexoConcept();
			}
		}
		return null;
	}

	private FlexoConcept retrieveContainerFlexoConcept() {

		DiagramShape shape = getFocusedObject().getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		if (shape.getParent() instanceof DiagramShape) {
			ObjectLookupResult lookup = getFocusedObject().getVirtualModelInstance().lookup(shape.getParent());
			if (lookup != null) {
				FlexoConcept containerConceptGR = lookup.flexoConceptInstance.getFlexoConcept();
				FlexoProperty<?> p = containerConceptGR.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
				FlexoConcept containerConcept = null;
				if (p instanceof FlexoConceptInstanceRole) {
					FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
					containerConcept = fciRole.getFlexoConceptType();
				}
				return containerConcept;
			}
		}
		return null;
	}

	public FlexoConcept getContainerConcept() {

		if (containerConcept == null) {
			return retrieveContainerFlexoConcept();
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

}
