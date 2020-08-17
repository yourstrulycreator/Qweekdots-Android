package com.creator.qweekdots.sdk.view;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tenor.android.core.view.IBaseView;

import java.util.List;

public interface ISearchSuggestionView extends IBaseView {

    void onReceiveSearchSuggestionsSucceeded(@NonNull String query, @NonNull List<String> suggestions);

    void onReceiveSearchSuggestionsFailed(@NonNull String query, @Nullable Exception error);
}

