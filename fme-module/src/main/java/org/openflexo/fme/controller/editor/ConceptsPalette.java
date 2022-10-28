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

package org.openflexo.fme.controller.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import org.openflexo.diana.Drawing.DrawingTreeNode;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.fme.controller.editor.DynamicPalette.GraphicalRepresentationSet;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.ContextualPalette;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.DiagramEditor;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.FMLControlledDiagramEditor;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.FMLControlledDiagramShape;
import org.openflexo.technologyadapter.diagram.fml.FMLControlledDiagramVirtualModelNature;
import org.openflexo.technologyadapter.diagram.fml.FMLDiagramPaletteElementBinding;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPalette;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPaletteElement;
import org.openflexo.technologyadapter.diagram.model.DiagramContainerElement;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;

public class ConceptsPalette extends ContextualPalette {

	private static final Logger logger = Logger.getLogger(ConceptsPalette.class.getPackage().getName());

	private FMEDiagramFreeModelInstance freeModelInstance;

	public ConceptsPalette(DiagramPalette diagramPalette, FreeModelDiagramEditor editor, FMEDiagramFreeModelInstance freeModelInstance) {
		super(diagramPalette, editor);
		this.freeModelInstance = freeModelInstance;
	}

	public FMEDiagramFreeModelInstance getFreeModelInstance() {
		return freeModelInstance;
	}

	public void setFreeModelInstance(FMEDiagramFreeModelInstance freeModelInstance) {
		if ((freeModelInstance == null && this.freeModelInstance != null)
				|| (freeModelInstance != null && !freeModelInstance.equals(this.freeModelInstance))) {
			FMEDiagramFreeModelInstance oldValue = this.freeModelInstance;
			this.freeModelInstance = freeModelInstance;
			getPropertyChangeSupport().firePropertyChange("freeModelInstance", oldValue, freeModelInstance);
		}
	}

	@Override
	public void delete() {
		super.delete();
	}

	@Override
	public FreeModelDiagramEditor getEditor() {
		return (FreeModelDiagramEditor) super.getEditor();
	}

	@Override
	public boolean handleFMLControlledDrop(DrawingTreeNode<?, ?> target, DiagramPaletteElement paletteElement, DianaPoint dropLocation,
			FMLControlledDiagramEditor editor) {

		if (getEditor() == null) {
			return false;
		}

		Object targetObject = target.getDrawable();

		if (targetObject instanceof DiagramContainerElement) {
			DiagramContainerElement<?> rootContainer = (DiagramContainerElement<?>) target.getDrawable();
			return handleFMLControlledDropInDiagramContainerElement(rootContainer, paletteElement, dropLocation, editor);
		}

		if (targetObject instanceof FMLControlledDiagramShape) {
			FMLControlledDiagramShape container = (FMLControlledDiagramShape) target.getDrawable();
			return handleFMLControlledDropInFMLControlledDiagramShape(container, paletteElement, dropLocation, editor);
		}

		return false;

	}

	private boolean handleFMLControlledDropInDiagramContainerElement(DiagramContainerElement<?> rootContainer,
			DiagramPaletteElement paletteElement, DianaPoint dropLocation, FMLControlledDiagramEditor editor) {

		FMLRTVirtualModelInstance vmi = editor.getVirtualModelInstance();
		TypedDiagramModelSlot ms = FMLControlledDiagramVirtualModelNature.getTypedDiagramModelSlot(vmi.getVirtualModel());
		FMLDiagramPaletteElementBinding binding = ms.getPaletteElementBinding(paletteElement);
		// DropScheme dropScheme = binding.getDropScheme();

		DropShape action = DropShape.actionType.makeNewAction(rootContainer, null, getEditor().getFlexoController().getEditor());
		action.setDiagramFreeModelInstance(getEditor().getDiagramFreeModelInstance());
		action.setGRConcept(binding.getBoundFlexoConcept());
		action.setDropLocation(dropLocation);

		action.doAction();

		// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new shape is FML-controlled
		rootContainer.getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, rootContainer);
		// FlexoConceptInstance newFlexoConceptInstance = action.getNewFlexoConceptInstance();
		// System.out.println("Created newFlexoConceptInstance:" + newFlexoConceptInstance);

		return action.hasActionExecutionSucceeded();

	}

	private boolean handleFMLControlledDropInFMLControlledDiagramShape(FMLControlledDiagramShape container,
			DiagramPaletteElement paletteElement, DianaPoint dropLocation, FMLControlledDiagramEditor editor) {

		FMLRTVirtualModelInstance vmi = editor.getVirtualModelInstance();
		TypedDiagramModelSlot ms = FMLControlledDiagramVirtualModelNature.getTypedDiagramModelSlot(vmi.getVirtualModel());
		FMLDiagramPaletteElementBinding binding = ms.getPaletteElementBinding(paletteElement);
		// DropScheme dropScheme = binding.getDropScheme();

		DropShape action = DropShape.actionType.makeNewAction(container.getDiagramElement(), null,
				getEditor().getFlexoController().getEditor());
		action.setDiagramFreeModelInstance(getEditor().getDiagramFreeModelInstance());
		action.setGRConcept(binding.getBoundFlexoConcept());
		action.setDropLocation(dropLocation);
		action.setContainer(container.getFlexoConceptInstance());

		action.doAction();

		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", false, true);
		freeModelInstance.getPropertyChangeSupport().firePropertyChange("getEmbeddedInstances(FlexoConceptInstance)", false, true);

		// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new shape is FML-controlled
		container.getDiagramElement().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null,
				container.getDiagramElement());
		// FlexoConceptInstance newFlexoConceptInstance = action.getNewFlexoConceptInstance();
		// System.out.println("Created newFlexoConceptInstance:" + newFlexoConceptInstance);

		return action.hasActionExecutionSucceeded();

	}

	@Override
	protected ContextualPaletteElement makePaletteElement(final DiagramPaletteElement element, DiagramEditor editor) {
		if (editor instanceof FMLControlledDiagramEditor) {
			FMLRTVirtualModelInstance vmi = ((FMLControlledDiagramEditor) editor).getVirtualModelInstance();
			TypedDiagramModelSlot ms = FMLControlledDiagramVirtualModelNature.getTypedDiagramModelSlot(vmi.getVirtualModel());
			if (ms != null) {
				FMLDiagramPaletteElementBinding binding = ms.getPaletteElementBinding(element);
				if (binding != null) {
					FlexoConcept concept = binding.getBoundFlexoConcept();
					if (concept != null) {
						final ShapeRole conceptShapeRole = (ShapeRole) concept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
						if (conceptShapeRole != null) {
							conceptShapeRole.getGraphicalRepresentation().getPropertyChangeSupport()
									.addPropertyChangeListener(new PropertyChangeListener() {
										@Override
										public void propertyChange(PropertyChangeEvent event) {
											if (element.getGraphicalRepresentation() != null && conceptShapeRole != null
													&& conceptShapeRole.getGraphicalRepresentation() != null) {
												element.getGraphicalRepresentation().setsWith(conceptShapeRole.getGraphicalRepresentation(),
														GraphicalRepresentationSet.IGNORED_PROPERTIES);
											}
										}
									});
						}
					}
				}
			}
		}
		return super.makePaletteElement(element, editor);
	}

	public class FreeConceptPaletteElement extends ContextualPaletteElement {
		public FreeConceptPaletteElement(DiagramPaletteElement aPaletteElement) {
			super(aPaletteElement);

			// if (aPaletteElement.getD)

		}
	}
}
