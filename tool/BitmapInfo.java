package com.stickermaker.whatsapp.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片信息对象
 * Created on 2021/7/20 09
 *
 * @author xjl
 */
public class BitmapInfo extends BaseInfo {
    /**
     * 操作时缓存的图片
     */
    protected List<Bitmap> cacheBitmapList = new ArrayList<>();

    /**
     * 撤销操作时回退的图片
     */
    protected List<Bitmap> repealBitmapList = new ArrayList<>();


    /**
     * 最后的擦除操作
     */
    private List<EditData> allRubberAction = new ArrayList<>();
    /**
     * 所有的裁剪区域
     */
    private List<Path> allClipPaths = new ArrayList<>();

    /**
     * 是否是清除橡皮擦模式
     */
    private boolean cleanRubberModel = false;

    public boolean isCleanRubberModel() {
        return cleanRubberModel;
    }

    public void setCleanRubberModel(boolean pCleanRubberModel, List<EditData> pAllRubberAction, List<Path> pAllClipPaths) {
        cleanRubberModel = pCleanRubberModel;

        if (cleanRubberModel) {
            allRubberAction.addAll(pAllRubberAction);
            allClipPaths.addAll(pAllClipPaths);
        }
    }

    @Override
    public void setBaseBitmap(Bitmap pBaseBitmap) {
        super.setBaseBitmap(pBaseBitmap);
        baseWidth = mBaseBitmap.getWidth();
        baseHeight = mBaseBitmap.getHeight();

        /**
         * 将初始图片添加到缓存列表
         */
        cacheBitmapList.add(mBaseBitmap);
    }


    @Override
    public void firstDrawBitmap(int w, int h) {
        super.firstDrawBitmap(w, h);
        drawRubberRect();
    }


    public void refreshBaseBitmap(Bitmap pBitmap) {
        mBaseBitmap = pBitmap;
        baseWidth = mBaseBitmap.getWidth();
        baseHeight = mBaseBitmap.getHeight();

        cacheBitmapList.clear();
        cacheBitmapList.add(mBaseBitmap);


        mCanvasMatrix = new Matrix();
        mCanvasMatrix.reset();
        mCanvasMatrix.postTranslate(Math.abs(canvasWidth - baseWidth) / 2, Math.abs(canvasHeight - baseHeight) / 2);

        if (isCenterCrop()) {
            float scale_x = (float) canvasWidth / baseWidth;
            float scale_y = (float) canvasHeight / baseHeight;
            float scale = Math.min(scale_x, scale_y) - 0.2f;

            minScale = scale;
            Log.i(TAG, "minScale=" + minScale);
            mCanvasMatrix.postScale(scale, scale, canvasWidth / 2, canvasHeight / 2);
        }

        mCacheMatrix = new Matrix();
        mCacheMatrix.set(mCanvasMatrix);

        drawBg();
        mCanvas.drawBitmap(mBaseBitmap, mCanvasMatrix, null);
        drawBaseBitmapStoker();

        postMatrixTranslate(0, 0);
    }

    /**
     * 画擦除区域，如果有
     */
    private void drawRubberRect() {
        Log.i(TAG, "画已有的橡皮擦区域");
        for (EditData lEditData : allRubberAction) {
            if (lEditData != null && lEditData.getEditModel() == EditModel.RUBBER) {
                mCanvas.save();

                Path lPath = new Path();
                Matrix lMatrix = new Matrix();
                lEditData.getMatrix().invert(lMatrix);
                lPath.addPath(lEditData.getGraffitiData().getPath(), lMatrix);

                Paint lPaint = new Paint();
                lPaint.set(lEditData.getGraffitiData().getPaint());

                if (cleanRubberModel) {
                    lPaint.setXfermode(null);
                    lPaint.setColor(Color.BLUE);
                    lPaint.setAlpha(100);
                }

                mCanvas.drawPath(lEditData.getGraffitiData().getPath(), lPaint);
                mCanvas.restore();
            }
        }
    }

    /**
     * 清空撤销的缓存图片
     */
    public void cleanRepealCache() {
        repealBitmapList.clear();
    }

    /**
     * 撤销
     *
     * @param pRepealEditData 撤销的操作
     * @param pLastEditData   撤销后最后的操作
     */
    public void repealAction(EditData pRepealEditData, EditData pLastEditData) {
        if (pRepealEditData == null) {
            Log.d("撤销", "撤销的操作错误");
            return;
        } else {
            switch (pRepealEditData.getEditModel()) {
                case RUBBER:
                case GRAFFITI:
                case CLIP:
                case RECT_CLIP:
                    repealBitmapList.add(cacheBitmapList.remove(cacheBitmapList.size() - 1));
                    Log.i("撤销 缓存数量", cacheBitmapList.size() + "");
                    break;
                default:
                    break;
            }

            mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
            drawBaseBitmapStoker();
            mCanvas.save();

            mCanvasMatrix.reset();
            if (pLastEditData != null) {
                Log.d("撤销", "重新设置mCanvasMatrix");
                mCanvasMatrix.set(pLastEditData.getMatrix());
            }

            drawBg();
            mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
            drawBaseBitmapStoker();
            mCanvas.restore();
        }
    }

