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

package org.openflexo.fme.view;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openflexo.fme.controller.FMEPerspective;
import org.openflexo.fme.controller.editor.FreeModelDiagramEditor;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.technologyadapter.diagram.controller.DiagramTechnologyAdapterController;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;
import org.openflexo.view.controller.model.FlexoPerspective;

@SuppressWarnings("serial")
public class FMEDiagramFreeModelModuleView extends JPanel implements ModuleView<FMEFreeModelInstance>, PropertyChangeListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMEDiagramFreeModelModuleView.class.getPackage().getName());

	private final FreeModelDiagramEditor editor;
	private final FMEPerspective perspective;

	private final JPanel bottomPanel;

	public FMEDiagramFreeModelModuleView(FreeModelDiagramEditor editor, FMEPerspective perspective) {
		super();
		setLayout(new BorderLayout());
		this.editor = editor;
		this.perspective = perspective;
		add(editor.getToolsPanel(), BorderLayout.NORTH);
		add(new JScrollPane(editor.getDrawingView()), BorderLayout.CENTER);

		bottomPanel = new JPanel(new BorderLayout());

		bottomPanel.add(editor.getFlexoController().makeInfoLabel(), BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		editor.getFlexoController().setInfoMessage("Free Modelling Editor - CTRL-drag to draw edges", false);

		validate();

		getRepresentedObject().getPropertyChangeSupport().addPropertyChangeListener(getRepresentedObject().getDeletedProperty(), this);
	}

	public FreeModelDiagramEditor getEditor() {
		return editor;
	}

	@Override
	public FMEPerspective getPerspective() {
		return perspective;
	}

	@Override
	public void deleteModuleView() {
		getRepresentedObject().getPropertyChangeSupport().removePropertyChangeListener(getRepresentedObject().getDeletedProperty(), this);
		getEditor().getFlexoController().removeModuleView(this);
		getEditor().delete();
	}

	@Override
	public FMEDiagramFreeModelInstance getRepresentedObject() {
		return editor.getDiagramFreeModelInstance();
	}

	@Override
	public boolean isAutoscrolled() {
		return true;
	}

	@Override
	public void willHide() {

		System.out.println("FreeModelModuleView WILL HIDE !!!!!!");

		getEditor().getFlexoController().getEditingContext().unregisterPasteHandler(getEditor().getPasteHandler());

		bottomPanel.remove(getDiagramTechnologyAdapterController(getEditor().getFlexoController()).getScaleSelector().getComponent());

		perspective.closeFreeModelBrowsers();
		perspective.setTopRightView(null);
		perspective.setBottomRightView(null);
		getEditor().getFlexoController().getControllerModel().setRightViewVisible(false);

	}

	@Override
	public void willShow() {

		System.out.println("FreeModelModuleView WILL SHOW !!!!!!");

		getEditor().getFlexoController().getEditingContext().registerPasteHandler(getEditor().getPasteHandler());

		bottomPanel.add(getDiagramTechnologyAdapterController(getEditor().getFlexoController()).getScaleSelector().getComponent(),
				BorderLayout.EAST);

		perspective.setTopRightView(getEditor().getPaletteView());
		perspective.setBottomRightView(getPerspective().getInspectorPanelGroup());
		getEditor().getFlexoController().getControllerModel().setRightViewVisible(true);

		getPerspective().focusOnObject(getRepresentedObject());
	}

	public DiagramTechnologyAdapterController getDiagramTechnologyAdapterController(FlexoController controller) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		return tacService.getTechnologyAdapterController(DiagramTechnologyAdapterController.class);
	}

	@Override
	public void show(final FlexoController controller, FlexoPerspective perspective) {

		// Sets palette view of editor to be the top right view
		perspective.setTopRightView(getEditor().getPaletteView());
		// perspective.setHeader(((FreeDiagramModuleView) moduleView).getEditor().getS());

		getDiagramTechnologyAdapterController(controller).getInspectors().attachToEditor(getEditor());
		getDiagramTechnologyAdapterController(controller).getDialogInspectors().attachToEditor(getEditor());
		getDiagramTechnologyAdapterController(controller).getScaleSelector().attachToEditor(getEditor());

		/*JScrollPane scrollPane = new JScrollPane(getDiagramTechnologyAdapterController(controller).getInspectors().getPanelGroup(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);*/

		perspective.setBottomRightView(getPerspective().getInspectorPanelGroup());

		SwingUtilities.invokeLater(() -> {
			// Force right view to be visible
			controller.getControllerModel().setRightViewVisible(true);
		});

		controller.getControllerModel().setRightViewVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getRepresentedObject() && evt.getPropertyName().equals(getRepresentedObject().getDeletedProperty())) {
			deleteModuleView();
		}
	}
}
