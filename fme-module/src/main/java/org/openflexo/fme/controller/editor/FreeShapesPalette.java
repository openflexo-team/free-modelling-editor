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

package org.openflexo.fme.controller.editor;

import java.util.logging.Logger;

import org.openflexo.diana.BackgroundImageBackgroundStyle;
import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.diana.Drawing.ContainerNode;
import org.openflexo.diana.Drawing.DrawingTreeNode;
import org.openflexo.diana.ShapeGraphicalRepresentation.LocationConstraints;
import org.openflexo.diana.control.DianaInteractiveEditor.EditorTool;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.diana.shapes.ShapeSpecification.ShapeType;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.foundation.action.FlexoUndoManager.FlexoActionCompoundEdit;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.technologyadapter.diagram.controller.DiagramCst;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.CommonPalette;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.FMLControlledDiagramShape;
import org.openflexo.technologyadapter.diagram.model.Diagram;
import org.openflexo.technologyadapter.diagram.model.DiagramContainerElement;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.technologyadapter.diagram.model.action.AddShape;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoFIBController;

public class FreeShapesPalette extends CommonPalette {

	@SuppressWarnings("unused")
	private static final Logger logger = FlexoLogger.getLogger(FreeShapesPalette.class.getPackage().getName());

	public FreeShapesPalette(FreeModelDiagramEditor editor) {
		super(editor);
	}

	@Override
	public FreeModelDiagramEditor getEditor() {
		return (FreeModelDiagramEditor) super.getEditor();
	}

	@Override
	public boolean shouldAcceptDrop(DrawingTreeNode<?, ?> target) {
		return getEditor() != null && target instanceof ContainerNode && (target.getDrawable() instanceof Diagram
				|| target.getDrawable() instanceof DiagramShape || target.getDrawable() instanceof FMLControlledDiagramShape);
	}

