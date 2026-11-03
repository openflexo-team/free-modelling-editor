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

/**
 * This action is used to declare a concept to be an instance of an existing concept (move to None to Something)<br>
 * The Diagram part is the one of {@link ShapeRepresentation}: the shape of the instance and the one of its GR concept, and the palette
 *
 * Focused object is here a {@link FlexoConceptInstance} of the NoneGR
 * 
 * @author sylvain
 * 
 */
public class DeclareInstanceOfExistingConcept
		extends AbstractDeclareInstanceOfExistingConcept<DeclareInstanceOfExistingConcept, FlexoConceptInstance, FMEDiagramFreeModel, FMEDiagramFreeModelInstance>
		implements ShapeRepresentation.Owner {

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

	private final ShapeRepresentation shapeRepresentation = new ShapeRepresentation(this);

	private DeclareInstanceOfExistingConcept(FlexoConceptInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
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
	 * The name carried by the instance of the NoneGR
	 */
	@Override
	protected String getDefaultInstanceName() {
		return getFocusedObject().getFlexoPropertyValue(FMEFreeModel.NAME_ROLE_NAME);
	}

	@Override
	protected FlexoConceptInstance getContainerGRInstance() {
		return DiagramRepresentations.containerGRInstanceOf(getFMEFreeModelInstance(), DiagramRepresentations.shapeOf(getFocusedObject()));
	}

	/**
	 * The instance of the NoneGR itself
	 */
	@Override
	protected FlexoConceptInstance retrieveRepresentationInstance(FlexoConcept grConcept) {
		return getFocusedObject();
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
