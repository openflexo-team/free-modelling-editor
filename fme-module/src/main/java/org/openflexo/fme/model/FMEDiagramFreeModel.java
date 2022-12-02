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

package org.openflexo.fme.model;

import java.awt.Font;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.ParseException;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.diana.shapes.Rectangle;
import org.openflexo.diana.shapes.ShapeSpecification.ShapeType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateTechnologyRole;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot;
import org.openflexo.technologyadapter.diagram.fml.ConnectorRole;
import org.openflexo.technologyadapter.diagram.fml.DropScheme;
import org.openflexo.technologyadapter.diagram.fml.FMLControlledDiagramVirtualModelNature;
import org.openflexo.technologyadapter.diagram.fml.LinkScheme;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.fml.action.CreateDiagramPalette;
import org.openflexo.technologyadapter.diagram.fml.action.CreateFMLControlledDiagramPaletteElement;
import org.openflexo.technologyadapter.diagram.fml.editionaction.AddConnector;
import org.openflexo.technologyadapter.diagram.fml.editionaction.AddShape;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPalette;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPaletteElement;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramSpecification;
import org.openflexo.technologyadapter.diagram.model.Diagram;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramFactory;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.technologyadapter.diagram.rm.DiagramResource;

/**
 * A diagram-based {@link FMEFreeModel}
 * 
 * The base of a {@link FMEDiagramFreeModel} is a {@link VirtualModel} which aims to federate the {@link FMEConceptualModel} with a diagram
 * representation to elicitate new or existing concepts.
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@XMLElement
@ImplementationClass(FMEDiagramFreeModel.FMEDiagramFreeModelImpl.class)
public interface FMEDiagramFreeModel extends FMEFreeModel {

	public static final String DIAGRAM_MODEL_SLOT_NAME = "diagram";
	public static final String SHAPE_ROLE_NAME = "shape";
	public static final String CONNECTOR_ROLE_NAME = "connector";
	public static final String PALETTE_NAME = "Concepts";
	public static final int PALETTE_GRID_WIDTH = 50;
	public static final int PALETTE_GRID_HEIGHT = 40;
	public static final Font PALETTE_DEFAULT_TEXT_FONT = new Font("SansSerif", Font.PLAIN, 7);
	public static final Font PALETTE_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 11);
	public static final String DEFAULT_DIAGRAM_SPECIFICATION_FOLDER = "DiagramSpecification";

	public TypedDiagramModelSlot getTypedDiagramModelSlot();

	public DiagramSpecification getDiagramSpecification();

	public DiagramPaletteElement createPaletteElementForConcept(FlexoConcept grConcept, FlexoConcept concept,
			ShapeGraphicalRepresentation gr, FlexoAction<?, ?, ?> ownerAction) throws FlexoException;

	public DiagramPalette getConceptsPalette() throws FlexoException;

	public abstract class FMEDiagramFreeModelImpl extends FMEFreeModelImpl implements FMEDiagramFreeModel {

		private static final Logger logger = FlexoLogger.getLogger(FMEDiagramFreeModel.class.getPackage().getName());

		@Override
		public TypedDiagramModelSlot getTypedDiagramModelSlot() {
			return FMLControlledDiagramVirtualModelNature.getTypedDiagramModelSlot(getAccessedVirtualModel());
		}

		@Override
		public DiagramSpecification getDiagramSpecification() {
			try {
				return getTypedDiagramModelSlot().getMetaModelResource().getResourceData();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				e.printStackTrace();
			} catch (FlexoException e) {
				e.printStackTrace();
			}
			return null;
		}

		private DiagramShape createShape(String name, FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {
			return createShapeInDiagram(getDiagramSpecification().getDefaultExampleDiagram(), name, editor, ownerAction);
		}

		private static DiagramShape createShapeInDiagram(Diagram diagram, String name, FlexoEditor editor,
				FlexoAction<?, ?, ?> ownerAction) {
			org.openflexo.technologyadapter.diagram.model.action.AddShape addShapeAction;
			if (ownerAction != null) {
				addShapeAction = org.openflexo.technologyadapter.diagram.model.action.AddShape.actionType.makeNewEmbeddedAction(diagram,
						null, ownerAction);
			}
			else {
				addShapeAction = org.openflexo.technologyadapter.diagram.model.action.AddShape.actionType.makeNewAction(diagram, null,
						editor);
			}
			addShapeAction.setNewShapeName(name);
			DiagramFactory factory = ((DiagramResource) diagram.getResource()).getFactory();
			ShapeGraphicalRepresentation shapeGR = factory.newInstance(ShapeGraphicalRepresentation.class);
			Rectangle rectangleShape = factory.newInstance(Rectangle.class);
			shapeGR.setShapeSpecification(rectangleShape);
			addShapeAction.setGraphicalRepresentation(shapeGR);
			addShapeAction.doAction();
			return addShapeAction.getNewShape();
		}

		private static DiagramConnector createConnectorInDiagram(DiagramShape fromShape, DiagramShape toShape, String name,
				FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {
			org.openflexo.technologyadapter.diagram.model.action.AddConnector addConnectorAction;
			if (ownerAction != null) {
				addConnectorAction = org.openflexo.technologyadapter.diagram.model.action.AddConnector.actionType
						.makeNewEmbeddedAction(fromShape, null, ownerAction);
			}
			else {
				addConnectorAction = org.openflexo.technologyadapter.diagram.model.action.AddConnector.actionType.makeNewAction(fromShape,
						null, editor);
			}
			addConnectorAction.setFromShape(fromShape);
			addConnectorAction.setToShape(toShape);
			addConnectorAction.setNewConnectorName(name);
			addConnectorAction.doAction();
			return addConnectorAction.getNewConnector();
		}

		@Override
		public DiagramPalette getConceptsPalette() throws FlexoException {
			if (getDiagramSpecification().getPalette(PALETTE_NAME) == null) {
				// Should not happen, but...
				CreateDiagramPalette createPalette = CreateDiagramPalette.actionType.makeNewAction(getDiagramSpecification(), null, null);
				createPalette.setNewPaletteName(FMEDiagramFreeModel.PALETTE_NAME);
				createPalette.doAction();
				System.out.println("Palette has been created: " + createPalette.getNewPalette());
			}
			return getDiagramSpecification().getPalette(PALETTE_NAME);
		}

		@Override
		public DiagramPaletteElement createPaletteElementForConcept(FlexoConcept grConcept, FlexoConcept concept,
				ShapeGraphicalRepresentation gr, FlexoAction<?, ?, ?> ownerAction) throws FlexoException {

			System.out.println("Hop, on cree le palette element maintenant");

			CreateFMLControlledDiagramPaletteElement action = CreateFMLControlledDiagramPaletteElement.actionType
					.makeNewEmbeddedAction(grConcept.getOwningVirtualModel(), null, ownerAction);
			action.setPalette(getConceptsPalette());

			ShapeGraphicalRepresentation paletteElementGR = (ShapeGraphicalRepresentation) gr.cloneObject();
			int px, py;
			int index = getConceptsPalette().getElements().size();
			px = index % 3;
			py = index / 3;

			// FACTORY.applyDefaultProperties(gr);
			if (paletteElementGR.getShapeSpecification().getShapeType() == ShapeType.SQUARE
					|| paletteElementGR.getShapeSpecification().getShapeType() == ShapeType.CIRCLE) {
				paletteElementGR.setX(10);
				paletteElementGR.setY(10);
				paletteElementGR.setX(px * FMEDiagramFreeModel.PALETTE_GRID_WIDTH + 15);
				paletteElementGR.setY(py * FMEDiagramFreeModel.PALETTE_GRID_HEIGHT + 10);
				paletteElementGR.setWidth(30);
				paletteElementGR.setHeight(30);
			}
			else {
				paletteElementGR.setX(px * FMEDiagramFreeModel.PALETTE_GRID_WIDTH + 10);
				paletteElementGR.setY(py * FMEDiagramFreeModel.PALETTE_GRID_HEIGHT + 10);
				paletteElementGR.setWidth(40);
				paletteElementGR.setHeight(30);
			}
			paletteElementGR.setText(concept.getName());
			action.setNewElementName(concept.getName());

			action.setGraphicalRepresentation(paletteElementGR);
			action.setConcept(grConcept);
			if (grConcept.getFlexoBehaviours(DropScheme.class).size() > 0) {
				action.setDropScheme(grConcept.getFlexoBehaviours(DropScheme.class).get(0));
			}

			System.out.println("On cree le palette element");
			System.out.println("paletteElementGR=" + paletteElementGR);
			System.out.println("concept=" + concept);
			System.out.println("grConcept=" + grConcept);
			System.out.println("ds=" + action.getDropScheme());

			action.doAction();

			return action.getNewElement();

		}

		@Override
		protected void configureGRFlexoConcept(FlexoConcept returned, FlexoConcept concept, FlexoConcept containerConceptGR,
				FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {

			// Creates shape property
			CreateTechnologyRole createShapeRole = null;
			if (ownerAction != null) {
				createShapeRole = CreateTechnologyRole.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
			}
			else {
				createShapeRole = CreateTechnologyRole.actionType.makeNewAction(returned, null, editor);
			}
			createShapeRole.setModelSlot(getTypedDiagramModelSlot());
			createShapeRole.setRoleName(SHAPE_ROLE_NAME);
			createShapeRole.setFlexoRoleClass(ShapeRole.class);
			createShapeRole.doAction();
			ShapeRole role = (ShapeRole) createShapeRole.getNewFlexoRole();

			// Bind shapes's label to name property
			if (concept != null) {
				// If we are bound to a concept instance, use that name
				role.setLabel(new DataBinding<>(CONCEPT_ROLE_NAME + ".name"));
			}
			else {
				// Otherwise, this is the NoneGR, use primitive name
				role.setLabel(new DataBinding<>("name"));
			}

			// Init a default GR
			DiagramShape newShape = createShape(returned.getName(), editor, ownerAction);
			role.setMetamodelElement(newShape);

			// Create new DropScheme
			CreateFlexoBehaviour createDropScheme = null;
			if (ownerAction != null) {
				createDropScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
			}
			else {
				createDropScheme = CreateFlexoBehaviour.actionType.makeNewAction(returned, null, editor);
			}
			createDropScheme.setFlexoBehaviourName("drop");
			createDropScheme.setFlexoBehaviourClass(DropScheme.class);
			createDropScheme.doAction();
			DropScheme dropScheme = (DropScheme) createDropScheme.getNewFlexoBehaviour();
			dropScheme.setSkipConfirmationPanel(false);
			if (containerConceptGR == null) {
				dropScheme.setTopTarget(true);
			}
			else {
				dropScheme.setTargetFlexoConcept(containerConceptGR);
			}

			// Create new DropScheme parameter
			CreateGenericBehaviourParameter createDropSchemeParameter = null;
			if (ownerAction != null) {
				createDropSchemeParameter = CreateGenericBehaviourParameter.actionType.makeNewEmbeddedAction(dropScheme, null, ownerAction);
			}
			else {
				createDropSchemeParameter = CreateGenericBehaviourParameter.actionType.makeNewAction(dropScheme, null, editor);
			}
			createDropSchemeParameter.setParameterName(FMEConceptualModel.CONCEPT_NAME_PARAMETER);
			createDropSchemeParameter.setParameterType(String.class);
			createDropSchemeParameter.setWidgetType(WidgetType.TEXT_FIELD);

			createDropSchemeParameter.doAction();
			FlexoBehaviourParameter parameter = createDropSchemeParameter.getNewParameter();
			if (concept != null) {
				parameter.setDefaultValue(new DataBinding<String>("\"" + concept.getName() + "\""));
			}
			else {
				parameter.setDefaultValue(new DataBinding<String>("\"" + FMEConceptualModel.CONCEPT_NAME_PARAMETER + "\""));
			}

			if (concept != null) {

				CreateEditionAction createAddFlexoConceptInstance = null;
				if (ownerAction != null) {
					createAddFlexoConceptInstance = CreateEditionAction.actionType.makeNewEmbeddedAction(dropScheme.getControlGraph(), null,
							ownerAction);
				}
				else {
					createAddFlexoConceptInstance = CreateEditionAction.actionType.makeNewAction(dropScheme.getControlGraph(), null,
							editor);
				}
				createAddFlexoConceptInstance.setModelSlot(getSampleDataModelSlot());
				createAddFlexoConceptInstance.setEditionActionClass(ExpressionAction.class);
				createAddFlexoConceptInstance.setAssignation(new DataBinding<>(CONCEPT_ROLE_NAME));
				createAddFlexoConceptInstance.doAction();

				ExpressionAction newFCIAction = (ExpressionAction) createAddFlexoConceptInstance.getBaseEditionAction();
				List<DataBinding<?>> args = Collections.singletonList(new DataBinding<>("parameters.conceptName"));
				CreationSchemePathElement pathElement = (CreationSchemePathElement) dropScheme.getBindingFactory()
						.makeNewInstancePathElement(concept.getInstanceType(), null, concept.getCreationSchemes().get(0).getName(), args,
								newFCIAction);

				DataBinding expression = new DataBinding(newFCIAction, concept.getInstanceType(), BindingDefinitionType.GET);
				BindingPath bindingPath = null;

				try {
					if (containerConceptGR != null) {
						bindingPath = BindingPath.parse(DropScheme.TARGET_KEY + "." + FMEFreeModel.CONCEPT_ROLE_NAME, newFCIAction);
						bindingPath.addBindingPathElement(pathElement);
					}
					else {
						bindingPath = BindingPath.parse(SAMPLE_DATA_MODEL_SLOT_NAME, newFCIAction);
						// bindingPath = new BindingPath(Collections.singletonList(pathElement), newFCIAction,
						// FMLPrettyPrinter.getInstance());
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				bindingPath.addBindingPathElement(pathElement);

				expression.setExpression(bindingPath);
				newFCIAction.setExpression(expression);

				// Code replacing
				/*AddFlexoConceptInstance<?> addFCI = (AddFlexoConceptInstance<?>) createAddFlexoConceptInstance.getBaseEditionAction();
				addFCI.setCreationScheme(concept.getCreationSchemes().get(0));
				
				System.out.println(concept.getFMLPrettyPrint());
				System.out.println(dropScheme.getFMLPrettyPrint());
				
				AddFlexoConceptInstanceParameter addFCINameParam = addFCI.getParameter(FMEConceptualModel.CONCEPT_NAME_PARAMETER);
				addFCINameParam.setValue(new DataBinding<>("parameters.conceptName"));
				addFCI.setReceiver(new DataBinding<>(SAMPLE_DATA_MODEL_SLOT_NAME));
				if (containerConceptGR == null) {
					addFCI.setContainer(new DataBinding<>(SAMPLE_DATA_MODEL_SLOT_NAME));
				}
				else {
					addFCI.setContainer(new DataBinding<>(DropScheme.TARGET_KEY + "." + FMEFreeModel.CONCEPT_ROLE_NAME));
				}*/
			}
			else {
				CreateEditionAction givesNameAction = null;
				if (ownerAction != null) {
					givesNameAction = CreateEditionAction.actionType.makeNewEmbeddedAction(dropScheme.getControlGraph(), null, ownerAction);
				}
				else {
					givesNameAction = CreateEditionAction.actionType.makeNewAction(dropScheme.getControlGraph(), null, editor);
				}
				// givesNameAction.actionChoice = CreateEditionActionChoice.BuiltInAction;
				givesNameAction.setEditionActionClass(ExpressionAction.class);
				givesNameAction.setAssignation(new DataBinding<>(FMEFreeModel.NAME_ROLE_NAME));
				givesNameAction.doAction();

				AssignationAction<?> nameAssignation = (AssignationAction<?>) givesNameAction.getNewEditionAction();
				((ExpressionAction<?>) nameAssignation.getAssignableAction()).setExpression(new DataBinding<>("parameters.conceptName"));

			}

			CreateEditionAction createAddShape = null;
			if (ownerAction != null) {
				createAddShape = CreateEditionAction.actionType.makeNewEmbeddedAction(dropScheme.getControlGraph(), null, ownerAction);
			}
			else {
				createAddShape = CreateEditionAction.actionType.makeNewAction(dropScheme.getControlGraph(), null, editor);
			}
			// createAddShape.actionChoice = CreateEditionActionChoice.ModelSlotSpecificAction;
			createAddShape.setModelSlot(getTypedDiagramModelSlot());
			createAddShape.setEditionActionClass(AddShape.class);
			createAddShape.setAssignation(new DataBinding<>(SHAPE_ROLE_NAME));
			createAddShape.doAction();

			// AssignationAction<DiagramShape> addShapeAssigment = (AssignationAction<DiagramShape>)
			// createAddShape.getNewEditionAction();
			AddShape addShape = (AddShape) createAddShape.getBaseEditionAction();

			if (containerConceptGR == null) {
				addShape.setContainer(new DataBinding<>(DIAGRAM_MODEL_SLOT_NAME));
			}
			else {
				addShape.setContainer(new DataBinding<>(DropScheme.TARGET_KEY + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));
			}

			DeletionScheme deletionScheme = returned.getDefaultDeletionScheme();

			CreateEditionAction createDeleteShape = null;
			if (ownerAction != null) {
				createDeleteShape = CreateEditionAction.actionType.makeNewEmbeddedAction(deletionScheme.getControlGraph(), null,
						ownerAction);
			}
			else {
				createDeleteShape = CreateEditionAction.actionType.makeNewAction(deletionScheme.getControlGraph(), null, editor);
			}
			// createDeleteShape.actionChoice = CreateEditionActionChoice.BuiltInAction;
			// createDeleteShape.setModelSlot(getTypedDiagramModelSlot());
			createDeleteShape.setEditionActionClass(DeleteAction.class);
			createDeleteShape.doAction();

			DeleteAction<?> deleteShape = (DeleteAction<?>) createDeleteShape.getNewEditionAction();
			deleteShape.setObject(new DataBinding<>(SHAPE_ROLE_NAME));

		}

		@Override
		protected void configureGRRelationalFlexoConcept(FlexoConcept returned, FlexoConcept concept, FlexoConcept fromConceptGR,
				FlexoConcept toConceptGR, FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {

			// Creates connector property
			CreateTechnologyRole createConnectorRole = null;
			if (ownerAction != null) {
				createConnectorRole = CreateTechnologyRole.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
			}
			else {
				createConnectorRole = CreateTechnologyRole.actionType.makeNewAction(returned, null, editor);
			}
			createConnectorRole.setModelSlot(getTypedDiagramModelSlot());
			createConnectorRole.setRoleName(CONNECTOR_ROLE_NAME);
			createConnectorRole.setFlexoRoleClass(ConnectorRole.class);
			createConnectorRole.doAction();
			ConnectorRole role = (ConnectorRole) createConnectorRole.getNewFlexoRole();

			// Bind shapes's label to renderer
			role.setReadOnlyLabel(true);
			role.setLabel(new DataBinding<>(CONCEPT_ROLE_NAME + ".render"));

			// Sets connector
			ShapeRole fromShapeRole = (ShapeRole) fromConceptGR.getAccessibleProperty(SHAPE_ROLE_NAME);
			DiagramShape fromShape = fromShapeRole.getMetamodelElement();
			ShapeRole toShapeRole = (ShapeRole) toConceptGR.getAccessibleProperty(SHAPE_ROLE_NAME);
			DiagramShape toShape = toShapeRole.getMetamodelElement();

			DiagramConnector newConnector = createConnectorInDiagram(fromShape, toShape, returned.getName(), editor, ownerAction);
			role.setMetamodelElement(newConnector);

			// Create new LinkScheme
			CreateFlexoBehaviour createLinkScheme = null;
			if (ownerAction != null) {
				createLinkScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
			}
			else {
				createLinkScheme = CreateFlexoBehaviour.actionType.makeNewAction(returned, null, editor);
			}
			createLinkScheme.setFlexoBehaviourName("link");
			createLinkScheme.setFlexoBehaviourClass(LinkScheme.class);
			createLinkScheme.doAction();
			LinkScheme linkScheme = (LinkScheme) createLinkScheme.getNewFlexoBehaviour();
			linkScheme.setSkipConfirmationPanel(true);
			linkScheme.setFromTargetFlexoConcept(fromConceptGR);
			linkScheme.setToTargetFlexoConcept(toConceptGR);

			CreateEditionAction createAddFlexoConceptInstance = null;
			if (ownerAction != null) {
				createAddFlexoConceptInstance = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null,
						ownerAction);
			}
			else {
				createAddFlexoConceptInstance = CreateEditionAction.actionType.makeNewAction(linkScheme.getControlGraph(), null, editor);
			}
			createAddFlexoConceptInstance.setModelSlot(getSampleDataModelSlot());
			createAddFlexoConceptInstance.setEditionActionClass(AddFlexoConceptInstance.class);
			createAddFlexoConceptInstance.setAssignation(new DataBinding<>(CONCEPT_ROLE_NAME));
			createAddFlexoConceptInstance.doAction();
			AddFlexoConceptInstance<?> addFCI = (AddFlexoConceptInstance<?>) createAddFlexoConceptInstance.getBaseEditionAction();
			addFCI.setCreationScheme(concept.getCreationSchemes().get(0));
			AddFlexoConceptInstanceParameter addFCISourceConceptParam = addFCI.getParameter(FMEConceptualModel.FROM_CONCEPT_ROLE_NAME);
			addFCISourceConceptParam.setValue(new DataBinding<>(LinkScheme.FROM_TARGET_KEY + "." + FMEFreeModel.CONCEPT_ROLE_NAME));
			AddFlexoConceptInstanceParameter addFCIDestinationConceptParam = addFCI.getParameter(FMEConceptualModel.TO_CONCEPT_ROLE_NAME);
			addFCIDestinationConceptParam.setValue(new DataBinding<>(LinkScheme.TO_TARGET_KEY + "." + FMEFreeModel.CONCEPT_ROLE_NAME));
			addFCI.setReceiver(new DataBinding<>(SAMPLE_DATA_MODEL_SLOT_NAME));

			CreateEditionAction createAddConnector = null;
			if (ownerAction != null) {
				createAddConnector = CreateEditionAction.actionType.makeNewEmbeddedAction(linkScheme.getControlGraph(), null, ownerAction);
			}
			else {
				createAddConnector = CreateEditionAction.actionType.makeNewAction(linkScheme.getControlGraph(), null, editor);
			}
			// createAddShape.actionChoice = CreateEditionActionChoice.ModelSlotSpecificAction;
			createAddConnector.setModelSlot(getTypedDiagramModelSlot());
			createAddConnector.setEditionActionClass(AddConnector.class);
			createAddConnector.setAssignation(new DataBinding<>(CONNECTOR_ROLE_NAME));
			createAddConnector.doAction();

			AddConnector addConnector = (AddConnector) createAddConnector.getBaseEditionAction();

			addConnector.setFromShape(new DataBinding<>(LinkScheme.FROM_TARGET_KEY + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));
			addConnector.setToShape(new DataBinding<>(LinkScheme.TO_TARGET_KEY + "." + FMEDiagramFreeModel.SHAPE_ROLE_NAME));

			DeletionScheme deletionScheme = returned.getDefaultDeletionScheme();

			CreateEditionAction createDeleteConnector = null;
			if (ownerAction != null) {
				createDeleteConnector = CreateEditionAction.actionType.makeNewEmbeddedAction(deletionScheme.getControlGraph(), null,
						ownerAction);
			}
			else {
				createDeleteConnector = CreateEditionAction.actionType.makeNewAction(deletionScheme.getControlGraph(), null, editor);
			}
			createDeleteConnector.setEditionActionClass(DeleteAction.class);
			createDeleteConnector.doAction();

			DeleteAction<?> deleteConnector = (DeleteAction<?>) createDeleteConnector.getNewEditionAction();
			deleteConnector.setObject(new DataBinding<>(CONNECTOR_ROLE_NAME));

		}

	}

}
