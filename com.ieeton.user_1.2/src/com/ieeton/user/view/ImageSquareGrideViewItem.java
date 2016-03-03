package com.ieeton.user.view;

import android.widget.RelativeLayout;
import android.content.Context;
import android.util.AttributeSet;

public class ImageSquareGrideViewItem extends RelativeLayout {

    public ImageSquareGrideViewItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageSquareGrideViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageSquareGrideViewItem(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
                getDefaultSize(0, heightMeasureSpec));
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        // 高度和宽度一样
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
