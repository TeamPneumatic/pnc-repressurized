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

import com.google.gson.JsonObject;

import java.util.function.BiConsumer;

public class ArmorHUDLayout extends AuxConfigJson {
    private static final LayoutItem POWER_DEF = new LayoutItem(0.995f, 0.005f, true);
    private static final LayoutItem MESSAGE_DEF = new LayoutItem(0.005f, 0.15f, false);
    private static final LayoutItem BLOCK_TRACKER_DEF = new LayoutItem(0.995f, 0.1f, true);
    private static final LayoutItem ENTITY_TRACKER_DEF = new LayoutItem(0.995f, 0.2f, true);
    private static final LayoutItem ITEM_SEARCH_DEF = new LayoutItem(0.005f, 0.1f, false);
    private static final LayoutItem AIR_CON_DEF = new LayoutItem(0.5f, 0.005f, false);
    private static final LayoutItem JET_BOOTS_DEF = new LayoutItem(0.7f, 0.005f, true);

    // needs to be *after* the defaults above!
    public static final ArmorHUDLayout INSTANCE = new ArmorHUDLayout();

    public LayoutItem powerStat = POWER_DEF;
    public LayoutItem messageStat = MESSAGE_DEF;
    public LayoutItem blockTrackerStat = BLOCK_TRACKER_DEF;
    public LayoutItem entityTrackerStat = ENTITY_TRACKER_DEF;
    public LayoutItem itemSearchStat = ITEM_SEARCH_DEF;
    public LayoutItem airConStat = AIR_CON_DEF;
    public LayoutItem jetBootsStat = JET_BOOTS_DEF;

    private ArmorHUDLayout() {
        super(true);
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("Description", "Stores the layout of Pneumatic Armor HUD elements");
        JsonObject sub = new JsonObject();
        sub.add("power", powerStat.toJson());
        sub.add("message", messageStat.toJson());
        sub.add("blockTracker", blockTrackerStat.toJson());
        sub.add("entityTracker", entityTrackerStat.toJson());
        sub.add("itemSearch", itemSearchStat.toJson());
        sub.add("airCon", airConStat.toJson());
        sub.add("jetBoots", jetBootsStat.toJson());
        json.add("stats", sub);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        if (json.has("stats")) { // will always be false on dedicated server
            JsonObject sub = json.getAsJsonObject("stats");
            powerStat = readLayout(sub, "power", POWER_DEF);
            messageStat = readLayout(sub, "message", MESSAGE_DEF);
            blockTrackerStat = readLayout(sub, "blockTracker", BLOCK_TRACKER_DEF);
            entityTrackerStat = readLayout(sub, "entityTracker", ENTITY_TRACKER_DEF);
            itemSearchStat = readLayout(sub, "itemSearch", ITEM_SEARCH_DEF);
            airConStat = readLayout(sub, "airCon", AIR_CON_DEF);
            jetBootsStat = readLayout(sub, "jetBoots", JET_BOOTS_DEF);
        }
    }

    private LayoutItem readLayout(JsonObject json, String name, LayoutItem def) {
        return json.has(name) ? LayoutItem.fromJson(json.get(name).getAsJsonObject()) : def;
    }

    @Override
    public String getConfigFilename() {
        return "PneumaticArmorHUDLayout";
    }

    public void updateLayout(LayoutType layoutType, float x, float y, boolean leftSided) {
        layoutType.update(this, new LayoutItem(x, y, leftSided));
        tryWriteToFile();
    }

    public static class LayoutItem {
        private final float x;
        private final float y;
        private final boolean leftSided;

        LayoutItem(float x, float y, boolean leftSided) {
            this.x = x;
            this.y = y;
            this.leftSided = leftSided;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public boolean isLeftSided() {
            return leftSided;
        }

        JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", x);
            obj.addProperty("y", y);
            obj.addProperty("leftSided", leftSided);
            return obj;
        }

        static LayoutItem fromJson(JsonObject obj) {
            return new LayoutItem(
                    obj.get("x").getAsFloat(),
                    obj.get("y").getAsFloat(),
                    obj.get("leftSided").getAsBoolean()
            );
        }
    }

    public enum LayoutType {
        POWER((layout, item) -> layout.powerStat = item),
        MESSAGE((layout, item) -> layout.messageStat = item),
        ENTITY_TRACKER((layout, item) -> layout.entityTrackerStat = item),
        BLOCK_TRACKER((layout, item) -> layout.blockTrackerStat = item),
        ITEM_SEARCH((layout, item) -> layout.itemSearchStat = item),
        AIR_CON((layout, item) -> layout.airConStat = item),
        JET_BOOTS((layout, item) -> layout.jetBootsStat = item);

        private final BiConsumer<ArmorHUDLayout, LayoutItem> consumer;

        LayoutType(BiConsumer<ArmorHUDLayout, LayoutItem> consumer) {
            this.consumer = consumer;
        }

        void update(ArmorHUDLayout layout, LayoutItem item) {
            consumer.accept(layout, item);
        }
    }
}
