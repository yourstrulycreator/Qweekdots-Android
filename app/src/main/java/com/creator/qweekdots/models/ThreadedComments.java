package com.creator.qweekdots.models;

import java.util.ArrayList;
import java.util.List;

class ThreadedComments {
    static List<CommentItem> toThreadedComments(List<CommentItem> comments) {
        //The resulting array of threaded comments
        List<CommentItem> threaded = new ArrayList<>();
        //An array used to hold processed comments which should be removed at the end of the cycle
        List<CommentItem> removeComments = new ArrayList<>();
        //get the root comments first (comments with no parent)
        for(int i = 0; i < comments.size(); i++) {
            CommentItem c = comments.get(i);
            if(c.getParent_Id() == 0) {
                c.setCommentDepth(0); //A property of Comment to hold its depth
                c.setChildCount(0); //A property of Comment to hold its child count
                threaded.add(c);
                removeComments.add(c);
            }
        }
        if(removeComments.size() > 0) {
            //clear processed comments
            comments.removeAll(removeComments);
            removeComments.clear();
        }
        int depth = 0;
        //get the child comments up to a max depth of 10
        while(comments.size() > 0 && depth <= 10) {
            depth++;
            for(int j = 0; j< comments.size(); j++) {
                CommentItem child = comments.get(j);
                //check root comments for match
                for(int i = 0; i < threaded.size(); i++) {
                    CommentItem parent = threaded.get(i);
                    if(parent.getId() == child.getParent_Id()) {
                        parent.setChildCount(parent.getChildCount()+1);
                        child.setCommentDepth(depth+parent.getCommentDepth());
                        threaded.add(i+parent.getChildCount(),child);
                        removeComments.add(child);
                    }
                }
            }
            if(removeComments.size() > 0) {
                //clear processed comments
                comments.removeAll(removeComments);
                removeComments.clear();
            }
        }
        return threaded;
    }
}
