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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.fme.model.action.InstantiateNewFMEProperty;
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
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Instantiating a property from an instance: the value of that instance, and the default value given to the other instances of the
 * concept (stored in the role as well, for the instances created afterwards)
 */
@RunWith(OrderedRunner.class)
public class TestInstantiateNewFMEProperty extends OpenflexoProjectAtRunTimeTestCase {

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEDiagramFreeModelInstance freeModelInstance;
	private static FMEDiagramFreeModel freeModel;
	private static FlexoConceptInstance tutu;
	private static FlexoConceptInstance tutu2;
	private static FlexoConcept tutuConcept;

	private static FlexoConceptInstance dropShape(int x) {
		DropShape action = DropShape.actionType.makeNewAction(freeModelInstance.getDiagram(), null, editor);
		action.setDiagramFreeModelInstance(freeModelInstance);
		action.setDropLocation(new DianaPoint(x, x));
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewFlexoConceptInstance();
	}

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testSetup() throws SaveResourceException {
		instanciateTestServiceManager(DiagramTechnologyAdapter.class);
		editor = createStandaloneProject("TestFMEProject", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);

		CreateFMEDiagramFreeModel create = CreateFMEDiagramFreeModel.actionType.makeNewAction(nature, null, editor);
		create.setFreeModelName("FreeModel");
		create.setFreeModelDescription("A description");
		create.doAction();
		freeModel = create.getNewFreeModel();

		InstantiateFMEDiagramFreeModel instantiate = InstantiateFMEDiagramFreeModel.actionType.makeNewAction(freeModel, null, editor);
		instantiate.setFreeModelInstanceName("FreeModelInstance");
		instantiate.setFreeModelInstanceDescription("A description");
		instantiate.doAction();
		freeModelInstance = instantiate.getNewFreeModelInstance();

		tutu = dropShape(12);
		CreateNewConceptFromNoneConcept makeConcept = CreateNewConceptFromNoneConcept.actionType.makeNewAction(tutu, null, editor);
		makeConcept.setNewConceptName("TutuConcept");
		makeConcept.setNewConceptDescription("A description");
		makeConcept.doAction();
		assertTrue(makeConcept.hasActionExecutionSucceeded());
		tutuConcept = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcepts().get(0);

		tutu2 = dropShape(56);
		DeclareInstanceOfExistingConcept declare = DeclareInstanceOfExistingConcept.actionType.makeNewAction(tutu2, null, editor);
		declare.setConcept(tutuConcept);
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());
		assertEquals(2, nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances(tutuConcept).size());
	}

	private FlexoConceptInstance conceptInstance(FlexoConceptInstance gr) {
		return gr.getFlexoPropertyValue(FMEFreeModel.conceptRoleName(gr.getFlexoConcept()));
	}

	private InstantiateNewFMEProperty instantiate(String name, FMEType type) {
		InstantiateNewFMEProperty action = InstantiateNewFMEProperty.actionType.makeNewAction(tutu, null, editor);
		action.getCreatePropertyAction().setPropertyName(name);
		action.getCreatePropertyAction().setFMEType(type);
		return action;
	}

	private void check(String name, Object forThisInstance, Object forOthers, String expectedExpression) {
		FlexoRole<?> role = (FlexoRole<?>) tutuConcept.getAccessibleProperty(name);
		assertNotNull(role);
		assertEquals(forThisInstance, conceptInstance(tutu).getFlexoPropertyValue(name));
		assertEquals(forOthers, conceptInstance(tutu2).getFlexoPropertyValue(name));
		if (expectedExpression == null) {
			assertFalse(role.getDefaultValue().isSet());
		}
		else {
			assertEquals(expectedExpression, role.getDefaultValue().toString());
		}
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testDefaultValues() {
		InstantiateNewFMEProperty a = instantiate("comment", FMEType.String);
		a.setStringValue("mine");
		a.setDefaultStringValue("a default");
		a.doAction();
		assertTrue(a.hasActionExecutionSucceeded());
		check("comment", "mine", "a default", "\"a default\"");

		InstantiateNewFMEProperty b = instantiate("count", FMEType.Integer);
		b.setIntegerValue(3);
		b.setDefaultIntegerValue(7);
		b.doAction();
		check("count", 3, 7, "7");

		InstantiateNewFMEProperty c = instantiate("active", FMEType.Boolean);
		c.setBooleanValue(false);
		c.setDefaultBooleanValue(true);
		c.doAction();
		check("active", false, true, "true");

		InstantiateNewFMEProperty d = instantiate("weight", FMEType.Float);
		d.setFloatValue(1.5);
		d.setDefaultFloatValue(2.5);
		d.doAction();
		check("weight", 1.5, 2.5, "2.5");

		// A string with a quote has no literal: the other instances get it, the role has no default
		InstantiateNewFMEProperty q = instantiate("quoted", FMEType.String);
		q.setDefaultStringValue("say \"hi\"");
		q.doAction();
		check("quoted", null, "say \"hi\"", null);

		// No default value: the other instances are left untouched
		InstantiateNewFMEProperty e = instantiate("note", FMEType.String);
		e.setStringValue("only mine");
		e.doAction();
		check("note", "only mine", null, null);
	}

	@Test
	@TestOrder(3)
	@Category(UITest.class)
	public void testRoleDefaultIsGivenToNewInstances() {
		FlexoConceptInstance gr3 = dropShape(90);
		DeclareInstanceOfExistingConcept declare = DeclareInstanceOfExistingConcept.actionType.makeNewAction(gr3, null, editor);
		declare.setConcept(tutuConcept);
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());
		FlexoConceptInstance third = conceptInstance(gr3);
		assertEquals("a default", third.getFlexoPropertyValue("comment"));
		assertEquals(7, (Object) third.getFlexoPropertyValue("count"));
		assertEquals(true, third.getFlexoPropertyValue("active"));
	}
}
