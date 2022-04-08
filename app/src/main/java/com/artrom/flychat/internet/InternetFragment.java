package com.artrom.flychat.internet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.artrom.flychat.BuildConfig;
import com.artrom.flychat.R;
import com.artrom.flychat.adapters.RequestsAdapter;
import com.artrom.flychat.pre.FirstActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.artrom.flychat.MySystem.Auth;
import static com.artrom.flychat.MySystem.CAPABLE;
import static com.artrom.flychat.MySystem.MyToast;
import static com.artrom.flychat.MySystem.VERSION;
import static com.artrom.flychat.MySystem.createAnimBundle;
import static com.artrom.flychat.MySystem.file_email;
import static com.artrom.flychat.MySystem.file_name;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.hasConnection;
import static com.artrom.flychat.MySystem.loadBaseContacts;
import static com.artrom.flychat.MySystem.notBlankSpaceEnter;
import static com.artrom.flychat.MySystem.readFile;
import static com.artrom.flychat.MySystem.thisUser;

public class InternetFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    ImageButton btn_find_user;
    EditText find_by_nick;
    RelativeLayout main_layout_int_fragment;
    FirebaseUser currentUser;
    RecyclerView recycle_view;
    ListView listView;
    static RecyclerView recycle_requests;

    public static RequestsAdapter requests_adapter;
    public static LinearLayout linlay_list;
    public static LinearLayout no_users;
    public static LinearLayout linlay_bar;

    LinearLayout linlay_btn;
    LinearLayout requests;
    LinearLayout no_requests;

    ArrayList<String> Requests_nick = new ArrayList<>();
    ArrayList<String> Requests_name = new ArrayList<>();
    ArrayList<String> Requests_status = new ArrayList<>();

    String user_nick;
    String user_email;
    String user_name;

    String[] AllUsersList;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        user_nick = readFile(getContext(), file_nick);

        View view = inflater.inflate(R.layout.fragment_internet, container, false);
        View rowList = getLayoutInflater().inflate(R.layout.users_row, null);
        findViews(view, rowList);

        user_name = readFile(getContext(), file_name + user_nick);
        user_email = readFile(getContext(), file_email + user_nick);

        if (!hasConnection(requireActivity())) {

            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        if (hasConnection(requireContext())) {
                            startContactsPart();
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            t.start();

            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
            alert.setTitle("Отсутствует подключение к интернету");
            alert.setMessage("Подключиться к сети?");
            alert.setPositiveButton("Да", (dialog, which) -> {
                WifiManager wifi = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                    startActivityForResult(panelIntent, 0);
                } else {
                    wifi.setWifiEnabled(true);
                }
            });
            alert.setNegativeButton("Нет", (dialog, which) -> dialog.dismiss());
            alert.show();
        } else {

            startContactsPart();
        }
        return view;
    }

    private void startContactsPart() {
        if (notBlankSpaceEnter(user_email) && notBlankSpaceEnter(user_nick) && notBlankSpaceEnter(user_name)) {

            currentUser = Auth.getCurrentUser();

            CAPABLE.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                    checkCapability(datasnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                    checkCapability(snapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                    checkCapability(snapshot);
                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                    checkCapability(snapshot);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }

                private void checkCapability(DataSnapshot datasnapshot) {
                    if (datasnapshot.exists()) {
                        try {
                            String status = datasnapshot.getValue(String.class);
                            Log.wtf("Capable", status);

                            if (status.equals("NO")) {
                                final boolean[] pass = new boolean[1];

                                DatabaseReference CheckAcc = FirebaseDatabase.getInstance().getReference("/Status/Capable/Acc/");
                                ValueEventListener acc_listener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshotacc) {
                                        if (dataSnapshotacc.exists()) {
                                            if (dataSnapshotacc.getValue(String.class).equals(user_nick)) {
                                                pass[0] = true;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                };
                                try {
                                    CheckAcc.child("acc1").addListenerForSingleValueEvent(acc_listener);
                                    CheckAcc.child("acc2").addListenerForSingleValueEvent(acc_listener);
                                    CheckAcc.child("acc3").addListenerForSingleValueEvent(acc_listener);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    if (pass[0]) {
                                        loadBaseContacts(requireContext(), user_nick, recycle_view, linlay_bar, linlay_list, no_users);
                                    } else {
                                        try {
                                            MyToast(requireActivity(), "Ведутся работы, приложение временно недоступно", Toast.LENGTH_LONG);
                                        } catch (Exception ignored) {
                                        }
                                        //requireActivity().finish();
                                    }
                                }, 3000);
                            }
                            if (status.equals("YES")) {

                                VERSION.addChildEventListener(new ChildEventListener() {
                                    @SuppressLint("ResourceType")
                                    @Override
                                    public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {

                                        int last_version;
                                        try {
                                            last_version = Integer.parseInt(datasnapshot.getValue(String.class));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            last_version = datasnapshot.getValue(Integer.class);
                                        }
                                        Log.wtf("VersionDatabase", String.valueOf(last_version));
                                        Log.wtf("VersionApp", String.valueOf(BuildConfig.VERSION_CODE));

                                        if (last_version != BuildConfig.VERSION_CODE) {

                                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                            builder.setTitle("Требуется обновление")
                                                    .setMessage("Неактуальная версия приложения. Требуется обновление на сайте")
                                                    .setNegativeButton("Обновить", (dialog, which) -> {

                                                        Intent intent = new Intent(requireContext(), FirstActivity.class);
                                                        startActivity(intent, createAnimBundle(requireContext()));

                                                        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://artem-romanovich.github.io/flychat_share/"));
                                                        //startActivity(browserIntent);
                                                    });
                                            AlertDialog dialog = builder.create();

                                            dialog.show();
                                        } else {
                                            loadBaseContacts(requireContext(), user_nick, recycle_view, linlay_bar, linlay_list, no_users);
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot snapshot) {
                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                    }
                                });
                            } else {
                                if (!status.equals("NO")) {
                                    MyToast(requireActivity(), status, Toast.LENGTH_LONG);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.wtf("Capable", "!incapable!");
                    }
                }
            });
        }
    }

    private void prepareViews() {
        linlay_bar.setVisibility(View.VISIBLE);
        linlay_list.setVisibility(View.GONE);
        no_users.setVisibility(View.GONE);

        createAllUsersList();

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.main_purple);
        swipeRefreshLayout.setOnRefreshListener(() -> {

                    loadBaseContacts(requireContext(), user_nick, recycle_view, linlay_bar, linlay_list, no_users);

                    Handler handler = new Handler();
                    handler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 700);
                }
        );

        recycle_view.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    private void createAllUsersList() {
        DatabaseReference AllUsers = FirebaseDatabase.getInstance().getReference("Users/UsersList");
        AllUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> users = (Map<String, Object>) dataSnapshot.getValue();
                if (users != null) {

                    ArrayList<String> Users = new ArrayList<>();

                    try {
                        for (Map.Entry<String, Object> entry : users.entrySet()) {

                            Map singleUser = (Map) entry.getValue();

                            String user;
                            if (singleUser.get("nick") != null) {
                                user = singleUser.get("name") + " (" + singleUser.get("nick") + ")";
                            } else {
                                user = (String) singleUser.get("name");
                            }

                            Users.add(user);
                        }
                        Collections.sort(Users);

                        AllUsersList = Users.toArray(new String[0]);

                    } catch (Exception ignored) {
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void findViews(View root, View rowList) {
        swipeRefreshLayout = root.findViewById(R.id.swiperefresh);
        main_layout_int_fragment = root.findViewById(R.id.main_layout_int_fragment);
        linlay_bar = root.findViewById(R.id.linlay_bar);
        linlay_btn = root.findViewById(R.id.linlay_btn);
        linlay_list = root.findViewById(R.id.linlay_list);
        no_users = root.findViewById(R.id.no_users);
        recycle_view = root.findViewById(R.id.recycle_view);

        listView = rowList.findViewById(R.id.listView);
        btn_find_user = rowList.findViewById(R.id.new_btn_find_user);
        find_by_nick = rowList.findViewById(R.id.new_find_by_nick);

        prepareViews();
    }

    public static void addContactToBase(String add_to_him, String add_him) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TAG", "Number of children: " + dataSnapshot.getChildrenCount());
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String item = (String) ds.getValue();
                    String key = ds.getKey();
                    Log.d(key, item);
                }

                thisUser(add_to_him + "/Contacts/")
                        .push()
                        .setValue(add_him);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        thisUser(add_to_him + "/Contacts/").addListenerForSingleValueEvent(valueEventListener);
    }
}
