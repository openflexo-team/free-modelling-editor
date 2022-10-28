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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.diagram.fml.FMLControlledDiagramVirtualModelInstanceNature;
import org.openflexo.technologyadapter.diagram.model.Diagram;

/**
 * Represents a {@link FMEDiagramFreeModelInstance} in the FreeModellingEditor<br>
 * 
 * The base of a {@link FMEDiagramFreeModelInstance} is a {@link FMLRTVirtualModelInstance} with the specific
 * {@link FMLControlledDiagramVirtualModelInstanceNature}<br>
 * From a technical point of view, a {@link FMEDiagramFreeModelInstance} is just a wrapper above a {@link FMLRTVirtualModelInstance} located
 * in project's freeModellingView
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@XMLElement
@ImplementationClass(FMEDiagramFreeModelInstance.FMEDiagramFreeModelInstanceImpl.class)
public interface FMEDiagramFreeModelInstance extends FMEFreeModelInstance {

	public static final String DEFAULT_DIAGRAM_FOLDER = "Diagram";

	@Override
	public FMEDiagramFreeModel getFreeModel();

	public Diagram getDiagram();

	public abstract class FMEDiagramFreeModelInstanceImpl extends FMEFreeModelInstanceImpl implements FMEDiagramFreeModelInstance {

		private static final Logger logger = FlexoLogger.getLogger(FMEDiagramFreeModelInstance.class.getPackage().getName());

		@Override
		public Diagram getDiagram() {
			return FMLControlledDiagramVirtualModelInstanceNature.getDiagram(getAccessedVirtualModelInstance());
		}

		@Override
		public FMEDiagramFreeModel getFreeModel() {
			return (FMEDiagramFreeModel) performSuperGetter(FREE_MODEL);
		}
	}

}
