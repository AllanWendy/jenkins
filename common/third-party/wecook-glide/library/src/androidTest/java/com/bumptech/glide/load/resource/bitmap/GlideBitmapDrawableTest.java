package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GlideBitmapDrawableTest {

    private Bitmap bitmap;
    private GlideBitmapDrawable drawable;

    @Before
    public void setUp() {
        bitmap = Bitmap.createBitmap(123, 456, Bitmap.Config.ARGB_8888);
        drawable = new GlideBitmapDrawable(Robolectric.application.getResources(), bitmap);
    }

    @Test
    public void testReturnsWidthOfGivenBitmap() {
        assertEquals(bitmap.getWidth(), drawable.getIntrinsicWidth());
    }

    @Test
    public void testReturnsHeightOfGivenBitmap() {
        assertEquals(bitmap.getHeight(), drawable.getIntrinsicHeight());
    }

    @Test
    public void testReturnsNotAnimated() {
        assertFalse(drawable.isAnimated());
    }

    @Test
    public void testReturnsNotRunning() {
        assertFalse(drawable.isRunning());
    }

    @Test
    public void testReturnsOpacityOpaqueIfBitmapDoesNotHaveAlpha() {
        bitmap.setHasAlpha(false);
        drawable.setAlpha(255);
        assertEquals(PixelFormat.OPAQUE, drawable.getOpacity());
    }

    @Test
    public void testReturnsOpacityTranslucentIfAlphaIsSet() {
        bitmap.setHasAlpha(false);
        drawable.setAlpha(100);
        assertEquals(PixelFormat.TRANSLUCENT, drawable.getOpacity());
    }

    @Test
    public void testReturnsNonNullConstantState() {
        assertNotNull(drawable.getConstantState());
    }

    @Test
    public void testConstantStateReturnsNewGlideBitmapDrawable() {
        Drawable newDrawable = drawable.getConstantState().newDrawable();
        assertThat(newDrawable, instanceOf(GlideBitmapDrawable.class));
    }

    @Test
    public void testDrawableReturnedByConstantStateWrapsSameBitmap() {
        GlideBitmapDrawable newDrawable = (GlideBitmapDrawable) drawable.getConstantState().newDrawable();
        assertEquals(drawable.getBitmap(), newDrawable.getBitmap());
    }

    @Test
    public void testMutatedDrawableIsGlideBitmapDrawable() {
        Drawable newDrawable = drawable.mutate();
        assertThat(newDrawable, instanceOf(GlideBitmapDrawable.class));
    }

    @Test
    public void testMutatedDrawableWrapsSameBitmap() {
        GlideBitmapDrawable mutated = (GlideBitmapDrawable) drawable.mutate();
        assertEquals(drawable.getBitmap(), mutated.getBitmap());
    }

    @Test
    public void testRetainsSameBoundsAcrossMutation() {
        Rect bounds = new Rect(0, 0, 100, 100);
        drawable.setBounds(bounds);
        drawable.onBoundsChange(bounds);
        Canvas canvas = mock(Canvas.class);
        drawable.draw(canvas);

        Drawable mutated = drawable.mutate();
        mutated.draw(canvas);

        assertThat(mutated.getBounds(), equalTo(bounds));
        verify(canvas, times(2)).drawBitmap(eq(bitmap), isNull(Rect.class), eq(bounds), any(Paint.class));
    }

    @Test
    public void testMutatedDrawableUsesNewPaint() {
        drawable.setAlpha(1);
        Drawable newDrawable = drawable.getConstantState().newDrawable();
        Drawable mutated = drawable.mutate();
        mutated.setAlpha(100);

        Canvas canvas = mock(Canvas.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Paint paint = (Paint) invocation.getArguments()[3];
                assertThat(paint.getAlpha(), equalTo(1));
                return null;
            }
        }).when(canvas).drawBitmap(any(Bitmap.class), any(Rect.class), any(Rect.class), any(Paint.class));
        newDrawable.draw(canvas);
        verify(canvas).drawBitmap(eq(bitmap), any(Rect.class), any(Rect.class), any(Paint.class));
    }

    @Test
    public void testMutatedDrawableUsesNewColorFilter() {
        final ColorFilter originalColorFilter = new LightingColorFilter(1, 1);
        drawable.setColorFilter(originalColorFilter);
        Drawable newDrawable = drawable.getConstantState().newDrawable();
        Drawable mutated = drawable.mutate();
        ColorFilter newColorFilter = new LightingColorFilter(2, 2);
        mutated.setColorFilter(newColorFilter);

        Canvas canvas = mock(Canvas.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Paint paint = (Paint) invocation.getArguments()[3];
                assertThat(paint.getColorFilter(), equalTo(originalColorFilter));
                return null;
            }
        }).when(canvas).drawBitmap(any(Bitmap.class), any(Rect.class), any(Rect.class), any(Paint.class));
        newDrawable.draw(canvas);
        verify(canvas).drawBitmap(eq(bitmap), any(Rect.class), any(Rect.class), any(Paint.class));
    }
}