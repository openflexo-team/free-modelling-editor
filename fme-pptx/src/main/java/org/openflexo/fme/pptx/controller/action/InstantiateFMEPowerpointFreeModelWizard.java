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
package org.openflexo.fme.pptx.controller.action;

import java.awt.Image;
import java.util.List;

import org.openflexo.fme.controller.action.AbstractInstantiateFMEFreeModelWizard;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.action.InstantiateFMEPowerpointFreeModel;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.technologyadapter.powerpoint.gui.PowerpointIconLibrary;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;
import org.openflexo.view.controller.FlexoController;

/**
 * Wizard annotating a slideshow of a resource center, where it is: the user only chooses the slideshow (and may rename the annotation).
 * The PowerPoint free model of the project is implicit.
 */
public class InstantiateFMEPowerpointFreeModelWizard
		extends AbstractInstantiateFMEFreeModelWizard<InstantiateFMEPowerpointFreeModel, FMEPowerpointFreeModel> {

	public InstantiateFMEPowerpointFreeModelWizard(InstantiateFMEPowerpointFreeModel action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("instantiate_free_model_with_a_slideshow");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	/**
	 * The PowerPoint free model is implicit: one per project, created with the first annotation
	 */
	@Override
	protected boolean showsChooseFreeModel() {
		return false;
	}

	@Override
	public ChoosePowerpointFreeModel makeChooseFreeModel() {
		return new ChoosePowerpointFreeModel();
	}

	@Override
	public DescribePowerpointFreeModelInstance makeDescribeFreeModelInstance() {
		return new DescribePowerpointFreeModelInstance();
	}

	/**
	 * Choose the free model to instantiate, or create a new one: nothing more for PowerPoint
	 */
	@FIBPanel("Fib/Wizard/ChooseFreeModel.fib")
	public class ChoosePowerpointFreeModel extends ChooseFreeModel {
	}

	/**
	 * The name and the description of the free model instance, and the slideshow it annotates
	 */
	@FIBPanel("Fib/Wizard/DescribePowerpointFreeModelInstance.fib")
	public class DescribePowerpointFreeModelInstance extends DescribeFreeModelInstance {

		@Override
		public boolean isValid() {

			if (getSlideshowResource() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_choose_a_slideshow"), IssueMessageType.ERROR);
				return false;
			}

			if (getAction().getExistingAnnotation() != null) {
				// A slideshow is annotated once in a project: its annotation is opened
				setIssueMessage(getAction().getLocales().localizedForKey("that_slideshow_is_already_annotated"), IssueMessageType.WARNING);
				return true;
			}

			return super.isValid();
		}

		public List<PowerpointSlideshowResource> getAvailableSlideshows() {
			return getAction().getAvailableSlideshows();
		}

		public PowerpointSlideshowResource getSlideshowResource() {
			return getAction().getSlideshowResource();
		}

		public void setSlideshowResource(PowerpointSlideshowResource slideshowResource) {
			if (slideshowResource != getSlideshowResource()) {
				PowerpointSlideshowResource oldValue = getSlideshowResource();
				getAction().setSlideshowResource(slideshowResource);
				getPropertyChangeSupport().firePropertyChange("slideshowResource", oldValue, slideshowResource);
				// The name of the instance follows the slideshow, unless it was typed
				getPropertyChangeSupport().firePropertyChange("freeModelInstanceName", null, getFreeModelInstanceName());
				checkValidity();
			}
		}

	}

}
