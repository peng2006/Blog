package com.example.calendarview;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by peng on 2017/5/7.
 * 辅助绘画工具
 */
/*
    转换位图大小工具
    第一个参数为位图，第二个为想转换的宽，第三个为想转换的高
 */
public class DrawTool {
    public static Bitmap changeSize(Bitmap bm,float targetW,float targetH){
        float width = bm.getWidth();
        float height = bm.getHeight();
        float scaleW = targetW/width;
        float scaleH = targetH/height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleW,scaleH);
        Bitmap bitmap = Bitmap.createBitmap(bm,0,0,(int)width,(int)height,matrix,true);
        return bitmap;

    }
}
