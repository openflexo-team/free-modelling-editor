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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.CreateNewConcept;
import org.openflexo.fme.model.action.CreateNewRelationalConcept;
import org.openflexo.fme.model.action.GeneralizeConcepts;
import org.openflexo.fme.model.action.GeneralizeConcepts.FactoredProperty;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.PropertyEntry;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Several concepts are generalized by a common super concept, which gets what they share
 */
@RunWith(OrderedRunner.class)
public class TestGeneralizeConcepts extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModel freeModel;

	private static FlexoConcept cat;
	private static FlexoConcept dog;
	private static FlexoConceptInstance felix;

	private static Vector<FlexoObject> selection(FlexoObject... objects) {
		return new Vector<>(Arrays.asList(objects));
	}

	private static FlexoConcept createConcept(String name, Class<?> weightType) throws Exception {
		CreateNewConcept action = CreateNewConcept.actionType.makeNewAction(freeModel, null, editor);
		action.setNewConceptName(name);
		// name, description, then 'age' (shared) and 'weight' (the type of which differs)
		PropertyEntry<?> age = action.getStructure().newPropertyEntry();
		age.setName("age");
		age.setType(Integer.class);
		PropertyEntry<?> weight = action.getStructure().newPropertyEntry();
		weight.setName("weight");
		weight.setType(weightType);
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConcept();
	}

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateConcepts() throws Exception {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);

		editor = createStandaloneProject("TestFMEGeneralize", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);

		CreateFMEDiagramFreeModel createFreeModel = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		createFreeModel.setFreeModelName("FreeModel");
		createFreeModel.doAction();
		assertTrue(createFreeModel.hasActionExecutionSucceeded());
		freeModel = createFreeModel.getNewFreeModel();

		cat = createConcept("Cat", Integer.class);
		dog = createConcept("Dog", Double.class);

		CreateFlexoConceptInstance instantiate = CreateFlexoConceptInstance.actionType
				.makeNewAction(nature.getSampleData().getAccessedVirtualModelInstance(), null, editor);
		instantiate.setFlexoConcept(cat);
		instantiate.setCreationScheme(cat.getCreationSchemes().get(0));
		instantiate.setParameterValue(cat.getCreationSchemes().get(0).getParameters().get(0), "Felix");
		instantiate.doAction();
		assertTrue(instantiate.hasActionExecutionSucceeded());
		felix = instantiate.getNewFlexoConceptInstance();
		felix.setFlexoPropertyValue("age", 7);
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testProposal() throws Exception {

		// Alone: nothing to generalize
		GeneralizeConcepts alone = GeneralizeConcepts.actionType.makeNewAction(cat, null, editor);
		assertEquals(1, alone.getConcepts().size());
		assertFalse(GeneralizeConcepts.actionType.isVisibleForSelection(cat, null));

		GeneralizeConcepts generalize = GeneralizeConcepts.actionType.makeNewAction(cat, selection(cat, dog), editor);
		assertEquals(Arrays.asList(cat, dog), generalize.getConcepts());
		assertTrue(generalize.getCommonParents().isEmpty());
		assertNull(generalize.getCommonContainer());

		StringBuilder names = new StringBuilder();
		for (FactoredProperty property : generalize.getProposedProperties()) {
			names.append(property.getName()).append(' ');
		}
		assertEquals("name description age ", names.toString());
		// Same name, different types
		assertEquals(Arrays.asList("weight"), generalize.getNotFactoredProperties());

		assertFalse(generalize.isValid());
		generalize.setNewConceptName("animal");
		assertEquals("invalid_concept_name", generalize.getIssue());
		generalize.setNewConceptName("Cat");
		assertEquals("a_concept_with_that_name_already_exists", generalize.getIssue());
		generalize.setNewConceptName("Animal");
		assertTrue(generalize.isValid());
	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testGeneralize() throws Exception {

		// Selecting an instance of a concept and a concept is enough
		GeneralizeConcepts generalize = GeneralizeConcepts.actionType.makeNewAction(felix, selection(felix, dog), editor);
		assertEquals(Arrays.asList(cat, dog), generalize.getConcepts());
		generalize.setNewConceptName("Animal");
		generalize.setNewConceptDescription("Any animal");
		// 'description' stays where it is
		for (FactoredProperty property : generalize.getProposedProperties()) {
			if (property.getName().equals("description")) {
				property.setFactored(false);
			}
		}
		assertTrue(generalize.isValid());
		generalize.doAction();
		assertTrue(generalize.hasActionExecutionSucceeded());

		FlexoConcept animal = generalize.getNewFlexoConcept();
		assertNotNull(animal);
		assertEquals("Animal", animal.getName());
		assertEquals("Any animal", animal.getDescription());
		assertEquals(Arrays.asList(animal), cat.getParentFlexoConcepts());
		assertEquals(Arrays.asList(animal), dog.getParentFlexoConcepts());

		// Abstract by default: it is never instantiated by itself
		assertTrue(generalize.isNewConceptAbstract());
		assertTrue(animal.isAbstract());
		assertTrue(animal.getCreationSchemes().isEmpty());
		assertTrue(animal.getDeletionSchemes().isEmpty());

		// The shared properties went up
		assertNotNull(animal.getDeclaredProperty("name"));
		assertNotNull(animal.getDeclaredProperty("age"));
		assertNull(animal.getDeclaredProperty("description"));
		assertNull(animal.getDeclaredProperty("weight"));
		assertNull(cat.getDeclaredProperty("name"));
		assertNull(cat.getDeclaredProperty("age"));
		assertNull(dog.getDeclaredProperty("age"));
		assertNotNull(cat.getDeclaredProperty("description"));
		assertNotNull(cat.getDeclaredProperty("weight"));
		assertNotNull(dog.getDeclaredProperty("weight"));
		// ... and are still there for the concepts
		assertNotNull(cat.getAccessibleProperty("name"));
		assertNotNull(dog.getAccessibleProperty("age"));

		// The label still works, the instances are intact
		assertEquals("name", FMEConceptualModel.labelPropertyName(cat));
		assertEquals("Felix", felix.getFlexoPropertyValue("name"));
		assertEquals(7, ((Number) felix.getFlexoPropertyValue("age")).intValue());
		assertEquals("Felix", felix.getStringRepresentation());

		// New instances still work
		CreateFlexoConceptInstance instantiate = CreateFlexoConceptInstance.actionType
				.makeNewAction(nature.getSampleData().getAccessedVirtualModelInstance(), null, editor);
		instantiate.setFlexoConcept(dog);
		instantiate.setCreationScheme(dog.getCreationSchemes().get(0));
		instantiate.setParameterValue(dog.getCreationSchemes().get(0).getParameters().get(0), "Rex");
		instantiate.doAction();
		assertTrue(instantiate.hasActionExecutionSucceeded());
		assertEquals("Rex", instantiate.getNewFlexoConceptInstance().getFlexoPropertyValue("name"));

		// No renderer was invented for the super concept, even when somebody reads it
		assertFalse(animal.hasMetaData(FlexoConcept.RENDERER_METADATA));
		animal.getRenderer().isSet();
		animal.getRenderer().isValid();
		animal.getRenderer().setUnparsedBinding("");
		animal.revalidateBindings();
		// What a binding analysis notifies
		animal.getRenderer().getOwner().notifiedBindingChanged(animal.getRenderer());
		assertFalse(animal.hasMetaData(FlexoConcept.RENDERER_METADATA));

		// The inspectors show the inherited properties as well
		FMEInspectorAssertions.assertInspectorIsValid(cat, "nameTextField", "weightNumber", "ageNumber", "descriptionTextArea");
		FMEInspectorAssertions.assertInspectorIsValid(animal, "nameTextField", "ageNumber");

		// Marked, not saved
		assertTrue(animal.getDeclaringCompilationUnit().getResource().isModified());
	}

	@Test
	@TestOrder(4)
	@Category(UITest.class)
	public void testInsertBetweenParentsAndRefusals() throws Exception {

		FlexoConcept animal = cat.getParentFlexoConcepts().get(0);
		FlexoConcept bird = createConcept("Bird", Integer.class);
		FlexoConcept fish = createConcept("Fish", Integer.class);
		bird.addToParentFlexoConcepts(animal);
		fish.addToParentFlexoConcepts(animal);

		// Bird and Fish have Animal in common: the new super concept comes in between
		GeneralizeConcepts generalize = GeneralizeConcepts.actionType.makeNewAction(bird, selection(bird, fish), editor);
		assertEquals(Arrays.asList(animal), generalize.getCommonParents());
		generalize.setNewConceptName("Swimmer");
		// Not abstract: its instances are labelled by a String property it declares
		generalize.setNewConceptAbstract(false);
		assertTrue(generalize.isValid());
		for (FactoredProperty property : generalize.getProposedProperties()) {
			property.setFactored(!property.getName().equals("name") && !property.getName().equals("description"));
		}
		assertEquals("a_concept_that_is_not_abstract_needs_a_string_property", generalize.getPropertiesIssue());
		assertFalse(generalize.isValid());
		for (FactoredProperty property : generalize.getProposedProperties()) {
			property.setFactored(true);
		}
		assertEquals("name", generalize.getLabelPropertyName());
		assertTrue(generalize.isValid());
		generalize.doAction();
		assertTrue(generalize.hasActionExecutionSucceeded());
		FlexoConcept swimmer = generalize.getNewFlexoConcept();
		assertEquals(Arrays.asList(animal), swimmer.getParentFlexoConcepts());
		assertEquals(Arrays.asList(swimmer), bird.getParentFlexoConcepts());
		assertEquals(Arrays.asList(swimmer), fish.getParentFlexoConcepts());
		assertFalse(swimmer.isAbstract());
		assertEquals(1, swimmer.getCreationSchemes().size());
		assertEquals(1, swimmer.getDeletionSchemes().size());
		assertEquals("instance.name", swimmer.getRenderer().toString());

		// ... and then instantiable
		CreateFlexoConceptInstance instantiate = CreateFlexoConceptInstance.actionType
				.makeNewAction(nature.getSampleData().getAccessedVirtualModelInstance(), null, editor);
		instantiate.setFlexoConcept(swimmer);
		instantiate.setCreationScheme(swimmer.getCreationSchemes().get(0));
		instantiate.setParameterValue(swimmer.getCreationSchemes().get(0).getParameters().get(0), "Nemo");
		instantiate.doAction();
		assertTrue(instantiate.hasActionExecutionSucceeded());
		assertEquals("Nemo", instantiate.getNewFlexoConceptInstance().getFlexoPropertyValue("name"));
		assertNotNull(bird.getAccessibleProperty("age"));

		// A concept cannot be generalized with one of its descendants
		List<FlexoConcept> withDescendant = Arrays.asList(animal, bird);
		assertEquals("a_concept_cannot_be_generalized_with_one_of_its_descendants", GeneralizeConcepts.selectionIssue(withDescendant));
		assertEquals("select_at_least_two_concepts", GeneralizeConcepts.selectionIssue(Arrays.asList(animal)));
	}

	private static FlexoConcept createRelation(String name, FlexoConcept from, FlexoConcept to) throws Exception {
		return createRelation(name, from, to, null, null);
	}

	private static FlexoConcept createRelation(String name, FlexoConcept from, FlexoConcept to, String fromRoleName, String toRoleName)
			throws Exception {
		CreateNewRelationalConcept action = CreateNewRelationalConcept.actionType.makeNewAction(freeModel, null, editor);
		action.setNewConceptName(name);
		action.setFromConcept(from);
		action.setToConcept(to);
		if (fromRoleName != null) {
			action.setFromRoleName(fromRoleName);
			action.setToRoleName(toRoleName);
		}
		action.setFromGRConcept(freeModel.getGRFlexoConcept(from, null, editor, null, false));
		action.setToGRConcept(freeModel.getGRFlexoConcept(to, null, editor, null, false));
		PropertyEntry<?> since = action.getStructure().newPropertyEntry();
		since.setName("since");
		since.setType(Integer.class);
		PropertyEntry<?> strength = action.getStructure().newPropertyEntry();
		strength.setName("strength");
		strength.setType(Integer.class);
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConcept();
	}

	/** Relationships (the concepts a connector stands for) are generalized as well, the super concept having no end */
	@Test
	@TestOrder(5)
	@Category(UITest.class)
	public void testGeneralizeRelationships() throws Exception {

		FlexoConcept bird = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Bird");
		FlexoConcept fish = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Fish");
		FlexoConcept eats = createRelation("Eats", bird, fish);
		FlexoConcept chases = createRelation("Chases", fish, bird);
		assertTrue(FMEConceptualModel.isRelationship(eats));
		String fromRole = FMEConceptualModel.fromRoleName(eats);

		GeneralizeConcepts generalize = GeneralizeConcepts.actionType.makeNewAction(eats, selection(eats, chases), editor);
		assertEquals(Arrays.asList(eats, chases), generalize.getConcepts());
		assertTrue(GeneralizeConcepts.actionType.isVisibleForSelection(eats, selection(eats, chases)));
		assertTrue(GeneralizeConcepts.actionType.isEnabledForSelection(eats, selection(eats, chases)));

		// The properties of the relationship, not the roles pointing to its ends
		StringBuilder names = new StringBuilder();
		for (FactoredProperty property : generalize.getProposedProperties()) {
			names.append(property.getName()).append(' ');
		}
		assertEquals("since strength ", names.toString());
		assertTrue(generalize.getNotFactoredProperties().isEmpty());

		// The roles pointing to the ends are not named the same way: no end for the super concept
		assertNull(generalize.getCommonEnds());
		assertEquals("relationship_ends_have_different_role_names", generalize.getEndsIssue());
		assertNull(generalize.getGeneralizedEnds());

		generalize.setNewConceptName("Interaction");
		assertTrue(generalize.isValid());
		generalize.doAction();
		assertTrue(generalize.hasActionExecutionSucceeded());

		FlexoConcept interaction = generalize.getNewFlexoConcept();
		assertNotNull(interaction.getDeclaredProperty("since"));
		assertNotNull(interaction.getDeclaredProperty("strength"));
		assertFalse(FMEConceptualModel.isRelationship(interaction));
		assertEquals(Arrays.asList(interaction), eats.getParentFlexoConcepts());
		assertEquals(Arrays.asList(interaction), chases.getParentFlexoConcepts());
		assertNull(eats.getDeclaredProperty("since"));
		assertNotNull(eats.getAccessibleProperty("since"));

		// Still relationships, from the same bird to the same fish
		assertTrue(FMEConceptualModel.isRelationship(eats));
		assertTrue(FMEConceptualModel.isRelationship(chases));
		assertEquals(fromRole, FMEConceptualModel.fromRoleName(eats));

		// A relationship and a concept can be generalized together
		GeneralizeConcepts mixed = GeneralizeConcepts.actionType.makeNewAction(eats, selection(eats, cat), editor);
		assertEquals(Arrays.asList(eats, cat), mixed.getConcepts());
		assertTrue(GeneralizeConcepts.actionType.isEnabledForSelection(eats, selection(eats, cat)));
		assertTrue(mixed.getProposedProperties().isEmpty());
	}

	/** The ends the relationships share (same role names, concepts with a concept in common) go to the super concept */
	@Test
	@TestOrder(6)
	@Category(UITest.class)
	public void testGeneralizeEnds() throws Exception {

		FlexoConcept bird = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Bird");
		FlexoConcept fish = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Fish");
		FlexoConcept swimmer = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Swimmer");
		FlexoConcept hunts = createRelation("Hunts", bird, fish, "hunter", "prey");
		FlexoConcept follows = createRelation("Follows", fish, bird, "hunter", "prey");

		GeneralizeConcepts generalize = GeneralizeConcepts.actionType.makeNewAction(hunts, selection(hunts, follows), editor);
		assertTrue(generalize.isRelationshipsSelection());
		assertNull(generalize.getEndsIssue());
		GeneralizeConcepts.RelationEnds ends = generalize.getCommonEnds();
		assertNotNull(ends);
		assertEquals("hunter", ends.getFromRoleName());
		assertEquals("prey", ends.getToRoleName());
		// Bird and Fish are both Swimmers
		assertEquals(swimmer, ends.getFromType());
		assertEquals(swimmer, ends.getToType());
		assertEquals(ends.toString(), generalize.getGeneralizedEnds().toString());

		generalize.setNewConceptName("Pursuit");
		// A relationship with ends has no connector: it is abstract
		generalize.setNewConceptAbstract(false);
		assertEquals("a_relationship_with_ends_must_be_abstract", generalize.getPropertiesIssue());
		assertFalse(generalize.isValid());
		// ... unless it has no end
		generalize.setGeneralizeEnds(false);
		assertTrue(generalize.getPropertiesIssue() == null || !"a_relationship_with_ends_must_be_abstract".equals(generalize.getPropertiesIssue()));
		generalize.setGeneralizeEnds(true);
		generalize.setNewConceptAbstract(true);
		assertTrue(generalize.isValid());
		generalize.doAction();
		assertTrue(generalize.hasActionExecutionSucceeded());

		FlexoConcept pursuit = generalize.getNewFlexoConcept();
		assertTrue(pursuit.isAbstract());
		assertTrue(FMEConceptualModel.isRelationship(pursuit));
		assertEquals("hunter", FMEConceptualModel.fromRoleName(pursuit));
		assertEquals("prey", FMEConceptualModel.toRoleName(pursuit));
		assertEquals(swimmer, ((FlexoConceptInstanceRole) pursuit.getDeclaredProperty("hunter")).getFlexoConceptType());
		assertEquals(swimmer, ((FlexoConceptInstanceRole) pursuit.getDeclaredProperty("prey")).getFlexoConceptType());
		assertNotNull(pursuit.getDeclaredProperty("since"));

		// The relationships override them with their own, more specific, types
		assertEquals(Arrays.asList(pursuit), hunts.getParentFlexoConcepts());
		assertTrue(FMEConceptualModel.isRelationship(hunts));
		assertEquals(bird, ((FlexoConceptInstanceRole) hunts.getAccessibleProperty("hunter")).getFlexoConceptType());
		assertEquals(fish, ((FlexoConceptInstanceRole) hunts.getAccessibleProperty("prey")).getFlexoConceptType());
		assertEquals(fish, ((FlexoConceptInstanceRole) follows.getAccessibleProperty("hunter")).getFlexoConceptType());
		assertEquals(bird, ((FlexoConceptInstanceRole) follows.getAccessibleProperty("prey")).getFlexoConceptType());
		assertNull(hunts.getDeclaredProperty("since"));
		FMEInspectorAssertions.assertInspectorIsValid(pursuit, "hunterSelector", "preySelector", "sinceNumber", "strengthNumber");
	}

	/** Same role names, but no concept in common at the ends: the super concept has none */
	@Test
	@TestOrder(7)
	@Category(UITest.class)
	public void testNoCommonEnds() throws Exception {

		FlexoConcept bird = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Bird");
		FlexoConcept fish = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Fish");
		FlexoConcept rock = createConcept("Rock", Integer.class);
		FlexoConcept hits = createRelation("Hits", rock, rock, "origin", "target");
		FlexoConcept bites = createRelation("Bites", bird, fish, "origin", "target");

		GeneralizeConcepts generalize = GeneralizeConcepts.actionType.makeNewAction(hits, selection(hits, bites), editor);
		assertNull(generalize.getCommonEnds());
		assertEquals("relationship_ends_have_no_common_type", generalize.getEndsIssue());
		generalize.setNewConceptName("Contact");
		assertTrue(generalize.isValid());
		generalize.doAction();
		assertTrue(generalize.hasActionExecutionSucceeded());
		assertFalse(FMEConceptualModel.isRelationship(generalize.getNewFlexoConcept()));
		assertNotNull(generalize.getNewFlexoConcept().getDeclaredProperty("since"));
		assertTrue(FMEConceptualModel.isRelationship(hits));
		assertTrue(FMEConceptualModel.isRelationship(bites));
	}
}
