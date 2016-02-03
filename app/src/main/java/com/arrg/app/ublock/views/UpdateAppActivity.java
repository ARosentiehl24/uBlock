package com.arrg.app.ublock.views;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.arrg.app.ublock.R;
import com.arrg.app.ublock.util.AppUtils;
import com.arrg.app.ublock.util.Util;
import com.arrg.app.ublock.views.uviews.UTextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UpdateAppActivity extends ATEActivity {

    @Bind(R.id.download_progress)
    ProgressBar progressBar;

    @Bind(R.id.text_progress)
    UTextView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycle", "onCreate");

        setContentView(R.layout.activity_update_app);
        ButterKnife.bind(this);

        Util.hideActionBar(this);

        Bundle bundle = getIntent().getExtras();

        new DownloadUpdate().execute(bundle.getString(getString(R.string.link_of_ublock_update)));

        getWindow().getDecorView().setBackgroundColor(Config.primaryColorDark(this, null));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LifeCycle", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LifeCycle", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LifeCycle", "onStop");
    }

    @Override
    protected void onDestroy() {
        System.gc();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    class DownloadUpdate extends AsyncTask<String, Integer, String> {

        private Integer lengthOfFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress.setText(R.string.connecting_with_server);
            progressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(String... params) {
            int count;

            try {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(getExternalFilesDir(null) + "/" + "uBlock.apk");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progress.setText(String.format(getString(R.string.download_progress), readableFileSize(((values[0] * lengthOfFile) / 100)), readableFileSize(lengthOfFile)));
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Intent startHomeScreen = new Intent(Intent.ACTION_MAIN);
            startHomeScreen.addCategory(Intent.CATEGORY_HOME);
            startActivity(startHomeScreen);

            AppUtils.installApk(UpdateAppActivity.this, Uri.fromFile(new File(getExternalFilesDir(null) + "/uBlock.apk")));
        }

        public String readableFileSize(long size) {
            if (size <= 0) {
                return "0";
            }

            final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }
}
