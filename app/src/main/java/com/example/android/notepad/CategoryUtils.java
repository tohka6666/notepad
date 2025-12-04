package com.example.android.notepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryUtils {

    private static final String PREF_CATEGORIES = "note_categories";
    private static final String TAG = "CategoryUtils";

    // 初始化默认分类
    public static void initDefaultCategories(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains(PREF_CATEGORIES)) {
            Set<String> defaultCategories = new HashSet<>();
            defaultCategories.add("默认");
            defaultCategories.add("工作");
            defaultCategories.add("学习");
            defaultCategories.add("生活");
            defaultCategories.add("个人");
            defaultCategories.add("重要");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(PREF_CATEGORIES, defaultCategories);
            editor.apply();

            Log.d(TAG, "初始化默认分类完成");
        }
    }

    // 获取所有分类
    public static List<String> getAllCategories(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> categories = prefs.getStringSet(PREF_CATEGORIES, new HashSet<String>());

        List<String> result = new ArrayList<>(categories);

        // 确保包含默认分类
        if (!result.contains("默认")) {
            result.add(0, "默认");
        }

        return result;
    }

    // 添加分类
    public static boolean addCategory(Context context, String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        category = category.trim();
        if (category.equals("默认")) {
            return false; // 不能添加"默认"分类，因为它已经存在
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> categories = prefs.getStringSet(PREF_CATEGORIES, new HashSet<String>());

        // 创建新的Set（StringSet不可直接修改）
        Set<String> newCategories = new HashSet<>(categories);
        if (newCategories.add(category)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(PREF_CATEGORIES, newCategories);
            return editor.commit();
        }

        return false;
    }

    // 删除分类
    public static boolean deleteCategory(Context context, String category) {
        if ("默认".equals(category)) {
            return false; // 不能删除默认分类
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> categories = prefs.getStringSet(PREF_CATEGORIES, new HashSet<String>());

        Set<String> newCategories = new HashSet<>(categories);
        if (newCategories.remove(category)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(PREF_CATEGORIES, newCategories);
            return editor.commit();
        }

        return false;
    }

    // 获取分类数量
    public static int getCategoryCount(Context context) {
        return getAllCategories(context).size();
    }
}