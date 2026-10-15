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

package org.openflexo.fme.controller;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.action.RenameFMEConcept;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.ActionGroup;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.action.RenameFlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionFactory;
import org.openflexo.selection.ContextualMenuManager;
import org.openflexo.view.controller.action.MenuItemAction;

/**
 * 
 * Contextual menu manager for this module
 * 
 * @author yourname
 */
public class FMEContextualMenuManager extends ContextualMenuManager {

	private final FMESelectionManager selectionManager;

	/** Part of the menu being built (true for the one from the edit section on), null when none */
	private Boolean latePart = null;

	public FMEContextualMenuManager(FMESelectionManager selectionManager, FMEController controller) {
		super(selectionManager, controller);
		this.selectionManager = selectionManager;
	}

	/**
	 * A concept of the FreeModellingEditor is renamed with its graphical representation: the generic rename of a concept, which would
	 * leave them apart, is not offered on it
	 */
	@Override
	public JPopupMenu makePopupMenu(FlexoObject focusedObject, MenuFilter filter) {
		MenuFilter baseFilter = new MenuFilter() {
			@Override
			public boolean acceptActionType(FlexoActionFactory<?, ?, ?> actionType) {
				if (actionType == RenameFlexoConcept.actionType && RenameFMEConcept.conceptToRename(focusedObject) != null) {
					return false;
				}
				return filter.acceptActionType(actionType);
			}
		};
		// The proxied behaviours go where those of a concept instance go in any other module: before the edit section (copy/paste...).
		// The late part is built first, as the last built menu is the one the manager displays.
		JPopupMenu late = makePart(focusedObject, baseFilter, true);
		JPopupMenu returned = makePart(focusedObject, baseFilter, false);
		addProxiedBehaviours(returned, focusedObject);
		Component[] lateComponents = late.getComponents();
		if (lateComponents.length > 0) {
			if (returned.getComponentCount() > 0) {
				returned.addSeparator();
			}
			for (Component component : lateComponents) {
				returned.add(component);
			}
		}
		return returned;
	}

	/**
	 * Builds the early (before the edit section) or the late part of the menu. The behaviours of the focused instance (deletion scheme...)
	 * do not go through the filter but through {@link #acceptAction(FlexoActionFactory)}, which is therefore restricted to the part being
	 * built: otherwise they would show in both parts.
	 */
	private JPopupMenu makePart(FlexoObject focusedObject, MenuFilter baseFilter, boolean late) {
		latePart = late;
		try {
			return super.makePopupMenu(focusedObject, new MenuFilter() {
				@Override
				public boolean acceptActionType(FlexoActionFactory<?, ?, ?> actionType) {
					return isLate(actionType) == late && baseFilter.acceptActionType(actionType);
				}
			});
		} finally {
			latePart = null;
		}
	}

	@Override
	public boolean acceptAction(FlexoActionFactory<?, ?, ?> action) {
		return super.acceptAction(action) && (latePart == null || isLate(action) == latePart);
	}

	private static boolean isLate(FlexoActionFactory<?, ?, ?> actionType) {
		ActionGroup group = actionType.getActionMenu() != null ? actionType.getActionMenu().getActionGroup() : actionType.getActionGroup();
		return group != null && group.getIndex() >= FlexoActionFactory.editGroup.getIndex();
	}

	/**
	 * A GR concept is a proxy of the conceptual concept it represents: the public action schemes of the latter are offered on an instance
	 * of the former, in a section of its own, and run on the instance of the conceptual concept the GR instance points to.<br>
	 * With a selection of GR instances of the same concept, they run on all the instances of the conceptual concept.
	 */
	private void addProxiedBehaviours(JPopupMenu menu, FlexoObject focusedObject) {
		if (!(focusedObject instanceof FlexoConceptInstance) || getEditor() == null) {
			return;
		}
		FlexoConceptInstanceRole conceptRole = FMEFreeModel.conceptRole(((FlexoConceptInstance) focusedObject).getFlexoConcept());
		if (conceptRole == null || conceptRole.getFlexoConceptType() == null) {
			return;
		}

		List<FlexoConceptInstance> proxied = new ArrayList<>();
		List<FlexoObject> selection = new ArrayList<>(selectionManager.getSelection());
		if (!selection.contains(focusedObject)) {
			selection.clear();
			selection.add(focusedObject);
		}
		for (FlexoObject o : selection) {
			if (!(o instanceof FlexoConceptInstance)
					|| ((FlexoConceptInstance) o).getFlexoConcept() != ((FlexoConceptInstance) focusedObject).getFlexoConcept()) {
				// Not an homogeneous selection
				return;
			}
			FlexoConceptInstance target = ((FlexoConceptInstance) o).getFlexoActor(conceptRole.getRoleName());
			if (target == null) {
				return;
			}
			proxied.add(target);
		}
		FlexoConceptInstance proxiedFocused = ((FlexoConceptInstance) focusedObject).getFlexoActor(conceptRole.getRoleName());

		boolean separatorAdded = false;
		for (ActionScheme actionScheme : conceptRole.getFlexoConceptType().getAccessibleActionSchemes()) {
			if (actionScheme.getVisibility() != Visibility.Public) {
				continue;
			}
			ActionSchemeActionFactory factory = new ActionSchemeActionFactory(actionScheme, proxiedFocused);
			Vector<VirtualModelInstanceObject> proxiedSelection = new Vector<>(proxied);
			if (!getEditor().isActionVisible(factory, proxiedFocused, proxiedSelection)) {
				continue;
			}
			if (!separatorAdded) {
				if (menu.getComponentCount() > 0) {
					menu.addSeparator();
				}
				separatorAdded = true;
			}
			MenuItemAction<?, FlexoConceptInstance, VirtualModelInstanceObject> action = new MenuItemAction<>(factory, proxiedFocused,
					proxiedSelection, getEditor());
			JMenuItem item = menu.add(action);
			item.setText(action.getLocalizedActionName());
			if (getEditor().getEnabledIconFor(factory) != null) {
				item.setIcon(getEditor().getEnabledIconFor(factory));
			}
			if (getEditor().getDisabledIconFor(factory) != null) {
				item.setDisabledIcon(getEditor().getDisabledIconFor(factory));
			}
		}
	}

}
