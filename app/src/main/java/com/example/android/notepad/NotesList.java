package com.example.android.notepad;

import com.example.android.notepad.NotePad;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class NotesList extends ListActivity {

    // For logging and debugging
    private static final String TAG = "NotesList";
    private static final int REQUEST_CODE_THEME = 100;
    // 在类中添加成员变量
    private String currentCategory = null;
    private Spinner categorySpinner;
    private String currentFilter = null;

    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_NOTE, // 2 - 添加内容字段用于搜索
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE  // 3 - 添加修改时间

    };


    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    /** The index of the content column */
    private static final int COLUMN_INDEX_CONTENT = 2;
    /** The index of the modification date column */
    private static final int COLUMN_INDEX_MODIFICATION_DATE = 3;

    // 搜索相关变量
    private String mSearchQuery;
    private SearchView mSearchView;

    // 记录当前主题，用于检测主题是否改变
    private int currentTheme = ThemeUtils.THEME_LIGHT;

    /**
     * Time formatting utility method
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "";
        }

        SimpleDateFormat fullFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        return fullFormat.format(new Date(timestamp));
    }

    /**
     * onCreate is called when Android starts this Activity from scratch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 记录当前主题
        currentTheme = ThemeUtils.getCurrentTheme(this);

        super.onCreate(savedInstanceState);

        // The user does not need to hold down the key to use menu shortcuts.
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        /* If no data is given in the Intent that started this Activity, then this Activity
         * was started when the intent filter matched a MAIN action. We should use the default
         * provider URI.
         */
        // Gets the intent that started this Activity.
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        // 处理搜索Intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
        }

        getListView().setOnCreateContextMenuListener(this);

        // 设置自定义布局，包含分类筛选器
        setContentView(R.layout.noteslist_with_category);

        // 初始化分类筛选器
        setupCategoryFilter();

        // 加载笔记列表
        loadNotes();

        // 应用主题颜色到列表项
        applyThemeToUI();

        // 初始化分类系统
        CategoryUtils.initDefaultCategories(this);
    }


    // 添加setupCategoryFilter方法
    private void setupCategoryFilter() {
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);

        // 获取所有分类
        List<String> categories = new ArrayList<>();
        categories.add("全部笔记");
        categories.add("默认");
        categories.add("工作");
        categories.add("个人");
        categories.add("学习");

        // TODO: 替换为从CategoryUtils获取真实数据
        // List<String> categories = CategoryUtils.getAllCategories(this);
        // categories.add(0, "全部笔记");

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
    /**
     * 应用主题颜色到UI元素
     */
    private void applyThemeToUI() {
        // 获取当前主题的颜色
        int textColor = ThemeUtils.getCurrentTextColor(this);
        int bgColor = ThemeUtils.getCurrentBackgroundColor(this);

        // 设置列表背景色
        getListView().setBackgroundColor(bgColor);

        // 设置ActionBar颜色（如果可用）
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

    /**
     * 加载笔记列表
     */
    private void loadNotes() {
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " DESC";

        // 如果有搜索查询，添加搜索条件
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
        // 如果有分类筛选
        else if (currentCategory != null && !"全部笔记".equals(currentCategory)) {
            selection = NotePad.Notes.COLUMN_NAME_CATEGORY + " = ?";
            selectionArgs = new String[]{currentCategory};
        }else {
            setTitle(getText(R.string.notes_title));
        }

        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder
        );

        /*
         * Creates a custom adapter that displays the note title and formatted timestamp
         */
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                new String[] { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE },
                new int[] { android.R.id.text1, R.id.timestamp }
        ) {
            @Override
            public void setViewText(TextView v, String text) {
                // If this is the timestamp view, format the timestamp
                if (v.getId() == R.id.timestamp) {
                    try {
                        long timestamp = Long.parseLong(text);
                        v.setText(formatTimestamp(timestamp));
                    } catch (NumberFormatException e) {
                        v.setText("");
                    }
                } else {
                    super.setViewText(v, text);
                }
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);

                // 确保时间戳正确显示
                TextView timestampView = (TextView) view.findViewById(R.id.timestamp);
                long timestamp = cursor.getLong(COLUMN_INDEX_MODIFICATION_DATE);
                timestampView.setText(formatTimestamp(timestamp));

                // 设置内容预览
                TextView contentView = (TextView) view.findViewById(R.id.text2);
                String content = cursor.getString(COLUMN_INDEX_CONTENT);
                if (content != null) {
                    String contentPreview = content.length() > 50 ?
                            content.substring(0, 50) + "..." : content;
                    contentView.setText(contentPreview);
                } else {
                    contentView.setText("");
                }

                // 根据主题设置列表项颜色
                int textColor = ThemeUtils.getCurrentTextColor((NotesList) context);
                int bgColor = ThemeUtils.getCurrentBackgroundColor((NotesList) context);

                TextView titleView = (TextView) view.findViewById(android.R.id.text1);
                if (titleView != null) {
                    titleView.setTextColor(textColor);
                }
                if (timestampView != null) {
                    timestampView.setTextColor(textColor & 0x80FFFFFF); // 半透明
                }
                if (contentView != null) {
                    contentView.setTextColor(textColor & 0x80FFFFFF); // 半透明
                }
            }
        };

        // Sets the ListView's adapter to be the cursor adapter that was just created.
        setListAdapter(adapter);
    }

    /**
     * Called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Sets up a menu that provides the Insert option plus a list of alternative actions for
     * this Activity. Other applications that want to handle notes can "register" themselves in
     * Android by providing an intent filter that includes the category ALTERNATIVE and the
     * mimeTYpe NotePad.Notes.CONTENT_TYPE. If they do this, the code in onCreateOptionsMenu()
     * will add the Activity that contains the intent filter to its list of options. In effect,
     * the menu will offer the user other applications that can handle notes.
     * @param menu A Menu object, to which menu items should be added.
     * @return True, always. The menu should be displayed.
     */
    @Override
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

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);



    }

    /**
     * 执行搜索
     */
    private void performSearch(String query) {
        mSearchQuery = query;
        loadNotes();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // The paste menu item is enabled if there is data on the clipboard.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        // If the clipboard contains an item, enables the Paste option on the menu.
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // If the clipboard is empty, disables the menu's Paste option.
            mPasteItem.setEnabled(false);
        }

        // Gets the number of notes currently being displayed.
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {

            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Creates an array of Intents with one element. This will be used to send an Intent
            // based on the selected menu item.
            Intent[] specifics = new Intent[1];

            // Sets the Intent in the array to be an EDIT action on the URI of the selected note.
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // Creates an array of menu items with one element. This will contain the EDIT option.
            MenuItem[] items = new MenuItem[1];

            // Creates an Intent with no specific action, using the URI of the selected note.
            Intent intent = new Intent(null, uri);

            /* Adds the category ALTERNATIVE to the Intent, with the note ID URI as its
             * data. This prepares the Intent as a place to group alternative options in the
             * menu.
             */
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            /*
             * Add alternatives to the menu
             */
            menu.addIntentOptions(
                    Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
                    Menu.NONE,                  // A unique item ID is not required.
                    Menu.NONE,                  // The alternatives don't need to be in order.
                    null,                       // The caller's name is not excluded from the group.
                    specifics,                  // These specific options must appear first.
                    intent,                     // These Intent objects map to the options in specifics.
                    Menu.NONE,                  // No flags are required.
                    items                       // The menu items generated from the specifics-to-
                    // Intents mapping
            );
            // If the Edit menu item exists, adds shortcuts for it.
            if (items[0] != null) {

                // Sets the Edit menu item shortcut to numeric "1", letter "e"
                items[0].setShortcut('1', 'e');
            }
        } else {
            // If the list is empty, removes any existing alternative actions from the menu
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        // Displays the menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            /*
             * Launches a new Activity using an Intent. The intent filter for the Activity
             * has to have action ACTION_INSERT. No category is set, so DEFAULT is assumed.
             * In effect, this starts the NoteEditor Activity in NotePad.
             */
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        } else if (id == R.id.menu_paste) {
            /*
             * Launches a new Activity using an Intent. The intent filter for the Activity
             * has to have action ACTION_PASTE. No category is set, so DEFAULT is assumed.
             * In effect, this starts the NoteEditor Activity in NotePad.
             */
            startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
            return true;
        } else if (id == R.id.menu_search) {
            // 搜索菜单项已通过SearchView处理
            return true;
        } else if (id == R.id.menu_theme) {
            // 启动主题设置Activity，并期望返回结果
            Intent intent = new Intent(this, ThemeSettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_THEME);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // 添加onActivityResult方法
    @Override
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

    @Override
    protected void onResume() {
        super.onResume();

        // 检查主题是否在后台被更改
        int newTheme = ThemeUtils.getCurrentTheme(this);
        if (newTheme != currentTheme) {
            // 主题已更改，重新创建Activity
            recreate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // 处理新的搜索Intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
            loadNotes();
        }
    }

    /**
     * This method is called when the user context-clicks a note in the list. NotesList registers
     * itself as the handler for context menus in its ListView (this is done in onCreate()).
     *
     * The only available options are COPY and DELETE.
     *
     * Context-click is equivalent to long-press.
     *
     * @param menu A ContexMenu object to which items should be added.
     * @param view The View for which the context menu is being constructed.
     * @param menuInfo Data associated with view.
     * @throws ClassCastException
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        // Tries to get the position of the item in the ListView that was long-pressed.
        try {
            // Casts the incoming data object into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // If the menu object can't be cast, logs an error.
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        /*
         * Gets the data associated with the item at the selected position. getItem() returns
         * whatever the backing adapter of the ListView has associated with its list item. In NotesList,
         * the adapter associated all of the data for a note with its list item. As a result,
         * getItem() returns that data as a Cursor.
         */
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        // If the cursor is empty, then for some reason the adapter can't get the data from the
        // provider, so returns null to the caller.
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // Sets the menu header to be the title of the selected note.
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }

    /**
     * This method is called when the user selects an item from the context menu
     * (see onCreateContextMenu()). The only menu items that are actually handled are DELETE and
     * COPY. Anything else is an alternative option, for which default handling should be done.
     *
     * @param item The selected menu item
     * @return True if the menu item was DELETE, and no default processing is need, otherwise false,
     * which triggers the default handling of the item.
     * @throws ClassCastException
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        /*
         * Gets the extra info from the menu item. When an note in the Notes list is long-pressed, a
         * context menu appears. The menu items for the menu automatically get the data
         * associated with the note that was long-pressed. The data comes from the provider that
         * backs the list.
         *
         * The note's data is passed to the context menu creation routine in a ContextMenuInfo
         * object.
         *
         * When one of the context menu items is clicked, the same data is passed, along with the
         * note ID, to onContextItemSelected() via the item parameter.
         */
        try {
            // Casts the data object in the item into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {

            // If the object can't be cast, logs an error
            Log.e(TAG, "bad menuInfo", e);

            // Triggers default processing of the menu item.
            return false;
        }
        // Appends the selected note's ID to the URI sent with the incoming Intent.
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        /*
         * Gets the menu item's ID and compares it to known actions.
         */
        int id = item.getItemId();
        if (id == R.id.context_open) {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
            return true;
        } else if (id == R.id.context_copy) { //BEGIN_INCLUDE(copy)
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);

            // Copies the notes URI to the clipboard. In effect, this copies the note itself
            clipboard.setPrimaryClip(ClipData.newUri(   // new clipboard item holding a URI
                    getContentResolver(),               // resolver to retrieve URI info
                    "Note",                             // label for the clip
                    noteUri));                          // the URI

            // Returns to the caller and skips further processing.
            return true;
            //END_INCLUDE(copy)
        } else if (id == R.id.context_delete) {
            // Deletes the note from the provider by passing in a URI in note ID format.
            // Please see the introductory note about performing provider operations on the
            // UI thread.
            getContentResolver().delete(
                    noteUri,  // The URI of the provider
                    null,     // No where clause is needed, since only a single note ID is being
                    // passed in.
                    null      // No where clause is used, so no where arguments are needed.
            );

            // Returns to the caller and skips further processing.
            return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * This method is called when the user clicks a note in the displayed list.
     *
     * This method handles incoming actions of either PICK (get data from the provider) or
     * GET_CONTENT (get or create data). If the incoming action is EDIT, this method sends a
     * new Intent to start NoteEditor.
     * @param l The ListView that contains the clicked item
     * @param v The View of the individual item
     * @param position The position of v in the displayed list
     * @param id The row ID of the clicked item
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}