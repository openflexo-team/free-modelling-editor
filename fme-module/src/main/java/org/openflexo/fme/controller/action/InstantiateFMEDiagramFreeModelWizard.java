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

package org.openflexo.fme.controller.action;

import java.awt.Image;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.fme.FMEIconLibrary;
import org.openflexo.fme.model.FMEDiagramFreeModel;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramSpecification;
import org.openflexo.technologyadapter.diagram.model.Diagram;
import org.openflexo.technologyadapter.diagram.rm.DiagramResource;
import org.openflexo.view.controller.FlexoController;

public class InstantiateFMEDiagramFreeModelWizard
		extends AbstractInstantiateFMEFreeModelWizard<InstantiateFMEDiagramFreeModel, FMEDiagramFreeModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InstantiateFMEDiagramFreeModelWizard.class.getPackage().getName());

	public InstantiateFMEDiagramFreeModelWizard(InstantiateFMEDiagramFreeModel action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("instantiate_free_model_with_a_diagram");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMEIconLibrary.DIAGRAM_BIG_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	@Override
	public ChooseDiagramFreeModel makeChooseFreeModel() {
		return new ChooseDiagramFreeModel();
	}

	@Override
	public AbstractInstantiateFMEFreeModelWizard<InstantiateFMEDiagramFreeModel, FMEDiagramFreeModel>.DescribeFreeModelInstance makeDescribeFreeModelInstance() {
		return new DescribeDiagramFreeModelInstance();
	}

	/**
	 * This step is used to set name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/ChooseDiagramFreeModel.fib")
	public class ChooseDiagramFreeModel extends ChooseFreeModel {

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getDiagramSpecificationFolder() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_define_diagram_specification_folder"),
						IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public RepositoryFolder<?, ?> getDiagramSpecificationFolder() {
			return getAction().getCreateFreeModelAction().getDiagramSpecificationFolder();
		}

		public void setDiagramSpecificationFolder(RepositoryFolder<?, ?> diagramSpecificationFolder) {
			if (!diagramSpecificationFolder.equals(getDiagramSpecificationFolder())) {
				RepositoryFolder<?, ?> oldValue = getDiagramSpecificationFolder();
				getAction().getCreateFreeModelAction().setDiagramSpecificationFolder(diagramSpecificationFolder);
				getPropertyChangeSupport().firePropertyChange("diagramSpecificationFolder", oldValue, diagramSpecificationFolder);
				checkValidity();
			}
		}

		public Type getDiagramSpecificationType() {
			return DiagramSpecification.class;
		}

		public DiagramTechnologyAdapter getDiagramTechnologyAdapter() {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(DiagramTechnologyAdapter.class);
		}

	}

	/**
	 * This step is used to set name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/DescribeDiagramFreeModelInstance.fib")
	public class DescribeDiagramFreeModelInstance extends DescribeFreeModelInstance {

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getDiagramFolder() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_define_diagram_folder"), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		public RepositoryFolder<?, ?> getDiagramFolder() {
			return getAction().getDiagramFolder();
		}

		public void setDiagramFolder(RepositoryFolder<DiagramResource, ?> diagramFolder) {
			if ((diagramFolder == null && getDiagramFolder() != null)
					|| (diagramFolder != null && !diagramFolder.equals(getDiagramFolder()))) {
				RepositoryFolder<?, ?> oldValue = getDiagramFolder();
				getAction().setDiagramFolder(diagramFolder);
				getPropertyChangeSupport().firePropertyChange("diagramFolder", oldValue, diagramFolder);
				checkValidity();
			}
		}

		@Override
		public void setFreeModelInstanceName(String newFreeModelInstanceName) {

			if (!newFreeModelInstanceName.equals(getFreeModelInstanceName())) {
				String oldValue = getDiagramName();
				super.setFreeModelInstanceName(newFreeModelInstanceName);
				getPropertyChangeSupport().firePropertyChange("diagramName", oldValue, getDiagramName());
				checkValidity();
			}
		}

		public String getDiagramName() {
			return getAction().getDiagramName();
		}

		public void setDiagramName(String diagramName) {
			if ((diagramName == null && getDiagramName() != null) || (diagramName != null && !diagramName.equals(getDiagramName()))) {
				String oldValue = getDiagramName();
				getAction().setDiagramName(diagramName);
				getPropertyChangeSupport().firePropertyChange("diagramName", oldValue, diagramName);
				checkValidity();
			}
		}

		public Type getDiagramType() {
			return Diagram.class;
		}

		public DiagramTechnologyAdapter getDiagramTechnologyAdapter() {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(DiagramTechnologyAdapter.class);
		}

	}

}
