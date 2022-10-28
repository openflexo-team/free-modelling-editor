/*
 * (c) Copyright 2013- Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.fme.view;

import javax.swing.ImageIcon;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.IconFactory;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * View for a {@link FMEFreeModel}
 */
@SuppressWarnings("serial")
public class FMEFreeModelModuleView extends FIBModuleView<FMEFreeModel> {

	public static Resource FME_FREE_MODEL_MODULE_VIEW_FIB = ResourceLocator.locateResource("Fib/FMEFreeModelPanel.fib");

	private final FlexoPerspective perspective;

	public FMEFreeModelModuleView(FMEFreeModel freeModel, FlexoController controller, FlexoPerspective perspective) {
		super(freeModel, controller, FME_FREE_MODEL_MODULE_VIEW_FIB, controller.getModule().getLocales());
		this.perspective = perspective;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	public static class FMEFreeModelModuleViewFIBController extends FlexoFIBController {
		public FMEFreeModelModuleViewFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public FMEFreeModelModuleViewFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
			super(component, viewFactory, controller);
		}

		public void instantiateNewDiagramFreeModel(FMEFreeModel freeModel) {
			InstantiateFMEDiagramFreeModel action = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(freeModel, null, getEditor());
			action.doAction();
		}

		public ImageIcon getFreeModelIcon() {
			return IconFactory.getImageIcon(FMEIconLibrary.DIAGRAM_BIG_ICON, FMEIconLibrary.FME_BIG_MARKER);
		}

		public void selectFreeModelInstance(FMEFreeModelInstance modelInstance) {
			getFlexoController().selectAndFocusObject(modelInstance);
		}

	}

}
