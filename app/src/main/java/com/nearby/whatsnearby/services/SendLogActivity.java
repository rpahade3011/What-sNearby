package com.nearby.whatsnearby.services;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.nearby.whatsnearby.constants.GlobalSettings;
import com.nearby.whatsnearby.customalertdialog.SweetAlertDialog;
import com.nearby.whatsnearby.utilities.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by rudhraksh.pahade on 8/17/2016.
 */

public class SendLogActivity extends Activity {

    private static final String SEND_LOG_TO_EMAIL_TEXT = "rudraksh3011@gmail.com";
    private static final String SEND_LOG_TO_EMAIL_SUBJECT = "What'sNearby log file";
    private static final String SEND_LOG_TO_EMAIL_EXTRA_TEXT = "Log file attached.";
    private static final String REPORT_CONTENT_TITLE = "Unexpected Error occurred";
    private static final String REPORT_CONTENT_TEXT = "Send error report to developer to help this get fixed. " +
            "This won't take your much time. Also includes crash reports.";

    private String exceptionMessage;
    private String exceptionThreadName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        super.onCreate(savedInstanceState);

        getExceptionValues();

        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        // Put setContentView
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.ERROR_TYPE);
        sweetAlertDialog.setTitleText(REPORT_CONTENT_TITLE);
        sweetAlertDialog.setContentText(REPORT_CONTENT_TEXT);
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sendLogFile();
            }
        });
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        });
        sweetAlertDialog.setConfirmText("REPORT");
        sweetAlertDialog.setCancelText("Cancel");
        sweetAlertDialog.show();
    }

    private void getExceptionValues() {
        Intent bundleExceptionValues = getIntent();
        if (bundleExceptionValues != null) {
            exceptionMessage = bundleExceptionValues.getExtras().getString("EXCEPTION_MSG");
            exceptionThreadName = bundleExceptionValues.getExtras().getString("THREAD_NAME");
        }
    }

    private void sendLogFile() {
        // method as shown above
        String fullName = extractLogToFile();
        if (fullName == null)
            return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SEND_LOG_TO_EMAIL_TEXT});
        intent.putExtra(Intent.EXTRA_SUBJECT, SEND_LOG_TO_EMAIL_SUBJECT);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
        intent.putExtra(Intent.EXTRA_TEXT, SEND_LOG_TO_EMAIL_EXTRA_TEXT); // do this so some email clients don't complain about empty body.
        startActivity(intent);
    }

    private String extractLogToFile() {
        // method as shown above
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = GlobalSettings.LOG_FILE_PATH;
        boolean isDirectoryCreated = Utils.createDir(path);
        String fullName = path + "_log_file_" + System.currentTimeMillis();

        // Extract to file.
        File file = new File(fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time What'sNearby:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
            writer.write("Device: " + model + "\n");
            writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");
            writer.write("Root cause: " + exceptionMessage + "\n" + "at: " + exceptionThreadName);

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        } catch (IOException e) {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        return fullName;
    }

}
