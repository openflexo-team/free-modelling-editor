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
package org.openflexo.fme.controller.action;

import org.openflexo.fme.model.action.AbstractDisconnectInstanceFromConcept;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.FlexoActionRunnable;
import org.openflexo.view.controller.ActionInitializer;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;

/**
 * Initializer of an action disconnecting an element from its concept, whatever the technology: when the conceptual instance is deleted
 * with values of its properties, the user confirms first
 * 
 * @param <A>
 *            type of the action
 * @param <T>
 *            type of the focused object
 */
public class DisconnectInstanceFromConceptInitializer<A extends AbstractDisconnectInstanceFromConcept<A, T, ?, ?>, T extends FlexoObject>
		extends ActionInitializer<A, T, FlexoObject> {

	public DisconnectInstanceFromConceptInitializer(FlexoActionFactory<A, T, FlexoObject> actionType,
			ControllerActionInitializer actionInitializer) {
		super(actionType, actionInitializer);
	}

	@Override
	protected FlexoActionRunnable<A, T, FlexoObject> getDefaultInitializer() {
		return (e, action) -> {
			// The values of the properties of the instance are lost: nobody should discover that afterwards
			if (action.getLostPropertyNames().isEmpty()) {
				return true;
			}
			return FlexoController.confirm(getModuleLocales(action).localizedForKeyWithParams(
					"would_you_really_like_to_disconnect_this_instance_(the_values_of_($0)_will_be_lost)_?",
					String.join(", ", action.getLostPropertyNames())));
		};
	}

	@Override
	protected FlexoActionRunnable<A, T, FlexoObject> getDefaultFinalizer() {
		return (e, action) -> {
			getController().selectAndFocusObject(action.getFocusedObject());
			return true;
		};
	}
}
