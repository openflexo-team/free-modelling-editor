/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Free-modelling-editor, a component of the software infrastructure
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.localization.Language;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.rm.ResourceLocator;

/**
 * The messages the wizards of the free modelling editor show to the user are defined by its dictionaries
 */
public class TestFMELocalization {

	@Test
	public void testWizardMessagesAreDefined() {
		LocalizedDelegateImpl locales = new LocalizedDelegateImpl(ResourceLocator.locateResource("FlexoLocalization/FreeModellingEditor"),
				null, false, false);
		for (String key : new String[] { "invalid_concept_name", "invalid_property_name",
				"duplicate_property_name", "no_source_concept_defined", "no_destination_concept_defined", "source_role_name", "destination_role_name", "invalid_relation_end_name", "same_relation_end_names", "concept_instance_role_name", "invalid_concept_role_name", "concept_role_name_already_used", "no_string_property_for_the_label", "bind_shape_label_to", "no_concept_name_defined" }) {
			String english = locales.localizedForKeyAndLanguage(key, Language.ENGLISH);
			assertNotNull(key, english);
			assertTrue("'" + key + "' is not defined in English: " + english, !english.equals(key) && english.trim().length() > 0);
			String french = locales.localizedForKeyAndLanguage(key, Language.FRENCH);
			assertTrue("'" + key + "' is not defined in French: " + french, !french.equals(key) && french.trim().length() > 0);
		}
		assertEquals("Bind shape label to", locales.localizedForKeyAndLanguage("bind_shape_label_to", Language.ENGLISH));
	}
}
