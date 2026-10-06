/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of Free-modelling-editor, a component of the software infrastructure
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.fml.rt.controller.widget.FIBFlexoConceptInstanceSelector;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.FIBComponentResource;
import org.openflexo.foundation.fml.rm.FIBComponentResourceFactory;
import org.openflexo.foundation.fml.rm.FMLFIBComponent;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBCheckBox;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.model.widget.FIBDropDown;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.model.widget.FIBNumber;
import org.openflexo.gina.model.widget.FIBNumber.NumberType;
import org.openflexo.gina.model.widget.FIBTextArea;
import org.openflexo.gina.model.widget.FIBTextField;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;

/**
 * Generates the inspectors of the concepts the free modelling editor creates.
 *
 * <p>
 * The inspector of a concept is an ordinary GINA component, <code>&lt;ConceptName&gt;.inspector</code>, stored in the
 * <code>Xxx.fml/</code> container of the VirtualModel declaring the concept - where {@link FlexoConcept#getInspectorComponentResource()}
 * looks for it, and from where the platform merges it into the inspector of a {@link FlexoConceptInstance}.
 *
 * <p>
 * Generation is STATELESS: the component is rebuilt as a whole from the current structure of the concept, rather than patched. This is
 * what makes adding a property, or giving an inspector to a concept created before inspectors were generated, the same operation.
 *
 * <p>
 * A generated component is never saved here: its resource is created, or its component replaced, and marked modified. It is saved with
 * the rest of the project, when the user saves. Until then it resolves all the same, being registered in the contents of the compilation
 * unit resource.
 *
 * <p>
 * Widgets are built here rather than by the technology adapter controllers: the set of types a free model uses is closed ({@link FMEType}),
 * a controller is not available headless, and the widgets they build for a choice or a reference point into the deprecated
 * FlexoConceptInspector, which no longer exists for these concepts.
 *
 * @author sylvain
 */
public class FMEInspectorGenerator {

	private static final Logger logger = Logger.getLogger(FMEInspectorGenerator.class.getPackage().getName());

	/** Name of the entry showing the type of a graphical representation's concept */
	public static final String TYPE_ENTRY_NAME = "Type";
	/** Name of the entry showing the relationship a connector represents */
	public static final String RELATIONSHIP_ENTRY_NAME = "Relationship";

	private static final String DATA = FIBComponent.DEFAULT_DATA_VARIABLE;

	private FMEInspectorGenerator() {
	}

	/**
	 * (Re)generate the inspector of a concept of a free model - the graphical representation (GR) of a conceptual concept, the GR of a
	 * relationship, or the NoneGR standing for unclassified elements.
	 *
	 * <p>
	 * A GR with a conceptual counterpart - a plain concept or a relationship, found through its {@link FMEFreeModel#CONCEPT_ROLE_NAME}
	 * role - never gets an inspector of its own: showing the same properties twice, once on the GR and once on the conceptual concept
	 * (see {@link #updateConceptualInspector(FlexoConcept)}), serves nothing. It gets
	 * <code>@Inspector(derived=fmeConcept)</code> instead, handing inspection entirely to that conceptual instance - see
	 * {@link FlexoConcept#setDerivedInspector(DataBinding)}.
	 *
	 * <p>
	 * Only the NoneGR - no conceptual counterpart to derive to - still gets a generated inspector of its own, showing "unclassified" as
	 * its type and its own name.
	 *
	 * <p>
	 * Any other concept of a free model - the one holding a connector between two instances, for instance - gets no inspector, as it never
	 * had one.
	 *
	 * @param locales
	 *            where "unclassified" is localized; may be null
	 * @return the resource of the inspector generated for the NoneGR, or null otherwise (nothing generated, or the concept has none)
	 */
	public static FIBComponentResource updateGRInspector(FlexoConcept grConcept, LocalizedDelegate locales) {
		if (!isGRConcept(grConcept)) {
			return null;
		}
		if (conceptOf(grConcept) != null) {
			deriveToConceptualInspector(grConcept);
			return null;
		}
		return update(grConcept, new GRContents(grConcept, locales));
	}

