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

package org.openflexo.fme.controller;

import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.widget.FIBCompilationUnitBrowserFIBController.ViewMode;
import org.openflexo.fml.controller.widget.ViewModeButtons;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;

/**
 * Controller of the browser of the concepts of the conceptual model, which shows them either "flat", organized by inheritance or organized
 * by containment (the same three modes as the browser of the compilation units of the FML technology adapter).
 * 
 * @author sylvain
 */
public class FIBConceptBrowserFIBController extends FMEFIBController {

	private ViewMode viewMode = ViewMode.Hierarchical;

	private final ViewModeButtons viewModeButtons = new ViewModeButtons(this, "FlatIcon", "HierarchicalIcon", "EmbeddingIcon");

	public FIBConceptBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
		// The buttons exist once the view is built
		SwingUtilities.invokeLater(() -> setViewMode(ViewMode.Hierarchical, true));
	}

	public void setViewModeToEmbedding() {
		setViewMode(ViewMode.Embedding);
	}

	public void setViewModeToHierarchical() {
		setViewMode(ViewMode.Hierarchical);
	}

	public void setViewModeToFlat() {
		setViewMode(ViewMode.Flat);
	}

	public ViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(ViewMode viewMode) {
		setViewMode(viewMode, false);
	}

	public void setViewMode(ViewMode viewMode, boolean force) {
		if (force || (viewMode == null && this.viewMode != null) || (viewMode != null && !viewMode.equals(this.viewMode))) {
			ViewMode oldValue = this.viewMode;
			this.viewMode = viewMode;
			getPropertyChangeSupport().firePropertyChange("viewMode", oldValue, viewMode);
			viewModeButtons.select(viewMode);
		}
	}

}
