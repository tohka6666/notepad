package com.example.android.notepad;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 主题工具类
 */
public class ThemeUtils {

    // 主题常量
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_BLUE = 2;
    public static final int THEME_GREEN = 3;
    public static final int THEME_PURPLE = 4;
    public static final int THEME_ORANGE = 5;

    // 主题名称数组
    public static final String[] THEME_NAMES = {
            "浅色主题",
            "深色主题",
            "蓝色主题",
            "绿色主题",
            "紫色主题",
            "橙色主题"
    };

    // 主题文字颜色数组
    public static final int[] THEME_TEXT_COLORS = {
            0xFF000000, // 黑色 - 浅色主题
            0xFFFFFFFF, // 白色 - 深色主题
            0xFF0D47A1, // 深蓝色 - 蓝色主题
            0xFF1B5E20, // 深绿色 - 绿色主题
            0xFF4A148C, // 深紫色 - 紫色主题
            0xFFE65100  // 深橙色 - 橙色主题
    };

    // 主题背景颜色数组
    public static final int[] THEME_BACKGROUND_COLORS = {
            0xFFFFFFFF, // 白色 - 浅色主题
            0xFF303030, // 深灰色 - 深色主题
            0xFFE3F2FD, // 浅蓝色 - 蓝色主题
            0xFFE8F5E9, // 浅绿色 - 绿色主题
            0xFFF3E5F5, // 浅紫色 - 紫色主题
            0xFFFFF3E0  // 浅橙色 - 橙色主题
    };

    // 主题ActionBar颜色数组
    public static final int[] THEME_ACTIONBAR_COLORS = {
            0xFF3F51B5, // 靛蓝色 - 浅色主题
            0xFF212121, // 深灰色 - 深色主题
            0xFF2196F3, // 蓝色 - 蓝色主题
            0xFF4CAF50, // 绿色 - 绿色主题
            0xFF9C27B0, // 紫色 - 紫色主题
            0xFFFF9800  // 橙色 - 橙色主题
    };

    // 主题次要文字颜色（时间戳、内容预览等）
    public static final int[] THEME_SECONDARY_TEXT_COLORS = {
            0xFF757575, // 灰色 - 浅色主题
            0xFFB0B0B0, // 浅灰色 - 深色主题
            0xFF5472D3, // 蓝色 - 蓝色主题
            0xFF4CAF50, // 绿色 - 绿色主题
            0xFF7B1FA2, // 紫色 - 紫色主题
            0xFFF57C00  // 橙色 - 橙色主题
    };

    /**
     * 获取当前主题
     */
    public static int getCurrentTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("app_theme", THEME_LIGHT);
    }

    /**
     * 保存选中的主题
     */
    public static void saveTheme(Context context, int theme) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("app_theme", theme);
        editor.apply();
    }

    /**
     * 应用主题到Activity
     */
    public static void applyTheme(Activity activity) {
        int theme = getCurrentTheme(activity);

        // 根据主题设置样式
        switch (theme) {
            case THEME_DARK:
                activity.setTheme(R.style.Theme_Dark);
                break;
            case THEME_BLUE:
                activity.setTheme(R.style.Theme_Blue);
                break;
            case THEME_GREEN:
                activity.setTheme(R.style.Theme_Green);
                break;
            case THEME_PURPLE:
                activity.setTheme(R.style.Theme_Purple);
                break;
            case THEME_ORANGE:
                activity.setTheme(R.style.Theme_Orange);
                break;
            case THEME_LIGHT:
            default:
                activity.setTheme(R.style.Theme_Light);
                break;
        }
    }

    /**
     * 获取当前主题的文字颜色
     * @param context 上下文
     * @return 文字颜色值
     */
    public static int getCurrentTextColor(Context context) {
        int theme = getCurrentTheme(context);
        if (theme >= 0 && theme < THEME_TEXT_COLORS.length) {
            return THEME_TEXT_COLORS[theme];
        }
        return THEME_TEXT_COLORS[THEME_LIGHT]; // 默认返回浅色主题颜色
    }

    /**
     * 获取当前主题的背景颜色
     * @param context 上下文
     * @return 背景颜色值
     */
    public static int getCurrentBackgroundColor(Context context) {
        int theme = getCurrentTheme(context);
        if (theme >= 0 && theme < THEME_BACKGROUND_COLORS.length) {
            return THEME_BACKGROUND_COLORS[theme];
        }
        return THEME_BACKGROUND_COLORS[THEME_LIGHT]; // 默认返回浅色主题颜色
    }

    /**
     * 获取当前主题的ActionBar颜色
     * @param context 上下文
     * @return ActionBar颜色值
     */
    public static int getCurrentActionBarColor(Context context) {
        int theme = getCurrentTheme(context);
        if (theme >= 0 && theme < THEME_ACTIONBAR_COLORS.length) {
            return THEME_ACTIONBAR_COLORS[theme];
        }
        return THEME_ACTIONBAR_COLORS[THEME_LIGHT]; // 默认返回浅色主题颜色
    }

    /**
     * 获取当前主题的次要文字颜色
     * @param context 上下文
     * @return 次要文字颜色值
     */
    public static int getCurrentSecondaryTextColor(Context context) {
        int theme = getCurrentTheme(context);
        if (theme >= 0 && theme < THEME_SECONDARY_TEXT_COLORS.length) {
            return THEME_SECONDARY_TEXT_COLORS[theme];
        }
        return THEME_SECONDARY_TEXT_COLORS[THEME_LIGHT]; // 默认返回浅色主题颜色
    }

    /**
     * 获取主题名称
     * @param themeIndex 主题索引
     * @return 主题名称
     */
    public static String getThemeName(int themeIndex) {
        if (themeIndex >= 0 && themeIndex < THEME_NAMES.length) {
            return THEME_NAMES[themeIndex];
        }
        return THEME_NAMES[THEME_LIGHT];
    }

    /**
     * 获取当前主题名称
     * @param context 上下文
     * @return 当前主题名称
     */
    public static String getCurrentThemeName(Context context) {
        int theme = getCurrentTheme(context);
        return getThemeName(theme);
    }

    /**
     * 检查主题是否已更改
     * @param context 上下文
     * @param oldTheme 旧主题索引
     * @return 如果主题已更改返回true
     */
    public static boolean hasThemeChanged(Context context, int oldTheme) {
        int currentTheme = getCurrentTheme(context);
        return currentTheme != oldTheme;
    }

    /**
     * 获取所有主题数量
     * @return 主题数量
     */
    public static int getThemeCount() {
        return THEME_NAMES.length;
    }

    /**
     * 获取主题的文字颜色
     * @param themeIndex 主题索引
     * @return 文字颜色值
     */
    public static int getTextColor(int themeIndex) {
        if (themeIndex >= 0 && themeIndex < THEME_TEXT_COLORS.length) {
            return THEME_TEXT_COLORS[themeIndex];
        }
        return THEME_TEXT_COLORS[THEME_LIGHT];
    }

    /**
     * 获取主题的背景颜色
     * @param themeIndex 主题索引
     * @return 背景颜色值
     */
    public static int getBackgroundColor(int themeIndex) {
        if (themeIndex >= 0 && themeIndex < THEME_BACKGROUND_COLORS.length) {
            return THEME_BACKGROUND_COLORS[themeIndex];
        }
        return THEME_BACKGROUND_COLORS[THEME_LIGHT];
    }
}