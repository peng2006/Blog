package com.example.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



/**
 * Created by peng on 2017/5/5.
 */

public class CalendarView extends View implements View.OnTouchListener{
    float width,height;//控件的宽与高
    float c_width,c_height;//日历的宽与高
    float monthHeight;//年月所在格子的高度,也是两个按钮的实际宽与高
    float weekHeight;//星期所在格子的高度
    float cell_width,cell_height;//小格子的宽与高
    float smcell_width,smcell_height;//选择月份小格子的宽与高
    float wpadding,hpadding;//与左右，上下的间距
    boolean isChinese;//星期是否中文
    boolean isBold;//文本是否加粗
    boolean bg1IsFill;//背景1是否填充
    boolean bg2IsFill;//背景2是否填充
    RectF c_rect = new RectF();
    RectF bg2_rect = new RectF();
    //RectF selectMonth_rect = new RectF();
    float r;//小圆圈的半径，给日期当背景的
    float distance;//按钮与两边的距离

    int state;//当前状态 0-普通日历 1-选择月份

    Paint bg1Paint = new Paint();//年月背景1的画笔
    Paint bg2Paint = new Paint();//年月背景2的画笔
    Paint monthPaint = new Paint();//年月文本的画笔
    Paint weekPaint = new Paint();//上面一排星期文本的画笔
    Paint broderPaint = new Paint();//边框线的画笔
    Paint downBgPaint = new Paint();//绘制背景的画笔
    Paint smPaint = new Paint();//月份文本的画笔
    Paint testPaint = new Paint();//测试的画笔


    int bg1Color;//年月背景1的颜色
    int bg2Color;//年月背景2的颜色
    int downBgColor;//按下时的背景颜色
    int weekColor;//星期文本颜色
    int broderColor;//边框颜色
    //int dateColor;
    int[] dateColors = new int[3];//分别为 上个月或下个月日期的颜色，当前月的颜色，当前日期的颜色

    float weekScale = 1.0f;
    float dateScale = 1.0f;


    Paint datePaint = new Paint();

    public String[] weekText = { "Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    public int[] date = new int[42];
    public int[] month = {1,2,3,4,5,6,7,8,9,10,11,12};

    List<CenterPoint> centerPointList = new ArrayList<CenterPoint>();//42个格子的中心点
    List<CenterPoint> smcenterPointList = new ArrayList<CenterPoint>();//42个格子的中心点

    private Date curDate;   // 当前日历显示的日期
    private Date today;     // 今天的日期
    private int curyear,curmonth;//当前的年月
    private Calendar calendar,todayCalendar;
    private int curStartIndex, curEndIndex; // 当前显示的日历起始的索引

    private boolean down = false;//是否按在有效区域
    private int downIndex;//按下的区域索引，0-41为日期，42为左键，43为右键

    //左右箭头
    Bitmap pre = BitmapFactory.decodeResource(getResources(), R.drawable.calendar_month_left);
    Bitmap next = BitmapFactory.decodeResource(getResources(),R.drawable.calendar_month_right);

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Calendar);
        wpadding = ta.getFloat(R.styleable.Calendar_wpadding,0f);//xml中的左右间距
        hpadding = ta.getFloat(R.styleable.Calendar_hpadding,0f);//xml中的上下间距

        bg1Color = ta.getColor(R.styleable.Calendar_monthbg1color, 0x00000000);//xml中年月背景1的颜色
        bg2Color = ta.getColor(R.styleable.Calendar_monthbg2color, 0x00000000);//xml中年月背景2的颜色
        downBgColor = ta.getColor(R.styleable.Calendar_downcolor, 0x00000000);//xml中按下有效区域时的颜色
        weekColor = ta.getColor(R.styleable.Calendar_weekcolor, 0xFF000000);//xml中星期文本颜色
        broderColor = ta.getColor(R.styleable.Calendar_bordercolor, 0x00000000);//xml中边框颜色
        dateColors[0] = ta.getColor(R.styleable.Calendar_othermonthcolor, 0xFFCCCCCC);//xml中日期文本颜色
        dateColors[1] = ta.getColor(R.styleable.Calendar_normalcolor,0xFF000000);//xml中日期文本颜色
        dateColors[2] = ta.getColor(R.styleable.Calendar_todaycolor,0xFFFF0000);//xml中日期文本颜色

        bg1Paint.setColor(bg1Color);
        bg2Paint.setColor(bg2Color);
        downBgPaint.setColor(downBgColor);
        weekPaint.setColor(weekColor);
        broderPaint.setColor(broderColor);
        datePaint.setColor(dateColors[1]);
        smPaint.setColor(dateColors[1]);