    /**
     * 恢复撤销
     *
     * @param pRevocationEditData 恢复的操作
     */
    public void revocationAction(EditData pRevocationEditData) {
        if (pRevocationEditData == null) {
            Log.d("恢复撤销", "恢复撤销的操作错误");
            return;
        } else {
            switch (pRevocationEditData.getEditModel()) {
                case RUBBER:
                case GRAFFITI:
                case CLIP:
                case RECT_CLIP:
                    cacheBitmapList.add(repealBitmapList.remove(repealBitmapList.size() - 1));
                    Log.i("恢复撤销 ", "恢复图片缓存");
                    break;
                default:
                    break;
            }

            mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
            drawBaseBitmapStoker();

            mCanvas.save();

            mCanvasMatrix.reset();
            Log.d("恢复撤销", "重新设置mCanvasMatrix");
            mCanvasMatrix.set(pRevocationEditData.getMatrix());

            drawBg();
            mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
            drawBaseBitmapStoker();
            mCanvas.restore();
        }
    }

    /**
     * 橡皮擦
     *
     * @param pPath  橡皮擦路径
     * @param pPaint 橡皮擦画笔
     */
    public void rubber(Path pPath, Paint pPaint) {
        if (cleanRubberModel) {
            Bitmap cacheBm = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
            Canvas lCanvas = new Canvas(cacheBm);
            lCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            //先画基础图片
            lCanvas.save();
            lCanvas.drawBitmap(mBaseBitmap, 0, 0, null);

            lCanvas.save();

            drawRubberRect();

            //画涂鸦
            //Path需要先根据当前画布的Matrix进行形变操作
            //因为Path是在父容器生成的，缩放值为1位移值为0
            Path lPath = new Path();
            Matrix lMatrix = new Matrix();
            mCanvasMatrix.invert(lMatrix);
            lPath.addPath(pPath, lMatrix);

            //画笔的size需要根据当前画布的Matrix的缩放值进行相应调整
            //因为画笔是在父容器生成的，缩放值为1
            Paint lPaint = new Paint();
            lPaint.set(pPaint);
            float[] v = new float[9];
            lMatrix.getValues(v);
            float size = lPaint.getStrokeWidth() * v[Matrix.MSCALE_X];
            lPaint.setStrokeWidth(size);

            lPaint.setXfermode(null);
            lPaint.setColor(Color.BLUE);
            lPaint.setAlpha(100);
            lCanvas.drawPath(lPath, lPaint);

            lCanvas.restore();
            lCanvas.restore();

            cacheBitmapList.add(cacheBm);
            Log.i("缓存数量", cacheBitmapList.size() + "");

            mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
            drawBaseBitmapStoker();
            mCanvas.save();

            drawBg();
            mCanvas.drawBitmap(cacheBm, mCanvasMatrix, null);
            drawBaseBitmapStoker();
            mCanvas.restore();
        } else {
            Bitmap cacheBm = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
            Canvas lCanvas = new Canvas(cacheBm);
            lCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

            //先画基础图片
            lCanvas.save();
            lCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), 0, 0, null);
            lCanvas.restore();

            //画涂鸦
            //Path需要先根据当前画布的Matrix进行形变操作
            //因为Path是在父容器生成的，缩放值为1位移值为0
            Path lPath = new Path();
            Matrix lMatrix = new Matrix();
            mCanvasMatrix.invert(lMatrix);
            lPath.addPath(pPath, lMatrix);

            //画笔的size需要根据当前画布的Matrix的缩放值进行相应调整
            //因为画笔是在父容器生成的，缩放值为1
            Paint lPaint = new Paint();
            lPaint.set(pPaint);
            float[] v = new float[9];
            lMatrix.getValues(v);
            float size = lPaint.getStrokeWidth() * v[Matrix.MSCALE_X];
            lPaint.setStrokeWidth(size);
            lCanvas.drawPath(lPath, lPaint);

            cacheBitmapList.add(cacheBm);
            Log.i("缓存数量", cacheBitmapList.size() + "");

            mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
            drawBaseBitmapStoker();
            mCanvas.save();

