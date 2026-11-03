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
package org.openflexo.fme.model.action;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * Abstract action focused on an object of a free model instance: an element of the artefact it federates (a diagram shape, a PowerPoint
 * shape...), or an instance of one of its GR concepts. It gives access to the free model and the free model instance the object belongs
 * to.<br>
 * 
 * The type parameters are the kinds of free model and free model instance of the technology
 * 
 * @param <A>
 *            type of the action
 * @param <T>
 *            type of the focused object
 * @param <FM>
 *            type of the free model
 * @param <FMI>
 *            type of the free model instance
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractFreeModelInstanceAction<A extends AbstractFreeModelInstanceAction<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends FMEAction<A, T, FlexoObject> {

	private static final Logger logger = Logger.getLogger(AbstractFreeModelInstanceAction.class.getPackage().getName());

	protected AbstractFreeModelInstanceAction(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject,
			List<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The nature of the project of the focused instance, when the focused object is an instance, the one of the project of the editor
	 * otherwise
	 */
	@Override
	public FreeModellingProjectNature getFreeModellingProjectNature() {
		if (getFocusedObject() instanceof FlexoConceptInstance) {
			FreeModellingProjectNature returned = natureOf((FlexoConceptInstance) getFocusedObject());
			if (returned == null) {
				logger.warning("Could not access to FlexoProject from " + getFocusedObject());
			}
			return returned;
		}
		return super.getFreeModellingProjectNature();
	}

	/**
	 * The nature of the project supplied instance belongs to, or null
	 */
	public static FreeModellingProjectNature natureOf(FlexoConceptInstance instance) {
		if (instance == null || instance.getResourceCenter() == null) {
			return null;
		}
		FlexoProject<?> project = null;
		if (instance.getResourceCenter() instanceof FlexoProject) {
			project = (FlexoProject<?>) instance.getResourceCenter();
		}
		else if (instance.getResourceCenter().getDelegatingProjectResource() != null) {
			project = instance.getResourceCenter().getDelegatingProjectResource().getFlexoProject();
		}
		return project != null ? project.getNature(FreeModellingProjectNature.class) : null;
	}

	/**
	 * The free model instance the focused object belongs to: the one accessing the virtual model instance of the focused instance, or the
	 * one whose artefact contains the focused element
	 */
	@SuppressWarnings("unchecked")
	public FMI getFMEFreeModelInstance() {
		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		if (nature == null) {
			logger.warning("Sorry, project does not have FreeModellingProjectNature");
			return null;
		}
		if (getFocusedObject() instanceof FlexoConceptInstance) {
			return (FMI) nature.getFreeModelInstance(((FlexoConceptInstance) getFocusedObject()).getVirtualModelInstance());
		}
		return (FMI) nature.getFreeModelInstanceContaining(getFocusedObject());
	}

	@SuppressWarnings("unchecked")
	public FM getFMEFreeModel() {
		FMI freeModelInstance = getFMEFreeModelInstance();
		return freeModelInstance != null ? (FM) freeModelInstance.getFreeModel() : null;
	}

}
