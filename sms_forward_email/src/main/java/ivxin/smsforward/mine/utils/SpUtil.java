package ivxin.smsforward.mine.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.List;


/**
 * 存储通用类，用来存储各种需要缓存的数据
 *
 * @author admin
 */
public class SpUtil {
    public static final String SP_NAME = "data.xml";
    public static SharedPreferences sp;

    // 单例
    private static SpUtil spUtil = null;


    public static final String USERVO = "USERVO";
    public static final String LIST = "LIST";


    public static void initSp(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }


    /**
     * 存入某个key对应的value值
     *
     * @param key
     * @param value
     */
    public static void put(String key, Object value) {
        SharedPreferences.Editor edit = sp.edit();
        if (value instanceof String) {
            edit.putString(key, (String) value);
        } else if (value instanceof Integer) {
            edit.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            edit.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            edit.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            edit.putLong(key, (Long) value);
        }
        edit.commit();
    }

    /**
     * 得到某个key对应的值
     *
     * @param key
     * @param defValue
     * @return
     */
    public static Object get(String key, Object defValue) {
        if (defValue instanceof String) {
            return sp.getString(key, (String) defValue);
        } else if (defValue instanceof Integer) {
            return sp.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Float) {
            return sp.getFloat(key, (Float) defValue);
        } else if (defValue instanceof Long) {
            return sp.getLong(key, (Long) defValue);
        }
        return null;
    }

    /**
     * 存实体类
     *
     * @param key
     * @param t
     * @param <T>
     */
    public static <T> void putObject(String key, T t) {
        if (null != t) {
            JsonUtils.saveObjectToSharePreferences(t, key);
        } else {
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(key);
            editor.commit();
        }
    }

    /**
     * 获取实体类
     */
    public static <T> T getObject(String key, Class<T> cls) {
        return JsonUtils.getObjectFromSharePreferences(
                key, cls);
    }

    /**
     * 存列表
     *
     * @param key
     * @param t
     * @param <T>
     */
    public static <T> void putList(String key, List<T> t) {
        if (null != t) {
            JsonUtils.saveListToSharePreferences(t, key);
        } else {
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(key);
            editor.commit();
        }
    }

    /**
     * 获取列表
     */
    public static <T> List<T> getList(String key, Class<T> cls) {
        Type type = new ListParameterizedType(cls);
        return JsonUtils.getListFromSharePreferences(
                key, type);

    }


}
