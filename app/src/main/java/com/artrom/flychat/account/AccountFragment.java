package com.artrom.flychat.account;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.artrom.flychat.pre.RegistrationActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.artrom.flychat.MySystem.ANIM_MSEC;
import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.database;
import static com.artrom.flychat.MySystem.file_email;
import static com.artrom.flychat.MySystem.file_name;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.file_password;
import static com.artrom.flychat.MySystem.getCurrentTimeFromBase;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.notBlankSpaceEnter;
import static com.artrom.flychat.MySystem.photo_quality;
import static com.artrom.flychat.MySystem.readFile;
import static com.artrom.flychat.MySystem.storageRef;
import static com.artrom.flychat.MySystem.thisUser;

public class AccountFragment extends Fragment {

    TextView see_name, see_email, see_nick, account_title;
    Button btn_sign_out, btn_create_account, choose_photo, test;
    MaterialCardView cardUserParameters;
    CardView set_photo_card;
    LinearLayout cons_layout_flipping_card;
    ImageView set_photo;

    boolean isBack = false;
    String nick;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        findViews(root);

        prepareViews();

        setListeners();

        return root;
    }

    private void setListeners() {
        btn_create_account.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), RegistrationActivity.class);
            startActivity(intent);
        });
        btn_sign_out.setOnClickListener(v -> {

            nick = readFile(getContext(), file_nick);

            if (notBlankSpaceEnter(readFile(getContext(), file_name + nick)) && notBlankSpaceEnter(readFile(getContext(), file_email + nick)) &&
                    notBlankSpaceEnter(nick) && notBlankSpaceEnter(readFile(getContext(), file_password + nick))) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Выход");
                alert.setMessage("Вы точно хотите выйти из своего аккаунта?");
                alert.setPositiveButton("Да", (dialog, which) -> {

                    prepareLeaveAcc();

                });
                alert.setNegativeButton("Отмена", (dialog, which) -> {
                    dialog.dismiss();
                });
                alert.show();

            } else {
                prepareLeaveAcc();
            }
        });

        cardUserParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ObjectAnimator oa1 = ObjectAnimator.ofFloat(cardUserParameters, "scaleX", 1f, 0f);
                final ObjectAnimator oa2 = ObjectAnimator.ofFloat(cardUserParameters, "scaleX", 0f, 1f);

                int duration = 500;
                oa1.setDuration(duration);
                oa2.setDuration(duration);

                cons_layout_flipping_card.setClickable(false);
                Handler handler_clickability = new Handler();
                handler_clickability.postDelayed(() -> cons_layout_flipping_card.setClickable(true), (long) (1.1 * duration));

                oa1.setInterpolator(new DecelerateInterpolator());
                oa2.setInterpolator(new AccelerateDecelerateInterpolator());
                oa1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isBack) {
                            setUserParameters();
                            isBack = false;
                        } else {
                            see_nick.setText(Html.fromHtml("Никнейм: " + "<i>Скрыто</i>"));
                            see_name.setText(Html.fromHtml("Ваше имя: " + "<i>Скрыто</i>"));
                            see_email.setText(Html.fromHtml("Адрес почты: " + "<i>Скрыто</i>"));

                            isBack = true;
                        }
                        oa2.start();
                    }
                });
                oa1.start();
            }
        });

        choose_photo.setOnClickListener(v -> {
            loadFromGallery();
        });

        test.setVisibility(View.GONE);
    }

    private void loadFromGallery() {
        try {
            boolean READ_EXTERNAL_permission = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean WRITE_EXTERNAL_permission = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!READ_EXTERNAL_permission || !WRITE_EXTERNAL_permission) {

                if (!READ_EXTERNAL_permission) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);
                }
                if (!WRITE_EXTERNAL_permission) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            } else {

                MyToast(requireContext(), "Открываем...", Toast.LENGTH_SHORT);

                DatabaseReference offsetRef = database.getReference(".info/serverTimeOffset");
                offsetRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long delay_time_long = snapshot.getValue(Long.class);
                        nick = readFile(getContext(), file_nick);
                        String icon_name = getCurrentTimeFromBase(delay_time_long)[0];

                        try {
                            BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                    requireContext().openFileOutput("icon_name", MODE_PRIVATE)));
                            bnn.write(icon_name);
                            bnn.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareLeaveAcc() {
        FirebaseAuth.getInstance().signOut();

        try {
            BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                    requireActivity().openFileOutput(file_nick, MODE_PRIVATE)));
            bnn.flush();
            bnn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(requireActivity(), RegistrationActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @SuppressLint("SetTextI18n")
    private void prepareViews() {

        set_photo_card.setVisibility(View.GONE);
        choose_photo.setClickable(false);

        setUserParameters();

        if (hasConnection(requireContext())) {
            setPhoto();
        } else {
            setPreviousIcon(readFile(getContext(), file_nick));
        }

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.8f, 1f);
        alphaAnimation.setDuration(5000);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        account_title.startAnimation(alphaAnimation);
    }

    private void setPhoto() {

        AlphaAnimation alphaAnimation_up = new AlphaAnimation(0.05f, 1f);
        AlphaAnimation alphaAnimation_down = new AlphaAnimation(1f, 0.05f);
        alphaAnimation_up.setDuration(ANIM_MSEC * 2);
        alphaAnimation_down.setDuration(ANIM_MSEC * 2);

        String nick = readFile(getContext(), file_nick);

        thisUser(nick + "/icon").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    set_photo.setOnClickListener(view -> {

                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Сменить фото?");
                        //alert.setMessage("");
                        alert.setPositiveButton("Да", (dialog, which) -> {

                            loadFromGallery();
                        });
                        alert.setNegativeButton("Отмена", (dialog, which) -> {
                        });
                        alert.show();
                    });

                    String icon = snapshot.getValue(String.class);
                    MyLog("nick", String.valueOf(nick));
                    MyLog("icon", String.valueOf(icon));

                    try {
                        setIcon(nick, icon, choose_photo, set_photo, set_photo_card, alphaAnimation_up, alphaAnimation_down);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            private void setIcon(String nick, String icon, Button choose_photo, ImageView set_photo, CardView set_photo_card,
                                 AlphaAnimation alphaAnimation_up, AlphaAnimation alphaAnimation_down) {

                String previousIcon = whatPreviousIcon(nick);

                MyLog("previousIcon", previousIcon);
                MyLog("icon", icon);

                if (previousIcon == null)
                    load_set_saveIconBase(nick, icon, alphaAnimation_up, alphaAnimation_down);
                else if (previousIcon.equals(icon)) {
                    setPreviousIcon(nick);
                } else {
                    load_set_saveIconBase(nick, icon, alphaAnimation_up, alphaAnimation_down);
                }
            }

            private String whatPreviousIcon(String nick) {
                String icon = null;
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(requireContext().openFileInput(nick + "PREVIOUS_ICON")));
                    icon = br.readLine();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return icon;
            }

            private void load_set_saveIconBase(String nick, String icon, AlphaAnimation alphaAnimation_up, AlphaAnimation alphaAnimation_down) {

                StorageReference iconRef = storageRef.child("account_icon/" + nick + "/" + icon);

                iconRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    Log.wtf("f", downloadUrl.toString());
                    Thread t = new Thread(() -> {
                        try {
                            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl.toString()).openConnection();
                            connection.setRequestProperty("User-agent", "Mozilla/4.0");

                            connection.connect();
                            InputStream input = connection.getInputStream();

                            Bitmap bm = BitmapFactory.decodeStream(input);

                            ((WrapActivity) requireContext()).runOnUiThread(() -> {

                                //------------------------------------------------------------------------------------------LOAD AND SET

                                requireActivity().runOnUiThread(() -> {

                                    choose_photo.setVisibility(View.GONE);
                                    set_photo_card.setVisibility(View.VISIBLE);

                                    try {
                                        set_photo.setBackground(new BitmapDrawable(requireContext().getResources(), bm));
                                    } catch (Exception ignored) {
                                    }
                                    set_photo.startAnimation(alphaAnimation_up);

                                    /*choose_photo.startAnimation(alphaAnimation_down);
                                    Handler handler1 = new Handler();
                                    handler1.postDelayed(() -> {

                                        choose_photo.setVisibility(View.GONE);
                                        set_photo_card.setVisibility(View.VISIBLE);

                                        try {
                                            set_photo.setBackground(new BitmapDrawable(requireContext().getResources(), bm));
                                        } catch (Exception ignored) {
                                        }
                                        set_photo.startAnimation(alphaAnimation_up);
                                    }, ANIM_MSEC * 2);*/
                                });

                                //------------------------------------------------------------------------------------------SAVE

                                ByteArrayOutputStream blob = new ByteArrayOutputStream();
                                bm.compress(Bitmap.CompressFormat.PNG, 0, blob);
                                byte[] bitmapdata = blob.toByteArray();

                                try {
                                    FileOutputStream fos = requireContext().openFileOutput(nick + "PREVIOUS_ICON_ICON", MODE_PRIVATE);
                                    fos.flush();
                                    fos.write(bitmapdata);
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                MyLog("PREVIOUS_ICON", icon);
                                try {
                                    BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                            requireContext().openFileOutput(nick + "PREVIOUS_ICON", MODE_PRIVATE)));
                                    bnn.write(icon);
                                    bnn.close();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    t.start();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setPreviousIcon(String nick) {
        try {
            FileInputStream fin = requireContext().openFileInput(nick + "PREVIOUS_ICON_ICON");
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);

            ((WrapActivity) requireContext()).runOnUiThread(() -> {

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                requireActivity().runOnUiThread(() -> {

                    choose_photo.setVisibility(View.GONE);
                    set_photo_card.setVisibility(View.VISIBLE);

                    try {
                        set_photo.setBackground(new BitmapDrawable(requireContext().getResources(), bitmap));
                    } catch (Exception ignored) {
                    }

                            /*choose_photo.startAnimation(alphaAnimation_down);
                            Handler handler1 = new Handler();
                            handler1.postDelayed(() -> {

                                choose_photo.setVisibility(View.GONE);
                                set_photo_card.setVisibility(View.VISIBLE);

                                try {
                                    set_photo.setBackground(new BitmapDrawable(requireContext().getResources(), bitmap));
                                } catch (Exception ignored) {
                                }
                                set_photo.startAnimation(alphaAnimation_up);
                            }, ANIM_MSEC * 2);*/
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUserParameters() {
        String nick = readFile(getContext(), file_nick);

        if (notBlankSpaceEnter(readFile(getContext(), file_name + nick)) && notBlankSpaceEnter(readFile(getContext(), file_email + nick)) &&
                notBlankSpaceEnter(readFile(getContext(), file_nick)) && notBlankSpaceEnter(readFile(getContext(), file_password + nick))) {

            Log.wtf("UserParameters", nick + " " + readFile(getContext(), file_name + nick) + " " + readFile(getContext(), file_email + nick));

            String name = readFile(getContext(), file_name + nick);

            String email = readFile(getContext(), file_email + nick);
            int index_et = email.indexOf("@");
            if (index_et != -1) {
                email = email.substring(0, index_et);
            }

            String sss = "Никнейм: " + "<b>" + nick + "</b>";
            see_nick.setText(Html.fromHtml(sss));
            String s = "Ваше имя: " + "<b>" + name + "</b>";
            see_name.setText(Html.fromHtml(s));
            String ss = "Адрес почты: " + "<b>" + email + "</b>";
            see_email.setText(Html.fromHtml(ss));
        }
    }

    private void findViews(View root) {
        see_name = root.findViewById(R.id.see_name);
        see_email = root.findViewById(R.id.see_email);
        see_nick = root.findViewById(R.id.see_nick);
        choose_photo = root.findViewById(R.id.choose_photo);
        set_photo = root.findViewById(R.id.set_photo);
        cardUserParameters = root.findViewById(R.id.cardUserParameters);
        set_photo_card = root.findViewById(R.id.set_photo_card);

        btn_sign_out = root.findViewById(R.id.btn_sign_out);
        btn_create_account = root.findViewById(R.id.btn_create_account);
        cons_layout_flipping_card = root.findViewById(R.id.cons_layout_flipping_card);

        test = root.findViewById(R.id.test);
        account_title = root.findViewById(R.id.account_title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String FILENAME = null;
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(requireContext().openFileInput("icon_name")));
            FILENAME = br_nn.readLine();
            br_nn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyLog("FILENAME", FILENAME);

        Uri filePath;
        try {
            filePath = data.getData();
        } catch (Exception e) {
            filePath = null;
            e.printStackTrace();
        }

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(requireContext(), R.style.AppCompatProgressDialogStyle);
            progressDialog.setTitle("Пожалуйста, подождите...");
            progressDialog.show();

            StorageReference ref = storageRef.child("account_icon/" + nick + "/" + FILENAME);

            File f = null;
            try {
                Bitmap bitmap_icon = decodeUri(requireContext(), filePath, 150);

                int width = bitmap_icon.getWidth();
                int height = bitmap_icon.getHeight();

                if (height >= width) {
                    bitmap_icon = Bitmap.createBitmap(bitmap_icon, 0, (height - width) / 2, width, width);
                } else {
                    bitmap_icon = Bitmap.createBitmap(bitmap_icon, (width - height) / 2, 0, height, height);
                }

                f = new File(requireContext().getCacheDir(), FILENAME);
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap_icon.compress(Bitmap.CompressFormat.JPEG, photo_quality, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                filePath = Uri.fromFile(f);
            } catch (Exception e) {
                e.printStackTrace();
                MyToast(requireContext(), "Ошибка " + e.getMessage(), Toast.LENGTH_LONG);
            }

            String finalFILENAME = FILENAME;
            File finalF = f;
            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {

                        progressDialog.dismiss();
                        MyToast(requireContext(), "Успешно загружено", Toast.LENGTH_LONG);

                        thisUser(nick + "/icon").setValue(finalFILENAME);
                        setPhoto();

                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        MyToast(requireContext(), "Ошибка " + e.getMessage(), Toast.LENGTH_LONG);
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Загружено " + (int) progress + "% из " +
                                Integer.parseInt(String.valueOf(finalF.length() / 1024)) + " Кбайт");
                    });
        }
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws Exception {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth;
        int height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }
}