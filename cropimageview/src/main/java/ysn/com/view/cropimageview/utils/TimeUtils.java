package ysn.com.view.cropimageview.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author yangsanning
 * @ClassName TimeUtils
 * @Description 时间工具类
 * @Date 2020/1/7
 * @History 2020/1/7 author: description:
 */
public class TimeUtils {

    public static final String ALL_FORMAT = "yyyyMMddHHmmssSSS";

    public static String getAllFormat(Date date) {
        return new SimpleDateFormat(ALL_FORMAT, Locale.getDefault()).format(date);
    }
}
