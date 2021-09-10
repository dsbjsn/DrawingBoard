package com.stickermaker.whatsapp.tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.stickermaker.whatsapp.data.ConfigData;
import com.stickermaker.whatsapp.MainApplication;
import com.stickermaker.whatsapp.R;
import com.stickermaker.whatsapp.utils.BitMapUtil;
import com.stickermaker.whatsapp.utils.LogUtil;

/**
 * Created on 2021/8/4 18
 *
 * @author xjl
 */
public class BaseInfo {

    public enum Type {
        PIC,
        TXT
    }

    public BaseInfo() {
        boolean go = true;
        while (go) {
            mTag = (int) (Math.random() * 99999);
            if (mTag != 0) {
                go = false;
            }
        }
    }

    public void setType(Type pType) {
        mType = pType;
    }

    protected static final String TAG = "BaseInfo";

    private int mTag;
    private Type mType;

    public int getTag() {
        return mTag;
    }

    public Type getType() {
        return mType;
    }

    /**
     * 初始化时是否最大化
     */
    private boolean centerCrop = false;

    /**
     * 基础图片
     */
    protected Bitmap mBaseBitmap;
    /**
     * 原始宽度
     */
    protected int baseWidth;
    /**
     * 原始高度
     */
    protected int baseHeight;


    /**
     * 上画的Bitmap
     */
    protected Bitmap mCanvasBitmap;
    /**
     * 上画的Canvas
     */
    protected Canvas mCanvas;
    /**
     * 画布宽度
     */
    protected int canvasWidth;
    /**
     * 画布高度
     */
    protected int canvasHeight;

    /**
     * 上画的矩阵
     */
    protected Matrix mCanvasMatrix;

    /**
     * 缓存的矩阵
     */
    protected Matrix mCacheMatrix;

    /**
     * 最小缩放倍数
     */
    protected float minScale = 1;

    /**
     * 第一次画图片
     */
    protected boolean hadInitCache = false;


    /******触控相关参数******/
    /******触控相关参数******/
    /******触控相关参数******/
    /**
     * 允许拖动
     */
    private boolean canDrag = true;
    /**
     * 是否是单指操控位移 与 canClip/canRectClip/canRubber/canGraffiti 互斥
     */
    private boolean singleCtrlDrag = true;

    /**
     * 允许缩放
     */
    private boolean canScale = true;
    /**
     * 允许旋转
     */
    private boolean canRotate = true;
    /**
     * 允许任意裁剪  与 singleCtrlDrag/canRectClip 互斥
     */
    private boolean canClip = false;
    /**
     * 允许矩形裁剪 与 singleCtrlDrag/canClip/canCircleClip 互斥
     */
    private boolean canRectClip = false;
    /**
     * 允许圆形裁剪 与 singleCtrlDrag/canClip/canRectClip 互斥
     */
    private boolean canCircleClip = false;
    /**
     * 允许擦除
     */
    private boolean canRubber = false;
    /**
     * 允许涂鸦
     */
    private boolean canGraffiti = false;

    /**
     * 是否限制在画布内（缩放不能超过画布，位移不能移出画布）
     */
    private boolean limitRect = false;

    /**
     * 是否显示边框
     */
    private boolean showStoker = true;

    /**
     * 选中是否显示边框
     */
    private boolean selectShowStoker = false;

    /**
     * 是否被选中
     */
    private boolean selected = false;

    private boolean canDelete = false;

    public void firstDrawBitmap(int w, int h) {
        canvasWidth = w;
        canvasHeight = h;

        mCanvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mCanvasBitmap);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        Log.i(TAG, "图片size=" + baseWidth + " | " + baseHeight);
        Log.i(TAG, "画布size=" + canvasWidth + " | " + canvasHeight);

