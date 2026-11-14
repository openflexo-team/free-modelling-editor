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

import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.error.FMLExecutionException;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

/**
 * What the Diagram actions of the FreeModellingEditor share about the shapes of a diagram: the NoneGR instance standing for a plain shape,
 * the instance standing for the shape containing another one, the shape a new GR concept takes, the refresh of the drawing
 * 
 * @author sylvain
 * 
 */
public class DiagramRepresentations {

	private DiagramRepresentations() {
	}

	/**
	 * The instance of a GR concept standing for the shape containing supplied element, or null (a top-level element)
	 */
	public static FlexoConceptInstance containerGRInstanceOf(FMEDiagramFreeModelInstance freeModelInstance, DiagramElement<?> element) {
		if (freeModelInstance == null || element == null || !(element.getParent() instanceof DiagramShape)) {
			return null;
		}
		return freeModelInstance.getGRInstance(element.getParent());
	}

	/**
	 * Creates an instance of the NoneGR standing for supplied shape, named after it
	 */
	@SuppressWarnings("unchecked")
	public static FlexoConceptInstance createNoneInstance(FMEDiagramFreeModelInstance freeModelInstance, DiagramShape diagramShape,
			FlexoAction<?, ?, ?> ownerAction) throws FMLExecutionException {
		FlexoConcept none = freeModelInstance.getFreeModel().getNoneFlexoConcept(ownerAction.getEditor(), ownerAction);
		FlexoConceptInstance returned = freeModelInstance.getAccessedVirtualModelInstance().makeNewFlexoConceptInstance(none, null, null);
		returned.setFlexoActor(diagramShape, (ShapeRole) none.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME));
		returned.setFlexoActor(diagramShape.getName(), (PrimitiveRole<String>) none.getAccessibleProperty(FMEFreeModel.NAME_ROLE_NAME));
		return returned;
	}

	/**
	 * The shape supplied instance of a GR concept stands for, or null
	 */
	public static DiagramShape shapeOf(FlexoConceptInstance grInstance) {
		if (grInstance == null || grInstance.getFlexoConcept() == null
				|| !(grInstance.getFlexoConcept().getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME) instanceof ShapeRole)) {
			return null;
		}
		return grInstance.getFlexoActor((ShapeRole) grInstance.getFlexoConcept().getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME));
	}

	/**
	 * A new GR concept takes the shape of the element its concept emerged from, and the palette gets an entry for the concept
	 */
	public static void configureNewConceptShape(FMEDiagramFreeModel freeModel, FlexoConcept newGRConcept, FlexoConcept newConcept,
			DiagramShape shapeElement, FlexoAction<?, ?, ?> ownerAction) throws FlexoException {
		// Add entry in palette
		freeModel.createPaletteElementForConcept(newGRConcept, newConcept, shapeElement.getGraphicalRepresentation(), ownerAction);

		// Sets new concept GR with actual shape GR
		ShapeRole shapeRole = (ShapeRole) newGRConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		shapeRole.getGraphicalRepresentation().setsWith(shapeElement.getGraphicalRepresentation());
		shapeRole.getGraphicalRepresentation().setX(10);
		shapeRole.getGraphicalRepresentation().setY(10);
	}

	/**
	 * The element has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event. The drawing
	 * is now up-to-date... but there is something wrong if we are in FML-controlled mode. Since the element has been added BEFORE the
	 * FlexoConceptInstance has been set, the drawing only knows about the DiagramShape, and not about an FMLControlledDiagramShape. That's
	 * why we need to notify again the parent of the element, to be sure that the Drawing can discover that the element is FML-controlled
	 */
	public static void invalidate(DiagramElement<?> element) {
		if (element != null && element.getParent() != null) {
			element.getParent().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, element.getParent());
		}
	}

}
