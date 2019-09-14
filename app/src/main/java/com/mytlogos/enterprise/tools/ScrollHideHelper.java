package com.mytlogos.enterprise.tools;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ScrollHideHelper {
    private int previousScrollDiff = 0;
    private long lastScroll;
    static final int BOTTOM = 1;
    static final int TOP = 2;
    static final int RIGHT = 3;
    static final int LEFT = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {BOTTOM, TOP, RIGHT, LEFT})
    @interface Direction {

    }

    public void hideGroups(int oldY, int newY, View bottom, View left, View top, View right) {
        int diff = newY - oldY;
        long currentTime = System.currentTimeMillis();
        long lastScrollTimeDiff = currentTime - this.lastScroll;

        if (lastScrollTimeDiff < 100 && diff < 10 && Integer.signum(diff) != Integer.signum(this.previousScrollDiff)) {
            return;
        }
        if (bottom != null) {
            this.setHideViewGroupParams(diff, bottom, BOTTOM);
        }
        if (top != null) {
            this.setHideViewGroupParams(diff, top, TOP);
        }
        if (left != null) {
            this.setHideViewGroupParams(diff, left, LEFT);
        }
        if (right != null) {
            this.setHideViewGroupParams(diff, right, RIGHT);
        }
        this.lastScroll = currentTime;
        this.previousScrollDiff = diff;
    }

    public void toggleGroups(View bottom, View left, View top, View right) {
        if (bottom != null) {
            this.toggleDirectionGroup(bottom, BOTTOM);
        }
        if (top != null) {
            this.toggleDirectionGroup(top, TOP);
        }
        if (left != null) {
            this.toggleDirectionGroup(left, LEFT);
        }
        if (right != null) {
            this.toggleDirectionGroup(right, RIGHT);
        }

    }

    private void toggleDirectionGroup(View view, @Direction int direction) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        int margin;
        if (direction == BOTTOM) {
            margin = layoutParams.bottomMargin;
        } else if (direction == LEFT) {
            margin = layoutParams.leftMargin;
        } else if (direction == TOP) {
            margin = layoutParams.topMargin;
        } else if (direction == RIGHT) {
            margin = layoutParams.rightMargin;
        } else {
            throw new IllegalArgumentException("unknown direction: " + direction);
        }

        int start;
        int end;

        if (margin == 0) {
            start = 0;
            end = (direction == BOTTOM || direction == TOP) ? -view.getHeight() : -view.getWidth();
        } else {
            start = margin;
            end = 0;
        }

        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            Object value = animation.getAnimatedValue();
            System.out.println("animated value: " + value);

            if (value instanceof Integer) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                int intValue = ((Integer) value);

                if (direction == BOTTOM) {
                    params.bottomMargin = intValue;
                } else if (direction == LEFT) {
                    params.leftMargin = intValue;
                } else if (direction == TOP) {
                    params.topMargin = intValue;
                } else {
                    params.rightMargin = intValue;
                }
                view.setLayoutParams(params);
            } else {
                System.err.println("expected an integer, got: " + value);
            }
        });
        animator.start();
    }

    public void showGroups(View bottom, View left, View top, View right) {
        if (bottom != null) {
            this.setShowViewGroupParams(bottom, BOTTOM);
        }
        if (top != null) {
            this.setShowViewGroupParams(top, TOP);
        }
        if (left != null) {
            this.setShowViewGroupParams(left, LEFT);
        }
        if (right != null) {
            this.setShowViewGroupParams(right, RIGHT);
        }
    }

    private void setShowViewGroupParams(View view, @Direction int direction) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (direction == BOTTOM) {
            layoutParams.bottomMargin = 0;
        } else if (direction == LEFT) {
            layoutParams.leftMargin = 0;
        } else if (direction == TOP) {
            layoutParams.topMargin = 0;
        } else if (direction == RIGHT) {
            layoutParams.rightMargin = 0;
        } else {
            throw new IllegalArgumentException("unknown direction: " + direction);
        }
        view.setLayoutParams(layoutParams);
    }

    private void setHideViewGroupParams(int diffY, View view, @Direction int direction) {
        if (diffY == 0) {
            return;
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int margin;
        if (direction == BOTTOM) {
            margin = layoutParams.bottomMargin;
        } else if (direction == LEFT) {
            margin = layoutParams.leftMargin;
        } else if (direction == TOP) {
            margin = layoutParams.topMargin;
        } else if (direction == RIGHT) {
            margin = layoutParams.rightMargin;
        } else {
            throw new IllegalArgumentException("unknown direction: " + direction);
        }
        margin = margin - diffY;

        int minMargin = (direction == BOTTOM || direction == TOP) ? -view.getHeight() : -view.getWidth();
        int maxMargin = 0;

        if (margin < minMargin) {
            margin = minMargin;
        } else if (margin > maxMargin) {
            margin = maxMargin;
        }
        if (direction == BOTTOM) {
            layoutParams.bottomMargin = margin;
        } else if (direction == LEFT) {
            layoutParams.leftMargin = margin;
        } else if (direction == TOP) {
            layoutParams.topMargin = margin;
        } else {
            layoutParams.rightMargin = margin;
        }
        view.setLayoutParams(layoutParams);
    }
}
