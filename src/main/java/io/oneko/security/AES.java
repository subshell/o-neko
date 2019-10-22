package io.oneko.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AES {
	private final Key key;

	public AES(@Value("${o-neko.security.credentialsCoderKey}") final String password) {
		byte[] ivBytes = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivBytes);
		this.key = generateKey(password);
	}

	public String encrypt(String data) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encVal = c.doFinal(data.getBytes());
			return new String(Base64.getEncoder().encode(encVal));
		} catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException e) {
			throw new CryptoException(e);
		}
	}

	public String decrypt(String encryptedData) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
			byte[] decValue = c.doFinal(decordedValue);
			return new String(decValue);
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
			throw new CryptoException(e);
		}
	}

	/**
	 * Creates a key using the passphrase for an AES encryption.
	 *
	 * @param keyPhrase key phrase of any size
	 * @return key
	 */
	private Key generateKey(String keyPhrase) {
		return new SecretKeySpec(DigestUtils.md5(keyPhrase), "AES");
	}

	static class CryptoException extends RuntimeException {
		public CryptoException() {
		}

		public CryptoException(String message) {
			super(message);
		}

		public CryptoException(String message, Throwable cause) {
			super(message, cause);
		}

		public CryptoException(Throwable cause) {
			super(cause);
		}

		public CryptoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}

}
