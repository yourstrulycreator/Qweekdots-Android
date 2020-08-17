package com.creator.qweekdots.sdk.presenter;

import androidx.annotation.NonNull;

import com.creator.qweekdots.sdk.view.ISearchSuggestionView;
import com.tenor.android.core.presenter.IBasePresenter;
import com.tenor.android.core.response.impl.SearchSuggestionResponse;

import retrofit2.Call;

public interface ISearchSuggestionPresenter extends IBasePresenter<ISearchSuggestionView> {
    Call<SearchSuggestionResponse> getSearchSuggestions(@NonNull String query);
}
