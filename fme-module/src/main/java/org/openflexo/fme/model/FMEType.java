/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

import java.lang.reflect.Type;

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEnumType;

/**
 * All basic types used in the context of FreeModellingEditor
 * 
 * @author sylvain
 *
 */
public enum FMEType {
	Boolean {
		@Override
		public Type getType() {
			return Boolean.class;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return PrimitiveType.Boolean;
		}
	},
	String {
		@Override
		public Type getType() {
			return String.class;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return PrimitiveType.String;
		}
	},
	Date {
		@Override
		public Type getType() {
			return java.util.Date.class;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return PrimitiveType.Date;
		}
	},
	Integer {
		@Override
		public Type getType() {
			return Integer.class;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return PrimitiveType.Integer;
		}
	},
	Float {
		@Override
		public Type getType() {
			return Float.class;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return PrimitiveType.Float;
		}
	},
	Enumeration {
		@Override
		public Type getType() {
			return FlexoEnumType.UNDEFINED_FLEXO_ENUM_TYPE;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return null;
		}
	},
	Reference {
		@Override
		public Type getType() {
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}

		@Override
		public PrimitiveType getPrimitiveType() {
			return null;
		}
	};

	public abstract Type getType();

	public abstract PrimitiveType getPrimitiveType();

}
