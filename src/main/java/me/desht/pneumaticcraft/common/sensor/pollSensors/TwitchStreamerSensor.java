/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TwitchStreamerSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Twitch";
    }

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.DISPENSER.get());
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public void getAdditionalInfo(List<Component> info) {
        info.add(Component.literal("Player Name"));
    }

    @Override
    public int getPollFrequency(BlockEntity te) {
        return 20;
    }

    @Override
    public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
        return TwitchStream.isOnline(textBoxText) ? 15 : 0;
    }

    static class TwitchStream extends Thread {
        private static final Map<String, TwitchStream> trackedTwitchers = new ConcurrentHashMap<>();

        private final String channel;
        private boolean keptAlive = true;

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
                trackedTwitchers.remove(channel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        private void refresh() {
            try {
                URL url = new URL("https://api.twitch.tv/kraken/streams/" + channel);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                JsonElement json = JsonParser.parseReader(reader);
                JsonObject obj = json.getAsJsonObject();
                JsonElement streaming = obj.get("stream");
                online = !streaming.isJsonNull();
            } catch (Throwable ignored) {
            }
        }

        static boolean isOnline(String name) {
            TwitchStream stream = trackedTwitchers.computeIfAbsent(name, TwitchStream::new);
            stream.keptAlive = true;
            return stream.online;
        }
    }
}
