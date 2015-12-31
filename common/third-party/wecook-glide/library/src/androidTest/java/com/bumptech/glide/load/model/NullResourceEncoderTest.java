package com.bumptech.glide.load.model;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.NullResourceEncoder;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class NullResourceEncoderTest {
    private NullResourceEncoder<Object> encoder;

    @Before
    public void setUp() {
        encoder = new NullResourceEncoder<Object>();
    }

    @Test
    public void testReturnsFalse() {
        assertFalse(encoder.encode(mock(Resource.class), new ByteArrayOutputStream()));
    }

    @Test
    public void testReturnsEmptyId() {
        assertEquals("", encoder.getId());
    }
}