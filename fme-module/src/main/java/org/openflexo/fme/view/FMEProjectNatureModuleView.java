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

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.FMETechnologyExtension;
import org.openflexo.fme.FMETechnologyExtensions;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBModuleView;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * Main view for FreeModellingEditor
 */
@SuppressWarnings("serial")
public class FMEProjectNatureModuleView extends FIBModuleView<FreeModellingProjectNature> {

	public static Resource FME_PROJECT_MODULE_VIEW_FIB = ResourceLocator.locateResource("Fib/FMEProjectNaturePanel.fib");

	private final FlexoPerspective perspective;

	public FMEProjectNatureModuleView(FreeModellingProjectNature nature, FlexoController controller, FlexoPerspective perspective) {
		super(nature, controller, FME_PROJECT_MODULE_VIEW_FIB, controller.getModule().getLocales());
		this.perspective = perspective;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	public static class FMEProjectNatureModuleViewFIBController extends FlexoFIBController {
		public FMEProjectNatureModuleViewFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public FMEProjectNatureModuleViewFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
			super(component, viewFactory, controller);
		}

		/**
		 * Creates a new free model, of the technology the user chooses when several are available
		 */
		public void createNewFreeModel(FreeModellingProjectNature projectNature) {
			FMETechnologyExtension extension = chooseExtension();
			if (extension != null) {
				extension.getCreateFreeModelActionType().makeNewAction(projectNature, null, getEditor()).doAction();
			}
		}

		/**
		 * Creates a new free model instance, of the technology the user chooses when several are available, choosing or creating its free
		 * model
		 */
		public void createNewFreeModelInstance(FreeModellingProjectNature projectNature) {
			FMETechnologyExtension extension = chooseExtension();
			if (extension != null) {
				extension.getInstantiateFreeModelActionType().makeNewAction(projectNature, null, getEditor()).doAction();
			}
		}

		/**
		 * Creates a new instance of supplied free model
		 */
		public void instantiateFreeModel(FMEFreeModel freeModel) {
			FMETechnologyExtension extension = FMETechnologyExtensions.getExtension(freeModel);
			if (extension != null) {
				extension.getInstantiateFreeModelActionType().makeNewAction(freeModel, null, getEditor()).doAction();
			}
		}

		/**
		 * The technology extension to use: the only one when there is only one, the one the user chooses otherwise (null if cancelled)
		 */
		private FMETechnologyExtension chooseExtension() {
			List<FMETechnologyExtension> extensions = FMETechnologyExtensions.getExtensions();
			if (extensions.size() <= 1) {
				return extensions.isEmpty() ? null : extensions.get(0);
			}
			String[] names = new String[extensions.size()];
			for (int i = 0; i < names.length; i++) {
				names[i] = getFlexoController().getModuleLocales().localizedForKey(extensions.get(i).getIdentifier());
			}
			Object chosen = JOptionPane.showInputDialog(FlexoFrame.getActiveFrame(), getFlexoController().getModuleLocales().localizedForKey("choose_technology"),
					getFlexoController().getModuleLocales().localizedForKey("free_model"), JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(chosen)) {
					return extensions.get(i);
				}
			}
			return null;
		}

		public ImageIcon getProjectIcon() {
			return IconFactory.getImageIcon(IconLibrary.OPENFLEXO_NOTEXT_64, FMEIconLibrary.FME_BIG_MARKER);
		}

		public void selectFreeModel(FMEFreeModel model) {
			getFlexoController().selectAndFocusObject(model);
		}

	}

}
