package me.desht.pneumaticcraft.common.util.legacyconv;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.drone.area.AreaType;
import me.desht.pneumaticcraft.api.drone.area.AreaType.AreaAxis;
import me.desht.pneumaticcraft.api.drone.area.EnumOldAreaType;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.drone.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.*;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypeBox.BoxType;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypeCylinder.CylinderType;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypePyramid.PyramidType;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypeSphere.SphereType;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaTypeTorus.TorusType;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.mutable.MutableByte;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.legacyconv.ConversionUtil.*;

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
        JsonArray entries = json.getAsJsonObject("widgets").getAsJsonArray("value");

        for (JsonElement el : entries) {
            JsonObject oldEntry = el.getAsJsonObject();

            JsonObject nameObj = oldEntry.getAsJsonObject("name");
            String newName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, nameObj.get("value").getAsString());
            nameObj.addProperty("value", Names.MOD_ID + ":" + newName);

            if (newName.equals("area")) {
                // special handling for area widget area types
                JsonObject typeObj = oldEntry.getAsJsonObject("type");
                EnumOldAreaType oldType = EnumOldAreaType.values()[typeObj.get("value").getAsInt()];
                AreaType newType = LegacyAreaWidgetConverter.convertFromLegacyFormat(oldType, oldEntry.getAsJsonObject("typeInfo").get("value").getAsInt());
                typeObj.addProperty("type", 8);
                typeObj.addProperty("value", newType.getName());
            }
        }

        json.add("pneumaticcraft:progWidgets", Util.make(new JsonObject(), j -> {
            j.addProperty("type", 9);
            j.add("value", entries);
        }));
        json.remove("widgets");
    }

    /**
     * Handle version 2 (1.14-1.20) -> version 3 (1.21+) progwidget conversion
     * @param json the legacy data to convert
     */
    private static void convertV2toV3(JsonObject json) {
        JsonArray entries = json.getAsJsonObject("pneumaticcraft:progWidgets").getAsJsonArray("value");

        JsonArray newDoc = new JsonArray();

        for (JsonElement entry : entries) {
            JsonObject oldEntry = entry.getAsJsonObject();

            JsonObject newEntry = new JsonObject();
            JsonObject pos = new JsonObject();
            JsonObject inv = new JsonObject();
            JsonObject cond = new JsonObject();
            JsonObject droneCond = new JsonObject();
            JsonObject digPlace = new JsonObject();

            String widgetType = getString(oldEntry, "name").orElseThrow(() -> new JsonSyntaxException("no 'name' field!"));
            if (!widgetType.contains(":")) widgetType = Names.MOD_ID + ":" + widgetType;
            newEntry.addProperty("type", widgetType);
            String widgetBaseType = widgetType.contains(":") ? widgetType.split(":")[1] : widgetType;

            // pos
            getInt(oldEntry, "x").ifPresent(val -> pos.addProperty("x", val));
            getInt(oldEntry, "y").ifPresent(val -> pos.addProperty("y", val));

            // inv, but:
            //  crafting widget stores count/use_count in top-level
            //  emit redstone widget stores sides in top-level
            JsonObject destObj1 = widgetBaseType.equals("crafting") ? newEntry : inv;
            getInt(oldEntry, "count").ifPresent(val -> destObj1.addProperty("count", val));
            getBool(oldEntry, "useCount").ifPresent(val -> destObj1.addProperty("use_count", val));
            byte sides = findSides(oldEntry);
            if (sides != 0) {
                (widgetBaseType.equals("emit_redstone") ? newEntry : inv).addProperty("sides", sides);
            }

            // dig/place/right-click/harvest
            getInt(oldEntry, "order").ifPresent(val -> digPlace.addProperty("order", IBlockOrdered.Ordering.values()[val].getSerializedName()));
            getBool(oldEntry, "useMaxActions").ifPresent(val -> digPlace.addProperty("use_max_actions", val));
            getInt(oldEntry, "maxActions").ifPresent(val -> digPlace.addProperty("max_actions", val));

            getBool(oldEntry, "requireDiggingTool").ifPresent(val -> newEntry.addProperty("require_tool", val));
            getBool(oldEntry,"requireHoe").ifPresent(val -> newEntry.addProperty("require_hoe", val));
            getBool(oldEntry, "sneaking").ifPresent(val -> newEntry.addProperty("sneaking", val));
            getString(oldEntry, "clickType").ifPresent(val -> newEntry.addProperty("click_type", val.toLowerCase(Locale.ROOT)));
            getInt(oldEntry, "dir").ifPresent(val -> newEntry.addProperty("side", Direction.from3DDataValue(val).getSerializedName()));

            // conditions
            JsonObject destObj2 = widgetBaseType.startsWith("drone_condition") ? droneCond : cond;
            getBool(oldEntry,"isAndFunction").ifPresent(val -> destObj2.addProperty("and_func", val));
            getString(oldEntry,"measureVar").ifPresent(val -> destObj2.addProperty("measure_var", val));
            getInt(oldEntry,"operator").ifPresent(val -> {
                if (widgetBaseType.startsWith("condition_") || widgetBaseType.startsWith("drone_condition_")) {
                    destObj2.addProperty("cond_op", ICondition.Operator.values()[val].getSerializedName());
                }
            });
            getInt(oldEntry, "requiredCount").ifPresent(val -> droneCond.addProperty("required_count", val));
            getBool(oldEntry, "checkingForAir").ifPresent(val -> newEntry.addProperty("check_air", val));
            getBool(oldEntry, "checkingForLiquids").ifPresent(val -> newEntry.addProperty("check_liquid", val));

            // coordinate
            if (oldEntry.has("coord")) {
                convXYZ(oldEntry.getAsJsonObject("coord").getAsJsonObject("value"), newEntry, "", "coord");
            }
            if (oldEntry.has("posX")) {
                convXYZ(oldEntry, newEntry, "pos", "coord");
            }
            getString(oldEntry,"variable").ifPresent(val -> newEntry.addProperty("var", val));
            getBool(oldEntry,"useVariable").ifPresent(val -> newEntry.addProperty("using_var", val));

            // coordinate condition / operator
            byte axes = findAxes(oldEntry);
            if (axes != 0) {
                newEntry.add("axis_options", Util.make(new JsonObject(), j -> j.addProperty("axes", axes)));
            }
            getInt(oldEntry, "operator").ifPresent(val -> {
                switch (widgetBaseType) {
                    case "condition_coordinate" -> newEntry.addProperty("cond_op", ICondition.Operator.values()[val].getSerializedName());
                    case "coordinate_operator" -> newEntry.addProperty("coord_op", ProgWidgetCoordinateOperator.EnumOperator.values()[val].getSerializedName());
                }
            });

            // attack
            getBool(oldEntry,"checkSight").ifPresent(val -> newEntry.addProperty("check_sight", val));
            getBool(oldEntry,"useMaxActions").ifPresent(val -> newEntry.addProperty("use_max_actions", val));
            getInt(oldEntry,"maxActions").ifPresent(val -> newEntry.addProperty("max_actions", val));

            // area
            if (widgetBaseType.equals("area")) {
                if (oldEntry.has("pos1")) {
                    convXYZ(oldEntry.getAsJsonObject("pos1").getAsJsonObject("value"), newEntry, "", "pos1");
                    if (oldEntry.has("pos2")) {
                        convXYZ(oldEntry.getAsJsonObject("pos2").getAsJsonObject("value"), newEntry, "", "pos2");
                    }
                } else {
                    int x1 = getInt(oldEntry, "x1").orElse(0);
                    int y1 = getInt(oldEntry, "y1").orElse(0);
                    int z1 = getInt(oldEntry, "z1").orElse(0);
                    int x2 = getInt(oldEntry, "x2").orElse(0);
                    int y2 = getInt(oldEntry, "y2").orElse(0);
                    int z2 = getInt(oldEntry, "z2").orElse(0);
                    newEntry.add("pos1", makeArray(x1, y1, z1));
                    newEntry.add("pos2", makeArray(x2, y2, z2));
                }
                getString(oldEntry, "coord1Variable").ifPresent(val -> newEntry.addProperty("var1", val));
                getString(oldEntry, "var1").ifPresent(val -> newEntry.addProperty("var1", val));
                getString(oldEntry, "coord2Variable").ifPresent(val -> newEntry.addProperty("var2", val));
                getString(oldEntry, "var2").ifPresent(val -> newEntry.addProperty("var2", val));
                getString(oldEntry, "type").ifPresent(val -> {
                    JsonObject areaType = new JsonObject();
                    areaType.addProperty("type", Names.MOD_ID + ":" + val);
                    getInt(oldEntry, "axis").ifPresent(val1 -> areaType.addProperty("axis", AreaAxis.values()[val1].getSerializedName()));
                    getInt(oldEntry, "boxType").ifPresent(val1 -> areaType.addProperty("box_type", BoxType.values()[val1].getSerializedName()));
                    getInt(oldEntry, "cylinderType").ifPresent(val1 -> areaType.addProperty("cylinder_type", CylinderType.values()[val1].getSerializedName()));
                    getInt(oldEntry, "sphereType").ifPresent(val1 -> areaType.addProperty("sphere_type", SphereType.values()[val1].getSerializedName()));
                    getInt(oldEntry, "pyramidType").ifPresent(val1 -> areaType.addProperty("pyramid_type", PyramidType.values()[val1].getSerializedName()));
                    getInt(oldEntry, "torusType").ifPresent(val1 -> areaType.addProperty("torus_type", TorusType.values()[val1].getSerializedName()));
                    getInt(oldEntry, "interval").ifPresent(val1 -> areaType.addProperty("interval", val1));
                    getInt(oldEntry, "pickedAmount").ifPresent(val1 -> areaType.addProperty("picked_amount", val1));
                    newEntry.add("area_type", areaType);
                });
            }

            // external prog
            getBool(oldEntry, "shareVariables").ifPresent(val -> newEntry.addProperty("share_variables", val));

            // go to
            getString(oldEntry,"doneWhenDeparting").ifPresent(val -> newEntry.addProperty("done_when_depart", val));

            // text/comment
            getString(oldEntry,"string").ifPresent(val -> newEntry.addProperty("string", val));

            // edit sign
            getBool(oldEntry, "back").ifPresent(val -> newEntry.addProperty("back_side", val));

            // drop/pickup item
            getBool(oldEntry, "dropStraight").ifPresent(val -> newEntry.addProperty("drop_straight", val));
            getBool(oldEntry, "pickupDelay").ifPresent(val -> newEntry.addProperty("pickup_delay", val));
            getBool(oldEntry, "canSteal").ifPresent(val -> newEntry.addProperty("can_steal", val));

            // standby
            getBool(oldEntry, "allowStandbyPickup").ifPresent(val -> newEntry.addProperty("allow_pickup", val));

            // liquid export
            getBool(oldEntry, "placeFluidBlocks").ifPresent(val -> newEntry.addProperty("place_fluid_blocks", val));

            // item filter
            if (widgetBaseType.equals("item_filter")) {
                JsonObject item = new JsonObject();
                getString(oldEntry, "id").ifPresent(val -> item.addProperty("id", val));
                getInt(oldEntry, "Count").ifPresent(val -> item.addProperty("count", val));
                if (item.has("id")) newEntry.add("chk_item", item);
                getBool(oldEntry, "useMetadata").ifPresent(val -> newEntry.addProperty("chk_durability", val));
                getBool(oldEntry, "useNBT").ifPresent(val -> newEntry.addProperty("chk_components", val));
                getBool(oldEntry, "useModSimilarity").ifPresent(val -> newEntry.addProperty("chk_mod", val));
                getBool(oldEntry, "matchBlock").ifPresent(val -> newEntry.addProperty("chk_block", val));
            }

            // liquid filter
            if (widgetBaseType.equals("liquid_filter")) {
                getString(oldEntry, "fluid").ifPresent(val -> newEntry.add("fluid", Util.make(new JsonObject(), j -> {
                    j.addProperty("id", val);
                    j.addProperty("amount", 1000);
                })));
            }

            // add all the sub-objects
            newEntry.add("pos", pos);
            // TODO: .isEmpty() can't be used right now: https://github.com/neoforged/NeoForge/issues/1380
            if (digPlace.size() > 0) newEntry.add("dig_place", digPlace);
            if (inv.size() > 0) newEntry.add("inv", inv);
            if (cond.size() > 0) newEntry.add("cond", cond);
            if (droneCond.size() > 0) newEntry.add("drone_cond", cond);

            // phew, done
            newDoc.add(newEntry);
        }

        json.add("widgets", newDoc);
        json.remove("pneumaticcraft:progWidgets");
        json.addProperty("version", ProgWidget.JSON_VERSION);
    }

    private static byte findAxes(JsonObject obj) {
        MutableByte res = new MutableByte(0);

        getInt(obj, "checkX").ifPresent(val -> res.setValue(res.byteValue() | val));
        getInt(obj, "checkY").ifPresent(val -> res.setValue(res.byteValue() | val << 1));
        getInt(obj, "checkZ").ifPresent(val -> res.setValue(res.byteValue() | val << 2));

        return res.byteValue();
    }

    private static byte findSides(JsonObject obj) {
        MutableByte res = new MutableByte(0);

        getInt(obj, "DOWN").ifPresent(val -> res.setValue(res.byteValue() | val));
        getInt(obj, "UP").ifPresent(val -> res.setValue(res.byteValue() | val << 1));
        getInt(obj, "NORTH").ifPresent(val -> res.setValue(res.byteValue() | val << 2));
        getInt(obj, "SOUTH").ifPresent(val -> res.setValue(res.byteValue() | val << 3));
        getInt(obj, "WEST").ifPresent(val -> res.setValue(res.byteValue() | val << 4));
        getInt(obj, "EAST").ifPresent(val -> res.setValue(res.byteValue() | val << 5));

        return res.byteValue();
    }

    /**
     * Exists to support saved drone programs from 1.12 and older versions of PneumaticCraft, and also to support
     * the Computer Control progwidget's "addArea" and "removeArea" methods.
     */
    public static class LegacyAreaWidgetConverter {
        private static final Map<EnumOldAreaType, String> oldFormatToAreaTypes = new EnumMap<>(EnumOldAreaType.class);
        static {
            register(AreaTypeBox.ID, EnumOldAreaType.FILL, EnumOldAreaType.WALL, EnumOldAreaType.FRAME);
            register(AreaTypeSphere.ID, EnumOldAreaType.SPHERE);
            register(AreaTypeLine.ID, EnumOldAreaType.LINE);
            register(AreaTypeWall.ID, EnumOldAreaType.X_WALL, EnumOldAreaType.Y_WALL, EnumOldAreaType.Z_WALL);
            register(AreaTypeCylinder.ID, EnumOldAreaType.X_CYLINDER, EnumOldAreaType.Y_CYLINDER, EnumOldAreaType.Z_CYLINDER);
            register(AreaTypePyramid.ID, EnumOldAreaType.X_PYRAMID, EnumOldAreaType.Y_PYRAMID, EnumOldAreaType.Z_PYRAMID);
            register(AreaTypeGrid.ID, EnumOldAreaType.GRID);
            register(AreaTypeRandom.ID, EnumOldAreaType.RANDOM);
            if (oldFormatToAreaTypes.size() != EnumOldAreaType.values().length)
                throw new IllegalStateException("Not all old formats are handled!");
        }

        private static void register(String id, EnumOldAreaType... oldTypes) {
            for (EnumOldAreaType oldType : oldTypes) {
                oldFormatToAreaTypes.put(oldType, id);
            }
        }

        public static AreaType convertFromLegacyFormat(EnumOldAreaType oldType, int subTypeInfo) {
            String newTypeId = oldFormatToAreaTypes.get(oldType);
            if (newTypeId == null) {
                Log.error("Legacy import: no area converter found for {}! Substituting 'box'.", oldType);
                return new AreaTypeBox();
            } else {
                var s = PNCRegistries.AREA_TYPE_SERIALIZER_REGISTRY.get(RL(newTypeId));
                return s != null ? Util.make(s.createDefaultInstance(), t -> t.convertFromLegacy(oldType, subTypeInfo)) : new AreaTypeBox();
            }
        }
    }
}
