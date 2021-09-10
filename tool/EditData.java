package com.stickermaker.whatsapp.tool;

import android.graphics.Matrix;

/**
 * 记录操作步骤
 * Created on 2021/7/21 10
 *
 * @author xjl
 */
public class EditData {
    /**
     * 标识
     */
    int tag = 0;
    /**
     * 操作类型
     */
    EditModel mEditModel;

    /**
     * 形变矩阵
     */
    Matrix mMatrix;

    /**
     * 涂鸦的数据的数据
     */
    GraffitiData mGraffitiData;

    public int getTag() {
        return tag;
    }

    public void setTag(int pTag) {
        tag = pTag;
    }

    public EditModel getEditModel() {
        return mEditModel;
    }

    public void setEditModel(EditModel pEditModel) {
        mEditModel = pEditModel;
    }

    public GraffitiData getGraffitiData() {
        return mGraffitiData;
    }

    public void setGraffitiData(GraffitiData pGraffitiData) {
        mGraffitiData = pGraffitiData;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(Matrix pMatrix) {
        mMatrix = pMatrix;
    }

    @Override
    public String toString() {
        return "EditData{" +
                "tag=" + tag +
                ", mEditModel=" + mEditModel +
                ", mMatrix=" + mMatrix +
                ", mGraffitiData=" + mGraffitiData +
                '}';
    }
}
