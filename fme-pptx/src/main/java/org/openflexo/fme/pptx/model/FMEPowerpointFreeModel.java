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
package org.openflexo.fme.pptx.model;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateTechnologyRole;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.powerpoint.BasicPowerpointModelSlot;
import org.openflexo.technologyadapter.powerpoint.fml.PowerpointShapeRole;

/**
 * A PowerPoint-based {@link FMEFreeModel}: the concepts of the conceptual model are represented by the shapes of an existing slideshow,
 * which the free model instances annotate without modifying it<br>
 * 
 * The base of a {@link FMEPowerpointFreeModel} is a {@link VirtualModel} federating the conceptual model, the sample data and a
 * slideshow ({@link #SLIDESHOW_MODEL_SLOT_NAME}). Each GR concept points to the shape standing for an instance of its concept
 * ({@link #SHAPE_ROLE_NAME}): there is no NoneGR, a shape which is not annotated has no instance.
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@XMLElement
@ImplementationClass(FMEPowerpointFreeModel.FMEPowerpointFreeModelImpl.class)
public interface FMEPowerpointFreeModel extends FMEFreeModel {

	public static final String SLIDESHOW_MODEL_SLOT_NAME = "slideshow";
	public static final String SHAPE_ROLE_NAME = "shape";

	/**
	 * The model slot of the slideshow the free model instances annotate
	 */
	public BasicPowerpointModelSlot getPowerpointModelSlot();

	public abstract class FMEPowerpointFreeModelImpl extends FMEFreeModelImpl implements FMEPowerpointFreeModel {

		@Override
		public BasicPowerpointModelSlot getPowerpointModelSlot() {
			if (getAccessedVirtualModel() != null && getAccessedVirtualModel().getModelSlots(BasicPowerpointModelSlot.class).size() > 0) {
				return getAccessedVirtualModel().getModelSlots(BasicPowerpointModelSlot.class).get(0);
			}
			return null;
		}

		/**
		 * A GR concept points to the shape standing for an instance of its concept
		 */
		@Override
		protected void configureGRFlexoConcept(FlexoConcept returned, FlexoConcept concept, FlexoConcept containerConceptGR,
				FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {
			createShapeRole(returned, editor, ownerAction);
		}

		/**
		 * A relational GR concept points to the connector standing for an instance of its concept
		 */
		@Override
		protected void configureGRRelationalFlexoConcept(FlexoConcept returned, FlexoConcept concept, FlexoConcept fromConceptGR,
				FlexoConcept toConceptGR, FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {
			createShapeRole(returned, editor, ownerAction);
		}

		private PowerpointShapeRole createShapeRole(FlexoConcept grConcept, FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {
			CreateTechnologyRole createShapeRole;
			if (ownerAction != null) {
				createShapeRole = CreateTechnologyRole.actionType.makeNewEmbeddedAction(grConcept, null, ownerAction);
			}
			else {
				createShapeRole = CreateTechnologyRole.actionType.makeNewAction(grConcept, null, editor);
			}
			createShapeRole.setModelSlot(getPowerpointModelSlot());
			createShapeRole.setRoleName(SHAPE_ROLE_NAME);
			createShapeRole.setFlexoRoleClass(PowerpointShapeRole.class);
			createShapeRole.doAction();
			return (PowerpointShapeRole) createShapeRole.getNewFlexoRole();
		}

	}

}
