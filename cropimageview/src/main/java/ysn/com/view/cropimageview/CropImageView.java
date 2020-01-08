package ysn.com.view.cropimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import ysn.com.view.cropimageview.animation.JackAnimator;
import ysn.com.view.cropimageview.listener.BaseCropListener;
import ysn.com.view.cropimageview.listener.OnCropListener;
import ysn.com.view.cropimageview.listener.OnCropMultiListener;
import ysn.com.view.cropimageview.listener.OnCropSaveListener;
import ysn.com.view.cropimageview.listener.OnLoadListener;
import ysn.com.view.cropimageview.mode.CropMode;
import ysn.com.view.cropimageview.mode.GridLineMode;
import ysn.com.view.cropimageview.mode.RotateAngle;
import ysn.com.view.cropimageview.mode.SavedState;
import ysn.com.view.cropimageview.utils.ImageUtils;
import ysn.com.view.cropimageview.utils.Utils;

/**
 * @Author yangsanning
 * @ClassName CropImageView
 * @Description 一句话概括作用
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class CropImageView extends ImageView {

    private Matrix matrix = new Matrix();
    private float scale = 1.0f;
    private int viewHeight;
    private int viewWidth;
    private float imageWidth;
    private float imageHeight;
    private boolean isInit = false;
    private PointF centerPointF = new PointF();
    private float angle;
    private RectF imageRect;
    private Paint bitmapPaint;

    /**
     * 网格
     * gridMinSize: 网格最小范围
     * gridStroke: 网格边框宽度
     * gridColor: 网格颜色
     * isCropEnabled: 是否启用剪切
     * cropMode    : 裁剪模式
     */
    private Paint gridPaint;
    private RectF gridRect;
    private float gridMinSize;
    private float gridStroke;
    private int gridColor;
    private boolean isCropEnabled = true;
    private CropMode cropMode = CropMode.SQUARE;

    /**
     * 网格线
     * gridColor     : 网格线颜色
     * girdLineStroke: 网格线大小
     * girdRowPart   : 网格线横向分割为几部分
     * girdColumnPart: 网格线竖向分割为几部分
     * isShowGridLine：是否显示网格线
     * gridLineMode  ：网格线的显示模式
     */
    private float girdLineStroke;
    private int girdRowPart;
    private int girdColumnPart;
    private boolean isShowGridLine;
    private GridLineMode gridLineMode;

    /**
     * 拖拽点(网格角的四个角)
     * dragPointSize   : 拖拽点大小
     * dragPointPadding: 拖拽点padding(可通过dragPointPadding调整拖拽范围)
     * isShowDragPoint : 是否显示拖拽点
     */
    private int dragPointSize;
    private int dragPointPadding;
    private boolean isShowDragPoint;

    /**
     * maskPaint: 遮罩画笔
     * maskColor: 遮罩颜色
     */
    private Paint maskPaint;
    private int maskColor;

    private int bgColor;

    /**
     * 是否锁定截取区域
     */
    private boolean isLockCropArea = Boolean.FALSE;

    /**
     * 自定义的裁剪比例
     */
    private PointF customCropRatio = new PointF((1.0f), (1.0f));


    /**
     * 图片信息
     * sourceUri      : 源uri
     * saveUri        : 保存的uri
     * outputMaxWidth : 裁剪最大宽度(如果已设置固定的输出宽度将以固定值为准)
     * outputMaxHeight: 裁剪最大高度(如果已设置固定的输出高度将以固定值为准)
     * outputWidth    : 固定输出宽度
     * outputHeight   : 固定输出高度
     * compressQuality: 压缩质量
     * compressFormat : 压缩格式
     */
    private Uri sourceUri = null;
    private Uri saveUri = null;
    private int exifRotation = 0;
    private int outputMaxWidth;
    private int outputMaxHeight;
    private int outputWidth = 0;
    private int outputHeight = 0;
    private int compressQuality = 100;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;

    private AtomicBoolean isLoading = new AtomicBoolean(Boolean.FALSE);
    private AtomicBoolean isCropping = new AtomicBoolean(Boolean.FALSE);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * onTouchEvent
     * touchAnchor : 触摸点位置
     */
    private float lastX, lastY;
    private TouchAnchor touchAnchor = TouchAnchor.OUT_OF_BOUNDS;

    private boolean isRotating = Boolean.FALSE;
    private boolean isAnimating = Boolean.FALSE;
    private boolean isAnimEnabled = Boolean.TRUE;
    private int animDuration;
    private JackAnimator jackAnimator = null;
    private Interpolator interpolator = new DecelerateInterpolator();

    private OnCropMultiListener onCropMultiListener;
    private OnLoadListener onLoadListener;
    private OnCropListener onCropListener;
    private OnCropSaveListener onCropSaveListener;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
        initAttrs(context, attrs, defStyle);
    }

    private void initPaint() {
        gridPaint = new Paint();
        maskPaint = new Paint();
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, defStyle, 0);

        float density = getDensity();

        Drawable drawable = typedArray.getDrawable(R.styleable.CropImageView_civ_img_src);
        if (drawable != null) {
            setImageDrawable(drawable);
        }

        gridMinSize = typedArray.getDimensionPixelSize(R.styleable.CropImageView_civ_grid_min_size, (int) (50 * density));
        gridStroke = typedArray.getDimensionPixelSize(R.styleable.CropImageView_civ_grid_stroke, (int) (density));
        gridColor = typedArray.getColor(R.styleable.CropImageView_civ_grid_color, (0xBBFFFFFF));
        isCropEnabled = typedArray.getBoolean(R.styleable.CropImageView_civ_crop_enabled, Boolean.TRUE);
        cropMode = CropMode.getValue(typedArray.getInt(R.styleable.CropImageView_civ_crop_mode, CropMode.SQUARE.mode));

        bgColor = typedArray.getColor(R.styleable.CropImageView_civ_bg_color, (0x00000000));
        maskColor = typedArray.getColor(R.styleable.CropImageView_civ_make_color, (0xBB000000));

        girdLineStroke = typedArray.getDimensionPixelSize(R.styleable.CropImageView_civ_grid_line_stroke, (int) density);
        girdRowPart = typedArray.getInt(R.styleable.CropImageView_civ_gird_row_part, 3);
        girdColumnPart = typedArray.getInt(R.styleable.CropImageView_civ_gird_column_part, 3);
        gridLineMode = GridLineMode.getValue(
            typedArray.getInt(R.styleable.CropImageView_civ_grid_line_mode, GridLineMode.SHOW_ALWAYS.mode));
        setGridLineMode(gridLineMode);

        dragPointSize = typedArray.getDimensionPixelSize(R.styleable.CropImageView_civ_drag_point_size, (int) (12 * density));
        dragPointPadding = typedArray.getDimensionPixelSize(R.styleable.CropImageView_civ_drag_point_padding, 0);
        isShowDragPoint = typedArray.getBoolean(R.styleable.CropImageView_civ_drag_point_show, Boolean.TRUE);

        isAnimEnabled = typedArray.getBoolean(R.styleable.CropImageView_civ_anim_enabled, Boolean.TRUE);
        animDuration = typedArray.getInt(R.styleable.CropImageView_civ_anim_duration, 100);

        typedArray.recycle();
    }

    private float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(viewWidth, viewHeight);

        this.viewWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        this.viewHeight = viewHeight - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getDrawable() != null) {
            setupLayout();
        }
    }

    private void setupLayout() {
        if (viewWidth == 0 || viewHeight == 0) {
            return;
        }

        centerPointF = new PointF((getPaddingLeft() + viewWidth * 0.5f), (getPaddingTop() + viewHeight * 0.5f));
        scale = measureScale(angle);

        resetMatrix();
        imageRect = getImageRect(new RectF(0f, 0f, imageWidth, imageHeight), matrix);
        gridRect = getGridRect();

        isInit = Boolean.TRUE;
        invalidate();
    }

    private float measureScale(float angle) {
        imageWidth = getDrawable().getIntrinsicWidth();
        imageHeight = getDrawable().getIntrinsicHeight();
        if (imageWidth <= 0) {
            imageWidth = viewWidth;
        }
        if (imageHeight <= 0) {
            imageHeight = viewHeight;
        }
        float viewRatio = (float) viewWidth / (float) viewHeight;
        float imgRatio = getRotatedWidth(angle) / getRotatedHeight(angle);
        float scale = 1.0f;
        if (imgRatio >= viewRatio) {
            scale = viewWidth / getRotatedWidth(angle);
        } else if (imgRatio < viewRatio) {
            scale = viewHeight / getRotatedHeight(angle);
        }
        return scale;
    }

    private float getRotatedWidth(float angle) {
        return getRotatedWidth(angle, imageWidth, imageHeight);
    }

    private float getRotatedWidth(float angle, float width, float height) {
        return angle % 180 == 0 ? width : height;
    }

    private float getRotatedHeight(float angle) {
        return getRotatedHeight(angle, imageWidth, imageHeight);
    }

    private float getRotatedHeight(float angle, float width, float height) {
        return angle % 180 == 0 ? height : width;
    }

    private RectF getImageRect(RectF rect, Matrix matrix) {
        RectF applied = new RectF();
        matrix.mapRect(applied, rect);
        return applied;
    }

    private RectF getGridRect() {
        float gridWidth = getRatioX(imageRect.width());
        float gridHeight = getRatioY(imageRect.height());
        float imgRatio = imageRect.width() / imageRect.height();
        float gridRatio = gridWidth / gridHeight;

        float left = imageRect.left;
        float top = imageRect.top;
        float right = imageRect.right;
        float bottom = imageRect.bottom;
        if (gridRatio >= imgRatio) {
            float hy = (imageRect.top + imageRect.bottom) * 0.5f;
            float hh = (imageRect.width() / gridRatio) * 0.5f;
            top = hy - hh;
            bottom = hy + hh;
        } else if (gridRatio < imgRatio) {
            float hx = (imageRect.left + imageRect.right) * 0.5f;
            float hw = imageRect.height() * gridRatio * 0.5f;
            left = hx - hw;
            right = hx + hw;
        }
        float width = right - left;
        float height = bottom - top;
        float cx = left + width / 2;
        float cy = top + height / 2;
        return new RectF((cx - width / 2), (cy - height / 2), (cx + width / 2), (cy + height / 2));
    }

    private float getRatioX(float width) {
        switch (cropMode) {
            case FIT_IMAGE:
                return imageRect.width();
            case FREE:
                return width;
            case RATIO_4_3:
                return 4;
            case RATIO_3_4:
                return 3;
            case RATIO_16_9:
                return 16;
            case RATIO_9_16:
                return 9;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return customCropRatio.x;
            default:
                return width;
        }
    }

    private float getRatioY(float height) {
        switch (cropMode) {
            case FIT_IMAGE:
                return imageRect.height();
            case FREE:
                return height;
            case RATIO_4_3:
                return 3;
            case RATIO_3_4:
                return 4;
            case RATIO_16_9:
                return 9;
            case RATIO_9_16:
                return 16;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return customCropRatio.y;
            default:
                return height;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(bgColor);

        if (isInit) {
            resetMatrix();
            Bitmap bitmap = getBitmap();
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, matrix, bitmapPaint);
                // 绘制裁剪区域
                drawCropFrame(canvas);
            }
        }
    }

    private Bitmap getBitmap() {
        Bitmap bitmap = null;
        Drawable drawable = getDrawable();
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        return bitmap;
    }

    private void resetMatrix() {
        matrix.reset();
        matrix.setTranslate((centerPointF.x - imageWidth * 0.5f), (centerPointF.y - imageHeight * 0.5f));
        matrix.postScale(scale, scale, centerPointF.x, centerPointF.y);
        matrix.postRotate(angle, centerPointF.x, centerPointF.y);
    }

    /**
     * 绘制裁剪区域
     */
    private void drawCropFrame(Canvas canvas) {
        if (!isCropEnabled | isRotating) {
            return;
        }

        // 绘制遮罩区域
        drawMask(canvas);
        // 绘制网格边框
        drawCropArea(canvas);

        if (isShowGridLine) {
            // 绘制网格内部分割线
            drawGridLines(canvas);
        }

        if (isShowDragPoint) {
            // 绘制手势区域(网格角的四个点)
            drawDragPoint(canvas);
        }
    }

    /**
     * 绘制遮罩区域
     */
    private void drawMask(Canvas canvas) {
        maskPaint.setAntiAlias(true);
        maskPaint.setFilterBitmap(true);
        maskPaint.setColor(maskColor);
        maskPaint.setStyle(Paint.Style.FILL);

        Path path = new Path();
        RectF maskRect = new RectF((float) Math.floor(imageRect.left), (float) Math.floor(imageRect.top),
            (float) Math.ceil(imageRect.right), (float) Math.ceil(imageRect.bottom));

        if (!isAnimating && (cropMode == CropMode.CIRCLE || cropMode == CropMode.CIRCLE_SQUARE)) {
            path.addRect(maskRect, Path.Direction.CW);
            PointF circleCenter = new PointF(((gridRect.left + gridRect.right) / 2), ((gridRect.top + gridRect.bottom) / 2));
            float circleRadius = (gridRect.right - gridRect.left) / 2;
            path.addCircle(circleCenter.x, circleCenter.y, circleRadius, Path.Direction.CCW);
            canvas.drawPath(path, maskPaint);
        } else {
            path.addRect(maskRect, Path.Direction.CW);
            path.addRect(gridRect, Path.Direction.CCW);
            canvas.drawPath(path, maskPaint);
        }
    }

    /**
     * 绘制网格边框
     */
    private void drawCropArea(Canvas canvas) {
        gridPaint.setAntiAlias(true);
        gridPaint.setFilterBitmap(true);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(gridStroke);
        canvas.drawRect(gridRect, gridPaint);
    }

    /**
     * 绘制网格内部分割线
     */
    private void drawGridLines(Canvas canvas) {
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(girdLineStroke);

        // 绘制竖线
        for (int i = 1; i < girdColumnPart; i++) {
            float girdColumnX = gridRect.left + (gridRect.right - gridRect.left) / girdColumnPart * i;
            canvas.drawLine(girdColumnX, gridRect.top, girdColumnX, gridRect.bottom, gridPaint);
        }

        // 绘制横线
        for (int i = 1; i < girdRowPart; i++) {
            float girdRowY = gridRect.top + (gridRect.bottom - gridRect.top) / girdRowPart * i;
            canvas.drawLine(gridRect.left, girdRowY, gridRect.right, girdRowY, gridPaint);
        }
    }

    /**
     * 绘制拖拽点(网格角的四个角)
     */
    private void drawDragPoint(Canvas canvas) {
        gridPaint.setStyle(Paint.Style.FILL);
        gridPaint.setColor(gridColor);
        canvas.drawCircle(gridRect.left, gridRect.top, dragPointSize, gridPaint);
        canvas.drawCircle(gridRect.right, gridRect.top, dragPointSize, gridPaint);
        canvas.drawCircle(gridRect.left, gridRect.bottom, dragPointSize, gridPaint);
        canvas.drawCircle(gridRect.right, gridRect.bottom, dragPointSize, gridPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        executor.shutdown();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInit) {
            return false;
        }
        if (!isCropEnabled) {
            return false;
        }
        if (isLockCropArea) {
            return false;
        }
        if (isRotating) {
            return false;
        }
        if (isAnimating) {
            return false;
        }
        if (isLoading.get()) {
            return false;
        }
        if (isCropping.get()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                if (touchAnchor != TouchAnchor.OUT_OF_BOUNDS) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                onCancel();
                return true;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                onUp(event);
                return true;
        }
        return false;
    }

    private void onActionDown(MotionEvent e) {
        invalidate();
        lastX = e.getX();
        lastY = e.getY();

        // 测量锚点
        measureTouchAnchor(e.getX(), e.getY());
    }

    /**
     * 测量锚点
     */
    private void measureTouchAnchor(float x, float y) {
        if (isInsideLeftTopDragArea(x, y)) {
            touchAnchor = TouchAnchor.LEFT_TOP;
            if (gridLineMode == GridLineMode.SHOW_ON_TOUCH) {
                isShowGridLine = true;
            }
            return;
        }
        if (isInsideRightTopDragArea(x, y)) {
            touchAnchor = TouchAnchor.RIGHT_TOP;
            if (gridLineMode == GridLineMode.SHOW_ON_TOUCH) {
                isShowGridLine = true;
            }
            return;
        }
        if (isInsideLeftBottomDragArea(x, y)) {
            touchAnchor = TouchAnchor.LEFT_BOTTOM;
            if (gridLineMode == GridLineMode.SHOW_ON_TOUCH) {
                isShowGridLine = true;
            }
            return;
        }
        if (isInsideRightBottomDragArea(x, y)) {
            touchAnchor = TouchAnchor.RIGHT_BOTTOM;
            if (gridLineMode == GridLineMode.SHOW_ON_TOUCH) {
                isShowGridLine = true;
            }
            return;
        }
        if (isInsideGrid(x, y)) {
            if (gridLineMode == GridLineMode.SHOW_ON_TOUCH) {
                isShowGridLine = true;
            }
            touchAnchor = TouchAnchor.CENTER;
            return;
        }
        touchAnchor = TouchAnchor.OUT_OF_BOUNDS;
    }

    /**
     * 是否在左上角拖拽区域
     */
    private boolean isInsideLeftTopDragArea(float x, float y) {
        float dx = x - gridRect.left;
        float dy = y - gridRect.top;
        float area = dx * dx + dy * dy;
        return isEffectiveDragArea(area);
    }

    /**
     * 是否是有效的拖拽区域
     */
    private boolean isEffectiveDragArea(float area) {
        return Math.pow(dragPointSize + dragPointPadding, 2) >= area;
    }

    /**
     * 是否在右上角拖拽区域
     */
    private boolean isInsideRightTopDragArea(float x, float y) {
        float dx = x - gridRect.right;
        float dy = y - gridRect.top;
        float area = dx * dx + dy * dy;
        return isEffectiveDragArea(area);
    }

    /**
     * 是否在左下角拖拽区域
     */
    private boolean isInsideLeftBottomDragArea(float x, float y) {
        float dx = x - gridRect.left;
        float dy = y - gridRect.bottom;
        float area = dx * dx + dy * dy;
        return isEffectiveDragArea(area);
    }

    /**
     * 是否在右下角拖拽区域
     */
    private boolean isInsideRightBottomDragArea(float x, float y) {
        float dx = x - gridRect.right;
        float dy = y - gridRect.bottom;
        float area = dx * dx + dy * dy;
        return isEffectiveDragArea(area);
    }

    /**
     * 是否在表格内
     */
    private boolean isInsideGrid(float x, float y) {
        if (gridRect.left <= x && gridRect.right >= x) {
            if (gridRect.top <= y && gridRect.bottom >= y) {
                touchAnchor = TouchAnchor.CENTER;
                return true;
            }
        }
        return false;
    }

    private void onMove(MotionEvent e) {
        float diffX = e.getX() - lastX;
        float diffY = e.getY() - lastY;
        switch (touchAnchor) {
            case CENTER:
                moveDragCenter(diffX, diffY);
                break;
            case LEFT_TOP:
                moveDragLeftTop(diffX, diffY);
                break;
            case RIGHT_TOP:
                moveDragRightTop(diffX, diffY);
                break;
            case LEFT_BOTTOM:
                moveDragLeftBottom(diffX, diffY);
                break;
            case RIGHT_BOTTOM:
                moveDragRightBottom(diffX, diffY);
                break;
            case OUT_OF_BOUNDS:
                break;
            default:
                break;
        }
        invalidate();

        lastX = e.getX();
        lastY = e.getY();
    }

    private void moveDragCenter(float x, float y) {
        gridRect.left += x;
        gridRect.right += x;
        gridRect.top += y;
        gridRect.bottom += y;

        // 校准表格位置
        float diff = gridRect.left - imageRect.left;
        if (diff < 0) {
            gridRect.left -= diff;
            gridRect.right -= diff;
        }
        diff = gridRect.right - imageRect.right;
        if (diff > 0) {
            gridRect.left -= diff;
            gridRect.right -= diff;
        }
        diff = gridRect.top - imageRect.top;
        if (diff < 0) {
            gridRect.top -= diff;
            gridRect.bottom -= diff;
        }
        diff = gridRect.bottom - imageRect.bottom;
        if (diff > 0) {
            gridRect.top -= diff;
            gridRect.bottom -= diff;
        }
    }

    private void moveDragLeftTop(float diffX, float diffY) {
        if (cropMode == CropMode.FREE) {
            gridRect.left += diffX;
            gridRect.top += diffY;
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.left -= offsetX;
            }

            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.top -= offsetY;
            }
            calibrationGrid();
        } else {
            gridRect.left += diffX;
            gridRect.top += diffX * getRatioY() / getRatioX();
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.left -= offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.top -= offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.top -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.left -= offsetX;
            }
            if (!isInsideHorizontal(gridRect.left)) {
                float offsetX = imageRect.left - gridRect.left;
                gridRect.left += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.top += offsetY;
            }
            if (!isInsideVertical(gridRect.top)) {
                float offsetY = imageRect.top - gridRect.top;
                gridRect.top += offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.left += offsetX;
            }
        }
    }

    private boolean isWidthTooSmall() {
        return getGridWith() < gridMinSize;
    }

    private float getGridWith() {
        return (gridRect.right - gridRect.left);
    }

    private boolean isHeightTooSmall() {
        return getGridHeight() < gridMinSize;
    }

    private float getGridHeight() {
        return (gridRect.bottom - gridRect.top);
    }

    private boolean isInsideHorizontal(float x) {
        return imageRect.left <= x && imageRect.right >= x;
    }

    private boolean isInsideVertical(float y) {
        return imageRect.top <= y && imageRect.bottom >= y;
    }

    /**
     * 获取校准x比例
     */
    private float getRatioX() {
        switch (cropMode) {
            case FIT_IMAGE:
                return imageRect.width();
            case RATIO_4_3:
                return 4;
            case RATIO_3_4:
                return 3;
            case RATIO_16_9:
                return 16;
            case RATIO_9_16:
                return 9;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return customCropRatio.x;
            default:
                return 1;
        }
    }

    /**
     * 获取校准y比例
     */
    private float getRatioY() {
        switch (cropMode) {
            case FIT_IMAGE:
                return imageRect.height();
            case RATIO_4_3:
                return 3;
            case RATIO_3_4:
                return 4;
            case RATIO_16_9:
                return 9;
            case RATIO_9_16:
                return 16;
            case SQUARE:
            case CIRCLE:
            case CIRCLE_SQUARE:
                return 1;
            case CUSTOM:
                return customCropRatio.y;
            default:
                return 1;
        }
    }

    /**
     * 校准表格位置
     */
    private void calibrationGrid() {
        float leftDiff = gridRect.left - imageRect.left;
        float rightDiff = gridRect.right - imageRect.right;
        float topDiff = gridRect.top - imageRect.top;
        float bottomDiff = gridRect.bottom - imageRect.bottom;

        if (leftDiff < 0) {
            gridRect.left -= leftDiff;
        }
        if (rightDiff > 0) {
            gridRect.right -= rightDiff;
        }
        if (topDiff < 0) {
            gridRect.top -= topDiff;
        }
        if (bottomDiff > 0) {
            gridRect.bottom -= bottomDiff;
        }
    }

    private void moveDragRightTop(float diffX, float diffY) {
        if (cropMode == CropMode.FREE) {
            gridRect.right += diffX;
            gridRect.top += diffY;
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.right += offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.top -= offsetY;
            }
            calibrationGrid();
        } else {
            gridRect.right += diffX;
            gridRect.top -= diffX * getRatioY() / getRatioX();
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.right += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.top -= offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.top -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.right += offsetX;
            }
            if (!isInsideHorizontal(gridRect.right)) {
                float offsetX = gridRect.right - imageRect.right;
                gridRect.right -= offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.top += offsetY;
            }
            if (!isInsideVertical(gridRect.top)) {
                float offsetY = imageRect.top - gridRect.top;
                gridRect.top += offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.right -= offsetX;
            }
        }
    }

    private void moveDragLeftBottom(float diffX, float diffY) {
        if (cropMode == CropMode.FREE) {
            gridRect.left += diffX;
            gridRect.bottom += diffY;
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.left -= offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.bottom += offsetY;
            }
            calibrationGrid();
        } else {
            gridRect.left += diffX;
            gridRect.bottom -= diffX * getRatioY() / getRatioX();
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.left -= offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.bottom += offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.bottom += offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.left -= offsetX;
            }
            if (!isInsideHorizontal(gridRect.left)) {
                float offsetX = imageRect.left - gridRect.left;
                gridRect.left += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.bottom -= offsetY;
            }
            if (!isInsideVertical(gridRect.bottom)) {
                float offsetY = gridRect.bottom - imageRect.bottom;
                gridRect.bottom -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.left += offsetX;
            }
        }
    }

    private void moveDragRightBottom(float diffX, float diffY) {
        if (cropMode == CropMode.FREE) {
            gridRect.right += diffX;
            gridRect.bottom += diffY;
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.right += offsetX;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.bottom += offsetY;
            }
            calibrationGrid();
        } else {
            gridRect.right += diffX;
            gridRect.bottom += diffX * getRatioY() / getRatioX();
            if (isWidthTooSmall()) {
                float offsetX = gridMinSize - getGridWith();
                gridRect.right += offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.bottom += offsetY;
            }
            if (isHeightTooSmall()) {
                float offsetY = gridMinSize - getGridHeight();
                gridRect.bottom += offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.right += offsetX;
            }
            if (!isInsideHorizontal(gridRect.right)) {
                float offsetX = gridRect.right - imageRect.right;
                gridRect.right -= offsetX;
                float offsetY = offsetX * getRatioY() / getRatioX();
                gridRect.bottom -= offsetY;
            }
            if (!isInsideVertical(gridRect.bottom)) {
                float offsetY = gridRect.bottom - imageRect.bottom;
                gridRect.bottom -= offsetY;
                float offsetX = offsetY * getRatioX() / getRatioY();
                gridRect.right -= offsetX;
            }
        }
    }

    private void onUp(MotionEvent e) {
        if (gridLineMode == GridLineMode.SHOW_ON_TOUCH) {
            isShowGridLine = false;
        }
        touchAnchor = TouchAnchor.OUT_OF_BOUNDS;
        invalidate();
    }

    private void onCancel() {
        touchAnchor = TouchAnchor.OUT_OF_BOUNDS;
        invalidate();
    }

    /**
     * 重新计算GridRect
     */
    private void recalculateGridRect(int duration) {
        if (imageRect == null) {
            return;
        }
        if (isAnimating) {
            getAnimator().cancel();
        }
        final RectF currentRect = new RectF(gridRect);
        final RectF newRect = getGridRect();
        final float diffLeft = newRect.left - currentRect.left;
        final float diffTop = newRect.top - currentRect.top;
        final float diffRight = newRect.right - currentRect.right;
        final float diffBottom = newRect.bottom - currentRect.bottom;
        if (isAnimEnabled) {
            JackAnimator jackAnimator = getAnimator();
            jackAnimator.setOnAnimatorListener(new JackAnimator.OnJackAnimationListener() {
                @Override
                public void onAnimationStart() {
                    isAnimating = true;
                }

                @Override
                public void onAnimationUpdate(float scale) {
                    gridRect = new RectF(currentRect.left + diffLeft * scale, currentRect.top + diffTop * scale,
                        currentRect.right + diffRight * scale, currentRect.bottom + diffBottom * scale);
                    invalidate();
                }

                @Override
                public void onAnimationEnd() {
                    gridRect = newRect;
                    invalidate();
                    isAnimating = false;
                }
            });
            jackAnimator.start(duration);
        } else {
            gridRect = getGridRect();
            invalidate();
        }
    }

    private JackAnimator getAnimator() {
        if (jackAnimator == null) {
            jackAnimator = new JackAnimator(interpolator);
        }
        return jackAnimator;
    }

    private Bitmap scaleBitmapIfNeeded(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int outWidth = 0;
        int outHeight = 0;
        float imageRatio = getRatioX(gridRect.width()) / getRatioY(gridRect.height());

        if (outputWidth > 0) {
            outWidth = outputWidth;
            outHeight = Math.round(outputWidth / imageRatio);
        } else if (outputHeight > 0) {
            outHeight = outputHeight;
            outWidth = Math.round(outputHeight * imageRatio);
        } else {
            if (outputMaxWidth > 0 && outputMaxHeight > 0 && (width > outputMaxWidth || height > outputMaxHeight)) {
                float maxRatio = (float) outputMaxWidth / (float) outputMaxHeight;
                if (maxRatio >= imageRatio) {
                    outHeight = outputMaxHeight;
                    outWidth = Math.round((float) outputMaxHeight * imageRatio);
                } else {
                    outWidth = outputMaxWidth;
                    outHeight = Math.round((float) outputMaxWidth / imageRatio);
                }
            }
        }

        if (outWidth > 0 && outHeight > 0) {
            Bitmap scaled = ImageUtils.getScaledBitmap(bitmap, outWidth, outHeight);
            if (bitmap != getBitmap() && bitmap != scaled) {
                bitmap.recycle();
            }
            bitmap = scaled;
        }
        return bitmap;
    }

    /**
     * 获取Bitmap
     */
    public Bitmap getImageBitmap() {
        return getBitmap();
    }

    /**
     * 设置图片
     */
    @Override
    public void setImageResource(int resId) {
        isInit = false;
        resetImageInfo();
        super.setImageResource(resId);
        updateLayout();
    }

    /**
     * 重置图片信息
     */
    private void resetImageInfo() {
        if (isLoading.get()) {
            return;
        }
        sourceUri = null;
        saveUri = null;
        angle = exifRotation;
    }

    private void updateLayout() {
        Drawable d = getDrawable();
        if (d != null) {
            setupLayout();
        }
    }

    /**
     * 设置图片
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        isInit = false;
        resetImageInfo();
        setImageDrawableInternal(drawable);
    }

    private void setImageDrawableInternal(Drawable drawable) {
        super.setImageDrawable(drawable);
        updateLayout();
    }

    /**
     * 设置图片uri
     */
    @Override
    public void setImageURI(Uri uri) {
        isInit = false;
        super.setImageURI(uri);
        updateLayout();
    }

    /**
     * 设置裁剪模式
     *
     * @see CropMode
     */
    public void setCropMode(CropMode cropMode) {
        setCropMode(cropMode, animDuration);
    }

    /**
     * 设置裁剪模式
     *
     * @see CropMode
     */
    public void setCropMode(CropMode cropMode, int duration) {
        if (cropMode == CropMode.CUSTOM) {
            setCustomRatio(1, 1);
        } else {
            this.cropMode = cropMode;
            recalculateGridRect(duration);
        }
    }

    /**
     * 设置自定义裁剪比例
     */
    public void setCustomRatio(int ratioX, int ratioY) {
        setCustomRatio(ratioX, ratioY, animDuration);
    }

    /**
     * 设置自定义裁剪比例
     */
    public void setCustomRatio(int ratioX, int ratioY, int duration) {
        if (ratioX == 0 || ratioY == 0) {
            return;
        }
        cropMode = CropMode.CUSTOM;
        customCropRatio = new PointF(ratioX, ratioY);
        recalculateGridRect(duration);
    }

    /**
     * 设置背景颜色
     */
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        invalidate();
    }

    /**
     * 设置遮罩颜色
     */
    public void setOverlayColor(int overlayColor) {
        this.maskColor = overlayColor;
        invalidate();
    }

    /**
     * 设置网格的色调
     */
    public void setGridColor(int guideColor) {
        this.gridColor = guideColor;
        invalidate();
    }

    /**
     * 设置最小裁剪区域
     */
    public void setGridMinSize(int gridMinSize) {
        this.gridMinSize = gridMinSize;
    }

    /**
     * @param gridLineMode 网格线的显示模式
     * @see GridLineMode
     */
    public void setGridLineMode(GridLineMode gridLineMode) {
        this.gridLineMode = gridLineMode;
        switch (gridLineMode) {
            case SHOW_ALWAYS:
                isShowGridLine = Boolean.TRUE;
                break;
            case NOT_SHOW:
            case SHOW_ON_TOUCH:
                isShowGridLine = Boolean.FALSE;
                break;
            default:
                break;
        }
        invalidate();
    }

    /**
     * 网格边框宽度
     */
    public void setGridStroke(int gridStroke) {
        this.gridStroke = gridStroke;
        invalidate();
    }

    /**
     * 网格分割线大小
     */
    public void setGirdLineStroke(int girdLineStroke) {
        this.girdLineStroke = girdLineStroke;
        invalidate();
    }

    /**
     * 是否启用裁剪
     */
    public void setCropEnabled(boolean enabled) {
        isCropEnabled = enabled;
        invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setLockCropArea(!enabled);
    }

    /**
     * 是否锁定截取区域
     */
    public void setLockCropArea(boolean lockCropArea) {
        isLockCropArea = lockCropArea;
    }

    /**
     * 设置拖拽点的大小
     */
    public void setDragPointSize(int dragPointSize) {
        this.dragPointSize = dragPointSize;
    }

    /**
     * 设置拖拽点padding(可通过dragPointPadding调整拖拽范围)
     */
    public void setDragPointPadding(int dragPointPadding) {
        this.dragPointPadding = (dragPointPadding);
    }

    /**
     * 旋转图片
     *
     * @param degrees 旋转角度
     */
    public void rotateImage(RotateAngle degrees) {
        rotateImage(degrees, animDuration);
    }

    /**
     * 旋转图片
     *
     * @param degrees  旋转角度
     * @param duration 旋转动画持续时间
     */
    public void rotateImage(RotateAngle degrees, int duration) {
        if (isRotating) {
            getAnimator().cancel();
        }

        final float currentAngle = angle;
        final float newAngle = (angle + degrees.angle);
        final float angleDiff = newAngle - currentAngle;
        final float currentScale = scale;
        final float newScale = measureScale(newAngle);

        if (isAnimEnabled) {
            final float scaleDiff = newScale - currentScale;
            JackAnimator jackAnimator = getAnimator();
            jackAnimator.setOnAnimatorListener(new JackAnimator.OnJackAnimationListener() {
                @Override
                public void onAnimationStart() {
                    isRotating = true;
                }

                @Override
                public void onAnimationUpdate(float scale) {
                    angle = currentAngle + angleDiff * scale;
                    CropImageView.this.scale = currentScale + scaleDiff * scale;
                    resetMatrix();
                    invalidate();
                }

                @Override
                public void onAnimationEnd() {
                    angle = newAngle % 360;
                    scale = newScale;
                    setupLayout();
                    isRotating = false;
                }
            });
            jackAnimator.start(duration);
        } else {
            angle = newAngle % 360;
            scale = newScale;
            setupLayout();
        }
    }

    /**
     * 是否启用动画
     */
    public void setAnimationEnabled(boolean isAnimEnabled) {
        this.isAnimEnabled = isAnimEnabled;
    }

    /**
     * 设置动画持续时间
     */
    public void setAnimationDuration(int animDuration) {
        this.animDuration = animDuration;
    }

    /**
     * 设置动画插值器
     */
    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        jackAnimator = getAnimator();
    }

    /**
     * 设置输出宽度(裁剪宽度)
     */
    public void setOutputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
        outputHeight = 0;
    }

    /**
     * 设置输出高度(裁剪高度)
     */
    public void setOutputHeight(int outputHeight) {
        this.outputHeight = outputHeight;
        outputWidth = 0;
    }

    /**
     * 设置裁剪最大宽度以及最大高度(如果已设置固定的输出高度或宽度将以固定值为准)
     */
    public void setOutputMaxSize(int outputMaxWidth, int outputMaxHeight) {
        this.outputMaxWidth = outputMaxWidth;
        this.outputMaxHeight = outputMaxHeight;
    }

    /**
     * 设置图片格式
     *
     * @see android.graphics.Bitmap.CompressFormat
     */
    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    /**
     * 设置压缩质量
     */
    public void setCompressQuality(@IntRange(from = 0, to = 100) int compressQuality) {
        this.compressQuality = compressQuality;
    }

    /**
     * 是否正在裁剪
     */
    public boolean isCropping() {
        return isCropping.get();
    }

    /**
     * 获取数据源uri
     */
    public Uri getSourceUri() {
        return sourceUri;
    }

    /**
     * 获取保存的uri
     */
    public Uri getSaveUri() {
        return saveUri;
    }

    /**
     * 设置裁剪事件监听(裁剪、保存)
     */
    public CropImageView setOnCropMultiListener(OnCropMultiListener cropMultiListener) {
        this.onCropMultiListener = cropMultiListener;
        return this;
    }

    /**
     * 设置加载监听
     */
    public CropImageView setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    /**
     * 裁剪监听
     */
    public CropImageView setOnCropListener(OnCropListener onCropListener) {
        this.onCropListener = onCropListener;
        return this;
    }

    /**
     * 保存监听
     */
    public CropImageView setOnCropSaveListener(OnCropSaveListener onSaveListener) {
        this.onCropSaveListener = onSaveListener;
        return this;
    }

    /**
     * 加载图片
     */
    public void load(final Uri sourceUri) {
        load(sourceUri, false);
    }

    /**
     * 加载图片
     */
    public void load(final Uri sourceUri, final boolean useThumbnail) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    isLoading.set(true);
                    CropImageView.this.sourceUri = sourceUri;

                    if (useThumbnail) {
                        setThumbnail(sourceUri);
                    }

                    final Bitmap bitmap = getImage(sourceUri);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            angle = exifRotation;
                            setImageDrawableInternal(new BitmapDrawable(getResources(), bitmap));
                            if (onCropMultiListener == null) {
                                if (onLoadListener != null) {
                                    onLoadListener.onLoadSuccess();
                                }
                            } else {
                                onCropMultiListener.onLoadSuccess();
                            }
                        }
                    });
                } catch (Exception e) {
                    if (onCropMultiListener == null) {
                        postErrorOnMainThread(onLoadListener, e);
                    } else {
                        postErrorOnMainThread(onCropMultiListener, e);
                    }
                } finally {
                    isLoading.set(false);
                }
            }
        });
    }

    private void setThumbnail(Uri sourceUri) {
        final Bitmap thumb = getThumbnail(sourceUri);
        if (thumb == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                angle = exifRotation;
                setImageDrawableInternal(new BitmapDrawable(getResources(), thumb));
            }
        });
    }

    private Bitmap getThumbnail(Uri sourceUri) {
        if (sourceUri == null) {
            throw new NullPointerException("uri 不能为 null!");
        }

        exifRotation = Utils.getExifOrientation(getContext(), this.sourceUri);
        int requestSize = (int) (Math.max(viewWidth, viewHeight) * 0.1f);
        if (requestSize == 0) {
            return null;
        }
        return ImageUtils.getBitmapFromUri(getContext(), sourceUri, requestSize);
    }

    private Bitmap getImage(Uri sourceUri) {
        if (sourceUri == null) {
            throw new NullPointerException("uri 不能为 null!");
        }

        exifRotation = Utils.getExifOrientation(getContext(), sourceUri);
        int maxSize = Utils.getMaxSize();
        int requestSize = Math.max(viewWidth, viewHeight);
        if (requestSize == 0) {
            requestSize = maxSize;
        }
        return ImageUtils.getBitmapFromUri(getContext(), sourceUri, requestSize);
    }

    private void postErrorOnMainThread(final BaseCropListener baseCallback, final Throwable e) {
        if (baseCallback == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            baseCallback.onError(e);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    baseCallback.onError(e);
                }
            });
        }
    }

    /**
     * 开始裁剪图片(仅截取,不进行保存)
     */
    public void crop() {
        crop(null);
    }

    /**
     * 开始裁剪图片
     *
     * @param saveUri 图片保存的uri(null为不保存)
     */
    public void crop(final Uri saveUri) {
        this.saveUri = saveUri;

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap cropBitmap;
                try {
                    isCropping.set(Boolean.TRUE);
                    cropBitmap = cropImage();
                    final Bitmap tempCropBitmap = cropBitmap;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onCropMultiListener == null) {
                                if (onCropListener != null) {
                                    onCropListener.onCropSuccess(tempCropBitmap);
                                }
                            } else {
                                onCropMultiListener.onCropSuccess(tempCropBitmap);
                            }
                        }
                    });

                    // 保存截图
                    if (saveUri != null) {
                        Utils.saveImage(getContext(), cropBitmap, sourceUri, saveUri, compressFormat, compressQuality);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onCropMultiListener == null) {
                                    if (onCropSaveListener != null) {
                                        onCropSaveListener.onCropSaveSuccess(saveUri);
                                    }
                                } else {
                                    onCropMultiListener.onCropSaveSuccess(saveUri);
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    startCropError(e);
                } finally {
                    isCropping.set(Boolean.FALSE);
                }
            }
        });
    }

    /**
     * 裁剪错误处理
     */
    private void startCropError(final Exception e) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (onCropMultiListener == null) {
                postErrorOnMainThread(onCropListener, e);
                postErrorOnMainThread(onCropSaveListener, e);
            } else {
                postErrorOnMainThread(onCropMultiListener, e);
            }
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (onCropMultiListener == null) {
                        postErrorOnMainThread(onCropListener, e);
                        postErrorOnMainThread(onCropSaveListener, e);
                    } else {
                        postErrorOnMainThread(onCropMultiListener, e);
                    }
                }
            });
        }
    }

    private Bitmap cropImage() throws IOException, IllegalStateException {
        Bitmap bitmap;
        if (sourceUri == null) {
            // Use thumbnail
            bitmap = getCropBitmap();
        } else {
            // Use file
            bitmap = getCropBitmapFromUri();
            if (cropMode == CropMode.CIRCLE) {
                Bitmap circleBitmap = ImageUtils.getCircleBitmap(bitmap);
                if (bitmap != getBitmap()) {
                    bitmap.recycle();
                }
                bitmap = circleBitmap;
            }
        }

        bitmap = scaleBitmapIfNeeded(bitmap);
        return bitmap;
    }

    public Bitmap getCropBitmap() {
        Bitmap bitmap = getBitmap();
        if (bitmap == null) {
            return null;
        }

        Bitmap rotateBitmap = ImageUtils.getRotateBitmap(bitmap, angle);
        Rect cropRect = measureCropRect(bitmap.getWidth(), bitmap.getHeight());
        Bitmap cropBitmap = Bitmap.createBitmap(rotateBitmap, cropRect.left, cropRect.top, cropRect.width(),
            cropRect.height(), null, false);
        if (rotateBitmap != cropBitmap && rotateBitmap != bitmap) {
            rotateBitmap.recycle();
        }

        if (cropMode == CropMode.CIRCLE) {
            Bitmap circle = ImageUtils.getCircleBitmap(cropBitmap);
            if (cropBitmap != getBitmap()) {
                cropBitmap.recycle();
            }
            cropBitmap = circle;
        }
        return cropBitmap;
    }

    private Rect measureCropRect(int originalImageWidth, int originalImageHeight) {
        float scaleToOriginal = getRotatedWidth(angle, originalImageWidth, originalImageHeight) / imageRect.width();
        float offsetX = imageRect.left * scaleToOriginal;
        float offsetY = imageRect.top * scaleToOriginal;
        int left = Math.round(gridRect.left * scaleToOriginal - offsetX);
        int top = Math.round(gridRect.top * scaleToOriginal - offsetY);
        int right = Math.round(gridRect.right * scaleToOriginal - offsetX);
        int bottom = Math.round(gridRect.bottom * scaleToOriginal - offsetY);
        int imageWidth = Math.round(getRotatedWidth(angle, originalImageWidth, originalImageHeight));
        int imageHeight = Math.round(getRotatedHeight(angle, originalImageWidth, originalImageHeight));
        return new Rect(Math.max(left, 0), Math.max(top, 0), Math.min(right, imageWidth), Math.min(bottom, imageHeight));
    }

    private Bitmap getCropBitmapFromUri() throws IOException {
        Bitmap bitmap;
        InputStream inputStream = null;
        try {
            inputStream = getContext().getContentResolver().openInputStream(sourceUri);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
            final int originalImageWidth = decoder.getWidth();
            final int originalImageHeight = decoder.getHeight();
            Rect cropRect = measureCropRect(originalImageWidth, originalImageHeight);
            if (angle != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(-angle);
                RectF rotateRectF = new RectF();
                matrix.mapRect(rotateRectF, new RectF(cropRect));
                rotateRectF.offset(rotateRectF.left < 0 ? originalImageWidth : 0,
                    rotateRectF.top < 0 ? originalImageHeight : 0);
                cropRect = new Rect((int) rotateRectF.left, (int) rotateRectF.top,
                    (int) rotateRectF.right, (int) rotateRectF.bottom);
            }
            bitmap = decoder.decodeRegion(cropRect, new BitmapFactory.Options());
            if (angle != 0) {
                Bitmap rotateBitmap = ImageUtils.getRotateBitmap(bitmap, angle);
                if (bitmap != getBitmap() && bitmap != rotateBitmap) {
                    bitmap.recycle();
                }
                bitmap = rotateBitmap;
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);

        savedState.angle = this.angle;

        savedState.gridMinSize = this.gridMinSize;
        savedState.gridStroke = this.gridStroke;
        savedState.gridColor = this.gridColor;
        savedState.isCropEnabled = this.isCropEnabled;
        savedState.cropMode = this.cropMode;

        savedState.girdLineStroke = this.girdLineStroke;
        savedState.girdRowPart = this.girdRowPart;
        savedState.girdColumnPart = this.girdColumnPart;
        savedState.isShowGridLine = this.isShowGridLine;
        savedState.gridLineMode = this.gridLineMode;

        savedState.dragPointSize = this.dragPointSize;
        savedState.dragPointPadding = this.dragPointPadding;
        savedState.isShowDragPoint = this.isShowDragPoint;

        savedState.bgColor = this.bgColor;
        savedState.maskColor = this.maskColor;

        savedState.isLockCropArea = this.isLockCropArea;

        savedState.sourceUri = this.sourceUri;
        savedState.saveUri = this.saveUri;
        savedState.exifRotation = this.exifRotation;
        savedState.outputMaxWidth = this.outputMaxWidth;
        savedState.outputMaxHeight = this.outputMaxHeight;
        savedState.outputWidth = this.outputWidth;
        savedState.outputHeight = this.outputHeight;
        savedState.compressQuality = this.compressQuality;
        savedState.customCropRatioX = this.customCropRatio.x;
        savedState.customCropRatioY = this.customCropRatio.y;
        savedState.compressFormat = this.compressFormat;

        savedState.isAnimEnabled = this.isAnimEnabled;
        savedState.animDuration = this.animDuration;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        this.angle = savedState.angle;

        this.gridMinSize = savedState.gridMinSize;
        this.gridStroke = savedState.gridStroke;
        this.gridColor = savedState.gridColor;
        this.isCropEnabled = savedState.isCropEnabled;
        this.cropMode = savedState.cropMode;

        this.girdLineStroke = savedState.girdLineStroke;
        this.girdRowPart = savedState.girdRowPart;
        this.girdColumnPart = savedState.girdColumnPart;
        this.isShowGridLine = savedState.isShowGridLine;
        this.gridLineMode = savedState.gridLineMode;

        this.dragPointSize = savedState.dragPointSize;
        this.dragPointPadding = savedState.dragPointPadding;
        this.isShowDragPoint = savedState.isShowDragPoint;

        this.sourceUri = savedState.sourceUri;
        this.saveUri = savedState.saveUri;
        this.exifRotation = savedState.exifRotation;
        this.outputMaxWidth = savedState.outputMaxWidth;
        this.outputMaxHeight = savedState.outputMaxHeight;
        this.outputWidth = savedState.outputWidth;
        this.outputHeight = savedState.outputHeight;
        this.compressQuality = savedState.compressQuality;
        this.customCropRatio = new PointF(savedState.customCropRatioX, savedState.customCropRatioY);
        this.compressFormat = savedState.compressFormat;

        this.bgColor = savedState.bgColor;
        this.maskColor = savedState.maskColor;

        this.isAnimEnabled = savedState.isAnimEnabled;
        this.animDuration = savedState.animDuration;


    }

    /**
     * 触摸点位置
     */
    private enum TouchAnchor {
        OUT_OF_BOUNDS, CENTER, LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM
    }
}
