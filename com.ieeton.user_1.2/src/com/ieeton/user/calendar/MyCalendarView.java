package com.ieeton.user.calendar;

import java.util.List;

import com.ieeton.user.R;
import com.ieeton.user.calendar.CalendarCard.OnCellClickListener;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyCalendarView extends LinearLayout {
	private Context mContext;
    private ViewPager mViewPager;
    private int mCurrentIndex = 498;
    private CalendarCard[] mShowViews;
    private CalendarViewAdapter<CalendarCard> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE;
    }
     
//    private ImageButton preImgBtn, nextImgBtn;
    private TextView monthText;
	
	public MyCalendarView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public MyCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public MyCalendarView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public void setCellClickListener(OnCellClickListener listener){
		CalendarCard[] views = adapter.getAllItems();
		for(CalendarCard view : views){
			view.setCellClickListener(listener);
		}
	}
	
	public void setValidDateList(List<CustomDate> list){
		CalendarCard[] views = adapter.getAllItems();
		for(CalendarCard view : views){
			view.setValidDate(list);
		}
	}
	
	public TextView getMonthText(){
		return monthText;
	}
	
	private void init(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.calendar_view, this);
		
		mViewPager = (ViewPager) this.findViewById(R.id.vp_calendar);
//      preImgBtn = (ImageButton) this.findViewById(R.id.btnPreMonth);
//      nextImgBtn = (ImageButton) this.findViewById(R.id.btnNextMonth);
		monthText = (TextView) this.findViewById(R.id.tvCurrentMonth);
//      preImgBtn.setOnClickListener(this);
//      nextImgBtn.setOnClickListener(this);
		
		CustomDate date = new CustomDate();
		monthText.setText(date.year + "年" + date.month + "月");
		CalendarCard[] views = new CalendarCard[3];
		for (int i = 0; i < 3; i++) {
			views[i] = new CalendarCard(mContext, (OnCellClickListener)null);
		}
		adapter = new CalendarViewAdapter<CalendarCard>(views);
		setViewPager();
	}

    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
             
            @Override
            public void onPageSelected(int position) {
                measureDirection(position);
                updateCalendarView(position);               
            }
             
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                 
            }
             
            @Override
            public void onPageScrollStateChanged(int arg0) {
                 
            }
        });
    }
    
    /**
     * 计算方向
     * 
     * @param arg0
     */
    private void measureDirection(int arg0) {
 
        if (arg0 > mCurrentIndex) {
            mDirection = SildeDirection.RIGHT;
 
        } else if (arg0 < mCurrentIndex) {
            mDirection = SildeDirection.LEFT;
        }
        mCurrentIndex = arg0;
    }
 
    // 更新日历视图
    private void updateCalendarView(int arg0) {
        mShowViews = adapter.getAllItems();
        if (mDirection == SildeDirection.RIGHT) {
            mShowViews[arg0 % mShowViews.length].rightSlide();
        } else if (mDirection == SildeDirection.LEFT) {
            mShowViews[arg0 % mShowViews.length].leftSlide();
        }
        mDirection = SildeDirection.NO_SILDE;
    }
}
