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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.fme.model.FMEConceptualModel;
import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.toolbox.StringUtils;

/**
 * This action is used to create a new FlexoConcept, designed as a reified relationship between two concepts
 * 
 * @author sylvain
 * 
 */
public class CreateNewRelationalConcept extends FMEAction<CreateNewRelationalConcept, FMEFreeModel, FlexoObject> implements Bindable {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateNewRelationalConcept.class.getPackage().getName());

	public static FlexoActionFactory<CreateNewRelationalConcept, FMEFreeModel, FlexoObject> actionType = new FlexoActionFactory<CreateNewRelationalConcept, FMEFreeModel, FlexoObject>(
			"create_new_relational_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateNewRelationalConcept makeNewAction(FMEFreeModel focusedObject, Vector<FlexoObject> globalSelection,
				FlexoEditor editor) {
			return new CreateNewRelationalConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMEFreeModel object, Vector<FlexoObject> globalSelection) {
			return true;
		}

		@Override
		public boolean isEnabledForSelection(FMEFreeModel object, Vector<FlexoObject> globalSelection) {
			return true;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateNewRelationalConcept.actionType, FMEFreeModel.class);
	}

	private String newConceptName;
	private String newConceptDescription;
	private FlexoConcept fromConcept;
	private FlexoConcept toConcept;
	private FlexoConcept fromGRConcept;
	private FlexoConcept toGRConcept;

	private FlexoConcept newFlexoConcept;
	private FlexoConcept newGRFlexoConcept;

	// private FlexoConceptInstance newFlexoConceptInstance;

	private CreateNewRelationalConcept(FMEFreeModel focusedObject, Vector<FlexoObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public FreeModellingProjectNature getFreeModellingProjectNature() {
		return getFocusedObject().getNature();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		// Now we create the new concept
		newFlexoConcept = getFocusedObject().getConceptualModel().getRelationalFlexoConcept(getNewConceptName(), getFromConcept(),
				getToConcept(), getEditor(), this);
		newFlexoConcept.getInspector().setRenderer(getRenderer());

		// Now we create the new concept GR
		newGRFlexoConcept = getFocusedObject().getGRRelationalFlexoConcept(newFlexoConcept, getFromGRConcept(), getToGRConcept(),
				getEditor(), this, true);
	}

	public String getNewConceptName() {
		return newConceptName;
	}

	public void setNewConceptName(String newConceptName) {
		boolean wasValid = isValid();
		this.newConceptName = newConceptName;
		getPropertyChangeSupport().firePropertyChange("newConceptName", null, newConceptName);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public String getNewConceptDescription() {
		return newConceptDescription;
	}

	public void setNewConceptDescription(String newConceptDescription) {
		boolean wasValid = isValid();
		this.newConceptDescription = newConceptDescription;
		getPropertyChangeSupport().firePropertyChange("newConceptDescription", null, newConceptDescription);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
	}

	public FlexoConcept getFromConcept() {
		return fromConcept;
	}

	public void setFromConcept(FlexoConcept fromConcept) {
		if (fromConcept != this.fromConcept) {
			FlexoConcept oldValue = this.fromConcept;
			this.fromConcept = fromConcept;
			getPropertyChangeSupport().firePropertyChange("fromConcept", oldValue, fromConcept);
		}
	}

	public FlexoConcept getToConcept() {
		return toConcept;
	}

	public void setToConcept(FlexoConcept toConcept) {
		if (toConcept != this.toConcept) {
			FlexoConcept oldValue = this.toConcept;
			this.toConcept = toConcept;
			getPropertyChangeSupport().firePropertyChange("toConcept", oldValue, toConcept);
		}
	}

	public FlexoConcept getFromGRConcept() {
		return fromGRConcept;
	}

	public void setFromGRConcept(FlexoConcept fromGRConcept) {
		if ((fromGRConcept == null && this.fromGRConcept != null) || (fromGRConcept != null && !fromGRConcept.equals(this.fromGRConcept))) {
			FlexoConcept oldValue = this.fromGRConcept;
			this.fromGRConcept = fromGRConcept;
			getPropertyChangeSupport().firePropertyChange("fromGRConcept", oldValue, fromGRConcept);
		}
	}

	public FlexoConcept getToGRConcept() {
		return toGRConcept;
	}

	public void setToGRConcept(FlexoConcept toGRConcept) {
		if ((toGRConcept == null && this.toGRConcept != null) || (toGRConcept != null && !toGRConcept.equals(this.toGRConcept))) {
			FlexoConcept oldValue = this.toGRConcept;
			this.toGRConcept = toGRConcept;
			getPropertyChangeSupport().firePropertyChange("toGRConcept", oldValue, toGRConcept);
		}
	}

	public FlexoConcept getNewFlexoConcept() {
		return newFlexoConcept;
	}

	public FlexoConcept getNewGRFlexoConcept() {
		return newGRFlexoConcept;
	}

	@Override
	public boolean isValid() {

		if (StringUtils.isEmpty(newConceptName)) {
			return false;
		}

		if (getFocusedObject().getAccessedVirtualModel().getFlexoConcept(newConceptName) != null) {
			// a_concept_with_that_name_already_exists
			return false;
		}

		return true;
	}

	private DataBinding<String> renderer;
	private DataBinding<String> defaultRenderer;
	private RendererBindingModel rendererBM;

	private String getDefaultRendererAsString() {
		return "\"" + getNewConceptName() + " \"+sourceConcept.name+\" - \"+destinationConcept.name";
	}

	public DataBinding<String> getDefaultRenderer() {
		if (defaultRenderer == null) {
			defaultRenderer = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
			defaultRenderer.setBindingName("renderer");
			defaultRenderer.setMandatory(true);
		}
		if (StringUtils.isNotEmpty(getNewConceptName())) {
			defaultRenderer.setUnparsedBinding(getDefaultRendererAsString());
		}
		return defaultRenderer;
	}

	public DataBinding<String> getRenderer() {
		if (renderer == null) {
			renderer = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
			renderer.setBindingName("renderer");
			renderer.setMandatory(true);
		}
		if (!renderer.isSet()) {
			return getDefaultRenderer();
		}
		return renderer;
	}

	public void setRenderer(DataBinding<?> renderer) {
		if (renderer != null) {
			this.renderer = new DataBinding<String>(renderer.toString(), this, String.class, DataBinding.BindingDefinitionType.GET);
			renderer.setBindingName("renderer");
			renderer.setMandatory(true);
		}
		notifiedBindingChanged(this.renderer);
	}

	@Override
	public BindingFactory getBindingFactory() {
		return getFocusedObject().getAccessedVirtualModel().getBindingFactory();
	}

	@Override
	public BindingModel getBindingModel() {
		if (rendererBM == null) {
			rendererBM = new RendererBindingModel();
		}
		return rendererBM;
	}

	@Override
	public void notifiedBindingChanged(org.openflexo.connie.DataBinding<?> dataBinding) {
		if (dataBinding.isSet()) {
			if (!dataBinding.toString().equals(getDefaultRendererAsString())) {
				renderer.setUnparsedBinding(dataBinding.toString());
			}
		}
	}

	@Override
	public void notifiedBindingDecoded(org.openflexo.connie.DataBinding<?> dataBinding) {
		// TODO
	}

	/**
	 * This is the {@link BindingModel} used to define the renderer
	 * 
	 * @author sylvain
	 * 
	 */
	public class RendererBindingModel extends BindingModel {

		private RendererBindingModel() {
			super(getFocusedObject().getAccessedVirtualModel() != null ? getFocusedObject().getAccessedVirtualModel().getBindingModel()
					: null);

			BindingVariable fromConceptBV = new BindingVariable(FMEConceptualModel.FROM_CONCEPT_ROLE_NAME,
					FlexoConceptInstanceType.getFlexoConceptInstanceType(getFromConcept()));
			fromConceptBV.setCacheable(false);
			addToBindingVariables(fromConceptBV);

			BindingVariable toConceptBV = new BindingVariable(FMEConceptualModel.TO_CONCEPT_ROLE_NAME,
					FlexoConceptInstanceType.getFlexoConceptInstanceType(getToConcept()));
			toConceptBV.setCacheable(false);
			addToBindingVariables(toConceptBV);
		}

	}

}
