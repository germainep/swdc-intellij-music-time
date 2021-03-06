package com.musictime.intellij.plugin.fs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.musictime.intellij.plugin.KeystrokeCount;
import com.musictime.intellij.plugin.SoftwareCoMusic;
import com.musictime.intellij.plugin.SoftwareCoUtils;
import com.musictime.intellij.plugin.SoftwareResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class FileManager {

    public static final Logger log = Logger.getLogger("FileManager");

    private static Timer _timer = null;

    private static KeystrokeCount lastSavedKeystrokeStats = null;

    public static void clearLastSavedKeystrokeStats() {
        lastSavedKeystrokeStats = null;
    }

    public static String readmeMdFile = "\n" +
            "MUSIC TIME\n" +
            "----------\n" +
            "\n" +
            "Music Time is an IntelliJ plugin that discovers the most productive music to listen to as you code.\n" +
            "\n" +
            "\n" +
            "FEATURES\n" +
            "--------\n" +
            "\n" +
            "-  Integrated player controls: Control your music right from the status bar of your editor.\n" +
            "\n" +
            "-  Embedded playlists: Browse and play your Spotify and iTunes playlists and songs from your editor.\n" +
            "\n" +
            "-  AI playlists: Get a personalized AI playlist generated using deep learning to help you focus.\n" +
            "\n" +
            "-  Metrics profile: Learn how the underlying metrics of music (including tempo, loudness, speechiness, energy, and valence) impact how you code.\n" +
            "\n" +
            "-  Personal top 40: See your most productive songs, artists, and genres every week in your weekly top 40.\n" +
            "\n" +
            "-  Weekly music dashboard: See your top songs, artists, and genres each week by productivity score and plays while coding.\n" +
            "\n" +
            "-  Global top 40: Discover new music from developers around the world in our Software Top 40 playlist.\n" +
            "\n" +
            "-  Slack integration: Connect Slack to share songs and playlists in channels in your workspace.\n" +
            "\n" +
            "Music Time currently supports Spotify. We will support iTunes and other players in a future release.\n" +
            "\n" +
            "\n" +
            "GETTING STARTED\n" +
            "---------------\n" +
            "\n" +
            "1. Connect your Spotify account\n" +
            "\n" +
            "    Click the Connect Spotify button in the status bar or in the playlist tree, which will prompt you to log in to your Spotify account.\n" +
            "\n" +
            "2. Control your music and playlists right from your editor\n" +
            "\n" +
            "    Click on any song in your list of playlists. Music Time will prompt you to open a Spotify player—either the desktop app or web player.\n" +
            "\n" +
            "    NOTE: Music Time requires a premium Spotify account and an internet connection to control your music on Windows and Linux. If you are on a Mac, Music Time can also control the Spotify desktop app using AppleScript as either a premium or non-premium user.\n" +
            "\n" +
            "3. Generate your personal playlist\n" +
            "\n" +
            "    Click the Generate AI Playlist button to get a personalized AI playlist generated using deep learning. Your AI Top 40 playlist is initially based on your liked songs and global developer data, but will improve as you listen to more music while you code. \n" +
            "\n" +
            "4. Try a song recommendation\n" +
            "\n" +
            "    We also recommend songs by genre and mood of music based on your listening history. Try happy, energetic, or danceable music for upbeat work or classical or jazz for slower, more complex tasks. You can add a song to a playlist using the \"+\" button.\n" +
            "\n" +
            "5. Like a song\n" +
            "\n" +
            "    Like a song from the status bar by pressing the \"♡\" button, which helps us improve your song recommendations and adds that song to your Liked Songs playlist on Spotify.\n" +
            "\n" +
            "6. Check out the Software Top 40\n" +
            "\n" +
            "    Discover new music from developers around the world in a playlist generated by our algorithms. The Software Top 40 playlist is refreshed every week.\n" +
            "\n" +
            "\n" +
            "FIND YOUR MOST PRODUCTIVE MUSIC\n" +
            "-------------------------------\n" +
            "\n" +
            "As you listen to music while you code, we calculate a productivity score by combining your coding metrics with your listening history and data from over 10,000 developers.\n" +
            "\n" +
            "Here are the different ways you can discover your most productive music.\n" +
            "\n" +
            "-  View your web analytics\n" +
            "\n" +
            "    Click on the “See web analytics” button to see your most productive songs, artists, and genres by productivity score. You can also visit app.software.com/login and use your Spotify email address to log in.\n" +
            "\n" +
            "-  Open your Music Time dashboard\n" +
            "\n" +
            "    Click the “\uD83C\uDFA7” icon in the status bar then Music Time Dashboard to generate an in-editor report of your top songs, artists, and genres by productivity score.\n" +
            "\n" +
            "-  Explore your music metrics\n" +
            "\n" +
            "    Discover how the underlying metrics of music at app.software.com/music/metrics (including tempo, loudness, speechiness, energy, and valence) impact how you code.\n" +
            "\n" +
            "-  Visualize your Code Time metrics\n" +
            "\n" +
            "    Music Time is built on our Code Time plugin (https://github.com/swdotcom/swdc-intellij). You will be able to see data—such as your keystrokes, time by file and project, and lines of code—which is used calculate to your productivity scores. Visit your feed at app.software.com to see simple visualizations of your Code Time data, such as a rolling heatmap of your top programming times by hour of the day.\n" +
            "\n" +
            "\n" +
            "SHARE YOUR TOP SONGS\n" +
            "--------------------\n" +
            "\n" +
            "Share your top songs on Facebook, Twitter, WhatsApp, and Tumblr by clicking on the share icon next to a song in the playlist tree. You can also Connect Slack to share songs with your team.\n" +
            "\n" +
            "Connecting Slack requires team member permissions or above. You will not be able to connect Slack as a single or multi-channel guest.\n" +
            "\n" +
            "\n" +
            "CONTRIBUTING AND FEEDBACK\n" +
            "-------------------------\n" +
            "\n" +
            "Enjoying Music Time? Tweet at us @softwaretop40 and follow us on Instagram @softwaretop40.\n" +
            "\n" +
            "You can open an issue on a GitHub page or contact us at support@software.com with any additional questions or feedback.";

    public static String getReadmeMdContent() {
        return readmeMdFile;
    }

    public static String getSoftwareDir(boolean autoCreate) {
        String softwareDataDir = SoftwareCoUtils.getUserHomeDir();
        if (SoftwareCoUtils.isWindows()) {
            softwareDataDir += "\\.software";
        } else {
            softwareDataDir += "/.software";
        }

        File f = new File(softwareDataDir);
        if (!f.exists()) {
            // make the directory
            f.mkdirs();
        }

        return softwareDataDir;
    }

    public static String getSoftwareSessionFile(boolean autoCreate) {
        String file = getSoftwareDir(autoCreate);
        if (SoftwareCoUtils.isWindows()) {
            file += "\\session.json";
        } else {
            file += "/session.json";
        }
        return file;
    }

    public static String getMusicDataFile(boolean autoCreate) {
        String file = getSoftwareDir(autoCreate);
        if (SoftwareCoUtils.isWindows()) {
            file += "\\musicData.json";
        } else {
            file += "/musicData.json";
        }
        return file;
    }

    public static String getSongSessionDataFile(boolean autoCreate) {
        String file = getSoftwareDir(autoCreate);
        if (SoftwareCoUtils.isWindows()) {
            file += "\\songSessionData.json";
        } else {
            file += "/songSessionData.json";
        }
        return file;
    }

    public static String getSoftwareDataStoreFile() {
        String file = getSoftwareDir(true);
        if (SoftwareCoUtils.isWindows()) {
            file += "\\data.json";
        } else {
            file += "/data.json";
        }
        return file;
    }

    public static String getMusicDashboardFile() {
        String file = getSoftwareDir(true);
        if (SoftwareCoUtils.isWindows()) {
            file += "\\musicTime.txt";
        } else {
            file += "/musicTime.txt";
        }
        return file;
    }

    public static String getReadmeFile(boolean autoCreate) {
        String file = getSoftwareDir(autoCreate);
        if (SoftwareCoUtils.isWindows()) {
            file += "\\jetbrainsMt_README.md";
        } else {
            file += "/jetbrainsMt_README.md";
        }
        return file;
    }

    public static void saveFileContent(String file, String content) {
        File f = new File(file);

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f), Charset.forName("UTF-8")));
            writer.write(content);
        } catch (IOException ex) {
            // Report
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }

    public static JsonObject getFileContentAsJson(String file) {
        JsonParser parser = new JsonParser();
        try {
            Object obj = parser.parse(new FileReader(file));
            JsonObject jsonArray = parser.parse(cleanJsonString(obj.toString())).getAsJsonObject();
            return jsonArray;
        } catch (Exception e) {
            log.warning("Code Time: Error trying to read and parse " + file + ": " + e.getMessage());
        }
        return new JsonObject();
    }

    public static String cleanJsonString(String data) {
        data = data.replace("/\r\n/g", "").replace("/\n/g", "").trim();
        return data;
    }

    public static void writeData(String file, Object o) {
        if (o == null) {
            return;
        }
        File f = new File(file);
        final String content = SoftwareCoMusic.gson.toJson(o);

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f), Charset.forName("UTF-8")));
            writer.write(content);
        } catch (IOException e) {
            log.warning("Code Time: Error writing content: " + e.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {/*ignore*/}
        }
    }

    public static JsonArray getFileContentAsJsonArray(String file) {
        JsonParser parser = new JsonParser();
        try {
            Object obj = parser.parse(new FileReader(file));
            JsonArray jsonArray = parser.parse(cleanJsonString(obj.toString())).getAsJsonArray();
            return jsonArray;
        } catch (Exception e) {
            log.warning("Code Time: Error trying to read and parse " + file + ": " + e.getMessage());
        }
        return new JsonArray();
    }

    public static JsonObject getSoftwareSessionAsJson() {
        JsonObject sessionJson = new JsonObject();
        String sessionFile = getSoftwareSessionFile(true);
        File f = new File(sessionFile);
        if (f.exists()) {
            try {
                Path p = Paths.get(sessionFile);

                byte[] encoded = Files.readAllBytes(p);
                String content = new String(encoded, Charset.forName("UTF-8"));
                if (content != null) {
                    // json parse it
                    JsonElement jsonEl = JsonParser.parseString(cleanJsonString(content));
                    sessionJson = (JsonObject) jsonEl;
                }
            } catch (Exception e) {
                //
            }
        }
        if (sessionJson == null) {
            sessionJson = new JsonObject();
        }
        return sessionJson;
    }

    public static void setNumericItem(String key, Long val) {
        JsonObject sessionJson = getSoftwareSessionAsJson();
        sessionJson.addProperty(key, val);
        writeItem(sessionJson);
    }

    public static void setBooleanItem(String key, boolean val) {
        JsonObject sessionJson = getSoftwareSessionAsJson();
        sessionJson.addProperty(key, val);
        writeItem(sessionJson);
    }

    public static void setItem(String key, String val) {
        JsonObject sessionJson = getSoftwareSessionAsJson();
        if (val != null) {
            sessionJson.addProperty(key, val);
        } else {
            sessionJson.add(key, null);
        }
        writeItem(sessionJson);
    }

    private static void writeItem(JsonObject sessionJson) {
        String content = sessionJson.toString();
        String sessionFile = getSoftwareSessionFile(true);

        saveFileContent(sessionFile, content);
    }

    public static boolean getBooleanItem(String key) {
        JsonObject sessionJson = getSoftwareSessionAsJson();
        if (sessionJson != null && sessionJson.has(key) && !sessionJson.get(key).isJsonNull()) {
            return sessionJson.get(key).getAsBoolean();
        }
        return false;
    }

    public static long getNumericItem(String key, Long defaultVal) {
        JsonObject sessionJson = getSoftwareSessionAsJson();
        if (sessionJson != null && sessionJson.has(key) && !sessionJson.get(key).isJsonNull()) {
            return sessionJson.get(key).getAsLong();
        }
        return defaultVal.longValue();
    }

    public static String getItem(String key) {
        return getItem(key, "");
    }

    public static String getItem(String key, String defaultVal) {
        JsonObject sessionJson = getSoftwareSessionAsJson();
        if (sessionJson != null && sessionJson.has(key) && !sessionJson.get(key).isJsonNull()) {
            return sessionJson.get(key).getAsString();
        }
        return defaultVal;
    }

    public static JsonArray readAsJsonArray(String data) {
        try {
            JsonArray jsonArray = SoftwareCoMusic.gson.fromJson(buildJsonReader(data), JsonArray.class);
            return jsonArray;
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonReader buildJsonReader(String data) {
        // Clean the data
        data = cleanJsonString(data);
        JsonReader reader = new JsonReader(new StringReader(data));
        reader.setLenient(true);
        return reader;
    }

    public static KeystrokeCount getLastSavedKeystrokeStats() {
        List<KeystrokeCount> list = convertPayloadsToList(getKeystrokePayloads());

        if (list != null && list.size() > 0) {
            try {
                lastSavedKeystrokeStats = Collections.max(list, new KeystrokeCount.SortByLatestStart());
            } catch (Exception e) {
                // possible malformed json, get the zero element
                lastSavedKeystrokeStats = list.get(0);
            }
        }

        return lastSavedKeystrokeStats;
    }

    private static List<KeystrokeCount> convertPayloadsToList(String payloads) {
        if (StringUtils.isNotBlank(payloads)) {
            JsonArray jsonArray = readAsJsonArray(payloads);

            if (jsonArray != null && jsonArray.size() > 0) {
                Type type = new TypeToken<List<KeystrokeCount>>() {
                }.getType();
                List<KeystrokeCount> list = new ArrayList<>();
                try {
                    list = SoftwareCoMusic.gson.fromJson(jsonArray, type);
                } catch (Exception e) {}

                return list;
            }
        }
        return new ArrayList<>();
    }

    private static String getKeystrokePayloads() {
        final String dataStoreFile = getSoftwareDataStoreFile();
        File f = new File(dataStoreFile);

        if (f.exists()) {
            // found a data file, check if there's content
            StringBuffer sb = new StringBuffer();
            try {
                FileInputStream fis = new FileInputStream(f);

                //Construct BufferedReader from InputStreamReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.length() > 0) {
                        // clean the line in case there's undefined before the json brace
                        line = cleanJsonString(line);
                        sb.append(line).append(",");
                    }
                }

                br.close();

                if (sb.length() > 0) {
                    // we have data to send
                    String payloads = sb.toString();
                    payloads = payloads.substring(0, payloads.lastIndexOf(","));

                    payloads = "[" + payloads + "]";

                    return payloads;

                } else {
                    log.info("Code Time: No offline data to send");
                }
            } catch (Exception e) {
                log.warning("Code Time: Error trying to read and send offline data, error: " + e.getMessage());
            }
        }
        return null;
    }

    public static void deleteFile(String file) {
        File f = new File(file);
        // if the file exists, delete it
        if (f.exists()) {
            f.delete();
        }
    }
}