	/**
	 * Hands inspection of supplied GR concept entirely to the conceptual instance its {@link FMEFreeModel#CONCEPT_ROLE_NAME} role
	 * points to: <code>@Inspector(derived=fmeConcept)</code>. Removes whatever inspector FME generated for it before this change - a
	 * stale file left in the container would otherwise be driven by nothing (CORE-F-4), and the annotation makes it entirely
	 * redundant with the conceptual concept's own inspector.
	 */
	private static void deriveToConceptualInspector(FlexoConcept grConcept) {

		FIBComponentResource stale = ownInspectorResource(grConcept);
		if (stale != null) {
			stale.delete();
		}

		// A GR migrated from before this change may still carry the explicit @Inspector("...") FME now writes at
		// creation (or one a reader wrote by hand): clear it first, or setDerivedInspector() would only ADD the
		// 'derived' key next to a 'default' one left naming the file just deleted above - a broken declaration.
		if (grConcept.hasMetaData(FlexoConcept.INSPECTOR_METADATA)) {
			grConcept.removeFromMetaData(grConcept.getMetaData(FlexoConcept.INSPECTOR_METADATA));
		}

		grConcept.setDerivedInspector(new DataBinding<>(FMEFreeModel.conceptRoleName(grConcept)));

		// Tell whoever shows the FML source: the FML editor listens to "FMLPrettyPrint" on the compilation unit, which
		// setIsModified() fires - see CreateFIBComponent#declareComponent (openflexo-ui) for the full explanation.
		if (grConcept.getDeclaringCompilationUnit() != null) {
			grConcept.getDeclaringCompilationUnit().setIsModified();
		}
	}

	/**
	 * Test-only seam: (re)generates a GR concept's own inspector the way {@link #updateGRInspector(FlexoConcept, LocalizedDelegate)}
	 * did before <code>@Inspector(derived=…)</code> existed - simulates a free model saved before this change, so
	 * {@link #generateMissingGRInspectors(VirtualModel, LocalizedDelegate)}'s migration path can be exercised without a save/reload
	 * cycle. Never called from production code: a GR concept with a conceptual counterpart always derives now.
	 */
	static FIBComponentResource generateLegacyGRInspector(FlexoConcept grConcept, LocalizedDelegate locales) {
		return update(grConcept, new GRContents(grConcept, locales));
	}

	/**
	 * (Re)generate the inspector of a concept of the conceptual model: its name and description, and, for a relationship, its source and
	 * destination.
	 *
	 * <p>
	 * An inspector that exists is REPLACED, and with it any edit its author made: to show a property just added, use
	 * {@link #addPropertyToConceptualInspector(FlexoConcept, FlexoProperty)}.
	 *
	 * @return the resource of the inspector, or null when it could not be generated or the concept has none
	 */
	public static FIBComponentResource updateConceptualInspector(FlexoConcept concept) {
		if (!isConceptualConcept(concept)) {
			return null;
		}
		return update(concept, new ConceptualContents(concept));
	}

	/**
	 * Bring every GR concept of supplied free model VirtualModel to its final state - a free model created before inspectors were
	 * generated, or before <code>@Inspector(derived=…)</code> existed. A concept/relationship GR is final once it derives to its
	 * conceptual counterpart; the NoneGR, once it has a generated inspector of its own. Its enums are skipped: nothing inspects them.
	 */
	public static void generateMissingGRInspectors(VirtualModel freeModelVirtualModel, LocalizedDelegate locales) {
		if (freeModelVirtualModel == null) {
			return;
		}
		for (FlexoConcept concept : freeModelVirtualModel.getFlexoConcepts()) {
			if (concept instanceof FlexoEnum || !isGRConcept(concept)) {
				continue;
			}
			boolean upToDate = conceptOf(concept) != null ? concept.hasDerivedInspector() : ownInspectorResource(concept) != null;
			if (!upToDate) {
				updateGRInspector(concept, locales);
			}
		}
	}

