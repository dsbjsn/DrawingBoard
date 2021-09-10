package com.stickermaker.whatsapp.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

import com.stickermaker.whatsapp.utils.DpiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2021/8/4 18
 *
 * @author xjl
 */
public class TextInfo extends BaseInfo {
    private static final String TAG = "TextInfo";
    /**
     * 文本信息
     */
    private TextBean mTextBean;

    public TextBean getTextBean() {
        return mTextBean;
    }

    //换行位置
    private List<Integer> linesEndIndex = new ArrayList<>();

    public void refreshTextBean(TextBean pTextBean) {
        mTextBean = pTextBean;
        initTextBaseBitmap(canvasWidth - 50, canvasHeight);
    }

    public void setTextBean(TextBean pTextBean) {
        mTextBean = pTextBean;
    }

    @Override
    public void firstDrawBitmap(int w, int h) {
        if (mBaseBitmap == null) {
            initTextBaseBitmap(w - 50, h);
        }
        super.firstDrawBitmap(w, h);
    }

    private void initTextBaseBitmap(int maxWidth, int maxHeight) {
        /**
         * 画baseBitmap的画笔
         */
        TextPaint baseTextPaint = new TextPaint();
        Log.i("onDraw", "文字信息=" + mTextBean.toString());
        baseTextPaint.setAntiAlias(true);
        baseTextPaint.setColor(mTextBean.getColor());
        baseTextPaint.setTextSize(mTextBean.getSize());

        Typeface lTypeface = Typeface.createFromFile(mTextBean.getTtf_path());
        baseTextPaint.setTypeface(lTypeface);


        baseWidth = maxWidth;
        Log.i("文本最大宽", maxWidth + "");
        int startPoint = 0;
        int endPoint = mTextBean.getText().length();

        List<Integer> lIntegers = new ArrayList<>();
        boolean going = true;

        int lastLineWidth = 0;
        while (going) {
            Rect rect = new Rect();
            baseTextPaint.getTextBounds(mTextBean.getText(), startPoint, endPoint, rect);
            int width = rect.width() + 40;
            lastLineWidth = width;
            if (width > maxWidth) {
                endPoint -= 1;
            } else {
                lIntegers.add(endPoint);

                if (endPoint < mTextBean.getText().length()) {
                    startPoint = endPoint;
                    endPoint = mTextBean.getText().length();
                } else {
                    going = false;
                    if (lIntegers.size() == 1) {
                        baseWidth = width;
                    }
                }
            }
        }

        Log.i("换行的位置", lIntegers.toString());

        Paint.FontMetrics fontMetrics = baseTextPaint.getFontMetrics();
        baseHeight = ((int) (fontMetrics.descent - fontMetrics.ascent)) * lIntegers.size() + 40;

        Log.i("文本宽", baseWidth + "");
        Log.i("文本高", baseHeight + "");

        if (mBaseBitmap != null) {
            mBaseBitmap.recycle();
            mBaseBitmap = null;
        }
        mBaseBitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);

        Canvas baseBitmapCanvas = new Canvas(mBaseBitmap);

        lIntegers.add(0, 0);
        for (int i = 1; i < lIntegers.size(); i++) {
            String text = mTextBean.getText().substring(lIntegers.get(i - 1), lIntegers.get(i));
            if (i == lIntegers.size() - 1) {
                baseBitmapCanvas.drawText(text, (baseWidth - lastLineWidth) / 2 + 20, (Math.abs(fontMetrics.ascent) + 20) * i, baseTextPaint);
            } else {
                baseBitmapCanvas.drawText(text, 20, (Math.abs(fontMetrics.ascent) + 20) * i, baseTextPaint);
            }
        }

