package com.creator.qweekdots.snaps.gallery;

import java.util.List;

public interface GalleySelectedListener {

    void onSingleSelect(String address);
    void onMultiSelect(List<String> addresses);

}
