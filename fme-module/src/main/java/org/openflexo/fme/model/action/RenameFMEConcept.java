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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEInspectorGenerator;
import org.openflexo.fme.model.FMENames;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.toolbox.StringUtils;

/**
 * This action renames a concept of the FreeModellingEditor, together with its graphical representation: <code>Xxx</code> and
 * <code>XxxGR</code> (see {@link FMEFreeModel#getGRFlexoConcept}, which finds the GR by that name alone, and would create a second one
 * for a renamed concept).<br>
 * Focused object is either the conceptual concept, its GR concept, or an instance of one of them: the concept renamed is then the one
 * the instance is an instance of.
 * 
 * <p>
 * Nothing is saved: the resources are only marked as modified.
 * 
 * @author sylvain
 * 
 */
public class RenameFMEConcept extends FMEAction<RenameFMEConcept, FlexoObject, FlexoObject> {

	private static final Logger logger = Logger.getLogger(RenameFMEConcept.class.getPackage().getName());

	public static FlexoActionFactory<RenameFMEConcept, FlexoObject, FlexoObject> actionType = new FlexoActionFactory<RenameFMEConcept, FlexoObject, FlexoObject>(
			"rename_concept", FlexoActionFactory.refactorMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public RenameFMEConcept makeNewAction(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
			return new RenameFMEConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return conceptToRename(object) != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoObject object, Vector<FlexoObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(RenameFMEConcept.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(RenameFMEConcept.actionType, FlexoConceptInstance.class);
	}

	private String newConceptName;

	private RenameFMEConcept(FlexoObject focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		newConceptName = getConcept() != null ? getConcept().getName() : null;
	}

	/**
	 * The concept supplied object stands for: the concept itself, or the concept it is an instance of. Whether it is a GR concept or the
	 * conceptual one, the conceptual concept is returned.
	 * 
	 * @return null when this object is not the one of a concept FME can rename: not a concept of a free modelling project, the NoneGR
	 *         (which stands for no concept), an enum, a VirtualModel...
	 */
	public static FlexoConcept conceptToRename(FlexoObject object) {

		FlexoConcept focused = null;
		if (object instanceof FlexoConceptInstance) {
			focused = ((FlexoConceptInstance) object).getFlexoConcept();
		}
		else if (object instanceof FlexoConcept) {
			focused = (FlexoConcept) object;
		}
		if (focused == null || focused instanceof VirtualModel || focused instanceof FlexoEnum) {
			return null;
		}

		FreeModellingProjectNature nature = natureOf(focused);
		if (nature == null) {
			return null;
		}

		if (nature.getFreeModel(focused.getOwner()) != null) {
			// A concept of a free model: a GR concept (or the NoneGR, standing for no concept)
			FlexoConceptInstanceRole conceptRole = FMEFreeModel.conceptRole(focused);
			return conceptRole != null ? conceptRole.getFlexoConceptType() : null;
		}

		// A conceptual concept
		if (isInConceptualModel(nature.getConceptualModel(), focused.getOwner())
				&& (FMEConceptualModel.labelPropertyName(focused) != null || FMEConceptualModel.isRelationship(focused))) {
			return focused;
		}

		return null;
	}

	private static boolean isInConceptualModel(FMEConceptualModel conceptualModel, VirtualModel virtualModel) {
		if (conceptualModel == null || virtualModel == null) {
			return false;
		}
		if (conceptualModel.getAccessedVirtualModel() == virtualModel) {
			return true;
		}
		for (FMEConceptualModel child : conceptualModel.getChildren()) {
			if (isInConceptualModel(child, virtualModel)) {
				return true;
			}
		}
		return false;
	}

	private static FreeModellingProjectNature natureOf(FlexoConcept concept) {
		if (concept.getDeclaringCompilationUnit() == null || concept.getDeclaringCompilationUnit().getResource() == null) {
			return null;
		}
		FlexoResourceCenter<?> resourceCenter = concept.getDeclaringCompilationUnit().getResource().getResourceCenter();
		FlexoProject<?> project = null;
		if (resourceCenter instanceof FlexoProject) {
			project = (FlexoProject<?>) resourceCenter;
		}
		else if (resourceCenter != null && resourceCenter.getDelegatingProjectResource() != null) {
			project = resourceCenter.getDelegatingProjectResource().getFlexoProject();
		}
		if (project != null && project.hasNature(FreeModellingProjectNature.class)) {
			return project.getNature(FreeModellingProjectNature.class);
		}
		return null;
	}

	/**
	 * The concept to rename (conceptual model level)
	 */
	public FlexoConcept getConcept() {
		return conceptToRename(getFocusedObject());
	}

	/**
	 * The GR concepts of the concept to rename: one per free model representing it
	 */
	public List<FlexoConcept> getGRConcepts() {

		List<FlexoConcept> returned = new ArrayList<>();
		FlexoConcept concept = getConcept();
		if (concept == null) {
			return returned;
		}

		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		if (nature == null) {
			nature = natureOf(concept);
		}
		if (nature != null) {
			for (FMEFreeModel freeModel : nature.getFreeModels()) {
				VirtualModel freeModelVirtualModel = freeModel.getAccessedVirtualModel();
				FlexoConcept grConcept = freeModelVirtualModel != null ? freeModelVirtualModel.getFlexoConcept(grName(concept.getName()))
						: null;
				if (grConcept != null && isGROf(grConcept, concept)) {
					returned.add(grConcept);
				}
			}
		}

		// Whatever its name, the GR the user pointed at
		FlexoConcept focused = getFocusedObject() instanceof FlexoConceptInstance
				? ((FlexoConceptInstance) getFocusedObject()).getFlexoConcept()
				: (getFocusedObject() instanceof FlexoConcept ? (FlexoConcept) getFocusedObject() : null);
		if (focused != null && focused != concept && !returned.contains(focused)) {
			returned.add(focused);
		}

		return returned;
	}

	private static boolean isGROf(FlexoConcept grConcept, FlexoConcept concept) {
		FlexoConceptInstanceRole conceptRole = FMEFreeModel.conceptRole(grConcept);
		return conceptRole != null && conceptRole.getFlexoConceptType() == concept;
	}

	private static String grName(String conceptName) {
		return conceptName + "GR";
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

	/**
	 * Localization key of what prevents this action to run, or null when the new name can be used
	 */
	public String getIssue() {

		if (getConcept() == null) {
			return "cannot_rename_that_concept";
		}
		if (StringUtils.isEmpty(getNewConceptName())) {
			return "no_concept_name_defined";
		}
		if (!FMENames.isValidConceptName(getNewConceptName())) {
			return "invalid_concept_name";
		}
		if (getNewConceptName().equals(getConcept().getName())) {
			return "that_is_already_the_name_of_the_concept";
		}
		VirtualModel conceptVirtualModel = getConcept().getOwner();
		if (conceptVirtualModel != null && conceptVirtualModel.getFlexoConcept(getNewConceptName()) != null) {
			return "a_concept_with_that_name_already_exists";
		}
		for (FlexoConcept grConcept : getGRConcepts()) {
			VirtualModel grVirtualModel = grConcept.getOwner();
			if (grVirtualModel != null && grVirtualModel.getFlexoConcept(grName(getNewConceptName())) != null) {
				return "a_concept_with_that_name_already_exists";
			}
		}
		return null;
	}

	@Override
	public boolean isValid() {
		return getIssue() == null;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FlexoConcept concept = getConcept();
		String oldConceptName = concept.getName();
		// The GR concepts are found by their name: before it changes
		List<FlexoConcept> grConcepts = getGRConcepts();
		FreeModellingProjectNature nature = getFreeModellingProjectNature();
		if (nature == null) {
			nature = natureOf(concept);
		}

		// Before the names actually change: a .fib/.inspector still resolved by naming convention alone would otherwise be orphaned
		concept.freezeConventionalUIComponentNames();
		for (FlexoConcept grConcept : grConcepts) {
			grConcept.freezeConventionalUIComponentNames();
		}

		try {
			concept.setName(getNewConceptName());
			for (FlexoConcept grConcept : grConcepts) {
				grConcept.setName(grName(getNewConceptName()));
			}
		} catch (InvalidNameException e) {
			throw new FlexoException(e);
		}

		FMEInspectorGenerator.conceptRenamed(concept, oldConceptName);

		if (nature != null) {
			for (FlexoConcept grConcept : grConcepts) {
				FMEFreeModel freeModel = nature.getFreeModel(grConcept.getOwner());
				if (freeModel != null) {
					freeModel.conceptRenamed(grConcept, concept, oldConceptName);
				}
				else {
					logger.warning("No free model found for " + grConcept + ": its derived names are left as they are");
				}
			}
		}

		// Marked, never saved
		if (concept.getDeclaringCompilationUnit() != null) {
			concept.getDeclaringCompilationUnit().setIsModified();
		}
		for (FlexoConcept grConcept : grConcepts) {
			if (grConcept.getDeclaringCompilationUnit() != null) {
				grConcept.getDeclaringCompilationUnit().setIsModified();
			}
		}
	}

}
