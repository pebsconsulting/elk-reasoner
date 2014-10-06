/**
 * 
 */
package org.semanticweb.elk.proofs.expressions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.semanticweb.elk.owl.implementation.ElkObjectFactoryImpl;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkObjectFactory;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyChain;
import org.semanticweb.elk.owl.iris.ElkFullIri;
import org.semanticweb.elk.owl.iris.ElkIri;
import org.semanticweb.elk.proofs.expressions.lemmas.ElkLemmaObjectFactory;
import org.semanticweb.elk.proofs.expressions.lemmas.impl.ElkLemmaObjectFactoryImpl;

/**
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public class StructuralEquivalenceTest {

	@Test
	public void entities() {
		ElkObjectFactory factory = new ElkObjectFactoryImpl();
		ElkClass a = factory.getClass(getIri("A"));
		
		assertTrue(StructuralEquivalenceChecker.equal(a, TestEntities.a));
		
		ElkClass b = factory.getClass(getIri("B"));
		// conjunctions, different order
		assertTrue(StructuralEquivalenceChecker.equal(factory.getObjectIntersectionOf(a, b), factory.getObjectIntersectionOf(TestEntities.b, TestEntities.a)));
		assertFalse(StructuralEquivalenceChecker.equal(factory.getObjectIntersectionOf(a, b), factory.getObjectIntersectionOf(TestEntities.b, TestEntities.c)));
		// existentials
		ElkObjectProperty r = factory.getObjectProperty(getIri("R"));
		
		assertTrue(StructuralEquivalenceChecker.equal(factory.getObjectSomeValuesFrom(r, a), factory.getObjectSomeValuesFrom(TestEntities.r, TestEntities.a)));
		// recursion
		assertTrue(StructuralEquivalenceChecker.equal(factory.getObjectSomeValuesFrom(r, factory.getObjectIntersectionOf(a, b)), 
				factory.getObjectSomeValuesFrom(TestEntities.r, factory.getObjectIntersectionOf(TestEntities.b, TestEntities.a))));
		// property chains
		ElkObjectProperty s = factory.getObjectProperty(getIri("S"));
		ElkObjectProperty q = factory.getObjectProperty(getIri("Q"));
		
		assertTrue(StructuralEquivalenceChecker.equal(factory.getObjectPropertyChain(Arrays.asList(r, s, q)), factory.getObjectPropertyChain(Arrays.asList(TestEntities.r, TestEntities.s, TestEntities.q))));
		assertFalse(StructuralEquivalenceChecker.equal(factory.getObjectPropertyChain(Arrays.asList(r, s, q)), factory.getObjectPropertyChain(Arrays.asList(TestEntities.r, TestEntities.q, TestEntities.s))));
		// non-OWL expressions
		ElkLemmaObjectFactory lemmaObjFactory = new ElkLemmaObjectFactoryImpl();
		ElkObjectPropertyChain rs = factory.getObjectPropertyChain(Arrays.asList(r, s));
		
		assertTrue(StructuralEquivalenceChecker.equal(lemmaObjFactory.getComplexObjectSomeValuesFrom(rs, a), 
				lemmaObjFactory.getComplexObjectSomeValuesFrom(factory.getObjectPropertyChain(Arrays.asList(TestEntities.r, TestEntities.s)), TestEntities.a)));
	}

	@Test
	public void axioms() {
		ElkObjectFactory factory = new ElkObjectFactoryImpl();
		ElkClass a = factory.getClass(getIri("A"));
		ElkClass b = factory.getClass(getIri("B"));
		ElkClass c = factory.getClass(getIri("C"));
		ElkObjectProperty r = factory.getObjectProperty(getIri("R"));
		// disjointness
		assertTrue(StructuralEquivalenceChecker.equal(factory.getDisjointClassesAxiom(Arrays.asList(a, b, c)), factory.getDisjointClassesAxiom(Arrays.asList(a, b, c))));
		assertTrue(StructuralEquivalenceChecker.equal(factory.getDisjointClassesAxiom(Arrays.asList(a, b, c)), factory.getDisjointClassesAxiom(Arrays.asList(b, a, c))));
		assertFalse(StructuralEquivalenceChecker.equal(factory.getDisjointClassesAxiom(Arrays.asList(a, a, b, c)), factory.getDisjointClassesAxiom(Arrays.asList(a, b, c))));
		assertTrue(StructuralEquivalenceChecker.equal(factory.getDisjointClassesAxiom(Arrays.asList(a, a, b, c)), factory.getDisjointClassesAxiom(Arrays.asList(a, b, a, c, a, a))));
		// class equivalence
		assertTrue(StructuralEquivalenceChecker.equal(factory.getEquivalentClassesAxiom(Arrays.asList(a, b, c)), factory.getEquivalentClassesAxiom(Arrays.asList(a, b, c))));
		assertTrue(StructuralEquivalenceChecker.equal(factory.getEquivalentClassesAxiom(Arrays.asList(a, a, b, c)), factory.getEquivalentClassesAxiom(Arrays.asList(a, b, c))));
		assertTrue(StructuralEquivalenceChecker.equal(factory.getEquivalentClassesAxiom(Arrays.asList(a, b, c, a)), factory.getEquivalentClassesAxiom(Arrays.asList(c, b, a, b, c))));
		// subsumption
		assertTrue(StructuralEquivalenceChecker.equal(factory.getSubClassOfAxiom(a, b), factory.getSubClassOfAxiom(a, b)));
		// property domain
		assertTrue(StructuralEquivalenceChecker.equal(factory.getObjectPropertyDomainAxiom(r, a), factory.getObjectPropertyDomainAxiom(r, a)));
		// property axioms
		ElkObjectProperty s = factory.getObjectProperty(getIri("S"));
		ElkObjectPropertyChain rs = factory.getObjectPropertyChain(Arrays.asList(r, s));
		
		assertTrue(StructuralEquivalenceChecker.equal(factory.getSubObjectPropertyOfAxiom(r, s), factory.getSubObjectPropertyOfAxiom(r, s)));
		assertTrue(StructuralEquivalenceChecker.equal(factory.getSubObjectPropertyOfAxiom(rs, s), factory.getSubObjectPropertyOfAxiom(rs, s)));
		assertTrue(StructuralEquivalenceChecker.equal(factory.getReflexiveObjectPropertyAxiom(r), factory.getReflexiveObjectPropertyAxiom(r)));
	}
	
	@Test
	public void lemmas() {
		ElkObjectFactory factory = new ElkObjectFactoryImpl();
		ElkClass a = factory.getClass(getIri("A"));
		ElkObjectProperty r = factory.getObjectProperty(getIri("R"));
		ElkObjectProperty s = factory.getObjectProperty(getIri("S"));
		ElkLemmaObjectFactory lemmaObjFactory = new ElkLemmaObjectFactoryImpl();
		ElkObjectPropertyChain rs = factory.getObjectPropertyChain(Arrays.asList(r, s));
		// subclass lemmas
		assertTrue(StructuralEquivalenceChecker.equal(lemmaObjFactory.getComplexSubClassOfAxiom(a, lemmaObjFactory.getComplexObjectSomeValuesFrom(rs, a)), 
				lemmaObjFactory.getComplexSubClassOfAxiom(a, lemmaObjFactory.getComplexObjectSomeValuesFrom(rs, TestEntities.a))));
		// reflexive chains
		assertTrue(StructuralEquivalenceChecker.equal(lemmaObjFactory.getReflexivePropertyChain(rs), lemmaObjFactory.getReflexivePropertyChain(rs)));
		// subchains
		assertTrue(StructuralEquivalenceChecker.equal(lemmaObjFactory.getComplexSubPropertyChainAxiom(rs, rs), lemmaObjFactory.getComplexSubPropertyChainAxiom(rs, rs)));
	}
	
	
	private ElkIri getIri(String fragment) {
		return new ElkFullIri(TestEntities.prefix_.getIri().getFullIriAsString() + fragment);
	}
}
