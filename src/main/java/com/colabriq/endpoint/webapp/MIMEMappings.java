package com.colabriq.endpoint.webapp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.sparql.resultset.ResultsFormat;

import com.colabriq.shared.MIMEParse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class MIMEMappings {
	public static List<String> RESULT_MIME_TYPES = 
		ImmutableList.<String>builder()
			.add("application/sparql-results+xml")
			.add("application/sparql-results+json")
			.add("application/rdf+xml")
			.add("application/xml")
			.add("application/json")
			.build();
	
	public static Map<String, ResultsFormat> RESULT_MIME_MAP = 
		ImmutableMap.<String, ResultsFormat>builder()
			.put("application/sparql-results+xml", ResultsFormat.FMT_RS_XML)
			.put("application/sparql-results+json", ResultsFormat.FMT_RS_JSON)
			.put("application/rdf+xml", ResultsFormat.FMT_RS_XML)
			.put("application/xml", ResultsFormat.FMT_RS_XML)
			.put("application/json", ResultsFormat.FMT_RS_JSON)
			.build();
	
	public static Map<String, String> RESULT_LANG_MAP =
		ImmutableMap.<String, String>builder()
			.put("application/rdf+xml", "RDF/XML")
			.put("application/xml", "RDF/XML")
			.build();
	
	public static Map<String, String> FILE_TYPES = 
		ImmutableMap.<String, String>builder()
			.put("ttl", "TURTLE")
			.put("xml", "RDF/XML")
			.put("n3", "N-TRIPLE")
			.put("rdf", "RDF/XML")
			.build();
	
	public static Map<String, String> CONTENT_TYPES = 
		ImmutableMap.<String, String>builder()
			.put("ttl", "text/turtle")
			.put("xml", "application/rdf+xml")
			.put("n3", "text/n3")
			.put("rdf", "application/rdf+xml")
			.build();
	
	private static final String DEFAULT_RESULT_MIME_TYPE = "application/sparql-results+xml";
	
	public static Optional<String> getContentType(Optional<String> acceptHeader) {
		if (acceptHeader.isPresent()) {
			String contentType = MIMEParse.bestMatch(RESULT_MIME_TYPES, acceptHeader.get());
			
			if (contentType == null || contentType.length() == 0) {
				return Optional.empty();
			}
			else {
				return Optional.of(contentType);
			}
		}
		else {
			return Optional.of(DEFAULT_RESULT_MIME_TYPE);
		}
	}
	
	public static ResultsFormat getResultsFormat(String contentType) {
		return RESULT_MIME_MAP.get(contentType);
	}
	
	public static String getResultsLang(String contentType) {
		return RESULT_LANG_MAP.get(contentType);
	}
}
