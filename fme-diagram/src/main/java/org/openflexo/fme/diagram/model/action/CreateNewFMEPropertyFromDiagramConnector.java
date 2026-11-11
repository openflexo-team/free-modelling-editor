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

package org.openflexo.fme.diagram.model.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.diana.ConnectorGraphicalRepresentation;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FMEInspectorGenerator;
import org.openflexo.fme.model.FMENames;
import org.openflexo.fme.model.action.AbstractCreateNewFMEPropertyFromLink;
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
import org.openflexo.technologyadapter.diagram.fml.binding.LinkSchemeBindingModel;
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
public class CreateNewFMEPropertyFromDiagramConnector extends
		AbstractCreateNewFMEPropertyFromLink<CreateNewFMEPropertyFromDiagramConnector, DiagramConnector, FMEDiagramFreeModel, FMEDiagramFreeModelInstance> {

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

	private CreateNewFMEPropertyFromDiagramConnector(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected Object getStartElement() {
		return getFocusedObject().getStartShape();
	}

	@Override
	protected Object getEndElement() {
		return getFocusedObject().getEndShape();
	}

	/**
	 * A GR concept drawing such a link as a connector between the shapes of both concepts
	 */
	@Override
	protected void buildLinkRepresentation(String createdPropertyName) {
		buildConnectorGRFlexoConcept();
	}

	public static final String FROM_CONCEPT_INSTANCE = "fromConceptInstance";
	public static final String TO_CONCEPT_INSTANCE = "toConceptInstance";
	public static final String CONNECTOR_ROLE_NAME = "connector";

	private FlexoConcept buildConnectorGRFlexoConcept() {

		// Creates the concept
		CreateFlexoConcept action = CreateFlexoConcept.actionType
				.makeNewEmbeddedAction(getFromFlexoConceptGR().getDeclaringCompilationUnit().getVirtualModel(), null, this);
		String conceptName = FMENames.conceptName(getCreatedPropertyName()) + "ConnectorGR";
		action.setNewFlexoConceptName(conceptName);
		action.doAction();
		FlexoConcept returned = action.getNewFlexoConcept();

		// Create new FlexoConceptInstanceRole to store the start GR concept instance
		CreateFlexoConceptInstanceRole createConceptRole1 = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(returned, null,
				this);
		createConceptRole1.setRoleName(FROM_CONCEPT_INSTANCE);
		createConceptRole1.setFlexoConceptInstanceType(getFromFlexoConceptGR());
		createConceptRole1.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>("container"));
		createConceptRole1.doAction();

		// Create new FlexoConceptInstanceRole to store the end GR concept instance
		CreateFlexoConceptInstanceRole createConceptRole2 = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(returned, null,
				this);
		createConceptRole2.setRoleName(TO_CONCEPT_INSTANCE);
		createConceptRole2.setFlexoConceptInstanceType(getToFlexoConceptGR());
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
		createConnectorRole.setModelSlot(getFMEFreeModel().getTypedDiagramModelSlot());
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
		linkScheme.setFromTargetFlexoConcept(getFromFlexoConceptGR());
		linkScheme.setToTargetFlexoConcept(getToFlexoConceptGR());

		CreateEditionAction setFromFCIAction = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null,
				this);
		setFromFCIAction.setEditionActionClass(ExpressionAction.class);
		setFromFCIAction.setAssignation(new DataBinding<>(FROM_CONCEPT_INSTANCE));
		setFromFCIAction.doAction();
		ExpressionAction<?> expAction = (ExpressionAction<?>) setFromFCIAction.getBaseEditionAction();
		expAction.setExpression(new DataBinding<>(LinkSchemeBindingModel.FROM_TARGET));

		CreateEditionAction setToFCIAction = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null, this);
		setToFCIAction.setEditionActionClass(ExpressionAction.class);
		setToFCIAction.setAssignation(new DataBinding<>(TO_CONCEPT_INSTANCE));
		setToFCIAction.doAction();
		ExpressionAction<?> expAction2 = (ExpressionAction<?>) setToFCIAction.getBaseEditionAction();
		expAction2.setExpression(new DataBinding<>(LinkSchemeBindingModel.TO_TARGET));

		CreateEditionAction setPropertyAction = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null,
				this);
		setPropertyAction.setEditionActionClass(ExpressionAction.class);
		setPropertyAction
				.setAssignation(new DataBinding<>(FROM_CONCEPT_INSTANCE + "." + FMEFreeModel.conceptRoleName(getFromFlexoConceptGR()) + "." + getCreatedPropertyName()));
		setPropertyAction.doAction();
		ExpressionAction<?> expAction3 = (ExpressionAction<?>) setPropertyAction.getBaseEditionAction();
		expAction3.setExpression(new DataBinding<>(TO_CONCEPT_INSTANCE + "." + FMEFreeModel.conceptRoleName(getToFlexoConceptGR())));

		CreateEditionAction createAddConnector = null;
		createAddConnector = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null, this);

		// createAddShape.actionChoice = CreateEditionActionChoice.ModelSlotSpecificAction;
		createAddConnector.setModelSlot(getFMEFreeModel().getTypedDiagramModelSlot());
		createAddConnector.setEditionActionClass(AddConnector.class);
		createAddConnector.setAssignation(new DataBinding<>(CONNECTOR_ROLE_NAME));
		createAddConnector.doAction();

		// AssignationAction<DiagramShape> addShapeAssigment = (AssignationAction<DiagramShape>)
		// createAddShape.getNewEditionAction();
		AddConnector addConnector = (AddConnector) createAddConnector.getBaseEditionAction();
		addConnector.setFromShape(new DataBinding<>(LinkSchemeBindingModel.FROM_TARGET + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));
		addConnector.setToShape(new DataBinding<>(LinkSchemeBindingModel.TO_TARGET + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));

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

		ShapeRole startShapeRole = (ShapeRole) getFromFlexoConceptGR().getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		DiagramShape startShape = startShapeRole.getMetamodelElement();
		ShapeRole endShapeRole = (ShapeRole) getToFlexoConceptGR().getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
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
