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

package org.openflexo.fme.diagram.model.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.fme.diagram.controller.editor.DynamicPalette.GraphicalRepresentationSet;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.AbstractDeclareInstanceOfExistingConcept;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot;
import org.openflexo.technologyadapter.diagram.fml.FMLDiagramPaletteElementBinding;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPaletteElement;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

/**
 * This action is used to declare a concept to be an instance of an existing concept (move to None to Something)<br>
 * The Diagram part: the shape of the instance and the one of its GR concept, following a {@link GRStrategy}, and the palette
 *
 * Focused object is here a {@link FlexoConceptInstance} of the NoneGR
 * 
 * @author sylvain
 * 
 */
public class DeclareInstanceOfExistingConcept
		extends AbstractDeclareInstanceOfExistingConcept<DeclareInstanceOfExistingConcept, FMEDiagramFreeModel, FMEDiagramFreeModelInstance> {

	private static final Logger logger = Logger.getLogger(DeclareInstanceOfExistingConcept.class.getPackage().getName());

	public static enum GRStrategy {
		GetConceptShape, RedefineShapeOfConcept, KeepShape
	}

	public static FlexoActionFactory<DeclareInstanceOfExistingConcept, FlexoConceptInstance, FlexoObject> actionType = new FlexoActionFactory<DeclareInstanceOfExistingConcept, FlexoConceptInstance, FlexoObject>(
			"declare_instance_of_existing_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeclareInstanceOfExistingConcept makeNewAction(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new DeclareInstanceOfExistingConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptInstance object, Vector<FlexoObject> globalSelection) {
			return object.getFlexoConcept().getName().equals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeclareInstanceOfExistingConcept.actionType, FlexoConceptInstance.class);
	}

	private GRStrategy grStrategy = null; // GRStrategy.GetConceptShape;

	// The shape of the focused instance, set when the representation is prepared
	private DiagramShape shapeElement;

	private DeclareInstanceOfExistingConcept(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void conceptChanged() {
		getPropertyChangeSupport().firePropertyChange("grStrategy", null, getConcept());
	}

	public GRStrategy getGrStrategy() {
		if (grStrategy == null) {
			if (getFMEFreeModelInstance().getInstances(getGRConcept(false)).size() > 0) {
				return GRStrategy.GetConceptShape;
			}
			return GRStrategy.RedefineShapeOfConcept;
		}
		return grStrategy;
	}

	public void setGrStrategy(GRStrategy grStrategy) {
		boolean wasValid = isValid();
		this.grStrategy = grStrategy;
		getPropertyChangeSupport().firePropertyChange("grStrategy", null, grStrategy);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	@Override
	protected void prepareRepresentation(FlexoConceptInstance flexoConceptInstance, FlexoConcept grConcept, boolean grConceptWasExisting)
			throws FlexoException {

		// Retrieve shape property of this FC
		ShapeRole currentShapeRole = (ShapeRole) flexoConceptInstance.getFlexoConcept()
				.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		// Retrieve actual shape element
		shapeElement = flexoConceptInstance.getFlexoActor(currentShapeRole);

		ShapeRole newShapeRole = (ShapeRole) grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		if (grConceptWasExisting) {

			switch (getGrStrategy()) {
				case RedefineShapeOfConcept:
					// Sets concept GR with actual shape GR
					newShapeRole.getGraphicalRepresentation().setsWith(shapeElement.getGraphicalRepresentation(),
							ShapeGraphicalRepresentation.X, ShapeGraphicalRepresentation.Y);
					// Look at the palette element
					// DiagramPalette palette = freeModel.getMetaModel().getConceptsPalette();
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
	}

	@Override
	protected void completeRepresentation(FlexoConceptInstance flexoConceptInstance, FlexoConcept grConcept, boolean grConceptWasExisting)
			throws FlexoException {

		FMEDiagramFreeModelInstance freeModelInstance = getFMEFreeModelInstance();
		ShapeRole newShapeRole = (ShapeRole) grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);

		// In case of GRStrategy is to redefine concept shape, we now need to set GR of all instances
		if (grConceptWasExisting) {
			if (getGrStrategy() == GRStrategy.RedefineShapeOfConcept) {
				for (FlexoConceptInstance fci : freeModelInstance.getInstances(grConcept)) {
					DiagramShape fciShapeElement = fci.getFlexoActor(newShapeRole);
					fciShapeElement.getGraphicalRepresentation().setsWith(newShapeRole.getGraphicalRepresentation(),
							GraphicalRepresentationSet.IGNORED_PROPERTIES);
					fciShapeElement.getParent().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null,
							fciShapeElement.getParent());
				}
			}
		}

		if (freeModelInstance.getInstances(getConcept()).size() == 1) {
			// This was the first time such an instance of this concept is used
			// This might be a good idea to add a palette element (when non existant)

			// Unused DiagramPalette palette =
			freeModelInstance.getFreeModel().getConceptsPalette();
			DiagramPaletteElement existingElement = null;
			TypedDiagramModelSlot ms = freeModelInstance.getFreeModel().getTypedDiagramModelSlot();
			for (FMLDiagramPaletteElementBinding b : ms.getPaletteElementBindings()) {
				if (b.getBoundFlexoConcept() == getConcept()) {
					existingElement = b.getPaletteElement();
				}
			}
			if (existingElement == null) {
				// No palette element matching related concept was found
				freeModelInstance.getFreeModel().createPaletteElementForConcept(grConcept, getConcept(),
						shapeElement.getGraphicalRepresentation(), this);
			}
		}
	}

	@Override
	protected void refreshRepresentation(FlexoConceptInstance flexoConceptInstance) {
		// The new shape has well be added to the diagram, and the drawing (which listen to the diagram) has well received the event
		// The drawing is now up-to-date... but there is something wrong if we are in FML-controlled mode.
		// Since the shape has been added BEFORE the FlexoConceptInstance has been set, the drawing only knows about the DiagamShape,
		// and not about an FMLControlledDiagramShape. That's why we need to notify again the new diagram element's parent, to be
		// sure that the Drawing can discover that the new shape is FML-controlled
		shapeElement.getParent().getPropertyChangeSupport().firePropertyChange(DiagramElement.INVALIDATE, null, shapeElement.getParent());
	}

}
