package com.creator.qweekdots.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.creator.qweekdots.activity.StreamChatActivity;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.databinding.StreamchatMainBinding;
import com.creator.qweekdots.prefs.DarkModePrefManager;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.models.Filters;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.livedata.ChatDomain;
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel;
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModelBinding;
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory;

import static java.util.Collections.singletonList;

public class StreamChatMain extends Fragment {
    View rootView;
    private StreamchatMainBinding binding;
    private String streamID, username, avatar, apiKey;
    private final String TAG = StreamChatMain.class.getSimpleName();
    private ChatClient client;

    public StreamChatMain(ChatClient client) {
        this.client = client;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        apiKey = "4rhx4wbfxrca";
        streamID = AppController.getInstance().getPrefManager().getStreamUser().getId();
        username = AppController.getInstance().getPrefManager().getUser().getUserName();
        avatar = AppController.getInstance().getPrefManager().getUser().getAvatar();

        // Step 0 - inflate binding
        binding = StreamchatMainBinding.inflate(inflater,container,false);

        // Step 1 - Set up the client for API calls and the domain for offline storage
        //ChatClient client = new ChatClient.Builder(apiKey, getActivity().getApplicationContext()).internalBuild();

        new ChatDomain.Builder(client, getActivity().getApplicationContext()).build();

        // Step 2 - Authenticate and connect the user
        User user = new User();
        user.setId(username);
        user.setName("q/"+username);
        user.setImage(avatar);

        client.connectUser(
                user,
                streamID
        ).enqueue();

        // Step 3 - Set the channel list filter and order
        // This can be read as requiring only channels whose "type" is "messaging" AND
        // whose "members" include our "user.id"
        FilterObject filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.in("members", singletonList(user.getId()))
        );

        ChannelListViewModelFactory factory = new ChannelListViewModelFactory(
                filter,
                ChannelListViewModel.DEFAULT_SORT
        );

        ChannelListViewModel channelsViewModel = new ViewModelProvider(this).get(ChannelListViewModel.class);

        // Step 4 - Connect the ChannelListViewModel to the ChannelListView, loose
        //          coupling makes it easy to customize
        ChannelListViewModelBinding.bind(channelsViewModel, binding.channelListView, this);
        binding.channelListView.setChannelItemClickListener(
                channel -> startActivity(StreamChatActivity.newIntent(requireContext(), channel))
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "hidden: ");
    }
}