        weekScale = ta.getFloat(R.styleable.Calendar_weekscale,1.0f);//星期的缩小倍数
        weekScale = weekScale<1.0f?weekScale:1.0f;
        dateScale = ta.getFloat(R.styleable.Calendar_datescale,1.0f);//日期的缩小倍数
        dateScale = dateScale<1.0f?dateScale:1.0f;
        distance = ta.getFloat(R.styleable.Calendar_buttondistance,0f);//两个按钮与两边的距离

        isBold = ta.getBoolean(R.styleable.Calendar_isbold, false);//xml中文本是否加粗
        isChinese = ta.getBoolean(R.styleable.Calendar_ischinese,false);//xml中星期文本是否中文
        bg1IsFill = ta.getBoolean(R.styleable.Calendar_monthbg1fill,false);//xml中年月背景1是否填充
        bg2IsFill = ta.getBoolean(R.styleable.Calendar_monthbg1fill,false);//xml中年月背景2是否填充

        ta.recycle();
        init();
    }

    private void init(){
        curDate = today = new Date();//初始化日期
        todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);

        calendar = Calendar.getInstance();
        calendar.setTime(curDate);

        broderPaint.setStyle(Paint.Style.STROKE);
        broderPaint.setStrokeWidth(5);
        broderPaint.setAntiAlias(true);


        bg1Paint.setStrokeWidth(5);
        bg1Paint.setAntiAlias(true);

        bg2Paint.setStrokeWidth(5);
        bg2Paint.setAntiAlias(true);

        downBgPaint.setAntiAlias(true);

        monthPaint.setAntiAlias(true);
        monthPaint.setTextAlign(Paint.Align.CENTER);

        weekPaint.setAntiAlias(true);
        weekPaint.setTextAlign(Paint.Align.CENTER);

        datePaint.setAntiAlias(true);
        datePaint.setTextAlign(Paint.Align.CENTER);

        smPaint.setAntiAlias(true);
        smPaint.setTypeface(Typeface.DEFAULT_BOLD);
        smPaint.setTextAlign(Paint.Align.CENTER);

        testPaint.setColor(Color.BLUE);

        if(isBold){
            monthPaint.setTypeface(Typeface.DEFAULT_BOLD);
            weekPaint.setTypeface(Typeface.DEFAULT_BOLD);
            datePaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        if(bg1IsFill){
            bg1Paint.setStyle(Paint.Style.FILL);
        }else{
            bg1Paint.setStyle(Paint.Style.STROKE);
        }
        if(bg2IsFill){
            bg2Paint.setStyle(Paint.Style.FILL);
        }else{
            bg2Paint.setStyle(Paint.Style.STROKE);
        }

        if(isChinese){
            weekText = new String[]{ "星期日","星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        }else{
            weekText = new String[]{ "Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        }

        setOnTouchListener(this);


    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;



        wpadding = wpadding < width/20f? wpadding:width/20f;
        hpadding = hpadding < height/20f? hpadding:height/20f;

        c_width = width - 2*wpadding;
        c_height = height*2/5 - 2*hpadding;

        weekHeight = c_height/10f;
        monthHeight = weekHeight*3/2;
        pre = DrawTool.changeSize(pre, monthHeight, monthHeight);
        next = DrawTool.changeSize(next, monthHeight, monthHeight);


        c_rect.left = wpadding;
        c_rect.top = monthHeight + weekHeight + hpadding;
        c_rect.right = wpadding + c_width;
        c_rect.bottom = monthHeight + weekHeight + hpadding + c_height;

        //bg2   宽: width/3    高：monthHeight*4/5
        bg2_rect.left = (width - (width-2*wpadding)/3)/2;
        bg2_rect.top = (monthHeight - monthHeight*4/5)/2 + hpadding;
        bg2_rect.right = (width - (width-2*wpadding)/3)/2 + (width-2*wpadding)/3;
        bg2_rect.bottom = (monthHeight - monthHeight*4/5)/2 + monthHeight*4/5 +hpadding;

        Log.d("test", "left:" + wpadding +", top:" + (monthHeight + hpadding) +
                ", right:" + (width - wpadding) + ", bottom:" + (height - hpadding));

        cell_width = (c_width)/7f;
        cell_height = (c_height)/6f;

        smcell_width = (c_width)/4f;
        smcell_height = (c_height)/3f;

        distance = Math.min(distance, (width - 2 * wpadding) / 5);

        //小圆圈的半径
        r = Math.min(cell_width,cell_height)*4/5*1/2;

        smPaint.setTextSize(smcell_height * 1/2);
        monthPaint.setTextSize(cell_height * 2/3);
        weekPaint.setTextSize(cell_height / 2 * weekScale);
        datePaint.setTextSize(cell_height / 2 * dateScale);

        /*
            获取所有小格子的中心点
            1.这个中心点可以用来辅助绘制文本
            2.这个中心点可以辅助绘制圆
         */
        centerPointList.clear();
        for(int i=0;i<date.length;i++){
            centerPointList.add(new CenterPoint(wpadding + cell_width/2 + (int)(cell_width*(i%7)),monthHeight + weekHeight + hpadding + cell_height/2 + (cell_height*(i/7))));
        }

        smcenterPointList.clear();
        for(int i=0;i<month.length;i++){
            smcenterPointList.add(new CenterPoint(wpadding + smcell_width/2 + (int)(smcell_width*(i%4)),monthHeight + weekHeight + hpadding + smcell_height/2 + (smcell_height*(i/4))));
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(2*hpadding + monthHeight + weekHeight + c_height),
                MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateDate();
        if(state == 0){
            drawBg(canvas);
            drawDownBg(canvas);
            drawMonth_Year(canvas);
            drawWeek(canvas);
            drawCell(canvas);
            drawDate(canvas);
        }else{
            drawBg(canvas);
            drawMonth_Year(canvas);
            drawSelectMonth(canvas);
        }



    }


    //绘制选择月份界面
    private void drawSelectMonth(Canvas canvas){
        canvas.drawRoundRect(c_rect, 15, 15, broderPaint);
        //画横线

        for(int i=1;i<3;i++){
            canvas.drawLine(wpadding,monthHeight + weekHeight + hpadding+(i*smcell_height),wpadding+c_width,monthHeight + weekHeight + hpadding+(i*smcell_height),broderPaint);
        }
        //画竖线
        for(int i=1;i<4;i++){
            canvas.drawLine(wpadding + (i * smcell_width), monthHeight + weekHeight + hpadding, wpadding + (i * smcell_width), hpadding + monthHeight + weekHeight + c_height, broderPaint);
        }

        Paint.FontMetrics fontMetrics = smPaint.getFontMetrics();
        float baseline = (fontMetrics.bottom - fontMetrics.top)/2 -fontMetrics.bottom;
        for(int i=0;i<month.length;i++){
            canvas.drawText(month[i] + "", smcenterPointList.get(i).pointX, smcenterPointList.get(i).pointY + baseline, smPaint);
        }


    }
    //绘制年与月背景
    private void drawBg(Canvas canvas){
        canvas.drawRect(0,hpadding,width,hpadding+monthHeight,bg1Paint);
        canvas.drawRoundRect(bg2_rect,10,10,bg2Paint);
    }
    //绘制按下背景颜色，即按下后的背景
    private void drawDownBg(Canvas canvas){
        if(down){
            //这里按下左右减只是改背景色，也可以改图片，按自己需求来改
            if((downIndex>=0)&&(downIndex<42)){
                canvas.drawCircle(centerPointList.get(downIndex).pointX,centerPointList.get(downIndex).pointY,r,downBgPaint);
            }
            if(downIndex == 42){
                canvas.drawRect(wpadding + distance,hpadding + (monthHeight - pre.getHeight()) / 2,wpadding+distance+pre.getWidth(),hpadding + (monthHeight - pre.getHeight()) / 2+pre.getHeight(),downBgPaint);
            }else if(downIndex == 43){
                canvas.drawRect(width - wpadding - next.getWidth()-distance,hpadding + (monthHeight - next.getHeight()) / 2,width - wpadding-distance,hpadding + (monthHeight - next.getHeight()) / 2 +next.getHeight(),downBgPaint);
            }
        }
    }
    //绘制年与月相关控件
    private void drawMonth_Year(Canvas canvas){
        //canvas.drawRect(0,hpadding,width,monthHeight+hpadding,testPaint);
        canvas.drawBitmap(pre, wpadding + distance, hpadding + (monthHeight - pre.getHeight()) / 2, null);
        canvas.drawBitmap(next, width - wpadding - next.getWidth()-distance, hpadding + (monthHeight - next.getHeight()) / 2, null);

        Paint.FontMetrics fontMetrics = datePaint.getFontMetrics();
        float baseline = (fontMetrics.bottom - fontMetrics.top)/2 -fontMetrics.bottom;
        String year_month_text = "";
        if(isChinese){
            if (curmonth<10){
                year_month_text = curyear +"年"+"0"+curmonth+"月";
            }else{
                year_month_text = curyear +"年"+curmonth+"月";
            }

        }else{
            if (curmonth<10){
                year_month_text = curyear +"-"+"0"+curmonth;
            }else{
                year_month_text = curyear +"-"+curmonth;
            }
        }
        canvas.drawText(year_month_text, width / 2, hpadding + monthHeight / 2f + baseline, monthPaint);



    }
    //绘制星期文本
    private void drawWeek(Canvas canvas){
        float weekTextY = hpadding + monthHeight + weekHeight * 3 / 4f;
        for(int i=0;i<weekText.length;i++){
            /*
            float weekTextX = wpadding + (i* cell_width)
                    + (cell_width - weekPaint.measureText(weekText[i])) / 2f;
            */
            float weekTextX = wpadding + (i* cell_width) + cell_width/2;
            canvas.drawText(weekText[i], weekTextX, weekTextY,
                    weekPaint);
        }
    }
    //绘制框框
    private void drawCell(Canvas canvas){
        canvas.drawRoundRect(c_rect, 15, 15, broderPaint);
        //画横线
        for(int i=1;i<6;i++){
            canvas.drawLine(wpadding,monthHeight + weekHeight + hpadding+(i*cell_height),wpadding+c_width,monthHeight + weekHeight + hpadding+(i*cell_height),broderPaint);
        }
        //画竖线
        for(int i=1;i<7;i++){
            canvas.drawLine(wpadding + (i * cell_width), monthHeight + weekHeight + hpadding, wpadding + (i * cell_width), hpadding + monthHeight + weekHeight + c_height, broderPaint);
        }
    }
    //绘制日期文本
    private void drawDate(Canvas canvas){
        Paint.FontMetrics fontMetrics = datePaint.getFontMetrics();
        float baseline = (fontMetrics.bottom - fontMetrics.top)/2 -fontMetrics.bottom;
        /*
        Log.d("test", "bottom: " + fontMetrics.bottom + ", top: " + fontMetrics.top
                        + ", ascent: " + fontMetrics.ascent
                        + ", descent: " + fontMetrics.descent
                        + ", leading: " + fontMetrics.leading);
        */
        for(int i=0;i<date.length;i++){
            int color = dateColors[1];
            if (isLastMonth(i)) {
                color = dateColors[0];
            } else if (isNextMonth(i)) {
                color = dateColors[0];
            }else if(isToday(date[i])){
                color = dateColors[2];
            }
            datePaint.setColor(color);
            canvas.drawText(date[i] + "", centerPointList.get(i).pointX, centerPointList.get(i).pointY + baseline, datePaint);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(state == 0){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if((event.getX()>wpadding)&&(event.getX()<wpadding + c_width)&&//在日期区域
                            (event.getY()>monthHeight + weekHeight + hpadding)&&(event.getY()<hpadding + monthHeight + weekHeight + c_height)){
                        getSelectedDate(event.getX() - wpadding, event.getY() - (hpadding + monthHeight + weekHeight));
                    }
                    else if((event.getX()>wpadding+distance)&&(event.getX()<wpadding + distance +pre.getWidth())&&//点击pre按钮
                            (event.getY()>hpadding)&&(event.getY()<hpadding + pre.getHeight())){
                        clickLeftMonth();
                    }
                    else if((event.getX()>width-wpadding-distance-next.getWidth())&&(event.getX()<width-wpadding-distance)&&//点击pre按钮
                            (event.getY()>hpadding)&&(event.getY()<hpadding + next.getHeight())){
                        clickRightMonth();

                    }else if((event.getX()>(width - (width-2*wpadding)/3)/2)&&(event.getX()<(width - (width-2*wpadding)/3)/2 + (width-2*wpadding)/3)&&//点击pre按钮
                            (event.getY()>((monthHeight - monthHeight*4/5)/2 + hpadding))&&(event.getY()<(monthHeight - monthHeight*4/5)/2 + monthHeight*4/5 +hpadding)){
                        state = 1;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    down = false;
                    downIndex = 0;
                    invalidate();
                    break;

            }
        }else if (state == 1){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    if((event.getX()>wpadding)&&(event.getX()<wpadding + c_width)&&//在选择月份区域
                            (event.getY()>monthHeight + weekHeight + hpadding)&&(event.getY()<hpadding + monthHeight + weekHeight + c_height)){
                        getSelectedMonth(event.getX() - wpadding, event.getY() - (hpadding + monthHeight + weekHeight));

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    down = false;
                    downIndex = 0;
                    invalidate();
                    break;

            }
        }


        return true;
    }

    //获取点击到的日期"索引"
    private void getSelectedDate(float x, float y){
        int m = (int)(Math.floor((x/cell_width)) + 1);
        int n = (int)(Math.floor((y/cell_height)) + 1);
        int index = (n-1)*7 + (m-1);
        Log.d("test", "getSelectedDate: " + index);
        down = true;
        downIndex = index;
        invalidate();

    }

    //获取点击到的月份"索引"
    private void getSelectedMonth(float x, float y){
        int m = (int)(Math.floor((x/smcell_width)) + 1);
        int n = (int)(Math.floor((y/smcell_height)) + 1);
        int index = (n-1)*4 + (m-1);
        //Log.d("test", "getSelectedMonth: " + index);
        calendar.setTime(curDate);
        //Log.d("test", "curMonth: " + calendar.get(Calendar.MONTH));
        //这里用calendar.set好像傻傻的，所以用add保险点
        calendar.add(Calendar.MONTH,index-calendar.get(Calendar.MONTH));
        curDate = calendar.getTime();
        state = 0;
        invalidate();

    }

    //计算日期，填充时间
    private void calculateDate() {
        calendar.setTime(curDate);
        curyear = calendar.get(Calendar.YEAR);
        curmonth = calendar.get(Calendar.MONTH)+1;
        Log.d("test", "curyear:" + curyear + ", curmonth:" + curmonth );
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);//获取当前是星期几
        int monthStart = dayInWeek;//当月1号是星期几，辅助后面填充日期的
        if (monthStart == 1) {
            monthStart = 8;
        }
        monthStart -= 1;  //以日为开头-1，以星期一为开头-2
        curStartIndex = monthStart;
        //TODO 向date数组填充1号
        date[monthStart] = 1;
        //TODO 向date数组填充1号前面的日期
        if (monthStart > 0) {
            calendar.set(Calendar.DAY_OF_MONTH, 0);
            //TODO 这个是第一天的前面的一天，也就是上个月月尾
            int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);
            for (int i = monthStart - 1; i >= 0; i--) {
                date[i] = dayInmonth;
                dayInmonth--;
            }
            calendar.set(Calendar.DAY_OF_MONTH, date[0]);
        }

        //TODO 获取这个月的月尾
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);

        //TODO 向date数组填充1号之后到月尾的日期
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 1; i < monthDay; i++) {
            date[monthStart + i] = i + 1;
        }

        //TODO 向date数组填充月尾后的日期
        curEndIndex = monthStart + monthDay;
        for (int i = monthStart + monthDay; i < 42; i++) {
            date[i] = i - (monthStart + monthDay) + 1;
        }



    }
    //是否上个月
    private boolean isLastMonth(int i) {
        if (i < curStartIndex) {
            return true;
        }
        return false;
    }

    //是否下个月
    private boolean isNextMonth(int i) {
        if (i >= curEndIndex) {
            return true;
        }
        return false;
    }

    //是否今天
    private boolean isToday(int i) {
        calendar.setTime(curDate);
        if((calendar.get(Calendar.YEAR)==todayCalendar.get(Calendar.YEAR))&&(calendar.get(Calendar.MONTH)==todayCalendar.get(Calendar.MONTH))){
            if(i == todayCalendar.get(Calendar.DAY_OF_MONTH)){
                return true;
            }else{
                return false;
            }
        }

        return false;
    }

    //TODO 上一月,计算但前页面的月份，注意curDate
    public void clickLeftMonth(){
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, -1);
        curDate = calendar.getTime();
        //Log.d("getTimetest", "y:" + (calendar.get(Calendar.MONTH)+1) + " ,d:" + calendar.get(Calendar.DAY_OF_MONTH));
        down = true;
        downIndex = 42;
        invalidate();
    }
    //TODO 下一月,计算但前页面的月份，注意curDate
    public void clickRightMonth(){
        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);
        curDate = calendar.getTime();
        //Log.d("getTimetest", "y:" + (calendar.get(Calendar.MONTH)+1) + " ,d:" + calendar.get(Calendar.DAY_OF_MONTH));
        down = true;
        downIndex = 43;
        invalidate();
    }

}
