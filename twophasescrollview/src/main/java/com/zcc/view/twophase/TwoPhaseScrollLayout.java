package com.zcc.view.twophase;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import androidx.viewpager.widget.ViewPager;

import java.util.List;


/**
 * Created by zhuchengcheng on 15/10/29.
 */
public class TwoPhaseScrollLayout extends LinearLayout {
    private int mDeltaY = 0;
    private View mContent;
    private View mHeader;
    private View mNav;
    private View mEmptyView;
    private ViewPager mViewPager;
    private int mTopViewHeight;
    private ViewGroup mInnerScrollView;
    private boolean isTopHidden = false;
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private float mLastX;
    private float mLastY;
    private boolean mDragging;
    private boolean isInControl = false;
    private Context mContext;
    private StickyNavFactoryInterface mFactory;
    private HeaderScrollListener mHeaderScrollListener;
    private TwoPhaseScrollLayout.TopHiddenListener mTopHiddenListener;
    private boolean mScrollEnable = true;
    private IStickyNavScrollListener mStickyNavScrollListener;

    public TwoPhaseScrollLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public TwoPhaseScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void setDeltaY(int deltaY) {
        this.mDeltaY = deltaY;
    }

    public void setHeaderScrollListener(HeaderScrollListener l) {
        mHeaderScrollListener = l;
    }

    public void init() {
        setOrientation(LinearLayout.VERTICAL);
        mScroller = new OverScroller(mContext);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(mContext)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(mContext)
                .getScaledMinimumFlingVelocity();
    }


