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
package org.openflexo.fme.pptx.model.action;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.model.action.InstantiateFMEFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.nature.NatureObject;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;

/**
 * Annotates a slideshow: a new instance of the PowerPoint free model of the project (created the first time) annotates the slideshow
 * where it is, in its resource center. The slideshow is neither copied nor modified. A slideshow is annotated once in a project: when it
 * already is, its annotation is opened instead ({@link #getExistingAnnotation()}).
 * 
 * @author sylvain
 * 
 */
public class InstantiateFMEPowerpointFreeModel extends InstantiateFMEFreeModel<InstantiateFMEPowerpointFreeModel, FMEPowerpointFreeModel> {

	public static FlexoActionFactory<InstantiateFMEPowerpointFreeModel, NatureObject<FreeModellingProjectNature>, FlexoObject> actionType = new FlexoActionFactory<InstantiateFMEPowerpointFreeModel, NatureObject<FreeModellingProjectNature>, FlexoObject>(
			"instantiate_powerpoint_free_model", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public InstantiateFMEPowerpointFreeModel makeNewAction(NatureObject<FreeModellingProjectNature> focusedObject,
				Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new InstantiateFMEPowerpointFreeModel(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(NatureObject<FreeModellingProjectNature> object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(NatureObject<FreeModellingProjectNature> object, Vector<FlexoObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(InstantiateFMEPowerpointFreeModel.actionType, FreeModellingProjectNature.class);
		FlexoObjectImpl.addActionForClass(InstantiateFMEPowerpointFreeModel.actionType, FMEPowerpointFreeModel.class);
	}

	private static final Logger logger = Logger.getLogger(InstantiateFMEPowerpointFreeModel.class.getPackage().getName());

	private PowerpointSlideshowResource slideshowResource;
	private String freeModelInstanceName;

	private InstantiateFMEPowerpointFreeModel(NatureObject<FreeModellingProjectNature> focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * The free model of the project when a slideshow was already annotated, a new one otherwise: the user does not choose it
	 */
	@Override
	public FreeModelChoice getFreeModelChoice() {
		if (getFocusedObject() instanceof FMEPowerpointFreeModel || getExistingFreeModel() != null) {
			return FreeModelChoice.SelectExistingFreeModel;
		}
		return FreeModelChoice.CreateNewFreeModel;
	}

	@Override
	public FMEPowerpointFreeModel getExistingFreeModel() {
		if (getFocusedObject() instanceof FMEPowerpointFreeModel) {
			return (FMEPowerpointFreeModel) getFocusedObject();
		}
		return FMEPowerpointFreeModel.getFreeModel(getNature());
	}

	/**
	 * The free model instance already annotating the chosen slideshow, which this action opens instead of creating another one, or null
	 */
	public FMEPowerpointFreeModelInstance getExistingAnnotation() {
		FMEPowerpointFreeModel freeModel = getExistingFreeModel();
		if (freeModel == null || getSlideshowResource() == null) {
			return null;
		}
		for (FMEFreeModelInstance freeModelInstance : freeModel.getFreeModelInstances()) {
			if (freeModelInstance instanceof FMEPowerpointFreeModelInstance
					&& ((FMEPowerpointFreeModelInstance) freeModelInstance).getSlideshow() != null
					&& ((FMEPowerpointFreeModelInstance) freeModelInstance).getSlideshow().getResource() == getSlideshowResource()) {
				return (FMEPowerpointFreeModelInstance) freeModelInstance;
			}
		}
		return null;
	}

	/**
	 * The name of the new free model instance: the name of the slideshow, unless set
	 */
	@Override
	public String getFreeModelInstanceName() {
		if (freeModelInstanceName != null) {
			return freeModelInstanceName;
		}
		if (getSlideshowResource() != null) {
			return defaultNameFor(getSlideshowResource());
		}
		return super.getFreeModelInstanceName();
	}

	@Override
	public void setFreeModelInstanceName(String freeModelInstanceName) {
		this.freeModelInstanceName = freeModelInstanceName;
		super.setFreeModelInstanceName(freeModelInstanceName);
	}

	/**
	 * The name of the slideshow, without its extension and reduced to letters, digits, '-' and '_', made unique among the instances of the
	 * free model
	 */
	private String defaultNameFor(PowerpointSlideshowResource resource) {
		String baseName = resource.getName();
		if (baseName.toLowerCase().endsWith(".pptx")) {
			baseName = baseName.substring(0, baseName.length() - ".pptx".length());
		}
		baseName = baseName.replaceAll("[^A-Za-z0-9_-]", "_");
		FMEPowerpointFreeModel freeModel = getExistingFreeModel();
		String returned = baseName;
		int index = 2;
		while (freeModel != null && freeModel.getFreeModelInstance(returned) != null) {
			returned = baseName + index++;
		}
		return returned;
	}

	@Override
	public CreateFMEPowerpointFreeModel makeCreateFreeModelAction() {
		return CreateFMEPowerpointFreeModel.actionType.makeNewEmbeddedAction(getNature(), null, this);
	}

	@Override
	public CreateFMEPowerpointFreeModel getCreateFreeModelAction() {
		return (CreateFMEPowerpointFreeModel) super.getCreateFreeModelAction();
	}

	/**
	 * The slideshow the new free model instance annotates
	 */
	public PowerpointSlideshowResource getSlideshowResource() {
		return slideshowResource;
	}

	public void setSlideshowResource(PowerpointSlideshowResource slideshowResource) {
		if (slideshowResource != this.slideshowResource) {
			boolean wasValid = isValid();
			PowerpointSlideshowResource oldValue = this.slideshowResource;
			this.slideshowResource = slideshowResource;
			getPropertyChangeSupport().firePropertyChange("slideshowResource", oldValue, slideshowResource);
			getPropertyChangeSupport().firePropertyChange("freeModelInstanceName", null, getFreeModelInstanceName());
			getPropertyChangeSupport().firePropertyChange("existingAnnotation", null, getExistingAnnotation());
			getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
		}
	}

	/**
	 * The slideshows of all the resource centers
	 */
	public List<PowerpointSlideshowResource> getAvailableSlideshows() {
		List<PowerpointSlideshowResource> returned = new ArrayList<>();
		PowerpointTechnologyAdapter ta = getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(PowerpointTechnologyAdapter.class);
		if (ta != null) {
			for (FlexoResourceCenter<?> rc : getServiceManager().getResourceCenterService().getResourceCenters()) {
				if (ta.getPowerpointSlideShowRepository(rc) != null) {
					returned.addAll(ta.getPowerpointSlideShowRepository(rc).getAllResources());
				}
			}
		}
		return returned;
	}

	/**
	 * A slideshow is chosen: either it is annotated already (its annotation is opened), or a new free model instance is created
	 */
	@Override
	public boolean isValid() {
		if (getSlideshowResource() == null) {
			return false;
		}
		return getExistingAnnotation() != null || super.isValid();
	}

	/**
	 * A slideshow is annotated once in a project: when it already is, its annotation is returned instead of a new one
	 */
	@Override
	protected FMEFreeModelInstance instantiateFreeModel(String freeModelInstanceName) {

		FMEPowerpointFreeModelInstance existing = getExistingAnnotation();
		if (existing != null) {
			return existing;
		}

		PowerpointSlideshow slideshow;
		try {
			slideshow = getSlideshowResource().getResourceData();
		} catch (ResourceLoadingCancelledException | FileNotFoundException e) {
			logger.warning("Could not load " + getSlideshowResource() + ": " + e);
			return null;
		} catch (FlexoException e) {
			logger.warning("Could not load " + getSlideshowResource() + ": " + e);
			return null;
		}

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewEmbeddedAction(
				getFreeModel().getNature().getProject().getVirtualModelInstanceRepository().getRootFolder(), null, this);
		action.setNewVirtualModelInstanceName(freeModelInstanceName);
		action.setNewVirtualModelInstanceTitle(getFreeModelInstanceDescription());
		action.setVirtualModel(getFreeModel().getAccessedVirtualModel());
		CreationScheme creationScheme = getFreeModel().getAccessedVirtualModel().getCreationSchemes().get(0);
		action.setCreationScheme(creationScheme);
		action.setParameterValue(creationScheme.getParameter("sampleData"),
				getFreeModel().getSampleData().getAccessedVirtualModelInstance());
		action.setParameterValue(creationScheme.getParameter(FMEPowerpointFreeModel.SLIDESHOW_MODEL_SLOT_NAME),
				slideshow);
		action.doAction();

		FMLRTVirtualModelInstance newVirtualModelInstance = action.getNewVirtualModelInstance();

		FMEPowerpointFreeModelInstance newFreeModelInstance = getFreeModel().getNature().getOwner().getModelFactory()
				.newInstance(FMEPowerpointFreeModelInstance.class);
		newFreeModelInstance.setAccessedVirtualModelInstance(newVirtualModelInstance);
		getFreeModel().addToFreeModelInstances(newFreeModelInstance);

		return newFreeModelInstance;
	}

	@Override
	public FMEPowerpointFreeModelInstance getNewFreeModelInstance() {
		return (FMEPowerpointFreeModelInstance) super.getNewFreeModelInstance();
	}

}
