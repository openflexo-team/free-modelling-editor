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
package org.openflexo.fme.diagram.model;

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
import org.openflexo.fme.diagram.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.diagram.model.action.CreateNewConceptFromDiagramConnector;
import org.openflexo.fme.diagram.model.action.CreateNewConceptFromDiagramElement;
import org.openflexo.fme.diagram.model.action.CreateNewConceptFromNoneConcept;
import org.openflexo.fme.diagram.model.action.CreateNewFMEPropertyFromDiagramConnector;
import org.openflexo.fme.diagram.model.action.DeclareInstanceOfExistingConcept;
import org.openflexo.fme.diagram.model.action.DeclareInstanceOfExistingConceptFromDiagramElement;
import org.openflexo.fme.diagram.model.action.DisconnectInstanceFromConcept;
import org.openflexo.fme.diagram.model.action.DropShape;
import org.openflexo.fme.diagram.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.fml.FMLDiagramPaletteElementBinding;
import org.openflexo.technologyadapter.diagram.model.DiagramConnector;
import org.openflexo.technologyadapter.diagram.model.DiagramShape;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * The free modelling actions of the core (FME-F-3), as the Diagram extension declines them: an element standing for an existing
 * conceptual instance, a conceptual instance kept while something else represents it, the actions starting from a plain shape, and the
 * relationships and properties emerging from a connector
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestFreeModellingActions extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModel freeModel;
	private static FMEDiagramFreeModelInstance freeModelInstance;

	private static FlexoConcept animal;
	private static FlexoConcept person;
	private static FlexoConcept personGR;
	private static FlexoConceptInstance first;
	private static FlexoConceptInstance second;

	private static DiagramShape rex;
	private static DiagramShape ford;

	private static FlexoConceptInstance dropShape(double x, double y) {
		DropShape action = DropShape.actionType.makeNewAction(freeModelInstance.getDiagram(), null, editor);
		action.setDiagramFreeModelInstance(freeModelInstance);
		action.setDropLocation(new DianaPoint(x, y));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConceptInstance();
	}

	private static List<FlexoConceptInstance> sampleInstances() {
		return nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances();
	}

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateProjectAndConcepts() throws Exception {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFreeModellingActions", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);

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

		// A concept of the conceptual model which is not represented in the diagram yet: it has no GR concept
		animal = nature.getConceptualModel().getFlexoConcept("Animal", null, editor, null);
		assertNotNull(animal);
		assertNull(freeModel.getGRFlexoConcept(animal, null, editor, null, false));

		first = dropShape(12, 34);
		CreateNewConceptFromNoneConcept createConcept = CreateNewConceptFromNoneConcept.actionType.makeNewAction(first, null, editor);
		createConcept.setNewConceptName("Person");
		createConcept.doAction();
		assertTrue(createConcept.hasActionExecutionSucceeded());
		personGR = first.getFlexoConcept();
		person = FMEFreeModel.conceptRole(personGR).getFlexoConceptType();
		assertEquals(1, sampleInstances().size());

		// The free model instance knows what stands for its shapes
		DiagramShape firstShape = first.getFlexoActor(FMEDiagramFreeModel.SHAPE_ROLE_NAME);
		assertTrue(freeModelInstance.contains(firstShape));
		assertSame(first, freeModelInstance.getGRInstance(firstShape));
		assertSame(firstShape, freeModelInstance.getRepresentationElement(first));
		assertSame(freeModelInstance, nature.getFreeModelInstanceContaining(firstShape));
	}

	/** A shape standing for the instance another shape already stands for: no new instance in the sample data */
	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testDeclareExistingInstance() throws Exception {

		FlexoConceptInstance personInstance = FMEFreeModel.representedInstance(first);
		assertNotNull(personInstance);

		second = dropShape(56, 78);
		DeclareInstanceOfExistingConcept declare = DeclareInstanceOfExistingConcept.actionType.makeNewAction(second, null, editor);
		declare.setConcept(person);
		declare.setCreateNewInstance(false);
		assertFalse(declare.isValid());
		assertEquals("please_choose_an_instance", declare.getIssue());
		assertEquals(1, declare.getAvailableInstances().size());
		declare.setExistingInstance(personInstance);
		assertTrue(declare.isValid());
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());

		assertSame(personGR, second.getFlexoConcept());
		assertSame(personInstance, FMEFreeModel.representedInstance(second));
		assertEquals(1, sampleInstances().size());
		assertEquals(2, nature.getRepresentations(personInstance).size());

		// The concept already had its element in the palette: no other one
		assertEquals(1, paletteElementsDropping(personGR));
	}

	/** The conceptual instance is kept while another shape stands for it, deleted with its last representation */
	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testDisconnectSharedInstance() throws Exception {

		FlexoConceptInstance personInstance = FMEFreeModel.representedInstance(first);

		DisconnectInstanceFromConcept disconnectSecond = DisconnectInstanceFromConcept.actionType.makeNewAction(second, null, editor);
		assertFalse(disconnectSecond.willDeleteConceptInstance());
		assertTrue(disconnectSecond.getLostPropertyNames().isEmpty());
		disconnectSecond.doAction();
		assertTrue(disconnectSecond.hasActionExecutionSucceeded());
		assertFalse(disconnectSecond.isConceptInstanceDeleted());

		assertSame(freeModel.getNoneFlexoConcept(editor, null), second.getFlexoConcept());
		assertFalse(personInstance.isDeleted());
		assertEquals(1, sampleInstances().size());
		assertEquals(1, nature.getRepresentations(personInstance).size());
		assertSame(personInstance, FMEFreeModel.representedInstance(first));

		DisconnectInstanceFromConcept disconnectFirst = DisconnectInstanceFromConcept.actionType.makeNewAction(first, null, editor);
		assertTrue(disconnectFirst.willDeleteConceptInstance());
		disconnectFirst.doAction();
		assertTrue(disconnectFirst.hasActionExecutionSucceeded());
		assertTrue(disconnectFirst.isConceptInstanceDeleted());
		assertEquals(0, sampleInstances().size());
	}

	/**
	 * A plain shape (no instance of the NoneGR yet) declared as an instance of the concept the wizard proposes first, which has no GR
	 * concept yet: FME-D-2
	 */
	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testDeclarePlainShapeKeepingProposedConcept() throws Exception {

		rex = freeModelInstance.getDiagram().getDiagramFactory().makeNewShape("Rex", freeModelInstance.getDiagram());
		assertNull(freeModelInstance.getGRInstance(rex));

		DeclareInstanceOfExistingConceptFromDiagramElement declare = DeclareInstanceOfExistingConceptFromDiagramElement.actionType
				.makeNewAction(rex, null, editor);
		assertSame(animal, declare.getConcept());
		assertFalse(declare.hasGRConcept());
		assertEquals("Rex", declare.getNewInstanceName());
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());

		FlexoConceptInstance grInstance = freeModelInstance.getGRInstance(rex);
		assertNotNull(grInstance);
		assertSame(grInstance, declare.getGRInstance());
		assertEquals("AnimalGR", grInstance.getFlexoConcept().getName());
		FlexoConceptInstance rexInstance = FMEFreeModel.representedInstance(grInstance);
		assertSame(animal, rexInstance.getFlexoConcept());
		assertEquals("Rex", rexInstance.getFlexoPropertyValue("name"));

		// The first instance of the concept gives it an element in the palette, bound to its GR concept
		assertEquals(1, paletteElementsDropping(grInstance.getFlexoConcept()));
	}

	/** The number of elements of the palette dropping supplied GR concept */
	private static int paletteElementsDropping(FlexoConcept grConcept) {
		int returned = 0;
		for (FMLDiagramPaletteElementBinding binding : freeModel.getTypedDiagramModelSlot().getPaletteElementBindings()) {
			if (binding.getBoundFlexoConcept() == grConcept) {
				returned++;
			}
		}
		return returned;
	}

	/** A new concept emerging from a plain shape, named after it */
	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testCreateNewConceptFromPlainShape() throws Exception {

		ford = freeModelInstance.getDiagram().getDiagramFactory().makeNewShape("Ford", freeModelInstance.getDiagram());

		CreateNewConceptFromDiagramElement create = CreateNewConceptFromDiagramElement.actionType.makeNewAction(ford, null, editor);
		create.setNewConceptName("Car");
		assertTrue(create.isValid());
		create.doAction();
		assertTrue(create.hasActionExecutionSucceeded());

		FlexoConceptInstance grInstance = freeModelInstance.getGRInstance(ford);
		assertEquals("CarGR", grInstance.getFlexoConcept().getName());
		FlexoConceptInstance fordInstance = FMEFreeModel.representedInstance(grInstance);
		assertSame(create.getNewFlexoConcept(), fordInstance.getFlexoConcept());
		assertEquals("Ford", fordInstance.getFlexoPropertyValue("name"));
	}

	/** A relationship emerging from a connector between two shapes standing for instances */
	@Test
	@TestOrder(6)
	@Category(UITest.class)
	public void testCreateNewConceptFromConnector() throws Exception {

		DiagramConnector drives = freeModelInstance.getDiagram().getDiagramFactory().makeNewConnector("drives", rex, ford,
				freeModelInstance.getDiagram());

		CreateNewConceptFromDiagramConnector create = CreateNewConceptFromDiagramConnector.actionType.makeNewAction(drives, null, editor);
		assertTrue(create.linksTwoInstances());
		assertSame(animal, create.getFromFlexoConcept());
		assertEquals("Car", create.getToFlexoConcept().getName());
		create.getRelationalConceptAction().setNewConceptName("Drives");
		assertTrue(create.isValid());
		create.doAction();
		assertTrue(create.hasActionExecutionSucceeded());

		FlexoConceptInstance grInstance = freeModelInstance.getGRInstance(drives);
		assertSame(create.getNewFlexoConceptInstance(), grInstance);
		FlexoConceptInstance drivesInstance = FMEFreeModel.representedInstance(grInstance);
		assertEquals("Drives", drivesInstance.getFlexoConcept().getName());
		List<FlexoConceptInstanceRole> ends = FMEConceptualModel.relationEnds(drivesInstance.getFlexoConcept());
		assertEquals(2, ends.size());
		assertSame(FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(rex)), drivesInstance.getFlexoActor(ends.get(0)));
		assertSame(FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(ford)), drivesInstance.getFlexoActor(ends.get(1)));
	}

	/** A property emerging from a connector between two shapes standing for instances */
	@Test
	@TestOrder(7)
	@Category(UITest.class)
	public void testCreateNewPropertyFromConnector() throws Exception {

		DiagramConnector owner = freeModelInstance.getDiagram().getDiagramFactory().makeNewConnector("owner", ford, rex,
				freeModelInstance.getDiagram());

		CreateNewFMEPropertyFromDiagramConnector create = CreateNewFMEPropertyFromDiagramConnector.actionType.makeNewAction(owner, null,
				editor);
		assertTrue(create.linksTwoInstances());
		create.setPropertyName("owner");
		assertTrue(create.isValid());
		create.doAction();
		assertTrue(create.hasActionExecutionSucceeded());

		FlexoConceptInstance fordInstance = FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(ford));
		FlexoConceptInstance rexInstance = FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(rex));
		assertNotNull(fordInstance.getFlexoConcept().getAccessibleProperty("owner"));
		assertSame(rexInstance, fordInstance.getFlexoPropertyValue("owner"));
	}
}