    /*
        使用此方法进行内容的初始化
     */
    public void initContentView(StickyNavFactoryInterface factory) {
        if (mFactory != null) {
            return;
        }
        mFactory = factory;
        mHeader = mFactory.createHeaderView();
        mNav = mFactory.createNavView();
        mContent = mFactory.createContent();
        mViewPager = mFactory.createViewPager();
        if (factory instanceof DynaStickNavFactory) {
            addHeaderView(mFactory.createHeaderView());
            addNavView(mFactory.createNavView());
            addViewPager(mFactory.createViewPager());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (mViewPager != null) {
                ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
                if (mNav != null) {
                    params.height = getMeasuredHeight() - mNav.getMeasuredHeight() - mDeltaY;
                } else {
                    params.height = getMeasuredHeight();
                }
            }
            if (mHeader != null) {
                mTopViewHeight = mHeader.getMeasuredHeight() - mDeltaY;
            }
            if (mContent != null) {
                ViewGroup.LayoutParams params = mContent.getLayoutParams();
                params.height = getMeasuredHeight() + mTopViewHeight;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setScrollEnable(boolean scrollEnable) {
        mScrollEnable = scrollEnable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                updateInnerScrollView();
//               纵向 且 头部隐藏 且并不是分发中  将时间作为 down 事件重新分发 在次分发后， isincontrol  于是正常分发
                if (ScrollAbleViewHelper.isScrollToTop(mInnerScrollView) && isTopHidden && dy > 0
                        && !isInControl) {
                    isInControl = true;
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    MotionEvent ev2 = MotionEvent.obtain(ev);
                    dispatchTouchEvent(ev);
                    ev2.setAction(MotionEvent.ACTION_DOWN);
                    return dispatchTouchEvent(ev2);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mScrollEnable) {
            return super.onInterceptTouchEvent(ev);
        }
        final int action = ev.getAction();
        float x = ev.getX();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                float dx = x - this.mLastX;
                updateInnerScrollView();
//              纵向的情况下
                if (Math.abs(dy) > mTouchSlop && Math.abs(dy) >= Math.abs(dx)) {
                    mDragging = true;
                    // 如果topView没有隐藏
                    // 或sc的scrollY = 0 && topView隐藏 && 下拉，则拦截
                    if (mInnerScrollView != null && (!isTopHidden
                            || (ScrollAbleViewHelper.isScrollToTop(mInnerScrollView)
                            && isTopHidden && dy > 0))) {
                        initVelocityTrackerIfNotExists();
                        mVelocityTracker.addMovement(ev);
                        mLastY = y;
                        mLastX = x;
                        mInnerScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mDragging = false;
                recycleVelocityTracker();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void updateInnerScrollView() {
        if (mFactory != null) {
            mInnerScrollView = mFactory.createInnerScrollView(mViewPager);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mScrollEnable) {
            return super.onTouchEvent(event);
        }
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);
        int action = event.getAction();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished())
                    mScroller.abortAnimation();
                mLastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                if (!mDragging && Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                }
                if (mDragging) {
                    scrollBy(0, (int) -dy);
                    if (mHeaderScrollListener != null) {
                        mHeaderScrollListener.onHeaderScroll(getScrollX(), getScrollY());
                    }
                    // 如果topView隐藏，且上滑动时，则改变当前事件为ACTION_DOWN
                    if (getScrollY() == mTopViewHeight && dy < 0) {
                        event.setAction(MotionEvent.ACTION_DOWN);
                        dispatchTouchEvent(event);
                        isInControl = false;
                    }
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                recycleVelocityTracker();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
                mDragging = false;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityY = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(velocityY) > mMinimumVelocity) {
                    fling(-velocityY);
                }
                if (mHeaderScrollListener != null) {
                    mHeaderScrollListener.onScrollEnd();
                }
                recycleVelocityTracker();
                break;
        }

        try {
            return super.onTouchEvent(event);
        } catch (Exception var6) {
            return false;
        }
    }

    public void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mTopViewHeight);
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTopViewHeight) {
            y = mTopViewHeight;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }

        if (!this.isTopHidden && this.getScrollY() == this.mTopViewHeight && this.mTopHiddenListener != null) {
            this.mTopHiddenListener.onTopHidden();
        }

        isTopHidden = getScrollY() == mTopViewHeight;


        if (mStickyNavScrollListener != null) {
            mStickyNavScrollListener.onScrollTo(x, y);
        }

        if (!isTopHidden) {
            backAllChildToTop();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void setmTopHiddenListener(TwoPhaseScrollLayout.TopHiddenListener mTopHiddenListener) {
        this.mTopHiddenListener = mTopHiddenListener;
    }

    protected void addContentView(View contentView) {
        if (contentView != null) {
            mContent = contentView;
            if (mContent.getLayoutParams() == null) {
                mContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
            removeAllViews();
            this.addView(contentView);
        }

    }

    protected void addHeaderView(View headerView) {
        if (headerView != null) {
            mHeader = headerView;
            if (mHeader.getLayoutParams() == null) {
                mHeader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            this.addView(headerView);
        }

    }

    protected void addNavView(View navView) {
        if (navView != null) {
            mNav = navView;
            if (mNav.getLayoutParams() == null) {
                mNav.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
            }
            this.addView(mNav);
        }
    }

    protected void addViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
//        mViewPager.setId(R.id.id_stickynavlayout_viewpager);
        //这里设置的layoutparams的height在onMeasure中会改变
        if (mViewPager != null && mViewPager.getLayoutParams() == null) {
            mViewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        this.addView(mViewPager);
    }

    public void addEmptyView(View view) {
        mEmptyView = view;
        mEmptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mEmptyView.setVisibility(View.GONE);
        this.addView(mEmptyView);
    }

    public void showEmptyView() {
        if (mEmptyView != null) {
            mViewPager.setVisibility(GONE);
            if (mNav != null) {
                mNav.setVisibility(GONE);
            }
            mEmptyView.setVisibility(VISIBLE);
        }
    }

    public void hideEmptyView() {
        if (mEmptyView != null && mEmptyView.isShown()) {
            mViewPager.setVisibility(VISIBLE);
            mNav.setVisibility(VISIBLE);
            mEmptyView.setVisibility(GONE);
        }
    }

    public View getHeader() {
        return mHeader;
    }

    public View getNav() {
        return mNav;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public boolean isHeaderTop() {
        this.updateInnerScrollView();
        return this.getScrollY() != 0 ? false :
                (null == this.mInnerScrollView) ? true : ScrollAbleViewHelper.isScrollToTop(mInnerScrollView);

    }

    public void backToTop() {
        this.updateInnerScrollView();
        this.scrollTo(0, 0);
        ScrollAbleViewHelper.scrollToTop(mInnerScrollView);
    }

    protected void backAllChildToTop() {
        List<ViewGroup> childs = mFactory.getInnerScrollViewList(mViewPager);
        if (childs == null) return;
        ScrollAbleViewHelper.scrollAllChildToTop(childs);
    }

    public void hideTop() {
        this.updateInnerScrollView();
        this.scrollTo(0, this.mTopViewHeight);
        ScrollAbleViewHelper.scrollToTop(mInnerScrollView);

    }

    public boolean isListTop() {
        this.updateInnerScrollView();
        if (null == this.mInnerScrollView) {
            return true;
        } else {
            return ScrollAbleViewHelper.isScrollToTop(mInnerScrollView);
        }
    }

    public boolean isReadyForPullStart() {
        if (this.getScrollY() != 0) {
            return false;
        } else if (this.mTopViewHeight != 0) {
            return true;
        } else {
            this.updateInnerScrollView();
            return ScrollAbleViewHelper.isScrollToTop(mInnerScrollView);
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public boolean isTopHidden() {
        return this.isTopHidden;
    }

    public boolean isHasHeader() {
        return this.mTopViewHeight != 0;
    }

    public void setStickyNavScrollListener(IStickyNavScrollListener listener) {
        mStickyNavScrollListener = listener;
    }

    public interface HeaderScrollListener {
        void onHeaderScroll(float x, float y);

        void onScrollEnd();
    }

    public interface TopHiddenListener {
        void onTopHidden();
    }

    public interface IStickyNavScrollListener {
        void onScrollTo(float x, float y);
    }
}
