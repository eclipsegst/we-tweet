package com.zhaolongzhong.wetweet.nav;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zhaolongzhong.wetweet.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/7/16.
 */

public class ListsActivity extends AppCompatActivity {
    private static final String TAG = ListsActivity.class.getSimpleName();

    @BindView(R.id.lists_activity_text_view_id) TextView textView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ListsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists_activity);
        ButterKnife.bind(this);

        textView.setText(TAG);
        //todo: add content
    }

    /**
     * Invalidate all views
     */
    private void invalidateViews() {
        // invalidate views
    }
}
