package ysn.com.view.cropimageview.mode;

/**
 * @Author yangsanning
 * @ClassName CropMode
 * @Description 裁剪模式
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public enum CropMode {

    /**
     * 正方形
     */
    SQUARE(0),

    /**
     * 圆
     */
    CIRCLE(1),

    /**
     * 正方形截取
     */
    CIRCLE_SQUARE(2),

    /**
     * 满屏
     */
    FIT_IMAGE(3),

    /**
     * 4:3
     */
    RATIO_4_3(4),

    /**
     * 3:4
     */
    RATIO_3_4(5),

    /**
     * 16:9
     */
    RATIO_16_9(6),

    /**
     * 9:16
     */
    RATIO_9_16(7),

    FREE(8),

    /**
     * 自定义
     */
    CUSTOM(9);

    public final int mode;

    CropMode(final int mode) {
        this.mode = mode;
    }

    public static CropMode getValue(int value) {
        switch (value) {
            case 1:
                return CIRCLE;
            case 2:
                return CIRCLE_SQUARE;
            case 3:
                return FIT_IMAGE;
            case 4:
                return RATIO_4_3;
            case 5:
                return RATIO_3_4;
            case 6:
                return RATIO_16_9;
            case 7:
                return RATIO_9_16;
            case 8:
                return FREE;
            case 9:
                return CUSTOM;
            default:
                return SQUARE;
        }
    }
}
