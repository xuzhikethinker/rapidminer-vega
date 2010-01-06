/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.tools.cipher;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.rapidminer.io.Base64;
//
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 * This class can be used to encrypt and decrypt given strings
 * based on a key generated by the user. Please note that classes
 * using this tool class should first ensure that a user key is available
 * by invoking the method isKeyAvailable().
 *
 * @author Ingo Mierswa
 */
public class CipherTools {
	
	private static final String CIPHER_TYPE = "DESede";
	
	public static boolean isKeyAvailable() {
		try {
			KeyGeneratorTool.getUserKey();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static String encrypt(String text) throws CipherException {
		Key key = null;
		try {
			key = KeyGeneratorTool.getUserKey();
		} catch (IOException e) {
			throw new CipherException("Cannot retrieve key, probably no one was created: " + e.getMessage());
		}
		
        try { 
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE); 
            cipher.init(Cipher.ENCRYPT_MODE, key); 

            byte[] outputBytes = cipher.doFinal(text.getBytes()); 

            //BASE64Encoder encoder = new BASE64Encoder(); 
            //String base64 = encoder.encode(outputBytes); 
            String base64 = Base64.encodeBytes(outputBytes);
            return base64;  
        } catch (NoSuchAlgorithmException e) {
            throw new CipherException( "Failed to encrypt text: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
            throw new CipherException( "Failed to encrypt text: " + e.getMessage());
		} catch (InvalidKeyException e) {
            throw new CipherException( "Failed to encrypt text: " + e.getMessage());
		} catch (IllegalBlockSizeException e) {
            throw new CipherException( "Failed to encrypt text: " + e.getMessage());
		} catch (BadPaddingException e) {
            throw new CipherException( "Failed to encrypt text: " + e.getMessage());
		}
	}
	
	public static String decrypt(String text) throws CipherException {
		Key key = null;
		try {
			key = KeyGeneratorTool.getUserKey();
		} catch (IOException e) {
			throw new CipherException("Cannot retrieve key, probably no one was created: " + e.getMessage());
		}
		
        try { 
            //BASE64Decoder decoder = new BASE64Decoder();
        	//byte[] encrypted = decoder.decodeBuffer(text);
        	byte[] encrypted = Base64.decode(text);             

            Cipher cipher = Cipher.getInstance(CIPHER_TYPE); 
            cipher.init(Cipher.DECRYPT_MODE, key); 

            byte[] outputBytes = cipher.doFinal(encrypted); 
            String ret = new String(outputBytes); 
            return ret; 
        } catch (NoSuchAlgorithmException e) {
            throw new CipherException( "Failed to decrypt text: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
            throw new CipherException( "Failed to decrypt text: " + e.getMessage());
		} catch (IOException e) {
            throw new CipherException( "Failed to decrypt text: " + e.getMessage());
		} catch (InvalidKeyException e) {
            throw new CipherException( "Failed to decrypt text: " + e.getMessage());
		} catch (IllegalBlockSizeException e) {
            throw new CipherException( "Failed to decrypt text: " + e.getMessage());
		} catch (BadPaddingException e) {
            throw new CipherException( "Failed to decrypt text: " + e.getMessage());
		}
	}
}
