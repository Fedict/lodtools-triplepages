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

import com.codahale.metrics.annotation.ExceptionMetered;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;

/**
 * Storage for companies and organizations (BCE)
 * 
 * @author Bart.Hanssens
 */
@Path("/cbe")
@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES, RDFMediaType.TTL})
public class OrgResource extends RdfResource {
	public final static String PREFIX = "http://org.belgif.be/cbe/";
	public final static String ACTIVITY = "http://www.w3.org/ns/regorg#orgActivity";

	@GET
	@Path("/{type: org|registration|site}/{id}")
	@ExceptionMetered
	public Model getOrganisation(@PathParam("type") String type, @PathParam("id") String id) {
		return getById(PREFIX, type, id);
	}
	
	@GET
	@Path("/_search")
	@ExceptionMetered
	public Model searchOrganisation(@QueryParam("q") String text) {
		return getFTS(text);
	}
	
	@GET
	@Path("/_filter")
	@ExceptionMetered
	public Model searchByNace(@QueryParam("nace") String text) {
		return getFiltered(ACTIVITY, PREFIX, text + "#id");
	}
	
	public OrgResource(Repository repo) {
		super(repo);
	}
}
