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

import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.module.NatureSpecificModule;

/**
 * ViewPointModeller module
 * 
 * @author sylvain
 */
public class FreeModellingEditor extends NatureSpecificModule<FMEModule, FreeModellingProjectNature> {

	public static FreeModellingEditor INSTANCE;

	public FreeModellingEditor() {
		super(FMEModule.FME_MODULE_NAME, FMEModule.FME_MODULE_SHORT_NAME, FMEModule.class, FMEPreferences.class,
				"modules/freemodellingeditor", "10801", "fme", FMEIconLibrary.FME_SMALL_ICON, FMEIconLibrary.FME_MEDIUM_ICON,
				FMEIconLibrary.FME_MEDIUM_ICON_WITH_HOVER, FMEIconLibrary.FME_BIG_ICON, FreeModellingProjectNature.class);
		// WE set it now, because we are sure the ServiceManager did this call first
		// WE want to avoid twice defined objects
		INSTANCE = this;
	}

	@Override
	public String getHTMLDescription() {
		return "<html>The <b>FreeModellingEditor</b> highlights your business concepts from simple drawings, and allows you to initialize a tooling from your design and your proper way to represent it.<br>"
				+ "This module is a good starting point for discovering Openflexo tooling or to initiate a modeling activity.</html>";
	}
}
