package com.github.tehras.loadingskeleton;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Canvas;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.tehras.loadingskeleton.helpers.LoadingSkeletonViewConverter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@SuppressLint("WrongCall")
public class LoadingSkeletonTest {

    private Activity activity;

    @Before
    public void setUp() {
        this.activity = setupActivity(Activity.class);
        Robolectric.buildActivity(Activity.class).setup().get();
    }

    @Test
    public void testThatStartWaits() {
        LoadingSkeleton loadingSkeleton = new LoadingSkeleton(activity);
        loadingSkeleton.start();

        assertThat(ReflectionUtils.getProperty(loadingSkeleton, "startWhenLayoutFinished")).isEqualTo(true);
        assertThat(ReflectionUtils.getProperty(loadingSkeleton, "layoutFinished")).isEqualTo(false);


        boolean wentIntoStart = false;
        try {
            loadingSkeleton.onDraw(new Canvas());
        } catch (RuntimeException e) {
            wentIntoStart = true;
            assertThat(e.getMessage()).isEqualTo("View must have 1 child");
            assertThat(ReflectionUtils.getProperty(loadingSkeleton, "layoutFinished")).isEqualTo(true);
        }

        assertThat(wentIntoStart).isTrue();
    }

    @Test
    public void testWhenViewIsRendered() {
        LoadingSkeleton loadingSkeleton = loadedView();

        assertThat(ReflectionUtils.getProperty(loadingSkeleton, "layoutFinished")).isEqualTo(true);
        assertThat(ReflectionUtils.getProperty(loadingSkeleton, "startWhenLayoutFinished")).isEqualTo(false);

        try {
            loadingSkeleton.start();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("View must have 1 child");
        }
    }

    @Test
    public void testViewGroupException() {
        LoadingSkeleton loadingSkeleton = loadedView();

        loadingSkeleton.addView(new View(activity));

        boolean hasException = false;
        try {
            loadingSkeleton.start();
        } catch (RuntimeException e) {
            hasException = true;
            assertThat(e.getMessage()).isEqualTo("Layout must be a ViewGroup");
        }

        assertThat(hasException).isTrue();
    }

    @Test
    public void testShimmerLayout() {
        LoadingSkeleton loadingSkeleton = loadedViewWithChild();
        loadingSkeleton.setSkeletonViewConverter(new LoadingSkeletonViewConverter.Builder().shimmer(true).build());

        loadingSkeleton.start();
        View child = loadingSkeleton.getChildAt(0);

        assertThat(child).isInstanceOf(ShimmerFrameLayout.class);

        loadingSkeleton.setSkeletonViewConverter(new LoadingSkeletonViewConverter.Builder().shimmer(false).build());

        loadingSkeleton.stop();
        loadingSkeleton.start();
        child = loadingSkeleton.getChildAt(0);

        assertThat(child).isNotInstanceOf(ShimmerFrameLayout.class);
    }

    @Test
    public void testStopNegativeScenarios() {
        LoadingSkeleton loadingSkeleton = loadedView();

        try {
            loadingSkeleton.stop();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("View must have 1 child");
        }

        loadingSkeleton.addView(new View(activity));

        try {
            loadingSkeleton.stop();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Layout must be a ViewGroup");
        }
    }

    @Test
    public void testStop() {
        int defaultId = 10002;

        LoadingSkeleton loadingSkeleton = loadedViewWithChild();
        loadingSkeleton.getChildAt(0).setId(defaultId);

        loadingSkeleton.start();
        assertThat(loadingSkeleton.getChildAt(0).getId()).isNotEqualTo(defaultId);

        loadingSkeleton.stop();
        assertThat(loadingSkeleton.getChildAt(0).getId()).isEqualTo(defaultId);

    }

    private LoadingSkeleton loadedView() {
        LoadingSkeleton loadingSkeleton = new LoadingSkeleton(activity);
        loadingSkeleton.onDraw(new Canvas());

        return loadingSkeleton;
    }

    private LoadingSkeleton loadedViewWithChild() {
        LoadingSkeleton loadingSkeleton = new LoadingSkeleton(activity);
        loadingSkeleton.onDraw(new Canvas());

        loadingSkeleton.addView(new LinearLayout(activity));

        return loadingSkeleton;
    }

}
