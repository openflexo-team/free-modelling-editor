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

import org.openflexo.connie.DataBinding;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMESampleData;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateGenericBehaviourParameter;
import org.openflexo.foundation.fml.action.CreateModelSlot;
import org.openflexo.foundation.fml.action.CreateTopLevelVirtualModel;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract implementation for the creation of a {@link FMEFreeModel}
 * 
 * This action is used to create a new {@link FMEFreeModel} in a {@link FreeModellingProjectNature}<br>
 * 
 * @author sylvain
 * 
 */
public abstract class CreateFMEFreeModel<A extends CreateFMEFreeModel<A>> extends FMEAction<A, FreeModellingProjectNature, FlexoObject> {

	private static final Logger logger = Logger.getLogger(CreateFMEFreeModel.class.getPackage().getName());

	private FMEFreeModel freeModel;
	private String freeModelName;
	private String freeModelDescription;

	private ConceptualModelChoice conceptualModelChoice = ConceptualModelChoice.UseGeneralConceptualModel;
	private CompilationUnitResource existingConceptualModelResource;
	private String newConceptualModelName;
	private String newConceptualModelRelativePath;
	private FlexoResourceCenter<?> conceptualModelRC;

	private SampleDataChoice sampleDataChoice = SampleDataChoice.UseGeneralSampleData;
	private String sampleDataName;
	private String sampleDataRelativePath;
	private FMLRTVirtualModelInstanceResource existingSampleDataResource;

	public enum ConceptualModelChoice {
		UseGeneralConceptualModel, CreateNewTopLevelVirtualModel, CreateContainedVirtualModel, SelectExistingVirtualModel
	}

	public enum SampleDataChoice {
		UseGeneralSampleData, CreateNewVirtualModelInstance, SelectExistingVirtualModelInstance
	}

	protected CreateFMEFreeModel(FlexoActionFactory<A, FreeModellingProjectNature, FlexoObject> actionType,
			FreeModellingProjectNature focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws SaveResourceException {

		logger.info("Create free model");

		FMEConceptualModel conceptualModel = getFocusedObject().getConceptualModel();

		if (getConceptualModelChoice() != ConceptualModelChoice.UseGeneralConceptualModel) {
			conceptualModel = makeFMEConceptualModel();
		}

		freeModel = createNewFreeModel(getFreeModelName(), conceptualModel);

		if (getConceptualModelChoice() != ConceptualModelChoice.UseGeneralConceptualModel) {
			freeModel.setConceptualModel(conceptualModel);
			if (getSampleDataChoice() != SampleDataChoice.UseGeneralSampleData) {
				freeModel.setSampleData(makeFMESampleData(conceptualModel));
			}
		}

		getFocusedObject().addToFreeModels(freeModel);

		getFocusedObject().getOwner().setIsModified();
	}

	protected VirtualModel createVirtualModel(String metaModelName, CompilationUnitResource conceptualVM) {
		// Now we create the VirtualModel
		// System.out.println("Creating VirtualModel...");
		CreateTopLevelVirtualModel action = CreateTopLevelVirtualModel.actionType
				.makeNewEmbeddedAction(getFocusedObject().getOwner().getVirtualModelRepository().getRootFolder(), null, this);
		action.setNewVirtualModelName(metaModelName);
		action.doAction();
		VirtualModel newVirtualModel = action.getNewVirtualModel();
		// System.out.println("VirtualModel has been created: " + newVirtualModel);

		// Now we create the sample data model slot
		CreateModelSlot createMS = CreateModelSlot.actionType.makeNewEmbeddedAction(newVirtualModel, null, this);
		createMS.setTechnologyAdapter(getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class));
		createMS.setModelSlotClass(FMLRTVirtualModelInstanceModelSlot.class);
		createMS.setModelSlotName(FMEFreeModel.SAMPLE_DATA_MODEL_SLOT_NAME);
		createMS.setVmRes(conceptualVM);

		createMS.doAction();
		// Unused FMLRTVirtualModelInstanceModelSlot sampleDataModelSlot = (FMLRTVirtualModelInstanceModelSlot)
		createMS.getNewModelSlot();

		// CreationScheme
		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(newVirtualModel, null, this);
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();

		CreateGenericBehaviourParameter createSampleDataParameter = CreateGenericBehaviourParameter.actionType
				.makeNewEmbeddedAction(creationScheme, null, this);
		createSampleDataParameter.setParameterName("sampleData");
		createSampleDataParameter.setParameterType(conceptualVM.getCompilationUnit().getVirtualModel().getInstanceType());

		createSampleDataParameter.doAction();

		CreateEditionAction assignSampleDataAction = CreateEditionAction.actionType.makeNewEmbeddedAction(creationScheme.getControlGraph(),
				null, this);
		assignSampleDataAction.setEditionActionClass(ExpressionAction.class);
		assignSampleDataAction.setAssignation(new DataBinding<>("sampleData"));
		assignSampleDataAction.doAction();
		ExpressionAction<?> assignSampleData = (ExpressionAction<?>) assignSampleDataAction.getBaseEditionAction();
		assignSampleData.setExpression(new DataBinding<>("parameters.sampleData"));

		/*CreateGenericBehaviourParameter createDiagramParameter = CreateGenericBehaviourParameter.actionType
				.makeNewEmbeddedAction(creationScheme, null, this);
		createDiagramParameter.setParameterName("diagram");
		createDiagramParameter.setParameterType(getConceptualVirtualModelResource().getVirtualModel().getInstanceType());
		createDiagramParameter.doAction();*/

		return newVirtualModel;
	}

