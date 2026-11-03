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

package org.openflexo.fme;

import java.util.List;

import javax.swing.ImageIcon;

import org.openflexo.fme.controller.FMEController;
import org.openflexo.fme.controller.FMEPerspective;
import org.openflexo.fme.controller.action.FMEControllerActionInitializer;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.model.action.CreateFMEFreeModel;
import org.openflexo.fme.model.action.InstantiateFMEFreeModel;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.nature.NatureObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.swing.FlexoCollabsiblePanelGroup;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;

/**
 * A technology the FreeModellingEditor can use to represent the concepts of its conceptual model: the kind of {@link FMEFreeModel} it
 * brings, how such a free model is created, instantiated and displayed, and the actions it offers on its elements.<br>
 * 
 * Extensions are found with a {@link java.util.ServiceLoader}: an implementation is declared in
 * <code>META-INF/services/org.openflexo.fme.FMETechnologyExtension</code> of its module, and served by {@link FMETechnologyExtensions}.
 * 
 * @author sylvain
 * 
 */
public interface FMETechnologyExtension {

	/**
	 * Short identifier of this extension (<code>diagram</code>), also the root of its localized keys
	 */
	public String getIdentifier();

	/**
	 * The technology adapter the free models of this extension rely on, activated when the module starts
	 */
	public Class<? extends TechnologyAdapter<?>> getTechnologyAdapterClass();

	/**
	 * The PAMELA entities this extension adds to the {@link FreeModellingProjectNature}: its free model and free model instance classes
	 */
	public List<Class<?>> getModelEntities();

	/**
	 * The class of the free models this extension handles
	 */
	public Class<? extends FMEFreeModel> getFreeModelClass();

	/**
	 * The class of the free model instances this extension handles
	 */
	public Class<? extends FMEFreeModelInstance> getFreeModelInstanceClass();

	/**
	 * Action creating a new free model of this extension in a {@link FreeModellingProjectNature}
	 */
	public FlexoActionFactory<? extends CreateFMEFreeModel<?>, FreeModellingProjectNature, FlexoObject> getCreateFreeModelActionType();

	/**
	 * Action instantiating a free model of this extension, applying to the nature (the free model is then chosen or created) or to the
	 * free model itself
	 */
	public FlexoActionFactory<? extends InstantiateFMEFreeModel<?, ?>, NatureObject<FreeModellingProjectNature>, FlexoObject> getInstantiateFreeModelActionType();

	/**
	 * Icon of the free models of this extension (16x16)
	 */
	public ImageIcon getIcon();

	/**
	 * Icon of the free models of this extension (64x64)
	 */
	public ImageIcon getBigIcon();

	/**
	 * Icon for an object of this extension shown in the FreeModellingEditor, or null when it is not one of them
	 */
	public ImageIcon getIconForObject(Object object);

	/**
	 * Registers the initializers of the actions this extension brings
	 */
	public void initializeActions(FMEControllerActionInitializer actionInitializer);

	/**
	 * Builds the view of supplied free model instance, which is an instance of {@link #getFreeModelInstanceClass()}
	 */
	public ModuleView<?> createModuleView(FMEFreeModelInstance freeModelInstance, FMEController controller, FMEPerspective perspective);

	/**
	 * The inspectors of the technology, shown next to the inspector of the represented concept while a free model instance of this
	 * extension is displayed, or null when there are none
	 */
	public FlexoCollabsiblePanelGroup getInspectorPanelGroup(FlexoController controller);

	/**
	 * What the inspector of the represented concept shows when supplied object is selected: by default the object itself. A technology
	 * whose elements are not instances of concepts themselves (a PowerPoint shape) answers the instance of the GR concept standing for the
	 * element, or null when it stands for none; it answers supplied object for anything else.
	 */
	public default Object getInspectedObject(Object selectedObject) {
		return selectedObject;
	}

	public default boolean handles(FMEFreeModel freeModel) {
		return getFreeModelClass().isInstance(freeModel);
	}

	public default boolean handles(FMEFreeModelInstance freeModelInstance) {
		return getFreeModelInstanceClass().isInstance(freeModelInstance);
	}

}
