package com.example.android.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagerActivity extends Activity {

    private ListView listView;
    private List<String> categories;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_manager);

        listView = (ListView) findViewById(R.id.category_list);

        // 加载分类列表
        loadCategories();

        // 设置长按删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String category = categories.get(position);

                // 不能删除默认分类
                if ("默认".equals(category)) {
                    Toast.makeText(CategoryManagerActivity.this,
                            "不能删除默认分类", Toast.LENGTH_SHORT).show();
                    return true;
                }

                showDeleteDialog(category);
                return true;
            }
        });

        // 添加简单的返回按钮
        setupBackButton();
    }

    private void setupBackButton() {
        // 可以在布局中添加一个返回按钮，或者使用设备的返回键
        // 这里简单处理，使用设备的返回键
    }

    private void loadCategories() {
        categories = new ArrayList<>();
        categories.add("默认");
        categories.add("工作");
        categories.add("个人");
        categories.add("学习");

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);
    }

    private void showDeleteDialog(final String category) {
        new AlertDialog.Builder(this)
                .setTitle("删除分类")
                .setMessage("确定要删除分类 '" + category + "' 吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCategory(category);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteCategory(String category) {
        if (categories.remove(category)) {
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();

            // TODO: 这里可以添加将笔记移动到默认分类的逻辑
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_manager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_category) {
            showAddCategoryDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加新分类");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        final EditText editText = (EditText) view.findViewById(R.id.edit_category);

        builder.setView(view);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String category = editText.getText().toString().trim();
                if (!category.isEmpty()) {
                    addCategory(category);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
            adapter.notifyDataSetChanged();
             // 清空输入框
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "分类已存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // 返回时，可以将分类列表传递给NotesList
        finish();
    }
}