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

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.controller.action.AbstractCreateFMEFreeModelWizard;
import org.openflexo.fme.pptx.model.action.CreateFMEPowerpointFreeModel;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.technologyadapter.powerpoint.gui.PowerpointIconLibrary;
import org.openflexo.view.controller.FlexoController;

/**
 * Wizard creating a PowerPoint free model: its name and description, then its conceptual model and sample data
 */
public class CreateFMEPowerpointFreeModelWizard extends AbstractCreateFMEFreeModelWizard<CreateFMEPowerpointFreeModel> {

	public CreateFMEPowerpointFreeModelWizard(CreateFMEPowerpointFreeModel action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_powerpoint_free_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_BIG_ICON, FMEIconLibrary.FME_BIG_MARKER).getImage();
	}

	@Override
	public DescribePowerpointFreeModel makeDescribeFreeModel() {
		return new DescribePowerpointFreeModel();
	}

	/**
	 * The name and the description of the free model: nothing more for PowerPoint
	 */
	@FIBPanel("Fib/Wizard/DescribeFreeModel.fib")
	public class DescribePowerpointFreeModel extends DescribeFreeModel {
	}

}
