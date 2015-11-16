package com.lfk.drawapictiure.bluetoothc.PaintView;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lfk.drawapictiure.Info.PathNode;
import com.lfk.drawapictiure.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class PaintView extends View {
    // drawing board
    private Bitmap mBitmap;
    // if you set a picture in you will use it
    private Bitmap mBitmapInit;
	private int mBitmapBackGround = R.drawable.whitbackground;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mEraserPaint;
    private Paint mPaint;
    // width of screen
    private int width;
    // height of screen
    private int height;
    private Context context;
    // pass judgement on paint/eraser
    private boolean IsPaint = true;
    // drawing x,y
    private float mX, mY;
	// picture's x,y
	private float picX, picY;
    // judge your fingers' tremble
    private static final float TOUCH_TOLERANCE = 10;
    // judge long pressed
    private static final long TOUCH_LONG_PRESSED = 500;
    private boolean IsRecordPath = true;
    // private PathNode pathNode;
    private boolean mIsLongPressed;
	private boolean IsShowing = false;
	private boolean IsFirstOpen = true;
	private boolean AddPicTure = false;
	public static boolean IsEditting = false;
    private long Touch_Down_Time;
    private long Touch_Up_Time;
    private OnPathListener listener;
	private static final int CHOOSEPATH = 0;
	private static final int INDIVIDE = 1;
	private static final int GETCONTENT = 2;
	private boolean ReDoOrUnDoFlag = true;
	private PathNode pathNode;
	private ArrayList<PathNode.Node> ReDoNodes = new ArrayList<>();

	public PaintView(Context context,AttributeSet attrs) {
		super(context,attrs);
		this.context = context;
        mPaint = new Paint();
        mEraserPaint = new Paint();
        Init_Paint(UserInfo.PaintColor,UserInfo.PaintWidth);
        Init_Eraser(UserInfo.EraserWidth);
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        width = manager.getDefaultDisplay().getWidth();
        height = manager.getDefaultDisplay().getHeight();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	}

    public PaintView(Context context) {
        super(context);
        this.context = context;
        mPaint = new Paint();
        mEraserPaint = new Paint();
        Init_Paint(UserInfo.PaintColor, UserInfo.PaintWidth);
        Init_Eraser(UserInfo.EraserWidth);
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        width = manager.getDefaultDisplay().getWidth();
        height = manager.getDefaultDisplay().getHeight();
		mBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    // init paint
    private void Init_Paint(int color ,int width){
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(width);
    }


    // init eraser
    private void Init_Eraser(int width){
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setColor(0xFF000000);
        mEraserPaint.setStrokeWidth(width);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.SQUARE);
        // The most important
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    // while size is changed
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
		if(IsPaint)
        	Init_Paint(UserInfo.PaintColor, UserInfo.PaintWidth);
		else
			Init_Eraser(UserInfo.EraserWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if(IsPaint)
            canvas.drawPath(mPath, mPaint);
        else
            canvas.drawPath(mPath, mEraserPaint);
    }

	private void Touch_Down(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
		 if(IsRecordPath) {
			 listener.addNodeToPath(x, y, MotionEvent.ACTION_DOWN, IsPaint);
		 }
	}


	private void Touch_Move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
			if(IsRecordPath) {
				listener.addNodeToPath(x, y, MotionEvent.ACTION_MOVE, IsPaint);
			}
		}
	}
	private void Touch_Up(Paint paint){
		mPath.lineTo(mX, mY);
		mCanvas.drawPath(mPath, paint);
		mPath.reset();
        if(IsRecordPath) {
			listener.addNodeToPath(mX, mY, MotionEvent.ACTION_UP, IsPaint);
		}
	}


	public void setColor(int color) {
		showCustomToast("已选择颜色" + colorToHexString(color));
		mPaint.setColor(color);
	}


	public void setPenWidth(int width) {
		showCustomToast("设定笔粗为：" + width);
		mPaint.setStrokeWidth(width);
	}

    public void save(){
        mCanvas.save();
    }

	public void setIsPaint(boolean isPaint) {
		IsPaint = isPaint;
	}

	public void setOnPathListener(OnPathListener listener) {
		this.listener = listener;
	}

	public void setmEraserPaint(int width){
		showCustomToast("设定橡皮粗为："+width);
		mEraserPaint.setStrokeWidth(width);
	}

	public void setIsFirstOpen(boolean isFirstOpen) {
		IsFirstOpen = isFirstOpen;
	}

	public void setIsEditting(boolean isEditting) {
		IsEditting = isEditting;
	}

	// 设定是否纪录
	public void setIsRecordPath(boolean isRecordPath,PathNode pathNode) {
		this.pathNode = pathNode;
		IsRecordPath = isRecordPath;
	}

	public void setIsRecordPath(boolean isRecordPath) {
		IsRecordPath = isRecordPath;
	}

	// 判断是否正在播放
	public boolean isShowing() {
		return IsShowing;
	}


	private static String colorToHexString(int color) {
		return String.format("#%06X", color);
	}

	// switch eraser/paint
	public void Eraser(){
		showCustomToast("切换为橡皮");
		IsPaint = false;
		Init_Eraser(UserInfo.EraserWidth);
	}

	public void Paint(){
		showCustomToast("切换为铅笔");
		IsPaint = true;
		Init_Paint(UserInfo.PaintColor, UserInfo.PaintWidth);
	}

	public Paint getmEraserPaint() {
		return mEraserPaint;
	}

	public Paint getmPaint() {
		return mPaint;
	}

	/**
	 *  @author lfk_dsk@hotmail.com
	 *  clean the canvas
	 * */
	public void clean() {
        mBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);
		try {
			Message msg = new Message();
			msg.obj = PaintView.this;
			msg.what = INDIVIDE;
			handler.sendMessage(msg);
			Thread.sleep(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  @author lfk_dsk@hotmail.com
	 *  @param uri set the uri of a picture
	 * */
	public void setmBitmap(Uri uri){
//		Log.e("图片路径", String.valueOf(uri));
		final ContentResolver cr = context.getContentResolver();
		try {
			mBitmapInit = BitmapFactory.decodeStream(cr.openInputStream(uri));
			showCustomToast("添加图片");
			drawBitmapToCanvas(mBitmapInit);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		invalidate();
	}

	// 把图片画到canvas上
	private void drawBitmapToCanvas(Bitmap bitmap){
		if(bitmap.getHeight() > height || bitmap.getWidth() > width){
			RectF rectF = new RectF(0,0,width,height);
			mCanvas.drawBitmap(bitmap, null, rectF, mBitmapPaint);
		}else {
			mCanvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
		}
	}

	/**
	 *  @author lfk_dsk@hotmail.com
	 *  @param file Pictures' file
	 * */
	public Uri BitmapToPicture(File file){
		if(!file.exists()){
			file.mkdirs();
		}
		FileOutputStream fileOutputStream;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			Date now = new Date();
			File tempfile = new File(file+"/"+formatter.format(now)+".jpg");
			fileOutputStream = new FileOutputStream(tempfile);
			Bitmap mBitmapbg = BitmapFactory.decodeResource(context.getResources(), mBitmapBackGround).
					copy(Bitmap.Config.ARGB_8888, false);
			mBitmapbg = Bitmap.createScaledBitmap(mBitmapbg,width,height,false);
			if(mBitmapInit != null){
				mBitmapbg = toConformBitmap(mBitmapbg,mBitmapInit);
				mBitmapbg = toConformBitmap(mBitmapbg,mBitmap);
			}else {
				mBitmapbg = toConformBitmap(mBitmapbg,mBitmap);
			}
			mBitmapbg.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//			showCustomToast(tempfile.getName() + "已保存");
			return Uri.fromFile(tempfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return Uri.EMPTY;
	}

	// 把图片转换成字符串
	public String PathNodeToBitmapToString(){
		Bitmap mBitmapbg = BitmapFactory.decodeResource(context.getResources(), mBitmapBackGround).
				copy(Bitmap.Config.ARGB_8888, false);
		mBitmapbg = Bitmap.createScaledBitmap(mBitmapbg,width,height,false);
		if(mBitmapInit != null){
			mBitmapbg = toConformBitmap(mBitmapbg,mBitmapInit);
			mBitmapbg = toConformBitmap(mBitmapbg,mBitmap);
		}else {
			mBitmapbg = toConformBitmap(mBitmapbg,mBitmap);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mBitmapbg.compress(Bitmap.CompressFormat.JPEG, 1, baos);
		return PicTools.bitmapToString(ThumbnailUtils.extractThumbnail(mBitmapbg,
				dip2px(320), dip2px(160), ThumbnailUtils.OPTIONS_RECYCLE_INPUT));
	}

	// 把PathNode转换成json
	public void PathNodeToJson(PathNode pathNode,File file){
		String json = "";
		if(!file.exists()){
			file.mkdirs();
		}
		json = PathNodeToJsonString(pathNode,json);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date now = new Date();
		File tempfile = new File(file+"/"+formatter.format(now)+".lfk");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(tempfile);
			byte[] bytes = json.getBytes();
			fileOutputStream.write(bytes);
			fileOutputStream.close();
			showCustomToast(tempfile.getName() + "已保存");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// PathNode转换成json的字符串，还在存储数据库里面调用过
	public String PathNodeToJsonString(PathNode pathNode,String json){
		ArrayList<PathNode.Node> arrayList = pathNode.getPathList();
		JSONArray jsonArray = new JSONArray();
		for(int i = 0;i < arrayList.size();i++){
			PathNode.Node node = arrayList.get(i);
			JSONObject object = new JSONObject();
			try {
				object.put("x",node.x);
				object.put("y",node.y);
//				object.put("PenColor",node.PenColor);
//				object.put("PenWidth",node.PenWidth);
//				object.put("EraserWidth",node.EraserWidth);
				object.put("e",node.TouchEvent);
				object.put("is",node.IsPaint);
//				object.put("time",node.time);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(object);
		}
//		try {
//			json = enCrypto(jsonArray.toString(), "lfk_dsk@hotmail.com");
//		} catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
//			e.printStackTrace();
//		}
		return jsonArray.toString();
	}

	// 保存图片的时候使用其添加背景
	private Bitmap toConformBitmap(Bitmap background, Bitmap foreground) {
		if( background == null ) {
			return null;
		}
		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
		Canvas cv = new Canvas(bitmap);
		cv.drawBitmap(background, 0, 0, null);
		cv.drawBitmap(foreground, 0, 0, null);
		cv.save(Canvas.ALL_SAVE_FLAG);//保存
		cv.restore();//存储
		return bitmap;
	}

	// 清理ReDo和UnDo的栈
	public void clearReUnList(){
		ReDoNodes.clear();
		mBitmapInit = null;
	}

	// 传入Handler
	public void JsonToPathNodeToHandle(Uri uri){
		Message message = new Message();
		message.obj = uri.getPath();
		message.what = CHOOSEPATH;
		handler.sendMessage(message);
	}

	public void ContentToPathNodeToHandle(String content){
		Message message = new Message();
		message.obj = content;
		message.what = GETCONTENT;
		handler.sendMessage(message);
	}

	/**
	 *  @author lfk_dsk@hotmail.com
	 *  @param file the file of .lfk
	 * */
	private void JsonToPathNode(String file){
		String res = "";
		try {
//			Log.e("绝对路径",file);
			FileInputStream in = new FileInputStream(file);
			ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for(int i = in.read(buffer, 0, buffer.length); i > 0 ; i = in.read(buffer, 0, buffer.length)) {
				bufferOut.write(buffer, 0, i);
			}
			res = new String(bufferOut.toByteArray(), Charset.forName("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		sendJsonToPathNode(res);
	}

	// 解析Json字符串
	private boolean sendJsonToPathNode(String res){
		ArrayList<PathNode.Node> arrayList = new ArrayList<>();
//		try {
//			res = deCrypto(res, "lfk_dsk@hotmail.com");
//		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeySpecException e) {
//			e.printStackTrace();
//		}
		Log.e("res",res);
		try {
			JSONArray jsonArray = new JSONArray(res);
			for(int i = 0;i < jsonArray.length();i++){
				JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
	 			PathNode.Node node = new PathNode().NewAnode();
				node.x = jsonObject.getInt("x");
				node.y = jsonObject.getInt("y");
				node.TouchEvent = jsonObject.getInt("e");
//				node.PenWidth = jsonObject.getInt("PenWidth");
//				node.PenColor = jsonObject.getInt("PenColor");
//				node.EraserWidth = jsonObject.getInt("EraserWidth");
				node.IsPaint = jsonObject.getBoolean("is");
//				node.time = jsonObject.getLong("time");
				arrayList.add(node);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			showCustomToast("未获取成功");
			return false;
		}
		pathNode.setPathList(arrayList);
		preview(pathNode.getPathList());
		return true;
	}

	//  单位制转换
	public int px2dip(float pxValue) {
		final float scale = this.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}


	public int dip2px(float dpValue) {
		final float scale = this.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	// 按下判断
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if(!isShowing() && IsEditting) {
			UserInfo.Editabled = true;
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Touch_Down(x, y);
					invalidate();
					break;

				case MotionEvent.ACTION_MOVE:
					Touch_Move(x, y);
					invalidate();
					break;

				case MotionEvent.ACTION_UP:
					if (IsPaint) {
						Touch_Up(mPaint);
					} else {
						Touch_Up(mEraserPaint);
					}
					invalidate();
					break;
			}
		}
		return true;
	}

	// 预览
	public void preview(ArrayList<PathNode.Node> arrayList) {
		IsRecordPath = false;
		PreviewThread previewThread = new PreviewThread(this, arrayList);
		Thread thread = new Thread(previewThread);
		thread.start();
	}

	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case INDIVIDE:
					((View) msg.obj).invalidate();
					break;
				case CHOOSEPATH:
					JsonToPathNode(msg.obj.toString());
					break;
				case GETCONTENT:
					sendJsonToPathNode(msg.obj.toString());
					break;
			}
			super.handleMessage(msg);
		}
		
	};

	// 发送Toast
	public void showCustomToast(String toast) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast_item, (ViewGroup)findViewById(R.id.toast_item));
		TextView text = (TextView) view.findViewById(R.id.toast_text);
		text.setText(toast);
		Toast tempToast = new Toast(context);
		tempToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER, 0, 0);
		tempToast.setDuration(Toast.LENGTH_SHORT);
		tempToast.setView(view);
		tempToast.show();
	}

	// 播放栈
	class PreviewThread implements Runnable{
		private long time;
		private ArrayList<PathNode.Node> nodes;
		private View view;
		public PreviewThread(View view, ArrayList<PathNode.Node> arrayList) {
			this.view = view;
			this.nodes = arrayList;
		}
		public void run() {
			time = 0;
			IsShowing = true;
//			clean();
			if(mBitmapInit != null){
				drawBitmapToCanvas(mBitmapInit);
			}
			for(int i = 0 ;i < nodes.size();i++) {
                PathNode.Node node = nodes.get(i);
				float x = dip2px(node.x);
				float y = dip2px(node.y);
////                Log.e("pre"+x,"pre"+y);
//				if(i < nodes.size() - 1) {
//					time = nodes.get(i+1).time - node.time;
//				}
				IsPaint = node.IsPaint;
				if(node.IsPaint){
//					UserInfo.PaintColor = node.PenColor;
//					UserInfo.PaintWidth = node.PenWidth;
					Init_Paint(getResources().getColor(R.color.blue),10);
				}else {
//					UserInfo.EraserWidth = node.EraserWidth;
					Init_Eraser(50);
				}
				switch (node.TouchEvent) {
					case MotionEvent.ACTION_DOWN:
						Touch_Down(x,y);
						break;
					case MotionEvent.ACTION_MOVE:
					    Touch_Move(x,y);
						break;
					case MotionEvent.ACTION_UP:
						if(node.IsPaint){
							Touch_Up(mPaint);
						}else {
							Touch_Up(mEraserPaint);
						}
						break;
				}
				Message msg = new Message();
				msg.obj = view;
				msg.what = INDIVIDE;
				handler.sendMessage(msg);
				if(!IsFirstOpen) {
					if (!ReDoOrUnDoFlag && time < 1000) {
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			ReDoOrUnDoFlag = false;
			IsShowing = false;
			IsRecordPath = true;
			IsFirstOpen = false;
		}
	}

		/**
		 * 加密（使用DES算法）
		 *
		 * @param txt
		 *            需要加密的文本
		 * @param key
		 *            密钥
		 * @return 成功加密的文本
		 * @throws InvalidKeySpecException
		 * @throws InvalidKeyException
		 * @throws NoSuchPaddingException
		 * @throws IllegalBlockSizeException
		 * @throws BadPaddingException
		 */
	private static String enCrypto(String txt, String key)
				throws InvalidKeySpecException, InvalidKeyException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		StringBuffer sb = new StringBuffer();
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
		SecretKeyFactory skeyFactory = null;
		Cipher cipher = null;
		try {
			skeyFactory = SecretKeyFactory.getInstance("DES");
			cipher = Cipher.getInstance("DES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		SecretKey deskey = skeyFactory != null ? skeyFactory.generateSecret(desKeySpec) : null;
		if (cipher != null) {
			cipher.init(Cipher.ENCRYPT_MODE, deskey);
		}
		byte[] cipherText = cipher != null ? cipher.doFinal(txt.getBytes()) : new byte[0];
		for (int n = 0; n < cipherText.length; n++) {
			String stmp = (Integer.toHexString(cipherText[n] & 0XFF));

			if (stmp.length() == 1) {
				sb.append("0" + stmp);
			} else {
				sb.append(stmp);
			}
		}
		return sb.toString().toUpperCase();
	}

		/**
		 * 解密（使用DES算法）
		 *
		 * @param txt
		 *            需要解密的文本
		 * @param key
		 *            密钥
		 * @return 成功解密的文本
		 * @throws InvalidKeyException
		 * @throws InvalidKeySpecException
		 * @throws NoSuchPaddingException
		 * @throws IllegalBlockSizeException
		 * @throws BadPaddingException
		 */
	private static String deCrypto(String txt, String key)
				throws InvalidKeyException, InvalidKeySpecException,
				NoSuchPaddingException, IllegalBlockSizeException,
				BadPaddingException {
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
		SecretKeyFactory skeyFactory = null;
		Cipher cipher = null;
		try {
			skeyFactory = SecretKeyFactory.getInstance("DES");
			cipher = Cipher.getInstance("DES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		SecretKey deskey = skeyFactory != null ? skeyFactory.generateSecret(desKeySpec) : null;
		if (cipher != null) {
			cipher.init(Cipher.DECRYPT_MODE, deskey);
		}
		byte[] btxts = new byte[txt.length() / 2];
		for (int i = 0, count = txt.length(); i < count; i += 2) {
			btxts[i / 2] = (byte) Integer.parseInt(txt.substring(i, i + 2), 16);
		}
		assert cipher != null;
		return (new String(cipher.doFinal(btxts)));
	}

	// 撤销恢复
	public void ReDoORUndo(boolean flag){
		if(!IsShowing) {
			ReDoOrUnDoFlag = true;
			try {
				if (flag) {
//					Log.e("redo","");
					ReDoNodes.add(pathNode.getTheLastNote());
					pathNode.deleteTheLastNote();
					preview(pathNode.getPathList());
					invalidate();
//					ReDoOrUnDoFlag = true;
//					if(!isShowing())
//						preview(pathNode.getPathList());
				} else {
//					Log.e("undo","");
					pathNode.addNode(ReDoNodes.get(ReDoNodes.size() - 1));
					ReDoNodes.remove(ReDoNodes.size() - 1);
					preview(pathNode.getPathList());
//					ReDoOrUnDoFlag = true;
//					if(!isShowing())
//						preview(pathNode.getPathList());
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				showCustomToast("无法操作＝－＝");
			}
		}
	}


}
