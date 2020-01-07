package ysn.com.view.cropimageview.mode;

/**
 * @Author yangsanning
 * @ClassName RotateAngle
 * @Description 角度旋转
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public enum RotateAngle {

    /**
     * 90°
     */
    ROTATE_90D(90),

    /**
     * 180°
     */
    ROTATE_180D(180),

    /**
     * 270°
     */
    ROTATE_270D(270),

    /**
     * -90°
     */
    ROTATE_M90D(-90),

    /**
     * -180°
     */
    ROTATE_M180D(
        -180),

    /**
     * -270°
     */
    ROTATE_M270D(-270);

    public final int angle;

    RotateAngle(final int value) {
        this.angle = value;
    }

    public static RotateAngle getValue(int value) {
        switch (value) {
            case 180:
                return ROTATE_180D;
            case 270:
                return ROTATE_270D;
            case -90:
                return ROTATE_M90D;
            case -180:
                return ROTATE_M180D;
            case -270:
                return ROTATE_M270D;
            default:
                return ROTATE_90D;
        }
    }
}
