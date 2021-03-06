/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.manchester.cs.owl.owlapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObject;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.SWRLVariableExtractor;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 15-Jan-2007<br><br>
 */
public class SWRLRuleImpl extends OWLLogicalAxiomImpl implements SWRLRule {


	private static final long serialVersionUID = 30402L;

    private final Set<SWRLAtom> head;

    private final Set<SWRLAtom> body;

    private Set<SWRLVariable> variables;

    private Boolean containsAnonymousClassExpressions = null;

    private Set<OWLClassExpression> classAtomsPredicates;


    @SuppressWarnings("javadoc")
	public SWRLRuleImpl(Set<? extends SWRLAtom> body, Set<? extends SWRLAtom> head, Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
        this.head = new TreeSet<SWRLAtom>(head);
        this.body = new TreeSet<SWRLAtom>(body);
    }


    @Override
    public SWRLRule getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getSWRLRule(getBody(), getHead());
    }

    @Override
    public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getSWRLRule(getBody(), getHead());
    }

    @SuppressWarnings("javadoc")
	public SWRLRuleImpl(Set<? extends SWRLAtom> body, Set<? extends SWRLAtom> head) {
        this(body, head, new ArrayList<OWLAnnotation>(0));
    }


    @Override
    public Set<SWRLVariable> getVariables() {
        if (variables == null) {
            Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
            SWRLVariableExtractor extractor = new SWRLVariableExtractor();
            accept(extractor);
            vars.addAll(extractor.getVariables());
            variables = new HashSet<SWRLVariable>(vars);
        }
        return variables;
    }

    @Override
    public boolean containsAnonymousClassExpressions() {
        if (containsAnonymousClassExpressions == null) {
            for (SWRLAtom atom : head) {
                if (atom instanceof SWRLClassAtom) {
                    if (((SWRLClassAtom) atom).getPredicate().isAnonymous()) {
                        containsAnonymousClassExpressions = Boolean.TRUE;
                        break;
                    }
                }
            }
            if (containsAnonymousClassExpressions == null) {
                for (SWRLAtom atom : body) {
                    if (atom instanceof SWRLClassAtom) {
                        if (((SWRLClassAtom) atom).getPredicate().isAnonymous()) {
                            containsAnonymousClassExpressions = Boolean.TRUE;
                            break;
                        }
                    }
                }
            }
            if (containsAnonymousClassExpressions == null) {
                containsAnonymousClassExpressions = Boolean.FALSE;
            }
        }
        return containsAnonymousClassExpressions.booleanValue();
    }


    @Override
    public Set<OWLClassExpression> getClassAtomPredicates() {
        if (classAtomsPredicates == null) {
            Set<OWLClassExpression> predicates = new HashSet<OWLClassExpression>();
            for (SWRLAtom atom : head) {
                if (atom instanceof SWRLClassAtom) {
                    predicates.add(((SWRLClassAtom) atom).getPredicate());
                }
            }
            for (SWRLAtom atom : body) {
                if (atom instanceof SWRLClassAtom) {
                    predicates.add(((SWRLClassAtom) atom).getPredicate());
                }
            }
            classAtomsPredicates = new HashSet<OWLClassExpression>(predicates);
        }
        return classAtomsPredicates;
    }


    @Override
    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(SWRLObjectVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <O> O accept(SWRLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    /**
     * Gets the atoms in the antecedent
     * @return A set of <code>SWRLAtom</code>s, which represent the atoms
     *         in the antecedent of the rule.
     */
    @Override
    public Set<SWRLAtom> getBody() {
        return CollectionFactory.getCopyOnRequestSetFromImmutableCollection(body);
    }


    /**
     * Gets the atoms in the consequent.
     * @return A set of <code>SWRLAtom</code>s, which represent the atoms
     *         in the consequent of the rule
     */
    @Override
    public Set<SWRLAtom> getHead() {
        return CollectionFactory.getCopyOnRequestSetFromImmutableCollection(head);
    }


    @Override
    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    /**
     * If this rule contains atoms that have predicates that are inverse object properties, then this method
     * creates and returns a rule where the arguments of these atoms are fliped over and the predicate is the
     * inverse (simplified) property
     * @return The rule such that any atoms of the form  inverseOf(p)(x, y) are transformed to p(x, y).
     */
    @Override
    public SWRLRule getSimplified() {
        return (SWRLRule) this.accept(ATOM_SIMPLIFIER);
    }

    /**
     * Determines if this axiom is a logical axiom. Logical axioms are defined to be
     * axioms other than declaration axioms (including imports declarations) and annotation
     * axioms.
     * @return <code>true</code> if the axiom is a logical axiom, <code>false</code>
     *         if the axiom is not a logical axiom.
     */
    @Override
    public boolean isLogicalAxiom() {
        return true;
    }

    @Override
	public boolean equals(Object obj) {
    	if(super.equals(obj)) {
        if (!(obj instanceof SWRLRule)) {
            return false;
        }
        SWRLRule other = (SWRLRule) obj;
        return other.getBody().equals(body) && other.getHead().equals(head);
    	}
    	return false;
    }


    @Override
    public AxiomType<?> getAxiomType() {
        return AxiomType.SWRL_RULE;
    }


    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        SWRLRule other = (SWRLRule) object;

        int diff = compareSets(getBody(), other.getBody());
        if (diff == 0) {
            diff = compareSets(getHead(), other.getHead());
        }
        return diff;

    }

    protected final AtomSimplifier ATOM_SIMPLIFIER = new AtomSimplifier();

    protected class AtomSimplifier implements SWRLObjectVisitorEx<SWRLObject> {

        @Override
        public SWRLRule visit(SWRLRule node) {
            Set<SWRLAtom> nodebody = new HashSet<SWRLAtom>();
            for (SWRLAtom atom : node.getBody()) {
                nodebody.add((SWRLAtom) atom.accept(this));
            }
            Set<SWRLAtom> nodehead = new HashSet<SWRLAtom>();
            for (SWRLAtom atom : node.getHead()) {
                nodehead.add((SWRLAtom) atom.accept(this));
            }
            return getOWLDataFactory().getSWRLRule(nodebody, nodehead);
        }

        @Override
        public SWRLClassAtom visit(SWRLClassAtom node) {
            return node;
        }

        @Override
        public SWRLDataRangeAtom visit(SWRLDataRangeAtom node) {
            return node;
        }

        @Override
        public SWRLObjectPropertyAtom visit(SWRLObjectPropertyAtom node) {
            return node.getSimplified();
        }

        @Override
        public SWRLDataPropertyAtom visit(SWRLDataPropertyAtom node) {
            return node;
        }

        @Override
        public SWRLBuiltInAtom visit(SWRLBuiltInAtom node) {
            return node;
        }

        @Override
        public SWRLVariable visit(SWRLVariable node) {
            return node;
        }

        @Override
        public SWRLIndividualArgument visit(SWRLIndividualArgument node) {
            return node;
        }

        @Override
        public SWRLLiteralArgument visit(SWRLLiteralArgument node) {
            return node;
        }

        @Override
        public SWRLSameIndividualAtom visit(SWRLSameIndividualAtom node) {
            return node;
        }

        @Override
        public SWRLDifferentIndividualsAtom visit(SWRLDifferentIndividualsAtom node) {
            return node;
        }
    }
}
