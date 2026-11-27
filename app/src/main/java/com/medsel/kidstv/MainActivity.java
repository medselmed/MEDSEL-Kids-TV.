package com.medsel.kidstv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final List<Channel> channels = new ArrayList<Channel>();
    private VideoView videoView;
    private TextView status;

    private static class Channel {
        final String name;
        final String note;
        Channel(String name, String note) { this.name = name; this.note = note; }
        @Override public String toString() { return name; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        buildCatalog();
        buildScreen();
    }

    private void buildCatalog() {
        channels.add(new Channel("Spacetoon | سبيستون", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("MBC 3 | إم بي سي 3", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Majid | ماجد", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Baraem | براعم", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Jeem | جيم", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Toyor Al Janah | طيور الجنة", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Toyor Baby | طيور بيبي", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Cartoon Network Arabic", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Ajial | أجيال", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Karameesh | كراميش", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("Rotana Kids | روتانا كيدز", "Flux officiel gratuit à vérifier"));
        channels.add(new Channel("BabyBus Arabic", "Flux officiel gratuit à vérifier"));
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(12, 20, 38));
        root.setPadding(24, 18, 24, 18);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setPadding(8, 4, 18, 4);
        root.addView(left, new LinearLayout.LayoutParams(0, -1, 0.42f));

        TextView title = new TextView(this);
        title.setText("MEDSEL KIDS TV  |  قنوات الأطفال");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(8, 8, 8, 18);
        left.addView(title, new LinearLayout.LayoutParams(-1, -2));

        ListView list = new ListView(this);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setSelector(android.R.drawable.list_selector_background);
        list.setAdapter(new ArrayAdapter<Channel>(this, android.R.layout.simple_list_item_1, channels) {
            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                row.setTextColor(Color.WHITE);
                row.setTextSize(19);
                row.setPadding(16, 14, 12, 14);
                return row;
            }
        });
        left.addView(list, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.CENTER);
        right.setBackgroundColor(Color.rgb(5, 9, 18));
        root.addView(right, new LinearLayout.LayoutParams(0, -1, 0.58f));

        videoView = new VideoView(this);
        right.addView(videoView, new LinearLayout.LayoutParams(-1, 0, 1f));

        status = new TextView(this);
        status.setText("Choisis une chaîne avec les flèches de la télécommande.\nLes adresses des directs gratuits doivent encore être vérifiées.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        status.setPadding(16, 14, 16, 14);
        right.addView(status, new LinearLayout.LayoutParams(-1, -2));

        list.setOnItemClickListener((parent, view, position, id) -> {
            Channel channel = channels.get(position);
            videoView.stopPlayback();
            status.setText(channel.name + "\n" + channel.note + "\n\nAucun flux vérifié n'est encore intégré.");
            Toast.makeText(MainActivity.this, "Chaîne sélectionnée", Toast.LENGTH_SHORT).show();
        });
        setContentView(root);
        list.requestFocus();
    }

    @Override protected void onPause() {
        if (videoView != null && videoView.isPlaying()) videoView.pause();
        super.onPause();
    }
}
