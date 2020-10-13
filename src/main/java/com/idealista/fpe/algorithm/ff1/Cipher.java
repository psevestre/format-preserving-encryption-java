package com.idealista.fpe.algorithm.ff1;

import com.idealista.fpe.component.functions.prf.PseudoRandomFunction;

public class Cipher implements com.idealista.fpe.algorithm.Cipher {
	
	private static final int DEFAULT_ROUNDS = 10;
	
	private final int rounds;
	
	public Cipher() {
		this.rounds = DEFAULT_ROUNDS;
	}
	
	public Cipher(int rounds) {
		this.rounds = rounds;
	}


    @Override
    public int[] encrypt(int[] plainText, Integer radix, byte[] tweak, PseudoRandomFunction pseudoRandomFunction) {
        return FF1Algorithm.encrypt(plainText, radix, tweak, pseudoRandomFunction, this.rounds);
    }

    @Override
    public int[] decrypt(int[] cipherText, Integer radix, byte[] tweak, PseudoRandomFunction pseudoRandomFunction) {
        return FF1Algorithm.decrypt(cipherText, radix, tweak, pseudoRandomFunction, this.rounds);
    }
}
