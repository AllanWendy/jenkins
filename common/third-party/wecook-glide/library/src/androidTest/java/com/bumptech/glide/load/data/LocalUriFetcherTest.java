package com.bumptech.glide.load.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.bumptech.glide.Priority;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class LocalUriFetcherTest {
    private TestLocalUriFetcher fetcher;

    @Before
    public void setUp() {
        fetcher = new TestLocalUriFetcher(Robolectric.application, Uri.parse("content://empty"));
    }

    @Test
    public void testClosesDataOnCleanup() throws Exception {
        Closeable closeable = fetcher.loadData(Priority.NORMAL);
        fetcher.cleanup();

        verify(closeable).close();
    }

    @Test
    public void testDoesNotCLoseNullData() throws IOException {
        fetcher.cleanup();

        verify(fetcher.closeable, never()).close();
    }

    @Test
    public void testHandlesExceptionOnClose() throws Exception {
        Closeable closeable = fetcher.loadData(Priority.NORMAL);

        doThrow(new IOException("Test")).when(closeable).close();
        fetcher.cleanup();
        verify(closeable).close();
    }

    private static class TestLocalUriFetcher extends LocalUriFetcher<Closeable> {
        final Closeable closeable = mock(Closeable.class);
        public TestLocalUriFetcher(Context context, Uri uri) {
            super(context, uri);
        }

        @Override
        protected Closeable loadResource(Uri uri, ContentResolver contentResolver) throws FileNotFoundException {
            return closeable;
        }
    }
}
