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
package org.openflexo.fme.diagram.model.action;

import java.beans.PropertyChangeSupport;

import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.fme.diagram.controller.editor.DynamicPalette.GraphicalRepresentationSet;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.action.AbstractDeclareInstanceOfExistingConcept;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot;
import org.openflexo.technologyadapter.diagram.fml.FMLDiagramPaletteElementBinding;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPaletteElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * The Diagram part of the actions declaring a shape as an instance of an existing concept ({@link DeclareInstanceOfExistingConcept},
 * {@link DeclareInstanceOfExistingConceptFromDiagramElement}): how the shape of the element and the one of the GR concept relate
 * ({@link GRStrategy}), the palette element of a first instance, the refresh of the drawing
 * 
 * @author sylvain
 * 
 */
public class ShapeRepresentation implements HasPropertyChangeSupport {

	public static enum GRStrategy {
		GetConceptShape, RedefineShapeOfConcept, KeepShape
	}

	/**
	 * An action declaring a shape as an instance of an existing concept
	 */
	public static interface Owner {
		public ShapeRepresentation getShapeRepresentation();
	}

	private final AbstractDeclareInstanceOfExistingConcept<?, ?, FMEDiagramFreeModel, FMEDiagramFreeModelInstance> action;
	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	private GRStrategy grStrategy = null;

	// The shape of the element, set when the representation is prepared
	private DiagramShape shapeElement;

	public ShapeRepresentation(AbstractDeclareInstanceOfExistingConcept<?, ?, FMEDiagramFreeModel, FMEDiagramFreeModelInstance> action) {
		this.action = action;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	/**
	 * Whether the GR concept of the concept exists: only then is there a strategy to choose
	 */
	public boolean hasGRConcept() {
		return action.getConcept() != null && action.hasGRConcept();
	}

	/**
	 * Called when the concept of the action was changed
	 */
	public void conceptChanged() {
		pcSupport.firePropertyChange("hasGRConcept", !hasGRConcept(), hasGRConcept());
		pcSupport.firePropertyChange("grStrategy", null, getGrStrategy());
	}

	public GRStrategy getGrStrategy() {
		if (grStrategy == null) {
			if (hasGRConcept() && action.getFMEFreeModelInstance().getInstances(action.getGRConcept(false)).size() > 0) {
				return GRStrategy.GetConceptShape;
			}
			return GRStrategy.RedefineShapeOfConcept;
		}
		return grStrategy;
	}

	public void setGrStrategy(GRStrategy grStrategy) {
		GRStrategy oldValue = getGrStrategy();
		this.grStrategy = grStrategy;
		pcSupport.firePropertyChange("grStrategy", oldValue, grStrategy);
	}

	/**
	 * Before the instance becomes an instance of the GR concept: the shape of the element, or the one of an existing GR concept, is
	 * changed following the strategy
	 */
	public void prepare(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting) {

		// Retrieve actual shape element
		shapeElement = DiagramRepresentations.shapeOf(grInstance);

		ShapeRole newShapeRole = (ShapeRole) grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		if (grConceptWasExisting) {

			switch (getGrStrategy()) {
				case RedefineShapeOfConcept:
					// Sets concept GR with actual shape GR
					newShapeRole.getGraphicalRepresentation().setsWith(shapeElement.getGraphicalRepresentation(),
							ShapeGraphicalRepresentation.X, ShapeGraphicalRepresentation.Y);
					break;
				case GetConceptShape:
					// Sets actual shape GR with concept GR
					shapeElement.getGraphicalRepresentation().setsWith(newShapeRole.getGraphicalRepresentation(),
							ShapeGraphicalRepresentation.X, ShapeGraphicalRepresentation.Y);
					break;
				case KeepShape:
					// Nothing to do
					break;
			}
		}
		else {
			// The new GR concept takes the shape of the element
			newShapeRole.getGraphicalRepresentation().setsWith(shapeElement.getGraphicalRepresentation());
			newShapeRole.getGraphicalRepresentation().setX(10);
			newShapeRole.getGraphicalRepresentation().setY(10);
		}
	}

	/**
	 * Once the instance is an instance of the GR concept: the other instances follow a redefined shape, and a concept without palette
	 * element gets one
	 */
	public void complete(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting) throws FlexoException {

		FMEDiagramFreeModelInstance freeModelInstance = action.getFMEFreeModelInstance();
		ShapeRole newShapeRole = (ShapeRole) grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		// In case of GRStrategy is to redefine concept shape, we now need to set GR of all instances
		if (grConceptWasExisting && getGrStrategy() == GRStrategy.RedefineShapeOfConcept) {
			for (FlexoConceptInstance fci : freeModelInstance.getInstances(grConcept)) {
				DiagramShape fciShapeElement = fci.getFlexoActor(newShapeRole);
				fciShapeElement.getGraphicalRepresentation().setsWith(newShapeRole.getGraphicalRepresentation(),
						GraphicalRepresentationSet.IGNORED_PROPERTIES);
				DiagramRepresentations.invalidate(fciShapeElement);
			}
		}

		// The first shape standing for an instance of the concept gives it an element in the palette, when it has none yet
		// (a palette element is bound to the GR concept it drops)
		DiagramPaletteElement existingElement = null;
		TypedDiagramModelSlot ms = freeModelInstance.getFreeModel().getTypedDiagramModelSlot();
		for (FMLDiagramPaletteElementBinding b : ms.getPaletteElementBindings()) {
			if (b.getBoundFlexoConcept() == grConcept) {
				existingElement = b.getPaletteElement();
			}
		}
		if (existingElement == null) {
			// No palette element matching related concept was found
			freeModelInstance.getFreeModel().createPaletteElementForConcept(grConcept, action.getConcept(),
					shapeElement.getGraphicalRepresentation(), action);
		}
	}

	/**
	 * Last: the drawing discovers that the shape is FML-controlled
	 */
	public void refresh() {
		DiagramRepresentations.invalidate(shapeElement);
	}

}
