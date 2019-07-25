package com.goodforgoodbusiness.endpoint.dht.share;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.shared.TripleUtil;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the detail of a share request
 */
public class ShareRequest {
	private static final byte [] ANY_BYTES = new byte [] { 0, 0, 0 };
	
	@Expose
	@SerializedName("start")
	private ZonedDateTime start;
		
	@Expose
	@SerializedName("end")
	private ZonedDateTime end;
	
	@Expose
	@SerializedName("sub")
	private String subject;
	
	@Expose
	@SerializedName("pre")
	private String predicate;
	
	@Expose
	@SerializedName("obj")
	private String object;
	
	public ShareRequest() {
	}
	
	public Optional<ZonedDateTime> getStart() {
		return Optional.ofNullable(start);
	}

	public void setStart(ZonedDateTime start) {
		this.start = start;
	}
	
	public Optional<ZonedDateTime> getEnd() {
		return Optional.ofNullable(end);
	}

	public void setEnd(ZonedDateTime end) {
		this.end = end;
	}
	
	public Optional<String> getSubject() {
		return Optional.ofNullable(subject);
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Optional<String> getPredicate() {
		return Optional.ofNullable(predicate);
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public Optional<String> getObject() {
		return Optional.ofNullable(object);
	}

	public void setObject(String object) {
		this.object = object;
	}
	
	/**
	 * Set s/p/o from {@link Triple}
	 */
	public ShareRequest setTriple(Triple triple) {
		this.subject = TripleUtil.valueOf(triple.getSubject()).orElse(null);
		this.predicate = TripleUtil.valueOf(triple.getPredicate()).orElse(null);
		this.object = TripleUtil.valueOf(triple.getObject()).orElse(null);
		
		return this;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.subject, this.predicate, this.object);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof ShareRequest) {
			ShareRequest oo = (ShareRequest)o;
			return 
				Objects.equal(this.subject, oo.subject) && 
				Objects.equal(this.predicate, oo.predicate) && 
				Objects.equal(this.object, oo.object)
			;
		}
		
		return false;
	}
	
	/**
	 * Encode triple, values only (since sharekeys are shared with us as values only).
	 */
	public byte [] toByteArray() {
		var sBytes = getSubject().map(String::getBytes).orElse(ANY_BYTES);
		var pBytes = getPredicate().map(String::getBytes).orElse(ANY_BYTES);
		var oBytes = getObject().map(String::getBytes).orElse(ANY_BYTES);
		
		var tBytes = new byte[sBytes.length + pBytes.length + oBytes.length];
		
		var pos = 0;
		
		System.arraycopy(sBytes, 0, tBytes, pos, sBytes.length);
		pos += sBytes.length;
		
		System.arraycopy(pBytes, 0, tBytes, pos, pBytes.length);
		pos += pBytes.length;
		
		System.arraycopy(oBytes, 0, tBytes, pos, oBytes.length);
		
		return tBytes;
	}
	
	@Override
	public String toString() {
		return "ShareRequest(s=" + subject + " p=" + predicate + " o=" + object + ")";
	}
}


		