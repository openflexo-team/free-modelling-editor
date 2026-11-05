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
package org.openflexo.fme.pptx;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openflexo.fme.FMETechnologyExtension;
import org.openflexo.fme.FMETechnologyExtensions;
import org.openflexo.fme.model.FreeModellingProjectNature;
import org.openflexo.fme.model.FreeModellingProjectNatureFactory;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModel;
import org.openflexo.fme.pptx.model.FMEPowerpointFreeModelInstance;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;

/**
 * The PowerPoint extension is found on the classpath, and brings its entities to the {@link FreeModellingProjectNature}
 * 
 * @author sylvain
 * 
 */
public class TestPowerpointFMEExtension {

	@Test
	public void testExtensionIsFound() {
		FMETechnologyExtension extension = FMETechnologyExtensions.getExtension(PowerpointFMEExtension.IDENTIFIER);
		assertTrue(extension instanceof PowerpointFMEExtension);
		assertSame(PowerpointTechnologyAdapter.class, extension.getTechnologyAdapterClass());
		// What is not a PowerPoint shape is inspected as it is
		Object other = new Object();
		assertSame(other, extension.getInspectedObject(other));
	}

	@Test
	public void testNatureFactoryContributesExtensionEntities() {
		List<Class<?>> classes = new FreeModellingProjectNatureFactory().getProjectNatureSpecificClasses();
		assertTrue(classes.contains(FreeModellingProjectNature.class));
		assertTrue(classes.contains(FMEPowerpointFreeModel.class));
		assertTrue(classes.contains(FMEPowerpointFreeModelInstance.class));
	}

}
