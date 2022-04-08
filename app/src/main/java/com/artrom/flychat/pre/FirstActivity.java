package com.artrom.flychat.pre;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.artrom.flychat.BuildConfig;
import com.artrom.flychat.MySystem;
import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.createAnimBundle;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.storageRef;

public class FirstActivity extends AppCompatActivity {

    public String my_email;
    public String my_name;
    public String my_nick;
    public String my_password;

    String file_nick = MySystem.file_nick;
    String file_password = MySystem.file_password;
    String file_name = MySystem.file_name;
    String file_email = MySystem.file_email;

    LinearLayout linlay_gone, linlay_show, linlay_location;
    Button btn_to_settings;
    VideoView videoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {
        }

        findViews();

        int version = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        String lastVersionApk = "fly_chat_" + (version + 1);
        MyLog("lastVersionApk", lastVersionApk);

        if (hasConnection(FirstActivity.this)) {

            linlay_show.setVisibility(View.VISIBLE);
            linlay_gone.setVisibility(View.GONE);

            videoPlayer = findViewById(R.id.videoPlayer);
            Uri myVideoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.newsplashscreen);
            videoPlayer.setVideoURI(myVideoUri);
            videoPlayer.requestFocus(0);
            videoPlayer.setZOrderOnTop(true);
            videoPlayer.start();

            StorageReference versionsRef = storageRef.child("system/versions/" + lastVersionApk + ".apk");
            versionsRef.getDownloadUrl()
                    .addOnSuccessListener(downloadUrl -> {
                        MyLog("versionsRef", "is");

                        File rootPath = new File(getExternalFilesDir(null).getAbsolutePath(), "versions/" + lastVersionApk);
                        if (!rootPath.exists()) {
                            rootPath.mkdirs();
                        }

                        final File apkFile = new File(rootPath, lastVersionApk + ".apk");

                        AlertDialog.Builder alert = new AlertDialog.Builder(FirstActivity.this);
                        alert.setTitle("Требуется обновление");
                        alert.setMessage("Требуется последняя версия приложения для корректной работы. Обновить сейчас?");
                        alert.setPositiveButton("Да", (dialog, which) -> {

                            downloadLatestVersion(versionsRef, apkFile);

                        });
                        alert.setNegativeButton("Позже", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
                        alert.show();
                    })
                    .addOnFailureListener(downloadUrl -> {
                        startCheckingParams();
                    });
        } else startCheckingParams();
    }

    private void startCheckingParams() {
        String show_or_not = getIntent().getStringExtra("BACKGROUND_LOCATION");
        if (show_or_not != null) {

            if (show_or_not.equals("show")) {

                linlay_gone.setVisibility(View.GONE);
                linlay_location.setVisibility(View.VISIBLE);

                btn_to_settings.setOnClickListener(v -> {

                    boolean hasForegroundLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    boolean hasBackgroundLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

                    if (hasForegroundLocationPermission) {
                        if (hasBackgroundLocationPermission) {

                            Toast.makeText(getApplicationContext(), "Доступ разрешен", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(FirstActivity.this, WrapActivity.class);
                            startActivity(intent, createAnimBundle(FirstActivity.this));
                            finish();
                        } else {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
            }
        } else {

            try {
                BufferedReader br_nn = new BufferedReader(
                        new InputStreamReader(openFileInput(file_nick)));
                my_nick = br_nn.readLine();

                BufferedReader br_n = new BufferedReader(
                        new InputStreamReader(openFileInput(file_name + my_nick)));
                my_name = br_n.readLine();

                BufferedReader br_p = new BufferedReader(
                        new InputStreamReader(openFileInput(file_password + my_nick)));
                my_password = br_p.readLine();

                BufferedReader br_e = new BufferedReader(
                        new InputStreamReader(openFileInput(file_email + my_nick)));
                my_email = br_e.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.wtf("UserParameters", my_nick + " " + my_name + " " + my_email + " " + my_password);

            Intent intent;
            if (my_email == null || my_name == null || my_password == null) {
                intent = new Intent(FirstActivity.this, RegistrationActivity.class);
            } else {
                intent = new Intent(FirstActivity.this, WrapActivity.class);
            }

            startActivity(intent, createAnimBundle(FirstActivity.this));
            finish();
        }
    }

    private void downloadLatestVersion(StorageReference versionsRef, File apkFile) {

        final ProgressDialog progressDialog = new ProgressDialog(
                FirstActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setTitle("Выполняется скачивание...");
        progressDialog.show();

        versionsRef.getFile(apkFile)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 *
                            taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Скачано " + (int) progress + "% из " +
                            Integer.parseInt(String.valueOf(apkFile.length()/1024/1024)) + " Мбайт");
                })
                .addOnSuccessListener(taskSnapshot -> {

                    progressDialog.dismiss();

                    if (apkFile.exists()) {

                        Uri apkUri;
                        Intent intent;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            apkUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", apkFile);
                            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            apkUri = Uri.fromFile(apkFile);
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        startActivity(intent);

                    } else MyLog("not e");
                });
    }

    private void findViews() {
        linlay_show = findViewById(R.id.linlay_show);
        linlay_gone = findViewById(R.id.linlay_gone);
        linlay_location = findViewById(R.id.linlay_location);
        btn_to_settings = findViewById(R.id.btn_to_settings);

        linlay_gone.setVisibility(View.VISIBLE);
        linlay_show.setVisibility(View.GONE);
        linlay_location.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}