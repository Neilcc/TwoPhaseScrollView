package com.zcc.view.twophase;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


/**
 * Created by wangzhi on 15/10/28.
 */
public class ScrollAbleViewHelper {
    static boolean isScrollToTop(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return true;
        }

        if (viewGroup instanceof InnerScrollInterface) {
            InnerScrollInterface innerScrollInterface = (InnerScrollInterface) viewGroup;
            return innerScrollInterface.isScrollToTop();
        } else if (viewGroup instanceof ScrollView) {
            return viewGroup.getScrollY() == 0;
        } else if (viewGroup instanceof ListView) {
            ListView lv = (ListView) viewGroup;
            View child = lv.getChildAt(lv.getFirstVisiblePosition());
            return child != null && child.getTop() == 0;
        } else if (viewGroup instanceof RecyclerView) {
            RecyclerView rv = (RecyclerView) viewGroup;
            return !rv.canScrollVertically(-1);
//            return android.support.v4.view.ViewCompat.canScrollVertically(rv, -1);
        } else if (viewGroup instanceof NestedScrollView) {
            NestedScrollView view = (NestedScrollView) viewGroup;
            return view.getScrollY() == 0;
        } else if (viewGroup instanceof RelativeLayout) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child.getVisibility() == View.VISIBLE && child instanceof RecyclerView) {
                    RecyclerView rv = (RecyclerView) child;
                    return !rv.canScrollVertically(-1);
                }
            }
            return true;
        }

//        else if (viewGroup instanceof PullToRefreshListView) {
//            ListView lv = ((PullToRefreshListView) viewGroup).getRefreshableView();
//            if (lv != null && lv.getChildAt(lv.getFirstVisiblePosition()) != null) {
//                return lv.getChildAt(lv.getFirstVisiblePosition()).getTop() == 0;
//            }
//        } else if (viewGroup instanceof PictureWall) {
//            PictureWall pictureWall = (PictureWall) viewGroup;
//            RecyclerView rv = (RecyclerView) pictureWall.getRefreshView();
//            PictureWallLayoutManager lm = (PictureWallLayoutManager) rv.getLayoutManager();
//            PictureWallAdapter adapter = (PictureWallAdapter) rv.getAdapter();
//            if (rv.getAdapter().getItemCount() == adapter.getHeadersCount() + adapter.getFootersCount()) {
//                if (lm.findViewByPosition(lm.getFirstChildPosition()).getTop() == 0 && lm.getFirstChildPosition() == 0)
//                    return true;
//            }
//
//            if (lm.findViewByPosition(lm.getFirstChildPosition()).getTop() == pictureWall.getFirstLineVerticalDividerLength() && lm.getFirstChildPosition() == 0)
//                return true;
//        }

        return false;
    }

    static void scrollToTop(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        } else if (viewGroup instanceof ListView) {
            ((ListView) viewGroup).setSelection(0);
        } else if (viewGroup instanceof ScrollView) {
            viewGroup.scrollTo(0, 0);
        } else if (viewGroup instanceof NestedScrollView) {
            ((NestedScrollView) viewGroup).scrollTo(0, 0);
        }

//        else if (viewGroup instanceof PullToRefreshListView) {
//            ListView lv = ((PullToRefreshListView) viewGroup).getRefreshableView();
//            lv.setSelection(0);
//        }
    }

    static void scrollAllChildToTop(List<ViewGroup> viewGroups) {
        if (viewGroups == null || viewGroups.size() == 0) {
            return;
        }
        for (ViewGroup viewGroup : viewGroups) {
            scrollToTop(viewGroup);
        }
    }


    public interface InnerScrollInterface {
        boolean isScrollToTop();
    }

}
