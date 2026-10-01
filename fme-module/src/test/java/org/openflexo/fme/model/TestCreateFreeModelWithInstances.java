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
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.diana.geom.DianaPoint;
import org.openflexo.fme.model.action.CreateFMEDiagramFreeModel;
import org.openflexo.fme.model.action.CreateNewConceptFromNoneConcept;
import org.openflexo.fme.model.action.CreateNewFMEProperty;
import org.openflexo.fme.model.action.CreateNewRelationalConcept;
import org.openflexo.fme.model.action.DeclareInstanceOfExistingConcept;
import org.openflexo.fme.model.action.DropShape;
import org.openflexo.fme.model.action.InstantiateFMEDiagramFreeModel;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.widget.FIBDropDown;
import org.openflexo.gina.model.widget.FIBTextField;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
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

		// The NoneGR got its inspector, in the container of the free model...
		FlexoConcept noneGR = tutu.getFlexoConcept();
		FMEInspectorAssertions.assertInspectorIsValid(noneGR, "typeTextField", "nameTextField");

		// ...and it was NOT saved: it is modified, and waits for the user to save the project
		FIBComponentResource inspector = noneGR.getInspectorComponentFlexoResource();
		assertFalse(((File) inspector.getIODelegate().getSerializationArtefact()).exists());
		assertTrue(project.getServiceManager().getResourceManager().getUnsavedResources().contains(inspector));
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

		tutuConceptGR = tutu.getFlexoConcept();
		// The GR hands inspection entirely to the conceptual concept - it has no inspector of its own to generate
		FMEInspectorAssertions.assertDerivesToConceptualInspector(tutu);
		FMEInspectorAssertions.assertInspectorIsValid(tutuConcept, "nameTextField", "descriptionTextArea");

		project.save();
		project.saveModifiedResources();

		// Saved with the project
		assertTrue(((File) tutuConcept.getInspectorComponentFlexoResource().getIODelegate().getSerializationArtefact()).exists());
	}

	private static FlexoConcept tutuConceptGR;

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

	/**
	 * Adding a property to a concept regenerates the CONCEPTUAL concept's inspector, which then shows a widget of the type of that
	 * property - between the name and the description, where the deprecated inspector entries used to be inserted. The GR's own
	 * inspector is never touched: it has none, deriving entirely to the conceptual concept's (see
	 * {@link #testMakeNewConceptFromTutu()}).
	 */
	@Test
	@TestOrder(8)
	@Category(UITest.class)
	public void testAddPropertiesRegeneratesInspector()
			throws TypeMismatchException, NullReferenceException, ReflectiveOperationException {

		FIBComponentResource inspector = tutuConcept.getInspectorComponentFlexoResource();
		FIBComponent before = inspector.getComponent();

		// The author edits the inspector in the FIB editor, in place: a tooltip on the name, and a widget of their own after the
		// description. Adding properties must complete the component, never rebuild it - that erased these edits.
		FIBContainer beforeContainer = (FIBContainer) before;
		((FIBWidget) beforeContainer.getSubComponentNamed("nameTextField")).setTooltipText("edited by hand");
		FIBTextField reviewer = beforeContainer.getModelFactory().newFIBTextField();
		reviewer.setName("reviewerTextField");
		beforeContainer.addToSubComponents(reviewer,
				new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, true, false));

		addProperty("comment", FMEType.String, null, null);
		addProperty("active", FMEType.Boolean, null, null);
		addProperty("count", FMEType.Integer, null, null);
		addProperty("weight", FMEType.Float, null, null);
		addProperty("Birth date", FMEType.Date, null, null);
		addProperty("color", FMEType.Enumeration, "red, light blue,green", null);
		addProperty("friend", FMEType.Reference, null, tutuConcept);

		// Same resource, SAME component: the properties were added to what the author edited
		assertSame(inspector, tutuConcept.getInspectorComponentFlexoResource());
		assertSame(before, inspector.getComponent());
		assertEquals("edited by hand", ((FIBWidget) beforeContainer.getSubComponentNamed("nameTextField")).getTooltipText());
		assertTrue(project.getServiceManager().getResourceManager().getUnsavedResources().contains(inspector));

		FIBComponent component = FMEInspectorAssertions.assertInspectorIsValid(tutuConcept, "nameTextField", "commentTextField",
				"activeCheckBox", "countNumber", "weightNumber", "birthDateDate", "colorDropDown", "friendSelector",
				"descriptionTextArea", "reviewerTextField");

		// The GR still derives, and still resolves nothing of its own
		FMEInspectorAssertions.assertDerivesToConceptualInspector(tutu);

		// The choice of an enum is offered while the property is still unset
		FIBDropDown colorDropDown = (FIBDropDown) ((FIBContainer) component).getSubComponentNamed("colorDropDown");
		assertEquals("data.color.enumValues", colorDropDown.getList().toString());
		FlexoConceptInstance tutuConceptInstance = tutu.getFlexoPropertyValue(FMEFreeModel.CONCEPT_ROLE_NAME);
		assertNotNull(tutuConceptInstance);
		assertNull(tutuConceptInstance.getFlexoPropertyValue("color"));
		BindingEvaluationContext inspectingTutuConcept = new BindingEvaluationContext() {
			@Override
			public Object getValue(BindingVariable variable) {
				return FIBComponent.DEFAULT_DATA_VARIABLE.equals(variable.getVariableName()) ? tutuConceptInstance : null;
			}

			@Override
			public ExpressionEvaluator getEvaluator() {
				return new FMLExpressionEvaluator(this);
			}
		};
		List<?> colors = (List<?>) colorDropDown.getList().getBindingValue(inspectingTutuConcept);
		assertNotNull(colors);
		assertEquals(3, colors.size());
		assertNotNull(((FlexoEnum) nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Color")).getValue("LIGHT_BLUE"));
	}

	private void addProperty(String name, FMEType type, String enumValues, FlexoConcept referenceType) {
		CreateNewFMEProperty action = CreateNewFMEProperty.actionType.makeNewAction(tutuConceptGR, null, editor);
		action.setPropertyName(name);
		action.setFMEType(type);
		if (enumValues != null) {
			action.setEnumValues(enumValues);
		}
		action.setReferenceType(referenceType);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		// Created under a name FML accepts, whatever was typed
		assertNotNull(tutuConcept.getAccessibleProperty(FMENames.propertyName(name)));
	}

	/**
	 * A relationship, reified as a concept, has an inspector in the conceptual model; its GR derives to it, exactly like a plain
	 * concept's does.
	 */
	@Test
	@TestOrder(9)
	@Category(UITest.class)
	public void testCreateRelationship() {

		CreateNewRelationalConcept action = CreateNewRelationalConcept.actionType.makeNewAction(freeModel, null, editor);
		action.setNewConceptName("Knows");
		action.setNewConceptDescription("A tutu knows another tutu");
		action.setFromConcept(tutuConcept);
		action.setToConcept(tutuConcept);
		action.setFromGRConcept(tutuConceptGR);
		action.setToGRConcept(tutuConceptGR);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());

		FMEInspectorAssertions.assertInspectorIsValid(action.getNewFlexoConcept(), "sourceTutuConceptSelector", "destinationTutuConceptSelector");
		FMEInspectorAssertions.assertDerivesToConceptualInspector(action.getNewGRFlexoConcept());
	}

	/**
	 * A free model created before inspectors were generated gets one (the NoneGR case - none exists here to exercise, covered by
	 * {@link #testCreateInstance()} already having one); one created before <code>@Inspector(derived=…)</code> existed - a GR
	 * concept still carrying its own FME-generated inspector - gets MIGRATED to a derived one, and the stale file is removed
	 * (CORE-F-4: nothing may be left in the container that nothing resolves to).
	 */
	@Test
	@TestOrder(10)
	@Category(UITest.class)
	public void testGenerateMissingInspectors() throws SaveResourceException {

		project.saveModifiedResources();

		// Simulate a free model saved before @Inspector(derived=...) existed: TutuConceptGR has its own generated inspector,
		// not the annotation
		tutuConceptGR.setDerivedInspector(null);
		FIBComponentResource legacy = FMEInspectorGenerator.generateLegacyGRInspector(tutuConceptGR, null);
		assertNotNull(legacy);
		assertFalse(tutuConceptGR.hasDerivedInspector());
		assertSame(legacy, FMEInspectorGenerator.ownInspectorResource(tutuConceptGR));

		freeModel.generateMissingInspectors();

		// Migrated: derives again, and the legacy inspector is gone - not merely unused
		FMEInspectorAssertions.assertDerivesToConceptualInspector(tutu);

		project.saveModifiedResources();
	}

	/**
	 * Reload the project: the inspectors come back from the files the project was saved with
	 */
	@Test
	@TestOrder(11)
	@Category(UITest.class)
	public void testReloadProject() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);
		editor = loadProject(project.getProjectDirectory());
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);
		freeModel = (FMEDiagramFreeModel) nature.getFreeModel("FreeModel");
		assertNotNull(freeModel);

		// The conceptual model parses back, enum included: its values were normalized when typed in lower case
		FlexoConcept color = nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("Color");
		assertTrue(color instanceof FlexoEnum);
		assertEquals(3, ((FlexoEnum) color).getValues().size());

		FlexoConcept reloadedGR = freeModel.getAccessedVirtualModel().getFlexoConcept("TutuConceptGR");
		assertNotNull(reloadedGR);
		FMEInspectorAssertions.assertDerivesToConceptualInspector(reloadedGR);
		FMEInspectorAssertions.assertDerivesToConceptualInspector(freeModel.getAccessedVirtualModel().getFlexoConcept("KnowsGR"));
		FMEInspectorAssertions.assertInspectorIsValid(
				nature.getConceptualModel().getAccessedVirtualModel().getFlexoConcept("TutuConcept"), "nameTextField", "commentTextField",
				"activeCheckBox", "countNumber", "weightNumber", "birthDateDate", "colorDropDown", "friendSelector",
				"descriptionTextArea", "reviewerTextField");
	}
}
