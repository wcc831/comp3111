package hk.ust.cse.hunkim.questionroom.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

/**
 * Created by cc on 11/14/2015.
 */
public class AnimationFactory {

    public static void crossFade(final View v1, final View v2, final int duration) {
        v2.setAlpha(0f);
        v2.setVisibility(View.VISIBLE);
        v1.animate().alpha(0f).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                v1.setVisibility(View.GONE);
                v2.animate().alpha(1f).setDuration(duration).setListener(null);
            }
        });
    }

    public static void fadeIn(View v, int duration) {
        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(duration).setListener(null);
    }

    public static void move(View v) {

    }
}
