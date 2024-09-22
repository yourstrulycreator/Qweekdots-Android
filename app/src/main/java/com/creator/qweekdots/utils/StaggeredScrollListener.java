package com.creator.qweekdots.utils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.jetbrains.annotations.NotNull;

public abstract class StaggeredScrollListener extends RecyclerView.OnScrollListener {

    private StaggeredGridLayoutManager layoutManager;

    /**
     * Supporting only LinearLayoutManager for now.
     *
     */
    protected StaggeredScrollListener(StaggeredGridLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int[] firstVisibleItems = null;
        int pastVisibleItems = 0;
        firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems);
        if(firstVisibleItems != null && firstVisibleItems.length > 0) {
            pastVisibleItems = firstVisibleItems[0];
        }

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                loadMoreItems();
            }
        }

    }

    protected abstract void loadMoreItems();

    public abstract int getTotalPageCount();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

}
