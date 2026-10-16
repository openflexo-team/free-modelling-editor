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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.CreateNewConcept;
import org.openflexo.fme.model.action.RenameFMEConcept;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.fml.DropScheme;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * A concept and its graphical representation are renamed together, from the concept, from its GR concept or from an instance of one of
 * them: the GR is found by its name alone, so renaming only one of them would make FME create a second GR for the concept.
 */
@RunWith(OrderedRunner.class)
public class TestRenameFMEConcept extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModel freeModel;

	private static FlexoConcept person;
	private static FlexoConcept personGR;
	private static FlexoConceptInstance alice;

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateProjectFreeModelAndConcept() throws Exception {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFMERenameConcept", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);
		assertNotNull(nature);

		CreateFMEDiagramFreeModel createFreeModel = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		createFreeModel.setFreeModelName("FreeModel");
		createFreeModel.doAction();
		assertTrue(createFreeModel.hasActionExecutionSucceeded());
		freeModel = createFreeModel.getNewFreeModel();

		CreateNewConcept createConcept = CreateNewConcept.actionType.makeNewAction(freeModel, null, editor);
		createConcept.setNewConceptName("Person");
		createConcept.doAction();
		assertTrue(createConcept.hasActionExecutionSucceeded());
		person = createConcept.getNewFlexoConcept();
		personGR = createConcept.getNewGRFlexoConcept();
		assertEquals("PersonGR", personGR.getName());

		CreateFlexoConceptInstance instantiate = CreateFlexoConceptInstance.actionType
				.makeNewAction(nature.getSampleData().getAccessedVirtualModelInstance(), null, editor);
		instantiate.setFlexoConcept(person);
		instantiate.setCreationScheme(person.getCreationSchemes().get(0));
		instantiate.setParameterValue(person.getCreationSchemes().get(0).getParameters().get(0), "Alice");
		instantiate.doAction();
		assertTrue(instantiate.hasActionExecutionSucceeded());
		alice = instantiate.getNewFlexoConceptInstance();

		// An entry in the palette, as FME adds one when a shape is declared as a concept
		freeModel.createPaletteElementForConcept(personGR, person,
				((ShapeRole) personGR.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME)).getGraphicalRepresentation(),
				CreateNewConcept.actionType.makeNewAction(freeModel, null, editor));
		assertNotNull(freeModel.getConceptsPalette().getPaletteElement("Person"));
	}

	/** The concept is renamed along with its GR, wherever the action starts from */
	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testRenameFromTheConcept() throws Exception {

		RenameFMEConcept rename = RenameFMEConcept.actionType.makeNewAction(person, null, editor);
		assertSame(person, rename.getConcept());
		assertEquals(1, rename.getGRConcepts().size());
		assertSame(personGR, rename.getGRConcepts().get(0));
		assertEquals("Person", rename.getNewConceptName());

		// Not a new name
		assertFalse(rename.isValid());
		for (String invalid : new String[] { "my concept", "human", "2D", "class", "" }) {
			rename.setNewConceptName(invalid);
			assertFalse("'" + invalid + "' should be refused", rename.isValid());
		}

		rename.setNewConceptName("Human");
		assertTrue(rename.isValid());
		rename.doAction();
		assertTrue(rename.hasActionExecutionSucceeded());

		assertEquals("Human", person.getName());
		assertEquals("HumanGR", personGR.getName());
		assertEquals(person, FMEFreeModel.conceptRole(personGR).getFlexoConceptType());

		// The GR is still the one FME finds: no second GR gets created
		assertSame(personGR, freeModel.getGRFlexoConcept(person, null, editor, null, true));

		// What was derived from the name follows
		DropScheme drop = personGR.getFlexoBehaviours(DropScheme.class).get(0);
		assertEquals("\"Human\"", drop.getParameter(FMEConceptualModel.CONCEPT_NAME_PARAMETER).getDefaultValue().toString());
		assertNull(freeModel.getConceptsPalette().getPaletteElement("Person"));
		assertNotNull(freeModel.getConceptsPalette().getPaletteElement("Human"));
		assertEquals("HumanGR",
				((ShapeRole) personGR.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME)).getMetamodelElement().getName());

		// The instances are still instances of the concept
		assertSame(person, alice.getFlexoConcept());
		assertEquals("Alice", alice.getStringRepresentation());

		// Marked as modified, not saved
		assertTrue(person.getDeclaringCompilationUnit().getResource().isModified());
		assertTrue(personGR.getDeclaringCompilationUnit().getResource().isModified());
	}

	/** From the GR concept */
	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testRenameFromTheGRConcept() throws Exception {

		RenameFMEConcept rename = RenameFMEConcept.actionType.makeNewAction(personGR, null, editor);
		assertSame(person, rename.getConcept());
		rename.setNewConceptName("Individual");
		assertTrue(rename.isValid());
		rename.doAction();
		assertTrue(rename.hasActionExecutionSucceeded());
		assertEquals("Individual", person.getName());
		assertEquals("IndividualGR", personGR.getName());
	}

	/** From an instance of the concept */
	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testRenameFromAnInstance() throws Exception {

		RenameFMEConcept rename = RenameFMEConcept.actionType.makeNewAction(alice, null, editor);
		assertSame(person, rename.getConcept());
		assertEquals("Individual", rename.getNewConceptName());
		rename.setNewConceptName("Person");
		assertTrue(rename.isValid());
		rename.doAction();
		assertTrue(rename.hasActionExecutionSucceeded());
		assertEquals("Person", person.getName());
		assertEquals("PersonGR", personGR.getName());
		assertNotNull(freeModel.getConceptsPalette().getPaletteElement("Person"));
	}

	/** A name already used by a concept or by a GR, the NoneGR, a VirtualModel: nothing to rename */
	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testRefusedRenames() throws Exception {

		CreateNewConcept createConcept = CreateNewConcept.actionType.makeNewAction(freeModel, null, editor);
		createConcept.setNewConceptName("Animal");
		createConcept.doAction();
		assertTrue(createConcept.hasActionExecutionSucceeded());

		RenameFMEConcept rename = RenameFMEConcept.actionType.makeNewAction(person, null, editor);
		rename.setNewConceptName("Animal");
		assertEquals("a_concept_with_that_name_already_exists", rename.getIssue());
		assertFalse(rename.isValid());

		FlexoConcept none = freeModel.getNoneFlexoConcept(editor, null);
		assertNull(RenameFMEConcept.conceptToRename(none));
		assertNull(RenameFMEConcept.conceptToRename(freeModel.getAccessedVirtualModel()));
		assertNull(RenameFMEConcept.conceptToRename(nature.getConceptualModel().getAccessedVirtualModel()));
	}
}
