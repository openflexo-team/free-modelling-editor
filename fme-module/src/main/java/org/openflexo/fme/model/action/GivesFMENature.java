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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.action.ModuleSpecificFlexoAction;
import org.openflexo.fme.FMEModule;
import org.openflexo.fme.FreeModellingEditor;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMESampleData;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.action.CreateTopLevelVirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.nature.GivesNatureAction;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.StringUtils;

/**
 * This action is called to gives a new FME nature to a project
 * 
 * @author vincent
 */
public class GivesFMENature extends GivesNatureAction<GivesFMENature, FreeModellingProjectNature>
		implements ModuleSpecificFlexoAction<FMEModule> {

	private static final Logger logger = Logger.getLogger(GivesFMENature.class.getPackage().getName());

	public static final String DEFAULT_CONCEPTUAL_MODEL_NAME = "ConceptualModel";
	public static final String DEFAULT_SAMPLE_DATA_NAME = "SampleData";

	public static FlexoActionFactory<GivesFMENature, FlexoProject<?>, FlexoObject> actionType = new FlexoActionFactory<GivesFMENature, FlexoProject<?>, FlexoObject>(
			"gives_fme_nature") {

		/**
		 * Factory method
		 */
		@Override
		public GivesFMENature makeNewAction(FlexoProject<?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new GivesFMENature(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoProject<?> project, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FlexoProject<?> project, Vector<FlexoObject> globalSelection) {
			return project != null && !project.hasNature(FreeModellingProjectNature.class);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(GivesFMENature.actionType, FlexoProject.class);
	}

	public enum ConceptualModelChoice {
		CreateNewVirtualModel, SelectExistingVirtualModel
	}

	private ConceptualModelChoice conceptualModelChoice = ConceptualModelChoice.CreateNewVirtualModel;
	private CompilationUnitResource existingConceptualModelResource;
	private String newConceptualModelName;
	private String newConceptualModelRelativePath;
	private FlexoResourceCenter<?> conceptualModelRC;

	public enum SampleDataChoice {
		CreateNewVirtualModelInstance, SelectExistingVirtualModelInstance
	}

	private SampleDataChoice sampleDataChoice = SampleDataChoice.CreateNewVirtualModelInstance;
	private String sampleDataName;
	private String sampleDataRelativePath;
	private FMLRTVirtualModelInstanceResource existingSampleDataResource;

	private GivesFMENature(FlexoProject<?> focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public LocalizedDelegate getLocales() {
		if (getServiceManager() instanceof ApplicationContext) {
			return ((ApplicationContext) getServiceManager()).getModuleLoader().getModule(FreeModellingEditor.class)
					.getLoadedModuleInstance().getLocales();
		}
		return super.getLocales();
	}

	@Override
	public Class<FMEModule> getFlexoModuleClass() {
		return FMEModule.class;
	}

	/*@Override
	protected void doAction(Object context) throws SaveResourceException, InvalidArgumentException {
		if (getFocusedObject().getVirtualModelRepository() == null) {
			logger.warning("Could not determine VirtualModelRepository. Aborting operation.");
			throw new InvalidArgumentException("Could not determine VirtualModelRepository. Aborting operation.");
		}
	
		FreeModellingProjectNature FREE_MODELLING_NATURE = getServiceManager().getProjectNatureService()
				.getProjectNature(FreeModellingProjectNature.class);
	
		VirtualModelResource freeModellingViewPointResource = getFocusedObject().getVirtualModelRepository()
				.getResource(getFocusedObject().getProjectURI() + FreeModellingProjectNature.FREE_MODELLING_VIEWPOINT_RELATIVE_URI);
	
		// Since CreateViewpoint/View are LongRunning actions, we call them as embedded actions, therefore we are ensure that Viewpoint and
		// View are created after doAction() call.
		if (freeModellingViewPointResource == null) {
			CreateTopLevelVirtualModel action = CreateTopLevelVirtualModel.actionType
					.makeNewEmbeddedAction(getFocusedObject().getVirtualModelRepository().getRootFolder(), null, this);
			action.setNewVirtualModelName(FreeModellingProjectNature.FREE_MODELLING_VIEWPOINT_NAME);
			action.setNewVirtualModelURI(
					getFocusedObject().getProjectURI() + FreeModellingProjectNature.FREE_MODELLING_VIEWPOINT_RELATIVE_URI);
			action.setNewVirtualModelDescription("This is the generic ViewPoint storing all FreeModelling meta-models");
			action.doAction();
			freeModellingViewPointResource = (VirtualModelResource) action.getNewVirtualModel().getResource();
		}
	
		FMLRTVirtualModelInstanceResource freeModellingViewResource = getFocusedObject().getVirtualModelInstanceRepository()
				.getResource(getFocusedObject().getProjectURI() + FreeModellingProjectNature.FREE_MODELLING_VIEW_RELATIVE_URI);
	
		if (freeModellingViewResource == null) {
			CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
					.makeNewEmbeddedAction(getFocusedObject().getVirtualModelInstanceRepository().getRootFolder(), null, this);
			action.setNewVirtualModelInstanceName(FreeModellingProjectNature.FREE_MODELLING_VIEW_NAME);
			action.setNewVirtualModelInstanceTitle(FreeModellingProjectNature.FREE_MODELLING_VIEW_NAME);
			action.setVirtualModel(freeModellingViewPointResource.getVirtualModel());
			action.doAction();
			freeModellingViewResource = (FMLRTVirtualModelInstanceResource) action.getNewVirtualModelInstance().getResource();
		}
	
		DiagramTechnologyAdapter diagramTechnologyAdapter = getFocusedObject().getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(DiagramTechnologyAdapter.class);
		DiagramSpecificationRepository<?> dsRepository = diagramTechnologyAdapter.getDiagramSpecificationRepository(getFocusedObject());
		dsRepository.createNewFolder(FreeModellingProjectNature.DIAGRAM_SPECIFICATIONS_FOLDER);
	
		// We have now to notify project of nature modifications
		getFocusedObject().getPropertyChangeSupport().firePropertyChange("asNature(String)", false, true);
		getFocusedObject().getPropertyChangeSupport().firePropertyChange("hasNature(String)", false, true);
	
	}*/

	@Override
	public FreeModellingProjectNature makeNewNature() {

		FreeModellingProjectNature nature = getFocusedObject().getModelFactory().newInstance(FreeModellingProjectNature.class);

		FMEConceptualModel conceptualModel = makeFMEConceptualModel();
		nature.setConceptualModel(conceptualModel);
		nature.setSampleData(makeFMESampleData(conceptualModel));

		return nature;
	}

	private FMEConceptualModel makeFMEConceptualModel() {
		FMEConceptualModel conceptualModel = getFocusedObject().getModelFactory().newInstance(FMEConceptualModel.class);

		switch (getConceptualModelChoice()) {
			case CreateNewVirtualModel:
				// RepositoryFolder<?, ?> folder = getFocusedObject().getVirtualModelRepository().get
				CreateTopLevelVirtualModel action = CreateTopLevelVirtualModel.actionType
						.makeNewEmbeddedAction(getFocusedObject().getVirtualModelRepository().getRootFolder(), null, this);
				action.setNewVirtualModelName(getNewConceptualModelName());
				action.setNewVirtualModelDescription("Conceptual model for " + getFocusedObject().getDisplayableName());
				action.doAction();
				CompilationUnitResource newVirtualModelResource = (CompilationUnitResource) action.getNewVirtualModel().getResource();
				conceptualModel.setAccessedVirtualModelResource(newVirtualModelResource);
				break;
			case SelectExistingVirtualModel:
				conceptualModel.setAccessedVirtualModelResource(getExistingConceptualModelResource());
				break;

			default:
				break;
		}

		return conceptualModel;
	}

	public ConceptualModelChoice getConceptualModelChoice() {
		if (conceptualModelChoice == null) {
			return ConceptualModelChoice.CreateNewVirtualModel;
		}
		return conceptualModelChoice;
	}

	public void setConceptualModelChoice(ConceptualModelChoice conceptualModelChoice) {
		if (conceptualModelChoice != getConceptualModelChoice()) {
			ConceptualModelChoice oldValue = getConceptualModelChoice();
			this.conceptualModelChoice = conceptualModelChoice;
			getPropertyChangeSupport().firePropertyChange("conceptualModelChoice", oldValue, conceptualModelChoice);
		}
	}

	public CompilationUnitResource getExistingConceptualModelResource() {
		return existingConceptualModelResource;
	}

	public void setExistingConceptualModelResource(CompilationUnitResource existingConceptualModelResource) {
		if ((existingConceptualModelResource == null && this.existingConceptualModelResource != null)
				|| (existingConceptualModelResource != null
						&& !existingConceptualModelResource.equals(this.existingConceptualModelResource))) {
			CompilationUnitResource oldValue = this.existingConceptualModelResource;
			this.existingConceptualModelResource = existingConceptualModelResource;
			getPropertyChangeSupport().firePropertyChange("existingConceptualModelResource", oldValue, existingConceptualModelResource);
		}
	}

	public String getNewConceptualModelName() {
		if (StringUtils.isEmpty(newConceptualModelName)) {
			return DEFAULT_CONCEPTUAL_MODEL_NAME;
		}
		return newConceptualModelName;
	}

	public void setNewConceptualModelName(String newConceptualModelName) {
		if ((newConceptualModelName == null && this.newConceptualModelName != null)
				|| (newConceptualModelName != null && !newConceptualModelName.equals(this.newConceptualModelName))) {
			String oldValue = this.newConceptualModelName;
			this.newConceptualModelName = newConceptualModelName;
			getPropertyChangeSupport().firePropertyChange("newConceptualModelName", oldValue, newConceptualModelName);
		}
	}

	public String getNewConceptualModelRelativePath() {
		if (newConceptualModelRelativePath == null) {
			return "";
		}
		return newConceptualModelRelativePath;
	}

	public void setNewConceptualModelRelativePath(String newConceptualModelRelativePath) {
		if ((newConceptualModelRelativePath == null && this.newConceptualModelRelativePath != null)
				|| (newConceptualModelRelativePath != null
						&& !newConceptualModelRelativePath.equals(this.newConceptualModelRelativePath))) {
			String oldValue = this.newConceptualModelRelativePath;
			this.newConceptualModelRelativePath = newConceptualModelRelativePath;
			getPropertyChangeSupport().firePropertyChange("newConceptualModelRelativePath", oldValue, newConceptualModelRelativePath);
		}
	}

	public FlexoResourceCenter<?> getConceptualModelRC() {
		if (conceptualModelRC != null) {
			return getFocusedObject();
		}
		return conceptualModelRC;
	}

	public void setConceptualModelRC(FlexoResourceCenter<?> conceptualModelRC) {
		if ((conceptualModelRC == null && this.conceptualModelRC != null)
				|| (conceptualModelRC != null && !conceptualModelRC.equals(this.conceptualModelRC))) {
			FlexoResourceCenter<?> oldValue = this.conceptualModelRC;
			this.conceptualModelRC = conceptualModelRC;
			getPropertyChangeSupport().firePropertyChange("conceptualModelRC", oldValue, conceptualModelRC);
		}
	}

	private FMESampleData makeFMESampleData(FMEConceptualModel conceptualModel) {
		FMESampleData sampleData = getFocusedObject().getModelFactory().newInstance(FMESampleData.class);

		switch (getSampleDataChoice()) {
			case CreateNewVirtualModelInstance:
				// RepositoryFolder<?, ?> folder = getFocusedObject().getVirtualModelRepository().get
				CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
						.makeNewEmbeddedAction(getFocusedObject().getVirtualModelInstanceRepository().getRootFolder(), null, this);

				action.setNewVirtualModelInstanceName(getSampleDataName());
				action.setVirtualModel(conceptualModel.getAccessedVirtualModel());
				action.doAction();
				FMLRTVirtualModelInstanceResource newVirtualModelInstanceResource = (FMLRTVirtualModelInstanceResource) action
						.getNewVirtualModelInstance().getResource();

				System.out.println("Made new resource: " + newVirtualModelInstanceResource);

				sampleData.setAccessedVirtualModelInstanceResource(newVirtualModelInstanceResource);
				break;
			case SelectExistingVirtualModelInstance:
				sampleData.setAccessedVirtualModelInstanceResource(getExistingSampleDataResource());
				break;

			default:
				break;
		}

		return sampleData;

	}

	public SampleDataChoice getSampleDataChoice() {
		return sampleDataChoice;
	}

	public void setSampleDataChoice(SampleDataChoice sampleDataChoice) {
		if (sampleDataChoice != getSampleDataChoice()) {
			SampleDataChoice oldValue = getSampleDataChoice();
			this.sampleDataChoice = sampleDataChoice;
			getPropertyChangeSupport().firePropertyChange("sampleDataChoice", oldValue, sampleDataChoice);
		}
	}

	public String getSampleDataName() {
		if (StringUtils.isEmpty(sampleDataName)) {
			return DEFAULT_SAMPLE_DATA_NAME;
		}
		return sampleDataName;
	}

	public void setSampleDataName(String sampleDataName) {
		if ((sampleDataName == null && this.sampleDataName != null)
				|| (sampleDataName != null && !sampleDataName.equals(this.sampleDataName))) {
			String oldValue = this.sampleDataName;
			this.sampleDataName = sampleDataName;
			getPropertyChangeSupport().firePropertyChange("sampleDataName", oldValue, sampleDataName);
		}
	}

	public String getSampleDataRelativePath() {
		return sampleDataRelativePath;
	}

	public void setSampleDataRelativePath(String sampleDataRelativePath) {
		if ((sampleDataRelativePath == null && this.sampleDataRelativePath != null)
				|| (sampleDataRelativePath != null && !sampleDataRelativePath.equals(this.sampleDataRelativePath))) {
			String oldValue = this.sampleDataRelativePath;
			this.sampleDataRelativePath = sampleDataRelativePath;
			getPropertyChangeSupport().firePropertyChange("sampleDataRelativePath", oldValue, sampleDataRelativePath);
		}
	}

	public FMLRTVirtualModelInstanceResource getExistingSampleDataResource() {
		return existingSampleDataResource;
	}

	public void setExistingSampleDataResource(FMLRTVirtualModelInstanceResource existingSampleDataResource) {
		if ((existingSampleDataResource == null && this.existingSampleDataResource != null)
				|| (existingSampleDataResource != null && !existingSampleDataResource.equals(this.existingSampleDataResource))) {
			FMLRTVirtualModelInstanceResource oldValue = this.existingSampleDataResource;
			this.existingSampleDataResource = existingSampleDataResource;
			getPropertyChangeSupport().firePropertyChange("existingSampleDataResource", oldValue, existingSampleDataResource);
		}
	}

}
