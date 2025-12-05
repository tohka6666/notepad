
（一）笔记条目增加时间戳显示  
功能要求：  
新建笔记保存新建时间并显示，并在修改笔记后更新为修改时间。  
实现：  
在notelist_item中添加一个TextView用来显示时间  

```xml
<TextView android:id="@+id/timestamp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textAppearance="?android:attr/textAppearanceSmall"
        android:layout_marginTop="4dp" />
```
loadNotes方法中确保时间戳正确显示
```java
TextView timestampView = (TextView) view.findViewById(R.id.timestamp);
long timestamp = cursor.getLong(COLUMN_INDEX_MODIFICATION_DATE);
timestampView.setText(formatTimestamp(timestamp));
```
数据库中添加时间字段
```java
public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                   + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                   + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                   + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                   + NotePad.Notes.COLUMN_NAME_CATEGORY + " TEXT DEFAULT '默认'" // 添加分类字段
                   + ");");

       }
```
NoteEditor中的updateNote方法：
```java
 private final void updateNote(String text, String title) {
        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis();

        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);

        // 使用成员变量中的分类
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, mCurrentCategory);

        if (title != null) {
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        }

        if (text != null) {
            values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
        }

        if (mState == STATE_INSERT) {
            if (title == null && text != null) {
                title = text.substring(0, Math.min(30, text.length()));
            }
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);

            getContentResolver().insert(getIntent().getData(), values);
        } else {
            getContentResolver().update(mUri, values, null, null);
        }
    }
```
实现效果截图：  

<img width="355" height="213" alt="image" src="https://github.com/user-attachments/assets/22bfe1f5-9bcf-441b-b84d-0798a3572810" />

（二）笔记查询功能  
功能要求：  
可以根据标题和内容摘要对笔记进行搜索筛选。  

实现：  
先在笔记上方添加笔记搜索框  
```
<item
        android:id="@+id/menu_search"
        android:icon="@android:drawable/ic_menu_search"
        android:title="Search"
        android:actionViewClass="android.widget.SearchView"
        android:showAsAction="ifRoom|collapseActionView" />
```
在noteslist中添加搜索方法
```java
if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
            selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                    NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
            selectionArgs = new String[] {
                    "%" + mSearchQuery + "%",
                    "%" + mSearchQuery + "%"
            };

            // 如果有搜索查询，显示搜索结果标题
            setTitle("Search: " + mSearchQuery);
        }
```
设置搜索菜单：
```java
public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        // 设置搜索菜单项
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            mSearchView = (SearchView) searchItem.getActionView();

            if (mSearchView != null) {
                mSearchView.setQueryHint("Search notes...");
                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // 执行搜索
                        performSearch(query);
                        mSearchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // 实时搜索（可选）
                        if (newText.length() >= 2) {
                            // 取消注释下面这行启用实时搜索
                            // performSearch(newText);
                        } else if (newText.isEmpty()) {
                            // 清空搜索，显示所有笔记
                            mSearchQuery = null;
                            loadNotes();
                        }
                        return true;
                    }
                });

                // 如果有搜索查询，在搜索框中显示
                if (mSearchQuery != null && !mSearchQuery.isEmpty()) {
                    searchItem.expandActionView();
                    mSearchView.setQuery(mSearchQuery, false);
                }
            }
        }

        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }
```
实现效果截图：  

<img width="530" height="721" alt="image" src="https://github.com/user-attachments/assets/09bc4612-bc40-4404-b2af-99c0cdfdbc73" />


（三）ui美化  
功能要求：提供几种不同主题，让用户能够选择不同的ui界面  
功能实现：  
先在菜单中添加主题选择按钮：  
```
<item
        android:id="@+id/menu_theme"
        android:icon="@android:drawable/ic_menu_preferences"
        android:title="主题"
        app:showAsAction="ifRoom" />
```
在onCreate方法中调用主题应用函数
```java
private void applyThemeToUI() {
        // 获取当前主题的颜色
        int textColor = ThemeUtils.getCurrentTextColor(this);
        int bgColor = ThemeUtils.getCurrentBackgroundColor(this);

        // 设置列表背景色
        getListView().setBackgroundColor(bgColor);

        // 设置ActionBar颜色
        if (getActionBar() != null) {
            // 根据主题设置ActionBar颜色
            int theme = ThemeUtils.getCurrentTheme(this);
            switch (theme) {
                case ThemeUtils.THEME_DARK:
                    getActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF212121));
                    break;
                case ThemeUtils.THEME_BLUE:
                    getActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF2196F3));
                    break;
                case ThemeUtils.THEME_GREEN:
                    getActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF4CAF50));
                    break;
                case ThemeUtils.THEME_PURPLE:
                    getActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFF9C27B0));
                    break;
                case ThemeUtils.THEME_ORANGE:
                    getActionBar().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFFFF9800));
                    break;
            }
        }
    }
```
添加主题验证方法
```
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_THEME && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("theme_changed", false)) {
                // 检查主题是否真的改变了
                int newTheme = ThemeUtils.getCurrentTheme(this);
                if (newTheme != currentTheme) {
                    // 主题已更改，重新创建Activity
                    recreate();
                }
            }
        }
    }
```
添加ThemeUtils工具类
```java
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

}
```


实现效果截图：  
<img width="531" height="944" alt="image" src="https://github.com/user-attachments/assets/10adefc6-8530-47cb-8f3a-b51add0fe578" />  

<img width="526" height="963" alt="image" src="https://github.com/user-attachments/assets/f5e7cab6-b6e6-459c-893d-c4f7d5a9c467" />  

<img width="526" height="928" alt="image" src="https://github.com/user-attachments/assets/aff5d293-0603-4340-ad0b-77d8a1cbce23" />  


（四）分类管理  
功能要求：对笔记进行分类管理，能够进行分类筛选  

功能实现：  
添加分类筛选框  
```xml
<TextView
        android:id="@+id/text_category"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:layout_marginEnd="8dp"
        android:textSize="12sp"
        android:textColor="@android:color/darker_gray"/>
```

在数据库中添加分类字段
```java
private static final String[] READ_NOTE_PROJECTION = new String[] {
            NotePad.Notes._ID,               // Projection position 0, the note's id
            NotePad.Notes.COLUMN_NAME_NOTE,  // Projection position 1, the note's content
            NotePad.Notes.COLUMN_NAME_TITLE, // Projection position 2, the note's title
            NotePad.Notes.COLUMN_NAME_CATEGORY

    };
```

添加setupCategoryFilter方法，获取所有分类
```java
    private void setupCategoryFilter() {
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);

        List<String> categories = CategoryUtils.getAllCategories(this);
        categories.add(0, "全部笔记");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if ("全部笔记".equals(selected)) {
                    currentCategory = null;
                } else {
                    currentCategory = selected;
                }
                loadNotes(); // 重新加载笔记
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCategory = null;
            }
        });
    }
```

在loadNotes中添加分类筛选
```java
else if (currentCategory != null && !"全部笔记".equals(currentCategory)) {
    selection = NotePad.Notes.COLUMN_NAME_CATEGORY + " = ?";
    selectionArgs = new String[]{currentCategory};
}else {
    setTitle(getText(R.string.notes_title));
}
```

添加CategoryUtils工具类
```java
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
```


实现效果截图：  
<img width="527" height="753" alt="image" src="https://github.com/user-attachments/assets/6a5f2c67-bde1-4e6c-a4f9-931d9e80f30a" />
<img width="515" height="603" alt="image" src="https://github.com/user-attachments/assets/ace7b535-35b6-44f9-b2bb-2a6f3b0562df" />



