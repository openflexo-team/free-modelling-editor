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
package org.openflexo.fme.model.action;

import java.util.List;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;

/**
 * Abstract action focused on a link between two elements of a free model instance (a diagram connector, a PowerPoint connector glued to
 * two shapes...), each end standing for an instance of a concept. It resolves the instances of GR concepts standing for both ends, their
 * GR concepts, and the concepts and conceptual instances they stand for.<br>
 * 
 * The technology gives the elements at both ends of the link ({@link #getStartElement()}, {@link #getEndElement()})
 * 
 * @param <A>
 *            type of the action
 * @param <T>
 *            type of the focused link
 * @param <FM>
 *            type of the free model
 * @param <FMI>
 *            type of the free model instance
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractLinkAction<A extends AbstractLinkAction<A, T, FM, FMI>, T extends FlexoObject, FM extends FMEFreeModel, FMI extends FMEFreeModelInstance>
		extends AbstractFreeModelInstanceAction<A, T, FM, FMI> {

	private FlexoConcept fromFlexoConcept;
	private FlexoConcept toFlexoConcept;

	protected AbstractLinkAction(FlexoActionFactory<A, T, FlexoObject> actionFactory, T focusedObject, List<FlexoObject> globalSelection,
			FlexoEditor editor) {
		super(actionFactory, focusedObject, globalSelection, editor);
	}

	/**
	 * The element at the start of the focused link, or null
	 */
	protected abstract Object getStartElement();

	/**
	 * The element at the end of the focused link, or null
	 */
	protected abstract Object getEndElement();

	private FlexoConceptInstance grInstanceOf(Object element) {
		FMI freeModelInstance = getFMEFreeModelInstance();
		return freeModelInstance != null && element != null ? freeModelInstance.getGRInstance(element) : null;
	}

	private static FlexoConcept conceptOf(FlexoConceptInstance grInstance) {
		FlexoConceptInstanceRole role = grInstance != null ? FMEFreeModel.conceptRole(grInstance.getFlexoConcept()) : null;
		return role != null ? role.getFlexoConceptType() : null;
	}

	/**
	 * The instance of a GR concept standing for the start of the link
	 */
	public FlexoConceptInstance getFromGRFlexoConceptInstance() {
		return grInstanceOf(getStartElement());
	}

	/**
	 * The instance of a GR concept standing for the end of the link
	 */
	public FlexoConceptInstance getToGRFlexoConceptInstance() {
		return grInstanceOf(getEndElement());
	}

	public FlexoConcept getFromFlexoConceptGR() {
		return getFromGRFlexoConceptInstance() != null ? getFromGRFlexoConceptInstance().getFlexoConcept() : null;
	}

	public FlexoConcept getToFlexoConceptGR() {
		return getToGRFlexoConceptInstance() != null ? getToGRFlexoConceptInstance().getFlexoConcept() : null;
	}

	/**
	 * The conceptual instance the start of the link stands for
	 */
	public FlexoConceptInstance getFromFlexoConceptInstance() {
		return FMEFreeModel.representedInstance(getFromGRFlexoConceptInstance());
	}

	/**
	 * The conceptual instance the end of the link stands for
	 */
	public FlexoConceptInstance getToFlexoConceptInstance() {
		return FMEFreeModel.representedInstance(getToGRFlexoConceptInstance());
	}

	/**
	 * The concept at the start of the link: the one its start stands for, unless set
	 */
	public FlexoConcept getFromFlexoConcept() {
		if (fromFlexoConcept == null) {
			return conceptOf(getFromGRFlexoConceptInstance());
		}
		return fromFlexoConcept;
	}

	public void setFromFlexoConcept(FlexoConcept fromFlexoConcept) {
		if (fromFlexoConcept != getFromFlexoConcept()) {
			FlexoConcept oldValue = getFromFlexoConcept();
			this.fromFlexoConcept = fromFlexoConcept;
			getPropertyChangeSupport().firePropertyChange("fromFlexoConcept", oldValue, fromFlexoConcept);
		}
	}

	/**
	 * The concept at the end of the link: the one its end stands for, unless set
	 */
	public FlexoConcept getToFlexoConcept() {
		if (toFlexoConcept == null) {
			return conceptOf(getToGRFlexoConceptInstance());
		}
		return toFlexoConcept;
	}

	public void setToFlexoConcept(FlexoConcept toFlexoConcept) {
		if (toFlexoConcept != getToFlexoConcept()) {
			FlexoConcept oldValue = getToFlexoConcept();
			this.toFlexoConcept = toFlexoConcept;
			getPropertyChangeSupport().firePropertyChange("toFlexoConcept", oldValue, toFlexoConcept);
		}
	}

	/**
	 * Whether both ends of the link stand for a conceptual instance
	 */
	public boolean linksTwoInstances() {
		return getFromFlexoConceptInstance() != null && getToFlexoConceptInstance() != null;
	}

}
