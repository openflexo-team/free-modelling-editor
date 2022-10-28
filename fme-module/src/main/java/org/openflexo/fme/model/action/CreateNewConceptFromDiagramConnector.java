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

package org.openflexo.fme.model.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.diana.GraphicalRepresentation.VerticalTextAlignment;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance.ObjectLookupResult;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.fml.ConnectorRole;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;

/**
 * This action is used to create a new concept from a connector linking two concepts<br>
 * Focused object is the connector
 * 
 * @author sylvain
 * 
 */
public class CreateNewConceptFromDiagramConnector extends FMEAction<CreateNewConceptFromDiagramConnector, DiagramConnector, FlexoObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateNewConceptFromDiagramConnector.class.getPackage().getName());

	public static FlexoActionFactory<CreateNewConceptFromDiagramConnector, DiagramConnector, FlexoObject> actionType = new FlexoActionFactory<CreateNewConceptFromDiagramConnector, DiagramConnector, FlexoObject>(
			"create_new_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewConceptFromDiagramConnector makeNewAction(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewConceptFromDiagramConnector(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DiagramConnector object, Vector<FlexoObject> globalSelection) {
			// return object.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME) != null;
			return true;
		}

		@Override
		public boolean isEnabledForSelection(DiagramConnector object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateNewConceptFromDiagramConnector.actionType, DiagramConnector.class);
	}

	private FlexoConcept fromFlexoConcept;
	private FlexoConcept toFlexoConcept;

	private FlexoConcept fromFlexoConceptGR;
	private FlexoConcept toFlexoConceptGR;

	private FlexoConceptInstance fromGRFlexoConceptInstance;
	private FlexoConceptInstance toGRFlexoConceptInstance;

	private FlexoConceptInstance fromFlexoConceptInstance;
	private FlexoConceptInstance toFlexoConceptInstance;

	private FlexoConceptInstance newFlexoConceptInstance;

	private CreateNewConceptFromDiagramConnector(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

		ObjectLookupResult startConceptLookup = getFreeModelInstance().getAccessedVirtualModelInstance()
				.lookup(getFocusedObject().getStartShape());
		if (startConceptLookup != null) {
			fromGRFlexoConceptInstance = startConceptLookup.flexoConceptInstance;
			fromFlexoConceptGR = fromGRFlexoConceptInstance.getFlexoConcept();
			// System.out.println("START lookup: " + startConceptLookup.flexoConceptInstance + " of=" + fromFlexoConceptGR);
			FlexoProperty<?> p = fromFlexoConceptGR.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				fromFlexoConcept = fciRole.getFlexoConceptType();
			}
			fromFlexoConceptInstance = fromGRFlexoConceptInstance.getFlexoPropertyValue(FMEFreeModel.CONCEPT_ROLE_NAME);
		}

		ObjectLookupResult endConceptLookup = getFreeModelInstance().getAccessedVirtualModelInstance()
				.lookup(getFocusedObject().getEndShape());
		if (endConceptLookup != null) {
			toGRFlexoConceptInstance = endConceptLookup.flexoConceptInstance;
			toFlexoConceptGR = toGRFlexoConceptInstance.getFlexoConcept();
			// System.out.println("END lookup: " + endConceptLookup.flexoConceptInstance + " of=" + toFlexoConceptGR);
			FlexoProperty<?> p = toFlexoConceptGR.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				toFlexoConcept = fciRole.getFlexoConceptType();
			}
			toFlexoConceptInstance = toGRFlexoConceptInstance.getFlexoPropertyValue(FMEFreeModel.CONCEPT_ROLE_NAME);
		}

	}

	/**
	 * Return {@link FMEFreeModelInstance} which has diagram containing focused {@link DiagramElement}
	 * 
	 * @return
	 */
	public FMEDiagramFreeModelInstance getFreeModelInstance() {
		for (FMEFreeModel freeModel : getFreeModellingProjectNature().getFreeModels()) {
			for (FMEFreeModelInstance freeModelInstance : freeModel.getFreeModelInstances()) {
				if (freeModelInstance instanceof FMEDiagramFreeModelInstance
						&& ((FMEDiagramFreeModelInstance) freeModelInstance).getDiagram().equals(getFocusedObject().getDiagram())) {
					return (FMEDiagramFreeModelInstance) freeModelInstance;
				}
			}
		}
		return null;
	}

	/**
	 * Return {@link FMEFreeModelInstance} which has diagram containing focused {@link DiagramElement}
	 * 
	 * @return
	 */
	public FMEDiagramFreeModel getFreeModel() {
		if (getFreeModelInstance() != null) {
			return getFreeModelInstance().getFreeModel();
		}
		return null;
	}

	public FlexoConcept getFromFlexoConcept() {
		return fromFlexoConcept;
	}

	public void setFromFlexoConcept(FlexoConcept fromFlexoConcept) {
		if ((fromFlexoConcept == null && this.fromFlexoConcept != null)
				|| (fromFlexoConcept != null && !fromFlexoConcept.equals(this.fromFlexoConcept))) {
			FlexoConcept oldValue = this.fromFlexoConcept;
			this.fromFlexoConcept = fromFlexoConcept;
			getPropertyChangeSupport().firePropertyChange("fromFlexoConcept", oldValue, fromFlexoConcept);
		}
	}

	public FlexoConcept getToFlexoConcept() {
		return toFlexoConcept;
	}

	public void setToFlexoConcept(FlexoConcept toFlexoConcept) {
		if ((toFlexoConcept == null && this.toFlexoConcept != null)
				|| (toFlexoConcept != null && !toFlexoConcept.equals(this.toFlexoConcept))) {
			FlexoConcept oldValue = this.toFlexoConcept;
			this.toFlexoConcept = toFlexoConcept;
			getPropertyChangeSupport().firePropertyChange("toFlexoConcept", oldValue, toFlexoConcept);
		}
	}

	@Override
	public boolean isValid() {

		if (getFromFlexoConcept() == null) {
			return false;
		}

		if (getToFlexoConcept() == null) {
			return false;
		}

		return true;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		// Now we create the new conceptual concept
		CreateNewRelationalConcept createNewConcept = CreateNewRelationalConcept.actionType.makeNewEmbeddedAction(getFreeModel(), null,
				this);
		createNewConcept.setFromConcept(getFromFlexoConcept());
		createNewConcept.setToConcept(getToFlexoConcept());
		createNewConcept.setFromGRConcept(fromFlexoConceptGR);
		createNewConcept.setToGRConcept(toFlexoConceptGR);
		createNewConcept.doAction();

		if (!createNewConcept.hasActionExecutionSucceeded()) {
			return;
		}

		FlexoConcept newFlexoConcept = createNewConcept.getNewFlexoConcept();
		FlexoConcept newGRFlexoConcept = createNewConcept.getNewGRFlexoConcept();

		// Sets new concept GR with actual connector GR
		ConnectorRole connectorRole = (ConnectorRole) newGRFlexoConcept.getAccessibleProperty(FMEDiagramFreeModel.CONNECTOR_ROLE_NAME);
		connectorRole.getGraphicalRepresentation().setsWith(getFocusedObject().getGraphicalRepresentation());
		// Default align on bottom
		getFocusedObject().getGraphicalRepresentation().setVerticalTextAlignment(VerticalTextAlignment.BOTTOM);
		connectorRole.getGraphicalRepresentation().setVerticalTextAlignment(VerticalTextAlignment.BOTTOM);

		// Now we instantiate that concept
		try {
			CreateFlexoConceptInstance instantiateConcept = CreateFlexoConceptInstance.actionType
					.makeNewEmbeddedAction(getFreeModel().getSampleData().getAccessedVirtualModelInstance(), null, this);
			instantiateConcept.setFlexoConcept(newFlexoConcept);
			CreationScheme cs = newFlexoConcept.getCreationSchemes().get(0);
			instantiateConcept.setCreationScheme(cs);
			FlexoConceptInstance fromFCI = fromGRFlexoConceptInstance.execute("fmeConcept");
			FlexoConceptInstance toFCI = toGRFlexoConceptInstance.execute("fmeConcept");

			instantiateConcept.setParameterValue(cs.getParameters().get(0), fromFCI);
			instantiateConcept.setParameterValue(cs.getParameters().get(1), toFCI);
			instantiateConcept.doAction();
			FlexoConceptInstance conceptInstance = instantiateConcept.getNewFlexoConceptInstance();

			newFlexoConceptInstance = getFreeModelInstance().getAccessedVirtualModelInstance()
					.makeNewFlexoConceptInstance(newGRFlexoConcept);
			ConnectorRole geRole = (ConnectorRole) newGRFlexoConcept.getAccessibleProperty(FMEDiagramFreeModel.CONNECTOR_ROLE_NAME);
			newFlexoConceptInstance.setFlexoActor(getFocusedObject(), geRole);
			FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) newGRFlexoConcept
					.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
			newFlexoConceptInstance.setFlexoActor(conceptInstance, fciRole);

		} catch (TypeMismatchException e) {
			e.printStackTrace();
		} catch (NullReferenceException e) {
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		} catch (InvalidBindingException e) {
			e.printStackTrace();
		}

		// We should notify the creation of a new FlexoConcept
		getFreeModelInstance().getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, newFlexoConcept);
		getFreeModelInstance().getPropertyChangeSupport().firePropertyChange("usedTopLevelFlexoConcepts", null, newFlexoConcept);

		// This is used to notify the adding of a new instance of a flexo concept
		getFreeModelInstance().getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null, getFocusedObject());
		getFreeModelInstance().getPropertyChangeSupport().firePropertyChange("getEmbeddedInstances(FlexoConceptInstance)", null,
				getFocusedObject());

		// The new connector has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the connector has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramConnector. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new connector is FML-controlled
		getFocusedObject().getParent().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null,
				getFocusedObject().getParent());

	}

	public FlexoConceptInstance getNewFlexoConceptInstance() {
		return newFlexoConceptInstance;
	}

}