	protected abstract FMEFreeModel createNewFreeModel(String metaModelName, FMEConceptualModel conceptualModel);

	@Override
	public boolean isValid() {

		if (StringUtils.isEmpty(getFreeModelName())) {
			return false;
		}

		if (getFocusedObject() != null && getFocusedObject().getFreeModel(getFreeModelName()) != null) {
			return false;
		}

		return true;
	}

	/**
	 * Return newly created FreeModel
	 * 
	 * @return
	 */
	public FMEFreeModel getNewFreeModel() {
		return freeModel;
	}

	private String defaultFreeModelName = null;

	public String getFreeModelName() {
		if (freeModelName == null) {
			if (defaultFreeModelName == null) {
				String baseName = "FreeModel";
				if (getFocusedObject().getFreeModel(baseName) != null) {
					int i = 2;
					while (getFocusedObject().getFreeModel(baseName + i) != null) {
						i++;
					}
					defaultFreeModelName = baseName + i;
				}
				else {
					defaultFreeModelName = baseName;
				}
			}
			return defaultFreeModelName;
		}
		return freeModelName;
	}

	public void setFreeModelName(String freeModelName) {
		boolean wasValid = isValid();
		this.freeModelName = freeModelName;
		getPropertyChangeSupport().firePropertyChange("freeModelName", null, freeModelName);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public String getFreeModelDescription() {
		return freeModelDescription;
	}

	public void setFreeModelDescription(String freeModelDescription) {
		boolean wasValid = isValid();
		this.freeModelDescription = freeModelDescription;
		getPropertyChangeSupport().firePropertyChange("freeModelDescription", null, freeModelDescription);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	private FMEConceptualModel makeFMEConceptualModel() {
		FMEConceptualModel conceptualModel = getFocusedObject().getNature().getProject().getModelFactory()
				.newInstance(FMEConceptualModel.class);

		switch (getConceptualModelChoice()) {
			case CreateNewTopLevelVirtualModel:
				// RepositoryFolder<?, ?> folder = getFocusedObject().getVirtualModelRepository().get
				CreateTopLevelVirtualModel action = CreateTopLevelVirtualModel.actionType.makeNewEmbeddedAction(
						getFocusedObject().getNature().getProject().getVirtualModelRepository().getRootFolder(), null, this);
				action.setNewVirtualModelName(getNewConceptualModelName());
				action.setNewVirtualModelDescription("Conceptual model for " + getFreeModelName());
				action.doAction();
				CompilationUnitResource newTopLevelVirtualModelResource = action.getNewVirtualModel().getResource();
				conceptualModel.setAccessedVirtualModelResource(newTopLevelVirtualModelResource);
				break;
			case CreateContainedVirtualModel:
				// RepositoryFolder<?, ?> folder = getFocusedObject().getVirtualModelRepository().get
				CreateContainedVirtualModel action2 = CreateContainedVirtualModel.actionType.makeNewEmbeddedAction(
						getFocusedObject().getConceptualModel().getAccessedVirtualModel().getCompilationUnit(), null, this);
				action2.setNewVirtualModelName(getNewConceptualModelName());
				action2.setNewVirtualModelDescription("Conceptual model for " + getFreeModelName());
				action2.doAction();
				CompilationUnitResource newContainedVirtualModelResource = action2.getNewVirtualModel().getResource();
				conceptualModel.setAccessedVirtualModelResource(newContainedVirtualModelResource);
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
			return ConceptualModelChoice.UseGeneralConceptualModel;
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

	public String getDefaultNewConceptualModelName() {
		return (StringUtils.isNotEmpty(getFreeModelName()) ? getFreeModelName() + "ConceptualModel" : "NewConceptualModel");
	}

	public String getNewConceptualModelName() {
		if (StringUtils.isEmpty(newConceptualModelName)) {
			return getDefaultNewConceptualModelName();
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
			return getFocusedObject().getProject();
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
		FMESampleData sampleData = getFocusedObject().getNature().getProject().getModelFactory().newInstance(FMESampleData.class);

		FMLRTVirtualModelInstanceResource newVirtualModelInstanceResource = null;
		switch (getSampleDataChoice()) {
			case CreateNewVirtualModelInstance:
				switch (getConceptualModelChoice()) {
					case CreateNewTopLevelVirtualModel:
						// Create new top-level VMI
						System.out.println("Nouvelles sample data top level");
						CreateBasicVirtualModelInstance action1 = CreateBasicVirtualModelInstance.actionType.makeNewEmbeddedAction(
								getFocusedObject().getNature().getProject().getVirtualModelInstanceRepository().getRootFolder(), null,
								this);
						action1.setNewVirtualModelInstanceName(getSampleDataName());
						action1.setVirtualModel(conceptualModel.getAccessedVirtualModel());
						action1.doAction();
						newVirtualModelInstanceResource = (FMLRTVirtualModelInstanceResource) action1.getNewVirtualModelInstance()
								.getResource();
						break;
					case CreateContainedVirtualModel:
						// Create new VMI with general sample data as container
						System.out.println("Nouvelles sample data dans " + getFocusedObject().getSampleData());
						CreateBasicVirtualModelInstance action2 = CreateBasicVirtualModelInstance.actionType
								.makeNewEmbeddedAction(getFocusedObject().getSampleData().getAccessedVirtualModelInstance(), null, this);
						action2.setNewVirtualModelInstanceName(getSampleDataName());
						action2.setVirtualModel(conceptualModel.getAccessedVirtualModel());
						action2.doAction();
						newVirtualModelInstanceResource = (FMLRTVirtualModelInstanceResource) action2.getNewVirtualModelInstance()
								.getResource();
						break;
					case SelectExistingVirtualModel:
						// TODO
						System.out.println("Not implemented");
						break;
				}

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

	public String getDefaultNewSampleDataName() {
		return (StringUtils.isNotEmpty(getFreeModelName()) ? getFreeModelName() + "SampleData" : "SampleData");
	}

	public String getSampleDataName() {
		if (StringUtils.isEmpty(sampleDataName)) {
			return getDefaultNewSampleDataName();
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
