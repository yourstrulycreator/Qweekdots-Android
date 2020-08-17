package com.creator.qweekdots.presenter;

import com.creator.qweekdots.adapter.view.IGifSearchView;
import com.tenor.android.core.presenter.IBasePresenter;
import com.tenor.android.core.response.impl.GifsResponse;

import retrofit2.Call;

public interface IGifSearchPresenter extends IBasePresenter<IGifSearchView> {
    Call<GifsResponse> search(String query, int limit, String pos, final boolean isAppend);
}