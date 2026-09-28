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
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

/**
 * Names typed in the free modelling editor become names the FML grammar accepts: a concept starts with an upper case letter, a property
 * with a lower case one, and an enum value is made of upper case letters, digits and underscores
 */
public class TestFMENames {

	@Test
	public void testConceptName() {
		assertEquals("TutuConcept", FMENames.conceptName("TutuConcept"));
		assertEquals("Person", FMENames.conceptName("person"));
		assertEquals("MyConcept", FMENames.conceptName("my concept"));
		assertEquals("Etudiant", FMENames.conceptName("étudiant"));
		assertEquals("C2d", FMENames.conceptName("2d"));
		assertNull(FMENames.conceptName(" - "));
	}

	@Test
	public void testPropertyName() {
		assertEquals("comment", FMENames.propertyName("comment"));
		assertEquals("comment", FMENames.propertyName("Comment"));
		assertEquals("birthDate", FMENames.propertyName("Birth date"));
		assertEquals("dateDeNaissance", FMENames.propertyName("date de naissance"));
		assertEquals("p1st", FMENames.propertyName("1st"));
		assertNull(FMENames.propertyName(""));
		assertNull(FMENames.propertyName(null));
	}

	@Test
	public void testEnumValues() {
		assertEquals("RED", FMENames.enumValue("red"));
		assertEquals("LIGHT_BLUE", FMENames.enumValue(" light blue "));
		assertEquals("ELEVE", FMENames.enumValue("élève"));
		assertEquals(Arrays.asList("RED", "LIGHT_BLUE", "GREEN"), FMENames.enumValues("red, light blue,green,,RED"));
	}
}
