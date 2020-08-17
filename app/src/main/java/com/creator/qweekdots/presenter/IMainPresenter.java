package com.creator.qweekdots.presenter;

import android.content.Context;

import com.creator.qweekdots.adapter.view.IMainView;
import com.tenor.android.core.presenter.IBasePresenter;
import com.tenor.android.core.response.impl.TagsResponse;

import java.util.List;

import retrofit2.Call;

public interface IMainPresenter extends IBasePresenter<IMainView> {
    Call<TagsResponse> getTags(Context context, List<String> categories);
}
