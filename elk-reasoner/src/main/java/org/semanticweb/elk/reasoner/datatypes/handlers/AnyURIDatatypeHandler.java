/*
 * #%L
 * ELK Reasoner
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner.datatypes.handlers;

import static org.semanticweb.elk.owl.interfaces.ElkDatatype.ELDatatype.*;
import static org.semanticweb.elk.reasoner.datatypes.enums.Facet.LENGTH;
import static org.semanticweb.elk.reasoner.datatypes.enums.Facet.MAX_LENGTH;
import static org.semanticweb.elk.reasoner.datatypes.enums.Facet.MIN_LENGTH;
import static org.semanticweb.elk.reasoner.datatypes.enums.Facet.PATTERN;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.elk.owl.interfaces.ElkDatatypeRestriction;
import org.semanticweb.elk.owl.interfaces.ElkFacetRestriction;
import org.semanticweb.elk.owl.interfaces.ElkLiteral;
import org.semanticweb.elk.owl.interfaces.ElkDatatype.ELDatatype;
import org.semanticweb.elk.reasoner.datatypes.enums.Facet;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.EmptyValueSpace;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.ValueSpace;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.restricted.LengthRestrictedValueSpace;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.restricted.PatternValueSpace;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.values.LiteralValue;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * xsd:AnyURI datatype handler
 * <p>
 * Very similar to {@link PlainLiteralDatatypeHandler} and uses same ValueSpace
 * objects for representation. Value space of anyURI is disjoint with all other
 * datatypes.
 *
 * @author Pospishnyi Olexandr
 * @author "Yevgeny Kazakov"
 */
public class AnyURIDatatypeHandler extends ElkDatatypeHandler {

	static final Logger LOGGER_ = Logger.getLogger(AnyURIDatatypeHandler.class);

	@Override
	public Set<ELDatatype> getSupportedDatatypes() {
		return EnumSet.of(xsd_anyURI);
	}

	@Override
	public Set<Facet> getSupportedFacets() {
		return EnumSet.of(LENGTH, MIN_LENGTH, MAX_LENGTH, PATTERN);
	}

	@Override
	public ValueSpace visit(ElkLiteral elkLiteral) {
		ELDatatype datatype = elkLiteral.getDatatype().asELDatatype();
		URI value = parse(elkLiteral.getLexicalForm());
		if (value != null) {
			return new LiteralValue(value.toString(), datatype, datatype);
		} else {
			return null;
		}
	}

	@Override
	public ValueSpace visit(ElkDatatypeRestriction elkDatatypeRestriction) {
		Integer minLength = 0;
		Integer maxLength = Integer.valueOf(Integer.MAX_VALUE);
		ELDatatype datatype = elkDatatypeRestriction.getDatatype().asELDatatype();

		List<? extends ElkFacetRestriction> facetRestrictions = elkDatatypeRestriction
				.getFacetRestrictions();
		outerloop: for (ElkFacetRestriction facetRestriction : facetRestrictions) {
			Facet facet = Facet.getByIri(facetRestriction
					.getConstrainingFacet().getFullIriAsString());
			String value = facetRestriction.getRestrictionValue()
					.getLexicalForm();

			switch (facet) {
			case LENGTH:
				minLength = Integer.valueOf(value);
				maxLength = minLength;
				break outerloop;
			case MIN_LENGTH:
				minLength = Integer.valueOf(value);
				break;
			case MAX_LENGTH:
				maxLength = Integer.valueOf(value);
				break;
			case PATTERN:
				Automaton pattern = new RegExp(value).toAutomaton();
				PatternValueSpace vs = new PatternValueSpace(pattern, datatype,
						datatype);
				if (vs.isEmptyInterval()) {
					return EmptyValueSpace.INSTANCE;
				} else {
					return vs;
				}
			default:
				LOGGER_.warn("Unsupported facet: " + facet.iri);
				return null;
			}

		}
		LengthRestrictedValueSpace vs = new LengthRestrictedValueSpace(
				datatype, minLength, maxLength);
		if (vs.isEmptyInterval()) {
			return EmptyValueSpace.INSTANCE;
		} else {
			return vs;
		}
	}

	private URI parse(String literal) {
		try {
			return new URI(literal);
		} catch (URISyntaxException ex) {
			LOGGER_.error("Invalid URI: " + literal);
			return null;
		}
	}
}