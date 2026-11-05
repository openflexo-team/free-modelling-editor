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
package org.openflexo.fme.pptx.view;

import javax.swing.BorderFactory;

import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.inspector.FIBFlexoConceptInstanceInspectorPanel;
import org.openflexo.inspector.ModuleInspectorController;
import org.openflexo.inspector.ModuleInspectorController.EmptySelectionActivated;
import org.openflexo.inspector.ModuleInspectorController.InspectedObjectChanged;
import org.openflexo.inspector.ModuleInspectorController.InspectorSwitching;
import org.openflexo.swing.FlexoCollabsiblePanel;
import org.openflexo.swing.FlexoCollabsiblePanelGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObject;
import org.openflexo.view.controller.FlexoController;

/**
 * The inspectors the PowerPoint technology adds to the right column of the FreeModellingEditor, below the inspector of the represented
 * concept that the perspective inserts first (and opens): the PowerPoint inspector of the selected shape, the counterpart of the
 * graphical inspectors of the Diagram technology.<br>
 * The panel reuses the inspector panel of the platform, but it does not follow the inspector of the module, which shows the instance of
 * the concept: the module view tells it what is selected in the slides ({@link #inspect(PowerpointObject)}).
 *
 * @author sylvain
 *
 */
@SuppressWarnings("serial")
public class PowerpointShapeInspectors extends FlexoCollabsiblePanelGroup {

	private final ShapeInspectorPanel shapeInspectorPanel;
	private final FlexoCollabsiblePanel shapePanel;

	public PowerpointShapeInspectors(FlexoController controller) {
		shapeInspectorPanel = new ShapeInspectorPanel(controller.getModuleInspectorController());
		shapeInspectorPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
		shapePanel = insertContentsAtIndex(controller.getModuleLocales().localizedForKey("powerpoint_shape"), shapeInspectorPanel, 0);
	}

	public FlexoCollabsiblePanel getShapePanel() {
		return shapePanel;
	}

	/**
	 * The object the shape inspector shows, null when it shows nothing
	 */
	public PowerpointObject getInspectedObject() {
		return shapeInspectorPanel.inspectedObject;
	}

	/**
	 * Shows the PowerPoint inspector of supplied object, or nothing when it is null
	 */
	public void inspect(PowerpointObject object) {
		shapeInspectorPanel.inspect(object);
	}

	public void delete() {
		shapeInspectorPanel.delete();
	}

	/**
	 * The inspector panel of the platform, fed by hand instead of by the inspector controller of the module (which only lends it the
	 * inspectors)
	 */
	private static class ShapeInspectorPanel extends FIBFlexoConceptInstanceInspectorPanel {

		private final ModuleInspectorController inspectorController;
		private FIBInspector inspector;
		private PowerpointObject inspectedObject;

		ShapeInspectorPanel(ModuleInspectorController inspectorController) {
			super(inspectorController);
			this.inspectorController = inspectorController;
			// The inspector of the module shows the instance of the concept, not the shape
			inspectorController.deleteObserver(this);
		}

		void inspect(PowerpointObject object) {
			if (object == inspectedObject) {
				return;
			}
			inspectedObject = object;
			FIBInspector newInspector = object != null ? inspectorController.inspectorForObject(object) : null;
			if (newInspector == null) {
				inspector = null;
				update(null, new EmptySelectionActivated());
				return;
			}
			if (newInspector != inspector) {
				inspector = newInspector;
				update(null, new InspectorSwitching(newInspector));
			}
			update(null, new InspectedObjectChanged(object));
		}
	}

}
