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
package org.openflexo.fme.pptx.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.pptx.PowerpointFMEExtension;
import org.openflexo.fme.pptx.model.action.CreateFMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.action.CreateNewConceptFromShape;
import org.openflexo.fme.pptx.model.action.DeclareShapeAsInstanceOfExistingConcept;
import org.openflexo.fme.pptx.model.action.DisconnectShapeFromConcept;
import org.openflexo.fme.pptx.model.action.InstantiateFMEPowerpointFreeModel;
import org.openflexo.fme.pptx.view.ConceptDecorationProvider;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;
import org.openflexo.technologyadapter.powerpoint.view.PowerpointElementDecorationProvider.Decoration;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * A PowerPoint free model annotating a sample deck of pptx-ta-test-rc: shapes declared as instances of new or existing concepts, then
 * disconnected, the project reloaded; the deck is never modified (FME-F-2)
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestPowerpointFreeModel extends OpenflexoProjectAtRunTimeTestCase {

	private static final String DECK = "CycloCity-Company-Presentation";

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static FreeModellingProjectNature nature;
	private static FMEPowerpointFreeModel freeModel;
	private static FMEPowerpointFreeModelInstance freeModelInstance;
	private static PowerpointSlideshowResource deckResource;
	private static byte[] deckDigest;

	private static FlexoConcept company;
	private static PowerpointShape first;
	private static PowerpointShape second;
	private static PowerpointShape third;
	private static String firstName;

	private static byte[] digest(PowerpointSlideshowResource resource) throws Exception {
		Object artefact = resource.getIODelegate().getSerializationArtefact();
		if (artefact instanceof File) {
			return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(((File) artefact).toPath()));
		}
		// In a jar: it cannot be modified anyway
		return null;
	}

	private static List<FlexoConceptInstance> sampleInstances() {
		return nature.getSampleData().getAccessedVirtualModelInstance().getFlexoConceptInstances();
	}

	@Test
	@TestOrder(1)
	public void testCreateAndInstantiateFreeModel() throws Exception {

		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);

		editor = createStandaloneProject("TestPowerpointFreeModel", FreeModellingProjectNature.class);
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);

		// Annotating a first slideshow creates the PowerPoint free model of the project: the user does not create it
		assertNull(FMEPowerpointFreeModel.getFreeModel(nature));
		assertTrue(CreateFMEPowerpointFreeModel.actionType.isEnabled(nature, null));
		InstantiateFMEPowerpointFreeModel instantiate = InstantiateFMEPowerpointFreeModel.actionType.makeNewAction(nature, null, editor);
		assertFalse(instantiate.isValid());
		for (PowerpointSlideshowResource resource : instantiate.getAvailableSlideshows()) {
			if (resource.getName().startsWith(DECK)) {
				deckResource = resource;
			}
		}
		assertNotNull(DECK + " not found", deckResource);
		deckDigest = digest(deckResource);
		instantiate.setSlideshowResource(deckResource);
		assertTrue(instantiate.isValid());
		// The annotation is named after the slideshow
		assertEquals(DECK, instantiate.getFreeModelInstanceName());
		instantiate.doAction();
		assertTrue(instantiate.hasActionExecutionSucceeded());
		freeModelInstance = instantiate.getNewFreeModelInstance();
		assertNotNull(freeModelInstance);
		assertEquals(DECK, freeModelInstance.getName());
		freeModel = freeModelInstance.getFreeModel();
		assertSame(freeModel, FMEPowerpointFreeModel.getFreeModel(nature));
		assertEquals(FMEPowerpointFreeModel.DEFAULT_NAME, freeModel.getName());
		assertNotNull(freeModel.getPowerpointModelSlot());

		// One PowerPoint free model per project
		assertFalse(CreateFMEPowerpointFreeModel.actionType.isEnabled(nature, null));

		// The instance annotates the deck where it is
		PowerpointSlideshow slideshow = freeModelInstance.getSlideshow();
		assertSame(deckResource.getResourceData(), slideshow);
		assertSame(freeModelInstance, nature.getFreeModelInstanceContaining(slideshow.getPowerpointSlides().get(0)));

		// Three shapes with a text, on the first slides
		List<PowerpointShape> shapes = new ArrayList<>();
		for (PowerpointSlide slide : slideshow.getPowerpointSlides()) {
			for (PowerpointShape shape : slide.getPowerpointShapes()) {
				if (shape.getText() != null && !shape.getText().trim().isEmpty() && shapes.size() < 3) {
					shapes.add(shape);
				}
			}
		}
		assertEquals(3, shapes.size());
		first = shapes.get(0);
		second = shapes.get(1);
		third = shapes.get(2);
		for (PowerpointShape shape : shapes) {
			assertTrue(freeModelInstance.contains(shape));
			assertNull(freeModelInstance.getGRInstance(shape));
			assertTrue(PowerpointAnnotations.isAnnotatable(shape));
		}
	}

	/** A slideshow is annotated once: annotating it again opens its annotation */
	@Test
	@TestOrder(2)
	public void testAnnotateSameSlideshowAgain() throws Exception {
		InstantiateFMEPowerpointFreeModel again = InstantiateFMEPowerpointFreeModel.actionType.makeNewAction(nature, null, editor);
		again.setSlideshowResource(deckResource);
		assertSame(freeModelInstance, again.getExistingAnnotation());
		assertTrue(again.isValid());
		again.doAction();
		assertTrue(again.hasActionExecutionSucceeded());
		assertSame(freeModelInstance, again.getNewFreeModelInstance());
		assertEquals(1, freeModel.getFreeModelInstances().size());
		assertEquals(1, nature.getFreeModels().size());
	}

	/** A new concept emerges from a shape: the instance is named after its text */
	@Test
	@TestOrder(3)
	public void testCreateNewConceptFromShape() throws Exception {

		assertTrue(CreateNewConceptFromShape.actionType.isEnabled(first, null));
		CreateNewConceptFromShape create = CreateNewConceptFromShape.actionType.makeNewAction(first, null, editor);
		create.setNewConceptName("Company");
		assertTrue(create.isValid());
		create.doAction();
		assertTrue(create.hasActionExecutionSucceeded());
		company = create.getNewFlexoConcept();

		FlexoConceptInstance grInstance = freeModelInstance.getGRInstance(first);
		assertNotNull(grInstance);
		assertSame(create.getNewGRFlexoConcept(), grInstance.getFlexoConcept());
		assertSame(first, freeModelInstance.getRepresentationElement(grInstance));
		FlexoConceptInstance instance = FMEFreeModel.representedInstance(grInstance);
		assertSame(company, instance.getFlexoConcept());
		firstName = PowerpointAnnotations.defaultInstanceName(first);
		assertEquals(firstName, instance.getFlexoPropertyValue("name"));
		assertTrue(firstName.length() <= PowerpointAnnotations.MAX_NAME_LENGTH);

		// One annotation per shape
		assertFalse(PowerpointAnnotations.isAnnotatable(first));
		assertFalse(CreateNewConceptFromShape.actionType.isEnabled(first, null));
		assertFalse(DeclareShapeAsInstanceOfExistingConcept.actionType.isEnabled(first, null));
		assertTrue(DisconnectShapeFromConcept.actionType.isEnabled(first, null));
	}

	/** Shapes declared as a new instance of the concept, and as the instance another shape already stands for */
	@Test
	@TestOrder(4)
	public void testDeclareShapes() throws Exception {

		DeclareShapeAsInstanceOfExistingConcept declare = DeclareShapeAsInstanceOfExistingConcept.actionType.makeNewAction(second, null,
				editor);
		declare.setConcept(company);
		assertEquals(PowerpointAnnotations.defaultInstanceName(second), declare.getNewInstanceName());
		declare.setNewInstanceName("Partner");
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());
		assertEquals("Partner", FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(second)).getFlexoPropertyValue("name"));
		assertEquals(2, sampleInstances().size());

		FlexoConceptInstance firstInstance = FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(first));
		DeclareShapeAsInstanceOfExistingConcept declareExisting = DeclareShapeAsInstanceOfExistingConcept.actionType.makeNewAction(third,
				null, editor);
		declareExisting.setConcept(company);
		declareExisting.setCreateNewInstance(false);
		declareExisting.setExistingInstance(firstInstance);
		declareExisting.doAction();
		assertTrue(declareExisting.hasActionExecutionSucceeded());
		assertSame(firstInstance, FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(third)));
		assertEquals(2, sampleInstances().size());
		assertEquals(2, nature.getRepresentations(firstInstance).size());
	}

	/**
	 * The overlay highlights the annotated shapes, lightly, and strongly those standing for the selection; nothing is written or bordered
	 * over them. The inspector shows their instance
	 */
	@Test
	@TestOrder(5)
	public void testOverlayAndInspectedObject() throws Exception {

		ConceptDecorationProvider decorations = new ConceptDecorationProvider(freeModelInstance);
		assertSame(company, decorations.getConcept(first));
		assertLight(decorations.getDecoration(first));
		assertLight(decorations.getDecoration(second));
		assertLight(decorations.getDecoration(third));

		// The shape alone
		decorations.setSelection(Collections.singletonList(first));
		assertStrong(decorations.getDecoration(first));
		assertLight(decorations.getDecoration(third));

		// The instance of the concept: every shape standing for it
		FlexoConceptInstance firstInstance = FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(first));
		decorations.setSelection(Collections.singletonList(firstInstance));
		assertStrong(decorations.getDecoration(first));
		assertStrong(decorations.getDecoration(third));
		assertLight(decorations.getDecoration(second));

		// The instance of the GR concept
		decorations.setSelection(Collections.singletonList(freeModelInstance.getGRInstance(second)));
		assertStrong(decorations.getDecoration(second));
		assertLight(decorations.getDecoration(first));

		decorations.setSelection(null);
		assertLight(decorations.getDecoration(second));

		// The shapes an instance selected in a browser selects in the slides
		assertEquals(new HashSet<>(Arrays.asList(first, third)), new HashSet<>(decorations.getAnnotatedElements(firstInstance)));
		assertEquals(Arrays.asList(second), decorations.getAnnotatedElements(freeModelInstance.getGRInstance(second)));
		assertTrue(decorations.getAnnotatedElements(first).isEmpty());

		PowerpointShape plain = null;
		for (PowerpointShape shape : first.getPowerpointSlide().getPowerpointShapes()) {
			if (freeModelInstance.getGRInstance(shape) == null) {
				plain = shape;
			}
		}
		assertNotNull(plain);
		assertNull(decorations.getDecoration(plain));

		PowerpointFMEExtension extension = new PowerpointFMEExtension();
		assertSame(freeModelInstance.getGRInstance(first), extension.getInspectedObject(first));
		// A shape that is not annotated shows no instance (its PowerPoint inspector is in PowerpointShapeInspectors)
		assertNull(extension.getInspectedObject(plain));
		// Nor does a slide
		assertNull(extension.getInspectedObject(first.getPowerpointSlide()));

		// The PowerPoint inspector: the PowerPoint object selected, annotated or not, or the first shape of the instance selected
		assertSame(plain, decorations.getInspectedPowerpointObject(Collections.singletonList(plain)));
		assertSame(first, decorations.getInspectedPowerpointObject(Collections.singletonList(first)));
		assertSame(first.getPowerpointSlide(), decorations.getInspectedPowerpointObject(Collections.singletonList(first.getPowerpointSlide())));
		assertSame(decorations.getAnnotatedElements(firstInstance).get(0),
				decorations.getInspectedPowerpointObject(Collections.singletonList(firstInstance)));
		assertSame(second, decorations.getInspectedPowerpointObject(Collections.singletonList(freeModelInstance.getGRInstance(second))));
		assertNull(decorations.getInspectedPowerpointObject(Arrays.asList(first, second)));
		assertNull(decorations.getInspectedPowerpointObject(null));
	}

	private static void assertLight(Decoration decoration) {
		assertNotNull(decoration);
		assertEquals(ConceptDecorationProvider.HIGHLIGHT, decoration.getFillTint());
		assertNull(decoration.getBorderColor());
		assertNull(decoration.getBadge());
	}

	private static void assertStrong(Decoration decoration) {
		assertNotNull(decoration);
		assertEquals(ConceptDecorationProvider.SELECTED_HIGHLIGHT, decoration.getFillTint());
		assertNull(decoration.getBorderColor());
		assertNull(decoration.getBadge());
	}

	/** The annotation goes, the shape stays; the conceptual instance goes with its last representation */
	@Test
	@TestOrder(6)
	public void testDisconnectShapes() throws Exception {

		FlexoConceptInstance firstInstance = FMEFreeModel.representedInstance(freeModelInstance.getGRInstance(first));
		PowerpointSlide slide = third.getPowerpointSlide();

		DisconnectShapeFromConcept disconnectThird = DisconnectShapeFromConcept.actionType.makeNewAction(third, null, editor);
		assertFalse(disconnectThird.willDeleteConceptInstance());
		disconnectThird.doAction();
		assertTrue(disconnectThird.hasActionExecutionSucceeded());
		assertNull(freeModelInstance.getGRInstance(third));
		assertTrue(PowerpointAnnotations.isAnnotatable(third));
		assertFalse(firstInstance.isDeleted());
		assertEquals(2, sampleInstances().size());
		assertTrue(slide.getPowerpointShapes().contains(third));
		assertFalse(third.isDeleted());

		DisconnectShapeFromConcept disconnectSecond = DisconnectShapeFromConcept.actionType.makeNewAction(second, null, editor);
		assertTrue(disconnectSecond.willDeleteConceptInstance());
		disconnectSecond.doAction();
		assertTrue(disconnectSecond.hasActionExecutionSucceeded());
		assertTrue(disconnectSecond.isConceptInstanceDeleted());
		assertEquals(1, sampleInstances().size());
		assertFalse(second.isDeleted());

		// Annotate the third shape again, for the reload
		DeclareShapeAsInstanceOfExistingConcept declare = DeclareShapeAsInstanceOfExistingConcept.actionType.makeNewAction(third, null,
				editor);
		declare.setConcept(company);
		declare.setNewInstanceName("Supplier");
		declare.doAction();
		assertTrue(declare.hasActionExecutionSucceeded());
	}

	/** Saved and reloaded: the annotations point to the same shapes, and the deck was never modified */
	@Test
	@TestOrder(7)
	public void testSaveAndReload() throws Exception {

		String firstURI = first.getUri();
		String thirdURI = third.getUri();
		assertFalse(deckResource.isModified());

		project.save();
		project.saveModifiedResources();

		assertFalse(deckResource.isModified());
		byte[] after = digest(deckResource);
		assertTrue("The deck was modified", deckDigest == null || Arrays.equals(deckDigest, after));

		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);
		editor = loadProject(project.getProjectDirectory());
		project = (FlexoProject<File>) editor.getProject();
		nature = project.getNature(FreeModellingProjectNature.class);
		freeModel = (FMEPowerpointFreeModel) FMEPowerpointFreeModel.getFreeModel(nature);
		assertNotNull(freeModel);
		freeModelInstance = (FMEPowerpointFreeModelInstance) freeModel.getFreeModelInstance(DECK);
		assertNotNull(freeModelInstance);
		assertNotNull(freeModelInstance.getSlideshow());

		FlexoConcept grConcept = freeModel.getAccessedVirtualModel().getFlexoConcept("CompanyGR");
		assertNotNull(grConcept);
		List<String> annotated = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (FlexoConceptInstance grInstance : freeModelInstance.getInstances(grConcept)) {
			PowerpointShape shape = (PowerpointShape) freeModelInstance.getRepresentationElement(grInstance);
			assertNotNull(shape);
			assertSame(freeModelInstance.getSlideshow(), shape.getResourceData());
			annotated.add(shape.getUri());
			names.add((String) FMEFreeModel.representedInstance(grInstance).getFlexoPropertyValue("name"));
		}
		assertEquals(2, annotated.size());
		assertTrue(annotated.contains(firstURI));
		assertTrue(annotated.contains(thirdURI));
		assertTrue(names.contains(firstName));
		assertTrue(names.contains("Supplier"));
	}
}
