package com.jiubai.lzenglish.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.lzenglish.R;
import com.jiubai.lzenglish.ui.activity.PurchaseActivity;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自用工具盒
 */
public class UtilBox {
    /**
     * 判断是否为电话号码
     *
     * @param number 电话号码
     * @return 是则为true
     */
    public static boolean isTelephoneNumber(String number) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
        return p.matcher(number).matches();
    }

    /**
     * 从字符串中截取连续6位数字组合 ([0-9]{" + 6 + "})截取六位数字 进行前后断言不能出现数字 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的6位动态密码
     */
    public static String getDynamicPassword(String str) {
        // 6是验证码的位数一般为六位
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{" + 6 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            dynamicPassword = m.group();
        }

        return dynamicPassword;
    }

    /**
     * 重新计算ListView的高度
     *
     * @param listView 需要计算的ListView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ;
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 重新计算ListView的高度
     *
     * @param gridView     需要计算的ListView
     * @param measureWidth 是否需要重新计算宽度
     */
    public static void setGridViewHeightBasedOnChildren(GridView gridView, boolean measureWidth) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int width = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i += 3) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, gridView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                totalHeight += listItem.getMeasuredHeight() + gridView.getVerticalSpacing();
            } else {
                totalHeight += listItem.getMeasuredHeight();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                width = listItem.getMeasuredWidth() * 3 + gridView.getHorizontalSpacing() * 2;
            } else {
                width = listItem.getMeasuredWidth() * 3;
            }
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + ((listAdapter.getCount() - 1));
        if (measureWidth) {
            params.width = width;
        }
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        gridView.setLayoutParams(params);
    }

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return px值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context 上下文
     * @param pxValue px值
     * @return dp值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 清除所有用户数据
     *
     * @param context 上下文
     */
    public static void clearAllData(Context context) {
        /*
        SharedPreferences sp = context.getSharedPreferences(
                Constants.SP_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();

        Config.COMPANY_BACKGROUND = null;
        Config.PORTRAIT = null;
        Config.CID = null;
        Config.COMPANY_NAME = null;
        Config.COOKIE = null;
        Config.COMPANY_CREATOR = null;
        Config.MID = null;
        Config.NICKNAME = null;
        Config.RANDOM = null;
        */
    }

    /**
     * 获取5位随机数
     */
    public static void getRandom() {
        int number = (int) (Math.random() * 100000);
        if (number < 90000) {
            number += 10000;
        }
        //Config.RANDOM = String.valueOf(number);
    }

    /**
     * 根据路径，解析成图片
     *
     * @param url    图片路径
     * @param width  指定的宽度
     * @param height 指定的宽度
     * @return 图片Bitmap
     */
    public static Bitmap getLocalBitmap(String url, int width, int height) {
        return getBitmapFromFile(new File(url), width, height);
    }

    @SuppressWarnings("deprecation")
    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();

                //设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength,
                        width * height);

                //这里一定要将其设置回false，因为之前我们将其设置成了true
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;

                while (true) {
                    try {
                        return BitmapFactory.decodeFile(dst.getPath(), opts);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        opts.inSampleSize += 1;
                    }
                }
            }
        }

        return null;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 设置控件参数
     *
     * @param view   控件
     * @param width  控件宽度
     * @param height 控件高度
     */
    public static void setViewParams(View view, int width, int height) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;

        view.setLayoutParams(params);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕宽度
     */
    public static int getWidthPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getHeightPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.heightPixels;
    }

    /**
     * 弹出键盘
     *
     * @param view 焦点
     */
    public static void toggleSoftInput(View view, boolean show) {
        InputMethodManager inputManager = (InputMethodManager) view.
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show && !inputManager.isActive(view)) {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.SHOW_IMPLICIT);
        } else {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean isConnected(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                // 获取网络连接管理的对象
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null&& info.isConnected()) {
                    // 判断当前网络是否已经连接
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String DATE = "MM-dd";
    public static String TIME = "HH:mm";
    public static String DATE_TIME = "MM-dd HH:mm";
    public static String DATE_TIME_SECOND = "MM-dd HH:mm:ss";
    public static String DATE_TIME_SECOND_MILL = "MM-dd HH:mm:ss SSS";

    /**
     * 时间戳转换成字符串
     *
     * @param time 时间戳
     * @param type 返回类型
     * @return 日期字符串
     */
    public static String getTimestampToString(long time, String type) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(type, Locale.CHINA);
        return sf.format(d);
    }

    /**
     * 将字符串转为时间戳
     *
     * @param time 日期字符串
     * @return 时间戳
     */
    public static long getStringToTimestamp(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.CHINA);
        Date date = new Date();
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date.getTime();
    }

    public static int getDataTodayYesterday(long time) {
        Date date = new Date(time);

        Calendar current = Calendar.getInstance();

        Calendar today = Calendar.getInstance();    //今天

        today.set(Calendar.YEAR, current.get(Calendar.YEAR));
        today.set(Calendar.MONTH, current.get(Calendar.MONTH));
        today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
        //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        Calendar yesterday = Calendar.getInstance();    //昨天

        yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
        yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
        yesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);

        current.setTime(date);

        if (current.after(today)) {
            return 1;
        } else if (current.before(today) && current.after(yesterday)) {
            return 2;
        } else {
            return -1;
        }
    }


    /**
     * Bitmap转Bytes
     *
     * @param bm bitmap
     * @return bytes
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 对字符串进行md5加密
     *
     * @param str 需要加密的字符串
     * @return 加密完成的字符串
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuilder md5StrBuff = new StringBuilder();

        for (byte aByteArray : byteArray) {
            if (Integer.toHexString(0xFF & aByteArray).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & aByteArray));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & aByteArray));
        }

        return md5StrBuff.toString();

//        // 16位加密，从第9位到25位
//        return md5StrBuff.substring(8, 24).toUpperCase();
    }

    /**
     * 获取缩略图文件名
     *
     * @return 缩略图文件名
     */
    public static String getThumbnailImageName(String url, int width, int height) {
        return url + "@" + width + "w_" + height + "h_50Q";
    }

    /**
     * 压缩一张图片至指定大小
     *
     * @param image 需要压缩的图片
     * @param size  压缩后的最大值(kb)
     * @return 压缩后的图片
     */
    public static Bitmap compressImage(Bitmap image, int size) {

        // 将bitmap放至数组中，意在bitmap的大小(与实际读取的原文件要大)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] b = baos.toByteArray();
        // 将字节换成KB
        double mid = b.length / 1024;

        // 判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > size) {
            // 获取bitmap大小 是允许最大大小的多少倍
            double i = mid / size;
            // 开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍
            // (保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小)
            image = zoomImage(image, image.getWidth() / Math.sqrt(i),
                    image.getHeight() / Math.sqrt(i));
        }

        return image;
    }

    private static Bitmap zoomImage(Bitmap image, double newWidth,
                                    double newHeight) {
        // 获取这个图片的宽和高
        float width = image.getWidth();
        float height = image.getHeight();

        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();

        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(image, 0, 0, (int) width,
                (int) height, matrix, true);
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * 创建一个可访问的临时文件
     *
     * @param fileName 文件名
     * @return 文件路径
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTempFilePath(String fileName) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) { // 文件可用使用外部存储
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/lzenglish/video/" + fileName);
            if (!f.exists()) {
                f.mkdirs();
            }
            return f;
        } else if ((new File("/mnt/sdcard2")).exists()) {  //特殊的手机，如中兴V955,存储卡为sdcard2
            String file = "/mnt/sdcard2/" + fileName;
            File f = new File(file);
            if (!f.exists()) {
                f.mkdirs();
            }
            return f;
        } else {
            return null;
        }
    }

    public static void showSnackbar(Context context, CharSequence text) {
        Snackbar.make(((Activity) context).getWindow().getDecorView(), text, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(Context context, @StringRes int resId) {
        showSnackbar(context, context.getResources().getText(resId));
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;

    }

    public static void startActivity(Activity activity, Intent intent, boolean finish) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);

        if (finish) {
            activity.finish();
        }
    }

    public static void startActivityWithTransition(Activity activity, Intent intent, boolean finish, View view, String transitionName) {
        activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity, view, transitionName).toBundle());

        if (finish) {
            activity.finish();
        }
    }

    public static void returnActivity(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
    }

    public static void restartApplication(Context context) {
        //System.exit(0);
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static double getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize / 1024.0 / 1024.0 / 1024.0;
    }

    //private static PromptDialog promptDialog;

    private static KProgressHUD kProgressHUD;

    public static void showLoading(Activity activity, boolean withAnim) {
//        promptDialog = new PromptDialog(activity);
//
//        promptDialog.showLoading("加载中", withAnim);

        kProgressHUD = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
	            .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        kProgressHUD.show();
    }

    public static void dismissLoading(boolean immediately) {
//        if (promptDialog != null) {
//            if (immediately) {
//                promptDialog.dismissImmediately();
//            } else {
//                promptDialog.dismiss();
//            }
//        }

       if (kProgressHUD != null) {
           kProgressHUD.dismiss();
       }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        if (result < dip2px(context, 10)) {
            result = dip2px(context, 25);
        }
        return result;
    }

    public static void alert(Context context, String message,
                             String positiveText, DialogInterface.OnClickListener positiveListener,
                             String negativeText, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                        .setMessage(message)
                        .setCancelable(true)
                        .setPositiveButton(positiveText, positiveListener)
                        .setNegativeButton(negativeText, negativeListener);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public static void alert(Context context, String message,
                             String positiveText, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                        .setMessage(message)
                        .setCancelable(true)
                        .setPositiveButton(positiveText, positiveListener);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public static AlertDialog colorfulDoubleAlertDialog;

    public static void colorfulAlert(final Context context, String message,
                                      String positiveText, View.OnClickListener positiveListener,
                                      String negativeText) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_colorful_double, null);

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                        .setView(contentView)
                        .setCancelable(true);

        colorfulDoubleAlertDialog = builder.create();
        colorfulDoubleAlertDialog.setCancelable(true);
        colorfulDoubleAlertDialog.setCanceledOnTouchOutside(true);
        colorfulDoubleAlertDialog.show();

        ((TextView)contentView.findViewById(R.id.textView_message)).setText(message);

        ((Button)contentView.findViewById(R.id.button_confirm)).setText(positiveText);

        ((Button)contentView.findViewById(R.id.button_cancel)).setText(negativeText);

        contentView.findViewById(R.id.button_confirm).setOnClickListener(positiveListener);

        contentView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorfulDoubleAlertDialog.dismiss();
            }
        });
    }

    public static void colorfulAlert(final Context context, String message, String text) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_colorful_single, null);

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                        .setView(contentView)
                        .setCancelable(true);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        ((TextView)contentView.findViewById(R.id.textView_message)).setText(message);

        ((Button)contentView.findViewById(R.id.button_cancel)).setText(text);

        contentView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public static void purchaseAlert(final Context context, String message, final int seasonId) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_purchase, null);

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                        .setView(contentView)
                        .setCancelable(true);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        ((TextView)contentView.findViewById(R.id.textView_message)).setText(message);

        contentView.findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Intent intent = new Intent(context, PurchaseActivity.class);
                intent.putExtra("seasonId", seasonId);
                startActivity((Activity) context, intent, false);
            }
        });

        contentView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public static PackageInfo getPackageInfo(Context context) {
        // 获取PackageManager的实例
        PackageManager packageManager = context.getPackageManager();

        try {
            // getPackageName()是当前类的包名，0代表是获取版本信息
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }
}