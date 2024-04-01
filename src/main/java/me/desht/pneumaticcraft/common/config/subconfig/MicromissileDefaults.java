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

package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.item.MicromissilesItem.FireMode;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MicromissileDefaults extends AuxConfigJson {
    public static final MicromissileDefaults INSTANCE = new MicromissileDefaults();

    public static final Entry FALLBACK = new Entry(1/3f, 1/3f, 1/3f, new PointXY(46, 54), "", FireMode.SMART);

    private final Map<UUID, Entry> defaults = new HashMap<>();

    private MicromissileDefaults() {
        super(true);
    }

    @Override
    protected void writeToJson(JsonObject json) {
        JsonObject sub = new JsonObject();
        for (Map.Entry<UUID, Entry> entry : defaults.entrySet()) {
            sub.add(entry.getKey().toString(), entry.getValue().toJson());
        }
        json.addProperty("Description", "Stores default Micromissile settings on a per-player basis");
        json.add("defaults", sub);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        defaults.clear();
        JsonObject sub = json.getAsJsonObject("defaults");
        for (Map.Entry<String, JsonElement> entry : sub.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                defaults.put(UUID.fromString(entry.getKey()), MicromissileDefaults.Entry.fromJson(entry.getValue().getAsJsonObject()));
            } else {
                Log.warning("Invalid JSON? entry '{}' in {}", entry.getKey(), getConfigFilename());
            }
        }
    }

    @Override
    public String getConfigFilename() {
        return "MicromissileDefaults";
    }

    @Override
    public Sidedness getSidedness() {
        return Sidedness.SERVER;
    }

    public void setDefaults(Player player, Entry entry) {
        entry.playerName = player.getName().getString();
        defaults.put(player.getUUID(), entry);
    }

    @Nonnull
    public Entry getDefaults(Player player) {
        return defaults.getOrDefault(player.getUUID(), FALLBACK);
    }

    public static class Entry {
        public final float topSpeed;
        public final float turnSpeed;
        public final float damage;
        public final PointXY p;
        public final String entityFilter;
        public final FireMode fireMode;
        String playerName = "";

        public Entry(float topSpeed, float turnSpeed, float damage, PointXY p, String entityFilter, FireMode fireMode) {
            this.topSpeed = topSpeed;
            this.turnSpeed = turnSpeed;
            this.damage = damage;
            this.p = p;
            this.entityFilter = entityFilter;
            this.fireMode = fireMode;
        }

        static Entry fromJson(JsonObject value) {
            Entry entry = new Entry(
                    value.get("topSpeed").getAsFloat(),
                    value.get("turnSpeed").getAsFloat(),
                    value.get("damage").getAsFloat(),
                    new PointXY(value.get("px").getAsInt(), value.get("py").getAsInt()),
                    value.get("entityFilter").getAsString(),
                    FireMode.fromString(value.get("fireMode").getAsString())
            );
            entry.playerName = value.get("playerName").getAsString();
            return entry;
        }


        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("topSpeed", topSpeed);
            obj.addProperty("turnSpeed", turnSpeed);
            obj.addProperty("damage", damage);
            obj.addProperty("px", p.x());
            obj.addProperty("py", p.y());
            obj.addProperty("entityFilter", entityFilter);
            obj.addProperty("playerName", playerName);
            obj.addProperty("fireMode", fireMode.toString());
            return obj;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat(MicromissilesItem.NBT_TOP_SPEED, topSpeed);
            tag.putFloat(MicromissilesItem.NBT_TURN_SPEED, turnSpeed);
            tag.putFloat(MicromissilesItem.NBT_DAMAGE, damage);
            tag.putString(MicromissilesItem.NBT_FILTER, entityFilter);
            tag.putInt(MicromissilesItem.NBT_PX, p.x());
            tag.putInt(MicromissilesItem.NBT_PY, p.y());
            tag.putString(MicromissilesItem.NBT_FIRE_MODE, fireMode.toString());
            return tag;
        }
    }
}