        if (mCanvasMatrix == null) {
            mCanvasMatrix = new Matrix();
            mCanvasMatrix.postTranslate(Math.abs(canvasWidth - baseWidth) / 2, Math.abs(canvasHeight - baseHeight) / 2);

            if (centerCrop) {
                float scale_x = (float) canvasWidth / baseWidth;
                float scale_y = (float) canvasHeight / baseHeight;
                float scale = Math.min(scale_x, scale_y) - 0.2f;

                minScale = scale;
                Log.i(TAG, "minScale=" + minScale);

                mCanvasMatrix.postScale(scale, scale, canvasWidth / 2, canvasHeight / 2);
            }

            mCacheMatrix = new Matrix();
            mCacheMatrix.set(mCanvasMatrix);
        }

        mCanvas.drawBitmap(mBaseBitmap, mCanvasMatrix, null);
        drawBaseBitmapStoker();
        hadInitCache = true;
    }

    /**
     * 刷新
     */
    public void invalidate(Canvas pCanvas) {
        if (mCanvasBitmap != null) {
            pCanvas.drawBitmap(mCanvasBitmap, 0, 0, null);
        }
    }


    /**
     * 通过矩阵位移
     *
     * @param x x轴方向距离
     * @param y y轴方向距离
     */
    public void postMatrixTranslate(float x, float y) {
        if (mCanvasMatrix == null) {
            mCanvasMatrix = new Matrix();
        }

        if (limitRect) {
            Matrix lMatrix = new Matrix();
            lMatrix.set(mCanvasMatrix);
            lMatrix.postTranslate(x, y);

            RectF lRectF;

            int offset_x = 0;
            int offset_y = 0;
            boolean going = true;
            while (going) {
                lRectF = new RectF(0, 0, baseWidth, baseHeight);
                lMatrix.mapRect(lRectF);

                offset_x = 0;
                offset_y = 0;

                if (lRectF.left < 0) {
                    offset_x = 1;
                } else if (lRectF.right > canvasWidth) {
                    offset_x = -1;
                }

                if (lRectF.top < 0) {
                    offset_y = 1;
                } else if (lRectF.bottom > canvasHeight) {
                    offset_y = -1;
                }

                if (offset_x == 0 && offset_y == 0) {
                    going = false;
                } else {
                    lMatrix.postTranslate(offset_x, offset_y);
                }
            }

            mCanvasMatrix.set(lMatrix);
        } else {
            mCanvasMatrix.postTranslate(x, y);
        }

        mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
    }

    /**
     * 通过矩阵缩放 post
     *
     * @param scale 缩放值
     */
    public void postMatrixScale(float scale, Point centerPoint) {
        if (mCanvasMatrix == null) {
            mCanvasMatrix = new Matrix();
        }

        if (limitRect) {
            Matrix lMatrix = new Matrix();
            lMatrix.set(mCanvasMatrix);
            lMatrix.postScale(scale, scale, centerPoint.x, centerPoint.y);

            RectF lRectF;

            float offset_scale = 1;
            boolean going = true;
            while (going) {
                lRectF = new RectF(0, 0, baseWidth, baseHeight);
                lMatrix.mapRect(lRectF);

                offset_scale = 1;

                if (lRectF.left < 0 || lRectF.right > canvasWidth || lRectF.top < 0 || lRectF.bottom > canvasHeight) {
                    offset_scale = 0.99f;
                }

                if (offset_scale == 1) {
                    going = false;
                } else {
                    lMatrix.postScale(offset_scale, offset_scale, centerPoint.x, centerPoint.y);
                }
            }
            mCanvasMatrix.set(lMatrix);
        } else {
            mCanvasMatrix.postScale(scale, scale, centerPoint.x, centerPoint.y);
        }

        mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
    }

    /**
     * 通过矩阵缩放
     *
     * @param angle 旋转角度
     */
    public void postMatrixRotate(float angle, Point midPoint) {
        if (angle == 0 || midPoint == null) {
            return;
        }

        if (mCanvasMatrix == null) {
            mCanvasMatrix = new Matrix();
        }

        mCanvasMatrix.postRotate(angle, canvasWidth / 2, canvasHeight / 2);
        mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
    }

    /**
     * 设置矩阵的各个系数
     *
     * @param pValues 缩放值
     */
    public void setMatrixValues(float[] pValues) {
        if (mCanvasMatrix == null) {
            mCanvasMatrix = new Matrix();
        }

        mCanvasMatrix.setValues(pValues);
        mCanvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
    }


    /**
     * 获取最后上画的图片
     *
     * @return
     */
    public Bitmap getCanvasBitmap() {
        return mCanvasBitmap;
    }


    /**
     * 获取当前画布的中心点位置
     *
     * @return
     */
    public Point getCanvasCenterPoint() {
        return new Point(Math.abs(canvasWidth - baseWidth) / 2, Math.abs(canvasHeight - baseHeight) / 2);
    }


    /**
     * 获取图片的宽度
     *
     * @return
     */
    public int getBaseWidth() {
        return baseWidth;
    }

    /**
     * 获取图片的高度
     *
     * @return
     */
    public int getBaseHeight() {
        return baseHeight;
    }

    /**
     * 获取画布的宽度
     *
     * @return
     */
    public int getCanvasWidth() {
        return canvasWidth;
    }

    /**
     * 获取画布的高度
     *
     * @return
     */
    public int getCanvasHeight() {
        return canvasHeight;
    }

    /**
     * 获取中心点位置在容器中的坐标
     *
     * @return
     */
    public Point getCenterPoint() {
        if (canvasWidth == 0 || canvasHeight == 0) {
            return null;
        }

        float[] center = new float[]{baseWidth / 2, baseWidth / 2};
        mCanvasMatrix.mapPoints(center);
        return new Point((int) center[0], (int) center[1]);
    }

    /**
     * 获取缩放后到中心的位移值
     *
     * @return
     */
    public float[] getCenterTransXFromMatrix() {
        RectF lRect = new RectF(0, 0, baseWidth, baseHeight);
        mCanvasMatrix.mapRect(lRect);

        float[] point = new float[]{0, 0};
        mCanvasMatrix.mapPoints(point);

        float[] offCenter = new float[2];
        offCenter[0] = (canvasWidth - lRect.width()) / 2 + (point[0] - lRect.left);
        offCenter[1] = (canvasHeight - lRect.height()) / 2 + (point[1] - lRect.top);
        return offCenter;
    }

    /**
     * 获取缩放后到中心的位移值
     *
     * @return
     */
    public boolean[] checkIsNearCenter(int offset) {
        int start_x = 0;
        int start_y = 0;
        RectF lRect = new RectF(start_x, start_y, start_x + baseWidth, start_y + baseHeight);
        mCanvasMatrix.mapRect(lRect);

        boolean center_x = Math.abs(canvasWidth / 2 - lRect.centerX()) < offset;
        boolean center_y = Math.abs(canvasHeight / 2 - lRect.centerY()) < offset;
        return new boolean[]{center_x, center_y};
    }

    /**
     * 获取矩阵
     *
     * @return
     */
    public Matrix getMatrix() {
        return mCanvasMatrix;
    }

    /**
     * 设置矩阵
     *
     * @param pMatrix
     */
    public void setMatrix(Matrix pMatrix) {
        if (mCanvasMatrix == null) {
            mCanvasMatrix = new Matrix();
        } else {
            mCanvasMatrix.reset();
        }

        mCanvasMatrix.set(pMatrix);
        Log.i(TAG, "初始化mCanvasMatrix3:" + mCanvasMatrix.toShortString());
    }


    /**
     * 检查点是否在图片的范围中
     *
     * @return
     */
    public boolean checkInRect(Point pPoint) {
        RectF lRect = new RectF(0 - 24, 0 - 24, baseWidth + 24, baseHeight + 24);
        mCanvasMatrix.mapRect(lRect);
        boolean isContains = lRect.contains(pPoint.x, pPoint.y);
        Log.i(TAG, "是否点中:" + isContains + "");

        if (lDeleteRect != null && canDelete) {
            touchDelete = lDeleteRect.contains(pPoint.x, pPoint.y);
            Log.i(TAG, "点中删除按钮=" + touchDelete);
        }
        return isContains;
    }


    /**
     * 删除按钮范围
     */
    private RectF lDeleteRect = null;
    private boolean touchDelete = false;

    public boolean isTouchDelete() {
        return touchDelete;
    }

    /**
     * 画与原图一致大小的边框
     */
    protected void drawBaseBitmapStoker() {
        if (!showStoker || !selected) {
            return;
        }

        //画边框
        int start_x = 0;
        int start_y = 0;
        RectF lRect = new RectF(start_x, start_y, start_x + baseWidth, start_y + baseHeight);

        mCanvasMatrix.mapRect(lRect);

        Paint lPaint = new Paint();
        lPaint.setStyle(Paint.Style.STROKE);
        lPaint.setStrokeWidth(1);
        lPaint.setColor(Color.BLACK);
        mCanvas.drawRect(lRect, lPaint);

        lPaint.setStyle(Paint.Style.FILL);

        Rect lIconRect = new Rect(0, 0, 32, 32);
        if (canDelete) {
            lDeleteRect = new RectF(lRect.left - 24, lRect.top - 24, lRect.left + 24, lRect.top + 24);
            RectF lRectF = new RectF(lRect.left - 16, lRect.top - 16, lRect.left + 16, lRect.top + 16);
            mCanvas.drawCircle(lRect.left, lRect.top, 24, lPaint);
            mCanvas.drawBitmap(BitmapFactory.decodeResource(MainApplication.getContext().getResources(), R.mipmap.cha_2), lIconRect, lRectF, null);
        }

//        lIconRectF = new RectF(lRect.right - 16, lRect.top - 16, lRect.right + 16, lRect.top + 16);
//        mCanvas.drawCircle(lRect.right, lRect.top, 24, lPaint);
//        mCanvas.drawBitmap(BitmapFactory.decodeResource(MainApplication.getContext().getResources(), R.mipmap.edit), lIconRect, lIconRectF, null);

//        lIconRectF = new RectF(lRect.left - 16, lRect.bottom - 16, lRect.left + 16, lRect.bottom + 16);
//        mCanvas.drawCircle(lRect.left, lRect.bottom, 24, lPaint);
//        mCanvas.drawBitmap(BitmapFactory.decodeResource(MainApplication.getContext().getResources(), R.mipmap.mirror), lIconRect, lIconRectF, null);

        RectF lIconRectF = new RectF(lRect.right - 16, lRect.bottom - 16, lRect.right + 16, lRect.bottom + 16);
        mCanvas.drawCircle(lRect.right, lRect.bottom, 24, lPaint);

        mCanvas.drawBitmap(BitmapFactory.decodeResource(MainApplication.getContext().getResources(), R.mipmap.rotate), lIconRect, lIconRectF, null);
    }

    /**
     * 获取矩阵缩放值
     *
     * @return
     */
    public float getMatrixScale() {
        if (mCanvasMatrix == null) {
            return 0;
        } else {
            float[] v = new float[9];
            mCanvasMatrix.getValues(v);
            return v[Matrix.MSCALE_X];
        }
    }

    /**
     * 获取与原始状态比较后的缩放值
     *
     * @return
     */
    public float getScale() {
        float m_scale = getMatrixScale();
        return m_scale / minScale;
    }

    /**
     * 获取原始缩放值
     *
     * @return
     */
    public float getMinScale() {
        return minScale;
    }

    /**
     * 获取矩阵
     *
     * @return
     */
    public Matrix getCacheMatrix() {
        return mCacheMatrix;
    }


    /**
     * 设置图片
     *
     * @param pBaseBitmap
     */
    public void setBaseBitmap(Bitmap pBaseBitmap) {
        if (pBaseBitmap == null) {
            return;
        }

        mBaseBitmap = BitMapUtil.getScaleBitmap(pBaseBitmap, ConfigData.EDIT_BITMAP_WIDTH, ConfigData.EDIT_BITMAP_HEIGHT);
    }

    /**
     * 拖动是否启用
     *
     * @param pCanDrag
     */
    public void setCanDrag(boolean pCanDrag) {
        canDrag = pCanDrag;
    }

    /**
     * 单指拖动是否启用
     *
     * @param pSingleCtrlDrag
     */
    public void setSingleCtrlDrag(boolean pSingleCtrlDrag) {
        singleCtrlDrag = pSingleCtrlDrag;
        if (singleCtrlDrag) {
            canClip = false;
            canRectClip = false;
            canCircleClip = false;
            canRubber = false;
            canGraffiti = false;
        }
    }

    /**
     * 缩放是否启用
     *
     * @param pCanScale
     */
    public void setCanScale(boolean pCanScale) {
        canScale = pCanScale;
    }

    /**
     * 旋转是否启用
     *
     * @param pCanRotate
     */
    public void setCanRotate(boolean pCanRotate) {
        canRotate = pCanRotate;
    }

    /**
     * 裁剪是否启用
     *
     * @param pCanClip
     */
    public void setCanClip(boolean pCanClip) {
        canClip = pCanClip;
        if (canClip) {
            singleCtrlDrag = false;
            canRectClip = false;
            canCircleClip = false;
        }
    }

    /**
     * 方形裁剪是否启用
     *
     * @param pCanRectClip
     */
    public void setCanRectClip(boolean pCanRectClip) {
        canRectClip = pCanRectClip;
        if (canRectClip) {
            singleCtrlDrag = false;
            canClip = false;
            canCircleClip = false;
        }
    }

    /**
     * 圆形裁剪是否启用
     *
     * @param pCanCircleClip
     */
    public void setCanCircleClip(boolean pCanCircleClip) {
        canCircleClip = pCanCircleClip;
        if (canCircleClip) {
            singleCtrlDrag = false;
            canClip = false;
            canRectClip = false;
        }
    }

    /**
     * 橡皮擦是否启用
     *
     * @param pCanRubber
     */
    public void setCanRubber(boolean pCanRubber) {
        canRubber = pCanRubber;
        if (canRubber) {
            singleCtrlDrag = false;
            canGraffiti = false;
        }
    }

    /**
     * 涂鸦是否启用
     *
     * @param pCanGraffiti
     */
    public void setCanGraffiti(boolean pCanGraffiti) {
        canGraffiti = pCanGraffiti;
        if (canGraffiti) {
            singleCtrlDrag = false;
            canRubber = false;
        }
    }

    /**
     * 设置是否自动缩放大小
     *
     * @param pCenterCrop
     */
    public void setCenterCrop(boolean pCenterCrop) {
        centerCrop = pCenterCrop;
    }

    /**
     * 设置是否限制在画布内
     *
     * @param pLimitRect
     */
    public void setLimitRect(boolean pLimitRect) {
        limitRect = pLimitRect;
    }

    /**
     * 设置是否显示边框
     *
     * @param pShowStoker
     */
    public void setShowStoker(boolean pShowStoker) {
        showStoker = pShowStoker;
        if (mCanvas != null) {
            postMatrixTranslate(0, 0);
        }
    }

    /**
     * 设置选中是否显示边框
     *
     * @param pSelectShowStoker
     */
    public void setSelectShowStoker(boolean pSelectShowStoker) {
        selectShowStoker = pSelectShowStoker;
    }

    /**
     * 设置是否被选中
     *
     * @param pSelected
     */
    public void setSelected(boolean pSelected) {
        selected = pSelected;
        if (mCanvas != null) {
            postMatrixTranslate(0, 0);
        }
    }


    public boolean isCenterCrop() {
        return centerCrop;
    }

    public boolean isCanDrag() {
        return canDrag;
    }

    public boolean isSingleCtrlDrag() {
        return singleCtrlDrag;
    }

    public boolean isCanScale() {
        return canScale;
    }

    public boolean isCanRotate() {
        return canRotate;
    }

    public boolean isCanClip() {
        return canClip;
    }

    public boolean isCanRectClip() {
        return canRectClip;
    }

    public boolean isCanCircleClip() {
        return canCircleClip;
    }

    public boolean isCanRubber() {
        return canRubber;
    }

    public boolean isCanGraffiti() {
        return canGraffiti;
    }

    public void setCanDelete(boolean pCanDelete) {
        canDelete = pCanDelete;
    }
}
