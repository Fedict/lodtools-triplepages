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
package be.fedict.lodtools.web.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;

/**
 * RDF Writer
 * 
 * @author Bart.Hanssens
 */
@Provider
@Consumes({RDFMediaType.JSONLD + ";charset=utf-8", 
			RDFMediaType.NTRIPLES + ";charset=utf-8", 
			RDFMediaType.TTL + ";charset=utf-8"})
public class RDFMessageBodyReader implements MessageBodyReader<Model> {
	private final static String BASE = "http://www.fedict.be"; // TODO
	
	@Override
	public boolean isReadable(Class<?> type, Type generic, Annotation[] antns, MediaType mt) {
		return generic == Model.class;
	}

	@Override
	public Model readFrom(Class<Model> type, Type generic, Annotation[] antns, MediaType mt, 
									MultivaluedMap<String, String> mm, InputStream in) 
							throws IOException, WebApplicationException {
		
		RDFFormat fmt = RDFMediaType.getRDFFormat(mt);
		Model m;
		try {
			m = Rio.parse(in, BASE, fmt);
		} catch (RDFHandlerException ex) {
			throw new WebApplicationException(ex);
		}
		if (m.isEmpty()) {
			throw new WebApplicationException(Response.Status.NO_CONTENT);
		}
		return m;
	}
}
