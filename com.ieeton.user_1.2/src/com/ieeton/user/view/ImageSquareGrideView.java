package com.ieeton.user.view;

import java.lang.reflect.Field;

import android.widget.GridView;
import android.content.Context;
import android.util.AttributeSet;

public class ImageSquareGrideView extends GridView {

    public ImageSquareGrideView(Context context) {
        super(context);
    }

    public ImageSquareGrideView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageSquareGrideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public int getNumColumn() {
        int numColumns = 0;
        // API level 11 引入getNumColumns()
        try {
            Field numColumnsField = GridView.class.getDeclaredField("mNumColumns");
            numColumnsField.setAccessible(true);
            numColumns = numColumnsField.getInt(this);
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (NoSuchFieldException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        return numColumns;
    }

}