            drawBg();
            mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
            drawBaseBitmapStoker();
            mCanvas.restore();
        }
    }

    /**
     * 涂鸦
     *
     * @param pPath  涂鸦路径
     * @param pPaint 涂鸦画笔
     */
    public void graffiti(Path pPath, Paint pPaint) {
        Bitmap cacheBm = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
        Canvas lCanvas = new Canvas(cacheBm);
        lCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        //先画基础图片
        lCanvas.save();
        lCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), 0, 0, null);
        lCanvas.restore();

        //画涂鸦
        //Path需要先根据当前画布的Matrix进行形变操作
        //因为Path是在父容器生成的，缩放值为1位移值为0
        Path lPath = new Path();
        Matrix lMatrix = new Matrix();
        mCanvasMatrix.invert(lMatrix);
        lPath.addPath(pPath, lMatrix);

        //画笔的size需要根据当前画布的Matrix的缩放值进行相应调整
        //因为画笔是在父容器生成的，缩放值为1
        Paint lPaint = new Paint();
        lPaint.set(pPaint);
        float[] v = new float[9];
        lMatrix.getValues(v);
        float size = lPaint.getStrokeWidth() * v[Matrix.MSCALE_X];
        lPaint.setStrokeWidth(size);

        lCanvas.drawPath(lPath, lPaint);

        cacheBitmapList.add(cacheBm);
        Log.i("缓存数量", cacheBitmapList.size() + "");

        mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
        drawBaseBitmapStoker();
        mCanvas.save();

        drawBg();
        mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
        drawBaseBitmapStoker();
        mCanvas.restore();
    }

    /**
     * 裁剪区域
     *
     * @param pPath 裁剪路径
     */
    public void clip(Path pPath) {
        /*裁剪时，新建一个缓存的bitmap*/
        Bitmap cacheBm = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888);
        Canvas lCanvas = new Canvas(cacheBm);
        lCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        lCanvas.save();

        Path lPath = new Path();
        lPath.set(pPath);
        //path 设置逆矩阵
        Matrix lMatrix = new Matrix();
        mCanvasMatrix.invert(lMatrix);
        lPath.transform(lMatrix);

        lCanvas.clipPath(lPath);
        lCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), 0, 0, null);
        lCanvas.restore();

        cacheBitmapList.add(cacheBm);
        Log.i("缓存数量", cacheBitmapList.size() + "");

        mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
        mCanvas.save();

        drawBg();
        mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
        drawBaseBitmapStoker();
        mCanvas.restore();
    }


    @Override
    public void postMatrixTranslate(float x, float y) {
        super.postMatrixTranslate(x, y);
        drawBg();
        mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }

    @Override
    public void postMatrixScale(float scale, Point centerPoint) {
        super.postMatrixScale(scale, centerPoint);
        drawBg();
        mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }


    @Override
    public void postMatrixRotate(float angle, Point midPoint) {
        super.postMatrixRotate(angle, midPoint);
        drawBg();
        mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }

    @Override
    public void setMatrixValues(float[] pValues) {
        super.setMatrixValues(pValues);
        drawBg();
        mCanvas.drawBitmap(cacheBitmapList.get(cacheBitmapList.size() - 1), mCanvasMatrix, null);
        drawBaseBitmapStoker();
    }

    private void drawBg() {
//        //画边框
//        int start_x = 0;
//        int start_y = 0;
//        RectF lRect = new RectF(start_x, start_y, start_x + baseWidth, start_y + baseHeight);
//
//        mCanvasMatrix.mapRect(lRect);
//
//        Paint lPaint = new Paint();
//        lPaint.setStyle(Paint.Style.FILL);
//        lPaint.setColor(Color.BLACK);
//        mCanvas.drawRect(lRect, lPaint);
    }


    /**
     * 获取最后缓存的图片
     *
     * @return
     */
    public Bitmap getLaseCacheBitmap() {
        return cacheBitmapList.get(cacheBitmapList.size() - 1);
    }

    public static class Builder {

        private Bitmap mBitmap;
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

        public Builder setBitmap(Bitmap pBitmap) {
            this.mBitmap = pBitmap;
            return this;
        }

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

        public BitmapInfo build() {
            BitmapInfo lBitmapInfo = new BitmapInfo();

            lBitmapInfo.setType(Type.PIC);
            lBitmapInfo.setBaseBitmap(mBitmap);

            lBitmapInfo.setCanDrag(canDrag);
            lBitmapInfo.setSingleCtrlDrag(singleCtrlDrag);
            lBitmapInfo.setCanRotate(canRotate);
            lBitmapInfo.setCanScale(canScale);

            lBitmapInfo.setCanClip(canClip);
            lBitmapInfo.setCanRectClip(canRectClip);
            lBitmapInfo.setCanCircleClip(canCircleClip);

            lBitmapInfo.setCanGraffiti(canGraffiti);
            lBitmapInfo.setCanRubber(canRubber);

            lBitmapInfo.setCenterCrop(centerCrop);
            lBitmapInfo.setLimitRect(limitRect);
            lBitmapInfo.setShowStoker(showStoker);
            lBitmapInfo.setSelectShowStoker(selectShowStoker);
            lBitmapInfo.setCanDelete(canDelete);

            return lBitmapInfo;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "mBitmap=" + mBitmap +
                    ", canDrag=" + canDrag +
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
