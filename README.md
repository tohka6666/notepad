拓展基本功能：
（一）.笔记条目增加时间戳显示
功能要求：
新建笔记保存新建时间并显示，并在修改笔记后更新为修改时间。
实现：
(1)在notelist_item中添加一个TextView用来显示时间

<TextView android:id="@+id/timestamp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textAppearance="?android:attr/textAppearanceSmall"
        android:layout_marginTop="4dp" />
  
