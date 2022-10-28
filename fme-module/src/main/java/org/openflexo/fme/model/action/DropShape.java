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

import java.awt.Dimension;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.diana.ShapeGraphicalRepresentation.LocationConstraints;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.technologyadapter.diagram.fml.DropScheme;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.model.DiagramContainerElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.technologyadapter.diagram.model.action.DropSchemeAction;

/**
 * This action is used to create a new FreeShape in the Free Modelling Editor
 * 
 * @author sylvain
 * 
 */
public class DropShape extends FMEAction<DropShape, DiagramContainerElement<?>, FlexoObject> {

	private static final Logger logger = Logger.getLogger(DropShape.class.getPackage().getName());

	public static FlexoActionFactory<DropShape, DiagramContainerElement<?>, FlexoObject> actionType = new FlexoActionFactory<DropShape, DiagramContainerElement<?>, FlexoObject>(
			"drop_free_shape", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DropShape makeNewAction(DiagramContainerElement<?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new DropShape(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DiagramContainerElement<?> object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(DiagramContainerElement<?> object, Vector<FlexoObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DropShape.actionType, DiagramContainerElement.class);
	}

	private FMEDiagramFreeModelInstance diagramFreeModelInstance;
	private DiagramContainerElement<?> parent;
	private ShapeGraphicalRepresentation graphicalRepresentation;
	private FlexoConcept grConcept;
	private DianaPoint dropLocation;
	private Dimension targetSize;
	private FlexoConceptInstance container;

	private FlexoConceptInstance newFlexoConceptInstance;

	private DropShape(DiagramContainerElement<?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws SaveResourceException {

		FlexoConcept grConcept = getGRConcept();
		FlexoConcept concept = getConcept();

		boolean noneConceptIsExisting = true;

		// When non concept supplied, use (eventually creates) None concept
		if (grConcept == null) {
			noneConceptIsExisting = diagramFreeModelInstance.getFreeModel().getAccessedVirtualModel()
					.getFlexoConcept(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME) != null;
			grConcept = diagramFreeModelInstance.getFreeModel().getNoneFlexoConcept(getEditor(), this);
		}

		List<DropScheme> dsList = grConcept.getFlexoBehaviours(DropScheme.class);

		if (dsList.size() == 1) {

			DropScheme dropScheme = dsList.get(0);
			FlexoBehaviourParameter nameParam = dropScheme.getParameters().size() > 0 ? dropScheme.getParameters().get(0) : null;
			DropSchemeAction action = new DropSchemeAction(dropScheme, getDiagramFreeModelInstance().getAccessedVirtualModelInstance(),
					null, this);
			action.setParentInformations(getContainer(), null);
			addToEmbeddedActions(action);
			if (nameParam != null) {
				action.setParameterValue(nameParam, getDiagramFreeModelInstance().getProposedName(concept));
			}
			action.setDropLocation(dropLocation);

			action.doAction();
			newFlexoConceptInstance = action.getFlexoConceptInstance();

			if (newFlexoConceptInstance == null) {
				return;
			}

			ShapeRole shapeRole = (ShapeRole) grConcept.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
			DiagramShape shape = newFlexoConceptInstance.getFlexoActor(shapeRole);

			// If another GR was defined (overriding the one from ShapeRole)
			if (getGraphicalRepresentation() != null && shape.getGraphicalRepresentation() != null) {
				shape.getGraphicalRepresentation().setsWith(getGraphicalRepresentation());
				shape.getGraphicalRepresentation().setIsReadOnly(false);
				shape.getGraphicalRepresentation().setIsFocusable(true);
				shape.getGraphicalRepresentation().setIsSelectable(true);
				shape.getGraphicalRepresentation().setLocationConstraints(LocationConstraints.FREELY_MOVABLE);
			}
			if (getTargetSize() != null) {
				shape.getGraphicalRepresentation().setHeight(getTargetSize().getHeight());
				shape.getGraphicalRepresentation().setWidth(getTargetSize().getWidth());
			}

			shape.getGraphicalRepresentation().setX(dropLocation.x);
			shape.getGraphicalRepresentation().setY(dropLocation.y);

			if (!noneConceptIsExisting) {
				// This means that None FlexoConcept was not existing and has been created
				// We should notify this
				diagramFreeModelInstance.getPropertyChangeSupport().firePropertyChange("usedFlexoConcepts", null, grConcept);
			}

			// This is used to notify the adding of a new instance of a flexo concept
			diagramFreeModelInstance.getPropertyChangeSupport().firePropertyChange("getInstances(FlexoConcept)", null,
					newFlexoConceptInstance);

			// This is used to notify the adding of a new shape, will be used in DynamicPalette
			// freeModel.getPropertyChangeSupport().firePropertyChange(DynamicPalette.SHAPE_ADDED, null, newFlexoConceptInstance);

		}
		else {
			logger.warning("Could not find DropScheme in " + grConcept);
		}
	}

	public FMEDiagramFreeModelInstance getDiagramFreeModelInstance() {
		return diagramFreeModelInstance;
	}

	public void setDiagramFreeModelInstance(FMEDiagramFreeModelInstance diagramFreeModelInstance) {
		this.diagramFreeModelInstance = diagramFreeModelInstance;
	}

	public ShapeGraphicalRepresentation getGraphicalRepresentation() {
		return graphicalRepresentation;
	}

	public void setGraphicalRepresentation(ShapeGraphicalRepresentation graphicalRepresentation) {
		this.graphicalRepresentation = graphicalRepresentation;
	}

	public FlexoConceptInstance getNewFlexoConceptInstance() {
		return newFlexoConceptInstance;
	}

	public FlexoConcept getGRConcept() {
		return grConcept;
	}

	public void setGRConcept(FlexoConcept grConcept) {
		this.grConcept = grConcept;
	}

	public FlexoConcept getConcept() {
		if (getGRConcept() != null) {
			FlexoProperty<?> p = getGRConcept().getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
			if (p instanceof FlexoConceptInstanceRole) {
				FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
				return fciRole.getFlexoConceptType();
			}
		}
		return null;
	}

	public DianaPoint getDropLocation() {
		return dropLocation;
	}

	public void setDropLocation(DianaPoint dropLocation) {
		this.dropLocation = dropLocation;
	}

	public Dimension getTargetSize() {
		return targetSize;
	}

	public void setTargetSize(Dimension targetSize) {
		this.targetSize = targetSize;
	}

	/**
	 * Return the {@link FlexoConceptInstance} in which we "drop"
	 * 
	 * @return
	 */
	public FlexoConceptInstance getContainer() {
		return container;
	}

	/**
	 * Sets the {@link FlexoConceptInstance} in which we "drop"
	 * 
	 * @param container
	 */
	public void setContainer(FlexoConceptInstance container) {
		if ((container == null && this.container != null) || (container != null && !container.equals(this.container))) {
			FlexoConceptInstance oldValue = this.container;
			this.container = container;
			getPropertyChangeSupport().firePropertyChange("container", oldValue, container);
		}
	}

}
