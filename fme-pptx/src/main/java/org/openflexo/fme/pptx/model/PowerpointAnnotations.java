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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.technologyadapter.powerpoint.fml.PowerpointShapeRole;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;

/**
 * What the PowerPoint actions of the FreeModellingEditor share about the shapes of a slideshow: the free model instances annotating a
 * shape, the instance standing for a shape, the name a shape gives to a new instance<br>
 * 
 * The free model instances whose virtual model instance is connected are registered here, so that the actions offered on a shape, which
 * only know the shape, find whether it is annotated
 * 
 * @author sylvain
 * 
 */
public class PowerpointAnnotations {

	/** The longest name proposed for a new instance, taken from the text of a shape */
	public static final int MAX_NAME_LENGTH = 40;

	private static final Set<FMEPowerpointFreeModelInstance> CONNECTED_INSTANCES = Collections
			.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private PowerpointAnnotations() {
	}

	/**
	 * Called when the virtual model instance of supplied free model instance is connected
	 */
	public static void register(FMEPowerpointFreeModelInstance freeModelInstance) {
		CONNECTED_INSTANCES.add(freeModelInstance);
	}

	/**
	 * Called when supplied free model instance no longer annotates its slideshow
	 */
	public static void unregister(FMEPowerpointFreeModelInstance freeModelInstance) {
		CONNECTED_INSTANCES.remove(freeModelInstance);
	}

	/**
	 * The connected free model instances annotating the slideshow of supplied shape
	 */
	public static List<FMEPowerpointFreeModelInstance> getFreeModelInstances(PowerpointShape shape) {
		List<FMEPowerpointFreeModelInstance> returned = new ArrayList<>();
		synchronized (CONNECTED_INSTANCES) {
			for (FMEPowerpointFreeModelInstance freeModelInstance : CONNECTED_INSTANCES) {
				if (!freeModelInstance.isDeleted() && freeModelInstance.contains(shape)) {
					returned.add(freeModelInstance);
				}
			}
		}
		return returned;
	}

	/**
	 * The instance of a GR concept standing for supplied shape, in one of the connected free model instances, or null
	 */
	public static FlexoConceptInstance getGRInstance(PowerpointShape shape) {
		for (FMEPowerpointFreeModelInstance freeModelInstance : getFreeModelInstances(shape)) {
			FlexoConceptInstance returned = freeModelInstance.getGRInstance(shape);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	/**
	 * Whether supplied shape belongs to a slideshow a free model instance annotates, and is not annotated yet: a shape carries one
	 * annotation at most
	 */
	public static boolean isAnnotatable(PowerpointShape shape) {
		return !getFreeModelInstances(shape).isEmpty() && getGRInstance(shape) == null;
	}

	/**
	 * Whether supplied shape stands for an instance of a concept
	 */
	public static boolean isAnnotated(PowerpointShape shape) {
		return FMEFreeModel.representedInstance(getGRInstance(shape)) != null;
	}

	/**
	 * The name a shape gives to a new instance: the first line of its text, shortened, or its PowerPoint name when it has no text
	 */
	public static String defaultInstanceName(PowerpointShape shape) {
		String text = shape.getText();
		if (text != null) {
			for (String line : text.split("\\R")) {
				String trimmed = line.trim();
				if (!trimmed.isEmpty()) {
					return trimmed.length() > MAX_NAME_LENGTH ? trimmed.substring(0, MAX_NAME_LENGTH).trim() : trimmed;
				}
			}
		}
		return shape.getName();
	}

	/**
	 * Creates, in supplied free model instance, an instance of supplied GR concept standing for supplied shape
	 */
	public static FlexoConceptInstance createGRInstance(FMEPowerpointFreeModelInstance freeModelInstance, PowerpointShape shape,
			FlexoConcept grConcept) throws FMLExecutionException {
		FlexoConceptInstance returned = freeModelInstance.getAccessedVirtualModelInstance().makeNewFlexoConceptInstance(grConcept, null,
				null);
		returned.setFlexoActor(shape, (PowerpointShapeRole) grConcept.getAccessibleProperty(FMEPowerpointFreeModel.SHAPE_ROLE_NAME));
		return returned;
	}

}
