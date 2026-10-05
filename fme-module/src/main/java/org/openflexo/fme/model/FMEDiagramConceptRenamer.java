/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.fme.model;

import java.util.ArrayList;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.technologyadapter.diagram.TypedDiagramModelSlot;
import org.openflexo.technologyadapter.diagram.fml.ConnectorRole;
import org.openflexo.technologyadapter.diagram.fml.DropScheme;
import org.openflexo.technologyadapter.diagram.fml.FMLDiagramPaletteElementBinding;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramPaletteElement;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;

/**
 * Brings up to date what a diagram-based free model ({@link FMEDiagramFreeModel}, {@link FMEPPTFreeModel}) derived, as text, from the
 * name of a concept once that concept has been renamed. None of it follows a rename by itself.
 * 
 * @author sylvain
 */
class FMEDiagramConceptRenamer {

	private FMEDiagramConceptRenamer() {
	}

	/**
	 * @param modelSlot
	 *            the diagram model slot of the free model
	 * @param grConcept
	 *            the GR concept, already renamed
	 * @param concept
	 *            the conceptual concept, already renamed
	 * @param oldConceptName
	 *            name of the conceptual concept before the rename
	 */
	static void rename(TypedDiagramModelSlot modelSlot, FlexoConcept grConcept, FlexoConcept concept, String oldConceptName) {

		String newConceptName = concept.getName();

		// The default value of the name asked when dropping is the name of the concept, as a String literal
		for (DropScheme dropScheme : grConcept.getFlexoBehaviours(DropScheme.class)) {
			FlexoBehaviourParameter parameter = dropScheme.getParameter(FMEConceptualModel.CONCEPT_NAME_PARAMETER);
			if (parameter != null && parameter.getDefaultValue() != null
					&& ("\"" + oldConceptName + "\"").equals(String.valueOf(parameter.getDefaultValue()))) {
				parameter.setDefaultValue(new DataBinding<String>("\"" + newConceptName + "\""));
			}
		}

		// The palette element: its name and, as long as nobody changed it, its text
		if (modelSlot != null) {
			for (FMLDiagramPaletteElementBinding binding : new ArrayList<>(modelSlot.getPaletteElementBindings())) {
				DiagramPaletteElement element = binding.getPaletteElement();
				if (element != null && binding.getBoundFlexoConcept() == grConcept) {
					if (oldConceptName.equals(element.getName()) && element.getPalette() != null
							&& element.getPalette().getPaletteElement(newConceptName) == null) {
						element.setName(newConceptName);
					}
					if (element.getGraphicalRepresentation() != null
							&& oldConceptName.equals(element.getGraphicalRepresentation().getText())) {
						element.getGraphicalRepresentation().setText(newConceptName);
					}
					if (element.getPalette() != null) {
						element.getPalette().setIsModified();
					}
				}
			}
		}

		// The shape (or connector) the GR concept was created with, named after the GR concept
		String oldGRName = oldConceptName + "GR";
		for (FlexoProperty<?> property : grConcept.getDeclaredProperties()) {
			if (property instanceof ShapeRole) {
				DiagramShape shape = ((ShapeRole) property).getMetamodelElement();
				if (shape != null && oldGRName.equals(shape.getName())) {
					shape.setName(grConcept.getName());
				}
			}
			else if (property instanceof ConnectorRole) {
				DiagramConnector connector = ((ConnectorRole) property).getMetamodelElement();
				if (connector != null && oldGRName.equals(connector.getName())) {
					connector.setName(grConcept.getName());
				}
			}
		}
	}

}
