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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * This unit test is intented to test project creation in the context of FreeModellingProjectNature
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestCreateFreeModel extends OpenflexoProjectAtRunTimeTestCase {

	static FlexoEditor editor;
	static FlexoProject<File> project;
	static FreeModellingProjectNature nature;
	static FMEDiagramFreeModelInstance freeModelInstance1;
	static FMEDiagramFreeModelInstance freeModelInstance2;
	static FMEDiagramFreeModel freeModel;

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateFreeModellingEditorProject() {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFMEProject", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.hasNature(FreeModellingProjectNature.class));
		assertNotNull(nature = project.getNature(FreeModellingProjectNature.class));
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testCreateFreeModel() throws SaveResourceException {

		CreateFMEDiagramFreeModel action = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		action.setFreeModelName("FreeModel");
		action.setFreeModelDescription("A description");
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		freeModel = action.getNewFreeModel();
		assertNotNull(freeModel);
		freeModel.getAccessedVirtualModelResource().save();
	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testAvoidDuplicatedFreeModel() {
		CreateFMEDiagramFreeModel action = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		action.setFreeModelName("FreeModel");
		assertFalse(action.isValid());
	}

	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testInstantiateFreeModel1() throws SaveResourceException {

		InstantiateFMEDiagramFreeModel action = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(freeModel, null, editor);
		action.setFreeModelInstanceName("FreeModelInstance1");
		action.setFreeModelInstanceDescription("A description");
		action.doAction();

		freeModelInstance1 = action.getNewFreeModelInstance();
		assertNotNull(freeModelInstance1);
		assertSame(freeModel.getFreeModelInstances().get(0), freeModelInstance1);

		freeModelInstance1.getAccessedVirtualModelInstance().getResource().save();
	}

	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testInstantiateFreeModel2() throws SaveResourceException {
		InstantiateFMEDiagramFreeModel action = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(freeModel, null, editor);
		action.setFreeModelInstanceName("FreeModelInstance2");
		action.setFreeModelInstanceDescription("A description");
		action.doAction();

		freeModelInstance2 = action.getNewFreeModelInstance();
		assertNotNull(freeModelInstance2);
		assertSame(freeModel.getFreeModelInstances().get(1), freeModelInstance2);

		freeModelInstance2.getAccessedVirtualModelInstance().getResource().save();

		project.save();
		project.saveModifiedResources();
	}

	/**
	 * Reload project, check that everything is still ok
	 * 
	 * @throws FileNotFoundException
	 * @throws ResourceLoadingCancelledException
	 * @throws FlexoException
	 */
	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testReloadProject() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);
		editor = loadProject(project.getProjectDirectory());
		project = (FlexoProject<File>) editor.getProject();
		assertNotNull(editor);
		assertNotNull(project);
		assertTrue(project.hasNature(FreeModellingProjectNature.class));

		nature = project.getNature(FreeModellingProjectNature.class);
		assertNotNull(nature);

		assertNotNull(nature.getConceptualModel());
		assertNotNull(nature.getSampleData());

		System.out.println("Project dir = " + project.getProjectDirectory());

		assertEquals(1, nature.getFreeModels().size());
		freeModel = (FMEDiagramFreeModel) nature.getFreeModel("FreeModel");
		assertSame(nature.getFreeModels().get(0), freeModel);

		assertEquals(2, freeModel.getFreeModelInstances().size());

		freeModelInstance1 = (FMEDiagramFreeModelInstance) freeModel.getFreeModelInstance("FreeModelInstance1");
		assertNotNull(freeModelInstance1);
		assertSame(freeModel.getFreeModelInstances().get(0), freeModelInstance1);
		freeModelInstance2 = (FMEDiagramFreeModelInstance) freeModel.getFreeModelInstance("FreeModelInstance2");
		assertNotNull(freeModelInstance2);
		assertSame(freeModel.getFreeModelInstances().get(1), freeModelInstance2);

	}
}
