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

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.diana.BackgroundImageBackgroundStyle;
import org.openflexo.diana.Drawing.ContainerNode;
import org.openflexo.diana.Drawing.DrawingTreeNode;
import org.openflexo.diana.GRProperty;
import org.openflexo.diana.GraphicalRepresentation;
import org.openflexo.diana.PaletteElementSpecification;
import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.diana.control.PaletteElement;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.diana.shapes.ShapeSpecification.ShapeType;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.DiagramEditorPaletteModel;
import org.openflexo.technologyadapter.diagram.controller.diagrameditor.FMLControlledDiagramShape;
import org.openflexo.technologyadapter.diagram.model.DiagramContainerElement;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

public class DynamicPalette extends DiagramEditorPaletteModel implements PropertyChangeListener {

	private static final Logger logger = FlexoLogger.getLogger(DynamicPalette.class.getPackage().getName());

	private static final int GRID_WIDTH = 50;
	private static final int GRID_HEIGHT = 40;
	public static final Font DEFAULT_TEXT_FONT = new Font("SansSerif", Font.PLAIN, 7);
	public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 11);

	// public static final String SHAPE_ADDED = "ShapeAdded";
	// public static final String SHAPE_REMOVED = "ShapeRemoved";

	// private List<PaletteElement>

	// private final Hashtable<ConceptGRAssociation, PaletteElement> elementsForAssociations;

	public DynamicPalette(FreeModelDiagramEditor editor) {
		super(editor, "dynamic_palette", 200, 200, 40, 30, 10, 10);
		// super(editor, 200, 200, "dynamic_palette");
		getEditor().getDiagram().getPropertyChangeSupport().addPropertyChangeListener(this);
		// elementsForAssociations = new Hashtable<ConceptGRAssociation, PaletteElement>();
		update();
	}

	@Override
	public void delete() {
		getEditor().getDiagramFreeModelInstance().getPropertyChangeSupport().removePropertyChangeListener(this);
		super.delete();
	}

	@SuppressWarnings("serial")
	public static class GraphicalRepresentationSet<T extends FlexoObject> extends HashMap<GraphicalRepresentation, List<T>> {

		public static final GRProperty<?>[] IGNORED_PROPERTIES = { GraphicalRepresentation.IDENTIFIER, GraphicalRepresentation.TEXT,
				GraphicalRepresentation.IS_READ_ONLY, GraphicalRepresentation.IS_FOCUSABLE, GraphicalRepresentation.IS_SELECTABLE,
				ShapeGraphicalRepresentation.X, ShapeGraphicalRepresentation.Y, ShapeGraphicalRepresentation.WIDTH,
				ShapeGraphicalRepresentation.HEIGHT, ShapeGraphicalRepresentation.LOCATION_CONSTRAINTS,
				ShapeGraphicalRepresentation.Y_CONSTRAINTS, ShapeGraphicalRepresentation.X_CONSTRAINTS,
				ShapeGraphicalRepresentation.HEIGHT_CONSTRAINTS, ShapeGraphicalRepresentation.WIDTH_CONSTRAINTS,
				ShapeGraphicalRepresentation.MINIMAL_HEIGHT, ShapeGraphicalRepresentation.MAXIMAL_HEIGHT,
				ShapeGraphicalRepresentation.MAXIMAL_WIDTH, ShapeGraphicalRepresentation.MINIMAL_HEIGHT,
				ShapeGraphicalRepresentation.ALLOW_TO_LEAVE_BOUNDS, ShapeGraphicalRepresentation.SELECTED_BACKGROUND,
				ShapeGraphicalRepresentation.FOCUSED_FOREGROUND };

		public static <T> T valueForParameter(GraphicalRepresentation gr, GRProperty<T> parameter) {
			if (gr.hasKey(parameter.getName())) {
				return (T) gr.objectForKey(parameter.getName());
			}
			return null;
		}

		public static boolean equals(GraphicalRepresentation gr1, GraphicalRepresentation gr2) {
			if (!gr1.getClass().equals(gr2.getClass())) {
				return false;
			}
			for (GRProperty<?> p : GRProperty.getGRParameters(gr1.getClass())) {
				boolean isToBeIgnored = false;
				for (GRProperty<?> ignoredP : IGNORED_PROPERTIES) {
					if (p.getName().equals(ignoredP.getName())) {
						isToBeIgnored = true;
						break;
					}
				}
				if (!isToBeIgnored) {
					Object value1 = valueForParameter(gr1, p);
					Object value2 = valueForParameter(gr2, p);
					if (value1 == null) {
						if (value2 != null) {
							// System.out.println("Differs 1 " + value1 + " and " + value2 + " for " + p);
							return false;
						}
					}
					else {
						if (value1 instanceof AccessibleProxyObject) {
							if (!((AccessibleProxyObject) value1).equalsObject(value2) && !asSameBackgroungImage(value1, value2)) {
								// System.out.println("Differs 2 " + value1 + " and " + value2 + " for " + p);
								return false;
							}
						}
						/*else if(value1 instanceof DataBinding){
							try {
								if(((DataBinding) value1).getBindingValue(gr1.get)!=((DataBinding) value2).getBindingValue(null)){
									return false;
								}
							} catch (TypeMismatchException e) {
								e.printStackTrace();
							} catch (NullReferenceException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}*/
						else {
							if (!value1.equals(value2)) {
								// System.out.println("Differs 3 " + value1 + " and " + value2 + " for " + p);
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		// LOW / MODULES-121 Special case to solve the problem of palette element duplication for shape with images.
		// Our current macanims to retrieve image resources instanciate a new FileResource each time
		// it locates it, thus for a same file, many FileResources are instanciated and so palette elements are duplicated.
		// This should be solve in 1.7, for instance by createing a dedicated technology adapter for images.
		// For now, just adress the problem rapidly
		private static boolean asSameBackgroungImage(Object value1, Object value2) {
			if (value1 instanceof BackgroundImageBackgroundStyle && value2 instanceof BackgroundImageBackgroundStyle) {
				BackgroundImageBackgroundStyle bibs1 = (BackgroundImageBackgroundStyle) value1;
				BackgroundImageBackgroundStyle bibs2 = (BackgroundImageBackgroundStyle) value2;
				try {
					if (bibs1.getImageFile() != null && bibs2.getImageFile() != null
							&& bibs1.getImageFile().getCanonicalPath().equals(bibs2.getImageFile().getCanonicalPath())) {
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		public T put(GraphicalRepresentation key, T value) {
			List<T> list;
			for (GraphicalRepresentation gr : keySet()) {
				if (equals(key, gr)) {
					// Found an existing list of FlexoObject which has same graphical representation
					list = get(gr);
					if (!list.contains(value)) {
						list.add(value);
						return value;
					}
				}
			}
			list = new ArrayList<>();
			put(key, list);
			list.add(value);
			return value;
		}

	}

	private final GraphicalRepresentationSet<DiagramElement<?>> representedElements = new GraphicalRepresentationSet<>();

	@Override
	public void addElement(PaletteElement element) {
		super.addElement(element);
	}

	@Override
	public void removeElement(PaletteElement element) {
		super.removeElement(element);
		element.delete();
	};

	/*private PaletteElement makePaletteElement(final ShapeGraphicalRepresentation prototypeGR, List<DiagramElement<?>> diagramElements) {
	
		final ShapeGraphicalRepresentation gr = (ShapeGraphicalRepresentation) prototypeGR.cloneObject();
		return new DynamicPaletteElement(gr, diagramElements);
	}*/

	@Override
	protected DynamicPaletteElement makePaletteElement(PaletteElementSpecification paletteElement) {
		return (DynamicPaletteElement) super.makePaletteElement(paletteElement);
	}

	private boolean isUpdating = false;

	public void update() {

		isUpdating = true;

		try {
			GraphicalRepresentationSet<DiagramElement<?>> diagramGRs = new GraphicalRepresentationSet<>();

			// For each existing DiagramElement which is not deleted:
			for (DiagramElement<?> e : getEditor().getDiagramFreeModelInstance().getDiagram().getDescendants()) {
				if (!e.isDeleted()) {
					diagramGRs.put(e.getGraphicalRepresentation(), e);
				}
			}

			List<PaletteElement> elementsToAdd = new ArrayList<>();
			List<PaletteElement> elementsToRemove = new ArrayList<>(getElements());

			for (GraphicalRepresentation key : diagramGRs.keySet()) {
				// We iterate here on each different Shape GR
				if (key instanceof ShapeGraphicalRepresentation) {
					DynamicPaletteElement existingElement = null;
					for (PaletteElement e : getElements()) {
						if (GraphicalRepresentationSet.equals(e.getGraphicalRepresentation(), key)) {
							existingElement = (DynamicPaletteElement) e;
							break;
						}
					}
					if (existingElement != null) {
						// Fine, nothing to do for this one
						elementsToRemove.remove(existingElement);
						// System.out.println("Found existing " + existingElement);
						if (existingElement instanceof DynamicPaletteElement) {
							existingElement.updateDiagramElements(diagramGRs.get(key));
						}
					}
					else {

						PaletteElementSpecification newSpec = getEditor().getFactory().newInstance(PaletteElementSpecification.class);
						newSpec.setGraphicalRepresentation(
								(ShapeGraphicalRepresentation) ((ShapeGraphicalRepresentation) key).cloneObject());
						existingElement = makePaletteElement(newSpec);
						existingElement.setDiagramElements(diagramGRs.get(key));
						// existingElement = makePaletteElement((ShapeGraphicalRepresentation) key, diagramGRs.get(key));
						elementsToAdd.add(existingElement);
					}
				}
			}

			for (PaletteElement e : elementsToRemove) {
				// System.out.println("Removing: " + e);
				removeElement(e);
			}
			for (PaletteElement e : elementsToAdd) {
				// System.out.println("Adding: " + e);
				addElement(e);
			}

			for (PaletteElement e : getElements()) {
				int px, py;
				int index = getElements().indexOf(e);

				px = index % 3;
				py = index / 3;

				if (e.getGraphicalRepresentation() != null) {
					if (e.getGraphicalRepresentation().getShapeSpecification() != null
							&& (e.getGraphicalRepresentation().getShapeSpecification().getShapeType() == ShapeType.SQUARE
									|| e.getGraphicalRepresentation().getShapeSpecification().getShapeType() == ShapeType.CIRCLE)) {
						e.getGraphicalRepresentation().setX(px * GRID_WIDTH + 15);
						e.getGraphicalRepresentation().setY(py * GRID_HEIGHT + 10);
						e.getGraphicalRepresentation().setWidth(30);
						e.getGraphicalRepresentation().setHeight(30);
					}
					else {
						e.getGraphicalRepresentation().setX(px * GRID_WIDTH + 10);
						e.getGraphicalRepresentation().setY(py * GRID_HEIGHT + 10);
						e.getGraphicalRepresentation().setWidth(40);
						e.getGraphicalRepresentation().setHeight(30);
					}
				}
			}
		} finally {
			isUpdating = false;
		}

	}

	@Override
	public FreeModelDiagramEditor getEditor() {
		return (FreeModelDiagramEditor) super.getEditor();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DiagramContainerElement.SHAPES)) {
			update();
		}
	}

	@Override
	protected PaletteElement buildPaletteElement(PaletteElementSpecification paletteElement) {
		return new DynamicPaletteElement(paletteElement.getGraphicalRepresentation());
	}

	private boolean handleDrop(DrawingTreeNode<?, ?> target, DynamicPaletteElement paletteElement, DianaPoint dropLocation) {
		if (getEditor() == null) {
			return false;
		}

		if (paletteElement.getFlexoConcept() != null) {

			DiagramContainerElement<?> rootContainer = (DiagramContainerElement<?>) target.getDrawable();
			// Unused FMLRTVirtualModelInstance vmi =
			getEditor().getVirtualModelInstance();
			DropShape action = DropShape.actionType.makeNewAction(rootContainer, null, getEditor().getFlexoController().getEditor());
			action.setDiagramFreeModelInstance(getEditor().getDiagramFreeModelInstance());
			action.setGRConcept(paletteElement.getFlexoConcept());
			action.setDropLocation(dropLocation);
			action.setGraphicalRepresentation(paletteElement.getGraphicalRepresentation());
			action.setTargetSize(paletteElement.getInitialDimensions());
			action.doAction();

			// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
			// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
			// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
			// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
			// sure that the Drawing can discover that the new shape is FML-controlled
			rootContainer.getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, rootContainer);

			// Unused FlexoConceptInstance newFlexoConceptInstance =
			action.getNewFlexoConceptInstance();
			return action.hasActionExecutionSucceeded();
		}

		else {
			// TODO: handle pure GR drop
			return false;
		}
	}

	@SuppressWarnings("serial")
	public class DynamicPaletteElement implements PaletteElement, PropertyChangeListener {

		private final List<DiagramElement<?>> diagramElements;
		private final ShapeGraphicalRepresentation gr;
		private final Dimension initialDimensions;

		public DynamicPaletteElement(ShapeGraphicalRepresentation gr) {
			this.gr = gr;
			this.diagramElements = new ArrayList<>();
			// If this is an image, fit the shape for a better rendering
			if (gr.getBackground() instanceof BackgroundImageBackgroundStyle) {
				((BackgroundImageBackgroundStyle) gr.getBackground()).setFitToShape(true);
			}
			// Store initial gr dimensions
			initialDimensions = new Dimension((int) gr.getWidth(), (int) gr.getHeight());
		}

		public void setDiagramElements(List<DiagramElement<?>> diagramElements) {
			this.diagramElements.addAll(diagramElements);
			for (DiagramElement<?> el : diagramElements) {
				if (el.getGraphicalRepresentation() != null && el.getGraphicalRepresentation().getPropertyChangeSupport() != null) {
					el.getGraphicalRepresentation().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean acceptDragging(DrawingTreeNode<?, ?> target) {
			return getEditor() != null && target instanceof ContainerNode;
		}

		@Override
		public boolean elementDragged(DrawingTreeNode<?, ?> target, DianaPoint dropLocation) {
			return handleDrop(target, this, dropLocation);
		}

		@Override
		public ShapeGraphicalRepresentation getGraphicalRepresentation() {
			return gr;
		}

		public Dimension getInitialDimensions() {
			return initialDimensions;
		}

		@Override
		public void delete(Object... context) {
			for (DiagramElement<?> el : diagramElements) {
				if (el.getGraphicalRepresentation() != null && el.getGraphicalRepresentation().getPropertyChangeSupport() != null) {
					el.getGraphicalRepresentation().getPropertyChangeSupport().removePropertyChangeListener(this);
				}
			}
			// Dont do this, otherwise some other unexpected GR will be deleted
			// gr.delete();
		}

		/**
		 * Called when PaletteElement was kept after an update, but where the list of DiagramElement has changed (this shape still exists,
		 * but the list of elements having this shape is no more the same)
		 * 
		 * @param updatedElements
		 */
		private void updateDiagramElements(List<DiagramElement<?>> updatedElements) {

			List<DiagramElement<?>> elementsToAdd = new ArrayList<>();
			List<DiagramElement<?>> elementsToRemove = new ArrayList<>(diagramElements);

			for (DiagramElement<?> e : updatedElements) {
				if (diagramElements.contains(e)) {
					elementsToRemove.remove(e);
				}
				else {
					elementsToAdd.add(e);
				}
			}

			for (DiagramElement<?> e : elementsToRemove) {
				e.getGraphicalRepresentation().getPropertyChangeSupport().removePropertyChangeListener(this);
				diagramElements.remove(e);
			}

			for (DiagramElement<?> e : elementsToAdd) {
				e.getGraphicalRepresentation().getPropertyChangeSupport().addPropertyChangeListener(this);
				diagramElements.add(e);
			}

		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {

			if (getEditor() == null || getEditor().getDiagram() == null) {
				return;
			}

			if (getEditor().getDiagram().getResource().isSerializing() || getEditor().getDiagram().getResource().isDeserializing()) {
				return;
			}

			if (event.getPropertyName() != null) {
				for (GRProperty<?> p : GraphicalRepresentationSet.IGNORED_PROPERTIES) {
					if (event.getPropertyName().equals(p.getName())) {
						// This property is ignored
						return;
					}
				}

				if (event.getPropertyName().equals(ProxyMethodHandler.SERIALIZING)
						|| event.getPropertyName().equals(ProxyMethodHandler.DESERIALIZING)
						|| event.getPropertyName().equals(ProxyMethodHandler.MODIFIED)) {
					return;
				}
			}

			// System.out.println("An observed DiagramElement GR has changed: " + event + " property " + event.getPropertyName());

			if (!isUpdating) {
				update();
			}
		}

		public FlexoConcept getFlexoConcept() {
			for (DiagramElement<?> e : diagramElements) {
				if (e instanceof DiagramShape) {
					FMLControlledDiagramShape fmlControlledShape = getEditor().getDrawing().getFederatedShape((DiagramShape) e);
					// First federated shape which has a DropScheme will be returned
					if (fmlControlledShape != null) {
						return fmlControlledShape.getFlexoConceptInstance().getFlexoConcept();
					}
				}
			}
			return null;
		}

	}

}
