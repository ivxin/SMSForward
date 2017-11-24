package ivxin.smsforward.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class StringUtils {

	private static Typeface face;

	public static Typeface getTypeface(Context context) {
		if (face == null)
			face = Typeface.createFromAsset(context.getAssets(), "fonts/fzltqh.ttf");
		return face;
	}

	/**
	 * 毫秒转时间
	 * 
	 * @param millis
	 *            long
	 * @return
	 */
	public static String getDateFomated(String patten, String millis) {
		return getDateFomated(patten, Long.parseLong(millis));
	}

	/**
	 * 毫秒转时间
	 * 
	 * @param millis
	 *            long
	 * @return
	 */
	public static String getDateFomated(String patten, long millis) {
		return new SimpleDateFormat(patten, Locale.CHINESE).format(millis);
	}

	/**
	 * BASE64 加密
	 * 
	 * @param str
	 * @return
	 */
	public static String encryptBASE64(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		try {
			byte[] encode = str.getBytes("UTF-8");
			// base64 加密
			return new String(Base64.encode(encode, 0, encode.length, Base64.DEFAULT), "UTF-8");

		} catch (UnsupportedEncodingException e) {
			return str;
		}

	}

	/**
	 * BASE64 解密
	 * 
	 * @param str
	 * @return
	 */
	public static String decryptBASE64(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		// 通过http通讯后的字符中加号会被替换成空格,解码时先替换回来
		str = str.replaceAll(" ", "+");
		try {
			byte[] encode = str.getBytes("UTF-8");
			// base64 解密
			return new String(Base64.decode(encode, 0, encode.length, Base64.DEFAULT), "UTF-8");

		} catch (Exception e) {
			return str;
		}
	}

}
