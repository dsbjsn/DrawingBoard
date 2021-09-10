package com.stickermaker.whatsapp.tool;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created on 2021/7/21 11
 * 涂鸦信息
 *
 * @author xjl
 */
public class GraffitiData {
    public GraffitiData(Path pPath, Paint pPaint) {
        mPath = pPath;
        mPaint = pPaint;
    }

    Paint mPaint;
    Path mPath;

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint pPaint) {
        mPaint = pPaint;
    }

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path pPath) {
        mPath = pPath;
    }
}
