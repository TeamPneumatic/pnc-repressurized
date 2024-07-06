package me.desht.pneumaticcraft.common.util.legacyconv;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.EnumOldAreaType;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;

public class ProgWidgetLegacyConv {
    public static int determineVersion(JsonObject json) {
        if (json.has("version")) {
            // 1.20.6 and newer
            return json.get("version").getAsInt();
        } else if (json.has("pneumaticcraft:progWidgets")) {
            // 1.14 - 1.20
            return 2;
        } else if (json.has("widgets")) {
            // 1.12.2 and older
            return 1;
        } else {
            throw new JsonSyntaxException("can't determine saved progwidget version!");
        }
    }

    public static void convertLegacy(JsonObject json, int oldVersion) {
        switch (oldVersion) {
            case 1 -> convertV1toV2(json);
            case 2 -> convertV2toV3(json);
            default -> {
            }
        }
    }

    /**
     * Handle legacy conversion: PNC 1.12.2 and older used a simple (mixed case) widget string
     * but now ProgWidgets are registry entries and use a ResourceLocation.  Also, convert any
     * Area widgets from the old-style format if necessary.
     *
     * @param json the legacy data to convert
     */
    private static void convertV1toV2(JsonObject json) {
        JsonObject sub = json.getAsJsonObject("widgets");
        JsonArray values = sub.getAsJsonArray("value");

        for (JsonElement el : values) {
            JsonObject value = el.getAsJsonObject();
            JsonObject nameObj = value.getAsJsonObject("name");
            String newName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, nameObj.get("value").getAsString());
            nameObj.addProperty("value", Names.MOD_ID + ":" + newName);
            if (newName.equals("area")) {
                JsonObject typeObj = value.getAsJsonObject("type");
                EnumOldAreaType oldType = EnumOldAreaType.values()[typeObj.get("value").getAsInt()];
                AreaType newType = LegacyAreaWidgetConverter.convertFromLegacyFormat(oldType, value.getAsJsonObject("typeInfo").get("value").getAsInt());
                typeObj.addProperty("type", 8);
                typeObj.addProperty("value", newType.getName());
            }
        }

        json.add("pneumaticcraft:progWidgets", values);
        json.remove("widgets");
    }


    //
//    private void doLegacyConversion(CompoundTag nbt) {
//        ListTag l = nbt.getList("widgets", Tag.TAG_COMPOUND);
//        int areaConversions = 0;
//        for (int i = 0; i < l.size(); i++) {
//            CompoundTag subTag = l.getCompound(i);
//            String newName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, subTag.getString("name"));
//            subTag.putString("name", Names.MOD_ID + ":" + newName);
//            if (newName.equals("area")) {
//                EnumOldAreaType oldType = EnumOldAreaType.values()[subTag.getInt("type")];
//                AreaType newType = LegacyAreaWidgetConverter.convertFromLegacyFormat(oldType, subTag.getInt("typeInfo"));
//                subTag.putString("type", newType.getName());
//                newType.writeToNBT(subTag);
//                areaConversions++;
//            }
//        }
//        nbt.put(IProgrammable.NBT_WIDGETS, l);
//        nbt.remove("widgets");
//        if (areaConversions > 0) {
//            Log.info("Pastebin import: converted {} legacy area widgets", areaConversions);
//        }
//    }

    /**
     * Handle version 2 (1.14-1.20) -> version 3 (1.21+) progwidget conversion
     * @param json the legacy data to convert
     */
    private static void convertV2toV3(JsonObject json) {
        // TODO
    }
}
