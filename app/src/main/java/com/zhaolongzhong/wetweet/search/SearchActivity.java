package com.zhaolongzhong.wetweet.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.zhaolongzhong.wetweet.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/5/16.
 */

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    @BindView(R.id.search_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.search_activity_toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.search_activity_toolbar_search_edit_text_id) EditText searchEditText;
    @BindView(R.id.search_activity_close_image_view_id) ImageView closeImageView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        ButterKnife.bind(this);

        setupToolbar();

        searchEditText.addTextChangedListener(searchTextWatcher);
        closeImageView.setOnClickListener(v -> {
            searchEditText.setText("");
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        backImageView.setOnClickListener(v -> close());
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            closeImageView.setVisibility(searchEditText.getText().length() == 0 ? View.GONE : View.VISIBLE);
            // todo: add search logic
        }
    };

    @Override
    public void onBackPressed() {
        close();
    }

    public void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }
}
