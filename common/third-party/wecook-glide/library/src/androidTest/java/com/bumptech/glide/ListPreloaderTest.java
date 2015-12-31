package com.bumptech.glide;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ListPreloaderTest {

    @Test
    public void testGetItemsIsCalledIncreasing() {
        final AtomicBoolean called = new AtomicBoolean(false);
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                called.set(true);
                assertEquals(11, start);
                assertEquals(21, end);
                return super.getItems(start, end);
            }
        };
        preloader.onScroll(null, 1, 10, 30);
        assertTrue(called.get());
    }

    @Test
    public void testGetItemsIsCalledInOrderIncreasing() {
        final int toPreload = 10;
        final List<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < toPreload; i++) {
            objects.add(new Object());
        }

        ListPreloader preloader = new ListPreloader(toPreload) {
            int expectedPosition;
            @Override
            protected int[] getDimensions(Object item) {
                return new int[] { 10, 10 };
            }

            @Override
            protected List getItems(int start, int end) {
                return objects;
            }

            @Override
            protected BitmapRequestBuilder getRequestBuilder(Object item) {
                assertEquals(objects.get(expectedPosition), item);
                expectedPosition++;
                return mock(BitmapRequestBuilder.class);
            }
        };

        preloader.onScroll(null, 1, 10, 30);
    }

    @Test
    public void testGetItemsIsCalledDecreasing() {
        final AtomicBoolean called = new AtomicBoolean(false);
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                // Ignore the preload caused from us starting at the end
                if (start == 40) {
                    return Collections.emptyList();
                }
                called.set(true);
                assertEquals(19, start);
                assertEquals(29, end);
                return super.getItems(start, end);
            }
        };
        preloader.onScroll(null, 30, 10, 40);
        preloader.onScroll(null, 29, 10, 40);
        assertTrue(called.get());
    }

    @Test
    public void testGetItemsIsCalledInOrderDecreasing() {
        final int toPreload = 10;
        final List<Object> objects = new ArrayList<Object>();
        for (int i = 0; i < toPreload; i++) {
            objects.add(new Object());
        }

        ListPreloader preloader = new ListPreloader(toPreload) {
            int expectedPosition = toPreload - 1;
            @Override
            protected int[] getDimensions(Object item) {
                return new int[] { 10, 10 };
            }

            @Override
            protected List getItems(int start, int end) {
                if (start == 40) {
                    return  Collections.emptyList();
                }
                return objects;
            }

            @Override
            protected BitmapRequestBuilder getRequestBuilder(Object item) {
                assertEquals(objects.get(expectedPosition), item);
                expectedPosition--;
                return mock(BitmapRequestBuilder.class);
            }
        };

        preloader.onScroll(null, 30, 10, 40);
        preloader.onScroll(null, 29, 10, 40);
    }

    @Test
    public void testGetItemsIsNeverCalledWithEndGreaterThanTotalItems() {
        final AtomicBoolean called = new AtomicBoolean(false);
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                called.set(true);
                assertEquals(26, start);
                assertEquals(30, end);
                return super.getItems(start, end);
            }
        };
        preloader.onScroll(null, 16, 10, 30);
        assertTrue(called.get());
    }

    @Test
    public void testGetItemsIsNeverCalledWithStartLessThanZero() {
        final AtomicBoolean called = new AtomicBoolean(false);
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                if (start == 17) {
                    return Collections.emptyList();
                }
                called.set(true);
                assertEquals(0, start);
                assertEquals(6, end);
                return super.getItems(start, end);
            }
        };
        preloader.onScroll(null, 7, 10, 30);
        preloader.onScroll(null, 6, 10, 30);
        assertTrue(called.get());
    }

    @Test
    public void testDontPreloadItemsRepeatedlyWhileIncreasing() {
        final AtomicInteger called = new AtomicInteger();
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                final int current = called.getAndIncrement();
                if (current == 0) {
                    assertEquals(11, start);
                    assertEquals(21, end);
                } else if (current == 1) {
                    assertEquals(21, start);
                    assertEquals(24, end);
                }
                return super.getItems(start, end);
            }
        };

        preloader.onScroll(null, 1, 10, 30);
        preloader.onScroll(null, 4, 10, 30);

        assertEquals(2, called.get());
    }

    @Test
    public void testDontPreloadItemsRepeatedlyWhileDecreasing() {
        final AtomicInteger called = new AtomicInteger();
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                if (start == 30) {
                    return Collections.emptyList();
                }
                final int current = called.getAndIncrement();
                if (current == 0) {
                    assertEquals(10, start);
                    assertEquals(20, end);
                } else if (current == 1) {
                    assertEquals(7, start);
                    assertEquals(10, end);
                }
                return super.getItems(start, end);
            }
        };

        preloader.onScroll(null, 21, 10, 30);
        preloader.onScroll(null, 20, 10, 30);
        preloader.onScroll(null, 17, 10, 30);
        assertEquals(2, called.get());
    }

    @Test
    public void testItemsArePreloadedWithGlide() {
        final List<Object> objects = new ArrayList<Object>();
        objects.add(new Object());
        objects.add(new Object());
        final HashSet<Object> loadedObjects = new HashSet<Object>();
        ListPreloaderAdapter preloader = new ListPreloaderAdapter(10) {
            @Override
            protected List<Object> getItems(int start, int end) {
                return objects;
            }

            @Override
            protected GenericRequestBuilder getRequestBuilder(Object item) {
                loadedObjects.add(item);
                return super.getRequestBuilder(item);
            }
        };

        preloader.onScroll(null, 1, 10, 30);

        assertThat(loadedObjects, containsInAnyOrder(objects.toArray()));
    }

    private static class ListPreloaderAdapter extends ListPreloader<Object> {

        public ListPreloaderAdapter(int maxPreload) {
            super(maxPreload);
        }

        @Override
        protected int[] getDimensions(Object item) {
            return new int[] { 100, 100 };
        }

        @Override
        protected List<Object> getItems(int start, int end) {
            ArrayList<Object> result = new ArrayList<Object>(end - start);
            Collections.fill(result, new Object());
            return result;
        }

        @Override
        protected GenericRequestBuilder getRequestBuilder(Object item) {
            return mock(BitmapRequestBuilder.class);
        }
    }
}
