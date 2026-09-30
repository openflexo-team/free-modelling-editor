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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.openflexo.foundation.fml.FMLKeywords;

/**
 * Turns the names a user types in the free modelling editor into names FML accepts.
 *
 * <p>
 * The free modelling editor writes what the user types straight into FML, whose grammar is strict about case: a concept name starts with
 * an upper case letter, a property name with a lower case one, and an enum value is made of upper case letters, digits and underscores
 * only. A name breaking one of these rules is serialized all the same, and the whole compilation unit then fails to parse when it is loaded
 * again - every concept of the conceptual model is lost, silently. So every name is normalized before it reaches the model.
 *
 * <p>
 * Only ASCII letters are kept for names: accents are stripped, and any other character separates words.
 *
 * @author sylvain
 */
public class FMENames {

	private FMENames() {
	}

	// FML identifiers, as declared by fml.sablecc: uidentifier for a concept, lidentifier for a property
	private static final Pattern CONCEPT_IDENTIFIER = Pattern.compile("[A-Z][\\p{L}\\p{Nd}$_]*");
	private static final Pattern PROPERTY_IDENTIFIER = Pattern.compile("[a-z_][\\p{L}\\p{Nd}$_]*");

	/**
	 * Whether supplied name can be written as is as the name of a concept: an FML <code>uidentifier</code> (an upper case ASCII letter
	 * followed by letters, digits, '$' or '_'), which is not a keyword.<br>
	 * Unlike {@link #conceptName(String)}, which turns what was typed into a name, this only tells whether a name is one.
	 */
	public static boolean isValidConceptName(String name) {
		return name != null && CONCEPT_IDENTIFIER.matcher(name).matches() && !FMLKeywords.isKeyword(name);
	}

	/**
	 * Whether supplied name can be written as is as the name of a property: an FML <code>lidentifier</code> (a lower case ASCII letter or
	 * '_' followed by letters, digits, '$' or '_'), which is not a keyword.
	 */
	public static boolean isValidPropertyName(String name) {
		return name != null && PROPERTY_IDENTIFIER.matcher(name).matches() && !FMLKeywords.isKeyword(name);
	}

	/**
	 * A concept name: UpperCamelCase, starting with a letter. <code>"my concept"</code> gives <code>"MyConcept"</code>.
	 *
	 * @return null when nothing usable remains
	 */
	public static String conceptName(String typed) {
		String camel = camelCase(typed);
		if (camel == null) {
			return null;
		}
		if (!Character.isLetter(camel.charAt(0))) {
			camel = "C" + camel;
		}
		return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
	}

	/**
	 * A property name: lowerCamelCase, starting with a letter. <code>"Birth date"</code> gives <code>"birthDate"</code>.
	 *
	 * @return null when nothing usable remains
	 */
	public static String propertyName(String typed) {
		String camel = camelCase(typed);
		if (camel == null) {
			return null;
		}
		if (!Character.isLetter(camel.charAt(0))) {
			camel = "p" + camel;
		}
		return Character.toLowerCase(camel.charAt(0)) + camel.substring(1);
	}

	/**
	 * An enum value: upper case letters, digits and underscores. <code>"light blue"</code> gives <code>"LIGHT_BLUE"</code>.
	 *
	 * @return null when nothing usable remains
	 */
	public static String enumValue(String typed) {
		String ascii = ascii(typed);
		if (ascii == null) {
			return null;
		}
		StringBuilder returned = new StringBuilder();
		for (char c : ascii.trim().toUpperCase().toCharArray()) {
			returned.append((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ? c : '_');
		}
		return returned.length() > 0 ? returned.toString() : null;
	}

	/**
	 * The enum values of a comma-separated list, normalized, empty ones and duplicates dropped
	 */
	public static List<String> enumValues(String commaSeparated) {
		List<String> returned = new ArrayList<>();
		if (commaSeparated != null) {
			StringTokenizer st = new StringTokenizer(commaSeparated, ",");
			while (st.hasMoreTokens()) {
				String value = enumValue(st.nextToken());
				if (value != null && !returned.contains(value)) {
					returned.add(value);
				}
			}
		}
		return returned;
	}

	/**
	 * Words of supplied name glued together, each capitalized but the first, which keeps its case - or null when there is no word
	 */
	private static String camelCase(String typed) {
		String ascii = ascii(typed);
		if (ascii == null) {
			return null;
		}
		StringBuilder returned = new StringBuilder();
		boolean newWord = false;
		for (char c : ascii.toCharArray()) {
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
				returned.append(newWord && returned.length() > 0 ? Character.toUpperCase(c) : c);
				newWord = false;
			}
			else {
				newWord = true;
			}
		}
		return returned.length() > 0 ? returned.toString() : null;
	}

	private static String ascii(String typed) {
		if (typed == null) {
			return null;
		}
		// "é" becomes "e" followed by a combining accent, which is then dropped
		return Normalizer.normalize(typed, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
	}
}
