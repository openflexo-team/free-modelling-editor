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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.CreateNewConcept;
import org.openflexo.fme.model.action.NewConceptStructure;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.action.PropertyEntry;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.diagram.fml.ShapeRole;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * The structure of a concept created by the user is configurable: its properties (a name and a type each), the description, and the String
 * property labelling its instances - the parameter of its creation scheme, its renderer and the label of its shape.
 */
@RunWith(OrderedRunner.class)
public class TestCreateConceptWithStructure extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModel freeModel;

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateProjectAndFreeModel() {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFMEConceptStructure", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);
		assertNotNull(nature);

		CreateFMEDiagramFreeModel action = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		action.setFreeModelName("FreeModel");
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		freeModel = action.getNewFreeModel();
		assertNotNull(freeModel);
	}

	/** The default structure is the one there has always been: a name and a description, the name labelling the instances */
	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testDefaultStructure() {

		CreateNewConcept action = CreateNewConcept.actionType.makeNewAction(freeModel, null, editor);
		action.setNewConceptName("Person");
		action.setNewConceptDescription("Somebody");
		assertEquals("name", action.getStructure().getLabelPropertyName());
		assertEquals(2, action.getStructure().getPropertiesEntries().size());
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());

		FlexoConcept person = action.getNewFlexoConcept();
		assertNotNull(person.getAccessibleProperty("name"));
		assertNotNull(person.getAccessibleProperty("description"));
		assertEquals("Somebody", person.getDescription());
		assertEquals("name", FMEConceptualModel.labelPropertyName(person));
		assertEquals("instance.name", person.getRenderer().toString());
	}

	/** Properties, description and label property are the ones the user configured */
	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testConfiguredStructure() throws Exception {

		CreateNewConcept action = CreateNewConcept.actionType.makeNewAction(freeModel, null, editor);
		action.setNewConceptName("Star");
		action.setNewConceptDescription("A star of the sky");

		NewConceptStructure structure = action.getStructure();
		PropertyEntry<?> nameEntry = structure.getPropertiesEntries().get(0);
		nameEntry.setName("title");
		// Renaming the property giving the label makes the label follow
		assertEquals("title", structure.getLabelPropertyName());
		structure.deletePropertyEntry(structure.getPropertiesEntries().get(1));
		PropertyEntry<?> magnitude = structure.newPropertyEntry();
		magnitude.setName("magnitude");
		magnitude.setType(Double.class);
		magnitude.setDescription("Apparent magnitude");
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());

		FlexoConcept star = action.getNewFlexoConcept();
		assertNull(star.getAccessibleProperty("name"));
		assertNull(star.getAccessibleProperty("description"));
		assertNotNull(star.getAccessibleProperty("title"));
		assertEquals(String.class, star.getAccessibleProperty("title").getResultingType());
		assertNotNull(star.getAccessibleProperty("magnitude"));
		assertEquals("Apparent magnitude", star.getAccessibleProperty("magnitude").getDescription());
		assertEquals("A star of the sky", star.getDescription());

		assertEquals("title", FMEConceptualModel.labelPropertyName(star));
		assertEquals("instance.title", star.getRenderer().toString());

		FlexoConcept starGR = action.getNewGRFlexoConcept();
		assertEquals("instance.fmeConcept.title", starGR.getRenderer().toString());
		assertEquals("fmeConcept.title", ((ShapeRole) starGR.getAccessibleProperty(FMEDiagramFreeModel.SHAPE_ROLE_NAME)).getLabel().toString());

		// The parameter of the creation scheme gives the label
		CreateFlexoConceptInstance instantiate = CreateFlexoConceptInstance.actionType
				.makeNewAction(nature.getSampleData().getAccessedVirtualModelInstance(), null, editor);
		instantiate.setFlexoConcept(star);
		instantiate.setCreationScheme(star.getCreationSchemes().get(0));
		instantiate.setParameterValue(star.getCreationSchemes().get(0).getParameters().get(0), "Vega");
		instantiate.doAction();
		assertTrue(instantiate.hasActionExecutionSucceeded());
		FlexoConceptInstance vega = instantiate.getNewFlexoConceptInstance();
		assertEquals("Vega", vega.getFlexoPropertyValue("title"));
		assertEquals("Vega", vega.getStringRepresentation());
	}

	/** A name the FML grammar would not accept, or a structure that cannot be created, forbids the action */
	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testInvalidStructures() {

		CreateNewConcept action = CreateNewConcept.actionType.makeNewAction(freeModel, null, editor);

		for (String invalid : new String[] { "my concept", "star", "2D", "Étoile", "class", "" }) {
			action.setNewConceptName(invalid);
			assertFalse("'" + invalid + "' should be refused", action.isValid());
		}
		action.setNewConceptName("Planet");
		assertTrue(action.isValid());

		// Two properties with the same name
		NewConceptStructure structure = action.getStructure();
		structure.newPropertyEntry().setName("name");
		assertFalse(action.isValid());
		assertEquals("duplicate_property_name", structure.getIssue());
		structure.deletePropertyEntry(structure.getPropertiesEntries().get(2));
		assertTrue(action.isValid());

		// A property name FML would not accept
		structure.getPropertiesEntries().get(1).setName("Bad name");
		assertEquals("invalid_property_name", structure.getIssue());
		structure.getPropertiesEntries().get(1).setName("description");

		// No String property to label the instances
		structure.getPropertiesEntries().get(0).setType(Integer.class);
		structure.getPropertiesEntries().get(1).setType(Integer.class);
		assertEquals("no_string_property_for_the_label", structure.getIssue());
		assertFalse(action.isValid());
	}
}
