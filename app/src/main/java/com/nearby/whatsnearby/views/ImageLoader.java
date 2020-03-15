package com.nearby.whatsnearby.views;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.nearby.whatsnearby.customasynctask.DownloadThumbImageTask;

public class ImageLoader {

    private String url;
    private ImageView imageView;
    private Context context;

    public ImageLoader(Context context, String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
        this.context = context;
    }

    public void loadThumbnailImage() throws Exception {
        try {
            new DownloadThumbImageTask(context, imageView)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        } catch (Exception ex) {
            throw new Exception("Something went wrong on the server.");
        }
    }
}