	/**
	 * Show a property just added to a conceptual concept in its inspector, WITHOUT rebuilding that inspector.
	 *
	 * <p>
	 * The inspector is an ordinary GINA component the author may have edited since it was generated, in the FIB editor: regenerating it
	 * ({@link #updateConceptualInspector(FlexoConcept)}) would replace the component and erase every such edit. The widget of the property
	 * is added to the existing component instead, in the place a generation would have given it - in front of the description - or at the
	 * end when the author moved or removed the description. A concept with no inspector yet gets one generated, having nothing to preserve.
	 *
	 * <p>
	 * As for a generation, the component is marked modified and never saved here. The resource announces the edit, since it is done in place
	 * and no <code>component</code> change will tell the inspectors built from it.
	 *
	 * @return the resource of the inspector, or null when it could not be updated or the concept has none
	 */
	public static FIBComponentResource addPropertyToConceptualInspector(FlexoConcept concept, FlexoProperty<?> property) {
		if (!isConceptualConcept(concept) || property == null) {
			return null;
		}
		FIBComponentResource resource = ownInspectorResource(concept);
		FIBComponent existing = resource != null ? resource.getComponent() : null;
		if (!(existing instanceof FIBInspector)) {
			return updateConceptualInspector(concept);
		}

		FIBInspector component = (FIBInspector) existing;
		try {
			// Already there - the author may even have added it by hand
			if (component.getSubComponentNamed(lowerCamelCase(property.getName()) + "Label") == null) {
				FIBComponent description = component
						.getSubComponentNamed(lowerCamelCase(FMEConceptualModel.DESCRIPTION_ROLE_NAME) + "Label");
				int index = description != null && description.getParent() == component ? component.getSubComponents().indexOf(description)
						: component.getSubComponents().size();
				FIBModelFactory factory = new FIBModelFactory(null, concept.getServiceManager().getTechnologyAdapterService(),
						FIBInspector.class);
				new Builder(component, factory, index).property(property, DATA);
			}
			component.setModified(true);
			resource.setModified(true);
			resource.notifyComponentEdited();
			return resource;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not add " + property + " to the inspector of " + concept, e);
			return null;
		}
	}

	/**
	 * Same as {@link #generateMissingGRInspectors(VirtualModel, LocalizedDelegate)}, for the conceptual model
	 */
	public static void generateMissingConceptualInspectors(VirtualModel conceptualVirtualModel) {
		for (FlexoConcept concept : conceptsWithoutInspector(conceptualVirtualModel)) {
			updateConceptualInspector(concept);
		}
	}

	private static List<FlexoConcept> conceptsWithoutInspector(VirtualModel virtualModel) {
		List<FlexoConcept> returned = new ArrayList<>();
		if (virtualModel != null) {
			for (FlexoConcept concept : virtualModel.getFlexoConcepts()) {
				if (!(concept instanceof FlexoEnum) && ownInspectorResource(concept) == null) {
					returned.add(concept);
				}
			}
		}
		return returned;
	}

	/**
	 * The inspector the container holds for supplied concept by the naming convention, or null. Not
	 * {@link FlexoConcept#getInspectorComponentFlexoResource()} as it is: that one would answer an inherited inspector too.
	 */
	public static FIBComponentResource ownInspectorResource(FlexoConcept concept) {
		FIBComponentResource returned = concept.getInspectorComponentFlexoResource();
		if (returned == null || returned.getIODelegate() == null || concept.getDeclaringCompilationUnit() == null) {
			return null;
		}
		Resource conventional = concept.getDeclaringCompilationUnit().getContainedArtefact(ownInspectorFileName(concept));
		return conventional != null && conventional.equals(returned.getIODelegate().getSerializationArtefactAsResource()) ? returned : null;
	}

	/**
	 * The file name of the inspector of supplied concept: the one it declares (<code>@Inspector("...")</code>), which a rename of the
	 * concept leaves as it was, otherwise the conventional one
	 */
	private static String ownInspectorFileName(FlexoConcept concept) {
		if (concept.hasMetaData(FlexoConcept.INSPECTOR_METADATA)
				&& concept.getMetaData(FlexoConcept.INSPECTOR_METADATA) instanceof SingleMetaData) {
			String declared = concept.getSingleMetaData(FlexoConcept.INSPECTOR_METADATA, String.class);
			if (declared != null && !declared.isEmpty()) {
				return declared;
			}
		}
		return inspectorFileName(concept);
	}

