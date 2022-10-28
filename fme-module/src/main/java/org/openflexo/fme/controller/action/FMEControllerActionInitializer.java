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

import java.util.logging.Logger;

import org.openflexo.fme.controller.FMEController;
import org.openflexo.selection.SelectionManager;
import org.openflexo.view.controller.ControllerActionInitializer;

/**
 * 
 * Action initializing for this module
 * 
 * @author yourname
 */
public class FMEControllerActionInitializer extends ControllerActionInitializer {

	private static final Logger logger = Logger.getLogger(ControllerActionInitializer.class.getPackage().getName());

	public FMEControllerActionInitializer(FMEController controller) {
		super(controller);
	}

	protected FMEController getFMEController() {
		return (FMEController) getController();
	}

	protected SelectionManager getFMESelectionManager() {
		return getFMEController().getSelectionManager();
	}

	@Override
	public void initializeActions() {
		super.initializeActions();

		new GivesFMENatureInitializer(this);

		new CreateFMEDiagramFreeModelInitializer(this);
		new InstantiateFMEDiagramFreeModelInitializer(this);
		// new CreateFreeModelFromPPTInitializer(this);

		new CreateFMEPPTFreeModelInitializer(this);

		new DropFreeShapeInitializer(this);
		new CreateNewConceptInitializer(this);
		new CreateNewRelationalConceptInitializer(this);
		new CreateNewConceptFromNoneInitializer(this);
		new DeclareInstanceOfExistingConceptInitializer(this);
		// new CreateFreeModelDiagramInitializer(this);
		// new CreateFreeModelDiagramFromPPTInitializer(this);

		// Actions applied on Diagram Elements(not currently associated with flexo concept)
		new CreateNewConceptFromDiagramElementInitializer(this);
		new DeclareInstanceOfExistingConceptFromDiagramElementInitializer(this);
		new CreateNewFMEPropertyFromDiagramConnectorInitializer(this);

		new DeleteFreeModelInitializer(this);
		new DeleteFreeModelInstanceInitializer(this);

		new DeleteFlexoConceptObjectsInitializer(this);

		new CreateNewFMEPropertyInitializer(this);
		new InstantiateNewFMEPropertyInitializer(this);
	}

}
