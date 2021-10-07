package com.x.provider.video.enums;

import com.x.core.web.api.IErrorCode;

public enum  VideoErrorEnum implements IErrorCode {
    VIDEO_TITLE_REVIEW_BLOCKED(92060001L, "视频标题审核没有通过哦，请调整视频标题"),
    COMMENT_REVIEW_BLOCKED(92060002L, "评论内容审核没有通过哦，请调整评论内容"),
        ;
    private long code;
    private String message;

    VideoErrorEnum(long code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public long getCode() {
        return 0;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
