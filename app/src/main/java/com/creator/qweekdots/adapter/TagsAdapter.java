package com.creator.qweekdots.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.holder.TagItemVH;
import com.creator.qweekdots.adapter.rvitem.TagRVItem;
import com.creator.qweekdots.activity.ReactionsActivity;
import com.tenor.android.core.model.impl.Tag;
import com.tenor.android.core.util.AbstractListUtils;
import com.tenor.android.core.widget.adapter.ListRVAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Adapter to display Tags as a list, with the option either multiple columns or multiple rows
 * depending on orientation given by the LayoutManager.
 */
public class TagsAdapter<CTX extends ReactionsActivity>
        extends ListRVAdapter<CTX, TagRVItem, TagItemVH<CTX>> {

    public static final int TYPE_REACTION_ITEM = 0;

    private String dropTxt;

    public TagsAdapter(@Nullable CTX ctx, String droptext) {
        super(ctx);
        dropTxt = droptext;
    }

    @Override
    public void insert(@Nullable List<TagRVItem> list, boolean isAppend) {
        if (!isAppend) {
            getList().clear();
            if (AbstractListUtils.isEmpty(list)) {
                notifyDataSetChanged();
                return;
            }
        }

        if (AbstractListUtils.isEmpty(list)) {
            return;
        }

        final int start = getItemCount();
        final int size = list.size();
        getList().addAll(list);

        if (!isAppend) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeChanged(start, size);
        }
    }

    @NotNull
    @Override
    public TagItemVH<CTX> onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create Tag view holder
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new TagItemVH<>(
                layoutInflater.inflate(R.layout.item_tag, parent, false),
                getRef(), dropTxt);
    }

    @Override
    public void onBindViewHolder(TagItemVH<CTX> holder, int position) {
        final Tag reactionTag = getList().get(position).getTag();
        holder.render(reactionTag);
    }

    @Override
    public int getItemCount() {
        return getList().size();
    }
}
