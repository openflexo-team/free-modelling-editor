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
package org.openflexo.fme.pptx;

import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.FMETechnologyExtension;
import org.openflexo.fme.controller.FMEController;
import org.openflexo.fme.controller.FMEPerspective;
import org.openflexo.fme.controller.action.CreateNewConceptFromElementInitializer;
import org.openflexo.fme.controller.action.DeclareInstanceOfExistingConceptInitializer;
import org.openflexo.fme.controller.action.DisconnectInstanceFromConceptInitializer;
import org.openflexo.fme.controller.action.FMEControllerActionInitializer;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.pptx.controller.action.CreateFMEPowerpointFreeModelInitializer;
import org.openflexo.fme.pptx.controller.action.InstantiateFMEPowerpointFreeModelInitializer;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.fme.pptx.model.PowerpointAnnotations;
import org.openflexo.fme.pptx.model.action.CreateFMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.action.CreateNewConceptFromShape;
import org.openflexo.fme.pptx.model.action.DeclareShapeAsInstanceOfExistingConcept;
import org.openflexo.fme.pptx.model.action.DisconnectShapeFromConcept;
import org.openflexo.fme.pptx.model.action.InstantiateFMEPowerpointFreeModel;
import org.openflexo.fme.pptx.view.FMEPowerpointFreeModelModuleView;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.nature.NatureObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.icon.IconFactory;
import org.openflexo.swing.FlexoCollabsiblePanelGroup;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.gui.PowerpointIconLibrary;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;

/**
 * The PowerPoint technology of the FreeModellingEditor: concepts are represented by the shapes of an existing slideshow, which the free
 * model instances annotate without modifying it
 * 
 * @author sylvain
 * 
 */
public class PowerpointFMEExtension implements FMETechnologyExtension {

	public static final String IDENTIFIER = "powerpoint";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Class<? extends TechnologyAdapter<?>> getTechnologyAdapterClass() {
		return PowerpointTechnologyAdapter.class;
	}

	@Override
	public List<Class<?>> getModelEntities() {
		return Arrays.asList(FMEPowerpointFreeModel.class, FMEPowerpointFreeModelInstance.class);
	}

	@Override
	public Class<? extends FMEFreeModel> getFreeModelClass() {
		return FMEPowerpointFreeModel.class;
	}

	@Override
	public Class<? extends FMEFreeModelInstance> getFreeModelInstanceClass() {
		return FMEPowerpointFreeModelInstance.class;
	}

	@Override
	public FlexoActionFactory<CreateFMEPowerpointFreeModel, FreeModellingProjectNature, FlexoObject> getCreateFreeModelActionType() {
		return CreateFMEPowerpointFreeModel.actionType;
	}

	@Override
	public FlexoActionFactory<InstantiateFMEPowerpointFreeModel, NatureObject<FreeModellingProjectNature>, FlexoObject> getInstantiateFreeModelActionType() {
		return InstantiateFMEPowerpointFreeModel.actionType;
	}

	@Override
	public ImageIcon getIcon() {
		return PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_ICON;
	}

	@Override
	public ImageIcon getBigIcon() {
		return PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_BIG_ICON;
	}

	@Override
	public ImageIcon getIconForObject(Object object) {
		if (object instanceof FMEPowerpointFreeModelInstance) {
			return PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_ICON;
		}
		if (object instanceof FMEPowerpointFreeModel) {
			return IconFactory.getImageIcon(PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_ICON, FMEIconLibrary.FME_MARKER);
		}
		return null;
	}

	@Override
	public void initializeActions(FMEControllerActionInitializer actionInitializer) {
		new CreateFMEPowerpointFreeModelInitializer(actionInitializer);
		new InstantiateFMEPowerpointFreeModelInitializer(actionInitializer);

		new CreateNewConceptFromElementInitializer<>(CreateNewConceptFromShape.actionType, actionInitializer);
		new DeclareInstanceOfExistingConceptInitializer<>(DeclareShapeAsInstanceOfExistingConcept.actionType, null, actionInitializer);
		new DisconnectInstanceFromConceptInitializer<>(DisconnectShapeFromConcept.actionType, actionInitializer);
	}

	@Override
	public ModuleView<?> createModuleView(FMEFreeModelInstance freeModelInstance, FMEController controller, FMEPerspective perspective) {
		return new FMEPowerpointFreeModelModuleView((FMEPowerpointFreeModelInstance) freeModelInstance, controller, perspective);
	}

	/**
	 * Only the inspector of the represented concept: no PowerPoint inspector
	 */
	@Override
	public FlexoCollabsiblePanelGroup getInspectorPanelGroup(FlexoController controller) {
		return null;
	}

	/**
	 * A shape of an annotated slideshow shows the instance of the concept it stands for, or nothing when it is not annotated
	 */
	@Override
	public Object getInspectedObject(Object selectedObject) {
		if (selectedObject instanceof PowerpointShape
				&& !PowerpointAnnotations.getFreeModelInstances((PowerpointShape) selectedObject).isEmpty()) {
			return PowerpointAnnotations.getGRInstance((PowerpointShape) selectedObject);
		}
		return selectedObject;
	}

}
