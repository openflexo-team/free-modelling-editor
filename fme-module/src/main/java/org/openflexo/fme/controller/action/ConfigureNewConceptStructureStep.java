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

package org.openflexo.fme.controller.action;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fme.model.action.NewConceptStructure;
import org.openflexo.foundation.fml.action.PropertyEntry;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.localization.LocalizedDelegate;

/**
 * This step is used to configure the structure of a concept being created: its properties (a name and a type each), and the String
 * property giving the label of its instances. It can be skipped, the default structure being a <code>name</code> and a
 * <code>description</code>.<br>
 * Shared by the wizards creating a concept.
 * 
 * @author sylvain
 */
@FIBPanel("Fib/Wizard/ConfigureNewConceptStructure.fib")
public class ConfigureNewConceptStructureStep extends WizardStep {

	/** The types a property may have: the primitive ones */
	private static final List<Class<?>> AVAILABLE_TYPES = Arrays.asList(String.class, Integer.class, Double.class, Boolean.class,
			Date.class);

	private final NewConceptStructure structure;
	private final LocalizedDelegate locales;
	private final ApplicationContext serviceManager;

	public ConfigureNewConceptStructureStep(NewConceptStructure structure, LocalizedDelegate locales, ApplicationContext serviceManager) {
		this.structure = structure;
		this.locales = locales;
		this.serviceManager = serviceManager;
		structure.getPropertyChangeSupport().addPropertyChangeListener(evt -> {
			if (getWizard() != null) {
				checkValidity();
			}
		});
	}

	public ApplicationContext getServiceManager() {
		return serviceManager;
	}

	public NewConceptStructure getStructure() {
		return structure;
	}

	public List<Class<?>> getAvailableTypes() {
		return AVAILABLE_TYPES;
	}

	@Override
	public String getTitle() {
		return locales.localizedForKey("configure_new_concept_structure");
	}

	@Override
	public boolean isValid() {
		String issue = structure.getIssue();
		if (issue != null) {
			setIssueMessage(locales.localizedForKey(issue), IssueMessageType.ERROR);
			return false;
		}
		return true;
	}

	public String getTypeName(PropertyEntry<?> entry) {
		return TypeUtils.simpleRepresentation(entry.getType());
	}

	public Icon getIconForProperty(PropertyEntry<?> entry) {
		if (TypeUtils.isString(entry.getType())) {
			return FMLIconLibrary.STRING_PRIMITIVE_ICON;
		}
		if (TypeUtils.isDate(entry.getType())) {
			return FMLIconLibrary.DATE_PRIMITIVE_ICON;
		}
		if (TypeUtils.isBoolean(entry.getType())) {
			return FMLIconLibrary.BOOLEAN_PRIMITIVE_ICON;
		}
		if (TypeUtils.isInteger(entry.getType())) {
			return FMLIconLibrary.INTEGER_PRIMITIVE_ICON;
		}
		if (TypeUtils.isFloat(entry.getType()) || TypeUtils.isDouble(entry.getType())) {
			return FMLIconLibrary.DOUBLE_PRIMITIVE_ICON;
		}
		return FMLIconLibrary.UNKNOWN_ICON;
	}

}
