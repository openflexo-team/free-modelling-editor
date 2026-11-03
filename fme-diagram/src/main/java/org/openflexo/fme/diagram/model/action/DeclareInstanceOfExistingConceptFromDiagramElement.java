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

import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.action.AbstractDeclareInstanceOfExistingConcept;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

/**
 * This action declares a shape of the diagram as an instance of an existing concept, starting from the shape itself: the instance of the
 * NoneGR standing for it is created first when there is none yet<br>
 * The Diagram part is the one of {@link ShapeRepresentation}, as for {@link DeclareInstanceOfExistingConcept}
 * 
 * @author vincent, sylvain
 * 
 */
public class DeclareInstanceOfExistingConceptFromDiagramElement extends
		AbstractDeclareInstanceOfExistingConcept<DeclareInstanceOfExistingConceptFromDiagramElement, DiagramElement<?>, FMEDiagramFreeModel, FMEDiagramFreeModelInstance>
		implements ShapeRepresentation.Owner {

	public static FlexoActionFactory<DeclareInstanceOfExistingConceptFromDiagramElement, DiagramElement<?>, FlexoObject> actionType = new FlexoActionFactory<DeclareInstanceOfExistingConceptFromDiagramElement, DiagramElement<?>, FlexoObject>(
			"declare_instance_of_existing_concept_from_diagram_element", FlexoActionFactory.defaultGroup,
			FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeclareInstanceOfExistingConceptFromDiagramElement makeNewAction(DiagramElement<?> focusedObject,
				Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new DeclareInstanceOfExistingConceptFromDiagramElement(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DiagramElement<?> object, Vector<FlexoObject> globalSelection) {
			return object instanceof DiagramShape;
		}

		@Override
		public boolean isEnabledForSelection(DiagramElement<?> object, Vector<FlexoObject> globalSelection) {
			return object instanceof DiagramShape;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeclareInstanceOfExistingConceptFromDiagramElement.actionType, DiagramShape.class);
	}

	private final ShapeRepresentation shapeRepresentation = new ShapeRepresentation(this);

	private DeclareInstanceOfExistingConceptFromDiagramElement(DiagramElement<?> focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public ShapeRepresentation getShapeRepresentation() {
		return shapeRepresentation;
	}

	@Override
	protected void conceptChanged() {
		shapeRepresentation.conceptChanged();
	}

	public ShapeRepresentation.GRStrategy getGrStrategy() {
		return shapeRepresentation.getGrStrategy();
	}

	public void setGrStrategy(ShapeRepresentation.GRStrategy grStrategy) {
		shapeRepresentation.setGrStrategy(grStrategy);
	}

	/**
	 * The name of the shape
	 */
	@Override
	protected String getDefaultInstanceName() {
		return getFocusedObject().getName();
	}

	@Override
	protected FlexoConceptInstance getContainerGRInstance() {
		return DiagramRepresentations.containerGRInstanceOf(getFMEFreeModelInstance(), getFocusedObject());
	}

	/**
	 * The instance standing for the shape: the one of the NoneGR, created when there is none yet
	 */
	@Override
	protected FlexoConceptInstance retrieveRepresentationInstance(FlexoConcept grConcept) throws FlexoException {
		if (!(getFocusedObject() instanceof DiagramShape)) {
			throw new InvalidArgumentException("Not implemented for " + getFocusedObject());
		}
		FlexoConceptInstance existing = getFMEFreeModelInstance().getGRInstance(getFocusedObject());
		if (existing != null) {
			return existing;
		}
		return DiagramRepresentations.createNoneInstance(getFMEFreeModelInstance(), (DiagramShape) getFocusedObject(), this);
	}

	@Override
	protected void prepareRepresentation(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting)
			throws FlexoException {
		shapeRepresentation.prepare(grInstance, grConcept, grConceptWasExisting);
	}

	@Override
	protected void completeRepresentation(FlexoConceptInstance grInstance, FlexoConcept grConcept, boolean grConceptWasExisting)
			throws FlexoException {
		shapeRepresentation.complete(grInstance, grConcept, grConceptWasExisting);
	}

	@Override
	protected void refreshRepresentation(FlexoConceptInstance grInstance) {
		shapeRepresentation.refresh();
	}

}
