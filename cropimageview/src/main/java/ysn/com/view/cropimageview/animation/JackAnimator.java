package ysn.com.view.cropimageview.animation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.Interpolator;

/**
 * @Author yangsanning
 * @ClassName JackAnimator
 * @Description 动画
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class JackAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private static final int DEFAULT_ANIMATION_DURATION = 150;

    private ValueAnimator valueAnimator;
    private OnJackAnimationListener onJackAnimationListener;

    public JackAnimator(Interpolator interpolator) {
        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.addListener(this);
        valueAnimator.addUpdateListener(this);
        valueAnimator.setInterpolator(interpolator);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (onJackAnimationListener != null) {
            onJackAnimationListener.onAnimationStart();
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (onJackAnimationListener != null) {
            onJackAnimationListener.onAnimationEnd();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        if (onJackAnimationListener != null) {
            onJackAnimationListener.onAnimationEnd();
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (onJackAnimationListener != null) {
            onJackAnimationListener.onAnimationUpdate(animation.getAnimatedFraction());
        }
    }

    public void start(long duration) {
        if (duration >= 0) {
            valueAnimator.setDuration(duration);
        } else {
            valueAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        }
        valueAnimator.start();
    }

    public void cancel() {
        valueAnimator.cancel();
    }

    public void setOnAnimatorListener(OnJackAnimationListener onJackAnimationListener) {
        if (onJackAnimationListener != null) {
            this.onJackAnimationListener = onJackAnimationListener;
        }
    }

    public interface OnJackAnimationListener {

        void onAnimationStart();

        void onAnimationUpdate(float scale);

        void onAnimationEnd();
    }
}
