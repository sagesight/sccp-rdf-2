package com.colabriq.endpoint.dht.keys;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.crypto.key.EncodeableShareKey;
import com.colabriq.endpoint.dht.share.SharePattern;
import com.colabriq.endpoint.dht.share.ShareResponse;
import com.colabriq.kpabe.KPABEEncryption;
import com.colabriq.kpabe.KPABEKeyManager;
import com.colabriq.shared.encode.JSON;

public class ShareRequestTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		var kpabe = KPABEEncryption.getInstance(keys);
		var esk = new EncodeableShareKey(kpabe.shareKey("foo"));
		
		var p1 = new SharePattern(new Triple(
			createURI("https://twitter.com/ijmad8x"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
		));
		
		var sr1 = new ShareResponse();
		sr1.setPattern(p1);
		sr1.setKey(esk);
		
		var j1 = JSON.encodeToString(sr1);
		System.out.println(j1);
		
		var sr1a = JSON.decode(j1, ShareResponse.class);
		System.out.println(sr1a);
		
		var p2 = new SharePattern(new Triple(
			createURI("https://twitter.com/ijmad8x"),
			Node.ANY,
			Node.ANY
		));
		
		var sr2 = new ShareResponse();
		sr2.setPattern(p2);
		sr2.setKey(esk);
		
		var j2 = JSON.encodeToString(sr2);
		System.out.println(j2);
		
		var sr2a = JSON.decode(j2, ShareResponse.class);
		System.out.println(sr2a);
	}
}
