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

package org.openflexo.fme;

import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.fme.controller.FMEController;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.module.FlexoModule;
import org.openflexo.view.controller.FlexoController;

/**
 * ViewPointModeller module
 * 
 * @author sylvain
 */
public class FMEModule extends FlexoModule<FMEModule> {

	private static final Logger logger = Logger.getLogger(FMEModule.class.getPackage().getName());

	public static final String FME_MODULE_SHORT_NAME = "FME";
	public static final String FME_MODULE_NAME = "Free Modelling Editor";

	public FMEModule(ApplicationContext applicationContext) throws Exception {
		super(applicationContext);
		// ProgressWindow.setProgressInstance(FlexoLocalization.getMainLocalizer().localizedForKey("build_editor"));
	}

	@Override
	public String getLocalizationDirectory() {
		return "FlexoLocalization/FreeModellingEditor";
	}

	/**
	 * The views of the free modelling editor show the virtual models and instances of the project: load them in the loading task
	 */
	@Override
	public void loadProjectContents(FlexoProject<?> project) {
		loadVirtualModelsAndInstances(project);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initModule() {
		super.initModule();
		TechnologyAdapterService taService = getApplicationContext().getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);
		for (FMETechnologyExtension extension : FMETechnologyExtensions.getExtensions()) {
			for (TechnologyAdapter technologyAdapter : taService.getTechnologyAdapters()) {
				if (extension.getTechnologyAdapterClass().isInstance(technologyAdapter)) {
					taService.activateTechnologyAdapter(technologyAdapter, true);
				}
			}
		}
		// Put here a code to display default view
		// getFMEController().setCurrentEditedObjectAsModuleView(getFMEController().getViewPointLibrary());
	}

	@Override
	protected FlexoController createControllerForModule() {
		return new FMEController(this);
	}

	@Override
	public FreeModellingEditor getModule() {
		return FreeModellingEditor.INSTANCE;
	}

	public FMEController getFMEController() {
		return (FMEController) getFlexoController();
	}

	@Override
	public boolean close() {
		if (getApplicationContext().getResourceManager().getUnsavedResources().size() == 0) {
			return super.close();
		}
		else {
			if (getFMEController().reviewModifiedResources()) {
				return super.close();
			}
			else {
				return false;
			}
		}
	}

	@Override
	public FMEPreferences getPreferences() {
		return (FMEPreferences) super.getPreferences();
	}

	public float getScreenshotQuality() {
		float reply = getPreferences().getScreenshotQuality();
		if (reply > 1) {
			return 1f;
		}
		if (reply < 0.1f) {
			return 0.1f;
		}
		return reply;
	}

}
