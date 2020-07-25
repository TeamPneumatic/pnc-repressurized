package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TwitchStreamerSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Twitch";
    }

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.DISPENSER);
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
        fontRenderer.drawString(matrixStack, "Player Name", 70, 48, 0x404040);
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 20;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        return TwitchStream.isOnline(textBoxText) ? 15 : 0;
    }

    static class TwitchStream extends Thread {
        private static final Map<String, TwitchStream> trackedTwitchers = new HashMap<>();

        private final String channel;
        private boolean keptAlive = true;

        private URL url;

        private boolean online = false;

        private TwitchStream(String name) {
            channel = name;
            start();
        }

        @Override
        public void run() {
            try {
                while (keptAlive) {
                    keptAlive = false;
                    refresh();
                    Thread.sleep(5000);
                }
                trackedTwitchers.remove(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        private void refresh() {
            try {
                url = new URL("https://api.twitch.tv/kraken/streams/" + channel);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

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

            } catch (Throwable e) {
                // e.printStackTrace();
            }
        }

        public URL getUrl() {
            return url;
        }

        static boolean isOnline(String name) {
            TwitchStream stream = trackedTwitchers.get(name);
            if (stream == null) {
                stream = new TwitchStream(name);
                trackedTwitchers.put(name, stream);
            }
            stream.keptAlive = true;
            return stream.online;
        }
    }
}
