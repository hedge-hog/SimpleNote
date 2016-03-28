package com.hedgehog.note.event;

import android.support.annotation.IntDef;

/**
 * Created by ciwei on 2016/3/26.
 */
public class NotifyEvent<T>{
    public static final int REFRESH_LIST = 0;
    public static final int CREATE_NOTE = 1;
    public static final int UPDATE_NOTE = 2;
    public static final int CHANGE_THEME = 3;
    public static final int CHANGE_ITEM_LAYOUT = 4;
    public static final int CHANGE_MENU_GRAVITY = 5;
    private int type;
    private T data;
    @IntDef({REFRESH_LIST, CREATE_NOTE, UPDATE_NOTE, CHANGE_THEME,
            CHANGE_ITEM_LAYOUT, CHANGE_MENU_GRAVITY})
    public @interface Type {
    }

    public @Type int getType() {
        return type;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}