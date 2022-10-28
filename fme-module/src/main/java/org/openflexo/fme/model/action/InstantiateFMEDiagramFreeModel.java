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

import org.apache.commons.lang3.StringUtils;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.FMEDiagramFreeModelInstance;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.nature.NatureObject;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.model.action.CreateDiagram;
import org.openflexo.technologyadapter.diagram.rm.DiagramRepository;
import org.openflexo.technologyadapter.diagram.rm.DiagramResource;

/**
 * This action is used to instantiate a new {@link FMEFreeModelInstance} conform to a {@link FMEDiagramFreeModel}<br>
 * 
 * @author sylvain
 * 
 */
public class InstantiateFMEDiagramFreeModel extends InstantiateFMEFreeModel<InstantiateFMEDiagramFreeModel, FMEDiagramFreeModel> {

	private static final Logger logger = Logger.getLogger(InstantiateFMEDiagramFreeModel.class.getPackage().getName());

	public static FlexoActionFactory<InstantiateFMEDiagramFreeModel, NatureObject<FreeModellingProjectNature>, FlexoObject> actionType = new FlexoActionFactory<InstantiateFMEDiagramFreeModel, NatureObject<FreeModellingProjectNature>, FlexoObject>(
			"instantiate_diagram_free_model", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public InstantiateFMEDiagramFreeModel makeNewAction(NatureObject<FreeModellingProjectNature> focusedObject,
				Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new InstantiateFMEDiagramFreeModel(focusedObject, globalSelection, editor);
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
		FlexoObjectImpl.addActionForClass(InstantiateFMEDiagramFreeModel.actionType, FreeModellingProjectNature.class);
		FlexoObjectImpl.addActionForClass(InstantiateFMEDiagramFreeModel.actionType, FMEDiagramFreeModel.class);
	}

	private String diagramName;
	private RepositoryFolder<DiagramResource, ?> diagramFolder;

	InstantiateFMEDiagramFreeModel(NatureObject<FreeModellingProjectNature> focusedObject, Vector<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public CreateFMEDiagramFreeModel makeCreateFreeModelAction() {
		return CreateFMEDiagramFreeModel.actionType.makeNewEmbeddedAction(getNature(), null, this);
	}

	@Override
	public CreateFMEDiagramFreeModel getCreateFreeModelAction() {
		return (CreateFMEDiagramFreeModel) super.getCreateFreeModelAction();
	}

	@Override
	protected FMEFreeModelInstance instantiateFreeModel(String freeModelInstanceName) {

		System.out.println("diagramFolder=" + getDiagramFolder());
		CreateDiagram createDiagram = CreateDiagram.actionType.makeNewEmbeddedAction(getDiagramFolder(), null, this);
		createDiagram.setDiagramName(getDiagramName() + ".diagram");
		createDiagram.setDiagramTitle(getFreeModelInstanceDescription());
		createDiagram.setDiagramSpecification(getFreeModel().getDiagramSpecification());

		createDiagram.doAction();

		DiagramResource newDiagramResource = createDiagram.getNewDiagramResource();
		System.out.println("newDiagramResource=" + newDiagramResource);

		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType.makeNewEmbeddedAction(
				getFreeModel().getNature().getProject().getVirtualModelInstanceRepository().getRootFolder(), null, this);
		action.setNewVirtualModelInstanceName(getFreeModelInstanceName());
		action.setVirtualModel(getFreeModel().getAccessedVirtualModel());
		CreationScheme creationScheme = getFreeModel().getAccessedVirtualModel().getCreationSchemes().get(0);
		action.setCreationScheme(creationScheme);

		// System.out.println("Found: " + creationScheme.getFMLRepresentation());

		action.setParameterValue(creationScheme.getParameter("sampleData"),
				getFreeModel().getSampleData().getAccessedVirtualModelInstance());

		System.out.println("Les sample data: " + getFreeModel().getSampleData().getAccessedVirtualModelInstance());

		action.setParameterValue(creationScheme.getParameter("diagram"), newDiagramResource.getDiagram());

		System.out.println("Le diagram: " + newDiagramResource.getDiagram());

		// System.out.println("On execute donc " + creationScheme.getFMLRepresentation());
		// System.out.println("avec " + action.getCreationSchemeAction().getParametersValues());

		action.doAction();

		FMLRTVirtualModelInstance newVirtualModelInstance = action.getNewVirtualModelInstance();

		System.out.println(newVirtualModelInstance.getFactory().stringRepresentation(newVirtualModelInstance));

		FMEDiagramFreeModelInstance newFreeModelInstance = getFreeModel().getNature().getOwner().getModelFactory()
				.newInstance(FMEDiagramFreeModelInstance.class);
		newFreeModelInstance.setAccessedVirtualModelInstance(newVirtualModelInstance);
		getFreeModel().addToFreeModelInstances(newFreeModelInstance);

		return newFreeModelInstance;
	}

	/**
	 * Return newly created {@link FMEDiagramFreeModelInstance}
	 * 
	 * @return
	 */
	@Override
	public FMEDiagramFreeModelInstance getNewFreeModelInstance() {
		return (FMEDiagramFreeModelInstance) super.getNewFreeModelInstance();
	}

	private String diagramFolderName;

	public String getDiagramFolderName() {
		if (diagramFolderName == null) {
			return FMEDiagramFreeModelInstance.DEFAULT_DIAGRAM_FOLDER;
		}
		return diagramFolderName;
	}

	public void setDiagramFolderName(String diagramFolderName) {
		if ((diagramFolderName == null && this.diagramFolderName != null)
				|| (diagramFolderName != null && !diagramFolderName.equals(this.diagramFolderName))) {
			String oldValue = this.diagramFolderName;
			this.diagramFolderName = diagramFolderName;
			getPropertyChangeSupport().firePropertyChange("diagramFolderName", oldValue, diagramFolderName);
		}
	}

	public RepositoryFolder<DiagramResource, ?> getDiagramFolder() {
		if (diagramFolder == null && StringUtils.isNotEmpty(getDiagramFolderName())) {
			DiagramTechnologyAdapter diagramTechnologyAdapter = getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(DiagramTechnologyAdapter.class);
			DiagramRepository<?> dsRepository = diagramTechnologyAdapter.getDiagramRepository(getNature().getOwner());

			diagramFolder = dsRepository.getFolderWithName(getDiagramFolderName());
			if (diagramFolder == null) {
				diagramFolder = dsRepository.createNewFolder(getDiagramFolderName());
			}
		}
		return diagramFolder;
	}

	public void setDiagramFolder(RepositoryFolder<DiagramResource, ?> diagramFolder) {
		if ((diagramFolder == null && this.diagramFolder != null) || (diagramFolder != null && !diagramFolder.equals(this.diagramFolder))) {
			RepositoryFolder<?, ?> oldValue = this.diagramFolder;
			this.diagramFolder = diagramFolder;
			getPropertyChangeSupport().firePropertyChange("diagramFolder", oldValue, diagramFolder);
		}
	}

	public String getDiagramName() {
		if (diagramName == null) {
			return getFreeModelInstanceName();
		}
		return diagramName;
	}

	public void setDiagramName(String diagramName) {
		if ((diagramName == null && this.diagramName != null) || (diagramName != null && !diagramName.equals(this.diagramName))) {
			String oldValue = this.diagramName;
			this.diagramName = diagramName;
			getPropertyChangeSupport().firePropertyChange("diagramName", oldValue, diagramName);
		}
	}

}
