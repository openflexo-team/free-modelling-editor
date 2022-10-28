/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

import javax.swing.ImageIcon;

import org.openflexo.icon.IconMarker;
import org.openflexo.icon.ImageIconResource;
import org.openflexo.rm.ResourceLocator;

public class FMEIconLibrary {

	// Module icons
	public static final ImageIcon FME_SMALL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/module-fme-16.png"));
	public static final ImageIcon FME_MEDIUM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/module-fme-32.png"));
	public static final ImageIcon FME_MEDIUM_ICON_WITH_HOVER = new ImageIconResource(
			ResourceLocator.locateResource("Icons/module-fme-hover-32.png"));
	public static final ImageIcon FME_BIG_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/module-fme-hover-64.png"));

	public static final ImageIcon COPY_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Copy.png"));
	public static final ImageIcon PASTE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Paste.png"));
	public static final ImageIcon CUT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Cut.png"));
	public static final ImageIcon DELETE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Delete.png"));

	public static final ImageIconResource FREE_MODEL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FreeModel.png"));
	public static final ImageIconResource FREE_MODEL_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/FreeModel_32x32.png"));
	public static final ImageIconResource FREE_MODEL_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/FreeModel_64x64.png"));

	public static final IconMarker FME_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/FreeModelMarker.png")), 8, 0);
	public static final IconMarker FME_BIG_MARKER = new IconMarker(FME_MEDIUM_ICON, 32, 0);

	public static final ImageIcon CONCEPT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/EClass.gif"));
	public static final ImageIcon INSTANCE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/EObject.gif"));

	public static final ImageIcon DIAGRAM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Diagram.png"));
	public static final ImageIcon DIAGRAM_MEDIUM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Diagram32x32.png"));
	public static final ImageIcon DIAGRAM_BIG_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Diagram64x64.png"));
}
