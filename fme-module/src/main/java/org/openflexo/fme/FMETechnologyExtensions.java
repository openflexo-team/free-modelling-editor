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

package org.openflexo.fme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.openflexo.fme.model.FMEFreeModel;
import org.openflexo.fme.model.FMEFreeModelInstance;

/**
 * The {@link FMETechnologyExtension}s found on the classpath, loaded once
 * 
 * @author sylvain
 * 
 */
public class FMETechnologyExtensions {

	private static final Logger logger = Logger.getLogger(FMETechnologyExtensions.class.getPackage().getName());

	private static List<FMETechnologyExtension> extensions;

	private FMETechnologyExtensions() {
	}

	public static synchronized List<FMETechnologyExtension> getExtensions() {
		if (extensions == null) {
			List<FMETechnologyExtension> found = new ArrayList<>();
			for (FMETechnologyExtension extension : ServiceLoader.load(FMETechnologyExtension.class)) {
				logger.info("Found FreeModellingEditor extension " + extension.getIdentifier());
				found.add(extension);
			}
			extensions = Collections.unmodifiableList(found);
		}
		return extensions;
	}

	/**
	 * The PAMELA entities all extensions add to the nature
	 */
	public static List<Class<?>> getModelEntities() {
		List<Class<?>> returned = new ArrayList<>();
		for (FMETechnologyExtension extension : getExtensions()) {
			returned.addAll(extension.getModelEntities());
		}
		return returned;
	}

	public static FMETechnologyExtension getExtension(String identifier) {
		for (FMETechnologyExtension extension : getExtensions()) {
			if (extension.getIdentifier().equals(identifier)) {
				return extension;
			}
		}
		return null;
	}

	public static FMETechnologyExtension getExtension(FMEFreeModel freeModel) {
		for (FMETechnologyExtension extension : getExtensions()) {
			if (extension.handles(freeModel)) {
				return extension;
			}
		}
		return null;
	}

	public static FMETechnologyExtension getExtension(FMEFreeModelInstance freeModelInstance) {
		for (FMETechnologyExtension extension : getExtensions()) {
			if (extension.handles(freeModelInstance)) {
				return extension;
			}
		}
		return null;
	}

}
