package com.creator.qweekdots.utils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

    private LinearLayoutManager layoutManager;

    /**
     * Supporting only LinearLayoutManager for now.
     *
     */
    protected PaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems();
            }
        }

    }

    protected abstract void loadMoreItems();

    public abstract int getTotalPageCount();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

}
