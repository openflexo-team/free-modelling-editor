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

import javax.swing.JTabbedPane;

import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.diana.Drawing.ContainerNode;
import org.openflexo.diana.ShapeGraphicalRepresentation.LocationConstraints;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.diana.shapes.ShapeSpecification.ShapeType;
import org.openflexo.diana.swing.control.SwingToolFactory;
import org.openflexo.diana.swing.control.tools.JDianaPalette;
import org.openflexo.fme.controller.FreeModelPasteHandler;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.foundation.action.FlexoUndoManager.FlexoActionCompoundEdit;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.technologyadapter.diagram.controller.DiagramCst;
import org.openflexo.technologyadapter.diagram.controller.action.FMLControlledDiagramPasteHandler;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.DiagramEditorPaletteModel;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.ContextualPalette;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.FMLControlledDiagramEditor;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPalette;
import org.openflexo.technologyadapter.diagram.model.DiagramContainerElement;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Editor of a FreeModel diagram<br>
 * There is a FreeMetaModel who has a VirtualModel controlling the edition of the diagram in FML-controlled mode
 * 
 * @author sylvain
 * 
 */
public class FreeModelDiagramEditor extends FMLControlledDiagramEditor {

	private static final Logger logger = Logger.getLogger(FreeModelDiagramEditor.class.getPackage().getName());

	private final FMEDiagramFreeModelInstance diagramFreeModelInstance;

	private String conceptFilter;

	private final FreeModelPasteHandler freeModelPasteHandler;

	private final DynamicPalette dynamicPalette;
	private final JDianaPalette dynamicPaletteComponent;

	public FreeModelDiagramEditor(FMEDiagramFreeModelInstance diagramFreeModelInstance, boolean readOnly, FlexoController controller,
			SwingToolFactory swingToolFactory) {
		super(diagramFreeModelInstance.getAccessedVirtualModelInstance(), readOnly, controller, swingToolFactory);
		this.diagramFreeModelInstance = diagramFreeModelInstance;
		dynamicPalette = new DynamicPalette(this);
		dynamicPaletteComponent = swingToolFactory.makeDianaPalette(dynamicPalette);
		dynamicPaletteComponent.attachToEditor(this);

		for (ContextualPalette contextualPalette : getContextualPalettes()) {
			if (contextualPalette instanceof ConceptsPalette) {
				((ConceptsPalette) contextualPalette).setFreeModelInstance(diagramFreeModelInstance);
			}

		}

		conceptFilter = "*";
		// We have to switch properly between those paste handlers
		// AND do not forget to destroy them
		freeModelPasteHandler = new FreeModelPasteHandler(diagramFreeModelInstance, this);
	}

	@Override
	public FMLControlledDiagramPasteHandler getPasteHandler() {
		return freeModelPasteHandler;
	}

	@Override
	public void delete() {
		getFlexoController().getEditingContext().unregisterPasteHandler(freeModelPasteHandler);
		super.delete();
	}

	public FMEDiagramFreeModelInstance getDiagramFreeModelInstance() {
		return diagramFreeModelInstance;
	}

	@Override
	public String getCommonPaletteTitle() {
		return "Free shapes";
	}

	@Override
	public DiagramEditorPaletteModel makeCommonPalette() {
		return new FreeShapesPalette(this);
	}

	@Override
	public ContextualPalette makeContextualPalette(DiagramPalette palette) {
		return new ConceptsPalette(palette, this, getDiagramFreeModelInstance());
	}

	@Override
	protected JTabbedPane makePaletteView() {

		JTabbedPane returned = super.makePaletteView();

		returned.add(dynamicPaletteComponent.getPaletteViewInScrollPane(), "Used shapes", 0);

		if (getDiagram().getShapes().size() > 0) {
			if (getDiagramFreeModelInstance().getAccessedVirtualModelInstance().getVirtualModel().getFlexoConcepts().size() > 1) {
				// In this case, we should activate the concept palette (the second one)
				returned.setSelectedIndex(1);
			}
			else {
				// In this case, activate used shape palette
				returned.setSelectedIndex(0);
				activatePalette(dynamicPaletteComponent);
			}
		}
		else {
			// Empty diagram, activate free shapes
			returned.setSelectedIndex(2);
			activatePalette(getCommonPalette());
		}
		return returned;
	}

	public String getConceptFilter() {
		return conceptFilter;
	}

	public void setConceptFilter(String conceptFilter) {
		this.conceptFilter = conceptFilter;
	}

	@Override
	public boolean handleNewShapeCreation(ShapeGraphicalRepresentation shapeGR, ContainerNode<?, ?> parentNode, DianaPoint dropLocation,
			boolean applyCurrentForeground, boolean applyCurrentBackground, boolean applyCurrentTextStyle, boolean applyCurrentShadowStyle,
			boolean isImage, boolean resize) {

		/*if (true) {
			return super.handleNewShapeCreation(shapeGR, parentNode, dropLocation, applyCurrentForeground, applyCurrentBackground,
					applyCurrentTextStyle, applyCurrentShadowStyle, isImage, resize);
		}*/

		DiagramContainerElement<?> container = (DiagramContainerElement<?>) parentNode.getDrawable();

		// logger.info("dragging " + this + " in " + container);

		FlexoActionCompoundEdit edit = (FlexoActionCompoundEdit) getUndoManager().startRecording("Making new shape");

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
			shapeGR.setForeground(getInspectedForegroundStyle().cloneStyle());
		}
		if (applyCurrentBackground) {
			shapeGR.setBackground(getInspectedBackgroundStyle().cloneStyle());
		}
		if (applyCurrentTextStyle) {
			shapeGR.setTextStyle(getInspectedTextStyle().cloneStyle());
		}
		if (applyCurrentShadowStyle) {
			shapeGR.setShadowStyle(getInspectedShadowStyle().cloneStyle());
		}

		// shapeGR.setX(dropLocation.x);
		// shapeGR.setY(dropLocation.y);

		if (isImage) {
			FIBComponent fibComponent = getFIBLibrary().retrieveFIBComponent(DiagramCst.IMPORT_IMAGE_FILE_DIALOG_FIB);
			JFIBDialog dialog = JFIBDialog.instanciateAndShowDialog(fibComponent, shapeGR, FlexoFrame.getActiveFrame(), true,
					new FlexoFIBController(fibComponent, SwingViewFactory.INSTANCE, getFlexoController()));
			if (dialog.getStatus() == Status.CANCELED) {
				return false;
			}
		}

		DropShape action = DropShape.actionType.makeNewAction(container, null, getFlexoController().getEditor());
		action.setDiagramFreeModelInstance(getDiagramFreeModelInstance());
		action.setGraphicalRepresentation(shapeGR);
		action.setDropLocation(dropLocation);

		action.setCompoundEdit(edit);
		action.doAction();

		FlexoConceptInstance newFlexoConceptInstance = action.getNewFlexoConceptInstance();
		// DiagramShape shape = newFlexoConceptInstance.getFlexoActor(patternRole)

		setCurrentTool(EditorTool.SelectionTool);

		// getEditor().setSelectedObject(getEditor().getDrawing().getDrawingTreeNode(newShape));

		return action.hasActionExecutionSucceeded();

	}

}
