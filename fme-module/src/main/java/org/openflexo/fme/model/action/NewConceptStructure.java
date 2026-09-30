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

package org.openflexo.fme.model.action;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.action.PropertyEntry;
import org.openflexo.foundation.fml.action.PropertyEntry.PropertyType;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMENames;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * The structure of a concept the user is creating: its properties (a name and a type each), and the String property giving the label of
 * its instances.<br>
 * Shared by the actions creating a concept, and edited by their wizards. By default, a concept has a <code>name</code> and a
 * <code>description</code>, both Strings, and its instances are labelled with their <code>name</code>.
 * 
 * @author sylvain
 */
public class NewConceptStructure extends PropertyChangedSupportDefaultImplementation {

	public static final String PROPERTIES_ENTRIES = "propertiesEntries";
	public static final String LABEL_PROPERTY_NAME = "labelPropertyName";
	public static final String STRING_PROPERTIES_NAMES = "stringPropertiesNames";

	private final LocalizedDelegate locales;
	private final List<PropertyEntry<?>> propertiesEntries = new ArrayList<>();
	private String labelPropertyName = FMEConceptualModel.NAME_ROLE_NAME;
	// The name each entry had when last seen: renaming the entry giving the label makes the label follow
	private final Map<PropertyEntry<?>, String> knownNames = new IdentityHashMap<>();

	public NewConceptStructure() {
		// The entries only use their locales for the progress messages of the actions creating the properties
		this.locales = FlexoLocalization.getMainLocalizer();
		propertiesEntries.add(newPrimitiveEntry(FMEConceptualModel.NAME_ROLE_NAME));
		propertiesEntries.add(newPrimitiveEntry(FMEConceptualModel.DESCRIPTION_ROLE_NAME));
	}

	private PropertyEntry<?> newPrimitiveEntry(String name) {
		PropertyEntry<?> returned = new PropertyEntry<>(name, locales, null);
		returned.setType(String.class);
		returned.setPropertyType(PropertyType.PRIMITIVE);
		knownNames.put(returned, name);
		returned.getPropertyChangeSupport().addPropertyChangeListener(evt -> entryEdited(returned));
		return returned;
	}

	public List<PropertyEntry<?>> getPropertiesEntries() {
		return propertiesEntries;
	}

	public PropertyEntry<?> newPropertyEntry() {
		PropertyEntry<?> returned = newPrimitiveEntry("property" + (propertiesEntries.size() + 1));
		propertiesEntries.add(returned);
		propertiesChanged(returned, null);
		return returned;
	}

	public void deletePropertyEntry(PropertyEntry<?> entry) {
		if (entry != null) {
			propertiesEntries.remove(entry);
			knownNames.remove(entry);
			entry.delete();
			propertiesChanged(null, entry);
		}
	}

	public void propertyFirst(PropertyEntry<?> entry) {
		propertiesEntries.remove(entry);
		propertiesEntries.add(0, entry);
		propertiesChanged(null, null);
	}

	public void propertyUp(PropertyEntry<?> entry) {
		int index = propertiesEntries.indexOf(entry);
		if (index > 0) {
			propertiesEntries.remove(entry);
			propertiesEntries.add(index - 1, entry);
			propertiesChanged(null, null);
		}
	}

	public void propertyDown(PropertyEntry<?> entry) {
		int index = propertiesEntries.indexOf(entry);
		if (index > -1 && index < propertiesEntries.size() - 1) {
			propertiesEntries.remove(entry);
			propertiesEntries.add(index + 1, entry);
			propertiesChanged(null, null);
		}
	}

	public void propertyLast(PropertyEntry<?> entry) {
		propertiesEntries.remove(entry);
		propertiesEntries.add(entry);
		propertiesChanged(null, null);
	}

	private void propertiesChanged(PropertyEntry<?> added, PropertyEntry<?> removed) {
		getPropertyChangeSupport().firePropertyChange(PROPERTIES_ENTRIES, removed, added);
		getPropertyChangeSupport().firePropertyChange(STRING_PROPERTIES_NAMES, null, getStringPropertiesNames());
		// The label property is one of the properties: it must still exist
		if (!getStringPropertiesNames().contains(labelPropertyName)) {
			setLabelPropertyName(getStringPropertiesNames().isEmpty() ? null : getStringPropertiesNames().get(0));
		}
	}

	/**
	 * Called when an entry has been edited (its name, its type...): the candidates for the label property may have changed
	 */
	private void entryEdited(PropertyEntry<?> entry) {
		String formerName = knownNames.get(entry);
		String newName = entry.getName();
		if (formerName != null && !formerName.equals(newName)) {
			knownNames.put(entry, newName);
			if (formerName.equals(labelPropertyName)) {
				setLabelPropertyName(newName);
			}
		}
		getPropertyChangeSupport().firePropertyChange(STRING_PROPERTIES_NAMES, null, getStringPropertiesNames());
	}

	/**
	 * The names of the String properties: the candidates for the label property
	 */
	public List<String> getStringPropertiesNames() {
		List<String> returned = new ArrayList<>();
		for (PropertyEntry<?> entry : propertiesEntries) {
			Type type = entry.getType();
			if (entry.getPropertyType() == PropertyType.PRIMITIVE && TypeUtils.isString(type)
					&& FMENames.isValidPropertyName(entry.getName())) {
				returned.add(entry.getName());
			}
		}
		return returned;
	}

	public String getLabelPropertyName() {
		return labelPropertyName;
	}

	public void setLabelPropertyName(String labelPropertyName) {
		if ((labelPropertyName == null && this.labelPropertyName != null)
				|| (labelPropertyName != null && !labelPropertyName.equals(this.labelPropertyName))) {
			String oldValue = this.labelPropertyName;
			this.labelPropertyName = labelPropertyName;
			getPropertyChangeSupport().firePropertyChange(LABEL_PROPERTY_NAME, oldValue, labelPropertyName);
		}
	}

	/**
	 * The reason why this structure cannot be used to create a concept, as the key of a localized message, or null when it is fine
	 */
	public String getIssue() {
		Set<String> names = new HashSet<>();
		for (PropertyEntry<?> entry : propertiesEntries) {
			if (entry.getName() == null || !FMENames.isValidPropertyName(entry.getName())) {
				return "invalid_property_name";
			}
			if (!names.add(entry.getName())) {
				return "duplicate_property_name";
			}
			if (entry.getPropertyType() != PropertyType.PRIMITIVE) {
				return "only_primitive_properties_are_supported";
			}
		}
		if (labelPropertyName == null || !getStringPropertiesNames().contains(labelPropertyName)) {
			return "no_string_property_for_the_label";
		}
		return null;
	}

	public boolean isValid() {
		return getIssue() == null;
	}

}
