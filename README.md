拓展基本功能：
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
<item
        android:id="@+id/menu_search"
        android:icon="@android:drawable/ic_menu_search"
        android:title="Search"
        android:actionViewClass="android.widget.SearchView"
        android:showAsAction="ifRoom|collapseActionView" />

在noteslist中添加搜索方法

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

设置搜索菜单：

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



实现效果截图：
<img width="530" height="721" alt="image" src="https://github.com/user-attachments/assets/09bc4612-bc40-4404-b2af-99c0cdfdbc73" />


