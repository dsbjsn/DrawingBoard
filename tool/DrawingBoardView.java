package com.stickermaker.whatsapp.tool;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * 自定义画板view
 * Created on 2021/7/19 17
 *
 * @author xjl
 */
public class DrawingBoardView extends View {
    private static final String TAG = "DrawingBoardView";
    public static final int AUTO_ALIGNMENT_OFFSET = 40;

    public DrawingBoardView(Context context) {
        super(context);
        init();
    }

    public DrawingBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DrawingBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private EditActionListener mEditActionListener;

    /**
     * 组件宽高
     */
    private int width, height;

    /**
     * 画布圆角
     */
    private int canvasRadius = 0;
    /**
     * 背景bitmap
     */
    private Bitmap bgBitmap;
    /**
     * 缓存的bitmap
     */
    private Bitmap cacheBitmap;
    /**
     * 缓存的bitmap 的画布
     */
    private Canvas cacheCanvas;

    /**
     * 触控时的编辑模式
     */
    private EditModel editModel = EditModel.NONE;
    /**
     * 是否是多指触控
     */
    private boolean isMultiTouch = false;
    /**
     * 手指按下位置 1
     */
    private Point down_position = null;
    /**
     * 手指按下位置 2 多指触控时用到
     */
    private Point down_position_2 = null;
    /**
     * 多指触控时的中心点
     */
    private Point multiTouchCenter = null;
    /**
     * 实时位置
     */
    private Point realtime_position = null;
    /**
     * 实时位置 2 多指触控时用到
     */
    private Point realtime_position_2 = null;
    /**
     * 移动距离（相对于按下的位置）
     */
    private float[] move_position = new float[2];
    /**
     * 移动距离（相对于上个的位置）
     */
    private float[] off_position = new float[2];
    /**
     * 双指触控开始的距离
     */
    private float startDistance = 0;
    /**
     * 双指触控开始的角度
     */
    private float startAngle = 0;


    /******形变相关参数******/
    /******形变相关参数******/
    /******形变相关参数******/
    /**
     * 缩放倍数
     */
    private float currScale = 1;

    /**
     * 最小缩放倍数，如果小于这个数，自动回弹
     */
    private float minScale = 0;

    /**
     * 旋转角度
     */
    private float currAngle = 0;

    /**
     * 路径移动轨迹
     */
    private Path movePath;
    /**
     * 路径最小的点
     */
    private Point pathMinPoint;
    /**
     * 路径最大的点
     */
    private Point pathMaxPoint;
    /**
     * 最后一次擦除操作
     */
    private List<EditData> allRubberAction = new ArrayList<>();


    /**
     * 位移时是否自动对准中线
     */
    private boolean autoAlignment = false;

    /**
     * 画中线的画笔
     */
    private Paint middleLinePaint = null;

    /**
     * 圆形裁剪时的中心点
     */
    private Point circleClipCenter = null;
    /**
     * 圆形裁剪时的半径
     */
    private int circleClipRadius = 0;

    /**
     * 裁剪画笔
     */
    private Paint clipPaint = null;
    /**
     * 裁剪画笔大小
     */
    private int clipSize = 20;
    /**
     * 填充裁剪路径的图片
     */
    private Bitmap mClipDrawBitmap;


    /**
     * 涂鸦的画笔
     */
    private Paint graffitiPaint = null;
    /**
     * 涂鸦画笔大小
     */
    private int graffitiSize = 30;


    /**
     * 橡皮擦的画笔
     */
    private Paint rubberPaint = null;
    /**
     * 橡皮擦画笔大小
     */
    private int rubberSize = 100;


    /**
     * 涂鸦红点画笔
     */
    private Paint redPointPaint;


    /**
     * 画板内所有的图片对象（待抽象，还会有文字对象）
     */
    private List<BaseInfo> mAllChildInfo = new ArrayList<>();


    /**
     * 当前选择修改的图片对象
     */
    private BaseInfo mTargetBaseInfo = null;

    /**
     * 记录的操作集合
     */
    private List<EditData> mEditDataList = new ArrayList<>();

    /**
     * 撤销的操作集合
     */
    private List<EditData> mRepealEditDataList = new ArrayList<>();


    private OnClickChildListener mOnClickChildListener;

    public void setOnClickChildListener(OnClickChildListener pOnClickChildListener) {
        mOnClickChildListener = pOnClickChildListener;
    }

