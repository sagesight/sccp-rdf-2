package com.colabriq.endpoint.crypto.primitive;

import com.colabriq.endpoint.crypto.AsymmetricEncryption;
import com.colabriq.endpoint.crypto.key.EncodeablePrivateKey;
import com.colabriq.endpoint.crypto.key.EncodeablePublicKey;

public class AsymmetricEncryptionTest {
	public static void main(String[] args) throws Exception {
		var keyPair = AsymmetricEncryption.createKeyPair();
		
		System.out.println(keyPair.getPrivate().toEncodedString());
		System.out.println(keyPair.getPublic().toEncodedString());
		
		var privateKey = new EncodeablePrivateKey( keyPair.getPrivate().toEncodedString() );
		var signature = AsymmetricEncryption.sign("hello world", privateKey );
		System.out.println(signature);

		var publicKey = new EncodeablePublicKey( keyPair.getPublic().toEncodedString() );
		var result = AsymmetricEncryption.verify("hello world", signature, publicKey );
		System.out.println(result);
	}
}
