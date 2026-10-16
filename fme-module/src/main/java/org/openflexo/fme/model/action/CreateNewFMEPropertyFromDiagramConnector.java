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

import org.openflexo.connie.DataBinding;
import org.openflexo.diana.ConnectorGraphicalRepresentation;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FMEInspectorGenerator;
import org.openflexo.fme.model.FMENames;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreateTechnologyRole;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance.ObjectLookupResult;
import org.openflexo.technologyadapter.diagram.fml.ConnectorRole;
import org.openflexo.technologyadapter.diagram.fml.LinkScheme;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.fml.editionaction.AddConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.toolbox.StringUtils;

/**
 * This action is used to create a new property from a connector linking two concepts<br>
 * Focused object is the connector
 * 
 * @author sylvain
 * 
 */
public class CreateNewFMEPropertyFromDiagramConnector
		extends FMEAction<CreateNewFMEPropertyFromDiagramConnector, DiagramConnector, FlexoObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateNewFMEPropertyFromDiagramConnector.class.getPackage().getName());

	public static FlexoActionFactory<CreateNewFMEPropertyFromDiagramConnector, DiagramConnector, FlexoObject> actionType = new FlexoActionFactory<CreateNewFMEPropertyFromDiagramConnector, DiagramConnector, FlexoObject>(
			"create_new_property", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewFMEPropertyFromDiagramConnector makeNewAction(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewFMEPropertyFromDiagramConnector(focusedObject, globalSelection, editor);
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
		FlexoObjectImpl.addActionForClass(CreateNewFMEPropertyFromDiagramConnector.actionType, DiagramConnector.class);
	}

	private String propertyName;
	private String description;

	private FlexoConcept fromFlexoConcept;
	private FlexoConcept toFlexoConcept;

	private FlexoConcept fromFlexoConceptGR;
	private FlexoConcept toFlexoConceptGR;

	private FlexoConceptInstance fromGRFlexoConceptInstance;
	private FlexoConceptInstance toGRFlexoConceptInstance;

	private FlexoConceptInstance fromFlexoConceptInstance;
	private FlexoConceptInstance toFlexoConceptInstance;

	private CreateNewFMEPropertyFromDiagramConnector(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

		ObjectLookupResult startConceptLookup = getFreeModelInstance().getAccessedVirtualModelInstance()
				.lookup(getFocusedObject().getStartShape());
		if (startConceptLookup != null) {
			fromGRFlexoConceptInstance = startConceptLookup.flexoConceptInstance;
			fromFlexoConceptGR = fromGRFlexoConceptInstance.getFlexoConcept();
			// System.out.println("START lookup: " + startConceptLookup.flexoConceptInstance + " of=" + fromFlexoConceptGR);
			FlexoProperty<?> p = FMEFreeModel.conceptRole(fromFlexoConceptGR);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				fromFlexoConcept = fciRole.getFlexoConceptType();
			}
			fromFlexoConceptInstance = fromGRFlexoConceptInstance.getFlexoPropertyValue(FMEFreeModel.conceptRoleName(fromFlexoConceptGR));
		}

		ObjectLookupResult endConceptLookup = getFreeModelInstance().getAccessedVirtualModelInstance()
				.lookup(getFocusedObject().getEndShape());
		if (endConceptLookup != null) {
			toGRFlexoConceptInstance = endConceptLookup.flexoConceptInstance;
			toFlexoConceptGR = toGRFlexoConceptInstance.getFlexoConcept();
			// System.out.println("END lookup: " + endConceptLookup.flexoConceptInstance + " of=" + toFlexoConceptGR);
			FlexoProperty<?> p = FMEFreeModel.conceptRole(toFlexoConceptGR);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				toFlexoConcept = fciRole.getFlexoConceptType();
			}
			toFlexoConceptInstance = toGRFlexoConceptInstance.getFlexoPropertyValue(FMEFreeModel.conceptRoleName(toFlexoConceptGR));
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

	@Override
	protected void doAction(Object context) throws FlexoException {

		CreateFlexoConceptInstanceRole createPropertyAction = CreateFlexoConceptInstanceRole.actionType
				.makeNewEmbeddedAction(getFromFlexoConcept(), null, this);
		createPropertyAction.setRoleName(getCreatedPropertyName());
		createPropertyAction.setFlexoConceptInstanceType(getToFlexoConcept());
		createPropertyAction.setVirtualModelInstance(new DataBinding<>("container"));
		createPropertyAction.setDescription(getDescription());
		createPropertyAction.doAction();

		if (fromFlexoConceptGR != null) {
			// The source's inspector derives entirely to the conceptual concept's own (@Inspector(derived=...));
			// completing THAT one - not regenerating it: its author may have edited it - is what makes the new property show
			FMEInspectorGenerator.addPropertyToConceptualInspector(getFromFlexoConcept(),
					getFromFlexoConcept().getAccessibleProperty(getCreatedPropertyName()));
		}

		FlexoConcept connectorGR = buildConnectorGRFlexoConcept();

		fromFlexoConceptInstance.setFlexoPropertyValue(getCreatedPropertyName(), toFlexoConceptInstance);

	}

	/**
	 * Return graphical representation concept
	 * 
	 * @return
	 */
	/*public FlexoConcept getGRConcept() {
		// return getFocusedObject();
		return null;
	}*/

	/**
	 * Return concept (conceptual model level) where to create the property<br>
	 * 
	 * @return
	 */
	/*public FlexoConcept getConcept() {
		if (getGRConcept() != null) {
			FlexoProperty<?> p = getGRConcept().getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				return fciRole.getFlexoConceptType();
			}
		}
		return null;
	}*/

	/**
	 * The name the property is created with: {@link #getPropertyName()}, as typed, normalized into a name FML accepts
	 */
	public String getCreatedPropertyName() {
		return FMENames.propertyName(getPropertyName());
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		if ((propertyName == null && getPropertyName() != null) || (propertyName != null && !propertyName.equals(getPropertyName()))) {
			String oldValue = getPropertyName();
			this.propertyName = propertyName;
			getPropertyChangeSupport().firePropertyChange("propertyName", oldValue, propertyName);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if ((description == null && this.description != null) || (description != null && !description.equals(this.description))) {
			String oldValue = this.description;
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
		}
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

		if (getCreatedPropertyName() == null) {
			return false;
		}

		if (getFromFlexoConcept() == null) {
			return false;
		}

		return true;
	}

	public static final String FROM_CONCEPT_INSTANCE = "fromConceptInstance";
	public static final String TO_CONCEPT_INSTANCE = "toConceptInstance";
	public static final String CONNECTOR_ROLE_NAME = "connector";

	private FlexoConcept buildConnectorGRFlexoConcept() {

		// Creates the concept
		CreateFlexoConcept action = CreateFlexoConcept.actionType
				.makeNewEmbeddedAction(fromFlexoConceptGR.getDeclaringCompilationUnit().getVirtualModel(), null, this);
		String conceptName = FMENames.conceptName(getCreatedPropertyName()) + "ConnectorGR";
		action.setNewFlexoConceptName(conceptName);
		action.doAction();
		FlexoConcept returned = action.getNewFlexoConcept();

		// Create new FlexoConceptInstanceRole to store the start GR concept instance
		CreateFlexoConceptInstanceRole createConceptRole1 = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(returned, null,
				this);
		createConceptRole1.setRoleName(FROM_CONCEPT_INSTANCE);
		createConceptRole1.setFlexoConceptInstanceType(fromFlexoConceptGR);
		createConceptRole1.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>("container"));
		createConceptRole1.doAction();

		// Create new FlexoConceptInstanceRole to store the end GR concept instance
		CreateFlexoConceptInstanceRole createConceptRole2 = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(returned, null,
				this);
		createConceptRole2.setRoleName(TO_CONCEPT_INSTANCE);
		createConceptRole2.setFlexoConceptInstanceType(toFlexoConceptGR);
		createConceptRole2.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>("container"));
		createConceptRole2.doAction();

		// Create new DeletionScheme
		CreateFlexoBehaviour createDeletionScheme = null;
		createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(returned, null, this);
		createDeletionScheme.setAnonymous(true);
		// createDeletionScheme.setFlexoBehaviourName("delete");
		createDeletionScheme.setFlexoBehaviourClass(DeletionScheme.class);
		createDeletionScheme.doAction();
		DeletionScheme deletionScheme = (DeletionScheme) createDeletionScheme.getNewFlexoBehaviour();
		deletionScheme.setSkipConfirmationPanel(true);

		/*if (concept != null) {
			CreateEditionAction deleteConceptAction = null;
			deleteConceptAction = CreateEditionAction.actionType.makeNewEmbeddedAction(deletionScheme.getControlGraph(), null, this);
			deleteConceptAction.setEditionActionClass(DeleteFlexoConceptInstance.class);
			deleteConceptAction.doAction();
		
			DeleteFlexoConceptInstance<?> deleteConcept = (DeleteFlexoConceptInstance<?>) deleteConceptAction.getNewEditionAction();
			deleteConcept.setObject(new DataBinding<>(CONCEPT_ROLE_NAME));
		}*/

		// Bind shapes's label to name property
		/*if (concept != null) {
			// If we are bound to a concept instance, use name of concept
			returned.setRenderer(new DataBinding<String>("instance." + CONCEPT_ROLE_NAME + ".name"));
		}
		else {
			// Otherwise, this is the NoneGR, use primitive name
			returned.setRenderer(new DataBinding<String>("instance.name"));
		}*/

		// configureGRFlexoConcept(returned, concept, containerConceptGR, editor, ownerAction);

		// Creates connector property
		CreateTechnologyRole createConnectorRole = null;
		createConnectorRole = CreateTechnologyRole.actionType.makeNewEmbeddedAction(returned, null, this);
		createConnectorRole.setModelSlot(getFreeModelInstance().getFreeModel().getTypedDiagramModelSlot());
		createConnectorRole.setRoleName(CONNECTOR_ROLE_NAME);
		createConnectorRole.setFlexoRoleClass(ConnectorRole.class);
		createConnectorRole.doAction();
		ConnectorRole role = (ConnectorRole) createConnectorRole.getNewFlexoRole();

		role.setLabel(new DataBinding<>("'" + getCreatedPropertyName() + "'"));

		// Init GR
		DiagramConnector newConnector = createConnector();
		role.setMetamodelElement(newConnector);

		// Create new LinkScheme
		CreateFlexoBehaviour createLinkScheme = null;
		createLinkScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(returned, null, this);
		createLinkScheme.setFlexoBehaviourName("link");
		createLinkScheme.setFlexoBehaviourClass(LinkScheme.class);
		createLinkScheme.doAction();
		LinkScheme linkScheme = (LinkScheme) createLinkScheme.getNewFlexoBehaviour();
		linkScheme.setSkipConfirmationPanel(false);
		linkScheme.setFromTargetFlexoConcept(fromFlexoConceptGR);
		linkScheme.setToTargetFlexoConcept(toFlexoConceptGR);

		CreateEditionAction setFromFCIAction = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null,
				this);
		setFromFCIAction.setEditionActionClass(ExpressionAction.class);
		setFromFCIAction.setAssignation(new DataBinding<>(FROM_CONCEPT_INSTANCE));
		setFromFCIAction.doAction();
		ExpressionAction<?> expAction = (ExpressionAction<?>) setFromFCIAction.getBaseEditionAction();
		expAction.setExpression(new DataBinding<>(LinkScheme.FROM_TARGET_KEY));

		CreateEditionAction setToFCIAction = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null, this);
		setToFCIAction.setEditionActionClass(ExpressionAction.class);
		setToFCIAction.setAssignation(new DataBinding<>(TO_CONCEPT_INSTANCE));
		setToFCIAction.doAction();
		ExpressionAction<?> expAction2 = (ExpressionAction<?>) setToFCIAction.getBaseEditionAction();
		expAction2.setExpression(new DataBinding<>(LinkScheme.TO_TARGET_KEY));

		CreateEditionAction setPropertyAction = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null,
				this);
		setPropertyAction.setEditionActionClass(ExpressionAction.class);
		setPropertyAction
				.setAssignation(new DataBinding<>(FROM_CONCEPT_INSTANCE + "." + FMEFreeModel.conceptRoleName(fromFlexoConceptGR) + "." + getCreatedPropertyName()));
		setPropertyAction.doAction();
		ExpressionAction<?> expAction3 = (ExpressionAction<?>) setPropertyAction.getBaseEditionAction();
		expAction3.setExpression(new DataBinding<>(TO_CONCEPT_INSTANCE + "." + FMEFreeModel.conceptRoleName(toFlexoConceptGR)));

		CreateEditionAction createAddConnector = null;
		createAddConnector = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null, this);

		// createAddShape.actionChoice = CreateEditionActionChoice.ModelSlotSpecificAction;
		createAddConnector.setModelSlot(getFreeModel().getTypedDiagramModelSlot());
		createAddConnector.setEditionActionClass(AddConnector.class);
		createAddConnector.setAssignation(new DataBinding<>(CONNECTOR_ROLE_NAME));
		createAddConnector.doAction();

		// AssignationAction<DiagramShape> addShapeAssigment = (AssignationAction<DiagramShape>)
		// createAddShape.getNewEditionAction();
		AddConnector addConnector = (AddConnector) createAddConnector.getBaseEditionAction();
		addConnector.setFromShape(new DataBinding<>(LinkScheme.FROM_TARGET_KEY + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));
		addConnector.setToShape(new DataBinding<>(LinkScheme.TO_TARGET_KEY + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));

		/*DeletionScheme deletionScheme = returned.getDefaultDeletionScheme();
		
		CreateEditionAction createDeleteShape = null;
		if (ownerAction != null) {
			createDeleteShape = CreateEditionAction.actionType.makeNewEmbeddedAction(deletionScheme.getControlGraph(), null, ownerAction);
		}
		else {
			createDeleteShape = CreateEditionAction.actionType.makeNewAction(deletionScheme.getControlGraph(), null, editor);
		}
		
		createDeleteShape.setEditionActionClass(DeleteAction.class);
		createDeleteShape.doAction();
		
		DeleteAction<?> deleteShape = (DeleteAction<?>) createDeleteShape.getNewEditionAction();
		deleteShape.setObject(new DataBinding<>(SHAPE_ROLE_NAME));*/

		return returned;
	}

	private DiagramConnector createConnector() {

		ShapeRole startShapeRole = (ShapeRole) fromFlexoConceptGR.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		DiagramShape startShape = startShapeRole.getMetamodelElement();
		ShapeRole endShapeRole = (ShapeRole) toFlexoConceptGR.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		DiagramShape endShape = endShapeRole.getMetamodelElement();

		org.openflexo.technologyadapter.diagram.model.action.AddConnector addConnectorAction = org.openflexo.technologyadapter.diagram.model.action.AddConnector.actionType
				.makeNewEmbeddedAction(startShape, null, this);
		addConnectorAction.setNewConnectorName(getCreatedPropertyName());
		addConnectorAction.setFromShape(startShape);
		addConnectorAction.setToShape(endShape);

		addConnectorAction.setGraphicalRepresentation(
				(ConnectorGraphicalRepresentation) getFocusedObject().getGraphicalRepresentation().cloneObject());
		addConnectorAction.doAction();
		return addConnectorAction.getNewConnector();
	}

}
