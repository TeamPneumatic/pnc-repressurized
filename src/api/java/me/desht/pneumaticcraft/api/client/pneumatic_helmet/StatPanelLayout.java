package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import com.google.gson.JsonObject;

/**
 * Represents a resolution-independent position for an armor HUD stat panel
 *
 * @param x X position, in range 0..1
 * @param y Y position, in range 0..1
 * @param isLeftSided true if panel is anchored to the left, false is anchored to the right
 */
public record StatPanelLayout(float x, float y, boolean isLeftSided) {
    public static final StatPanelLayout DEFAULT = new StatPanelLayout(0f, 0.5f, false);

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", x);
        obj.addProperty("y", y);
        obj.addProperty("leftSided", isLeftSided);
        return obj;
    }

    public static StatPanelLayout fromJson(JsonObject obj) {
        return new StatPanelLayout(
                obj.get("x").getAsFloat(),
                obj.get("y").getAsFloat(),
                obj.get("leftSided").getAsBoolean()
        );
    }
}

