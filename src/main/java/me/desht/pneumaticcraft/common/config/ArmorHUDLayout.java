package me.desht.pneumaticcraft.common.config;

import com.google.gson.JsonObject;

import java.io.IOException;

public class ArmorHUDLayout extends JsonConfig {
    public static final ArmorHUDLayout INSTANCE = new ArmorHUDLayout();

    private boolean needLegacyImport = true;

    private static final LayoutItem POWER_DEF = new LayoutItem(0.995f, 0.005f, true);
    private static final LayoutItem MESSAGE_DEF = new LayoutItem(0.005f, 0.15f, false);
    private static final LayoutItem BLOCK_TRACKER_DEF = new LayoutItem(0.995f, 0.1f, true);
    private static final LayoutItem ENTITY_TRACKER_DEF = new LayoutItem(0.995f, 0.2f, true);
    private static final LayoutItem ITEM_SEARCH_DEF = new LayoutItem(0.005f, 0.1f, false);
    private static final LayoutItem AIR_CON_DEF = new LayoutItem(0.5f, 0.005f, false);
    private static final LayoutItem JET_BOOTS_DEF = new LayoutItem(0.7f, 0.005f, true);

    public LayoutItem powerStat = POWER_DEF;
    public LayoutItem messageStat = MESSAGE_DEF;
    public LayoutItem blockTrackerStat = BLOCK_TRACKER_DEF;
    public LayoutItem entityTrackerStat = ENTITY_TRACKER_DEF;
    public LayoutItem itemSearchStat = ITEM_SEARCH_DEF;
    public LayoutItem airConStat = AIR_CON_DEF;
    public LayoutItem jetBootsStat = JET_BOOTS_DEF;

    private ArmorHUDLayout() {
        super(false);
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("Description", "Stores the layout of Pneumatic Armor HUD elements");
        if (needLegacyImport) {
            json.addProperty("needLegacyImport", needLegacyImport);
        } else {
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
    }

    @Override
    protected void readFromJson(JsonObject json) {
        needLegacyImport = json.has("needLegacyImport") && json.get("needLegacyImport").getAsBoolean();

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

    /**
     * Called when a player logs in client-side to copy the old settings from the main config.
     *
     * @param sx screen X resolution
     * @param sy screen Y resolution
     */
    public void maybeImportLegacySettings(int sx, int sy) {
        if (needLegacyImport) {
            needLegacyImport = false;

            ConfigHandler.HelmetOptions ho = ConfigHandler.helmetOptions;
            powerStat = new LayoutItem(sx, sy, ho.powerX, ho.powerY, ho.powerLeft);
            messageStat = new LayoutItem(sx, sy, ho.messageX, ho.messageY, ho.messageLeft);
            blockTrackerStat = new LayoutItem(sx, sy, ho.blockTrackerX, ho.blockTrackerY, ho.blockTrackerLeft);
            entityTrackerStat = new LayoutItem(sx, sy, ho.entityTrackerX, ho.entityTrackerY, ho.entityTrackerLeft);
            itemSearchStat = new LayoutItem(sx, sy, ho.itemSearchX, ho.itemSearchY, ho.itemSearchLeft);
            airConStat = new LayoutItem(sx, sy, ho.acStatX, ho.acStatY, ho.acStatLeft);
            jetBootsStat = JET_BOOTS_DEF; // no legacy import for jetBootsStat

            try {
                writeToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateLayout(LayoutTypes what, float x, float y, boolean leftSided) {
        LayoutItem l = new LayoutItem(x, y, leftSided);
        switch (what) {
            case POWER: powerStat = l; break;
            case MESSAGE: messageStat = l; break;
            case ENTITY_TRACKER: entityTrackerStat = l; break;
            case BLOCK_TRACKER: blockTrackerStat = l; break;
            case ITEM_SEARCH: itemSearchStat = l; break;
            case AIR_CON: airConStat = l; break;
            case JET_BOOTS: jetBootsStat = l; break;
        }
        try {
            writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        LayoutItem(int screenX, int screenY, int x, int y, boolean leftSided) {
            this.x = (float) x / (float) screenX;
            this.y = (float) y / (float) screenY;
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

    public enum LayoutTypes {
        POWER,
        MESSAGE,
        ENTITY_TRACKER,
        BLOCK_TRACKER,
        ITEM_SEARCH,
        AIR_CON,
        JET_BOOTS
    }
}
