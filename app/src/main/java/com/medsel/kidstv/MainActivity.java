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

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final List<Channel> channels = new ArrayList<Channel>();
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private TextView status;
    private int selectedPosition = -1;

    private static class Channel {
        final String name;
        final String url;
        Channel(String name, String url) {
            this.name = name;
            this.url = url;
        }
        @Override public String toString() { return name; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        addChannels();
        buildScreen();
    }

    private void addChannels() {
        channels.add(new Channel("أطفال ومواهب | Atfal & Mawaheb",
                "https://5aafcc5de91f1.streamlock.net/atfal1.com/atfal2/playlist.m3u8"));
        channels.add(new Channel("كوچي TV | Koogi TV",
                "https://5d658d7e9f562.streamlock.net/koogi.tv/koogi.smil/playlist.m3u8"));
        channels.add(new Channel("سات 7 أطفال | SAT-7 Kids",
                "https://svs.itworkscdn.net/sat7kidslive/sat7kids.smil/playlist_dvr.m3u8"));
        channels.add(new Channel("God Stands Kids Arabic",
                "https://online.godstands.tv:5443/WebRTCApp/streams/KidsArabic.m3u8"));
        channels.add(new Channel("رؤيا كيدز | Roya Kids",
                "https://playlist.fasttvcdn.com/pl/ptllxjd03j6g9oxxjdfapg/roya-kids/playlist.m3u8"));
        channels.add(new Channel("Roya Kids Originals",
                "https://playlist.fasttvcdn.com/pl/ptllxjd03j6g9oxxjdfapg/roya-kids-originals/playlist.m3u8"));
        channels.add(new Channel("عمو يزيد | Amou Yazid",
                "http://mkstream.servehttp.com:1940/AmouYazid/ay1/playlist.m3u8"));
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.rgb(12, 20, 38));
        root.setPadding(20, 16, 20, 16);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setPadding(6, 4, 16, 4);
        root.addView(left, new LinearLayout.LayoutParams(0, -1, 0.38f));

        TextView title = new TextView(this);
        title.setText("MEDSEL KIDS TV\nقنوات الأطفال العربية");
        title.setTextColor(Color.WHITE);
        title.setTextSize(23);
        title.setGravity(Gravity.CENTER);
        title.setPadding(6, 8, 6, 18);
        left.addView(title, new LinearLayout.LayoutParams(-1, -2));

        ListView list = new ListView(this);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setSelector(android.R.drawable.list_selector_background);
        list.setAdapter(new ArrayAdapter<Channel>(this, android.R.layout.simple_list_item_1, channels) {
            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                row.setTextColor(Color.WHITE);
                row.setTextSize(17);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(14, 16, 10, 16);
                return row;
            }
        });
        left.addView(list, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.CENTER);
        right.setBackgroundColor(Color.rgb(5, 9, 18));
        root.addView(right, new LinearLayout.LayoutParams(0, -1, 0.62f));

        playerView = new SimpleExoPlayerView(this);
        playerView.setUseController(true);
        right.addView(playerView, new LinearLayout.LayoutParams(-1, 0, 1f));

        status = new TextView(this);
        status.setText("Choisis une chaîne pour lancer son flux dans cette application.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(12, 12, 12, 12);
        right.addView(status, new LinearLayout.LayoutParams(-1, -2));

        list.setOnItemClickListener((parent, view, position, id) -> playChannel(position));
        setContentView(root);
        list.requestFocus();
    }

    private void playChannel(int position) {
        selectedPosition = position;
        Channel channel = channels.get(position);
        status.setText("Connexion à " + channel.name + "…");
        releasePlayer();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        playerView.setPlayer(player);
        player.addListener(new Player.EventListener() {
            @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (selectedPosition != position) return;
                if (playbackState == Player.STATE_BUFFERING) {
                    status.setText("Chargement : " + channel.name);
                } else if (playbackState == Player.STATE_READY) {
                    status.setText("En lecture : " + channel.name);
                } else if (playbackState == Player.STATE_ENDED) {
                    status.setText("Le flux est terminé : " + channel.name);
                }
            }

            @Override public void onPlayerError(ExoPlaybackException error) {
                if (selectedPosition == position) {
                    status.setText("Flux indisponible ou incompatible : " + channel.name
                            + "\nEssaie une autre chaîne.");
                }
            }
        });

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "MEDSEL Kids TV"));
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(channel.url));
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (player != null) {
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }

    @Override protected void onStop() {
        releasePlayer();
        super.onStop();
    }

    @Override protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
