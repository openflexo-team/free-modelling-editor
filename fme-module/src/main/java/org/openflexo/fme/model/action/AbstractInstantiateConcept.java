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

package org.openflexo.fme.model.action;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * Abstract action is used to identify an instance of NoneGR as a conceptual instance<br>
 * 
 * Focused object is here a {@link FlexoConceptInstance} of the NoneGR
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractInstantiateConcept<A extends AbstractInstantiateConcept<A>>
		extends FMEAction<A, FlexoConceptInstance, FlexoObject> {

	private static final Logger logger = Logger.getLogger(AbstractInstantiateConcept.class.getPackage().getName());

	/**
	 * Instantiate a {@link FlexoAction} with a factory, a focused object and a global selection
	 * 
	 * @param actionFactory
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	public AbstractInstantiateConcept(FlexoActionFactory<A, FlexoConceptInstance, FlexoObject> actionFactory,
			FlexoConceptInstance focusedObject, List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	@Override
	public FreeModellingProjectNature getFreeModellingProjectNature() {
		FlexoProject<?> project = null;
		if (getFocusedObject().getResourceCenter() instanceof FlexoProject) {
			project = (FlexoProject<?>) getFocusedObject().getResourceCenter();
		}
		else if (getFocusedObject().getResourceCenter().getDelegatingProjectResource() != null) {
			project = getFocusedObject().getResourceCenter().getDelegatingProjectResource().getFlexoProject();
		}
		else {
			logger.warning("Could not access to FlexoProject from " + getFocusedObject());
			return null;
		}
		return project.getNature(FreeModellingProjectNature.class);
	}

	public FMEDiagramFreeModel getFMEFreeModel() {
		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		if (nature != null) {
			FMLRTVirtualModelInstance vmi = (FMLRTVirtualModelInstance) getFocusedObject().getVirtualModelInstance();
			VirtualModel vm = vmi.getVirtualModel();
			return (FMEDiagramFreeModel) nature.getFreeModel(vm.getName());
		}
		logger.warning("Sorry, project does not have FreeModellingProjectNature");
		return null;
	}

	public FMEDiagramFreeModelInstance getFMEFreeModelInstance() {
		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		if (nature == null) {
			logger.warning("Sorry, project does not have FreeModellingProjectNature");
			return null;
		}
		FMEFreeModel freeModel = getFMEFreeModel();
		if (freeModel == null) {
			logger.warning("Sorry, cannot access FMEFreeModel");
			return null;
		}
		FMLRTVirtualModelInstance vmi = (FMLRTVirtualModelInstance) getFocusedObject().getVirtualModelInstance();
		return (FMEDiagramFreeModelInstance) freeModel.getFreeModelInstance(vmi.getName());
	}

}
