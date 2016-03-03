package com.ieeton.agency.view;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.ieeton.agency.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class ChartView extends RelativeLayout{
	private Context mContext;
	
	public ChartView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public ChartView(Context context, AttributeSet attrs, int arg) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public void initView(){
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.chartview, this);
    	String[] seriesname = {"宝宝的成长曲线", "全国标准曲线"};
    	
        // 1, 构造显示用渲染图
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        // 2,进行显示
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // 2.1, 构建数据
        Random r = new Random();
        for (int i = 0; i < 2; i++) {
            XYSeries series = new XYSeries(seriesname[i]);
            // 填充数据
            for (int k = 0; k < 10; k++) {
                // 填x,y值
                series.add(k, Math.abs(20 + r.nextInt() % 100));
            }
            // 需要绘制的点放进dataset中
            dataset.addSeries(series);
        }
        // 3, 对点的绘制进行设置
        XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
        // 3.1设置颜色
        xyRenderer.setColor(Color.BLUE);
        // 3.2设置点的样式
        xyRenderer.setPointStyle(PointStyle.SQUARE);
        // 3.3, 将要绘制的点添加到坐标绘制中
        renderer.addSeriesRenderer(xyRenderer);
        // 3.4,重复 1~3的步骤绘制第二个系列点
        xyRenderer = new XYSeriesRenderer();
        xyRenderer.setColor(Color.RED);
        xyRenderer.setPointStyle(PointStyle.CIRCLE);
        renderer.addSeriesRenderer(xyRenderer);
        renderer.setApplyBackgroundColor(true);//设置是否显示背景色  
        renderer.setBackgroundColor(Color.argb(100, 50, 50, 50));//设置背景色  
        renderer.setAxisTitleTextSize(16); //设置轴标题文字的大小  
        renderer.setChartTitleTextSize(20);//?设置整个图表标题文字大小  
        renderer.setLabelsTextSize(15);//设置刻度显示文字的大小(XY轴都会被设置)  
        renderer.setLegendTextSize(20);//图例文字大小  
        renderer.setMargins(new int[] { 30, 70, 0, 10 });//设置图表的外边框(上/左/下/右)
        renderer.setMarginsColor(Color.argb(0, 0xff, 0, 0));//设置图表外边框透明
        renderer.setZoomButtonsVisible(true);//是否显示放大缩小按钮  
        renderer.setPointSize(5);//设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
        renderer.setXAxisMin(0);
        renderer.setYAxisMin(0);
  
//        Intent intent = ChartFactory
//                .getLineChartIntent(this, dataset, renderer);
//        startActivity(intent);
        GraphicalView view = ChartFactory.getLineChartView(mContext, dataset, renderer);
       
        RelativeLayout ry = (RelativeLayout)findViewById(R.id.chartview);
        ry.addView(view);
	}
}