        if (hadInitCache) {
            postMatrixTranslate(0, 0);
        }
    }

    @Override
    public void postMatrixTranslate(float x, float y) {
        super.postMatrixTranslate(x, y);
        mCanvas.drawBitmap(mBaseBitmap, mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }

    @Override
    public void postMatrixScale(float scale, Point centerPoint) {
        super.postMatrixScale(scale, centerPoint);
        mCanvas.drawBitmap(mBaseBitmap, mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }


    @Override
    public void postMatrixRotate(float angle, Point midPoint) {
        super.postMatrixRotate(angle, midPoint);
        mCanvas.drawBitmap(mBaseBitmap, mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }

    @Override
    public void setMatrixValues(float[] pValues) {
        super.setMatrixValues(pValues);
        mCanvas.drawBitmap(mBaseBitmap, mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }

    public static class Builder {
        private TextBean mTextBean;

        public Builder setTextBean(TextBean pTextBean) {
            this.mTextBean = pTextBean;
            return this;
        }

        private boolean canDrag = true;
        private boolean singleCtrlDrag = true;
        private boolean canScale = true;
        private boolean canRotate = true;
        private boolean canClip = false;
        private boolean canRectClip = false;
        private boolean canCircleClip = false;
        private boolean canRubber = false;
        private boolean canGraffiti = false;

        private boolean centerCrop = false;
        private boolean limitRect = false;
        private boolean showStoker = true;
        private boolean selectShowStoker = false;

        private boolean canDelete = true;

        public Builder setCanDrag(boolean pCanDrag) {
            this.canDrag = pCanDrag;
            return this;
        }

        public Builder setSingleCtrlDrag(boolean pSingleCtrlDrag) {
            this.singleCtrlDrag = pSingleCtrlDrag;
            return this;
        }

        public Builder setCanScale(boolean pCanScale) {
            this.canScale = pCanScale;
            return this;
        }

        public Builder setCanRotate(boolean pCanRotate) {
            this.canRotate = pCanRotate;
            return this;
        }

        public Builder setCanClip(boolean pCanClip) {
            this.canClip = pCanClip;
            return this;
        }

        public Builder setCanRectClip(boolean pCanRectClip) {
            this.canRectClip = pCanRectClip;
            return this;
        }

        public Builder setCanCircleClip(boolean pCanCircleClip) {
            this.canCircleClip = pCanCircleClip;
            return this;
        }

        public Builder setCanRubber(boolean pCanRubber) {
            this.canRubber = pCanRubber;
            return this;
        }

        public Builder setCanGraffiti(boolean pCanGraffiti) {
            this.canGraffiti = pCanGraffiti;
            return this;
        }

        public Builder setCenterCrop(boolean pCenterCrop) {
            this.centerCrop = pCenterCrop;
            return this;
        }

        public Builder setLimitRect(boolean pLimitRect) {
            limitRect = pLimitRect;
            return this;
        }

        public Builder setShowStoker(boolean pShowStoker) {
            showStoker = pShowStoker;
            return this;
        }

        public Builder setSelectShowStoker(boolean pSelectShowStoker) {
            selectShowStoker = pSelectShowStoker;
            return this;
        }

        public Builder setCanDelete(boolean pCanDelete) {
            canDelete = pCanDelete;
            return this;
        }

        public TextInfo build() {
            TextInfo lTextInfo = new TextInfo();

            lTextInfo.setType(Type.TXT);
            lTextInfo.setTextBean(mTextBean);

            lTextInfo.setCanDrag(canDrag);
            lTextInfo.setSingleCtrlDrag(singleCtrlDrag);
            lTextInfo.setCanRotate(canRotate);
            lTextInfo.setCanScale(canScale);

            lTextInfo.setCanClip(canClip);
            lTextInfo.setCanRectClip(canRectClip);
            lTextInfo.setCanCircleClip(canCircleClip);

            lTextInfo.setCanGraffiti(canGraffiti);
            lTextInfo.setCanRubber(canRubber);

            lTextInfo.setCenterCrop(centerCrop);
            lTextInfo.setLimitRect(limitRect);
            lTextInfo.setShowStoker(showStoker);
            lTextInfo.setSelectShowStoker(selectShowStoker);
            lTextInfo.setCanDelete(canDelete);
            return lTextInfo;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "canDrag=" + canDrag +
                    ", singleCtrlDrag=" + singleCtrlDrag +
                    ", canScale=" + canScale +
                    ", canRotate=" + canRotate +
                    ", canClip=" + canClip +
                    ", canRectClip=" + canRectClip +
                    ", canCircleClip=" + canCircleClip +
                    ", canRubber=" + canRubber +
                    ", canGraffiti=" + canGraffiti +
                    ", centerCrop=" + centerCrop +
                    ", limitRect=" + limitRect +
                    ", showStoker=" + showStoker +
                    ", selectShowStoker=" + selectShowStoker +
                    ", canDelete=" + canDelete +
                    '}';
        }
    }
}
