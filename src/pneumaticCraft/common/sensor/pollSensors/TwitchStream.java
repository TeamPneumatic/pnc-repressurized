package pneumaticCraft.common.sensor.pollSensors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TwitchStream extends Thread{
    private static Map<String, TwitchStream> trackedTwitchers = new HashMap<String, TwitchStream>();

    public String channel;
    public boolean keptAlive = true;

    private URL url;
    private BufferedReader reader;

    private boolean online = false;

    private TwitchStream(String name){
        channel = name;
        start();
    }

    @Override
    public void run(){
        try {
            while(keptAlive) {
                keptAlive = false;
                refresh();
                Thread.sleep(5000);
            }
            trackedTwitchers.remove(this);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void refresh(){
        try {
            url = new URL("https://api.twitch.tv/kraken/streams/" + channel);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // while((s = reader.readLine()) != null) {
            //   Log.info(s);
            JsonElement json = new JsonParser().parse(reader);
            JsonObject obj = json.getAsJsonObject();
            //   String title = obj.get("status").getAsString();
            JsonElement streaming = obj.get("stream");
            online = !streaming.isJsonNull();
            /* JsonArray array = json.getAsJsonArray();
             for(int i = 0; i < array.size(); i++) {
                 Log.info(array.get(i).getAsString());
             }*/
            // Log.info(json.toString());
            // }

        } catch(Throwable e) {
            // e.printStackTrace();
        }
    }

    public URL getUrl(){
        return url;
    }

    public static boolean isOnline(String name){
        TwitchStream stream = trackedTwitchers.get(name);
        if(stream == null) {
            stream = new TwitchStream(name);
            trackedTwitchers.put(name, stream);
        }
        stream.keptAlive = true;
        return stream.online;
    }
}
