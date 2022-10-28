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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidArgumentException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance.ObjectLookupResult;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

/**
 * This action is used to create a new FlexoConcept from a diagram element
 * 
 * @author vincent
 * 
 */
public class CreateNewConceptFromDiagramElement extends AbstractInstantiateConceptFromDiagramElement<CreateNewConceptFromDiagramElement> {

	private static final Logger logger = Logger.getLogger(CreateNewConceptFromDiagramElement.class.getPackage().getName());

	public static FlexoActionFactory<CreateNewConceptFromDiagramElement, DiagramElement<?>, FlexoObject> actionType = new FlexoActionFactory<CreateNewConceptFromDiagramElement, DiagramElement<?>, FlexoObject>(
			"create_new_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewConceptFromDiagramElement makeNewAction(DiagramElement<?> focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewConceptFromDiagramElement(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(DiagramElement<?> object, Vector<FlexoObject> globalSelection) {
			return object instanceof DiagramShape || object instanceof DiagramConnector;
		}

		@Override
		public boolean isEnabledForSelection(DiagramElement<?> object, Vector<FlexoObject> globalSelection) {
			return object instanceof DiagramShape || object instanceof DiagramConnector;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateNewConceptFromDiagramElement.actionType, DiagramShape.class);
		FlexoObjectImpl.addActionForClass(CreateNewConceptFromDiagramElement.actionType, DiagramConnector.class);
	}

	private CreateNewConceptFromDiagramElement(DiagramElement<?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	private FlexoConceptInstance flexoConceptInstance;
	private FlexoConcept flexoConcept;

	@Override
	protected void doAction(Object context) throws InvalidArgumentException {

		// Unused FlexoConcept containerConcept = null;
		if (getFocusedObject().getParent() != null) {
			ObjectLookupResult lookup = getFreeModelInstance().getAccessedVirtualModelInstance().lookup(getFocusedObject().getParent());
			if (lookup != null) {
				// System.out.println("lookup: " + lookup.flexoConceptInstance + " pty=" + lookup.property);
				FlexoConcept containerConceptGR = lookup.flexoConceptInstance.getFlexoConcept();
				FlexoProperty<?> p = containerConceptGR.getAccessibleProperty(FMEFreeModel.CONCEPT_ROLE_NAME);
				if (p instanceof FlexoConceptInstanceRole) {
					FlexoConceptInstanceRole fciRole = (FlexoConceptInstanceRole) p;
					// Unused containerConcept = fciRole.getFlexoConceptType();
				}
			}
		}

		logger.info("Create new instance of created concept from diagram element ");
		getNoneFlexoConcept();

		if (getFocusedObject() instanceof DiagramShape) {
			logger.info("Create new concept from shape element ");
			flexoConceptInstance = createFlexoConceptInstanceFromDiagramShape((DiagramShape) getFocusedObject());
			CreateNewConceptFromNoneConcept actionCreateNewConcept = CreateNewConceptFromNoneConcept.actionType
					.makeNewEmbeddedAction(flexoConceptInstance, null, this);
			actionCreateNewConcept.doAction();
			flexoConcept = actionCreateNewConcept.getNewFlexoConcept();
		}
		else if (getFocusedObject() instanceof DiagramConnector) {
			logger.info("Create new concept from connector element ");

			CreateNewConceptFromDiagramConnector actionCreateNewConcept = CreateNewConceptFromDiagramConnector.actionType
					.makeNewEmbeddedAction((DiagramConnector) getFocusedObject(), null, this);
			actionCreateNewConcept.doAction();

			if (!actionCreateNewConcept.hasActionExecutionSucceeded()) {
				return;
			}

			flexoConceptInstance = actionCreateNewConcept.getNewFlexoConceptInstance();

			if (flexoConceptInstance == null) {
				return;
			}

			flexoConcept = flexoConceptInstance.getFlexoConcept();

		}

	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	public void setFlexoConcept(FlexoConcept flexoConcept) {
		this.flexoConcept = flexoConcept;
	}

	public FlexoConceptInstance getFlexoConceptInstance() {
		return flexoConceptInstance;
	}
}
