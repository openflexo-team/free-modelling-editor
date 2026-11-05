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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openflexo.fme.FMETechnologyExtensions;
import org.openflexo.fme.controller.FMEController;
import org.openflexo.fme.controller.FMEPerspective;
import org.openflexo.fme.pptx.PowerpointFMEExtension;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.selection.SelectionListener;
import org.openflexo.selection.SelectionManager;
import org.openflexo.swing.FlexoCollabsiblePanelGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;
import org.openflexo.technologyadapter.powerpoint.view.PowerpointSlideshowView;
import org.openflexo.technologyadapter.powerpoint.view.PowerpointSlideshowView.ThumbnailsPosition;
import org.openflexo.view.SelectionSynchronizedModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * The view of a free model instance annotating a slideshow: the slideshow view of pptx-ta-ui (the shown slide, the thumbnails in a row
 * below it) with the overlay of the concepts ({@link ConceptDecorationProvider}).<br>
 * The left column of the perspective is the one of every free model instance. The right column shows the browser of the shown slide on
 * top, and below the inspector of the represented concept (opened) and the PowerPoint inspector of the selected shape
 * ({@link PowerpointShapeInspectors}).
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class FMEPowerpointFreeModelModuleView extends JPanel
		implements SelectionSynchronizedModuleView<FMEPowerpointFreeModelInstance>, PropertyChangeListener {

	private final FMEPowerpointFreeModelInstance freeModelInstance;
	private final FMEController controller;
	private final FMEPerspective perspective;
	private final PowerpointSlideshowView slideshowView;
	private final ConceptDecorationProvider decorations;

	public FMEPowerpointFreeModelModuleView(FMEPowerpointFreeModelInstance freeModelInstance, FMEController controller,
			FMEPerspective perspective) {
		super(new BorderLayout());
		this.freeModelInstance = freeModelInstance;
		this.controller = controller;
		this.perspective = perspective;
		// The thumbnails in a row below the slide leave the width to the slide, between the two columns of the perspective
		slideshowView = new PowerpointSlideshowView(freeModelInstance.getSlideshow(), controller, perspective, ThumbnailsPosition.BOTTOM);
		// The browser of the slide is in the right column
		slideshowView.setInstallsBrowser(false);
		decorations = new ConceptDecorationProvider(freeModelInstance);
		slideshowView.setDecorationProvider(decorations);
		// An instance selected in a browser selects its shapes in the slides, with their handles
		slideshowView.setRepresentedElements(decorations::getAnnotatedElements);
		add(slideshowView, BorderLayout.CENTER);
		freeModelInstance.getPropertyChangeSupport().addPropertyChangeListener(this);
	}

	public PowerpointSlideshowView getSlideshowView() {
		return slideshowView;
	}

	public ConceptDecorationProvider getDecorations() {
		return decorations;
	}

	@Override
	public FMEPowerpointFreeModelInstance getRepresentedObject() {
		return freeModelInstance;
	}

	@Override
	public FMEPerspective getPerspective() {
		return perspective;
	}

	private FlexoCollabsiblePanelGroup getInspectorPanelGroup() {
		return perspective.getInspectorPanelGroup(FMETechnologyExtensions.getExtension(PowerpointFMEExtension.IDENTIFIER));
	}

	/**
	 * The overlay follows the instances of the free model instance; the view goes with it
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(freeModelInstance.getDeletedProperty())) {
			deleteModuleView();
		}
		else {
			decorations.refresh();
		}
	}

	@Override
	public void deleteModuleView() {
		freeModelInstance.getPropertyChangeSupport().removePropertyChangeListener(this);
		slideshowView.setDecorationProvider(null);
		slideshowView.setRepresentedElements(null);
		slideshowView.deleteModuleView();
		controller.removeModuleView(this);
	}

	@Override
	public boolean isAutoscrolled() {
		return false;
	}

	@Override
	public void willShow() {
		perspective.setTopRightView(slideshowView.getSlideBrowser());
		perspective.setBottomRightView(getInspectorPanelGroup());
		controller.getControllerModel().setRightViewVisible(true);
		perspective.focusOnObject(freeModelInstance);
		updateHighlights();
		decorations.refresh();
	}

	@Override
	public void willHide() {
		perspective.closeFreeModelBrowsers();
		perspective.setTopRightView(null);
		perspective.setBottomRightView(null);
		controller.getControllerModel().setRightViewVisible(false);
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
		perspective.setTopRightView(slideshowView.getSlideBrowser());
		perspective.setBottomRightView(getInspectorPanelGroup());
		SwingUtilities.invokeLater(() -> controller.getControllerModel().setRightViewVisible(true));
		controller.getControllerModel().setRightViewVisible(true);
	}

	@Override
	public List<SelectionListener> getSelectionListeners() {
		Vector<SelectionListener> reply = new Vector<>();
		reply.add(this);
		return reply;
	}

	/**
	 * An instance chosen in a browser brings the slide of the first shape standing for it (the editors of the slides select those shapes)
	 */
	@Override
	public void fireObjectSelected(FlexoObject object) {
		updateHighlights();
		if (object instanceof FlexoConceptInstance) {
			List<PowerpointSlideElement> elements = decorations.getAnnotatedElements(object);
			if (!elements.isEmpty()) {
				slideshowView.fireObjectSelected(elements.get(0));
			}
			return;
		}
		slideshowView.fireObjectSelected(object);
	}

	@Override
	public void fireObjectDeselected(FlexoObject object) {
		updateHighlights();
	}

	@Override
	public void fireResetSelection() {
		updateHighlights();
	}

	@Override
	public void fireBeginMultipleSelection() {
	}

	@Override
	public void fireEndMultipleSelection() {
		updateHighlights();
	}

	/**
	 * The annotated shapes standing for what is selected are highlighted strongly, the others lightly; the PowerPoint inspector shows the
	 * selected PowerPoint object, or the first shape standing for the selected instance
	 */
	private void updateHighlights() {
		List<FlexoObject> selection = getSelectionManager() != null ? getSelectionManager().getSelection() : null;
		decorations.setSelection(selection);
		if (getInspectorPanelGroup() instanceof PowerpointShapeInspectors) {
			((PowerpointShapeInspectors) getInspectorPanelGroup()).inspect(decorations.getInspectedPowerpointObject(selection));
		}
	}

	@Override
	public SelectionManager getSelectionManager() {
		return controller.getSelectionManager();
	}

	@Override
	public Vector<FlexoObject> getSelection() {
		return getSelectionManager().getSelection();
	}

	@Override
	public void resetSelection() {
		getSelectionManager().resetSelection();
	}

	@Override
	public void addToSelected(FlexoObject object) {
		getSelectionManager().addToSelected(object);
	}

	@Override
	public void removeFromSelected(FlexoObject object) {
		getSelectionManager().removeFromSelected(object);
	}

	@Override
	public void addToSelected(Vector<? extends FlexoObject> objects) {
		getSelectionManager().addToSelected(objects);
	}

	@Override
	public void removeFromSelected(Vector<? extends FlexoObject> objects) {
		getSelectionManager().removeFromSelected(objects);
	}

	@Override
	public void setSelectedObjects(Vector<? extends FlexoObject> objects) {
		getSelectionManager().setSelectedObjects(objects);
	}

	@Override
	public FlexoObject getFocusedObject() {
		return getSelectionManager().getFocusedObject();
	}

	@Override
	public boolean mayRepresents(FlexoObject anObject) {
		if (anObject == freeModelInstance || slideshowView.mayRepresents(anObject)) {
			return true;
		}
		return anObject instanceof FlexoConceptInstance
				&& ((FlexoConceptInstance) anObject).getVirtualModelInstance() == freeModelInstance.getAccessedVirtualModelInstance();
	}

}
