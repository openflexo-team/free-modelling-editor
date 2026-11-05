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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObject;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;
import org.openflexo.technologyadapter.powerpoint.view.PowerpointElementDecorationProvider;

/**
 * The overlay of an annotated slideshow, as a highlighter: a shape standing for an instance of a concept is painted over in transparent
 * yellow, lightly to recall that it is annotated, strongly while it is selected (a selected shape also gets the selection handles of
 * every shape). A shape which is not annotated is left as it is. Nothing is written over the shapes, which stay readable.<br>
 * A shape is selected when the selection holds the shape, the instance of its GR concept, or the instance of the concept it stands for
 * (the other shapes standing for that instance are then highlighted too).
 * 
 * @author sylvain
 * 
 */
public class ConceptDecorationProvider extends PowerpointElementDecorationProvider {

	/** Light highlight of an annotated shape which is not selected */
	public static final Color HIGHLIGHT = new Color(255, 230, 0, 50);
	/** Strong highlight of a selected annotated shape */
	public static final Color SELECTED_HIGHLIGHT = new Color(255, 230, 0, 120);

	private final FMEPowerpointFreeModelInstance freeModelInstance;

	// The annotations of the elements, computed again after each refresh
	private Map<Object, Annotation> annotations;

	private Set<Object> selection = Collections.emptySet();

	/**
	 * What an annotated element stands for
	 */
	private static class Annotation {
		private final FlexoConceptInstance grInstance;
		private final FlexoConceptInstance conceptInstance;
		private final FlexoConcept concept;

		private Annotation(FlexoConceptInstance grInstance, FlexoConceptInstance conceptInstance, FlexoConcept concept) {
			this.grInstance = grInstance;
			this.conceptInstance = conceptInstance;
			this.concept = concept;
		}
	}

	public ConceptDecorationProvider(FMEPowerpointFreeModelInstance freeModelInstance) {
		this.freeModelInstance = freeModelInstance;
	}

	@Override
	public void refresh() {
		annotations = null;
		super.refresh();
	}

	/**
	 * Highlight strongly the elements standing for supplied selected objects (shapes, instances of GR concepts, instances of concepts),
	 * lightly the other annotated elements
	 * 
	 * @param selectedObjects
	 */
	public void setSelection(Collection<?> selectedObjects) {
		Set<Object> newSelection = selectedObjects != null ? new HashSet<>(selectedObjects) : Collections.emptySet();
		if (!newSelection.equals(selection)) {
			selection = newSelection;
			// The annotations did not change: only the decorations are asked again
			super.refresh();
		}
	}

	private Annotation getAnnotation(PowerpointSlideElement element) {
		if (annotations == null) {
			annotations = computeAnnotations();
		}
		return annotations.get(element);
	}

	/**
	 * The concept supplied element stands for an instance of, or null
	 */
	public FlexoConcept getConcept(PowerpointSlideElement element) {
		Annotation annotation = getAnnotation(element);
		return annotation != null ? annotation.concept : null;
	}

	/**
	 * Whether supplied element is annotated and selected, by itself or by what it stands for
	 */
	public boolean isSelected(PowerpointSlideElement element) {
		Annotation annotation = getAnnotation(element);
		return annotation != null && (selection.contains(element) || selection.contains(annotation.grInstance)
				|| selection.contains(annotation.conceptInstance));
	}

	/**
	 * The annotated elements standing for supplied object: the instance of a GR concept or the instance of a concept (all the shapes
	 * representing it), none for any other object
	 */
	public List<PowerpointSlideElement> getAnnotatedElements(Object object) {
		if (annotations == null) {
			annotations = computeAnnotations();
		}
		List<PowerpointSlideElement> returned = new ArrayList<>();
		if (object instanceof FlexoConceptInstance) {
			for (Map.Entry<Object, Annotation> entry : annotations.entrySet()) {
				if ((entry.getValue().grInstance == object || entry.getValue().conceptInstance == object)
						&& entry.getKey() instanceof PowerpointSlideElement) {
					returned.add((PowerpointSlideElement) entry.getKey());
				}
			}
		}
		return returned;
	}

	/**
	 * What the PowerPoint inspector shows for supplied selection: the PowerPoint object selected alone, the first shape standing for the
	 * instance selected alone, nothing otherwise
	 */
	public PowerpointObject getInspectedPowerpointObject(List<?> selection) {
		if (selection == null || selection.size() != 1) {
			return null;
		}
		Object selected = selection.get(0);
		if (selected instanceof PowerpointObject) {
			return (PowerpointObject) selected;
		}
		List<PowerpointSlideElement> elements = getAnnotatedElements(selected);
		return elements.isEmpty() ? null : elements.get(0);
	}

	private Map<Object, Annotation> computeAnnotations() {
		// In the order the shapes were annotated: the first shape of an instance does not change from one call to the other
		Map<Object, Annotation> returned = new LinkedHashMap<>();
		if (freeModelInstance.getAccessedVirtualModelInstance() != null) {
			for (FlexoConceptInstance grInstance : freeModelInstance.getAccessedVirtualModelInstance().getFlexoConceptInstances()) {
				Object element = freeModelInstance.getRepresentationElement(grInstance);
				FlexoConceptInstanceRole role = FMEFreeModel.conceptRole(grInstance.getFlexoConcept());
				FlexoConceptInstance conceptInstance = FMEFreeModel.representedInstance(grInstance);
				if (element != null && role != null && role.getFlexoConceptType() != null && conceptInstance != null) {
					returned.put(element, new Annotation(grInstance, conceptInstance, role.getFlexoConceptType()));
				}
			}
		}
		return returned;
	}

	@Override
	public Decoration getDecoration(PowerpointSlideElement element) {
		if (getAnnotation(element) == null) {
			return null;
		}
		if (isSelected(element)) {
			return new Decoration(null, SELECTED_HIGHLIGHT, null);
		}
		return new Decoration(null, HIGHLIGHT, null);
	}

}
