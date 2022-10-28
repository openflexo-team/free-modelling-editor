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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.connie.exception.InvalidBindingException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.model.Diagram;
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
public class TestCreateFreeModellingEditorProject extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEConceptualModel conceptualModel;
	private static FMESampleData sampleData;
	private static FlexoConcept conceptA, conceptB;
	private static FMEDiagramFreeModel diagramModel1;
	private static FMEDiagramFreeModel diagramModel2;

	@Test
	@TestOrder(1)
	@SuppressWarnings("unchecked")
	@Category(UITest.class)
	public void createFreeModellingEditorProject() {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFMEProject", FreeModellingProjectNature.class);

		project = (FlexoProject<File>) editor.getProject();

		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.hasNature(FreeModellingProjectNature.class));
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void checkFreeModellingNature() {

		assertNotNull(nature = project.getNature(FreeModellingProjectNature.class));

		conceptualModel = nature.getConceptualModel();
		assertNotNull(conceptualModel);

		sampleData = nature.getSampleData();
		assertNotNull(sampleData);
	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void checkConceptualModelEditing() throws FlexoException {

		conceptA = conceptualModel.getFlexoConcept("ConceptA", null, editor, null);
		assertNotNull(conceptA);

		conceptB = conceptualModel.getFlexoConcept("ConceptB", null, editor, null);
		assertNotNull(conceptB);

		FlexoConcept conceptAbis = conceptualModel.getFlexoConcept("ConceptA", null, editor, null);
		assertNotNull(conceptAbis);
		assertSame(conceptA, conceptAbis);

		System.out.println(conceptualModel.getAccessedVirtualModel().getFMLPrettyPrint());

		assertVirtualModelIsValid(conceptualModel.getAccessedVirtualModel());
	}

	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void createAndCheckNewFMEDiagramFreeModel() {

		CreateFMEDiagramFreeModel action = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		action.setFreeModelName("DiagramModel1");
		action.setFreeModelDescription("A description");
		action.doAction();

		diagramModel1 = action.getNewFreeModel();
		assertNotNull(diagramModel1);

		FlexoConcept conceptAGR = diagramModel1.getGRFlexoConcept(conceptA, null, editor, null, true);
		assertNotNull(conceptAGR);

		System.out.println(diagramModel1.getAccessedVirtualModel().getFMLPrettyPrint());

		FlexoConcept noneFlexoConceptGR = diagramModel1.getNoneFlexoConcept(editor, null);
		assertNotNull(noneFlexoConceptGR);

		System.out.println(diagramModel1.getAccessedVirtualModel().getFMLPrettyPrint());
		assertVirtualModelIsValid(diagramModel1.getAccessedVirtualModel());

	}

	@Test
	@TestOrder(6)
	@Category(UITest.class)
	public void instantiateNewFMEDiagramFreeModel()
			throws FlexoException, TypeMismatchException, NullReferenceException, ReflectiveOperationException, InvalidBindingException {

		InstantiateFMEDiagramFreeModel action = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(diagramModel1, null, editor);
		action.setFreeModelInstanceName("Instance1");
		action.setFreeModelInstanceDescription("A description");
		action.doAction();

		FMEDiagramFreeModelInstance instance1 = action.getNewFreeModelInstance();
		assertNotNull(instance1);

		project.save();
		project.saveModifiedResources();

		assertEquals(sampleData.getAccessedVirtualModelInstance(), instance1.getAccessedVirtualModelInstance().execute("sampleData"));

		Diagram newDiagram = instance1.getAccessedVirtualModelInstance().execute("diagram");
		assertNotNull(newDiagram);
		assertEquals(diagramModel1.getDiagramSpecification(), newDiagram.getDiagramSpecification());

	}

	@Test
	@TestOrder(7)
	@Category(UITest.class)
	public void instantiateNewFMEDiagramFreeModelWhileCreatingNewFreeModel()
			throws FlexoException, TypeMismatchException, NullReferenceException, ReflectiveOperationException, InvalidBindingException {

		InstantiateFMEDiagramFreeModel action = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		action.setFreeModelInstanceName("Instance2");
		action.setFreeModelInstanceDescription("A description");
		action.getCreateFreeModelAction().setFreeModelName("DiagramModel2");

		action.doAction();

		diagramModel2 = action.getFreeModel();
		assertNotNull(diagramModel2);

		FMEDiagramFreeModelInstance instance2 = action.getNewFreeModelInstance();
		assertNotNull(instance2);

		project.save();
		project.saveModifiedResources();

		assertEquals(sampleData.getAccessedVirtualModelInstance(), instance2.getAccessedVirtualModelInstance().execute("sampleData"));

		Diagram newDiagram2 = instance2.getAccessedVirtualModelInstance().execute("diagram");
		assertNotNull(newDiagram2);
		assertEquals(diagramModel2.getDiagramSpecification(), newDiagram2.getDiagramSpecification());

	}

}
