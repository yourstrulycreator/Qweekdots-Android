package com.creator.qweekdots.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.TagsAdapter;
import com.creator.qweekdots.adapter.decorations.MainTagsItemDecoration;
import com.creator.qweekdots.adapter.rvitem.TagRVItem;
import com.creator.qweekdots.adapter.view.IMainView;
import com.creator.qweekdots.presenter.IMainPresenter;
import com.creator.qweekdots.presenter.impl.MainPresenter;
import com.creator.qweekdots.widget.TenorStaggeredGridLayoutManager;
import com.tenor.android.core.constant.StringConstant;
import com.tenor.android.core.model.impl.Tag;
import com.tenor.android.core.network.ApiClient;
import com.tenor.android.core.network.ApiService;
import com.tenor.android.core.network.IApiClient;
import com.tenor.android.core.response.BaseError;
import com.tenor.android.core.util.AbstractUIUtils;
import com.tenor.android.core.widget.adapter.AbstractRVItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static maes.tech.intentanim.CustomIntent.customType;

public class ReactionsActivity extends AppCompatActivity implements IMainView {
    // Number of columns for the RecyclerView
    private static final int STAGGERED_GRID_LAYOUT_COLUMN_NUMBER = 2;
    // Minimum length a search term can be
    private static final int TEXT_QUERY_MIN_LENGTH = 2;

    // A search box for entering a search term
    public EditText mEditText;
    // RecyclerView to display the stream of Tags
    public RecyclerView mRecyclerView;

    // Adapter containing the tag items/view holders
    private TagsAdapter mTagsAdapter;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactions);

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // Create a builder for ApiService
        ApiService.IBuilder<IApiClient> builder = new ApiService.Builder<>(this, IApiClient.class);

        // add your tenor API key here
        builder.apiKey("WGZ6FYN2IEVZ");

        // initialize the Tenor ApiClient
        ApiClient.init(this, builder);

        mEditText = findViewById(R.id.am_et_search);
        mEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {

            final String query = textView.getText().toString().trim();

            if (query.length() < TEXT_QUERY_MIN_LENGTH) {
                Toasty.error(ReactionsActivity.this, getString(R.string.search_error), Toast.LENGTH_LONG).show();
                return true;
            }

            // The keyboard enter will perform the search
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch(query);
                return true;
            }
            return false;
        });

        mRecyclerView = findViewById(R.id.am_rv_tags);
        mRecyclerView.addItemDecoration(new MainTagsItemDecoration(getContext(), AbstractUIUtils.dpToPx(this, 2)));

        // Two column, vertical display
        final TenorStaggeredGridLayoutManager layoutManager = new TenorStaggeredGridLayoutManager(STAGGERED_GRID_LAYOUT_COLUMN_NUMBER,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mTagsAdapter = new TagsAdapter<>(this);
        mRecyclerView.setAdapter(mTagsAdapter);

        // Api calls for MainActivity performed here
        IMainPresenter mPresenter = new MainPresenter(this);
        mPresenter.getTags(getContext(), null);

    }

    private void startSearch(@Nullable final CharSequence text) {
        final String query = !TextUtils.isEmpty(text) ? text.toString().trim() : StringConstant.EMPTY;
        Intent intent = new Intent(this, SearchReactionsActivity.class);
        intent.putExtra(SearchReactionsActivity.KEY_QUERY, query);
        startActivity(intent);
        customType(this, "right-to-left");
    }

    @Override
    public Context getContext() {
        return getBaseContext();
    }

    @Override
    public void onReceiveReactionsSucceeded(List<Tag> tags) {

        // Map the tags into a list of TagRVItem for the mTagsAdapter
        List<AbstractRVItem> list = new ArrayList<>();
        for (Tag tag : tags) {
            list.add(new TagRVItem(TagsAdapter.TYPE_REACTION_ITEM, tag));
        }
        mTagsAdapter.insert(list, false);
    }

    @Override
    public void onReceiveReactionsFailed(BaseError error) {
        // For now, we will just display nothing if the tags fail to return
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
