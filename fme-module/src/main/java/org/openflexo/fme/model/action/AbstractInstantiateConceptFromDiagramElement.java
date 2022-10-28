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

import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.model.DiagramElement;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

/**
 * Abstract action is used to identify an instance of NoneGR as a conceptual instance<br>
 * 
 * Focused object is here a {@link FlexoConceptInstance} of the NoneGR
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractInstantiateConceptFromDiagramElement<A extends AbstractInstantiateConceptFromDiagramElement<A>>
		extends FMEAction<A, DiagramElement<?>, FlexoObject> {

	private static final Logger logger = Logger.getLogger(AbstractInstantiateConceptFromDiagramElement.class.getPackage().getName());

	AbstractInstantiateConceptFromDiagramElement(FlexoActionFactory<A, DiagramElement<?>, FlexoObject> actionFactory,
			DiagramElement<?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * Return {@link FMEFreeModelInstance} which has diagram containing focused {@link DiagramElement}
	 * 
	 * @return
	 */
	public FMEFreeModelInstance getFreeModelInstance() {
		for (FMEFreeModel freeModel : getFreeModellingProjectNature().getFreeModels()) {
			for (FMEFreeModelInstance freeModelInstance : freeModel.getFreeModelInstances()) {
				if (freeModelInstance instanceof FMEDiagramFreeModelInstance
						&& ((FMEDiagramFreeModelInstance) freeModelInstance).getDiagram().equals(getFocusedObject().getDiagram())) {
					return freeModelInstance;
				}
			}
		}
		return null;
	}

	private FlexoConcept none;

	protected FlexoConcept getNoneFlexoConcept() {
		if (none == null && getFreeModelInstance() != null) {
			none = getFreeModelInstance().getFreeModel().getNoneFlexoConcept(getEditor(), this);
		}
		return none;
	}

	@SuppressWarnings("unchecked")
	protected FlexoConceptInstance createFlexoConceptInstanceFromDiagramShape(DiagramShape diagramShape) {
		FlexoConceptInstance newFlexoConceptInstance = getFreeModelInstance().getAccessedVirtualModelInstance()
				.makeNewFlexoConceptInstance(none);
		ShapeRole geRole = (ShapeRole) none.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		newFlexoConceptInstance.setFlexoActor(diagramShape, geRole);
		PrimitiveRole<String> nameRole = (PrimitiveRole<String>) none.getAccessibleProperty(FMEFreeModel.NAME_ROLE_NAME);
		newFlexoConceptInstance.setFlexoActor(diagramShape.getName(), nameRole);
		return newFlexoConceptInstance;
	}

	public FMEDiagramFreeModel getFMEFreeModel() {
		if (getFMEFreeModelInstance() != null) {
			return getFMEFreeModelInstance().getFreeModel();
		}
		return null;
	}

	public FMEDiagramFreeModelInstance getFMEFreeModelInstance() {
		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		if (nature != null) {
			for (FMEFreeModel freeModel : nature.getFreeModels()) {
				if (freeModel instanceof FMEDiagramFreeModel) {
					for (FMEFreeModelInstance freeModelInstance : freeModel.getFreeModelInstances()) {
						if (freeModelInstance instanceof FMEDiagramFreeModelInstance) {
							if (getFocusedObject().getDiagram() == ((FMEDiagramFreeModelInstance) freeModelInstance).getDiagram()) {
								return (FMEDiagramFreeModelInstance) freeModelInstance;
							}
						}
					}
				}
			}
			return null;
		}
		logger.warning("Sorry, project does not have FreeModellingProjectNature");
		return null;
	}
}
