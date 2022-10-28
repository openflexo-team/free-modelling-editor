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
import org.openflexo.fme.model.action.CreateFMEPPTFreeModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.IconFactory;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.metamodel.DiagramSpecification;
import org.openflexo.technologyadapter.powerpoint.controller.ChoosePPTSlide;
import org.openflexo.technologyadapter.powerpoint.gui.PowerpointIconLibrary;
import org.openflexo.view.controller.FlexoController;

public class CreateFMEPPTFreeModelWizard extends AbstractCreateFMEFreeModelWizard<CreateFMEPPTFreeModel> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateFMEPPTFreeModelWizard.class.getPackage().getName());

	private ChoosePPTSlide choosePPTSlide;

	public CreateFMEPPTFreeModelWizard(CreateFMEPPTFreeModel action, FlexoController controller) {
		super(action, controller);
		addStep(choosePPTSlide = new ChoosePPTSlide(controller) {
			@Override
			public void done() {
				super.done();
				// getAction().setDiagramName(getDiagramName());
				// getAction().setDiagramTitle(getDiagramTitle());
				getAction().setFile(getFile());
				getAction().setSlide(getSlide());
			}
		});
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_powerpoint_free_model");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(PowerpointIconLibrary.POWERPOINT_TECHNOLOGY_BIG_ICON, FMEIconLibrary.FME_BIG_MARKER).getImage();
	}

	@Override
	public DescribePPTFreeModel makeDescribeFreeModel() {
		return new DescribePPTFreeModel();
	}

	/**
	 * This step is used to set name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/DescribePPTFreeModel.fib")
	public class DescribePPTFreeModel extends DescribeFreeModel {

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			/*if (getDiagramSpecificationFolder() == null) {
				setIssueMessage(getAction().getLocales().localizedForKey("please_define_diagram_specification_folder"),
						IssueMessageType.ERROR);
				return false;
			}*/

			return true;
		}

		/*public RepositoryFolder<?, ?> getDiagramSpecificationFolder() {
			return getAction().getDiagramSpecificationFolder();
		}
		
		public void setDiagramSpecificationFolder(RepositoryFolder<?, ?> diagramSpecificationFolder) {
			if (!diagramSpecificationFolder.equals(getDiagramSpecificationFolder())) {
				RepositoryFolder<?, ?> oldValue = getDiagramSpecificationFolder();
				getAction().setDiagramSpecificationFolder(diagramSpecificationFolder);
				getPropertyChangeSupport().firePropertyChange("diagramSpecificationFolder", oldValue, diagramSpecificationFolder);
				checkValidity();
			}
		}*/

		public Type getDiagramSpecificationType() {
			return DiagramSpecification.class;
		}

		public DiagramTechnologyAdapter getDiagramTechnologyAdapter() {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(DiagramTechnologyAdapter.class);
		}

	}

}
