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

import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.powerpoint.BasicPowerpointModelSlot;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObject;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;

/**
 * An instance of a {@link FMEPowerpointFreeModel}: it annotates a whole slideshow, referenced where it is (in its resource center), and
 * never modified<br>
 * 
 * From a technical point of view, a {@link FMEPowerpointFreeModelInstance} is a wrapper above a {@link FMLRTVirtualModelInstance} located
 * in the project, whose model slot is connected to the slideshow, and whose instances of GR concepts each point to an annotated shape
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@XMLElement
@ImplementationClass(FMEPowerpointFreeModelInstance.FMEPowerpointFreeModelInstanceImpl.class)
public interface FMEPowerpointFreeModelInstance extends FMEFreeModelInstance {

	@Override
	public FMEPowerpointFreeModel getFreeModel();

	/**
	 * The slideshow annotated by this instance
	 */
	public PowerpointSlideshow getSlideshow();

	public abstract class FMEPowerpointFreeModelInstanceImpl extends FMEFreeModelInstanceImpl implements FMEPowerpointFreeModelInstance {

		@Override
		public FMEPowerpointFreeModel getFreeModel() {
			return (FMEPowerpointFreeModel) performSuperGetter(FREE_MODEL);
		}

		/**
		 * Once connected, this instance is found by the actions on the shapes of its slideshow
		 */
		@Override
		public void fireVirtualModelInstanceConnected(FMLRTVirtualModelInstance aVirtualModelInstance) {
			super.fireVirtualModelInstanceConnected(aVirtualModelInstance);
			PowerpointAnnotations.register(this);
		}

		@Override
		public void fireVirtualModelInstanceDisconnected(FMLRTVirtualModelInstance aVirtualModelInstance) {
			super.fireVirtualModelInstanceDisconnected(aVirtualModelInstance);
			PowerpointAnnotations.unregister(this);
		}

		@Override
		public PowerpointSlideshow getSlideshow() {
			if (getAccessedVirtualModelInstance() == null || getFreeModel() == null || getFreeModel().getPowerpointModelSlot() == null) {
				return null;
			}
			ModelSlotInstance<BasicPowerpointModelSlot, PowerpointSlideshow> msInstance = getAccessedVirtualModelInstance()
					.getModelSlotInstance(getFreeModel().getPowerpointModelSlot());
			return msInstance != null ? msInstance.getAccessedResourceData() : null;
		}

		/**
		 * The slides of the slideshow of this instance and their elements
		 */
		@Override
		public boolean contains(Object element) {
			return element instanceof PowerpointObject && getSlideshow() != null
					&& ((PowerpointObject) element).getResourceData() == getSlideshow();
		}

		/**
		 * The shape supplied instance stands for
		 */
		@Override
		public Object getRepresentationElement(FlexoConceptInstance grInstance) {
			if (grInstance == null || grInstance.getFlexoConcept() == null) {
				return null;
			}
			Object actor = grInstance.getFlexoPropertyValue(FMEPowerpointFreeModel.SHAPE_ROLE_NAME);
			return actor instanceof PowerpointShape ? actor : null;
		}
	}

}
