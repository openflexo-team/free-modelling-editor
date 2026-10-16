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

package org.openflexo.fme.model.action;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEInspectorGenerator;
import org.openflexo.fme.model.FMENames;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.toolbox.StringUtils;

/**
 * This action creates a common super concept of several concepts of the FreeModellingEditor, factoring what they share: the properties
 * having the same name and the same type in all of them are moved to the super concept (and the values of the existing instances stay
 * valid, since they are stored by property name).<br>
 * The super concept is inserted in the hierarchy of concepts between the parents the selected concepts have in common (if any) and the
 * selected concepts.
 * 
 * <p>
 * Selected objects are concepts, GR concepts or instances of them: the action applies to the conceptual concepts they stand for. This
 * action works at the conceptual level only: the GR concepts are left as they are, and the super concept has no GR (it is never
 * instantiated by itself).
 * 
 * <p>
 * Nothing is saved: the resources are only marked as modified.
 * 
 * @author sylvain
 * 
 */
public class GeneralizeConcepts extends FMEAction<GeneralizeConcepts, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(GeneralizeConcepts.class.getPackage().getName());

	public static FlexoActionFactory<GeneralizeConcepts, FlexoObject, FlexoObject> actionType = new FlexoActionFactory<GeneralizeConcepts, FlexoObject, FlexoObject>(
			"generalize_concepts", FlexoActionFactory.refactorMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public GeneralizeConcepts makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new GeneralizeConcepts(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return concepts(object, globalSelection).size() > 1;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection) && selectionIssue(concepts(object, globalSelection)) == null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(GeneralizeConcepts.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(GeneralizeConcepts.actionType, FlexoConceptInstance.class);
	}

	/**
	 * A property that can be moved to the super concept: all the selected concepts declare it, with the same type
	 */
	public static class FactoredProperty {

		private final String name;
		private final Type type;
		private final List<FlexoProperty<?>> sources;
		private boolean factored = true;

		FactoredProperty(String name, Type type, List<FlexoProperty<?>> sources) {
			this.name = name;
			this.type = type;
			this.sources = sources;
		}

		public String getName() {
			return name;
		}

		public Type getType() {
			return type;
		}

		public String getTypeAsString() {
			return TypeUtils.simpleRepresentation(type);
		}

		/** The declarations of that property, one per selected concept (in the order of the concepts) */
		public List<FlexoProperty<?>> getSources() {
			return sources;
		}

		/** Whether the property is to be moved to the super concept */
		public boolean isFactored() {
			return factored;
		}

		public void setFactored(boolean factored) {
			this.factored = factored;
		}
	}

	/**
	 * The two ends of the super relationship: the name of the role pointing to each end, which all the relationships to generalize
	 * share, and the concept both ends have in common (the closest one)
	 */
	public static class RelationEnds {

		private final String fromRoleName;
		private final String toRoleName;
		private final FlexoConcept fromType;
		private final FlexoConcept toType;

		RelationEnds(String fromRoleName, FlexoConcept fromType, String toRoleName, FlexoConcept toType) {
			this.fromRoleName = fromRoleName;
			this.fromType = fromType;
			this.toRoleName = toRoleName;
			this.toType = toType;
		}

		public String getFromRoleName() {
			return fromRoleName;
		}

		public String getToRoleName() {
			return toRoleName;
		}

		public FlexoConcept getFromType() {
			return fromType;
		}

		public FlexoConcept getToType() {
			return toType;
		}

		@Override
		public String toString() {
			return fromRoleName + ": " + fromType.getName() + " \u2192 " + toRoleName + ": " + toType.getName();
		}
	}

	private String newConceptName;
	private String newConceptDescription;
	private boolean newConceptAbstract = true;
	private boolean generalizeEnds = true;
	private List<FactoredProperty> proposedProperties;
	private List<String> notFactoredProperties;

	private FlexoConcept newFlexoConcept;

	private GeneralizeConcepts(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	/**
	 * The conceptual concepts supplied selection stands for, without duplicates: a concept for itself, a GR concept or an instance for
	 * the concept it stands for (a relationship, which a connector stands for, as well as a concept). What is not the one of a concept FME
	 * can generalize (the NoneGR, an enum...) is ignored.
	 */
	public static List<FlexoConcept> concepts(FlexoObject focusedObject, List<? extends FlexoObject> globalSelection) {
		Set<FlexoConcept> returned = new LinkedHashSet<>();
		List<FlexoObject> objects = new ArrayList<>();
		if (focusedObject != null) {
			objects.add(focusedObject);
		}
		if (globalSelection != null) {
			objects.addAll(globalSelection);
		}
		for (FlexoObject object : objects) {
			FlexoConcept concept = RenameFMEConcept.conceptToRename(object);
			if (concept != null) {
				returned.add(concept);
			}
		}
		return new ArrayList<>(returned);
	}

	/**
	 * Localization key of what prevents to generalize supplied concepts, or null: they have to belong to the same VirtualModel, and none
	 * may be an ancestor of another one
	 */
	public static String selectionIssue(List<FlexoConcept> concepts) {
		if (concepts.size() < 2) {
			return "select_at_least_two_concepts";
		}
		VirtualModel owner = concepts.get(0).getOwner();
		for (FlexoConcept concept : concepts) {
			if (concept.getOwner() != owner) {
				return "concepts_to_generalize_must_belong_to_the_same_model";
			}
			for (FlexoConcept other : concepts) {
				if (other != concept && concept.isSuperConceptOf(other)) {
					return "a_concept_cannot_be_generalized_with_one_of_its_descendants";
				}
			}
		}
		return null;
	}

	/**
	 * The concepts to generalize
	 */
	public List<FlexoConcept> getConcepts() {
		return concepts(getFocusedObject(), getGlobalSelection());
	}

	/**
	 * The parents all the concepts to generalize have in common: the super concept takes their place
	 */
	public List<FlexoConcept> getCommonParents() {
		List<FlexoConcept> returned = new ArrayList<>();
		List<FlexoConcept> concepts = getConcepts();
		if (concepts.isEmpty()) {
			return returned;
		}
		for (FlexoConcept parent : concepts.get(0).getParentFlexoConcepts()) {
			boolean common = true;
			for (FlexoConcept concept : concepts) {
				if (!concept.getParentFlexoConcepts().contains(parent)) {
					common = false;
				}
			}
			if (common) {
				returned.add(parent);
			}
		}
		return returned;
	}

	/**
	 * The container concept all the concepts to generalize share, or null when they do not
	 */
	public FlexoConcept getCommonContainer() {
		FlexoConcept returned = null;
		boolean first = true;
		for (FlexoConcept concept : getConcepts()) {
			if (first) {
				returned = concept.getContainerFlexoConcept();
				first = false;
			}
			else if (returned != concept.getContainerFlexoConcept()) {
				return null;
			}
		}
		return returned;
	}

	/**
	 * What can be factored: the properties declared, with the same name and the same type, by all the concepts to generalize
	 */
	public List<FactoredProperty> getProposedProperties() {
		if (proposedProperties == null) {
			computeProposal();
		}
		return proposedProperties;
	}

	/**
	 * The properties the concepts have in common by name, which cannot be factored (their types are different, or they are not roles
	 * FME handles)
	 */
	public List<String> getNotFactoredProperties() {
		if (notFactoredProperties == null) {
			computeProposal();
		}
		return notFactoredProperties;
	}

	private void computeProposal() {

		proposedProperties = new ArrayList<>();
		notFactoredProperties = new ArrayList<>();
		List<FlexoConcept> concepts = getConcepts();
		if (concepts.isEmpty()) {
			return;
		}

		Map<String, List<FlexoProperty<?>>> byName = new LinkedHashMap<>();
		for (FlexoProperty<?> property : concepts.get(0).getDeclaredProperties()) {
			byName.put(property.getPropertyName(), new ArrayList<>());
		}
		// The roles pointing to the two ends of a relationship stay where they are: with them, the super concept would be a relationship
		for (FlexoConcept concept : concepts) {
			List<FlexoConceptInstanceRole> ends = FMEConceptualModel.relationEnds(concept);
			if (ends != null) {
				for (FlexoConceptInstanceRole end : ends) {
					byName.remove(end.getPropertyName());
				}
			}
		}
		for (FlexoConcept concept : concepts) {
			for (String name : new ArrayList<>(byName.keySet())) {
				FlexoProperty<?> property = concept.getDeclaredProperty(name);
				if (property == null) {
					byName.remove(name);
				}
				else {
					byName.get(name).add(property);
				}
			}
		}

		for (Map.Entry<String, List<FlexoProperty<?>>> entry : byName.entrySet()) {
			if (isFactorable(entry.getValue())) {
				proposedProperties.add(new FactoredProperty(entry.getKey(), entry.getValue().get(0).getResultingType(), entry.getValue()));
			}
			else {
				notFactoredProperties.add(entry.getKey());
			}
		}
	}

	private static boolean isFactorable(List<FlexoProperty<?>> properties) {
		FlexoProperty<?> reference = properties.get(0);
		if (!(reference instanceof PrimitiveRole) && !(reference instanceof FlexoConceptInstanceRole)) {
			return false;
		}
		for (FlexoProperty<?> property : properties) {
			if (reference instanceof PrimitiveRole != property instanceof PrimitiveRole) {
				return false;
			}
			if (reference instanceof FlexoConceptInstanceRole != property instanceof FlexoConceptInstanceRole) {
				return false;
			}
			if (property.getCardinality() != reference.getCardinality()) {
				return false;
			}
			Type type = property.getResultingType();
			Type referenceType = reference.getResultingType();
			if (type == null || referenceType == null
					|| !(TypeUtils.isTypeAssignableFrom(referenceType, type) && TypeUtils.isTypeAssignableFrom(type, referenceType))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether all the concepts to generalize are relationships: the super concept may then be one as well, having the ends they share
	 */
	public boolean isRelationshipsSelection() {
		List<FlexoConcept> concepts = getConcepts();
		if (concepts.isEmpty()) {
			return false;
		}
		for (FlexoConcept concept : concepts) {
			if (!FMEConceptualModel.isRelationship(concept)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The ends the super concept can have when all the concepts to generalize are relationships: the roles pointing to the source and to
	 * the destination are named the same way in all of them (the roles of the super concept are overridden, by name, by the roles of
	 * those concepts), and the concepts they point to have a concept in common.
	 * 
	 * @return null when the super concept cannot have any end: see {@link #getEndsIssue()}
	 */
	public RelationEnds getCommonEnds() {

		if (!isRelationshipsSelection()) {
			return null;
		}

		String fromRoleName = null;
		String toRoleName = null;
		List<FlexoConcept> fromTypes = new ArrayList<>();
		List<FlexoConcept> toTypes = new ArrayList<>();
		for (FlexoConcept concept : getConcepts()) {
			List<FlexoConceptInstanceRole> ends = FMEConceptualModel.relationEnds(concept);
			FlexoConceptInstanceRole from = ends.get(0);
			FlexoConceptInstanceRole to = ends.get(1);
			if (from.getFlexoConceptType() == null || to.getFlexoConceptType() == null) {
				return null;
			}
			if (fromRoleName == null) {
				fromRoleName = from.getRoleName();
				toRoleName = to.getRoleName();
			}
			else if (!fromRoleName.equals(from.getRoleName()) || !toRoleName.equals(to.getRoleName())) {
				return null;
			}
			fromTypes.add(from.getFlexoConceptType());
			toTypes.add(to.getFlexoConceptType());
		}

		FlexoConcept fromType = commonConcept(fromTypes);
		FlexoConcept toType = commonConcept(toTypes);
		if (fromType == null || toType == null) {
			return null;
		}
		return new RelationEnds(fromRoleName, fromType, toRoleName, toType);
	}

	/**
	 * The closest concept all supplied concepts are, or inherit from; null when they have none in common
	 */
	private static FlexoConcept commonConcept(List<FlexoConcept> concepts) {
		List<FlexoConcept> candidates = new ArrayList<>();
		candidates.add(concepts.get(0));
		candidates.addAll(concepts.get(0).getAllParentFlexoConcepts());
		List<FlexoConcept> common = new ArrayList<>();
		for (FlexoConcept candidate : candidates) {
			boolean isCommon = true;
			for (FlexoConcept concept : concepts) {
				if (candidate != concept && !candidate.isSuperConceptOf(concept)) {
					isCommon = false;
				}
			}
			if (isCommon && !common.contains(candidate)) {
				common.add(candidate);
			}
		}
		// The closest: the one no other common concept inherits from
		for (FlexoConcept candidate : common) {
			boolean closest = true;
			for (FlexoConcept other : common) {
				if (other != candidate && candidate.isSuperConceptOf(other)) {
					closest = false;
				}
			}
			if (closest) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Localization key telling why the super concept has no end although all the concepts to generalize are relationships, or null
	 */
	public String getEndsIssue() {
		if (!isRelationshipsSelection() || getCommonEnds() != null) {
			return null;
		}
		String fromRoleName = null;
		String toRoleName = null;
		for (FlexoConcept concept : getConcepts()) {
			List<FlexoConceptInstanceRole> ends = FMEConceptualModel.relationEnds(concept);
			if (fromRoleName == null) {
				fromRoleName = ends.get(0).getRoleName();
				toRoleName = ends.get(1).getRoleName();
			}
			else if (!fromRoleName.equals(ends.get(0).getRoleName()) || !toRoleName.equals(ends.get(1).getRoleName())) {
				return "relationship_ends_have_different_role_names";
			}
		}
		return "relationship_ends_have_no_common_type";
	}

	/**
	 * Whether the super concept gets the ends the concepts to generalize have in common (when they have some)
	 */
	public boolean isGeneralizeEnds() {
		return generalizeEnds;
	}

	public void setGeneralizeEnds(boolean generalizeEnds) {
		if (generalizeEnds != this.generalizeEnds) {
			this.generalizeEnds = generalizeEnds;
			getPropertyChangeSupport().firePropertyChange("generalizeEnds", !generalizeEnds, generalizeEnds);
		}
	}

	/**
	 * The ends given to the super concept, or null when it has none
	 */
	public RelationEnds getGeneralizedEnds() {
		return isGeneralizeEnds() ? getCommonEnds() : null;
	}

	public String getNewConceptName() {
		return newConceptName;
	}

	public void setNewConceptName(String newConceptName) {
		if ((newConceptName == null && this.newConceptName != null)
				|| (newConceptName != null && !newConceptName.equals(this.newConceptName))) {
			String oldValue = this.newConceptName;
			this.newConceptName = newConceptName;
			getPropertyChangeSupport().firePropertyChange("newConceptName", oldValue, newConceptName);
		}
	}

	public String getNewConceptDescription() {
		return newConceptDescription;
	}

	public void setNewConceptDescription(String newConceptDescription) {
		if ((newConceptDescription == null && this.newConceptDescription != null)
				|| (newConceptDescription != null && !newConceptDescription.equals(this.newConceptDescription))) {
			String oldValue = this.newConceptDescription;
			this.newConceptDescription = newConceptDescription;
			getPropertyChangeSupport().firePropertyChange("newConceptDescription", oldValue, newConceptDescription);
		}
	}

	/**
	 * Whether the super concept is abstract: it is then never instantiated by itself, and has neither creation scheme nor deletion
	 * scheme. Otherwise it is a concept as the others, with its own instances.
	 */
	public boolean isNewConceptAbstract() {
		return newConceptAbstract;
	}

	public void setNewConceptAbstract(boolean newConceptAbstract) {
		if (newConceptAbstract != this.newConceptAbstract) {
			this.newConceptAbstract = newConceptAbstract;
			getPropertyChangeSupport().firePropertyChange("newConceptAbstract", !newConceptAbstract, newConceptAbstract);
		}
	}

	/**
	 * The name of the property labelling the instances of the super concept when it is not abstract: <code>name</code> when it is moved to
	 * the super concept, otherwise the first String property moved to it (null when there is none)
	 */
	public String getLabelPropertyName() {
		String returned = null;
		for (FactoredProperty property : getProposedProperties()) {
			if (property.isFactored() && String.class.equals(property.getType())) {
				if (property.getName().equals(FMEFreeModel.NAME_ROLE_NAME)) {
					return property.getName();
				}
				if (returned == null) {
					returned = property.getName();
				}
			}
		}
		return returned;
	}

	/**
	 * Localization key of what, in the choice of the properties to move, prevents this action to run, or null: a concrete super concept
	 * labels its instances with a String property it declares
	 */
	public String getPropertiesIssue() {
		if (!isNewConceptAbstract() && getGeneralizedEnds() != null) {
			// Such a concept could not be drawn, and has no creation scheme: it is only a relationship to inherit from
			return "a_relationship_with_ends_must_be_abstract";
		}
		if (!isNewConceptAbstract() && getLabelPropertyName() == null) {
			return "a_concept_that_is_not_abstract_needs_a_string_property";
		}
		return null;
	}

	/**
	 * Localization key of what prevents this action to run, or null
	 */
	public String getIssue() {

		String selectionIssue = selectionIssue(getConcepts());
		if (selectionIssue != null) {
			return selectionIssue;
		}
		if (StringUtils.isEmpty(getNewConceptName())) {
			return "no_concept_name_defined";
		}
		if (!FMENames.isValidConceptName(getNewConceptName())) {
			return "invalid_concept_name";
		}
		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		for (FlexoConcept concept : getConcepts()) {
			if (concept.getOwner().getFlexoConcept(getNewConceptName()) != null) {
				return "a_concept_with_that_name_already_exists";
			}
		}
		if (nature != null) {
			for (FMEFreeModel freeModel : nature.getFreeModels()) {
				if (freeModel.isConceptNameUsed(getNewConceptName())) {
					return "a_concept_with_that_name_already_exists";
				}
			}
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return getIssue() == null && getPropertiesIssue() == null;
	}

	/**
	 * The super concept created by this action
	 */
	public FlexoConcept getNewFlexoConcept() {
		return newFlexoConcept;
	}

	private void createEnd(String roleName, FlexoConcept type) throws FlexoException {
		CreateFlexoConceptInstanceRole createRole = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(newFlexoConcept, null,
				this);
		createRole.setRoleName(roleName);
		createRole.setFlexoConceptInstanceType(type);
		createRole.setVirtualModelInstance(new DataBinding<>("container"));
		createRole.doAction();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		List<FlexoConcept> concepts = getConcepts();
		List<FlexoConcept> commonParents = getCommonParents();
		FlexoConcept container = getCommonContainer();
		List<FactoredProperty> factored = new ArrayList<>();
		for (FactoredProperty property : getProposedProperties()) {
			if (property.isFactored()) {
				factored.add(property);
			}
		}
		VirtualModel virtualModel = concepts.get(0).getOwner();

		// The super concept: no creation scheme nor deletion scheme, since it is never instantiated by itself
		CreateFlexoConcept createSuper = CreateFlexoConcept.actionType.makeNewEmbeddedAction(virtualModel, null, this);
		createSuper.setNewFlexoConceptName(getNewConceptName());
		createSuper.setContainerFlexoConcept(container);
		// Its schemes, if it is not abstract, are made once it has the property labelling its instances
		createSuper.setDefineDefaultDeletionScheme(false);
		createSuper.doAction();
		newFlexoConcept = createSuper.getNewFlexoConcept();
		newFlexoConcept.setAbstract(isNewConceptAbstract());
		if (StringUtils.isNotEmpty(getNewConceptDescription())) {
			newFlexoConcept.setDescription(getNewConceptDescription());
		}

		// The ends the relationships share: roles of the super concept, which those of the relationships override with their own types
		RelationEnds ends = getGeneralizedEnds();
		if (ends != null) {
			createEnd(ends.getFromRoleName(), ends.getFromType());
			createEnd(ends.getToRoleName(), ends.getToType());
		}

		// Inserted between the common parents and the concepts
		for (FlexoConcept parent : commonParents) {
			newFlexoConcept.addToParentFlexoConcepts(parent);
		}

		// The properties go up: copied to the super concept, removed from the concepts
		for (FactoredProperty property : factored) {
			FlexoProperty<?> copy = (FlexoProperty<?>) property.getSources().get(0).cloneObject();
			newFlexoConcept.addToFlexoProperties(copy);
		}

		for (FlexoConcept concept : concepts) {
			for (FlexoConcept parent : commonParents) {
				concept.removeFromParentFlexoConcepts(parent);
			}
			concept.addToParentFlexoConcepts(newFlexoConcept);
		}

		for (FactoredProperty property : factored) {
			for (FlexoProperty<?> source : property.getSources()) {
				source.getFlexoConcept().removeFromFlexoProperties(source);
			}
		}

		if (!isNewConceptAbstract()) {
			// Instantiable as any concept: creation scheme, deletion scheme and renderer
			getFreeModellingProjectNature().getConceptualModel().makeInstantiable(newFlexoConcept, getLabelPropertyName(), getEditor(), this);
		}

		FMEInspectorGenerator.updateConceptualInspector(newFlexoConcept);
		for (FlexoConcept concept : concepts) {
			FMEInspectorGenerator.updateConceptualInspector(concept);
		}

		// Marked, never saved
		if (virtualModel.getDeclaringCompilationUnit() != null) {
			virtualModel.getDeclaringCompilationUnit().setIsModified();
		}
		logger.info("Generalized " + concepts + " as " + newFlexoConcept);
	}

}
