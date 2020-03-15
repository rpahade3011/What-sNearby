package com.nearby.whatsnearby.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.nearby.whatsnearby.beans.PlaceDetailBean;
import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.receivers.GpsStatusReceiver;
import com.nearby.whatsnearby.utilities.TypefaceUtil;
import com.nearby.whatsnearby.utilities.Utils;
import com.wizchen.topmessage.util.TopActivityManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;


/**
 * Created by rudraksh on 10/7/16.
 */
public class AppController extends Application implements Application.ActivityLifecycleCallbacks {

    /**
     * Log or request TAG
     */
    public static final String TAG = "AppController";
    private Activity mCurrentActivity;
    @SuppressLint("StaticFieldLeak")
    private static AppController mInstance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private GpsStatusReceiver gpsStatusReceiver = null;

    private PlaceDetailBean.Review[] review;
    private String[] placePhotos;

    private static final String SEND_LOG_TO_EMAIL_TEXT = "rudraksh3011@gmail.com";
    private static final String SEND_LOG_TO_EMAIL_SUBJECT = "What'sNearby log file";
    private static final String SEND_LOG_TO_EMAIL_EXTRA_TEXT = "Log file attached.";
    private static final String REPORT_CONTENT_TITLE = "Unexpected Error occurred";
    private static final String REPORT_CONTENT_TEXT = "Send error report to developer to help this get fixed. " +
            "This won't take your much time. Also includes crash reports.";
    private static String CRASH_FILE_PATH_NAME;


