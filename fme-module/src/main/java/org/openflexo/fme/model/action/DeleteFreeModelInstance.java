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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.rt.action.DeleteVirtualModelInstance;
import org.openflexo.foundation.resource.SaveResourceException;

/**
 * This action is used to remove a {@link FMEFreeModelInstance} from a {@link FreeModellingProject}<br>
 * 
 * @author vincent
 * 
 */
public class DeleteFreeModelInstance extends FMEAction<DeleteFreeModelInstance, FMEFreeModelInstance, FlexoObject> {

	private static final Logger logger = Logger.getLogger(DeleteFreeModelInstance.class.getPackage().getName());

	public static FlexoActionFactory<DeleteFreeModelInstance, FMEFreeModelInstance, FlexoObject> actionType = new FlexoActionFactory<DeleteFreeModelInstance, FMEFreeModelInstance, FlexoObject>(
			"delete_free_model_instance", FlexoActionFactory.defaultGroup, FlexoActionFactory.DELETE_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public DeleteFreeModelInstance makeNewAction(FMEFreeModelInstance focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new DeleteFreeModelInstance(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMEFreeModelInstance object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FMEFreeModelInstance object, Vector<FlexoObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(DeleteFreeModelInstance.actionType, FMEFreeModelInstance.class);
	}

	private DeleteFreeModelInstance(FMEFreeModelInstance focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws SaveResourceException {

		logger.info("Remove free model action started ...");
		logger.info("Delete virtual model instance");
		getFocusedObject().getFreeModel().removeFromFreeModelInstances(getFocusedObject());
		DeleteVirtualModelInstance deleteVm = DeleteVirtualModelInstance.actionType
				.makeNewEmbeddedAction(getFocusedObject().getAccessedVirtualModelInstance(), null, this);
		deleteVm.doAction();
		logger.info("Delete free model");
		getFocusedObject().delete();
	}

}