	@Override
	public boolean handleBasicGraphicalRepresentationDrop(DrawingTreeNode<?, ?> target, ShapeGraphicalRepresentation gr,
			DianaPoint dropLocation, boolean applyCurrentForeground, boolean applyCurrentBackground, boolean applyCurrentTextStyle,
			boolean applyCurrentShadowStyle, boolean isImage, boolean resize) {

		if (getEditor() == null) {
			return false;
		}

		Object targetObject = target.getDrawable();

		ShapeGraphicalRepresentation clonedGR = (ShapeGraphicalRepresentation) gr.cloneObject();

		if (isImage) {

			if (clonedGR.getBackground() instanceof BackgroundImageBackgroundStyle) {
				((BackgroundImageBackgroundStyle) clonedGR.getBackground()).setImageFile(null);
			}

			FIBComponent fibComponent = getEditor().getFIBLibrary().retrieveFIBComponent(DiagramCst.IMPORT_IMAGE_FILE_DIALOG_FIB);
			JFIBDialog dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, clonedGR, FlexoFrame.getActiveFrame(), true,
					new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, getEditor().getFlexoController()));
			if (dialog.getStatus() != Status.VALIDATED) {
				return false;
			}
		}

		if (targetObject instanceof DiagramContainerElement) {
			DiagramContainerElement<?> container = (DiagramContainerElement<?>) target.getDrawable();
			return handleDropInDiagramContainerElement(container, clonedGR, dropLocation, applyCurrentForeground, applyCurrentBackground,
					applyCurrentTextStyle, applyCurrentShadowStyle, isImage, resize);
		}

		if (targetObject instanceof FMLControlledDiagramShape) {
			FMLControlledDiagramShape container = (FMLControlledDiagramShape) target.getDrawable();
			return handleDropInFMLControlledDiagramShape(container, clonedGR, dropLocation, applyCurrentForeground, applyCurrentBackground,
					applyCurrentTextStyle, applyCurrentShadowStyle, isImage, resize);
		}

		return false;
	}

	private ShapeGraphicalRepresentation prepareGR(ShapeGraphicalRepresentation gr, DianaPoint dropLocation, boolean applyCurrentForeground,
			boolean applyCurrentBackground, boolean applyCurrentTextStyle, boolean applyCurrentShadowStyle, boolean isImage,
			boolean resize) {

		ShapeGraphicalRepresentation shapeGR = getEditor().getFactory().makeShapeGraphicalRepresentation(gr);

		shapeGR.setIsReadOnly(false);
		shapeGR.setIsFocusable(true);
		shapeGR.setIsSelectable(true);
		shapeGR.setLocationConstraints(LocationConstraints.FREELY_MOVABLE);

		if (resize) {
			if (shapeGR.getShapeSpecification().getShapeType() == ShapeType.SQUARE
					|| shapeGR.getShapeSpecification().getShapeType() == ShapeType.CIRCLE) {
				shapeGR.setWidth(50);
				shapeGR.setHeight(50);
			}
			else {
				shapeGR.setWidth(60);
				shapeGR.setHeight(45);
			}
		}
		if (applyCurrentForeground) {
			shapeGR.setForeground(getEditor().getInspectedForegroundStyle().cloneStyle());
		}
		if (applyCurrentBackground) {
			shapeGR.setBackground(getEditor().getInspectedBackgroundStyle().cloneStyle());
		}
		if (applyCurrentTextStyle) {
			shapeGR.setTextStyle(getEditor().getInspectedTextStyle().cloneStyle());
		}
		if (applyCurrentShadowStyle) {
			shapeGR.setShadowStyle(getEditor().getInspectedShadowStyle().cloneStyle());
		}

		return shapeGR;
	}

	private boolean handleDropInDiagramContainerElement(DiagramContainerElement<?> container, ShapeGraphicalRepresentation gr,
			DianaPoint dropLocation, boolean applyCurrentForeground, boolean applyCurrentBackground, boolean applyCurrentTextStyle,
			boolean applyCurrentShadowStyle, boolean isImage, boolean resize) {

		logger.info("drop " + this + " in " + container);

		FlexoActionCompoundEdit edit = (FlexoActionCompoundEdit) getEditor().getUndoManager().startRecording("Dragging new Element");

		ShapeGraphicalRepresentation shapeGR = prepareGR(gr, dropLocation, applyCurrentForeground, applyCurrentBackground,
				applyCurrentTextStyle, applyCurrentShadowStyle, isImage, resize);

		DropShape action = DropShape.actionType.makeNewAction(container, null, getEditor().getFlexoController().getEditor());
		action.setDiagramFreeModelInstance(getEditor().getDiagramFreeModelInstance());
		action.setGraphicalRepresentation(shapeGR);
		action.setDropLocation(dropLocation);

		action.setCompoundEdit(edit);
		action.doAction();

		FlexoConceptInstance newFlexoConceptInstance = action.getNewFlexoConceptInstance();
		// DiagramShape shape = newFlexoConceptInstance.getFlexoActor(patternRole)

		// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new shape is FML-controlled
		container.getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, container);

		getEditor().setCurrentTool(EditorTool.SelectionTool);

		// getEditor().setSelectedObject(getEditor().getDrawing().getDrawingTreeNode(newShape));

		return action.hasActionExecutionSucceeded();
	}

	private boolean handleDropInFMLControlledDiagramShape(FMLControlledDiagramShape container, ShapeGraphicalRepresentation gr,
			DianaPoint dropLocation, boolean applyCurrentForeground, boolean applyCurrentBackground, boolean applyCurrentTextStyle,
			boolean applyCurrentShadowStyle, boolean isImage, boolean resize) {

		logger.info("drop " + this + " in " + container);
		System.out.println("FCI= " + container.getFlexoConceptInstance());

		FlexoConceptInstance conceptGR = container.getFlexoConceptInstance();
		if (conceptGR.getFlexoConcept().getName().equals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME)) {
			// OK, its a NoneGR
			// We will just add shape in container shape

		}
		else if (conceptGR.getFlexoConcept().getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME) != null) {
			// OK, its a concept GR
			// We will just add shape in container shape
		}

		FlexoActionCompoundEdit edit = (FlexoActionCompoundEdit) getEditor().getUndoManager().startRecording("Drop new shape in container");

		System.out.println("drop location=" + dropLocation);

		ShapeGraphicalRepresentation shapeGR = prepareGR(gr, dropLocation, applyCurrentForeground, applyCurrentBackground,
				applyCurrentTextStyle, applyCurrentShadowStyle, isImage, resize);

		AddShape action = AddShape.actionType.makeNewAction(container.getDiagramElement(), null,
				getEditor().getFlexoController().getEditor());
		action.setGraphicalRepresentation(shapeGR);
		action.setDropLocation(dropLocation);
		action.setNewShapeName(shapeGR.getText());
		if (action.getNewShapeName() == null) {
			action.setNewShapeName(getEditor().getFlexoController().getModuleLocales().localizedForKey("unnamed"));
		}

		action.setCompoundEdit(edit);
		action.doAction();

		// Unused DiagramShape newShape = action.getNewShape();

		/*DropShape action = DropShape.actionType.makeNewAction(container, null, getEditor().getFlexoController().getEditor());
		action.setDiagramFreeModelInstance(getEditor().getDiagramFreeModelInstance());
		action.setGraphicalRepresentation(shapeGR);
		action.setDropLocation(dropLocation);
		
		action.setCompoundEdit(edit);
		action.doAction();*/

		// FlexoConceptInstance newFlexoConceptInstance = action.getNewFlexoConceptInstance();

		// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new shape is FML-controlled
		container.getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, container);

		getEditor().setCurrentTool(EditorTool.SelectionTool);

		// getEditor().setSelectedObject(getEditor().getDrawing().getDrawingTreeNode(newShape));

		return action.hasActionExecutionSucceeded();
	}

}
