package com.zhaolongzhong.wetweet.moments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaolongzhong.wetweet.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/4/16.
 */

public class MomentsFragment extends Fragment {
    private static final String TAG = MomentsFragment.class.getSimpleName();

    @BindView(R.id.moments_fragment_text_view_id) TextView textView;

    public static Fragment newInstance() {
        return new MomentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.moments_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        textView.setText(TAG);
    }

    /**
     * Invalidate all views
     */
    private void invalidateViews() {
        // invalidate views
    }
}
