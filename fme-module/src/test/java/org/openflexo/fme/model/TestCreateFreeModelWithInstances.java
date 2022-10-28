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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.CreateNewConceptFromNoneConcept;
import org.openflexo.fme.model.action.DeclareInstanceOfExistingConcept;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
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
public class TestCreateFreeModelWithInstances extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModelInstance freeModelInstance;
	private static FMEDiagramFreeModel freeModel;

	private static FlexoConceptInstance tutu;
	private static FlexoConceptInstance tutu2;

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
	@TestOrder(4)
	@Category(UITest.class)
	public void testInstantiateFreeModel() throws SaveResourceException {

		InstantiateFMEDiagramFreeModel action = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(freeModel, null, editor);
		action.setFreeModelInstanceName("FreeModelInstance");
		action.setFreeModelInstanceDescription("A description");
		action.doAction();

		freeModelInstance = action.getNewFreeModelInstance();
		assertNotNull(freeModelInstance);
		assertSame(freeModel.getFreeModelInstances().get(0), freeModelInstance);

		freeModelInstance.getAccessedVirtualModelInstance().getResource().save();
	}

	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testCreateInstance() {
		DropShape action = DropShape.actionType.makeNewAction(freeModelInstance.getDiagram(), null, editor);
		action.setDiagramFreeModelInstance(freeModelInstance);
		action.setDropLocation(new DianaPoint(12, 34));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		List<FlexoConceptInstance> result = freeModelInstance
				.getInstances(freeModelInstance.getFreeModel().getNoneFlexoConcept(editor, action));
		assertEquals(1, result.size());
		tutu = result.get(0);
		assertNotNull(tutu);
		DiagramShape laShapeDeLinstance = tutu.getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		assertNotNull(laShapeDeLinstance);
		List<DiagramShape> lesShapes = freeModelInstance.getDiagram().getShapes();
		assertNotNull(lesShapes);
		assertEquals(1, lesShapes.size());
		assertSame(laShapeDeLinstance, lesShapes.get(0));

		assertEquals(FMEFreeModel.NONE_FLEXO_CONCEPT_NAME, tutu.getFlexoConcept().getName());

	}

	private static FlexoConcept tutuConcept;

	@Test
	@TestOrder(6)
	@Category(UITest.class)
	public void testMakeNewConceptFromTutu() throws SaveResourceException {

		assertEquals(1, freeModelInstance.getInstances(freeModelInstance.getFreeModel().getNoneFlexoConcept(editor, null)).size());

		CreateNewConceptFromNoneConcept action = CreateNewConceptFromNoneConcept.actionType.makeNewAction(tutu, null, editor);
		action.setNewConceptName("TutuConcept");
		action.setNewConceptDescription("This is the description for TutuConcept");

		assertEquals(nature, action.getFreeModellingProjectNature());
		assertEquals(freeModel, action.getFMEFreeModel());
		assertEquals(freeModelInstance, action.getFMEFreeModelInstance());

		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());

		assertEquals("TutuConceptGR", tutu.getFlexoConcept().getName());

		assertEquals(0, freeModelInstance.getInstances(freeModelInstance.getFreeModel().getNoneFlexoConcept(editor, null)).size());

		System.out.println("concepts: " + nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts());
		assertEquals(1, nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().size());
		tutuConcept = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().get(0);

		System.out.println("concepts GR: " + freeModel.getAccessedVirtualModel().getFlexoConcepts());
		assertEquals(2, freeModel.getAccessedVirtualModel().getFlexoConcepts().size());

		System.out.println("concepts instances: " + nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances());
		assertEquals(1, nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances().size());

		System.out.println("GR concepts instances: " + freeModelInstance.getAccessedVirtualModelInstance().getFlexoConceptInstances());
		assertEquals(1, freeModelInstance.getAccessedVirtualModelInstance().getFlexoConceptInstances().size());

		project.save();
		project.saveModifiedResources();

	}

	@Test
	@TestOrder(7)
	@Category(UITest.class)
	public void testCreateTutu2Instance() throws SaveResourceException {

		assertEquals(0, freeModelInstance.getInstances(freeModelInstance.getFreeModel().getNoneFlexoConcept(editor, null)).size());

		// Create the shape as an instance of NoneGR
		DropShape action = DropShape.actionType.makeNewAction(freeModelInstance.getDiagram(), null, editor);
		action.setDiagramFreeModelInstance(freeModelInstance);
		action.setDropLocation(new DianaPoint(56, 78));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		tutu2 = action.getNewFlexoConceptInstance();

		List<FlexoConceptInstance> result = freeModelInstance
				.getInstances(freeModelInstance.getFreeModel().getNoneFlexoConcept(editor, action));
		assertEquals(1, result.size());
		assertSame(tutu2, result.get(0));

		DeclareInstanceOfExistingConcept declareAction = DeclareInstanceOfExistingConcept.actionType.makeNewAction(tutu2, null, editor);
		declareAction.setConcept(tutuConcept);

		assertEquals(nature, declareAction.getFreeModellingProjectNature());
		assertEquals(freeModel, declareAction.getFMEFreeModel());
		assertEquals(freeModelInstance, declareAction.getFMEFreeModelInstance());

		declareAction.doAction();
		assertTrue(declareAction.hasActionExecutionSucceeded());

		assertEquals("TutuConceptGR", tutu.getFlexoConcept().getName());
		assertEquals(0, freeModelInstance.getInstances(freeModelInstance.getFreeModel().getNoneFlexoConcept(editor, null)).size());

		System.out.println("concepts: " + nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts());
		assertEquals(1, nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().size());

		System.out.println("concepts GR: " + freeModel.getAccessedVirtualModel().getFlexoConcepts());
		assertEquals(2, freeModel.getAccessedVirtualModel().getFlexoConcepts().size());

		System.out.println("concepts instances: " + nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances());
		assertEquals(2, nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances().size());

		System.out.println("GR concepts instances: " + freeModelInstance.getAccessedVirtualModelInstance().getFlexoConceptInstances());
		assertEquals(2, freeModelInstance.getAccessedVirtualModelInstance().getFlexoConceptInstances().size());

		project.save();
		project.saveModifiedResources();

	}
}
