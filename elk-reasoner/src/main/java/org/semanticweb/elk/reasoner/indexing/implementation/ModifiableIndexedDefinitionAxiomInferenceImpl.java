package org.semanticweb.elk.reasoner.indexing.implementation;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2015 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.reasoner.indexing.inferences.IndexedAxiomInferenceVisitor;
import org.semanticweb.elk.reasoner.indexing.inferences.IndexedDefinitionAxiomInferenceVisitor;
import org.semanticweb.elk.reasoner.indexing.inferences.ModifiableIndexedDefinitionAxiomInference;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClass;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpression;

/**
 * Implements {@link ModifiableIndexedDefinitionAxiomInference}
 * 
 * @author "Yevgeny Kazakov"
 */
abstract class ModifiableIndexedDefinitionAxiomInferenceImpl<A extends ElkAxiom>
		extends
			ModifiableIndexedDefinitionAxiomImpl<A>
		implements
			ModifiableIndexedDefinitionAxiomInference {

	protected ModifiableIndexedDefinitionAxiomInferenceImpl(A originalAxiom,
			ModifiableIndexedClass definedClass,
			ModifiableIndexedClassExpression definition) {
		super(originalAxiom, definedClass, definition);
	}

	@Override
	public <I, O> O accept(IndexedAxiomInferenceVisitor<I, O> visitor,
			I input) {
		return accept((IndexedDefinitionAxiomInferenceVisitor<I, O>) visitor,
				input);
	}

}