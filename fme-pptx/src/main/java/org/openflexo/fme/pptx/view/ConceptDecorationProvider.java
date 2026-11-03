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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;
import org.openflexo.technologyadapter.powerpoint.view.PowerpointElementDecorationProvider;

/**
 * The overlay of an annotated slideshow: a shape standing for an instance of a concept gets a border of the color of the concept, and a
 * badge with the name of the concept. A shape which is not annotated is left as it is.<br>
 * The color of a concept is the one of its rank in the conceptual model, in a fixed palette.
 * 
 * @author sylvain
 * 
 */
public class ConceptDecorationProvider extends PowerpointElementDecorationProvider {

	/** Colors readable over most slides, by rank of the concept in the conceptual model */
	public static final Color[] PALETTE = { new Color(0xE6194B), new Color(0x3CB44B), new Color(0x4363D8), new Color(0xF58231),
			new Color(0x911EB4), new Color(0x42D4F4), new Color(0xF032E6), new Color(0x9A6324), new Color(0x469990), new Color(0x800000) };

	private final FMEPowerpointFreeModelInstance freeModelInstance;

	// The concept each annotated element stands for an instance of, computed again after each refresh
	private Map<Object, FlexoConcept> concepts;

	public ConceptDecorationProvider(FMEPowerpointFreeModelInstance freeModelInstance) {
		this.freeModelInstance = freeModelInstance;
	}

	@Override
	public void refresh() {
		concepts = null;
		super.refresh();
	}

	/**
	 * The concept supplied element stands for an instance of, or null
	 */
	public FlexoConcept getConcept(PowerpointSlideElement element) {
		if (concepts == null) {
			concepts = computeConcepts();
		}
		return concepts.get(element);
	}

	private Map<Object, FlexoConcept> computeConcepts() {
		Map<Object, FlexoConcept> returned = new HashMap<>();
		if (freeModelInstance.getAccessedVirtualModelInstance() != null) {
			for (FlexoConceptInstance grInstance : freeModelInstance.getAccessedVirtualModelInstance().getFlexoConceptInstances()) {
				Object element = freeModelInstance.getRepresentationElement(grInstance);
				FlexoConceptInstanceRole role = FMEFreeModel.conceptRole(grInstance.getFlexoConcept());
				if (element != null && role != null && role.getFlexoConceptType() != null
						&& FMEFreeModel.representedInstance(grInstance) != null) {
					returned.put(element, role.getFlexoConceptType());
				}
			}
		}
		return returned;
	}

	@Override
	public Decoration getDecoration(PowerpointSlideElement element) {
		FlexoConcept concept = getConcept(element);
		if (concept == null) {
			return null;
		}
		return new Decoration(colorOf(concept), null, concept.getName());
	}

	/**
	 * The color of supplied concept: the one of its rank in the conceptual model
	 */
	public Color colorOf(FlexoConcept concept) {
		int rank = 0;
		if (freeModelInstance.getConceptualModel() != null && freeModelInstance.getConceptualModel().getAccessedVirtualModel() != null) {
			List<FlexoConcept> all = freeModelInstance.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts();
			rank = Math.max(0, all.indexOf(concept));
		}
		return PALETTE[rank % PALETTE.length];
	}

}
