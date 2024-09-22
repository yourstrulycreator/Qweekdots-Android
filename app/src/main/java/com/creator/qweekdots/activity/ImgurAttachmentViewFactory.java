package com.creator.qweekdots.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.creator.qweekdots.R;
import com.creator.qweekdots.databinding.AttachmentImgurBinding;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.jetbrains.annotations.NotNull;

import coil.Coil;
import coil.request.ImageRequest;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.ui.message.list.MessageListItemStyle;
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer;
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory;

public class ImgurAttachmentViewFactory extends AttachmentViewFactory {
    @NotNull
    @Override
    public View createAttachmentView(@NotNull MessageListItem.MessageItem data,
                                     @Nullable MessageListListenerContainer listeners,
                                     @NotNull MessageListItemStyle style,
                                     @NotNull ViewGroup parent) {
        Attachment imgurAttachment = null;
        for (Attachment attachment : data.getMessage().getAttachments()) {
            String imageUrl = attachment.getImageUrl();
            if (imageUrl != null && imageUrl.contains("imgur")) {
                imgurAttachment = attachment;
                break;
            }
        }

        if (imgurAttachment != null) {
            return createImgurAttachmentView(imgurAttachment, parent);
        } else {
            return super.createAttachmentView(data, listeners, style, parent);
        }
    }

    private View createImgurAttachmentView(Attachment imgurAttachment, ViewGroup parent) {
        AttachmentImgurBinding binding = AttachmentImgurBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        float cornerRadius = binding.getRoot()
                .getResources()
                .getDimension(R.dimen.stream_ui_selected_attachment_corner_radius);
        ShapeAppearanceModel model = binding.ivMediaThumb.getShapeAppearanceModel()
                .toBuilder()
                .setAllCornerSizes(cornerRadius)
                .build();
        binding.ivMediaThumb.setShapeAppearanceModel(model);

        ImageRequest imageRequest = new ImageRequest.Builder(parent.getContext())
                .data(imgurAttachment.getImageUrl())
                .allowHardware(false)
                .crossfade(true)
                .placeholder(R.drawable.stream_ui_picture_placeholder)
                .target(binding.ivMediaThumb)
                .build();
        Coil.imageLoader(parent.getContext()).enqueue(imageRequest);

        return binding.getRoot();
    }
}
