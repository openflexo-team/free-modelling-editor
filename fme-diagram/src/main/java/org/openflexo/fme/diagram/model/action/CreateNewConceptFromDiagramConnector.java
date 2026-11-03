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

import org.openflexo.diana.GraphicalRepresentation.VerticalTextAlignment;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.action.AbstractCreateNewConceptFromLink;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.fml.ConnectorRole;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;

/**
 * This action is used to create a new concept from a connector linking two concepts<br>
 * Focused object is the connector
 * 
 * @author sylvain
 * 
 */
public class CreateNewConceptFromDiagramConnector extends
		AbstractCreateNewConceptFromLink<CreateNewConceptFromDiagramConnector, DiagramConnector, FMEDiagramFreeModel, FMEDiagramFreeModelInstance> {

	public static FlexoActionFactory<CreateNewConceptFromDiagramConnector, DiagramConnector, FlexoObject> actionType = new FlexoActionFactory<CreateNewConceptFromDiagramConnector, DiagramConnector, FlexoObject>(
			"create_new_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewConceptFromDiagramConnector makeNewAction(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewConceptFromDiagramConnector(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DiagramConnector object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(DiagramConnector object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateNewConceptFromDiagramConnector.actionType, DiagramConnector.class);
	}

	private CreateNewConceptFromDiagramConnector(DiagramConnector focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected Object getStartElement() {
		return getFocusedObject().getStartShape();
	}

	@Override
	protected Object getEndElement() {
		return getFocusedObject().getEndShape();
	}

	/**
	 * Sets new concept GR with actual connector GR
	 */
	@Override
	protected void configureRepresentation(FlexoConcept newGRConcept) {
		ConnectorRole connectorRole = (ConnectorRole) newGRConcept.getAccessibleProperty(FMEDiagramFreeModel.CONNECTOR_ROLE_NAME);
		connectorRole.getGraphicalRepresentation().setsWith(getFocusedObject().getGraphicalRepresentation());
		// Default align on bottom
		getFocusedObject().getGraphicalRepresentation().setVerticalTextAlignment(VerticalTextAlignment.BOTTOM);
		connectorRole.getGraphicalRepresentation().setVerticalTextAlignment(VerticalTextAlignment.BOTTOM);
	}

	@Override
	protected FlexoConceptInstance createRepresentationInstance(FlexoConcept newGRConcept) throws FlexoException {
		FlexoConceptInstance returned = getFMEFreeModelInstance().getAccessedVirtualModelInstance().makeNewFlexoConceptInstance(newGRConcept,
				null, null);
		returned.setFlexoActor(getFocusedObject(),
				(ConnectorRole) newGRConcept.getAccessibleProperty(FMEDiagramFreeModel.CONNECTOR_ROLE_NAME));
		return returned;
	}

	@Override
	protected void refreshRepresentation(FlexoConceptInstance grInstance) {
		DiagramRepresentations.invalidate(getFocusedObject());
	}

}
