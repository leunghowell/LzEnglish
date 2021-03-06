package com.jiubai.lzenglish.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jiubai.lzenglish.R;
import com.jiubai.lzenglish.adapter.SearchVideoAdapter;
import com.jiubai.lzenglish.bean.Cartoon;
import com.jiubai.lzenglish.common.StatusBarUtil;
import com.jiubai.lzenglish.common.UtilBox;
import com.jiubai.lzenglish.config.Config;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchVideoActivity extends BaseActivity {

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.editText)
    EditText mEditText;

    @Bind(R.id.view_background)
    View mBackgroundView;

    @Bind(R.id.imageView_search)
    ImageView mSearchImageView;

    @Bind(R.id.layout_toolbar)
    RelativeLayout mToolbarLayout;

    private SearchVideoAdapter mAdapter;
    private ArrayList<Cartoon> resultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_video);

        StatusBarUtil.StatusBarLightMode(this);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        resultList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new SearchVideoAdapter(this, resultList);
        mRecyclerView.setAdapter(mAdapter);

        mToolbarLayout.setPadding(0, Config.StatusbarHeight, 0, 0);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                UtilBox.toggleSoftInput(mEditText, false);
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable currentText) {
                resultList = new ArrayList<>();

                if (TextUtils.isEmpty(mEditText.getText().toString())) {
                    mAdapter = new SearchVideoAdapter(SearchVideoActivity.this, resultList);
                    mAdapter.setEditing(false);
                } else {
                    for (Cartoon cartoon : Config.CartoonList) {
                        String currentString = currentText.toString().toLowerCase();
                        if (cartoon.getName().toLowerCase().contains(currentString)) {
                            resultList.add(cartoon);
                        }
                    }

                    mAdapter = new SearchVideoAdapter(SearchVideoActivity.this, resultList);
                    mAdapter.setHint(currentText.toString());
                    mAdapter.setEditing(true);
                }

                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    @OnClick({R.id.textView_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        UtilBox.returnActivity(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onResume() {
        if (TextUtils.isEmpty(Config.ThirdSession)) {
            finish();
        }

        super.onResume();
    }
}
