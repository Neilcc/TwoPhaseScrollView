package com.zcc.view.twophase;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.ViewPager;

import java.util.List;

/**
 * Created by zhuchengcheng on 15/10/29.
 */
public interface StickyNavFactoryInterface {

    View createHeaderView();

    View createNavView();

    ViewPager createViewPager();

    ViewGroup createContent();

    ViewGroup createInnerScrollView(ViewPager viewPager);

    List<ViewGroup> getInnerScrollViewList(ViewPager viewPager);
}
