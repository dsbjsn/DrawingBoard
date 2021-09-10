package com.stickermaker.whatsapp.tool;

import com.stickermaker.whatsapp.utils.DpiUtil;

import java.io.Serializable;

/**
 * Created on 2021/8/4 14
 *
 * @author xjl
 */
public class TextBean implements Serializable {
    private String text="";
    private String ttf_path="";
    private int color=0;
    private int size= DpiUtil.dipTopx(30);

    public String getText() {
        return text;
    }

    public void setText(String pText) {
        text = pText;
    }

    public String getTtf_path() {
        return ttf_path;
    }

    public void setTtf_path(String pTtf_path) {
        ttf_path = pTtf_path;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int pColor) {
        color = pColor;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int pSize) {
        size = pSize;
    }

    @Override
    public String toString() {
        return "TextInfo{" +
                "text='" + text + '\'' +
                ", ttf_path='" + ttf_path + '\'' +
                ", color=" + color +
                ", size=" + size +
                '}';
    }
}