    private void init() {
        Log.i(TAG, "init");
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //裁剪画笔
        clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clipPaint.setStyle(Paint.Style.STROKE);
        clipPaint.setStrokeJoin(Paint.Join.ROUND);
        clipPaint.setStrokeCap(Paint.Cap.ROUND);
        clipPaint.setAntiAlias(true);
        clipPaint.setStrokeWidth(clipSize);
        if (mClipDrawBitmap != null) {
            Shader lShader = new BitmapShader(mClipDrawBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            clipPaint.setShader(lShader);
        }

        //涂鸦画笔
        graffitiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graffitiPaint.setStyle(Paint.Style.STROKE);
        graffitiPaint.setStrokeCap(Paint.Cap.ROUND);
        graffitiPaint.setStrokeJoin(Paint.Join.ROUND);
        graffitiPaint.setAntiAlias(true);
        graffitiPaint.setStrokeWidth(graffitiSize);
        graffitiPaint.setColor(Color.BLUE);

        //橡皮擦画笔
        rubberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rubberPaint.setStyle(Paint.Style.STROKE);
        rubberPaint.setStrokeCap(Paint.Cap.ROUND);
        rubberPaint.setStrokeJoin(Paint.Join.ROUND);
        rubberPaint.setAntiAlias(true);
        rubberPaint.setStrokeWidth(rubberSize);
        rubberPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        //红点画笔
        redPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redPointPaint.setColor(Color.RED);
        redPointPaint.setAlpha(100);
        redPointPaint.setAntiAlias(true);
        redPointPaint.setStyle(Paint.Style.FILL);
    }


    /**
     * 自动对齐相关
     */
    private float startAlignment_x = -1;
    private float startAlignment_y = -1;

    /**
     * 是否自动对齐了X轴
     */
    private boolean drawMiddle_x_line = false;
    /**
     * 是否自动对齐了Y轴
     */
    private boolean drawMiddle_y_line = false;

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");

        //裁剪出圆角矩形
        if (canvasRadius > 0) {
            RectF lRectF = new RectF(0, 0, width, height);
            Path lPath = new Path();
            lPath.addRoundRect(lRectF, canvasRadius, canvasRadius, Path.Direction.CW);
            canvas.clipPath(lPath);
        }

        //绘制画板背景
        drawBoardBg(canvas);

        //保存图层，不然橡皮擦会覆盖背景
        int layer_2 = canvas.saveLayer(new RectF(0, 0, width, height), null);

        if (cacheCanvas != null) {
            cacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (BaseInfo lBitmapInfo : mAllChildInfo) {
                if (lBitmapInfo.getTag() == mTargetBaseInfo.getTag()) {
                    drawTarget(cacheCanvas);
                } else {
                    lBitmapInfo.invalidate(cacheCanvas);
                }
            }
        }

        if (cacheBitmap != null) {
            canvas.drawBitmap(cacheBitmap, 0, 0, null);
        }

        canvas.restoreToCount(layer_2);
    }

