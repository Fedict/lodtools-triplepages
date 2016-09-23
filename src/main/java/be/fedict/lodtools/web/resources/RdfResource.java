/*
 * Copyright (c) 2016, Bart Hanssens <bart.hanssens@fedict.be>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.fedict.lodtools.web.resources;

import be.fedict.lodtools.web.helpers.RDFMediaType;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;


/**
 * Abstract resource querying the RDF triple store.
 * 
 * @author Bart.Hanssens
 */

@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES, RDFMediaType.TTL, MediaType.TEXT_HTML})
public abstract class RdfResource {
	private final Repository repo;
	private final ValueFactory fac;
	
	private final static String Q_IRI = 
			"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
	
	private final static String Q_FTS = 
			"PREFIX luc: <http://www.ontotext.com/owlim/lucene#> "
			+ "CONSTRUCT { ?s ?p ?o } WHERE { "
			+ "?o luc:myIndex ?fts . "
			+ "?s ?p ?o } "
			+ "LIMIT 100";
		
	/**
	 * Get string as URI
	 * 
	 * @param uri
	 * @return URI representation
	 */
	protected IRI asURI(String uri) {
		return fac.createIRI(uri);
	}
	
	/**
	 * Get string as RDF literal
	 * 
	 * @param lit
	 * @return literal 
	 */
	protected Literal asLiteral(String lit) {
		return fac.createLiteral(lit);
	}
	
	/**
	 * Prepare a SPARQL query
	 * @param qry
	 * @param bindings bindings (if any)
	 * @return 
	 */
	protected Model prepare(String qry, Map<String,Value> bindings) {
		try {
			RepositoryConnection conn = this.repo.getConnection();
			GraphQuery gq = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);
			bindings.forEach((k,v) -> gq.setBinding(k, v));
			
			Model m = QueryResults.asModel(gq.evaluate());
			conn.close();
			
			m.setNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
			m.setNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
			m.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
			m.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
			m.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
			
			return m;
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Get by ID (URI)
	 * 
	 * @param prefix
	 * @param type
	 * @param id
	 * @return RDF model 
	 */
	protected Model getById(String prefix, String type, String id) {
		Map<String,Value> map = new HashMap();
		map.put("s", asURI(prefix + type + "/" + id + "#id"));
		return prepare(Q_IRI, map);
	}
	
	/**
	 * Full text search
	 * 
	 * @param prefix
	 * @param type
	 * @param text
	 * @return RDF model 
	 */
	protected Model getFTS(String prefix, String type, String text) {
		Map<String,Value> map = new HashMap();
		map.put("fts", asLiteral(text + "*"));
		return prepare(Q_FTS, map);
	}
	
	/**
	 * Constructor
	 * 
	 * @param repo 
	 */
	public RdfResource(Repository repo) {
		this.repo = repo;
		this.fac = repo.getValueFactory();
	}
}

