package com.example.android.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ThemeSettingsActivity extends Activity {

    private ListView themeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用当前主题
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        // 设置ActionBar标题
        if (getActionBar() != null) {
            getActionBar().setTitle("主题设置");
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化ListView
        themeListView = (ListView) findViewById(R.id.theme_list);

        // 创建适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_single_choice,
                ThemeUtils.THEME_NAMES
        );

        themeListView.setAdapter(adapter);
        themeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // 设置当前选中的主题
        int currentTheme = ThemeUtils.getCurrentTheme(this);
        themeListView.setItemChecked(currentTheme, true);

        // 设置点击监听器
        themeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取当前主题
                int oldTheme = ThemeUtils.getCurrentTheme(ThemeSettingsActivity.this);

                // 如果选择的是新主题才保存
                if (position != oldTheme) {
                    // 保存主题
                    ThemeUtils.saveTheme(ThemeSettingsActivity.this, position);

                    // 显示提示
                    Toast.makeText(ThemeSettingsActivity.this,
                            "已选择：" + ThemeUtils.THEME_NAMES[position],
                            Toast.LENGTH_SHORT).show();

                    // 设置结果，通知调用者主题已更改
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("theme_changed", true);
                    setResult(Activity.RESULT_OK, resultIntent);

                    // 立即应用主题到当前Activity
                    ThemeUtils.applyTheme(ThemeSettingsActivity.this);

                    // 重新创建当前Activity以立即看到效果
                    recreate();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}