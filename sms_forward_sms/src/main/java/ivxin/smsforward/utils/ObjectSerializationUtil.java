package ivxin.smsforward.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializationUtil {
	protected static final int GET = 123;
	protected static final int SAVE = 2123;
	private File folder;
	private Handler handler;
	private OnSerializationEndListener onEndListener;

	public ObjectSerializationUtil setOnEndListener(OnSerializationEndListener onEndListener) {
		this.onEndListener = onEndListener;
		return this;
	}

	public interface OnSerializationEndListener {
		void onTheEnd(final Serializable object);
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Serializable object = (Serializable) msg.obj;
			switch (msg.what) {
			case GET:
			case SAVE:
				if (onEndListener != null) {
					onEndListener.onTheEnd(object);
				}
				break;
			default:
				break;
			}
		}
	}

	private ObjectSerializationUtil(Context context, Handler handler, OnSerializationEndListener listener) {
		if (handler == null) {
			this.handler = new MyHandler();
		} else {
			this.handler = handler;
		}
		onEndListener = listener;
		folder = new File(context.getFilesDir().getAbsolutePath() + "/temp_vos");
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	/**
	 * 序列化工具
	 * 
	 * @param context
	 * @return
	 */
	public static ObjectSerializationUtil getInstance(Context context) {
		return new ObjectSerializationUtil(context, null, null);
	}
	/**
	 * 序列化工具
	 *
	 * @param context
	 * @param listener
	 * @return
	 */
	public static ObjectSerializationUtil getInstance(Context context,@Nullable OnSerializationEndListener listener) {
		return new ObjectSerializationUtil(context, null, listener);
	}

	/**
	 * 
	 * @param context
	 * @param handler
	 * @return
	 */
	public static ObjectSerializationUtil getInstance(Context context, Handler handler) {
		return new ObjectSerializationUtil(context, handler, null);
	}

	/**
	 * 
	 * @param fileName
	 *            文件名
	 * @param object
	 *            序列化对象
	 */
	public void saveObject(final String fileName, final Serializable object) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (fileName != null && object != null) {
						FileOutputStream fout = new FileOutputStream(new File(folder, fileName));
						ObjectOutputStream out = new ObjectOutputStream(fout);
						out.writeObject(object);
						out.close();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = SAVE;
					msg.obj = object;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	/**
	 * 
	 * @param fileName
	 *            文件名
	 */
	public void getObject(final String fileName) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Serializable object = null;
				try {
					FileInputStream fin = new FileInputStream(new File(folder, fileName));
					ObjectInputStream in = new ObjectInputStream(fin);
					object = (Serializable) in.readObject();
					in.close();
				} catch (ClassNotFoundException | IOException e) {
					object = null;
				}
				if (handler != null) {
					Message msg = handler.obtainMessage();
					msg.what = GET;
					msg.obj = object;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}
}
