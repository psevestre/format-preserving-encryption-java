/**
 * 
 */
package com.idealista.fpe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.idealista.fpe.builder.FormatPreservingEncryptionBuilder;
import com.idealista.fpe.config.Alphabet;
import com.idealista.fpe.config.GenericDomain;
import com.idealista.fpe.config.GenericTransformations;
import com.idealista.fpe.config.LengthRange;

/**
 * @author Philippe
 *
 */
public class PerformanceTestsShould {
	
	@Test
	public void encrypt10000() {
		
		int COUNT = 10000;
		byte[] key = "The quick brown fox jumps over the lazy dog".substring(0,32).getBytes();
		byte[] tweak = "2020-08-31".getBytes();
		
		Alphabet abc = new StandardAlphabet();
		GenericDomain domain = new GenericDomain(
				abc,
				new GenericTransformations(abc.availableCharacters()), 
				new GenericTransformations(abc.availableCharacters())); 
		
		FormatPreservingEncryption fpe = FormatPreservingEncryptionBuilder
		  .ff1Implementation(13) // 13 rounds
		  .withDomain(domain)
		  .withDefaultPseudoRandomFunction(key)
		  .withLengthRange(new LengthRange(2, 64))
		  .build();
		
		long start = System.currentTimeMillis();
		String plain = "JOAO-Carlos Nogueira Silvestre JR 33";
		String encrypted = null;
		for ( int i = 0 ; i < COUNT ; i++ ) {
			encrypted = fpe.encrypt(plain, tweak);
		}
		
		long encryptElapsed = System.currentTimeMillis() - start;
		System.out.println(String.format("encrypt: elapsed=%d, tps=%5.2f",encryptElapsed, (COUNT*1000.0)/encryptElapsed));
		
		long decryptStart = System.currentTimeMillis();
		String decrypted = null;
		for( int i = 0 ; i < COUNT ; i++ ) {
			decrypted = fpe.decrypt(encrypted, tweak);
		}
		
		long decryptElapsed = System.currentTimeMillis() - decryptStart;
		System.out.println(String.format("decrypt: elapsed=%d, tps=%5.2f", decryptElapsed, (COUNT*1000.0)/decryptElapsed));

		System.out.println("plainTest: " + plain);
		System.out.println("encrypted: " + encrypted);
		
		assertEquals(plain, decrypted);
		
	}
	
	static class StandardAlphabet implements Alphabet {
		
		private static final char[] ALPHABET = new char[] {
			'-',' ','@',
			'Q','W','E','R','T','Y','U','I','O','P',
			'A','S','D','F','G','H','J','K','L',
			'Z','X','C','V','B','N','M',
			'1','2','3','4','5','6','7','8','9','0',
			'q','w','e','r','t','y','u','i','o','p',
			'a','s','d','f','g','h','j','k','l',
			'z','x','c','v','b','n','m',
		};

		@Override
		public char[] availableCharacters() {
			return ALPHABET;
		}

		@Override
		public Integer radix() {
			// TODO Auto-generated method stub
			return ALPHABET.length;
		}
		
	}
	
	static class NumericAlphabet implements Alphabet {
		
		private static final char[] ALPHABET = new char[] {
			'1','2','3','4','5','6','7','8','9','0'
		};

		@Override
		public char[] availableCharacters() {
			return ALPHABET;
		}

		@Override
		public Integer radix() {
			// TODO Auto-generated method stub
			return ALPHABET.length;
		}
		
	}
	

}
