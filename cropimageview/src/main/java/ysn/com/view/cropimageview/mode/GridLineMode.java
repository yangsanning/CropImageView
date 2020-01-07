package ysn.com.view.cropimageview.mode;

/**
 * @Author yangsanning
 * @ClassName GridLineMode
 * @Description 网格线的显示模式
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public enum GridLineMode {

    /**
     * 总是显示
     */
    SHOW_ALWAYS(0),

    /**
     * 触摸显示
     */
    SHOW_ON_TOUCH(1),

    /**
     * 不显示
     */
    NOT_SHOW(2);

    public final int mode;

    GridLineMode(final int mode) {
        this.mode = mode;
    }

    public static GridLineMode getValue(int value) {
        switch (value) {
            case 1:
                return SHOW_ON_TOUCH;
            case 2:
                return NOT_SHOW;
            default:
                return SHOW_ALWAYS;
        }
    }
}
