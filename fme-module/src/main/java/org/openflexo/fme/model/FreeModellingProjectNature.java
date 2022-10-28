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

package org.openflexo.fme.model;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.fme.FreeModellingEditor;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.nature.NatureObject;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;

/**
 * Defines the nature of a project to be interpreted as a FreeModelProject<br>
 * 
 * A nature shoud define a {@link FMEConceptualModel} and a list of
 *
 * Such project also gives access to the list of {@link FMEFreeModel} encoded in this project
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@XMLElement
@ImplementationClass(FreeModellingProjectNature.FreeModellingProjectNatureImpl.class)
public interface FreeModellingProjectNature extends ProjectNature<FreeModellingProjectNature>, NatureObject<FreeModellingProjectNature> {

	@PropertyIdentifier(type = FMEConceptualModel.class)
	public static final String CONCEPTUAL_MODEL = "conceptualModel";
	@PropertyIdentifier(type = FMEFreeModel.class, cardinality = Cardinality.LIST)
	public static final String FREE_MODELS = "freeModels";
	@PropertyIdentifier(type = FMESampleData.class)
	public static final String SAMPLE_DATA = "sampleData";

	@Getter(value = CONCEPTUAL_MODEL, inverse = FMEConceptualModel.OWNER_KEY)
	@XMLElement
	public FMEConceptualModel getConceptualModel();

	@Setter(CONCEPTUAL_MODEL)
	public void setConceptualModel(FMEConceptualModel conceptualModel);

	@Getter(value = SAMPLE_DATA, inverse = FMESampleData.OWNER_KEY)
	@XMLElement
	public FMESampleData getSampleData();

	@Setter(SAMPLE_DATA)
	public void setSampleData(FMESampleData sampleData);

	@Getter(value = FREE_MODELS, cardinality = Cardinality.LIST, inverse = FMEFreeModel.NATURE)
	@XMLElement
	@Embedded
	public List<FMEFreeModel> getFreeModels();

	@Setter(value = FREE_MODELS)
	public void setFreeModels(List<FMEFreeModel> freeModels);

	@Adder(FREE_MODELS)
	public void addToFreeModels(FMEFreeModel freeModel);

	@Remover(value = FREE_MODELS)
	public void removeFromFreeModels(FMEFreeModel freeModel);

	public FMEFreeModel getFreeModel(String freeModelName);

	public String getInstanceName(FlexoConceptInstance flexoConceptInstance);

	public void setInstanceName(FlexoConceptInstance flexoConceptInstance, String value);

	/**
	 * Return {@link FMEFreeModel} which access to supplied {@link VirtualModel} or null if no such free model exists
	 * 
	 * @return
	 */
	public FMEFreeModel getFreeModel(VirtualModel virtualModel);

	public abstract class FreeModellingProjectNatureImpl extends ProjectNatureImpl<FreeModellingProjectNature>
			implements FreeModellingProjectNature {

		@SuppressWarnings("unused")
		private static final Logger logger = FlexoLogger.getLogger(FreeModellingProjectNature.class.getPackage().getName());

		@Override
		public FMEFreeModel getFreeModel(String freeModelName) {
			for (FMEFreeModel freeModel : getFreeModels()) {
				if (freeModel.getName().equals(freeModelName)) {
					return freeModel;
				}
				if (freeModel.getName().equals(freeModelName + CompilationUnitResourceFactory.FML_SUFFIX)) {
					return freeModel;
				}
			}
			return null;
		}

		@Override
		public LocalizedDelegate getLocales() {
			if (getProject() != null && getProject().getServiceManager() instanceof ApplicationContext) {
				return ((ApplicationContext) getProject().getServiceManager()).getModuleLoader().getModule(FreeModellingEditor.class)
						.getLoadedModuleInstance().getLocales();
			}
			return super.getLocales();
		}

		@Override
		public String getInstanceName(FlexoConceptInstance flexoConceptInstance) {
			System.out.println("TODO: refactor this");
			FlexoRole<String> nameRole = (FlexoRole<String>) flexoConceptInstance.getFlexoConcept()
					.getAccessibleProperty(FMEFreeModel.NAME_ROLE_NAME);
			return flexoConceptInstance.getFlexoActor(nameRole);
		}

		@Override
		public void setInstanceName(FlexoConceptInstance flexoConceptInstance, String value) {
			System.out.println("TODO: refactor this");
			FlexoRole<String> nameRole = (FlexoRole<String>) flexoConceptInstance.getFlexoConcept()
					.getAccessibleProperty(FMEFreeModel.NAME_ROLE_NAME);
			flexoConceptInstance.setFlexoActor(value, nameRole);
		}

		/**
		 * Return {@link FMEFreeModel} which access to supplied {@link VirtualModel} or null if no such free model exists
		 * 
		 * @return
		 */
		@Override
		public FMEFreeModel getFreeModel(VirtualModel virtualModel) {
			for (FMEFreeModel freeModel : getFreeModels()) {
				if (freeModel.getAccessedVirtualModel() == virtualModel) {
					return freeModel;
				}
			}
			return null;
		}

	}

	/*private ProjectNatureService projectNatureService;
	
	public static final String FREE_MODELLING_VIEW_NAME = "FreeModellingView";
	public static final String FREE_MODELLING_VIEW_RELATIVE_URI = "/" + FREE_MODELLING_VIEW_NAME
			+ FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX;
	public static final String FREE_MODELLING_VIEWPOINT_NAME = "FreeModellingViewPoint";
	public static final String FREE_MODELLING_VIEWPOINT_RELATIVE_URI = "/" + FREE_MODELLING_VIEWPOINT_NAME
			+ VirtualModelResourceFactory.FML_SUFFIX;
	public static final String DIAGRAM_SPECIFICATIONS_FOLDER = "DiagramSpecifications";
	
	private final Map<FlexoProject, FreeModellingProject> freeModellingProjects;
	
	// Never call this: this is done via services
	public FreeModellingProjectNature() {
		freeModellingProjects = new HashMap<>();
	}
	
	@Override
	public void setProjectNatureService(ProjectNatureService projectNatureService) {
		this.projectNatureService = projectNatureService;
	}
	
	@Override
	public ProjectNatureService getProjectNatureService() {
		return projectNatureService;
	}
	
	@Override
	public boolean hasNature(FlexoProject project) {
		if (project == null) {
			return false;
		}
		if (project.getVirtualModelRepository() == null) {
			return false;
		}
		if (project.getVirtualModelRepository().getAllResources().size() == 0) {
			return false;
		}
		if (project.getVirtualModelInstanceRepository().getAllResources().size() == 0) {
			return false;
		}
		FreeModellingProject factory = getFreeModellingProject(project);
		if (factory == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public FreeModellingProject getProjectWrapper(FlexoProject project) {
		return getFreeModellingProject(project);
	}
	
	@Override
	public void givesNature(FlexoProject project, FlexoEditor editor) {
		GivesFMENature action = GivesFMENature.actionType.makeNewAction(project, null, editor);
		action.doAction();
	}
	
	public List<FreeModel> getFreeModels(FlexoProject project) {
		FreeModellingProject freeModellingProject = getFreeModellingProject(project);
		if (freeModellingProject == null) {
			return null;
		}
		return freeModellingProject.getFreeModels();
	}
	
	public List<FreeMetaModel> getFreeMetaModels(FlexoProject project) {
		FreeModellingProject freeModellingProject = getFreeModellingProject(project);
		if (freeModellingProject == null) {
			return null;
		}
		return freeModellingProject.getFreeMetaModels();
	}
	
	public VirtualModel getFreeModellingViewPoint(FlexoProject project) {
		FreeModellingProject factory = getFreeModellingProject(project);
		if (factory == null) {
			return null;
		}
		return factory.getFreeModellingViewPoint();
	}
	
	public FMLRTVirtualModelInstance getFreeModellingView(FlexoProject project) {
		FreeModellingProject freeModellingProject = getFreeModellingProject(project);
		if (freeModellingProject == null) {
			return null;
		}
		return freeModellingProject.getFreeModellingView();
	}
	
	public FreeModellingProject getFreeModellingProject(FlexoProject project) {
		FreeModellingProject returned = freeModellingProjects.get(project);
		if (returned == null) {
			try {
				returned = new FreeModellingProject(project, this);
				freeModellingProjects.put(project, returned);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return returned;
	}
	
	public String getInstanceName(FlexoConceptInstance flexoConceptInstance) {
		FlexoRole<String> nameRole = (FlexoRole<String>) flexoConceptInstance.getFlexoConcept()
				.getAccessibleProperty(FreeMetaModel.NAME_ROLE_NAME);
		return flexoConceptInstance.getFlexoActor(nameRole);
	}
	
	public void setInstanceName(FlexoConceptInstance flexoConceptInstance, String value) {
		FlexoRole<String> nameRole = (FlexoRole<String>) flexoConceptInstance.getFlexoConcept()
				.getAccessibleProperty(FreeMetaModel.NAME_ROLE_NAME);
		flexoConceptInstance.setFlexoActor(value, nameRole);
	}*/

}
