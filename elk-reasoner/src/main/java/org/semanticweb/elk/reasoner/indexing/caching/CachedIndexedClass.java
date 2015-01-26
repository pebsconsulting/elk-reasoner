package org.semanticweb.elk.reasoner.indexing.caching;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClass;

/**
 * A {@link ModifiableIndexedClass} that can be used for memoization (caching).
 * 
 * @author "Yevgeny Kazakov"
 *
 * @param <T>
 *            the type of the {@link CachedIndexedClass}
 */
public interface CachedIndexedClass extends ModifiableIndexedClass,
		CachedIndexedClassEntity<CachedIndexedClass> {

	static class Helper extends CachedIndexedObject.Helper {

		public static int structuralHashCode(ElkClass entity) {
			return combinedHashCode(CachedIndexedClass.class, entity.getIri());
		}

		public static CachedIndexedClass structuralEquals(
				CachedIndexedClass first, Object second) {
			if (first == second) {
				return first;
			}
			if (second instanceof CachedIndexedClass) {
				CachedIndexedClass secondEntry = (CachedIndexedClass) second;
				if (first.getElkEntity().getIri()
						.equals(secondEntry.getElkEntity().getIri()))
					return secondEntry;
			}
			return null;
		}

	}
}
