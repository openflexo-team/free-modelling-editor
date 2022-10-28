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

package org.openflexo.fme.widget;

import java.util.logging.Logger;

import org.openflexo.fme.controller.FMEController;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBBrowserView;

/**
 * Browser allowing to browse all resources of a {@link FlexoProject}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBRepresentedConceptBrowser extends FIBBrowserView<FMEFreeModelInstance> {
	static final Logger logger = Logger.getLogger(FIBRepresentedConceptBrowser.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FIBRepresentedConceptBrowser.fib");

	public FIBRepresentedConceptBrowser(FMEFreeModelInstance freeModel, FMEController controller) {
		super(freeModel, controller, FIB_FILE, controller.getModuleLocales());
		// System.out.println("Showing browser with " + project);
	}

	public void setFreeModel(FMEFreeModelInstance freeModel) {
		setDataObject(freeModel);
	}
}
