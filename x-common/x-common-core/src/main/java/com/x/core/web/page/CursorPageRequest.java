package com.x.core.web.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class CursorPageRequest {

    public static final String START_CURSOR_DEFAULT = "0";

    /** 每页显示记录数 */
    @ApiModelProperty(value = "每页数量")
    private Integer pageSize;

    @ApiModelProperty(value = "分页游标, 第一页请求cursor是0, response中会返回下一页请求用到的cursor, 同时response还会返回has_more来表明是否有更多的数据。")
    private String cursor;

    @JsonIgnore
    public Long getDescOrderCursor(){
        if (START_CURSOR_DEFAULT.equals(cursor)){
            return Long.MAX_VALUE;
        }
        return Long.parseLong(cursor);
    }

    @JsonIgnore
    public Long getLongCursor(){
        return Long.parseLong(cursor);
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}