    /**
     * Enabling multi dex options to avoid 64K Limit.
     *
     * @param base Context - Application Context.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                handleUncaughtException(thread, ex);
            }
        });
        initTypeface();
        mInstance = this;
        mInstance.registerActivityLifecycleCallbacks(this);
        registerActivityLifecycleCallbacks(TopActivityManager.getInstance());
        registerGpsStatusReceiver();

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    /**
     * here you can handle all unexpected crashes
     *
     * @param thread Thread object
     * @param e      Throwable object
     */
    private void handleUncaughtException(final Thread thread, final Throwable e) {
        // not all Android versions will print the stack trace automatically
        if (isUiThread()) {
            writeLogToFile(thread, e);
        } else {
            //handle non UI thread throw uncaught exception
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    writeLogToFile(thread, e);
                }
            });
        }

    }

    private boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private void writeLogToFile(final Thread t, final Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        final StringBuffer report = new StringBuffer(e.toString());
        final String lineSeperator = "-------------------------------\n\n";
        final String DOUBLE_LINE_SEP = "\n\n";
        final String SINGLE_LINE_SEP = "\n";
        report.append(DOUBLE_LINE_SEP);
        report.append("--------- Stack trace ---------\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
            report.append(SINGLE_LINE_SEP);
        }
        report.append(lineSeperator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append(DOUBLE_LINE_SEP);
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append(SINGLE_LINE_SEP);
            }
        }
        // Getting the Device brand, model and sdk version details.
        report.append(lineSeperator);
        report.append("--------- Device ---------\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append(SINGLE_LINE_SEP);
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append(SINGLE_LINE_SEP);
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append(SINGLE_LINE_SEP);
        report.append("Id: ");
        report.append(Build.ID);
        report.append(SINGLE_LINE_SEP);
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        report.append("--------- Firmware ---------\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK_INT);
        report.append(SINGLE_LINE_SEP);
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append(SINGLE_LINE_SEP);
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append(SINGLE_LINE_SEP);
        report.append(lineSeperator);
        Log.e(TAG ,"Crash Report :: " + report.toString());

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = GlobalSettings.LOG_FILE_PATH;
        boolean isDirectoryCreated = Utils.getInstance().createDir(path);
        String fileNameForNougat = "crash_log_file_" + System.currentTimeMillis() + ".txt";
        CRASH_FILE_PATH_NAME = path + fileNameForNougat;

        // Extract to file.
        File file = new File(CRASH_FILE_PATH_NAME);
        FileWriter writer = null;
        try {
            // write output stream
            writer = new FileWriter(file);
            writer.write(report.toString());

            writer.close();
        } catch (IOException iOex) {
            Log.e(TAG, "Exception while writing crash file: " + iOex.getMessage());
        }

        invokeCrashedDialog(CRASH_FILE_PATH_NAME, fileNameForNougat);
    }

    public void restartApplication() {
        // make sure we die, otherwise the app will hang ...
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void invokeCrashedDialog(final String fullName, final String fileNameForNougat) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mCurrentActivity,
                        SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText(REPORT_CONTENT_TITLE);
                sweetAlertDialog.setContentText(REPORT_CONTENT_TEXT);
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        sendLogFile(fullName, fileNameForNougat);
                    }
                });
                sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        restartApplication();
                    }
                });
                sweetAlertDialog.setConfirmText("REPORT");
                sweetAlertDialog.setCancelText("Cancel");
                sweetAlertDialog.show();

                Looper.loop();
            }
        }).start();
    }

    private void sendLogFile(String fullName, String fileNameForNougat) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            File file = new File("/storage/emulated/0/WhatsNearby/" + fileNameForNougat);
            uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
            String encodedUri = uri.getEncodedPath();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SEND_LOG_TO_EMAIL_TEXT});
            intent.putExtra(Intent.EXTRA_SUBJECT, SEND_LOG_TO_EMAIL_SUBJECT);
            intent.putExtra(Intent.EXTRA_STREAM, encodedUri);
            intent.putExtra(Intent.EXTRA_TEXT, SEND_LOG_TO_EMAIL_EXTRA_TEXT); // do this so some email clients don't complain about empty body.
            mCurrentActivity.startActivity(intent);
        } else {
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SEND_LOG_TO_EMAIL_TEXT});
            intent.putExtra(Intent.EXTRA_SUBJECT, SEND_LOG_TO_EMAIL_SUBJECT);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
            intent.putExtra(Intent.EXTRA_TEXT, SEND_LOG_TO_EMAIL_EXTRA_TEXT); // do this so some email clients don't complain about empty body.
            mCurrentActivity.startActivity(intent);
        }
        new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Log.e(TAG, "RESTARTING APPLICATION...");
                restartApplication();
            }
        }.start();
    }

    private void initTypeface() {
        TypefaceUtil.overrideFont(this, "SERIF", "fonts/Hanken-Book.ttf");
        TypefaceUtil.overrideFont(this, "MONOSPACE", "fonts/Hanken-Book.ttf");
        TypefaceUtil.overrideFont(this, "DEFAULT", "fonts/Hanken-Book.ttf");
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (imageLoader == null) {
            imageLoader = new ImageLoader(this.requestQueue,
                    new LruBitmapCache());
        }
        return this.imageLoader;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mInstance = null;
        unRegisterGpsStatusReceiver();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @TargetApi(VERSION_CODES.KITKAT)
    private void registerGpsStatusReceiver() {
        IntentFilter gpsIntentFilter = new IntentFilter("android.location.PROVIDERS_CHANGED");
        // Registering broadcast receiver for GpsStatusChanges
        if (gpsStatusReceiver == null) {
            gpsStatusReceiver = new GpsStatusReceiver();
            try {
                registerReceiver(gpsStatusReceiver, gpsIntentFilter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unRegisterGpsStatusReceiver() {
        // Un registering broadcast receiver for GpsStatusChanges
        if (gpsStatusReceiver != null) {
            try {
                this.unregisterReceiver(gpsStatusReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        registerGpsStatusReceiver();
        setCurrentActivity(activity);

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        setCurrentActivity(activity);
        registerGpsStatusReceiver();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        clearReferences();
        registerGpsStatusReceiver();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Close all the instances of Activity
        clearReferences();
        unRegisterGpsStatusReceiver();
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    private void clearReferences() {
        Activity currActivity = getCurrentActivity();
        if (this.equals(currActivity))
            setCurrentActivity(null);
    }

    public PlaceDetailBean.Review[] getReview() {
        return review;
    }

    public void setReview(PlaceDetailBean.Review[] review) {
        this.review = review;
    }

    public String[] getPlacePhotos() {
        return placePhotos;
    }

    public void setPlacePhotos(String[] placePhotos) {
        this.placePhotos = placePhotos;
    }

}