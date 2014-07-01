/*
 * Copyright (c) 2010 CommonsWare, LLC
 * Portions Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appspot.afnf4199ga.twawm.ctl;

import net.afnf.and.twawm2.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.appspot.afnf4199ga.utils.Logger;

@SuppressLint("NewApi")
public class CwacTouchListView extends ListView {
    private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private int mDragPos; // which item is being dragged
    private int mFirstDragPos; // where was the dragged item originally
    private int mDragPoint; // at what offset inside the item did the user grab it
    private int mCoordOffset; // the difference between screen coordinates and coordinates in this view
    private DragListener mDragListener;
    private DropListener mDropListener;
    private RemoveListener mRemoveListener;
    private int mViewHeight;
    public static final int SLIDE_RIGHT = 1;
    public static final int SLIDE_LEFT = 2;
    private int mRemoveMode = -1;
    private Rect mTempRect = new Rect();
    private Bitmap mDragBitmap;
    private int mItemHeightNormal = -1;
    private int mItemHeightExpanded = -1;
    private int grabberId = -1;
    private int prev_y = Integer.MIN_VALUE;
    private ScrollThread scrollThread = null;
    private boolean scrolling = false;
    private final float dragVeilViewStartAlpha = 0.2f;

    public CwacTouchListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CwacTouchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CwacTouchListView, 0, 0);

            mItemHeightNormal = a.getDimensionPixelSize(R.styleable.CwacTouchListView_normal_height, 0);
            mItemHeightExpanded = a.getDimensionPixelSize(R.styleable.CwacTouchListView_expanded_height, mItemHeightNormal);
            grabberId = a.getResourceId(R.styleable.CwacTouchListView_grabber, -1);
            mRemoveMode = a.getInt(R.styleable.CwacTouchListView_remove_mode, -1);

            a.recycle();
        }
    }

    @Override
    final public void addHeaderView(View v, Object data, boolean isSelectable) {
        throw new RuntimeException("Headers are not supported with TouchListView");
    }

    @Override
    final public void addHeaderView(View v) {
        throw new RuntimeException("Headers are not supported with TouchListView");
    }

    @Override
    final public void addFooterView(View v, Object data, boolean isSelectable) {
        if (mRemoveMode == SLIDE_LEFT || mRemoveMode == SLIDE_RIGHT) {
            throw new RuntimeException("Footers are not supported with TouchListView in conjunction with remove_mode");
        }
    }

    @Override
    final public void addFooterView(View v) {
        if (mRemoveMode == SLIDE_LEFT || mRemoveMode == SLIDE_RIGHT) {
            throw new RuntimeException("Footers are not supported with TouchListView in conjunction with remove_mode");
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int itemnum = pointToPosition(x, y);
            if (itemnum == AdapterView.INVALID_POSITION) {
                break;
            }

            View item = (View) getChildAt(itemnum - getFirstVisiblePosition());

            if (isDraggableRow(item)) {
                mDragPoint = y - item.getTop();
                mCoordOffset = ((int) ev.getRawY()) - y;
                View dragger = item.findViewById(grabberId);
                Rect r = mTempRect;

                r.left = dragger.getLeft();
                r.right = dragger.getRight();
                r.top = dragger.getTop();
                r.bottom = dragger.getBottom();

                if ((r.left < x) && (x < r.right)) {

                    // first, change drag_veil_view
                    View dragVeilView = null;
                    if (Build.VERSION.SDK_INT >= 11) {
                        dragVeilView = item.findViewById(R.id.drag_veil_view);
                        Animation animation = dragVeilView.getAnimation();
                        if (animation != null) {
                            animation.cancel();
                        }
                        dragVeilView.setVisibility(VISIBLE);
                        dragVeilView.setAlpha(dragVeilViewStartAlpha);
                    }

                    item.setDrawingCacheEnabled(true);
                    // Create a copy of the drawing cache so that it does not get recycled
                    // by the framework when the list tries to clean up memory
                    Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
                    item.setDrawingCacheEnabled(false);

                    // reset drag_veil_view
                    if (Build.VERSION.SDK_INT >= 11) {
                        dragVeilView.setAlpha(0);
                        dragVeilView.setVisibility(INVISIBLE);
                    }

                    Rect listBounds = new Rect();
                    getGlobalVisibleRect(listBounds, null);

                    startDragging(bitmap, listBounds.left, y);
                    mDragPos = itemnum;
                    mFirstDragPos = mDragPos;
                    mViewHeight = getHeight();
                    return false;
                }

                mDragView = null;
            }

            break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    protected boolean isDraggableRow(View view) {
        return (view.findViewById(grabberId) != null);
    }

    /*
     * Restore size and visibility for all listitems
     */
    private void unExpandViews(boolean deletion) {
        for (int i = 0;; i++) {
            View vv = getChildAt(i);
            if (vv == null) {
                if (deletion) {
                    // HACK force update of mItemCount
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                    // end hack
                }
                layoutChildren(); // force children to be recreated where needed
                vv = getChildAt(i);
                if (vv == null) {
                    break;
                }
            }

            if (isDraggableRow(vv)) {
                //Log.i("ctd", "unExpandViews:" + i);
                RelativeLayout rl = (RelativeLayout) vv;
                rl.setGravity(Gravity.BOTTOM);
                ViewGroup.LayoutParams params = vv.getLayoutParams();
                params.height = mItemHeightNormal;
                vv.setLayoutParams(params);
                vv.setVisibility(View.VISIBLE);
            }
        }
    }

    /* Adjust visibility and size to make it appear as though
     * an item is being dragged around and other items are making
     * room for it:
     * If dropping the item would result in it still being in the
     * same place, then make the dragged listitem's size normal,
     * but make the item invisible.
     * Otherwise, if the dragged listitem is still on screen, make
     * it as small as possible and expand the item below the insert
     * point.
     * If the dragged item is not on screen, only expand the item
     * below the current insertpoint.
     */
    private void doExpansion(int relDragPos) {

        int relFirstDragPos = mFirstDragPos - getFirstVisiblePosition();
        int childCount = getChildCount();

        //int count = getCount();
        //int firstVisible = getFirstVisiblePosition();
        //Log.i("ctd", "count=" + count + ", child=" + childCount + ", firstV=" + firstVisible + ", mDragPos=" + mDragPos + ", relDrag=" + relDragPos + ", first=" + (mFirstDragPos - getFirstVisiblePosition()));

        for (int i = 0;; i++) {
            View vv = getChildAt(i);
            if (vv == null) {
                break;
            }
            int height = mItemHeightNormal;
            int visibility = View.VISIBLE;
            int gravity = Gravity.BOTTOM;

            if (i == relFirstDragPos) {
                // processing the item that is being dragged
                if (relDragPos == relFirstDragPos || childCount - 1 == relFirstDragPos) {
                    // hovering over the original location
                    visibility = View.INVISIBLE;
                }
                else {
                    // not hovering over it
                    visibility = View.INVISIBLE;
                    height = 1;
                }
            }
            else if (i == relDragPos - 1) {
                if (childCount == relDragPos) {
                    height = mItemHeightExpanded;
                    gravity = Gravity.TOP;
                }
            }
            else if (i == relDragPos) {
                if (childCount == relDragPos + 1) {
                    height = mItemHeightExpanded + mItemHeightNormal;
                    gravity = Gravity.CENTER_VERTICAL;
                }
                else {
                    height = mItemHeightExpanded;
                }
            }

            if (isDraggableRow(vv)) {
                RelativeLayout rl = (RelativeLayout) vv;
                rl.setGravity(gravity);
                ViewGroup.LayoutParams params = vv.getLayoutParams();
                params.height = height;
                vv.setLayoutParams(params);
                vv.setVisibility(visibility);

                // うごかない
                //rl.setBackgroundResource(i % 2 == 0 ? R.color.row_even : R.color.row_odd);
            }
        }

        // Request re-layout since we changed the items layout
        // and not doing this would cause bogus hitbox calculation
        // in myPointToPosition
        layoutChildren();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if ((mDragListener != null || mDropListener != null) && mDragView != null) {
            int action = ev.getAction();
            switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                prev_y = Integer.MIN_VALUE;
                Rect r = mTempRect;
                mDragView.getDrawingRect(r);

                stopScroll();

                if (mRemoveMode == SLIDE_RIGHT && ev.getX() > r.left + (r.width() * 3 / 4)) {
                    if (mRemoveListener != null) {
                        mRemoveListener.remove(mFirstDragPos);
                    }
                    unExpandViews(true);
                }
                else if (mRemoveMode == SLIDE_LEFT && ev.getX() < r.left + (r.width() / 4)) {
                    if (mRemoveListener != null) {
                        mRemoveListener.remove(mFirstDragPos);
                    }
                    unExpandViews(true);
                }
                else if (mDragPos >= 0) {

                    int iv_delta = 1;
                    int distance_delta = 1;

                    int halfHeight = mViewHeight / 2;
                    int ref = pointToPosition(0, halfHeight);
                    if (ref == AdapterView.INVALID_POSITION) {
                        //Log.i("ctd", "INVALID_POSITION");
                        //we hit a divider or an invisible view, check somewhere else
                        ref = pointToPosition(0, halfHeight + iv_delta);
                    }
                    View v = getChildAt(ref - getFirstVisiblePosition());

                    int distance = 0;
                    if (v != null) {
                        int pos = v.getTop();
                        distance = pos + distance_delta;
                    }

                    int absPos = mDragPos;
                    if (mDragPos >= getCount()) {
                        absPos = (getCount() - 1);
                    }

                    boolean x = mFirstDragPos < getFirstVisiblePosition();
                    if (x) {
                        absPos -= 1;
                    }

                    if (mDropListener != null) {
                        mDropListener.drop(mFirstDragPos, absPos);
                    }

                    unExpandViews(false);

                    if (x) {
                        setSelectionFromTop(ref - 1, distance);
                    }

                    {
                        stopDragging();

                        if (Build.VERSION.SDK_INT >= 11) {
                            int firstV = getFirstVisiblePosition();
                            int lastV = getLastVisiblePosition();
                            if (firstV <= absPos && absPos <= lastV) {
                                int relPos = absPos - firstV;
                                View item = getChildAt(relPos);
                                final View dragVeilView = item.findViewById(R.id.drag_veil_view);
                                dragVeilView.setVisibility(VISIBLE);
                                dragVeilView.setAlpha(1);
                                AlphaAnimation animation = new AlphaAnimation(dragVeilViewStartAlpha, 0f);
                                animation.setDuration(1000);
                                animation.setBackgroundColor(0);
                                animation.setAnimationListener(new AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        dragVeilView.setVisibility(INVISIBLE);
                                    }
                                });
                                dragVeilView.startAnimation(animation);
                            }
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int x = (int) ev.getX();
                int y = (int) ev.getY();

                dragView(x, y);
                int vec_y = prev_y == Integer.MIN_VALUE ? y : y - prev_y;
                prev_y = y;

                int fv = getFirstVisiblePosition();
                int h = mItemHeightNormal;
                int k = getChildAt(0).getTop();
                double f = Math.max(0.0, (double) (y - mDragPoint + h / 2 - k) / h);
                int itemnum = fv + (int) f;

                if (itemnum >= 0) {
                    if (scrollThread == null) {
                        Boolean down = null;
                        if (y < h && vec_y < 0) {
                            down = false;
                        }
                        else if (mViewHeight - h < y && vec_y > 0) {
                            down = true;
                        }
                        if (down != null) {
                            scrollThread = new ScrollThread();
                            scrollThread.startScroll(down);
                        }
                    }
                    else {
                        if (false == (y < h || mViewHeight - h < y)) {
                            stopScroll();
                        }
                    }

                    if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                        if (mDragListener != null) {
                            mDragListener.drag(mDragPos, itemnum);
                        }
                        mDragPos = itemnum;

                        if (itemnum >= mFirstDragPos && fv <= mFirstDragPos) {
                            itemnum += 1;
                        }

                        int relDragPos = itemnum - fv;

                        //Log.i("ctd", "itemnum=" + itemnum + ", relDragPos=" + relDragPos + ", y=" + y + ", k=" + k + ", f=" + ((int) (f * 100)));

                        doExpansion(relDragPos);
                    }
                }
                break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    public void stopScroll() {
        if (scrollThread != null) {
            scrollThread.stopScroll();
            scrollThread = null;
        }
    }

    class ScrollThread extends Thread {
        private boolean down;

        public boolean isDownScrolling() {
            return down;
        }

        public void startScroll(boolean down) {
            this.down = down;
            scrolling = true;
            start();
        }

        public void stopScroll() {
            scrolling = false;
            this.interrupt();
        }

        @Override
        public void run() {
            try {
                while (scrolling) {
                    Thread.sleep(30);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            doScroll(down);
                        }
                    });
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
            catch (Throwable e) {
                Logger.e("ScrollThread error", e);
            }
        }
    }

    private void doScroll(Boolean down) {

        int iv_delta = 1;
        int distance_delta = 1;
        if (down != null) {
            int speed = 4;
            if (mViewHeight >= 1200) {
                speed = 12;
            }
            else if (mViewHeight >= 800) {
                speed = 8;
            }
            else if (mViewHeight >= 600) {
                speed = 6;
            }
            distance_delta = (down ? -1 : +1) * 2 * speed;
            iv_delta = down ? +32 : -32;
        }

        int halfHeight = mViewHeight / 2;
        int ref = pointToPosition(0, halfHeight);
        if (ref == AdapterView.INVALID_POSITION) {
            //Log.i("ctd", "INVALID_POSITION");
            //we hit a divider or an invisible view, check somewhere else
            ref = pointToPosition(0, halfHeight + iv_delta);
        }
        View v = getChildAt(ref - getFirstVisiblePosition());
        if (v != null) {
            int pos = v.getTop();
            int distance = pos + distance_delta;
            //Log.i("ctd", "before Scroll 1");
            //Log.i("ctd", "ScrollThread ref=" + ref + ", pos=" + pos + ", distance=" + distance);

            if (down == null) {
                setSelectionFromTop(ref, distance);
            }
            else if (scrolling) {
                setSelectionFromTop(ref, distance);

                int relDragPos = down ? getChildCount() : 0;
                mDragPos = getFirstVisiblePosition() + relDragPos;
                //Log.i("ctd", "mDragPos=" + mDragPos);
                doExpansion(relDragPos);
            }
        }
    }

    private void startDragging(Bitmap bm, int x, int y) {
        stopDragging();

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = x;
        mWindowParams.y = y - mDragPoint + mCoordOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        ImageView v = new ImageView(getContext());
        v.setImageBitmap(bm);
        mDragBitmap = bm;

        mWindowManager = (WindowManager) getContext().getSystemService("window");
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }

    private void dragView(int x, int y) {
        float alpha = 1.0f;
        int width = mDragView.getWidth();

        if (mRemoveMode == SLIDE_RIGHT) {
            if (x > width / 2) {
                alpha = ((float) (width - x)) / (width / 2);
            }
            mWindowParams.alpha = alpha;
        }
        else if (mRemoveMode == SLIDE_LEFT) {
            if (x < width / 2) {
                alpha = ((float) x) / (width / 2);
            }
            mWindowParams.alpha = alpha;
        }
        mWindowParams.y = Math.max(0, y - mDragPoint) + mCoordOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);
    }

    private void stopDragging() {
        if (mDragView != null) {
            mDragView.setVisibility(View.INVISIBLE);
            WindowManager wm = (WindowManager) getContext().getSystemService("window");
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }
    }

    public void setDragListener(DragListener l) {
        mDragListener = l;
    }

    public void setDropListener(DropListener l) {
        mDropListener = l;
    }

    public void setRemoveListener(RemoveListener l) {
        mRemoveListener = l;
    }

    public interface DragListener {
        void drag(int from, int to);
    }

    public interface DropListener {
        void drop(int from, int to);
    }

    public interface RemoveListener {
        void remove(int which);
    }
}
