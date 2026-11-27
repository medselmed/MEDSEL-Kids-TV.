package com.medsel.kidstv;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String RELEASES_URL =
            "https://api.github.com/repos/medselmed/MEDSEL-Kids-TV./releases/latest";
    private static final String APK_NAME = "MEDSEL-Kids-TV-update.apk";

    private final List<Channel> channels = new ArrayList<Channel>();
    private VideoView videoView;
    private TextView status;
    private Button updateButton;
    private long pendingDownload = -1L;
    private BroadcastReceiver downloadReceiver;

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
        registerDownloadReceiver();
    }

    private void buildCatalog() {
        channels.add(new Channel("Spacetoon | سبيستون", "Direct officiel non confirmé sur Android 4.4"));
        channels.add(new Channel("MBC 3 | إم بي سي 3", "Direct officiel proposé par Shahid ; accès/DRM à confirmer"));
        channels.add(new Channel("Majid | ماجد", "Accès officiel via plateforme du diffuseur ; direct gratuit non confirmé"));
        channels.add(new Channel("Al Majd Kids | المجد للأطفال", "Aucun flux officiel gratuit compatible vérifié"));
        channels.add(new Channel("Baraem | براعم", "Aucun flux officiel gratuit compatible vérifié"));
        channels.add(new Channel("Jeem | جيم", "Aucun flux officiel gratuit compatible vérifié"));
        channels.add(new Channel("Toyor Al Janah | طيور الجنة", "Page de direct officielle trouvée ; compatibilité du lecteur à vérifier"));
        channels.add(new Channel("Toyor Baby | طيور بيبي", "Aucun flux officiel gratuit compatible vérifié"));
        channels.add(new Channel("Cartoon Network Arabic", "Direct linéaire gratuit non confirmé"));
        channels.add(new Channel("Ajial | أجيال", "Aucun flux officiel gratuit compatible vérifié"));
        channels.add(new Channel("Karameesh | كراميش", "Aucun flux officiel gratuit compatible vérifié"));
        channels.add(new Channel("Rotana Kids | روتانا كيدز", "Direct officiel gratuit non confirmé"));
        channels.add(new Channel("BabyBus Arabic", "Contenus officiels à la demande ; direct non confirmé"));
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
        title.setPadding(8, 8, 8, 12);
        left.addView(title, new LinearLayout.LayoutParams(-1, -2));

        updateButton = new Button(this);
        updateButton.setText("Vérifier les mises à jour");
        updateButton.setFocusable(true);
        updateButton.setOnClickListener(v -> checkForUpdate());
        left.addView(updateButton, new LinearLayout.LayoutParams(-1, -2));

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
        status.setText("Choisis une chaîne. Les flux non vérifiés sont signalés et ne sont pas présentés comme opérationnels.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        status.setPadding(16, 14, 16, 14);
        right.addView(status, new LinearLayout.LayoutParams(-1, -2));

        list.setOnItemClickListener((parent, view, position, id) -> {
            Channel channel = channels.get(position);
            videoView.stopPlayback();
            status.setText(channel.name + "\n" + channel.note
                    + "\n\nLecture non activée : aucun flux direct gratuit et compatible n'a encore été validé.");
        });
        setContentView(root);
        list.requestFocus();
    }

    private void checkForUpdate() {
        updateButton.setEnabled(false);
        updateButton.setText("Recherche...");
        new AsyncTask<Void, Void, UpdateInfo>() {
            String error;
            @Override protected UpdateInfo doInBackground(Void... ignored) {
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(RELEASES_URL).openConnection();
                    connection.setConnectTimeout(12000);
                    connection.setReadTimeout(12000);
                    connection.setRequestProperty("Accept", "application/vnd.github+json");
                    connection.setRequestProperty("User-Agent", "MEDSEL-Kids-TV");
                    int code = connection.getResponseCode();
                    if (code == 404) { error = "Aucune version publiée pour le moment."; return null; }
                    if (code != 200) { error = "Serveur de mise à jour indisponible (" + code + ")."; return null; }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) json.append(line);
                    reader.close();
                    JSONObject release = new JSONObject(json.toString());
                    String tag = release.optString("tag_name", "");
                    int remoteCode = parseVersionCode(tag);
                    JSONArray assets = release.optJSONArray("assets");
                    if (assets == null) { error = "La publication ne contient aucun APK."; return null; }
                    String apkUrl = null;
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.optJSONObject(i);
                        if (asset != null && asset.optString("name", "").toLowerCase().endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url", null);
                            if (apkUrl != null) break;
                        }
                    }
                    if (apkUrl == null || apkUrl.length() == 0) {
                        error = "Aucun fichier APK n'est attaché à la dernière version.";
                        return null;
                    }
                    return new UpdateInfo(tag, remoteCode, apkUrl);
                } catch (Exception e) {
                    error = "Échec de vérification : " + e.getMessage();
                    return null;
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
            @Override protected void onPostExecute(UpdateInfo info) {
                updateButton.setEnabled(true);
                updateButton.setText("Vérifier les mises à jour");
                if (info == null) {
                    Toast.makeText(MainActivity.this, error == null ? "Vérification impossible." : error, Toast.LENGTH_LONG).show();
                    return;
                }
                int installed = getPackageVersionCode();
                if (info.versionCode <= installed) {
                    Toast.makeText(MainActivity.this, "Application déjà à jour (" + getPackageVersionName() + ").", Toast.LENGTH_LONG).show();
                } else {
                    status.setText("Nouvelle version " + info.tag + " disponible. Téléchargement en cours...");
                    downloadUpdate(info.apkUrl);
                }
            }
        }.execute();
    }

    private int getPackageVersionCode() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; }
        catch (Exception e) { return 1; }
    }

    private String getPackageVersionName() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; }
        catch (Exception e) { return "inconnue"; }
    }

    private static int parseVersionCode(String tag) {
        String digits = tag == null ? "" : tag.replaceAll("[^0-9]", "");
        try { return Integer.parseInt(digits); } catch (Exception e) { return -1; }
    }

    private void downloadUpdate(String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("MEDSEL Kids TV");
            request.setDescription("Téléchargement de la mise à jour");
            request.setMimeType("application/vnd.android.package-archive");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, APK_NAME);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            pendingDownload = manager.enqueue(request);
            Toast.makeText(this, "Téléchargement lancé. Ouvre la notification quand il est terminé.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            status.setText("Téléchargement impossible : " + e.getMessage());
        }
    }

    private void registerDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
                if (id != pendingDownload) return;
                DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
                Cursor cursor = manager.query(query);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            Uri apk = manager.getUriForDownloadedFile(id);
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setDataAndType(apk, "application/vnd.android.package-archive");
                            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(install);
                        } else {
                            Toast.makeText(MainActivity.this, "Le téléchargement de la mise à jour a échoué.", Toast.LENGTH_LONG).show();
                        }
                    } finally { cursor.close(); }
                }
            }
        };
        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private static class UpdateInfo {
        final String tag, apkUrl;
        final int versionCode;
        UpdateInfo(String tag, int versionCode, String apkUrl) {
            this.tag = tag; this.versionCode = versionCode; this.apkUrl = apkUrl;
        }
    }

    @Override protected void onPause() {
        if (videoView != null && videoView.isPlaying()) videoView.pause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (downloadReceiver != null) {
            try { unregisterReceiver(downloadReceiver); } catch (Exception ignored) { }
        }
        super.onDestroy();
    }
}
