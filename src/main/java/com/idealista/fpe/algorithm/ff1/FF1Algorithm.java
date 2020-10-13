package com.idealista.fpe.algorithm.ff1;

import static com.idealista.fpe.component.functions.ComponentFunctions.num;
import static com.idealista.fpe.component.functions.ComponentFunctions.stringOf;
import static com.idealista.fpe.component.functions.DataFunctions.*;
import static java.lang.Math.ceil;

import java.math.BigInteger;
import java.util.Arrays;

import com.idealista.fpe.component.functions.prf.PseudoRandomFunction;
import com.idealista.fpe.data.ByteString;
import com.idealista.fpe.data.IntString;

class FF1Algorithm {

    private static final int NUMBER_OF_ROUNDS = 10;

    private FF1Algorithm (){}

    static int[] encrypt(int[] plainText, Integer radix, byte[] tweak, PseudoRandomFunction pseudoRandomFunction ) {
    	return encrypt(plainText, radix, tweak, pseudoRandomFunction, NUMBER_OF_ROUNDS);
    }
    
    static int[] encrypt(int[] plainText, Integer radix, byte[] tweak, PseudoRandomFunction pseudoRandomFunction,int rounds) {
        IntString target = new IntString(plainText);
        int leftSideLength = target.leftSideLength();
        int rightSideLength = target.rightSideLength();
        int lengthOfLeftAfterEncoded = (int) ceil(ceil(rightSideLength * log(radix)) / 8.0);
        int paddingToEnsureFeistelOutputIsBigger = (int) (4 * ceil(lengthOfLeftAfterEncoded / 4.0) + 4);
        ByteString padding = generateInitialPadding(radix, target.length(), tweak.length, leftSideLength);

        int[] left = target.left();
        int[] right = target.right();
        for (int round=0; round< rounds; round++) {
            BigInteger roundNumeral = roundNumeral(num(right, radix), tweak, padding, pseudoRandomFunction, lengthOfLeftAfterEncoded, paddingToEnsureFeistelOutputIsBigger, round);
            int partialLength = round % 2 == 0 ? leftSideLength : rightSideLength;
            BigInteger partialNumeral = num(left, radix).add(roundNumeral).mod(BigInteger.valueOf(radix).pow(partialLength));
            int[] partialBlock = stringOf(partialLength, radix, partialNumeral);
            left = right;
            right = partialBlock;
        }
        return concatenate(left, right);
    }

    static int[] decrypt(int[] cipherText, Integer radix, byte[] tweak, PseudoRandomFunction pseudoRandomFunction) {
    	return decrypt(cipherText, radix, tweak, pseudoRandomFunction, NUMBER_OF_ROUNDS);
    }
    
    static int[] decrypt(int[] cipherText, Integer radix, byte[] tweak, PseudoRandomFunction pseudoRandomFunction, int rounds) {
        IntString target = new IntString(cipherText);
        int leftSideLength = target.leftSideLength();
        int rightSideLength = target.rightSideLength();
        int lengthOfLeftAfterEncoded = (int) ceil(ceil(rightSideLength * log(radix)) / 8.0);
        int paddingToEnsureFeistelOutputIsBigger = (int) (4 * ceil(lengthOfLeftAfterEncoded / 4.0) + 4);
        ByteString padding = generateInitialPadding(radix, target.length(), tweak.length, leftSideLength);

        int[] left = target.left();
        int[] right = target.right();
        for (int round=rounds-1; round>=0; round--) {
            BigInteger roundNumeral = roundNumeral(num(left, radix), tweak, padding, pseudoRandomFunction, lengthOfLeftAfterEncoded, paddingToEnsureFeistelOutputIsBigger, round);
            int partialLength = round % 2 == 0 ? leftSideLength : rightSideLength;
            BigInteger partialNumeral = num(right, radix).subtract(roundNumeral).mod(BigInteger.valueOf(radix).pow(partialLength));
            int[] partialBlock = stringOf(partialLength, radix, partialNumeral);
            right = left;
            left = partialBlock;
        }
        return concatenate(left, right);
    }

    private static BigInteger roundNumeral(BigInteger targetBlockNumeral, byte[] tweak, ByteString padding, PseudoRandomFunction pseudoRandomFunction, int lengthOfLeftAfterEncoded, int paddingToEnsureFeistelOutputIsBigger, int round) {
        ByteString q = generateQ(new ByteString(tweak), targetBlockNumeral, lengthOfLeftAfterEncoded, round);
        ByteString roundBlock = roundFunction(pseudoRandomFunction, paddingToEnsureFeistelOutputIsBigger, padding, q);
        return num(Arrays.copyOf(roundBlock.raw(), paddingToEnsureFeistelOutputIsBigger));
    }

    private static ByteString roundFunction(PseudoRandomFunction pseudoRandomFunction, int paddingToEnsureFeistelOutputIsBigger, ByteString padding, ByteString q) {
        byte[] r = pseudoRandomFunction.apply(padding.concatenate(q).raw());
        ByteString s = new ByteString(r);
        for (int j = 1; j <= ceil(paddingToEnsureFeistelOutputIsBigger / 16.0) - 1; j++) {
            s = s.concatenate(new ByteString(pseudoRandomFunction.apply(xor(r, numberAsArrayOfBytes(j, 16).getData()))));
        }
        return s;
    }

    private static ByteString generateQ(ByteString tweak, BigInteger targetSideNumeral, int lengthOfLeftAfterEncoded, int round) {
        return tweak
                .concatenate(numberAsArrayOfBytes(0, mod(- tweak.length() - lengthOfLeftAfterEncoded - 1, 16)))
                .concatenate(numberAsArrayOfBytes(round, 1))
                .concatenate(numberAsArrayOfBytes(targetSideNumeral, lengthOfLeftAfterEncoded));
    }

    private static ByteString generateInitialPadding(Integer radix, int textLength, int tweakLength, int leftSideLength) {
        return new ByteString(new byte[]{ (byte) 0x01, (byte) 0x02, (byte) 0x01 } )
                .concatenate(numberAsArrayOfBytes(radix, 3))
                .concatenate(new ByteString( new byte[] { (byte) 0x0A } ))
                .concatenate(new ByteString( new byte[] { (byte) (mod(leftSideLength, 256) & 0xFF) }))
                .concatenate(numberAsArrayOfBytes(textLength, 4))
                .concatenate(numberAsArrayOfBytes(tweakLength, 4));
    }
}
