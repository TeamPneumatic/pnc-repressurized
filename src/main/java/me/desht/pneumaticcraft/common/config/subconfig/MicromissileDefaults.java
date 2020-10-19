package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MicromissileDefaults extends AuxConfigJson {
    public static final MicromissileDefaults INSTANCE = new MicromissileDefaults();

    private static final Map<UUID, Entry> defaults = new HashMap<>();

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
                Log.warning("Invalid JSON? entry '" + entry.getKey() + "' in " + getConfigFilename());
            }
        }
    }

    @Override
    public String getConfigFilename() {
        return "MicromissileDefaults";
    }

    public void setDefaults(PlayerEntity player, Entry record) {
        record.playerName = player.getName().getFormattedText();
        defaults.put(player.getUniqueID(), record);
    }

    public Entry getDefaults(PlayerEntity player) {
        return defaults.get(player.getUniqueID());
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
            obj.addProperty("px", p.x);
            obj.addProperty("py", p.y);
            obj.addProperty("entityFilter", entityFilter);
            obj.addProperty("playerName", playerName);
            obj.addProperty("fireMode", fireMode.toString());
            return obj;
        }

        public CompoundNBT toNBT() {
            CompoundNBT tag = new CompoundNBT();
            tag.putFloat(ItemMicromissiles.NBT_TOP_SPEED, topSpeed);
            tag.putFloat(ItemMicromissiles.NBT_TURN_SPEED, turnSpeed);
            tag.putFloat(ItemMicromissiles.NBT_DAMAGE, damage);
            tag.putString(ItemMicromissiles.NBT_FILTER, entityFilter);
            tag.putInt(ItemMicromissiles.NBT_PX, p.x);
            tag.putInt(ItemMicromissiles.NBT_PY, p.y);
            tag.putString(ItemMicromissiles.NBT_FIRE_MODE, fireMode.toString());
            return tag;
        }
    }
}
