/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.action.CreateEditionAction;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.action.CreateInspectorEntry;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.nature.VirtualModelBasedNatureObject;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.DeserializationFinalizer;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;

/**
 * Represents a {@link FMEFreeModel} in the FreeModellingEditor<br>
 * This is an abstract concept.
 * 
 * The base of a {@link FMEFreeModel} is a {@link VirtualModel} which aims to federate the {@link FMEConceptualModel} with a given
 * representation to elicitate new or existing concepts.
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FMEDiagramFreeModel.class) })
public interface FMEFreeModel extends VirtualModelBasedNatureObject<FreeModellingProjectNature> {

	public static final String NONE_FLEXO_CONCEPT_NAME = "NoneGR";
	public static final String CONCEPT_ROLE_NAME = "fmeConcept";
	public static final String NAME_ROLE_NAME = "name";
	public static final String SAMPLE_DATA_MODEL_SLOT_NAME = "sampleData";

	@PropertyIdentifier(type = FMEFreeModelInstance.class, cardinality = Cardinality.LIST)
	public static final String FREE_MODELS_INSTANCES = "freeModelInstances";

	@PropertyIdentifier(type = FMEConceptualModel.class)
	public static final String CONCEPTUAL_MODEL = "conceptualModel";
	@PropertyIdentifier(type = FMESampleData.class)
	public static final String SAMPLE_DATA = "sampleData";

	public String getName();

	public String getDescription();

	public void setDescription(String description);

	public FMLRTVirtualModelInstanceModelSlot getSampleDataModelSlot();

	/**
	 * Return conceptual model addressed by this {@link FMEFreeModel}.<br>
	 * If none specific supplied, return {@link FMEConceptualModel} of the {@link FreeModellingProjectNature}
	 * 
	 * @return
	 */
	@Getter(value = CONCEPTUAL_MODEL, inverse = FMEConceptualModel.OWNER_KEY)
	@XMLElement
	public FMEConceptualModel getConceptualModel();

	/**
	 * Sets conceptual model addressed by this {@link FMEFreeModel}.<br>
	 * 
	 * @param conceptualModel
	 */
	@Setter(CONCEPTUAL_MODEL)
	public void setConceptualModel(FMEConceptualModel conceptualModel);

	/**
	 * Return sample data addressed by this {@link FMEFreeModel}.<br>
	 * If none specific supplied, return {@link FMESampleData} of the {@link FreeModellingProjectNature}
	 * 
	 * @return
	 */
	@Getter(value = SAMPLE_DATA, inverse = FMESampleData.OWNER_KEY)
	@XMLElement
	public FMESampleData getSampleData();

	@Setter(SAMPLE_DATA)
	public void setSampleData(FMESampleData sampleData);

	@Getter(value = FREE_MODELS_INSTANCES, cardinality = Cardinality.LIST, inverse = FMEFreeModelInstance.FREE_MODEL)
	@XMLElement
	@Embedded
	public List<FMEFreeModelInstance> getFreeModelInstances();

	@Setter(value = FREE_MODELS_INSTANCES)
	public void setFreeModelInstances(List<FMEFreeModelInstance> freeModelInstances);

	@Adder(FREE_MODELS_INSTANCES)
	public void addToFreeModelInstances(FMEFreeModelInstance freeModelInstance);

	@Remover(value = FREE_MODELS_INSTANCES)
	public void removeFromFreeModelInstances(FMEFreeModelInstance freeModelInstance);

	public FMEFreeModelInstance getFreeModelInstance(String freeModelInstanceName);

	public FlexoConcept getNoneFlexoConcept(FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction);

	/**
	 * Return (creates when non-existant) a FlexoConcept (in the context of FreeModellingEditor)
	 * 
	 * @param conceptName
	 * @param editor
	 * @param ownerAction
	 * @return
	 * @throws FlexoException
	 */
	public FlexoConcept getGRFlexoConcept(FlexoConcept concept, FlexoConcept containerConceptGR, FlexoEditor editor,
			FlexoAction<?, ?, ?> ownerAction, boolean createWhenNotExistant);

	/**
	 * Return (creates when non-existant) a FlexoConcept (in the context of FreeModellingEditor) Created {@link FlexoConcept} will be
	 * designed as a concept linking two other concepts
	 * 
	 * @param conceptName
	 *            name of concept beeing created
	 * @param fromConcept
	 * @param toConcept
	 * @param editor
	 * @param ownerAction
	 * @return
	 * @throws FlexoException
	 */
	public FlexoConcept getGRRelationalFlexoConcept(FlexoConcept concept, FlexoConcept fromConceptGR, FlexoConcept toConceptGR,
			FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction, boolean createWhenNotExistant);

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public abstract class FMEFreeModelImpl extends VirtualModelBasedNatureObjectImpl<FreeModellingProjectNature> implements FMEFreeModel {

		private static final Logger logger = FlexoLogger.getLogger(FMEFreeModel.class.getPackage().getName());

		@Override
		public String getName() {
			if (getAccessedVirtualModel() != null) {
				return getAccessedVirtualModel().getName();
			}
			return null;
		}

		@Override
		public String getDescription() {
			if (getAccessedVirtualModel() != null) {
				return getAccessedVirtualModel().getDescription();
			}
			return null;
		}

		@Override
		public void setDescription(String description) {
			if (getAccessedVirtualModel() != null) {
				getAccessedVirtualModel().setDescription(description);
			}
		}

		@Override
		public FMLRTVirtualModelInstanceModelSlot getSampleDataModelSlot() {
			if (getAccessedVirtualModel() != null
					&& getAccessedVirtualModel().getModelSlots(FMLRTVirtualModelInstanceModelSlot.class).size() > 0) {
				return getAccessedVirtualModel().getModelSlots(FMLRTVirtualModelInstanceModelSlot.class).get(0);
			}
			return null;
		}

		@Override
		public FlexoConcept getNoneFlexoConcept(FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction) {
			return getGRFlexoConcept(null, null, editor, ownerAction, true);
		}

		/**
		 * Return (creates when non-existant) a FlexoConcept (in the context of FreeModellingEditor)
		 * 
		 * @param conceptName
		 * @param editor
		 * @param ownerAction
		 * @return
		 * @throws FlexoException
		 */
		@Override
		public FlexoConcept getGRFlexoConcept(FlexoConcept concept, FlexoConcept containerConceptGR, FlexoEditor editor,
				FlexoAction<?, ?, ?> ownerAction, boolean createWhenNotExistant) {

			FlexoConcept returned = getAccessedVirtualModel()
					.getFlexoConcept(concept != null ? concept.getName() + "GR" : NONE_FLEXO_CONCEPT_NAME);

			if (returned == null && createWhenNotExistant) {

				// Creates the concept
				CreateFlexoConcept action;
				if (ownerAction != null) {
					action = CreateFlexoConcept.actionType.makeNewEmbeddedAction(getAccessedVirtualModel(), null, ownerAction);
				}
				else {
					action = CreateFlexoConcept.actionType.makeNewAction(getAccessedVirtualModel(), null, editor);
				}
				action.setNewFlexoConceptName(concept != null ? concept.getName() + "GR" : NONE_FLEXO_CONCEPT_NAME);
				action.doAction();
				returned = action.getNewFlexoConcept();

				if (concept != null) {
					// Create new FlexoConceptInstanceRole to store the concept
					CreateFlexoConceptInstanceRole createConceptRole = null;
					if (ownerAction != null) {
						createConceptRole = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
					}
					else {
						createConceptRole = CreateFlexoConceptInstanceRole.actionType.makeNewAction(returned, null, editor);
					}
					createConceptRole.setRoleName(CONCEPT_ROLE_NAME);
					createConceptRole.setFlexoConceptInstanceType(concept);
					createConceptRole.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>(SAMPLE_DATA_MODEL_SLOT_NAME));
					createConceptRole.doAction();
				}
				else {
					// Create new PrimitiveRole (String type) to store the name of this instance
					CreatePrimitiveRole createNameRole = null;
					if (ownerAction != null) {
						createNameRole = CreatePrimitiveRole.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
					}
					else {
						createNameRole = CreatePrimitiveRole.actionType.makeNewAction(returned, null, editor);
					}
					createNameRole.setRoleName(NAME_ROLE_NAME);
					createNameRole.setPrimitiveType(PrimitiveType.String);
					createNameRole.doAction();
				}

				// Create new DeletionScheme
				CreateFlexoBehaviour createDeletionScheme = null;
				if (ownerAction != null) {
					createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
				}
				else {
					createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewAction(returned, null, editor);
				}
				createDeletionScheme.setFlexoBehaviourName("delete");
				createDeletionScheme.setFlexoBehaviourClass(DeletionScheme.class);
				createDeletionScheme.doAction();
				DeletionScheme deletionScheme = (DeletionScheme) createDeletionScheme.getNewFlexoBehaviour();
				deletionScheme.setSkipConfirmationPanel(true);
				deletionScheme.setVisibility(Visibility.Public);

				if (concept != null) {
					CreateEditionAction deleteConceptAction = null;
					if (ownerAction != null) {
						deleteConceptAction = CreateEditionAction.actionType.makeNewEmbeddedAction(deletionScheme.getControlGraph(), null,
								ownerAction);
					}
					else {
						deleteConceptAction = CreateEditionAction.actionType.makeNewAction(deletionScheme.getControlGraph(), null, editor);
					}
					deleteConceptAction.setEditionActionClass(DeleteFlexoConceptInstance.class);
					deleteConceptAction.doAction();

					DeleteFlexoConceptInstance<?> deleteConcept = (DeleteFlexoConceptInstance<?>) deleteConceptAction.getNewEditionAction();
					deleteConcept.setObject(new DataBinding<>(CONCEPT_ROLE_NAME));
				}

				// Create inspector type entry
				CreateInspectorEntry createTypeEntry = null;
				if (ownerAction != null) {
					createTypeEntry = CreateInspectorEntry.actionType.makeNewEmbeddedAction(returned.getInspector(), null, ownerAction);
				}
				else {
					createTypeEntry = CreateInspectorEntry.actionType.makeNewAction(returned.getInspector(), null, editor);
				}
				createTypeEntry.setEntryName("Type");
				createTypeEntry.setEntryType(String.class);
				createTypeEntry.setWidgetType(WidgetType.TEXT_FIELD);
				if (concept == null) {
					createTypeEntry.setData(new DataBinding<String>('"' + getLocales().localizedForKey("unclassified") + '"'));
				}
				else {
					createTypeEntry.setData(new DataBinding<String>(CONCEPT_ROLE_NAME + ".concept.name"));
				}
				createTypeEntry.setIsReadOnly(true);
				createTypeEntry.doAction();
				// Unused InspectorEntry typeEntry =
				createTypeEntry.getNewEntry();

				// Create inspector name entry
				CreateInspectorEntry createNameEntry = null;
				if (ownerAction != null) {
					createNameEntry = CreateInspectorEntry.actionType.makeNewEmbeddedAction(returned.getInspector(), null, ownerAction);
				}
				else {
					createNameEntry = CreateInspectorEntry.actionType.makeNewAction(returned.getInspector(), null, editor);
				}
				createNameEntry.setEntryName(FMEConceptualModel.NAME_ROLE_NAME);
				createNameEntry.setEntryType(String.class);
				createNameEntry.setWidgetType(WidgetType.TEXT_FIELD);
				if (concept == null) {
					createNameEntry.setData(new DataBinding<String>(FMEConceptualModel.NAME_ROLE_NAME));
				}
				else {
					createNameEntry.setData(new DataBinding<String>(CONCEPT_ROLE_NAME + ".name"));
				}

				createNameEntry.doAction();
				// Unused InspectorEntry nameEntry = createNameEntry.getNewEntry();

				// Create inspector description entry if concept is not null
				if (concept != null) {
					CreateInspectorEntry createDescriptionEntry = null;
					if (ownerAction != null) {
						createDescriptionEntry = CreateInspectorEntry.actionType.makeNewEmbeddedAction(returned.getInspector(), null,
								ownerAction);
					}
					else {
						createDescriptionEntry = CreateInspectorEntry.actionType.makeNewAction(returned.getInspector(), null, editor);
					}
					createDescriptionEntry.setEntryName(FMEConceptualModel.DESCRIPTION_ROLE_NAME);
					createDescriptionEntry.setEntryType(String.class);
					createDescriptionEntry.setWidgetType(WidgetType.TEXT_AREA);
					createDescriptionEntry.setData(new DataBinding<String>(CONCEPT_ROLE_NAME + ".description"));

					createDescriptionEntry.doAction();
					InspectorEntry descriptionEntry = createDescriptionEntry.getNewEntry();
				}

				// Bind shapes's label to name property
				if (concept != null) {
					// If we are bound to a concept instance, use name of concept
					returned.getInspector().setRenderer(new DataBinding<String>("instance." + CONCEPT_ROLE_NAME + ".name"));
				}
				else {
					// Otherwise, this is the NoneGR, use primitive name
					returned.getInspector().setRenderer(new DataBinding<String>("instance.name"));
				}

				configureGRFlexoConcept(returned, concept, containerConceptGR, editor, ownerAction);
			}

			return returned;
		}

		/**
		 * Return (creates when non-existant) a FlexoConcept (in the context of FreeModellingEditor) Created {@link FlexoConcept} will be
		 * designed as a concept linking two other concepts
		 * 
		 * @param conceptName
		 *            name of concept beeing created
		 * @param fromConcept
		 * @param toConcept
		 * @param editor
		 * @param ownerAction
		 * @return
		 * @throws FlexoException
		 */
		@Override
		public FlexoConcept getGRRelationalFlexoConcept(FlexoConcept concept, FlexoConcept fromConceptGR, FlexoConcept toConceptGR,
				FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction, boolean createWhenNotExistant) {

			FlexoConcept returned = getAccessedVirtualModel().getFlexoConcept(concept.getName() + "GR");

			if (returned == null && createWhenNotExistant) {

				// Creates the concept
				CreateFlexoConcept action;
				if (ownerAction != null) {
					action = CreateFlexoConcept.actionType.makeNewEmbeddedAction(getAccessedVirtualModel(), null, ownerAction);
				}
				else {
					action = CreateFlexoConcept.actionType.makeNewAction(getAccessedVirtualModel(), null, editor);
				}
				action.setNewFlexoConceptName(concept.getName() + "GR");
				action.doAction();
				returned = action.getNewFlexoConcept();

				// Create new FlexoConceptInstanceRole to store the concept
				CreateFlexoConceptInstanceRole createConceptRole = null;
				if (ownerAction != null) {
					createConceptRole = CreateFlexoConceptInstanceRole.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
				}
				else {
					createConceptRole = CreateFlexoConceptInstanceRole.actionType.makeNewAction(returned, null, editor);
				}
				createConceptRole.setRoleName(CONCEPT_ROLE_NAME);
				createConceptRole.setFlexoConceptInstanceType(concept);
				createConceptRole.setVirtualModelInstance(new DataBinding<VirtualModelInstance<?, ?>>(SAMPLE_DATA_MODEL_SLOT_NAME));
				createConceptRole.doAction();

				// Create new DeletionScheme
				CreateFlexoBehaviour createDeletionScheme = null;
				if (ownerAction != null) {
					createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(returned, null, ownerAction);
				}
				else {
					createDeletionScheme = CreateFlexoBehaviour.actionType.makeNewAction(returned, null, editor);
				}
				createDeletionScheme.setFlexoBehaviourName("delete");
				createDeletionScheme.setFlexoBehaviourClass(DeletionScheme.class);
				createDeletionScheme.doAction();
				DeletionScheme deletionScheme = (DeletionScheme) createDeletionScheme.getNewFlexoBehaviour();
				deletionScheme.setSkipConfirmationPanel(true);
				deletionScheme.setVisibility(Visibility.Public);

				CreateEditionAction deleteConceptAction = null;
				if (ownerAction != null) {
					deleteConceptAction = CreateEditionAction.actionType.makeNewEmbeddedAction(deletionScheme.getControlGraph(), null,
							ownerAction);
				}
				else {
					deleteConceptAction = CreateEditionAction.actionType.makeNewAction(deletionScheme.getControlGraph(), null, editor);
				}
				deleteConceptAction.setEditionActionClass(DeleteFlexoConceptInstance.class);
				deleteConceptAction.doAction();

				DeleteFlexoConceptInstance<?> deleteConcept = (DeleteFlexoConceptInstance<?>) deleteConceptAction.getNewEditionAction();
				deleteConcept.setObject(new DataBinding<>(CONCEPT_ROLE_NAME));

				// Create inspector type entry
				CreateInspectorEntry createTypeEntry = null;
				if (ownerAction != null) {
					createTypeEntry = CreateInspectorEntry.actionType.makeNewEmbeddedAction(returned.getInspector(), null, ownerAction);
				}
				else {
					createTypeEntry = CreateInspectorEntry.actionType.makeNewAction(returned.getInspector(), null, editor);
				}
				createTypeEntry.setEntryName("Relationship");
				createTypeEntry.setEntryType(String.class);
				createTypeEntry.setWidgetType(WidgetType.TEXT_FIELD);
				createTypeEntry.setData(new DataBinding<String>(CONCEPT_ROLE_NAME + ".render"));
				createTypeEntry.setIsReadOnly(true);
				createTypeEntry.doAction();
				createTypeEntry.getNewEntry();

				// Create inspector source entry
				CreateInspectorEntry createSourceEntry = null;
				if (ownerAction != null) {
					createSourceEntry = CreateInspectorEntry.actionType.makeNewEmbeddedAction(returned.getInspector(), null, ownerAction);
				}
				else {
					createSourceEntry = CreateInspectorEntry.actionType.makeNewAction(returned.getInspector(), null, editor);
				}
				createSourceEntry.setEntryName(FMEConceptualModel.FROM_CONCEPT_ROLE_NAME);
				createSourceEntry.setEntryType(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE);
				createSourceEntry.setWidgetType(WidgetType.CUSTOM_WIDGET);
				createSourceEntry.setData(new DataBinding<String>(CONCEPT_ROLE_NAME + "." + FMEConceptualModel.FROM_CONCEPT_ROLE_NAME));
				createSourceEntry.doAction();

				// Create inspector destination entry
				CreateInspectorEntry createDestinationEntry = null;
				if (ownerAction != null) {
					createDestinationEntry = CreateInspectorEntry.actionType.makeNewEmbeddedAction(returned.getInspector(), null,
							ownerAction);
				}
				else {
					createDestinationEntry = CreateInspectorEntry.actionType.makeNewAction(returned.getInspector(), null, editor);
				}
				createDestinationEntry.setEntryName(FMEConceptualModel.TO_CONCEPT_ROLE_NAME);
				createDestinationEntry.setEntryType(FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE);
				createDestinationEntry.setWidgetType(WidgetType.CUSTOM_WIDGET);
				createDestinationEntry.setData(new DataBinding<String>(CONCEPT_ROLE_NAME + "." + FMEConceptualModel.TO_CONCEPT_ROLE_NAME));
				createDestinationEntry.doAction();

				// Bind shapes's label to name property
				// If we are bound to a concept instance, use name of concept
				returned.getInspector().setRenderer(new DataBinding<String>("instance." + CONCEPT_ROLE_NAME + ".render"));

				configureGRRelationalFlexoConcept(returned, concept, fromConceptGR, toConceptGR, editor, ownerAction);
			}

			return returned;
		}

		protected abstract void configureGRFlexoConcept(FlexoConcept returned, FlexoConcept concept, FlexoConcept containerConceptGR,
				FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction);

		protected abstract void configureGRRelationalFlexoConcept(FlexoConcept returned, FlexoConcept concept, FlexoConcept fromConceptGR,
				FlexoConcept toConceptGR, FlexoEditor editor, FlexoAction<?, ?, ?> ownerAction);

		@Override
		public FMEFreeModelInstance getFreeModelInstance(String freeModelName) {
			for (FMEFreeModelInstance freeModelInstance : getFreeModelInstances()) {
				if (freeModelInstance.getName().equals(freeModelName)) {
					return freeModelInstance;
				}
				if (freeModelInstance.getName().equals(freeModelName + FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX)) {
					return freeModelInstance;
				}
			}
			return null;
		}

		@Override
		public FMEConceptualModel getConceptualModel() {
			FMEConceptualModel returned = (FMEConceptualModel) performSuperGetter(CONCEPTUAL_MODEL);
			if (returned != null) {
				return returned;
			}
			if (getNature() != null) {
				return getNature().getConceptualModel();
			}
			return null;
		}

		@Override
		public void setConceptualModel(FMEConceptualModel conceptualModel) {
			performSuperSetter(CONCEPTUAL_MODEL, conceptualModel);
			if (conceptualModel != null) {
				conceptualModel.setNature(getNature());
			}
		}

		@Override
		public FMESampleData getSampleData() {
			FMESampleData returned = (FMESampleData) performSuperGetter(SAMPLE_DATA);
			if (returned != null) {
				return returned;
			}
			if (getNature() != null) {
				return getNature().getSampleData();
			}
			return null;
		}

		@Override
		public void setSampleData(FMESampleData sampleData) {
			performSuperSetter(SAMPLE_DATA, sampleData);
			if (sampleData != null) {
				sampleData.setNature(getNature());
			}
		}

		@Override
		public void finalizeDeserialization() {
			System.out.println("a la fin, " + getNature());
			if (performSuperGetter(CONCEPTUAL_MODEL) != null) {
				getConceptualModel().setNature(getNature());
			}
			if (performSuperGetter(SAMPLE_DATA) != null) {
				getSampleData().setNature(getNature());
			}
		}

	}

}
