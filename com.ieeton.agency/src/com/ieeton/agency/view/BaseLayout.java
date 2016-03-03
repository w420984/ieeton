package com.ieeton.agency.view;


import com.ieeton.agency.R;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseLayout extends RelativeLayout {
    // Title type
    public final static int TYPE_BUTTON_GROUP = 0;
    public final static int TYPE_NORMAL = 1;

	public ImageView leftButton;
    public TextView rightButton;
    public ImageView rightImage;
    public TextView tvInfo;

    public RelativeLayout progressButton;
    public View titlebar;

    public void setButtonTypeAndInfo(String left, String middle, String right) {
        // mode = type;
        Resources r = this.getResources();
        if (TextUtils.isEmpty(left)) {
            leftButton.setVisibility(View.GONE);
        } else {
            if (left.equalsIgnoreCase(r.getString(R.string.back))) {
                leftButton.setImageResource(R.drawable.mm_title_back);
            } else {
                //leftButton.setImageResource(R.drawable.common_tab_bg);
            }
        }
        if (!(TextUtils.isEmpty(middle))) {
            this.setTitle(middle);
        }
        if (TextUtils.isEmpty(right)) {
            rightButton.setVisibility(View.GONE);
        	rightImage.setVisibility(View.GONE);
        } else {
            if (right.equalsIgnoreCase(r.getString(R.string.title_add))) {
            	rightButton.setVisibility(View.GONE);
            	rightImage.setVisibility(View.VISIBLE);
            	rightImage.setImageResource(R.drawable.icon_title_add);
            } else if (right.equalsIgnoreCase(r.getString(R.string.share))) {
            	rightButton.setVisibility(View.GONE);
            	rightImage.setVisibility(View.VISIBLE);
            	rightImage.setImageResource(R.drawable.btn_share_selector);
            } else {
            	rightButton.setVisibility(View.VISIBLE);
            	rightImage.setVisibility(View.GONE);
            	rightButton.setText(right);
            }
        }
    }

    public void setTitle(String title) {
        tvInfo.setText(title, TextView.BufferType.NORMAL);
    }

    public BaseLayout(Context context, int resId) {
        super(context);

        LayoutInflater i = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        titlebar = i.inflate(R.layout.titlebar, null);
        LayoutParams titlelp = null;

        int titleHeight = getResources().getDimensionPixelSize(R.dimen.height_top_bar);
        titlelp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, titleHeight);
        titlelp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        // titlebar.setLayoutParams(titlelp);
        this.addView(titlebar, titlelp);

        View contentView = i.inflate(resId, null);
        LayoutParams contentlp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        contentlp.addRule(RelativeLayout.BELOW, R.id.lyTitleBar);
        this.addView(contentView, contentlp);
        leftButton = (ImageView) findViewById(R.id.titleBack);
        rightButton = (TextView) findViewById(R.id.titleSave);
        rightImage = (ImageView) findViewById(R.id.titleRight);
        tvInfo = (TextView) findViewById(R.id.titleText);
        progressButton = (RelativeLayout) findViewById(R.id.rlProgressBar);
        tvInfo.setBackgroundDrawable(null);
    }

}
