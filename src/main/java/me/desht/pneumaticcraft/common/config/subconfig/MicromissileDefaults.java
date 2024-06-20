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
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.common.item.MicromissilesItem.Settings;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MicromissileDefaults extends AuxConfigJson {
    public static final MicromissileDefaults INSTANCE = new MicromissileDefaults();

    private final Map<UUID, Settings> defaults = new HashMap<>();

    private MicromissileDefaults() {
        super(true);
    }

    @Override
    protected void writeToJson(JsonObject json) {
        JsonObject sub = new JsonObject();
        defaults.forEach((id, settings) ->
                Settings.CODEC.encodeStart(JsonOps.INSTANCE, settings).ifSuccess(j -> sub.add(id.toString(), j))
        );
        json.addProperty("Description", "Stores default Micromissile settings on a per-player basis");
        json.add("defaults", sub);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        defaults.clear();
        JsonObject sub = json.getAsJsonObject("defaults");
        for (Map.Entry<String, JsonElement> entry : sub.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                Settings.CODEC.parse(JsonOps.INSTANCE, entry.getValue().getAsJsonObject())
                        .ifSuccess(settings -> defaults.put(UUID.fromString(entry.getKey()), settings));
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

    public void setDefaults(Player player, Settings entry) {
        defaults.put(player.getUUID(), entry);
    }

    @Nonnull
    public Settings getDefaults(Player player) {
        return defaults.getOrDefault(player.getUUID(), Settings.DEFAULT);
    }

}
