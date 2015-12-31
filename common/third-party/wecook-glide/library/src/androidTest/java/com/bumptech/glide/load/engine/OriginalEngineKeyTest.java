package com.bumptech.glide.load.engine;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class OriginalEngineKeyTest {

    @Test
    public void testIsEqualToAnotherKeyWithSameId() {
        String id = "fakeId";
        OriginalEngineKey first = new OriginalEngineKey(id);
        OriginalEngineKey second = new OriginalEngineKey(id);
        assertEquals(first, second);
    }

    @Test
    public void testReturnsSameHashCodeAsAnotherKeyWithSameId() {
        String id = "testId";
        OriginalEngineKey first = new OriginalEngineKey(id);
        OriginalEngineKey second = new OriginalEngineKey(id);

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void testUpdatesDigestWithGivenId() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String id = "testId2";

        OriginalEngineKey firstKey = new OriginalEngineKey(id);
        MessageDigest firstDigest = MessageDigest.getInstance("SHA-1");
        firstKey.updateDiskCacheKey(firstDigest);
        byte[] firstBytes = firstDigest.digest();

        OriginalEngineKey secondKey = new OriginalEngineKey(id);
        MessageDigest secondDigest = MessageDigest.getInstance("SHA-1");
        secondKey.updateDiskCacheKey(secondDigest);
        byte[] secondBytes = secondDigest.digest();

        assertArrayEquals(firstBytes, secondBytes);
    }
}
