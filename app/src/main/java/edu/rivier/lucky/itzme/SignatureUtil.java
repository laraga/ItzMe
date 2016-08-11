package edu.rivier.lucky.itzme;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.security.cert.*;

public class SignatureUtil
{
	/* Method to create signature for a given file using private key */
	static byte[] signFile(String privKeyFile, String filePath) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
	{
		
		FileInputStream keyfis = new FileInputStream(privKeyFile);
		byte[] encKey = new byte[keyfis.available()];  
		keyfis.read(encKey);

		keyfis.close();

		PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");//, "SUN");
		PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

		Signature dsa = Signature.getInstance("SHA1withRSA");//, "SUN");
		dsa.initSign(privKey);

		FileInputStream fis = new FileInputStream(filePath);
		BufferedInputStream bufin = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0)
		{
			len = bufin.read(buffer);
			dsa.update(buffer, 0, len);
		};

       bufin.close();
       byte[] realSig = dsa.sign();
	   
	   return realSig;

	}
	
	/* Method to validate given file against signature file using public key */
	static boolean verifySignature(String dir, String certFile, String sigFilePath, String dataFilePath)
			throws FileNotFoundException, IOException, SignatureException, NoSuchAlgorithmException, 
					InvalidKeySpecException, InvalidKeyException, NoSuchProviderException, CertificateException
	{
		boolean result = false;
		
		FileInputStream keyfis = new FileInputStream(dir + "/" + certFile);

		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

		X509Certificate cert = (X509Certificate)certFactory.generateCertificate(keyfis);
		PublicKey pubKey = cert.getPublicKey();

		/*
		byte[] encKey = new byte[keyfis.available()];  
		keyfis.read(encKey);

		keyfis.close();

		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);

		KeyFactory keyFactory = KeyFactory.getInstance("DSA");

		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		*/

		/* input the signature bytes */
		FileInputStream sigfis = new FileInputStream(dir + "/" + sigFilePath);
		byte[] sigToVerify = new byte[sigfis.available()]; 
		sigfis.read(sigToVerify );

		sigfis.close();

		/* create a Signature object and initialize it with the public key */
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(pubKey);

		/* Update and verify the data */

		FileInputStream datafis = new FileInputStream(dir + "/" + dataFilePath);
		BufferedInputStream bufin = new BufferedInputStream(datafis);

		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0) {
			len = bufin.read(buffer);
			sig.update(buffer, 0, len);
		};

		bufin.close();
		
		result = sig.verify(sigToVerify);
		
		return result;
	}
	
	/* Utility method to write bytes into a file */
	public static void writeBytesToFile(byte[] data, String filePath) throws FileNotFoundException, IOException
	{
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(data);
            fos.close();		
	}
	
	/* Sample test program to test above methods */
	/*
	public static void main(String args[]) throws Exception
	{
		//use private key and generate signature for file.txt
		byte[] sigBytes = signFile("privkey", "file.txt");
		//write signature into file signedFile
		writeBytesToFile(sigBytes, "signedFile");
		
		//Verify file.txt against signedFile using pubkey, should succeed
		if(verifySignature("pubkey", "signedFile", "file.txt") == true)
			System.out.println("Success");
		else
			System.out.println("Failed");
		
		//Verify file.txt.gz against signedFile using pubkey, should fail
		if(verifySignature("pubkey", "signedFile", "file.txt.gz") == true)
			System.out.println("Success");
		else
			System.out.println("Failed");
			
	}
	*/
}
