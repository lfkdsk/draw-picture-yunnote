package com.lfk.drawapictiure.View;

/**
 * Created by liufengkai on 15/11/16.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lfk.drawapictiure.R;

/**
 * Created by Administrator on 2015/11/16.
 */
public class ZoomTextView extends TextView {
    private PopupWindow popup;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    private static final long DELAY_TIME = 100;
    private Magnifier magnifier;
    private Context context;
    private boolean isLongPressState;
    private int mLastMotionX,
            mLastMotionY;
    // 是否移动了
    private boolean isMoved;
    // 移动的阈值
    private static final int TOUCH_SLOP = 20;

    public ZoomTextView(Context context) {
        super(context);
        this.context = context;
        initMagnifier();
    }

    public ZoomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initMagnifier();
    }


    private void initMagnifier() {
        BitmapDrawable resDrawable = (BitmapDrawable) context.getResources().getDrawable(R.mipmap.ic_launcher);
        if (resDrawable != null) {
            resBitmap = resDrawable.getBitmap();
        }
        magnifier = new Magnifier(context);
        // pop在宽高的基础上多加出边框的宽高
        popup = new PopupWindow(magnifier, WIDTH + 2, HEIGHT + 10);
        popup.setAnimationStyle(android.R.style.Animation_Toast);

        dstPoint = new Point(0, 0);
    }

    Runnable showZoom = new Runnable() {
        public void run() {
            popup.showAtLocation(ZoomTextView.this,
                    Gravity.NO_GRAVITY,
                    getLeft() + dstPoint.x,
                    getTop() + dstPoint.y);
        }
    };


    private Bitmap resBitmap;
    private Point dstPoint;

    class Magnifier extends View {
        private Paint mPaint;
//        private Path path1;

        public Magnifier(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xff008000);
            mPaint.setStyle(Paint.Style.STROKE);
//            path1 = new Path();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            // draw popup
            mPaint.setAlpha(255);
            canvas.drawBitmap(resBitmap, 0, 0, mPaint);
            canvas.restore();

            //draw popup frame
            mPaint.reset();//重置
            mPaint.setColor(Color.LTGRAY);
            mPaint.setStyle(Paint.Style.STROKE);//设置空心
            mPaint.setStrokeWidth(2);
            Path path1 = new Path();
            path1.moveTo(0, 0);
            path1.lineTo(WIDTH, 0);
            path1.lineTo(WIDTH, HEIGHT);
            path1.lineTo(WIDTH / 2 + 15, HEIGHT);
            path1.lineTo(WIDTH / 2, HEIGHT + 10);
            path1.lineTo(WIDTH / 2 - 15, HEIGHT);
            path1.lineTo(0, HEIGHT);
            path1.close();//封闭
            canvas.drawPath(path1, mPaint);
        }
    }


    private Bitmap bitmap;//生成的位图
    //截图

    /**
     * @param activity
     * @param x        截图起始的横坐标
     * @param y        截图起始的纵坐标
     * @param width
     * @param height
     * @return
     */
    private Bitmap getBitmap(Activity activity, int x, int y, int width, int height) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        // 边界处理,否则会崩滴
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x + width > bitmap.getWidth()) {
//            x = x + WIDTH / 2;
//            width = bitmap.getWidth() - x;
            //保持不改变,截取图片宽高的原则
            x = bitmap.getWidth() - width;
        }
        if (y + height > bitmap.getHeight()) {
//            y = y + HEIGHT / 2;
//            height = bitmap.getHeight() - y;
            y = bitmap.getHeight() - height;
        }
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int toHeight = frame.top;
        bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private boolean calculate(int x, int y, int action) {
        dstPoint.set(x - WIDTH / 2, y - 1 * HEIGHT);
        if (y < 0) {
            // hide popup if out of bounds
            popup.dismiss();
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            removeCallbacks(showZoom);
            postDelayed(showZoom, DELAY_TIME);
        } else if (!popup.isShowing()) {
            showZoom.run();
        }
        popup.update(getLeft() + dstPoint.x, getTop() + dstPoint.y, -1, -1);
        magnifier.invalidate();
        return true;
    }


    private final int LONGPRESS = 1;
    private Handler mPressHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //长按->初次启动--->显示放大镜&提词
                case LONGPRESS:
                    isLongPressState = true;
                    Bundle data = msg.getData();
                    int X = data.getInt("X");
                    int RawX = data.getInt("RawX");
                    int Y = data.getInt("Y");
                    int RawY = data.getInt("RawY");
                    resBitmap = getBitmap((Activity) context, RawX - WIDTH / 2, RawY - HEIGHT / 2, WIDTH, HEIGHT);
                    //放大镜-初次显示
                    calculate(RawX, RawY, MotionEvent.ACTION_DOWN);
                    break;
            }
        }

    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                isMoved = false;

                Message message = mPressHandler != null ? mPressHandler.obtainMessage()
                        : new Message();
                //传对象,过去后,getRawY,不是相对的Y轴.
//                message.obj = event;
                Bundle bundle = new Bundle();
                bundle.putInt("X", (int) event.getX());
                bundle.putInt("RawX", (int) event.getRawX());
                bundle.putInt("Y", (int) event.getY());
                bundle.putInt("RawY", (int) event.getRawY());
                message.setData(bundle);
                message.what = LONGPRESS;
                mPressHandler.sendMessageDelayed(message, 500);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLongPressState)
                    if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                            || Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
                        //放大镜
                        resBitmap = getBitmap((Activity) context, (int) event.getRawX() - WIDTH / 2, (int) event.getRawY() - HEIGHT / 2, WIDTH, HEIGHT);
                        calculate((int) event.getRawX(), (int) event.getRawY(), MotionEvent.ACTION_MOVE);
                        return true;
                    }
                if (isMoved && !isLongPressState)
                    break;
                //如果移动超过阈值
                if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
                        || Math.abs(mLastMotionY - y) > TOUCH_SLOP)
                    //并且非长按状态下
                    if (!isLongPressState) {
                        // 则表示移动了
                        isMoved = true;
                        cleanLongPress();// 如果超出规定的移动范围--取消[长按事件]

                    }
                break;
            case MotionEvent.ACTION_UP:
                if (isLongPressState) {
                    //dis掉放大镜
                    removeCallbacks(showZoom);
                    //drawLayout();
                    popup.dismiss();
                    cleanLongPress();
                    break;
                }
                cleanLongPress();// 只要一抬起就释放[长按事件]
                break;
            case MotionEvent.ACTION_CANCEL:
                // 事件一取消也释放[长按事件],解决在ListView中滑动的时候长按事件的激活
                cleanLongPress();
                break;
        }
        return super.onTouchEvent(event);
    }


    private void cleanLongPress() {
        isLongPressState = false;
        mPressHandler.removeMessages(LONGPRESS);
    }

    public void dismiss() {
        if (popup.isShowing()) {
            popup.dismiss();
        }
    }
}

