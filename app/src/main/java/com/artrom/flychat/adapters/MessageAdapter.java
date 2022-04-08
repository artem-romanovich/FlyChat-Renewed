package com.artrom.flychat.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.artrom.flychat.R;
import com.artrom.flychat.system.MyMessage;
import com.artrom.flychat.system.OnSwipeTouchListener;
import com.artrom.flychat.system.TouchListener;

import java.util.ArrayList;

import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.count;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<MyMessage> messages;
    private String my_nick;
    private Context context;

    int USER_VARIANT = 0;
    int INTERLOCUTOR_VARIANT = 1;
    int MULTI_VARIANT = 2;
    int EVENT_VARIANT = 3;

    public MessageAdapter(Context context, ArrayList<MyMessage> messages, String my_nick) {
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        this.my_nick = my_nick;
        this.context = context;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        MyLog("viewType", String.valueOf(viewType));
        if (viewType == USER_VARIANT) {
            view = inflater.inflate(R.layout.message_item_user, parent, false);
        } else if (viewType == INTERLOCUTOR_VARIANT) {
            view = inflater.inflate(R.layout.message_item_interlocutor, parent, false);
        } else if (viewType == EVENT_VARIANT) {
            view = inflater.inflate(R.layout.message_item_event, parent, false);
        } else
            view = inflater.inflate(R.layout.message_item_universal, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        int t;
        /*try {
            if (MESSAGES.get(position).contains("_(" + my_nick + ")_")) {
                t = USER_VARIANT;
            } else {

                if (MESSAGES.get(position).contains("_(system)_")) {
                    t = EVENT_VARIANT;
                } else {
                    t = INTERLOCUTOR_VARIANT;
                }

            }
        } catch (Exception e) {
            t = MULTI_VARIANT;
            e.printStackTrace();
        }*/

        try {
            if (messages.get(position).getName().equals(my_nick)) {
                t = USER_VARIANT;
            } else {

                if (messages.get(position).getName().equals("system")) {
                    t = EVENT_VARIANT;
                } else {
                    t = INTERLOCUTOR_VARIANT;
                }

            }
        } catch (Exception e) {
            t = MULTI_VARIANT;
            e.printStackTrace();
        }

        return t;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder viewHolder, int position) {

        final MyMessage message = messages.get(position);

        //int msgLen = message.getText().length();
        //if (msgLen <= 3) {
        //    //viewHolder.text_message.setWidth(text_message.getWidth());// + 5 * msgLen
        //    viewHolder.text_message.setGravity(Gravity.CENTER);
        //    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        //            LinearLayout.LayoutParams.WRAP_CONTENT,
        //            LinearLayout.LayoutParams.WRAP_CONTENT
        //    );
        //    params.leftMargin = SPtoPixels(context, 7.5);
        //    params.topMargin = SPtoPixels(context, 7.5);
        //    params.rightMargin = SPtoPixels(context, 7.5);
        //    params.bottomMargin = SPtoPixels(context, 5);
        //    viewHolder.text_message.setLayoutParams(params);
        //}

        viewHolder.whole_message.setOnTouchListener(new OnSwipeTouchListener(context, new TouchListener() {
            @Override
            public void onSingleTap() {
                Log.i("TAG", ">> Single tap");
            }

            @Override
            public void onDoubleTap() {
                Log.i("TAG", ">> Double tap");
            }

            @Override
            public void onLongPress() {
                Log.i("TAG", ">> Long press");
            }

            @Override
            public void onSwipeLeft() {
                Log.i("TAG", ">> Swipe left");
            }

            @Override
            public void onSwipeRight() {
                Log.i("TAG", ">> Swipe right");

            }
        }));

        MyLog(message.getName());
        MyLog(message.getText());
        MyLog(message.getTime());

        //viewHolder.name_message.setText(message.getName());

        //try {
        //viewHolder.text_message.setText(message.getText());

        //SpannableStringBuilder text = changeTextColor(message.getText());
        viewHolder.text_message.setText(message.getText());
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}

        try {
            viewHolder.time_message.setText(message.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SpannableStringBuilder changeTextColor(String string) {

        int colorRed = Color.parseColor("#F44336");
        int colorBlue = Color.parseColor("#2196F3");
        int colorBlack = Color.parseColor("#000000");

        String tagRed = "~r~";
        String tagBlue = "~b~";

        int[] COLORS = new int[]{colorRed, colorBlue};
        String[] TAGS = new String[]{tagRed, tagBlue};


        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(string);

        String tag = "~r~";
        if (count(string, tag) > 0) {
            if (count(string, tag) % 2 == 0) {

                ArrayList<SpannableString> SPANS = splitSpanToArray(new SpannableString(string), tag);
                spannableStringBuilder = new SpannableStringBuilder();

                for (int i = 0; i < SPANS.size(); i++) {
                    SpannableString stringPart = new SpannableString(SPANS.get(i));

                    MyLog("stringPart", String.valueOf(stringPart));

                    if (i % 2 != 0) {
                        MyLog("stringPart.length()", String.valueOf(stringPart.length()));
                        stringPart.setSpan(new ForegroundColorSpan(colorRed), 0, stringPart.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    spannableStringBuilder.append(stringPart);
                }

            }
        }

        return spannableStringBuilder;
    }

    private ArrayList<SpannableString> splitSpanToArray(SpannableString spannableString, String tag) {

        String string = String.valueOf(spannableString);
        ArrayList<SpannableString> SPANNABLE_STRING = new ArrayList<>();

        while (count(string, tag) > 0) {

            int index_tag = string.indexOf(tag);

            SPANNABLE_STRING.add(new SpannableString(spannableString.subSequence(0, index_tag)));
            string = string.substring(index_tag + tag.length());
            spannableString = new SpannableString(spannableString.subSequence(index_tag + tag.length(), spannableString.length()));
        }
        SPANNABLE_STRING.add(spannableString);

        MyLog("SPANNABLE_STRING", String.valueOf(SPANNABLE_STRING));

        return SPANNABLE_STRING;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView //name_message,
                time_message, text_message;
        final LinearLayout whole_message;

        ViewHolder(View view) {
            super(view);
            //name_message = view.findViewById(R.id.user_message);
            time_message = view.findViewById(R.id.time_message);
            text_message = view.findViewById(R.id.text_message);
            whole_message = view.findViewById(R.id.all_message);
        }
    }
}