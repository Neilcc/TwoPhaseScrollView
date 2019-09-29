package com.zcc.view.twophase;

import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by cc on 15/10/29.
 */
public class DynaStickNavFactory implements StickyNavFactoryInterface {

    private View mHeaderView;
    private View mNavView;
    private ViewPager mViewPager;
    private int mInnerScrollViewId;

    protected DynaStickNavFactory() {
    }


    public DynaStickNavFactory(View headerView, View navView, ViewPager pager, int innerScrollViewId) {
        ViewParent headerParent = headerView.getParent();
        ViewParent navParent = navView.getParent();
        ViewParent pagerParent = pager.getParent();
        if (headerParent != null && headerParent instanceof ViewGroup) {
            ((ViewGroup) headerParent).removeView(headerView);
        }
        if (navParent != null && navParent instanceof ViewGroup) {
            ((ViewGroup) navParent).removeView(navView);
        }
        if (pagerParent != null && pagerParent instanceof ViewGroup) {
            ((ViewGroup) pagerParent).removeView(pager);
        }
        mHeaderView = headerView;
        mNavView = navView;
        mViewPager = pager;
        mInnerScrollViewId = innerScrollViewId;
    }

    public View createHeaderView() {
        return mHeaderView;
    }

    public View createNavView() {
        return mNavView;
    }

    public ViewPager createViewPager() {
        return mViewPager;
    }

    @Override
    public ViewGroup createContent() {
        return null;
    }

    @Override
    public ViewGroup createInnerScrollView(ViewPager viewPager) {
        ViewGroup mInnerScrollView = null;
        PagerAdapter adapter = null;
        if (viewPager != null && (adapter = viewPager.getAdapter()) != null && adapter.getCount() > 0) {
            int currentItem = viewPager.getCurrentItem();
            Object obj = adapter.instantiateItem(viewPager, currentItem);
            if (obj instanceof Fragment) {
                Fragment item = (Fragment) adapter.instantiateItem(viewPager, currentItem);
                if (item != null && item.getView() != null) {
                    mInnerScrollView = (ViewGroup) (item.getView()
                            .findViewById(mInnerScrollViewId));
                }

            }else if(obj instanceof androidx.fragment.app.Fragment){
                androidx.fragment.app.Fragment item = (androidx.fragment.app.Fragment) adapter.instantiateItem(viewPager, currentItem);
                if (item != null && item.getView() != null) {
                    mInnerScrollView = (ViewGroup) (item.getView()
                            .findViewById(mInnerScrollViewId));
                }
            }
//            else if (obj instanceof android.support.v4.app.Fragment) {
//                android.support.v4.app.Fragment item =
//                        (android.support.v4.app.Fragment) adapter.instantiateItem(viewPager, currentItem);
//                if (item != null && item.getView() != null) {
//                    mInnerScrollView = (ViewGroup) (item.getView()
//                            .findViewById(mInnerScrollViewId));
//                }
//            }
        }
        return mInnerScrollView;
    }

    @Override
    public List<ViewGroup> getInnerScrollViewList(ViewPager viewPager) {
        if (viewPager == null) return null;
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null || adapter.getCount() == 0) return null;
        if (adapter instanceof FragmentPagerAdapter) {
            FragmentPagerAdapter fragmentPagerAdapter = (FragmentPagerAdapter) adapter;
            List<ViewGroup> childs = new ArrayList<>();
            for (int i = 0; i < adapter.getCount(); i++) {
//                final long itemId = fragmentPagerAdapter.getItemId(i);
//                final FragmentManager fm = fragmentPagerAdapter.
//                 Do we already have this fragment?
//                String name = makeFragmentName(container.getId(), itemId);
//                android.support.v4.app.Fragment fragment = mFragmentManager.findFragmentByTag(name);

                androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) fragmentPagerAdapter.instantiateItem(viewPager, i);
                if (fragment != null && fragment.getView() != null ) {
                    ViewGroup viewGroup = fragment.getView().findViewById(mInnerScrollViewId);
                    if(viewGroup!=null){
                        childs.add(viewGroup);
                    }
                }
//                android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) fragmentPagerAdapter.instantiateItem(viewPager, i);
//                if (fragment != null && fragment.getView() != null ) {
//                    ViewGroup viewGroup = fragment.getView().findViewById(mInnerScrollViewId);
//                    if(viewGroup!=null){
//                        childs.add(viewGroup);
//                    }
//                }
            }
            return childs;
        }
        return null;
    }


}