	/**
	 * Called once supplied conceptual concept has been renamed: the inspector it owns keeps its file (see
	 * {@link FlexoConcept#freezeConventionalUIComponentNames()}) and what its author wrote in it, but its root, which names the tab
	 * showing the concept, follows the concept.
	 */
	public static void conceptRenamed(FlexoConcept concept, String oldConceptName) {
		FIBComponentResource resource = ownInspectorResource(concept);
		FIBComponent root = resource != null ? resource.getComponent() : null;
		if (root != null) {
			if ((lowerCamelCase(oldConceptName) + "Inspector").equals(root.getName())) {
				root.setName(lowerCamelCase(concept.getName()) + "Inspector");
				root.setModified(true);
				resource.setModified(true);
			}
		}
	}

	private static String inspectorFileName(FlexoConcept concept) {
		return concept.getName() + FIBComponentResourceFactory.INSPECTOR_SUFFIX;
	}

	private static FIBComponentResource update(FlexoConcept concept, Contents contents) {

		if (concept == null || concept.getDeclaringCompilationUnit() == null
				|| !(concept.getDeclaringCompilationUnit().getResource() instanceof CompilationUnitResource)) {
			logger.warning("Cannot generate the inspector of " + concept + ": it belongs to no compilation unit resource");
			return null;
		}

		try {
			FIBModelFactory factory = new FIBModelFactory(null, concept.getServiceManager().getTechnologyAdapterService(),
					FIBInspector.class);
			FIBInspector component = buildComponent(concept, contents, factory);

			FIBComponentResource resource = ownInspectorResource(concept);
			if (resource == null) {
				resource = makeResource(concept);
				FMLFIBComponent resourceData = FMLFIBComponent.newInstance(component);
				resourceData.setResource(resource);
				resource.setResourceData(resourceData);

				// An explicit @Inspector("...") from the start, exactly like CreateInspector (openflexo-ui) writes for a
				// hand-created one: relying on the naming convention alone is what CORE-F-4 was about - a later rename of
				// the concept would silently orphan the file, generated one or not.
				// setSingleMetaData() only replaces an EXISTING SingleMetaData - a leftover MultiValuedMetaData under the
				// same key (e.g. an emptied @Inspector(derived=...), mid-migration) must be cleared first, or it would sit
				// alongside the new one rather than being replaced by it.
				if (concept.hasMetaData(FlexoConcept.INSPECTOR_METADATA)
						&& !(concept.getMetaData(FlexoConcept.INSPECTOR_METADATA) instanceof SingleMetaData)) {
					concept.removeFromMetaData(concept.getMetaData(FlexoConcept.INSPECTOR_METADATA));
				}
				concept.setSingleMetaData(FlexoConcept.INSPECTOR_METADATA, inspectorFileName(concept), String.class);
				if (concept.getDeclaringCompilationUnit() != null) {
					concept.getDeclaringCompilationUnit().setIsModified();
				}
			}
			else {
				// Replacing the component - rather than patching it - is what the platform inspector listens to
				resource.getResourceData().setComponent(component);
			}
			component.setModified(true);
			resource.setModified(true);
			return resource;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not generate the inspector of " + concept, e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <I> FIBComponentResource makeResource(FlexoConcept concept) throws FlexoException {

		CompilationUnitResource compilationUnitResource = (CompilationUnitResource) concept.getDeclaringCompilationUnit().getResource();
		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) compilationUnitResource.getResourceCenter();

		// Beside the FML source, in the Xxx.fml/ container. createEntry() writes nothing: the file appears when the resource is saved
		I containerDirectory = resourceCenter.getContainer((I) compilationUnitResource.getIODelegate().getSerializationArtefact());
		I artefact = resourceCenter.createEntry(inspectorFileName(concept), containerDirectory);

		FIBComponentResourceFactory factory = concept.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class).getResourceFactory(FIBComponentResourceFactory.class);
		try {
			// No empty contents: the factory would SAVE them right away
			return factory.makeResource(artefact, resourceCenter, false);
		} catch (Exception e) {
			throw new FlexoException("Could not create " + inspectorFileName(concept) + " in " + containerDirectory, e);
		}
	}

	private static FIBInspector buildComponent(FlexoConcept concept, Contents contents, FIBModelFactory factory) {

		// A plain panel: the platform wraps it into a tab titled after the concept, first of the tabs of the instance inspector
		FIBInspector root = factory.newInstance(FIBInspector.class);
		root.setName(lowerCamelCase(concept.getName()) + "Inspector");
		root.setDataClass(FlexoConceptInstance.class);
		root.setControllerClassName("org.openflexo.inspector.FIBInspectorController");
		root.setLayout(Layout.twocols);

		// Typed by the concept, which is what makes 'data.fmeConcept.name' resolve
		factory.newFIBVariable(root, DATA, concept.getInstanceType());

		contents.append(new Builder(root, factory));

		root.finalizeDeserialization();
		return root;
	}

	/**
	 * What an inspector shows, in order
	 */
	private interface Contents {
		void append(Builder builder);
	}

	private static class GRContents implements Contents {

		private final FlexoConcept grConcept;
		private final LocalizedDelegate locales;

		GRContents(FlexoConcept grConcept, LocalizedDelegate locales) {
			this.grConcept = grConcept;
			this.locales = locales;
		}

		@Override
		public void append(Builder builder) {

			FlexoConcept concept = conceptOf(grConcept);
			// The role of the GR pointing to its conceptual instance has the name chosen when the concept was created
			final String CONCEPT = DATA + "." + FMEFreeModel.conceptRoleName(grConcept);

			if (concept == null) {
				// NoneGR: an unclassified element, carrying its own name
				String unclassified = locales != null ? locales.localizedForKey("unclassified") : "unclassified";
				builder.readOnlyTextField(TYPE_ENTRY_NAME, '"' + unclassified + '"');
				builder.textField(FMEFreeModel.NAME_ROLE_NAME, DATA + "." + FMEFreeModel.NAME_ROLE_NAME);
				return;
			}

			List<String> shown = new ArrayList<>();

			if (isRelationship(concept)) {
				builder.readOnlyTextField(RELATIONSHIP_ENTRY_NAME, CONCEPT + ".render");
				builder.property(concept.getAccessibleProperty(FMEConceptualModel.fromRoleName(concept)), CONCEPT);
				builder.property(concept.getAccessibleProperty(FMEConceptualModel.toRoleName(concept)), CONCEPT);
				shown.add(FMEConceptualModel.fromRoleName(concept));
				shown.add(FMEConceptualModel.toRoleName(concept));
			}
			else {
				// As it has always been: the "Type" entry shows the name of the concept instance, read-only
				String label = FMEConceptualModel.labelPropertyName(concept);
				if (label != null) {
					builder.readOnlyTextField(TYPE_ENTRY_NAME, CONCEPT + "." + label);
					builder.textField(label, CONCEPT + "." + label);
					shown.add(label);
				}
				shown.add(FMEConceptualModel.DESCRIPTION_ROLE_NAME);
			}

			// The properties the user added to the concept, in declaration order
			for (FlexoProperty<?> property : concept.getAccessibleProperties()) {
				if (!shown.contains(property.getName())) {
					builder.property(property, CONCEPT);
				}
			}

			if (!isRelationship(concept) && concept.getAccessibleProperty(FMEConceptualModel.DESCRIPTION_ROLE_NAME) != null) {
				builder.textArea(FMEConceptualModel.DESCRIPTION_ROLE_NAME, CONCEPT + "." + FMEConceptualModel.DESCRIPTION_ROLE_NAME);
			}
		}
	}

	private static class ConceptualContents implements Contents {

		private final FlexoConcept concept;

		ConceptualContents(FlexoConcept concept) {
			this.concept = concept;
		}

		@Override
		public void append(Builder builder) {

			List<String> shown = new ArrayList<>();

			if (isRelationship(concept)) {
				builder.property(concept.getAccessibleProperty(FMEConceptualModel.fromRoleName(concept)), DATA);
				builder.property(concept.getAccessibleProperty(FMEConceptualModel.toRoleName(concept)), DATA);
				shown.add(FMEConceptualModel.fromRoleName(concept));
				shown.add(FMEConceptualModel.toRoleName(concept));
			}
			else {
				String label = FMEConceptualModel.labelPropertyName(concept);
				if (label != null) {
					builder.textField(label, DATA + "." + label);
					shown.add(label);
				}
				shown.add(FMEConceptualModel.DESCRIPTION_ROLE_NAME);
			}

			// The properties the user added to the concept, in declaration order - this concept's own inspector is now the
			// ONLY one a GR deriving to it shows (@Inspector(derived=...)), so nothing may be left out here any more than it
			// would be in GRContents, which this mirrors.
			for (FlexoProperty<?> property : concept.getAccessibleProperties()) {
				if (!shown.contains(property.getName())) {
					builder.property(property, DATA);
				}
			}

			if (!isRelationship(concept) && concept.getAccessibleProperty(FMEConceptualModel.DESCRIPTION_ROLE_NAME) != null) {
				builder.textArea(FMEConceptualModel.DESCRIPTION_ROLE_NAME, DATA + "." + FMEConceptualModel.DESCRIPTION_ROLE_NAME);
			}
		}
	}

	private static boolean isGRConcept(FlexoConcept concept) {
		return concept != null && (conceptOf(concept) != null || FMEFreeModel.NONE_FLEXO_CONCEPT_NAME.equals(concept.getName()));
	}

	private static boolean isConceptualConcept(FlexoConcept concept) {
		return concept != null && !(concept instanceof FlexoEnum)
				&& (FMEConceptualModel.labelPropertyName(concept) != null || isRelationship(concept));
	}

	/**
	 * The conceptual concept a graphical representation stands for, or null for the NoneGR
	 */
	private static FlexoConcept conceptOf(FlexoConcept grConcept) {
		FlexoConceptInstanceRole conceptRole = FMEFreeModel.conceptRole(grConcept);
		return conceptRole != null ? conceptRole.getFlexoConceptType() : null;
	}

	private static boolean isRelationship(FlexoConcept concept) {
		return FMEConceptualModel.isRelationship(concept);
	}

	/**
	 * Name of a widget: GINA binds a component through FML rules once driven by a concept, and under them a capitalized path element is read
	 * as a type name - so every widget is named in lowerCamelCase.
	 */
	private static String lowerCamelCase(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Appends a label on the left column and a widget on the right one - the shape every inspector of the infrastructure has
	 */
	private static class Builder {

		private final FIBInspector root;
		private final FIBModelFactory factory;
		/** Where the next widget goes in an existing component, or -1 to append silently at the end of a component being built */
		private int insertionIndex = -1;

		Builder(FIBInspector root, FIBModelFactory factory) {
			this.root = root;
			this.factory = factory;
		}

		/**
		 * A builder completing a component that already exists - and may be open in the GINA editor: what it adds goes at supplied index and
		 * is notified.
		 */
		Builder(FIBInspector root, FIBModelFactory factory, int insertionIndex) {
			this(root, factory);
			this.insertionIndex = insertionIndex;
		}

		void textField(String entryName, String data) {
			FIBTextField widget = factory.newFIBTextField();
			append(entryName, widget, "TextField", data, true, false);
		}

		void readOnlyTextField(String entryName, String data) {
			FIBTextField widget = factory.newFIBTextField();
			widget.setReadOnly(true);
			append(entryName, widget, "TextField", data, true, false);
		}

		void textArea(String entryName, String data) {
			FIBTextArea widget = factory.newFIBTextArea();
			widget.setValidateOnReturn(true);
			widget.setUseScrollBar(true);
			append(entryName, widget, "TextArea", data, true, true);
		}

		/**
		 * The widget matching the type of supplied property, reached as <code>owner.propertyName</code>
		 */
		void property(FlexoProperty<?> property, String owner) {
			if (property == null) {
				return;
			}
			String data = owner + "." + property.getName();

			if (property instanceof PrimitiveRole) {
				PrimitiveType primitiveType = ((PrimitiveRole<?>) property).getPrimitiveType();
				switch (primitiveType != null ? primitiveType : PrimitiveType.String) {
					case Boolean:
						FIBCheckBox checkBox = factory.newFIBCheckBox();
						append(property.getName(), checkBox, "CheckBox", data, false, false);
						return;
					case Integer:
					case Long:
						append(property.getName(), number(NumberType.IntegerType), "Number", data, false, false);
						return;
					case Float:
					case Double:
						append(property.getName(), number(NumberType.DoubleType), "Number", data, false, false);
						return;
					case Date:
						append(property.getName(), factory.newFIBDate(), "Date", data, false, false);
						return;
					default:
						textField(property.getName(), data);
						return;
				}
			}

			if (property instanceof FlexoConceptInstanceRole) {
				FlexoConcept type = ((FlexoConceptInstanceRole) property).getFlexoConceptType();
				if (type instanceof FlexoEnum) {
					FIBDropDown dropDown = factory.newFIBDropDown();
					// enumValues does not need a value to be read through: the choice is offered while the property is unset
					dropDown.setList(new DataBinding<>(data + ".enumValues"));
					append(property.getName(), dropDown, "DropDown", data, true, false);
					return;
				}
				append(property.getName(), instanceSelector(type, owner), "Selector", data, true, false);
				return;
			}

			logger.warning("No widget generated for " + property + " of type " + property.getType());
		}

		private FIBNumber number(NumberType numberType) {
			FIBNumber number = factory.newFIBNumber();
			number.setNumberType(numberType);
			return number;
		}

		/**
		 * A selector of the instances of supplied concept, living in the same VirtualModel instance as <code>owner</code>. The concept is
		 * named by its URI: a binding cannot denote a concept statically.
		 */
		private FIBCustom instanceSelector(FlexoConcept type, String owner) {
			FIBCustom selector = factory.newFIBCustom();
			selector.setComponentClass(FIBFlexoConceptInstanceSelector.class);
			// The service manager first: resolving the URI of the expected concept goes through it
			selector.addToAssignments(factory.newFIBCustomAssignment(selector, new DataBinding<>("component.serviceManager"),
					new DataBinding<>("controller.flexoController.applicationContext"), true));
			selector.addToAssignments(factory.newFIBCustomAssignment(selector, new DataBinding<>("component.virtualModelInstance"),
					new DataBinding<>(owner + ".container"), true));
			if (type != null && type.getURI() != null) {
				selector.addToAssignments(factory.newFIBCustomAssignment(selector,
						new DataBinding<>("component.expectedFlexoConceptTypeURI"), new DataBinding<>('"' + type.getURI() + '"'), true));
			}
			return selector;
		}

		private void append(String entryName, FIBWidget widget, String widgetSuffix, String data, boolean expandHorizontally,
				boolean expandVertically) {

			FIBLabel label = factory.newFIBLabel(entryName);
			label.setName(lowerCamelCase(entryName) + "Label");
			TwoColsLayoutConstraints labelConstraints = new TwoColsLayoutConstraints(TwoColsLayoutLocation.left, false, false);

			widget.setName(lowerCamelCase(entryName) + widgetSuffix);
			widget.setData(new DataBinding<>(data));
			TwoColsLayoutConstraints widgetConstraints = new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, expandHorizontally,
					expandVertically);

			if (insertionIndex < 0) {
				root.addToSubComponentsNoNotification(label, labelConstraints);
				root.addToSubComponentsNoNotification(widget, widgetConstraints);
			}
			else {
				root.addToSubComponents(label, labelConstraints, insertionIndex++);
				root.addToSubComponents(widget, widgetConstraints, insertionIndex++);
			}
		}
	}
}
