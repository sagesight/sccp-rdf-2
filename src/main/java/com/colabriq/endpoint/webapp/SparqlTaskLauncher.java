package com.colabriq.endpoint.webapp;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.colabriq.endpoint.processor.ModelTaskResult;
import com.colabriq.endpoint.processor.task.ImportStreamTask;
import com.colabriq.endpoint.processor.task.Importer;
import com.colabriq.endpoint.processor.task.QueryTask;
import com.colabriq.endpoint.processor.task.UpdateTask;
import com.colabriq.vertx.stream.InputWriteStream;
import com.colabriq.webapp.ContentType;
import com.colabriq.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

/**
 * Common query/update methods for SPARQL queries
 */
@Singleton
public class SparqlTaskLauncher {
	private static final Logger log = Logger.getLogger(SparqlTaskLauncher.class);
	
	protected final ExecutorService service;
	protected final Dataset dataset;
	protected final Importer importer;
	
	@Inject
	public SparqlTaskLauncher(ExecutorService service, Dataset dataset, Importer importer) {
		this.service = service;
		this.dataset = dataset;
		this.importer = importer;
	}
	
	/**
	 * Run a 'query' SPARQL query
	 */
	public void query(RoutingContext ctx, Buffer sparqlStmt) {
		String acceptHeader = ctx.request().getHeader(HttpHeaders.ACCEPT);
		
		var responseContentType = MIMEMappings.getContentType(Optional.ofNullable(acceptHeader));
		if (responseContentType.isPresent()) {
			log.info("Query accept = " + acceptHeader + ", reply= " + responseContentType.get());
			
			ctx.response().setStatusCode(200);
			ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, responseContentType.get());
			ctx.response().setChunked(true);
			
			service.submit(new QueryTask(
				dataset,
				responseContentType.get(),
				sparqlStmt.toString(),
				ctx.response(),
				Future.<Long>future().setHandler(result -> {
					if (result.failed()) {
						ctx.fail(result.cause());
					}
					else {
						ctx.response().end();
//						ctx.next();
					}
				})
			));
		}
		else {
			ctx.fail(new BadRequestException("Must specify a valid accept header"));
		}
	}
	
	/**
	 * As {@link #query(RoutingContext, Buffer)} but with a String
	 */
	public void query(RoutingContext ctx, String stmt) {
		query(ctx, Buffer.buffer(stmt.getBytes()));
	}
	
	/**
	 * Run a 'update' SPARQL query
	 */
	public void update(RoutingContext ctx, Buffer stmt) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		service.submit(new UpdateTask(
			dataset,
			stmt.toString(),
			Future.<ModelTaskResult>future().setHandler(result -> {
				if (result.failed()) {
					ctx.fail(result.cause());
				}
				else {
					ctx.response().end(result.result().toJson());
				}
			})
		));
	}
	
	/**
	 * As {@link #update(RoutingContext, Buffer)} but with a String
	 */
	public void update(RoutingContext ctx, String stmt) {
		update(ctx, Buffer.buffer(stmt.getBytes()));
	}
	
	/**
	 * Starts an import from an uploaded file
	 */
	public void importFile(RoutingContext ctx, String lang, AsyncFile file) {
		// pipe the file into our InputWriteStream
		InputWriteStream iws;
		
		try {
			iws = new InputWriteStream();
			file.pipeTo(iws);
		}
		catch (IOException e) {
			ctx.fail(e);
			return;
		}
		
		service.submit(
			new ImportStreamTask(
			    importer,
			    iws.getInputStream(),
			    lang,
			    false,
				Future.<ModelTaskResult>future().setHandler(result -> {
					file.close();
					
					if (result.failed()) {
						ctx.fail(result.cause());
					}
					else {
						// standard JSON result
						ctx.response().end(result.result().toJson());
					}
				})
			)
		);
	}
}