    /**
     * 画选中的对象
     *
     * @param pCanvas
     */
    private void drawTarget(Canvas pCanvas) {
        drawMiddle_x_line = false;
        drawMiddle_y_line = false;

        if (repealAction) {
            if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
                pCanvas.save();
                ((BitmapInfo) mTargetBaseInfo).repealAction(repealEditData, lastEditData);
                mTargetBaseInfo.invalidate(pCanvas);
                pCanvas.restore();
                repealAction = false;
            }
        } else if (revocationAction) {
            if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
                pCanvas.save();
                ((BitmapInfo) mTargetBaseInfo).revocationAction(revocationEditData);
                mTargetBaseInfo.invalidate(pCanvas);
                pCanvas.restore();
                revocationAction = false;
            }
        } else {
            //保存状态
            pCanvas.save();

            if (isMultiTouch) {
                switch (editModel) {
                    case DRAG:
                        mTargetBaseInfo.postMatrixTranslate(off_position[0], off_position[1]);
                        if (autoAlignment) {
                            autoAlignmentCenter();
                        }
                        break;
                    case ROTATE:
                        mTargetBaseInfo.postMatrixRotate(currAngle, multiTouchCenter);
                        break;
                    case SCALE:
                        mTargetBaseInfo.postMatrixScale(currScale, multiTouchCenter);
                        break;
                    default:
                        break;
                }
            } else {
                switch (editModel) {
                    case DRAG:
                        mTargetBaseInfo.postMatrixTranslate(off_position[0], off_position[1]);
                        if (autoAlignment) {
                            autoAlignmentCenter();
                        }
                        break;
                    case GRAFFITI:
                    case RUBBER:
                    case CLIP:
                    case RECT_CLIP:
                    case CIRCLE_CLIP:
                    default:
                        break;
                }
            }

            //画基础图片
            mTargetBaseInfo.invalidate(pCanvas);

            //画中线
            if (editModel == EditModel.DRAG) {
                if (drawMiddle_y_line) {
                    drawMiddleLine(pCanvas, 1);
                }

                if (drawMiddle_x_line) {
                    drawMiddleLine(pCanvas, 2);
                }
            }

            //画裁剪路径
            if ((editModel == EditModel.CLIP || editModel == EditModel.RECT_CLIP) && movePath != null) {
                pCanvas.drawPath(movePath, clipPaint);
            }

            //画圆形裁剪路径
            if (editModel == EditModel.CIRCLE_CLIP && circleClipCenter != null) {
                pCanvas.drawCircle(circleClipCenter.x, circleClipCenter.y, circleClipRadius, clipPaint);
            }

            //画涂鸦
            if (editModel == EditModel.GRAFFITI && movePath != null) {
                pCanvas.drawPath(movePath, graffitiPaint);
                //画红点
                if (realtime_position != null) {
                    pCanvas.drawCircle(realtime_position.x, realtime_position.y, graffitiPaint.getStrokeWidth() / 2, redPointPaint);
                }
            }

            //橡皮擦
            if (editModel == EditModel.RUBBER && movePath != null) {
                pCanvas.drawPath(movePath, rubberPaint);

                //画红点
                if (realtime_position != null) {
                    pCanvas.drawCircle(realtime_position.x, realtime_position.y, rubberPaint.getStrokeWidth() / 2, redPointPaint);
                }
            }


            //返回上个状态
            pCanvas.restore();
        }

    }

    /**
     * 自动对齐
     */
    private void autoAlignmentCenter() {

        boolean[] lBooleans = mTargetBaseInfo.checkIsNearCenter(AUTO_ALIGNMENT_OFFSET);

        float[] values = new float[9];
        mTargetBaseInfo.getMatrix().getValues(values);

        float[] offCenter = mTargetBaseInfo.getCenterTransXFromMatrix();
        if (lBooleans[0]) {
            values[Matrix.MTRANS_X] = offCenter[0];

            drawMiddle_y_line = true;

            if (startAlignment_x == -1) {
                startAlignment_x = realtime_position.x;
            } else {
                float off_start_x = realtime_position.x - startAlignment_x;
                if (Math.abs(off_start_x) >= AUTO_ALIGNMENT_OFFSET) {
                    values[Matrix.MTRANS_X] += off_start_x;
                    startAlignment_x = -1;
                    drawMiddle_y_line = false;
                }
            }
        }
        if (lBooleans[1]) {
            values[Matrix.MTRANS_Y] = offCenter[1];

            drawMiddle_x_line = true;

            if (startAlignment_y == -1) {
                startAlignment_y = realtime_position.y;
            } else {
                float off_start_y = realtime_position.y - startAlignment_y;
                if (Math.abs(off_start_y) >= AUTO_ALIGNMENT_OFFSET) {
                    values[Matrix.MTRANS_Y] += off_start_y;
                    startAlignment_y = -1;
                    drawMiddle_x_line = false;
                }
            }
        }
        mTargetBaseInfo.setMatrixValues(values);
    }


    /**
     * 画中线
     *
     * @param canvas
     */
    private void drawMiddleLine(Canvas canvas, int model) {
        Point centerPoint = new Point(getWidth() / 2, getHeight() / 2);
        if (model == 1 || model == 3) {
            if (middleLinePaint == null) {
                middleLinePaint = new Paint();
                middleLinePaint.setAntiAlias(true);
                middleLinePaint.setStyle(Paint.Style.FILL);
                middleLinePaint.setColor(Color.BLACK);
                middleLinePaint.setStrokeWidth(2);
            }
            canvas.drawLine(centerPoint.x, 0, centerPoint.x, centerPoint.y * 2, middleLinePaint);
        }

        if (model == 2 || model == 3) {
            if (middleLinePaint == null) {
                middleLinePaint = new Paint();
                middleLinePaint.setAntiAlias(true);
                middleLinePaint.setStyle(Paint.Style.FILL);
                middleLinePaint.setColor(Color.BLACK);
                middleLinePaint.setStrokeWidth(2);
            }
            canvas.drawLine(0, centerPoint.y, centerPoint.x * 2, centerPoint.y, middleLinePaint);
        }
    }

    /**
     * 绘制画板背景
     */
    private void drawBoardBg(Canvas pCanvas) {
        if (pCanvas == null) {
            Log.e(TAG, "绘制画板背景失败");
            return;
        }

        if (bgBitmap == null) {
//            pCanvas.drawColor(Color.parseColor("#ffffff"));
        } else {
            Paint lPaint = new Paint();
            Shader lShader = new BitmapShader(bgBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            lPaint.setShader(lShader);
            pCanvas.drawRect(new Rect(0, 0, width, height), lPaint);
        }
    }

    //触控操作是否完成
    private boolean motionEventIsFinish = true;
    //是否执行了动作开始回调
    private boolean hadCallBackStartAction = false;

    private long start_touch_time = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            /*手指按下*/
            case MotionEvent.ACTION_DOWN:
                start_touch_time = System.currentTimeMillis();
                motionEventIsFinish = false;
                isMultiTouch = false;

                //记录位置
                realtime_position = new Point();
                realtime_position.set((int) event.getX(), (int) event.getY());

                down_position = new Point();
                down_position.set((int) event.getX(), (int) event.getY());

                mTargetBaseInfo = getCtrlBitmapInto(down_position);

                /*涂鸦/橡皮擦模式 记录滑动轨迹*/
                if (mTargetBaseInfo != null) {
                    if (mTargetBaseInfo.isCanClip()) {
                        editModel = EditModel.CLIP;
                    } else if (mTargetBaseInfo.isCanRectClip()) {
                        editModel = EditModel.RECT_CLIP;
                    } else if (mTargetBaseInfo.isCanCircleClip()) {
                        editModel = EditModel.CIRCLE_CLIP;
                    } else if (mTargetBaseInfo.isCanRubber()) {
                        editModel = EditModel.RUBBER;
                    } else if (mTargetBaseInfo.isCanGraffiti()) {
                        editModel = EditModel.GRAFFITI;
                    } else {
                        //判断是否是单指操控位移
                        if (mTargetBaseInfo.isCanDrag() && mTargetBaseInfo.isSingleCtrlDrag()) {
                            editModel = EditModel.DRAG;
                        } else {
                            editModel = EditModel.NONE;
                        }
                    }
                }

                Log.i(TAG, "单指触控:" + editModel.toString());

                movePath = new Path();
                pathMinPoint = new Point();
                pathMaxPoint = new Point();

                pathMinPoint.set((int) event.getX(), (int) event.getY());
                pathMaxPoint.set((int) event.getX(), (int) event.getY());
                movePath.moveTo(event.getX(), event.getY());

                return true;

            /*多指触控按下*/
            case MotionEvent.ACTION_POINTER_DOWN:
                motionEventIsFinish = false;
                isMultiTouch = true;

                editModel = EditModel.NONE;

                //记录位置
                down_position = new Point();
                down_position.set((int) event.getX(0), (int) event.getY(0));
                down_position_2 = new Point();
                down_position_2.set((int) event.getX(1), (int) event.getY(1));

                realtime_position = new Point();
                realtime_position.set((int) event.getX(0), (int) event.getY(0));
                realtime_position_2 = new Point();
                realtime_position_2.set((int) event.getX(1), (int) event.getY(1));

                mTargetBaseInfo = getCtrlBitmapInto(down_position);

                //获取角度
                startAngle = rotation2(event);
                //获取两点的中心点
                multiTouchCenter = midPoint(event);
                //获取两点之间的距离
                startDistance = distance(event);
                return true;

            /*滑动*/
            case MotionEvent.ACTION_MOVE:
                if (motionEventIsFinish || mTargetBaseInfo == null) {
                    return true;
                }

                if (isMultiTouch) {
                    if (event.getPointerCount() > 1) {
                        //与按下位置的偏移值
                        if (down_position != null) {
                            move_position[0] = event.getX(0) - down_position.x;
                            move_position[1] = event.getY(0) - down_position.y;
                        } else {
                            down_position = new Point();
                            down_position.set((int) event.getX(0), (int) event.getY(0));
                            down_position_2 = new Point();
                            down_position_2.set((int) event.getX(1), (int) event.getY(1));
                        }

                        float[] off_position_2 = new float[2];

                        float move_x_1, move_x_2, move_y_1, move_y_2;
                        //与上次位置的偏移值
                        if (realtime_position != null && realtime_position_2 != null) {
                            off_position[0] = event.getX(0) - realtime_position.x;
                            off_position[1] = event.getY(0) - realtime_position.y;

                            off_position_2[0] = event.getX(1) - realtime_position_2.x;
                            off_position_2[1] = event.getY(1) - realtime_position_2.y;

                        } else {
                            realtime_position = new Point();
                            realtime_position.set((int) event.getX(0), (int) event.getY(0));
                            realtime_position_2 = new Point();
                            realtime_position_2.set((int) event.getX(1), (int) event.getY(1));
                        }

                        //刷新实时位置
                        realtime_position = new Point();
                        realtime_position.set((int) event.getX(0), (int) event.getY(0));
                        realtime_position_2 = new Point();
                        realtime_position_2.set((int) event.getX(1), (int) event.getY(1));

                        //获取当前两指间距离
                        float currDistance = distance(event);
                        currScale = currDistance / startDistance;
                        startDistance = currDistance;

                        float angle = rotation2(event);
                        currAngle = angle - startAngle;
                        startAngle = angle;
                        multiTouchCenter = midPoint(event);

                        float off_finger_x = Math.abs(off_position_2[0] - off_position[0]);
                        float off_finger_y = Math.abs(off_position_2[1] - off_position[1]);

                        Log.i("双指触控", "双指位移差模=" + off_finger_x + " | " + off_finger_y);
                        Log.i("双指触控", mTargetBaseInfo.isCanRotate() + "双指角度差=" + Math.abs(currAngle));
                        Log.i("双指触控", mTargetBaseInfo.isCanScale() + "双指距离差=" + Math.abs(currScale - 1));

                        if (off_finger_x < 5 && off_finger_y < 5 && mTargetBaseInfo.isCanDrag() && !mTargetBaseInfo.isSingleCtrlDrag()) {
                            editModel = EditModel.DRAG;
                        } else {
                            if (Math.abs(currAngle) >= 0.5f && Math.abs(currScale - 1) < 0.01 && mTargetBaseInfo.isCanRotate()) {
                                editModel = EditModel.ROTATE;
                            } else if (Math.abs(currAngle) < 0.5f && Math.abs(currScale - 1) > 0.01 && mTargetBaseInfo.isCanScale()) {
                                editModel = EditModel.SCALE;
                            }
                        }

                        Log.i("双指触控", "editModel=" + editModel);
                    }
                } else {
                    if (realtime_position == null || down_position == null) {
                        realtime_position = new Point();
                        realtime_position.set((int) event.getX(), (int) event.getY());

                        down_position = new Point();
                        down_position.set((int) event.getX(), (int) event.getY());
                    }

                    move_position[0] = event.getX(0) - down_position.x;
                    move_position[1] = event.getY(0) - down_position.y;

                    off_position[0] = event.getX() - realtime_position.x;
                    off_position[1] = event.getY() - realtime_position.y;

                    switch (editModel) {
                        /*涂鸦/橡皮擦模式 记录滑动轨迹*/
                        case GRAFFITI:
                        case RUBBER:
                        case CLIP:
                            pathMinPoint.set(
                                    Math.min((int) event.getX(), pathMinPoint.x),
                                    Math.min((int) event.getY(), pathMinPoint.y)
                            );

                            pathMaxPoint.set(
                                    Math.max((int) event.getX(), pathMaxPoint.x),
                                    Math.max((int) event.getY(), pathMaxPoint.y)
                            );

                            movePath.lineTo(realtime_position.x, realtime_position.y);
                            invalidate();
                            break;
                        case RECT_CLIP:
                            Point lPoint_1 = new Point();
                            lPoint_1.set((int) event.getX(), down_position.y);

                            Point lPoint_2 = new Point();
                            lPoint_2.set((int) event.getX(), (int) event.getY());

                            Point lPoint_3 = new Point();
                            lPoint_3.set(down_position.x, (int) event.getY());

                            movePath.reset();
                            movePath.moveTo(down_position.x, down_position.y);
                            movePath.lineTo(lPoint_1.x, lPoint_1.y);
                            movePath.lineTo(lPoint_2.x, lPoint_2.y);
                            movePath.lineTo(lPoint_3.x, lPoint_3.y);
                            movePath.close();
                            break;
                        case CIRCLE_CLIP:
                            Point lPoint_4 = new Point();
                            lPoint_4.set((int) event.getX(), (int) event.getY());

                            circleClipCenter = midPoint(down_position, lPoint_4);
                            circleClipRadius = (int) (distance(down_position, lPoint_4) / 2);

                            movePath.reset();
                            movePath.moveTo(down_position.x, down_position.y);
                            movePath.addCircle(circleClipCenter.x, circleClipCenter.y, circleClipRadius, Path.Direction.CW);
                            break;
                        default:
                            break;
                    }
                }

                realtime_position = new Point();
                realtime_position.set((int) event.getX(), (int) event.getY());

                if (!hadCallBackStartAction && editModel != EditModel.NONE && mEditActionListener != null) {
                    mEditActionListener.onActionStart(editModel, mTargetBaseInfo, mAllChildInfo.indexOf(mTargetBaseInfo));
                    hadCallBackStartAction = true;
                }

                //是否触发删除回调事件
                if (editModel == EditModel.DRAG) {
                    if (mEditActionListener != null) {
                        mEditActionListener.onTriggerDeleteAction(realtime_position.y > height);
                    }
                }
                invalidate();
                return true;

            /*抬起手指*/
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                Log.i("点击时间", (System.currentTimeMillis() - start_touch_time) + "");
                if (System.currentTimeMillis() - start_touch_time < 150) {
                    if (mTargetBaseInfo.isTouchDelete()) {
                        Log.i("点击事件", "删除");
                        deleteLayer(mTargetBaseInfo);
                    } else {
                        if (mOnClickChildListener != null) {
                            mOnClickChildListener.onClick(mTargetBaseInfo);
                        }
                    }
                    resetTouchData();
                    return super.onTouchEvent(event);
                }


                if (motionEventIsFinish) {
                    return super.onTouchEvent(event);
                }

                motionEventIsFinish = true;

                /*记录当前的操作*/
                EditData lEditData = new EditData();
                lEditData.setTag(mTargetBaseInfo.getTag());
                lEditData.setEditModel(editModel);

                Matrix lMatrix = new Matrix();
                lMatrix.set(mTargetBaseInfo.getMatrix());
                lEditData.setMatrix(lMatrix);

                switch (editModel) {
                    case DRAG:
                        mEditDataList.add(lEditData);
                        break;
                    case SCALE:
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            mEditDataList.add(lEditData);
                        }
                        break;
                    case ROTATE:
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            mEditDataList.add(lEditData);
                        }
                        break;
                    case RUBBER:
                        if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
                            Path lRubberPath = new Path();
                            lRubberPath.addPath(movePath);

                            Paint lRubberPaint = new Paint();
                            lRubberPaint.set(rubberPaint);

                            GraffitiData lRubberData = new GraffitiData(lRubberPath, lRubberPaint);
                            lEditData.setGraffitiData(lRubberData);
                            mEditDataList.add(lEditData);

                            allRubberAction.add(lEditData);

                            ((BitmapInfo) mTargetBaseInfo).rubber(lRubberPath, rubberPaint);
                        }
                        break;
                    case GRAFFITI:
                        if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
                            Path lGraffitiPath = new Path();
                            lGraffitiPath.set(movePath);

                            Paint lGraffitiPaint = new Paint();
                            lGraffitiPaint.set(graffitiPaint);

                            GraffitiData lGraffitiData = new GraffitiData(lGraffitiPath, lGraffitiPaint);
                            lEditData.setGraffitiData(lGraffitiData);
                            mEditDataList.add(lEditData);

                            ((BitmapInfo) mTargetBaseInfo).graffiti(movePath, graffitiPaint);
                        }
                        break;
                    case CLIP:
                    case RECT_CLIP:
                    case CIRCLE_CLIP:
                        if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
                            if (editModel == EditModel.CLIP) {
                                movePath.close();
                            }

                            ((BitmapInfo) mTargetBaseInfo).clip(movePath);

                            Path lPath2 = new Path();
                            lPath2.set(movePath);

                            GraffitiData lGraffitiData2 = new GraffitiData(lPath2, null);
                            lEditData.setGraffitiData(lGraffitiData2);
                            break;
                        }
                    default:
                        break;
                }

                Log.i("记录操作", lEditData.toString());
                Log.i("操作栈数量", mEditDataList.size() + "");
                invalidate();

                /*检查缩放是否超过限定值*/
                if (minScale > 0) {
                    checkScaleIsOverMin();
                }

                //延迟100ms，手指抬起时可能会有些其他操作
                new Handler().postDelayed(() -> resetTouchData(), 100);
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 重置一些参数
     */
    private void resetTouchData() {
        //是否触发删除回调事件
        if (mAllChildInfo.indexOf(mTargetBaseInfo) > 0 && editModel == EditModel.DRAG && realtime_position.y > height) {
            mAllChildInfo.remove(mTargetBaseInfo);
            mTargetBaseInfo = mAllChildInfo.get(0);
        }

        //涂鸦路径
        if (movePath != null) {
            movePath.reset();
        }

        //按下位置
        down_position = null;
        down_position_2 = null;
        //实时位置
        realtime_position = null;
        realtime_position_2 = null;
        //移动位置
        move_position = new float[2];
        //与上一个位置的偏移位置
        off_position = new float[2];

        currAngle = 0;

        //重置记录的自动对齐位置
        startAlignment_x = -1;
        startAlignment_y = -1;

        invalidate();

        //新增了操作，清空撤销内容
        mRepealEditDataList.clear();

        if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
            ((BitmapInfo) mTargetBaseInfo).cleanRepealCache();
        }

        if (mEditActionListener != null) {
            mEditActionListener.onActionComplete(editModel, mEditDataList, mRepealEditDataList);
            hadCallBackStartAction = false;
        }

        editModel = EditModel.NONE;
    }


    /*********************/
    /*     公开方法      */
    /*********************/

    /**
     * 添加子画布到末尾层级
     *
     * @param pInfo
     */
    public void addLayer(BaseInfo pInfo) {
        addLayer(pInfo, mAllChildInfo.size());
    }

    /**
     * 添加子画布到指定层级
     *
     * @param pInfo
     */
    public void addLayer(BaseInfo pInfo, int position) {
        mTargetBaseInfo = pInfo;
        mTargetBaseInfo.setSelected(true);

        for (BaseInfo lInfo : mAllChildInfo) {
            lInfo.setSelected(false);
        }

        mAllChildInfo.add(position, pInfo);

        this.post(() -> {
            width = getWidth();
            height = getHeight();

            if (cacheBitmap == null) {
                cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }

            if (cacheCanvas == null) {
                cacheCanvas = new Canvas(cacheBitmap);
                cacheCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            }

            mTargetBaseInfo.firstDrawBitmap(width, height);

            invalidate();


            if (mEditDataList.isEmpty()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*记录初始状态*/
                        EditData lEditData = new EditData();
                        lEditData.setTag(mTargetBaseInfo.getTag());
                        lEditData.setEditModel(EditModel.NONE);

                        Matrix lMatrix = new Matrix();
                        lMatrix.set(mTargetBaseInfo.getMatrix());
                        lEditData.setMatrix(lMatrix);

                        mEditDataList.add(lEditData);
                    }
                }, 200);
            }
        });
    }


    public void deleteLayer(BaseInfo pBaseInfo) {
        mAllChildInfo.remove(pBaseInfo);
        if (mEditActionListener != null) {
            mEditActionListener.onDeleteLayer(pBaseInfo);
        }

        if (!mAllChildInfo.isEmpty()) {
            mTargetBaseInfo = mAllChildInfo.get(0);
        }
    }


    /**
     * 清空所有
     */
    public void clearAll() {
        mAllChildInfo.clear();
    }

    /**
     * 重新设置
     *
     * @param pInfo
     */
    public void resetLayer(BaseInfo pInfo) {
        clearAll();
        addLayer(pInfo);
    }

    /**
     * 重新设置原始图片
     *
     * @param pBitmap
     */
    public void refreshFirstBitmap(Bitmap pBitmap) {
        if (mAllChildInfo.isEmpty()) {
            Log.e(TAG, "未找到该图层");
        } else {
            if (mAllChildInfo.get(0) instanceof BitmapInfo) {
                refreshBitmapInfoByTag(pBitmap, mAllChildInfo.get(0).getTag());
            } else {
                Log.e(TAG, "该图层不是图片");
            }
        }
    }

    /**
     * 重新设置图片
     *
     * @param pBitmap
     * @param pTag    图层tag
     */
    public void refreshBitmapInfoByTag(Bitmap pBitmap, int pTag) {
        BaseInfo lInfo = getChildInfoByTag(pTag);
        if (lInfo == null) {
            Log.e(TAG, "未找到该图层");
        } else {
            if (lInfo.getType() == BaseInfo.Type.PIC) {
                ((BitmapInfo) lInfo).refreshBaseBitmap(pBitmap);
                invalidate();
            }
        }
    }

    /**
     * 重新设置文本信息
     *
     * @param pTextBean
     */
    public void refreshTargetText(TextBean pTextBean) {
        if (mTargetBaseInfo != null) {
            refreshTextBeanByTag(pTextBean, mTargetBaseInfo.getTag());
        }
    }

    /**
     * 重新设置图片通过tag
     *
     * @param pTextBean
     * @param pTag      标识
     */
    public void refreshTextBeanByTag(TextBean pTextBean, int pTag) {
        if (mAllChildInfo.isEmpty()) {
            return;
        }

        for (BaseInfo lInfo : mAllChildInfo) {
            if (lInfo.getTag() == pTag) {
                ((TextInfo) lInfo).refreshTextBean(pTextBean);
                new Handler().postDelayed(() -> invalidate(), 200);
            }
        }
    }

    /**
     * 设置圆角画布
     *
     * @param pCanvasRadius
     */
    public void setCanvasRadius(int pCanvasRadius) {
        canvasRadius = pCanvasRadius;
        invalidate();
    }

    /**
     * 设置背景图片
     *
     * @param pBgBitmap
     */
    public void setBgBitmap(Bitmap pBgBitmap) {
        bgBitmap = pBgBitmap;
        invalidate();
    }


    /**
     * 自动对齐是否启用
     *
     * @param pAutoAlignment
     */
    public void setAutoAlignment(boolean pAutoAlignment) {
        autoAlignment = pAutoAlignment;
    }


    /**
     * 设置填充裁剪路径的图片
     *
     * @param pClipDrawBitmap
     */
    public void setClipDrawBitmap(Bitmap pClipDrawBitmap) {
        mClipDrawBitmap = pClipDrawBitmap;

        if (clipPaint != null) {
            Shader lShader = new BitmapShader(mClipDrawBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            clipPaint.setShader(lShader);
        }
    }

    /**
     * 设置涂鸦画笔颜色
     *
     * @param color
     */
    public void setGraffitiColor(int color) {
        if (graffitiPaint != null) {
            graffitiPaint.setColor(color);
        }
    }

    /**
     * 设置涂鸦画笔大小
     *
     * @param size
     */
    public void setGraffitiSize(int size) {
        rubberSize = size;
        if (graffitiPaint != null) {
            graffitiPaint.setStrokeWidth(size);
        }
    }


    /**
     * 设置动作监听
     *
     * @param pEditActionListener
     */
    public void setEditActionListener(EditActionListener pEditActionListener) {
        mEditActionListener = pEditActionListener;
    }


    /**
     * 获取当前选择对象的裁剪后的图片
     *
     * @return
     */
    public Bitmap getCurryBitMap() {
        if (mTargetBaseInfo.getType() == BaseInfo.Type.PIC) {
            return ((BitmapInfo) mTargetBaseInfo).getLaseCacheBitmap();
        } else {
            return mTargetBaseInfo.mBaseBitmap;
        }
    }

    public interface GetBitmapListener {
        void result(Bitmap pBitmap);
    }

    /**
     * 获取缓存图片
     */
    public void getBitMap(GetBitmapListener pGetBitmapListener) {
        for (BaseInfo lInfo : mAllChildInfo) {
            lInfo.setSelected(false);
        }
        invalidate();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pGetBitmapListener != null) {
                    pGetBitmapListener.result(cacheBitmap);
                }
            }
        }, 200);
    }

    /**
     * 获取裁剪路径
     *
     * @return
     */
    public Path getClipPath() {
        return movePath;
    }


    /**
     * 设置最小缩放倍数
     *
     * @param pMinScale
     */
    public void setMinScale(float pMinScale) {
        minScale = pMinScale;
    }

    /**
     * 获取当前对象的矩阵
     *
     * @return
     */
    public Matrix getTargetMatrix() {
        if (mTargetBaseInfo != null) {
            return mTargetBaseInfo.getMatrix();
        } else {
            return null;
        }
    }

    /**
     * 获取所有擦除操作
     *
     * @return
     */
    public List<EditData> getAllRubberAction() {
        return allRubberAction;
    }

    /**
     * 获取所有子视图列表
     *
     * @return
     */
    public List<BaseInfo> getAllChildInfo() {
        return mAllChildInfo;
    }

    /**
     * 获取所有子视图列表
     *
     * @return
     */
    public BaseInfo getChildInfoByTag(int pTAG) {
        for (BaseInfo lInfo : mAllChildInfo) {
            if (lInfo.getTag() == pTAG) {
                return lInfo;
            }
        }
        return null;
    }

    //是否是撤销操作
    private boolean repealAction = false;
    //撤销的操作
    private EditData repealEditData = null;
    //撤销后的最后一把操作
    private EditData lastEditData = null;

    /*
     * 撤销操作
     */
    public void repealAction() {
        if (mEditDataList.size() == 1) {
            return;
        }

        repealEditData = mEditDataList.remove(mEditDataList.size() - 1);
        lastEditData = mEditDataList.get(mEditDataList.size() - 1);

        Log.i(TAG, "撤销操作：" + repealEditData.toString());
        Log.i(TAG, "撤销后最后操作：" + lastEditData.toString());
        Log.i(TAG, "操作栈数量：" + mEditDataList.size());

        if (repealEditData.getEditModel() == EditModel.RUBBER) {
            allRubberAction.remove(allRubberAction.size() - 1);
        }

        mRepealEditDataList.add(repealEditData);

        if (mEditActionListener != null) {
            mEditActionListener.onRepeatComplete(repealEditData, mEditDataList, mRepealEditDataList);
        }

        repealAction = true;
        invalidate();
    }

    //是否是恢复撤销操作
    private boolean revocationAction = false;
    //恢复撤销的操作
    private EditData revocationEditData = null;

    /*
     * 恢复撤销操作
     */
    public void revocationAction() {
        if (mRepealEditDataList.isEmpty()) {
            return;
        }

        revocationEditData = mRepealEditDataList.remove(mRepealEditDataList.size() - 1);

        if (revocationEditData.getEditModel() == EditModel.RUBBER) {
            allRubberAction.add(revocationEditData);
        }

        mEditDataList.add(revocationEditData);

        if (mEditActionListener != null) {
            mEditActionListener.onRevocationComplete(repealEditData, mEditDataList, mRepealEditDataList);
        }

        revocationAction = true;
        invalidate();
    }


    /****************/
    /*    私有方法  */
    /****************/

    /**
     * 检查是否超过缩放范围
     */
    private void checkScaleIsOverMin() {
        float[] old_values = new float[9];
        mTargetBaseInfo.getMatrix().getValues(old_values);

        if (mTargetBaseInfo.getScale() < minScale) {
            float[] target_values = new float[9];
            mTargetBaseInfo.getCacheMatrix().getValues(target_values);

            ObjectAnimator lObjectAnimator = new ObjectAnimator();
            lObjectAnimator.setFloatValues(0, 1);
            lObjectAnimator.setDuration(200);
            lObjectAnimator.addUpdateListener(pValueAnimator -> {
                float progress = (float) pValueAnimator.getAnimatedValue();
                float[] new_values = new float[9];

                new_values[0] = old_values[0] + (target_values[0] - old_values[0]) * progress;
                new_values[1] = old_values[1] + (target_values[1] - old_values[1]) * progress;
                new_values[2] = old_values[2] + (target_values[2] - old_values[2]) * progress;
                new_values[3] = old_values[3] + (target_values[3] - old_values[3]) * progress;
                new_values[4] = old_values[4] + (target_values[4] - old_values[4]) * progress;
                new_values[5] = old_values[5] + (target_values[5] - old_values[5]) * progress;
                new_values[6] = old_values[6] + (target_values[6] - old_values[6]) * progress;
                new_values[7] = old_values[7] + (target_values[7] - old_values[7]) * progress;
                new_values[8] = old_values[8] + (target_values[8] - old_values[8]) * progress;

                mTargetBaseInfo.setMatrixValues(new_values);

                invalidate();
            });
            lObjectAnimator.start();
        }
    }


    /**
     * 双指触控，获取两点的距离
     *
     * @param event
     * @return
     */
    private float distance(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            Point lPoint1 = new Point((int) event.getX(0), (int) event.getY(0));
            Point lPoint2 = new Point((int) event.getX(1), (int) event.getY(1));
            return distance(lPoint1, lPoint2);
        } else {
            return 0;
        }
    }

    /**
     * 获取两点的距离
     *
     * @param pPoint1
     * @param pPoint2
     * @return
     */
    private float distance(Point pPoint1, Point pPoint2) {
        float dx = pPoint1.x - pPoint2.x;
        float dy = pPoint1.y - pPoint2.y;
        return (float) Math.abs(Math.sqrt(dx * dx + dy * dy));
    }

    /**
     * 双指触控，获取两点的中心点
     *
     * @param event
     * @return
     */
    private Point midPoint(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            Point lPoint1 = new Point((int) event.getX(0), (int) event.getY(0));
            Point lPoint2 = new Point((int) event.getX(1), (int) event.getY(1));
            return midPoint(lPoint1, lPoint2);
        } else {
            return null;
        }
    }

    /**
     * 获取两点的中心点
     *
     * @param pPoint1 点1
     * @param pPoint2 点2
     * @return
     */
    private Point midPoint(Point pPoint1, Point pPoint2) {
        Point lPoint = new Point();
        lPoint.set((pPoint1.x + pPoint2.x) / 2, (pPoint1.y + pPoint2.y) / 2);
        return lPoint;
    }


    /**
     * @param point1
     * @param point2
     * @param point3
     * @return
     * @deprecated {@link #rotation2(MotionEvent)}
     * 求三点只间的夹角a 过时
     * cosA = (AB*AB + AC*AC - BC*BC ) / 2*AB*AC
     */
    private float angleOfPoints(Point point1, Point point2, Point point3) {
        if (point1 == null || point2 == null || point3 == null) {
            return 0;
        }

        Double distanceAB = Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
        Double distanceAC = Math.sqrt(Math.pow(point1.x - point3.x, 2) + Math.pow(point1.y - point3.y, 2));
        Double distanceBC = Math.sqrt(Math.pow(point2.x - point3.x, 2) + Math.pow(point2.y - point3.y, 2));
        Double cosA = (Math.pow(distanceAB, 2) + Math.pow(distanceAC, 2) - Math.pow(distanceBC, 2)) / (2 * distanceAB * distanceAC);

        boolean isPositive = true;
        if (point2.y < point1.y) {
            isPositive = point3.x > point2.x;
        } else {
            isPositive = point3.x < point2.x;
        }

        return (float) (Math.acos(cosA) * 180 / Math.PI * (isPositive ? 1 : -1));
    }


    /**
     * 获取旋转角度
     *
     * @param event
     * @return
     */
    private float rotation2(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 获取按下时选中的child
     *
     * @param pDown_position 按下坐标
     * @return
     */
    private BaseInfo getCtrlBitmapInto(Point pDown_position) {
        if (mAllChildInfo.isEmpty()) {
            return null;
        }

        BaseInfo selectInfo = null;
        for (int i = mAllChildInfo.size() - 1; i >= 0; i--) {
            BaseInfo lBaseInfo = mAllChildInfo.get(i);
            if (selectInfo == null && lBaseInfo.checkInRect(pDown_position)) {
                Log.i(TAG, "控制对象为：" + (i + 1));
                selectInfo = lBaseInfo;
                selectInfo.setSelected(true);
            } else {
                lBaseInfo.setSelected(false);
            }
        }

        if (selectInfo == null) {
            selectInfo = mTargetBaseInfo;
            if (selectInfo == null) {
                selectInfo = mAllChildInfo.get(0);
            }
        }
        return selectInfo;
    }
}
