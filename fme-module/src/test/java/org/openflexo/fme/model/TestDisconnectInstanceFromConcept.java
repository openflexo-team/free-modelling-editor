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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.CreateNewConceptFromNoneConcept;
import org.openflexo.fme.model.action.DeclareInstanceOfExistingConcept;
import org.openflexo.fme.model.action.DisconnectInstanceFromConcept;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * An instance of a concept is disconnected from it: its shape stays in the diagram, as an instance of the NoneGR carrying the label of the
 * instance, and the instance of the conceptual concept is deleted. It is the opposite of {@link DeclareInstanceOfExistingConcept}.
 */
@RunWith(OrderedRunner.class)
public class TestDisconnectInstanceFromConcept extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModel freeModel;
	private static FMEDiagramFreeModelInstance freeModelInstance;

	private static FlexoConcept person;
	private static FlexoConcept personGR;
	private static FlexoConceptInstance first;
	private static FlexoConceptInstance second;

	private static FlexoConceptInstance dropShape(double x, double y) {
		DropShape action = DropShape.actionType.makeNewAction(freeModelInstance.getDiagram(), null, editor);
		action.setDiagramFreeModelInstance(freeModelInstance);
		action.setDropLocation(new DianaPoint(x, y));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConceptInstance();
	}

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateProjectFreeModelAndInstances() throws Exception {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFMEDisconnect", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);
		assertNotNull(nature);

		CreateFMEDiagramFreeModel createFreeModel = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		createFreeModel.setFreeModelName("FreeModel");
		createFreeModel.doAction();
		assertTrue(createFreeModel.hasActionExecutionSucceeded());
		freeModel = createFreeModel.getNewFreeModel();

		InstantiateFMEDiagramFreeModel instantiate = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(freeModel, null, editor);
		instantiate.setFreeModelInstanceName("FreeModelInstance");
		instantiate.doAction();
		freeModelInstance = instantiate.getNewFreeModelInstance();
		assertNotNull(freeModelInstance);

		// A shape promoted to a new concept, then another one declared as an instance of it
		first = dropShape(12, 34);
		CreateNewConceptFromNoneConcept createConcept = CreateNewConceptFromNoneConcept.actionType.makeNewAction(first, null, editor);
		createConcept.setNewConceptName("Person");
		createConcept.doAction();
		assertTrue(createConcept.hasActionExecutionSucceeded());
		personGR = first.getFlexoConcept();
		assertEquals("PersonGR", personGR.getName());
		person = FMEFreeModel.conceptRole(personGR).getFlexoConceptType();

		second = dropShape(56, 78);
		DeclareInstanceOfExistingConcept declare = DeclareInstanceOfExistingConcept.actionType.makeNewAction(second, null, editor);
		declare.setConcept(person);
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());
		assertSame(personGR, second.getFlexoConcept());
		assertEquals(2, nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances().size());
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testDisconnect() throws Exception {

		FlexoConcept none = freeModel.getNoneFlexoConcept(editor, null);
		FlexoConceptInstance conceptInstance = second.getFlexoActor(FMEFreeModel.conceptRoleName(personGR));
		assertNotNull(conceptInstance);
		String label = conceptInstance.getFlexoPropertyValue("name");
		assertNotNull(label);
		DiagramShape shape = second.getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		assertNotNull(shape);

		assertSame(conceptInstance, DisconnectInstanceFromConcept.conceptInstanceOf(second));
		assertTrue(DisconnectInstanceFromConcept.actionType.isEnabled(second, null));
		assertNull(DisconnectInstanceFromConcept.getIssue(second));

		DisconnectInstanceFromConcept disconnect = DisconnectInstanceFromConcept.actionType.makeNewAction(second, null, editor);
		assertTrue(disconnect.isValid());
		disconnect.doAction();
		assertTrue(disconnect.hasActionExecutionSucceeded());

		// A plain graphical element again, carrying the label
		assertSame(none, second.getFlexoConcept());
		assertEquals(label, second.getFlexoPropertyValue(FMEFreeModel.NAME_ROLE_NAME));
		assertSame(shape, second.getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME));
		List<DiagramShape> shapes = freeModelInstance.getDiagram().getShapes();
		assertEquals(2, shapes.size());
		assertTrue(shapes.contains(shape));

		// The instance of the concept is gone, the other one is untouched
		assertEquals(1, nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances().size());
		assertEquals(1, freeModelInstance.getInstances(personGR).size());
		assertEquals(1, freeModelInstance.getInstances(none).size());
		assertSame(personGR, first.getFlexoConcept());

		// The concept still exists: its name cannot be used again, whichever way a concept is created
		CreateNewConceptFromNoneConcept again = CreateNewConceptFromNoneConcept.actionType.makeNewAction(second, null, editor);
		again.setNewConceptName("Person");
		assertFalse(again.isValid());
		assertTrue(freeModel.isConceptNameUsed("Person"));
		assertFalse(freeModel.isConceptNameUsed("Animal"));
		again.setNewConceptName("Animal");
		assertTrue(again.isValid());

		// Nothing left to disconnect
		assertNull(DisconnectInstanceFromConcept.conceptInstanceOf(second));
		assertFalse(DisconnectInstanceFromConcept.actionType.isEnabled(second, null));
	}

	/** The way back: the shape can be declared as an instance of the concept again */
	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testDeclareAgain() throws Exception {

		DeclareInstanceOfExistingConcept declare = DeclareInstanceOfExistingConcept.actionType.makeNewAction(second, null, editor);
		declare.setConcept(person);
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());
		assertSame(personGR, second.getFlexoConcept());
		assertEquals(2, nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances().size());
		assertEquals(2, freeModelInstance.getInstances(personGR).size());
	}
}
