package org.semanticweb.elk.reasoner.indexing.visitors;

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

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedFiller;
import org.semanticweb.elk.reasoner.saturation.IndexedContextRoot;

/**
 * An {@link IndexedContextRootVisitor} that always returns {@code null}.
 * 
 * @author "Yevgeny Kazakov"
 *
 * @param <O>
 */
public class NoOpIndexedContextRootVisitor<O> extends
		NoOpIndexedClassExpressionVisitor<O> implements
		IndexedContextRootVisitor<O> {

	@SuppressWarnings("unused")
	protected O defaultVisit(IndexedContextRoot element) {
		return null;
	}

	@Override
	protected O defaultVisit(IndexedClassExpression element) {
		return defaultVisit((IndexedContextRoot) element);
	}

	@Override
	public O visit(IndexedFiller element) {
		return defaultVisit(element);
	}

}