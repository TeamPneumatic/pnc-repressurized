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

package me.desht.pneumaticcraft.common.variables;

import me.desht.pneumaticcraft.api.misc.IVariableProvider;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TextVariableParser {
    private final String orig;
    private final IVariableProvider variableProvider;
    private final Set<String> relevantVariables = new HashSet<>();
    private final UUID playerID;

    private static final Map<String, VariableRetriever> RETRIEVERS = new HashMap<>();
    static {
        RETRIEVERS.put("pos",  (p, id, varName) -> PneumaticCraftUtils.posToString(p.getCoordinate(id, varName).orElse(BlockPos.ZERO)));
        RETRIEVERS.put("x",    (p, id, varName) -> Integer.toString(p.getCoordinate(id, varName).orElse(BlockPos.ZERO).getX()));
        RETRIEVERS.put("y",    (p, id, varName) -> Integer.toString(p.getCoordinate(id, varName).orElse(BlockPos.ZERO).getY()));
        RETRIEVERS.put("z",    (p, id, varName) -> Integer.toString(p.getCoordinate(id, varName).orElse(BlockPos.ZERO).getZ()));
        RETRIEVERS.put("item", (p, id, varName) -> stackToStr(p.getStack(id, varName), false));
        RETRIEVERS.put("id",   (p, id, varName) -> stackToStr(p.getStack(id, varName), true));
    }

    public TextVariableParser(String str, UUID playerID) {
        this.orig = str;
        this.variableProvider = GlobalVariableHelper.getInstance().getVariableProvider();
        this.playerID = playerID;
    }

    public TextVariableParser(String str, DroneAIManager droneAIManager) {
        this.orig = str;
        this.variableProvider = droneAIManager;
        this.playerID = droneAIManager.getDrone().getOwnerUUID();
    }

    public static String parseString(String input, UUID playerID) {
        return new TextVariableParser(input, playerID).parse();
    }

    public static Component parseComponent(Component input, UUID playerID) {
        if (input.getSiblings().isEmpty() && input.getContents() instanceof PlainTextContents contents) {
            // simple case, just a single literal string
            return Component.literal(parseString(contents.text(), playerID)).withStyle(input.getStyle());
        }

        MutableComponent parsed = Component.empty();
        input.visit((style, str) -> {
            TextVariableParser parser = new TextVariableParser(str, playerID);
            parsed.append(Component.literal(parser.parse()).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return parsed;
    }

    public String parse() {
        int index;
        String ret = orig;
        while ((index = ret.indexOf("${")) >= 0) {
            int secondIndex = ret.indexOf("}", index);
            if (secondIndex >= 0) {
                String varName = ret.substring(index + 2, secondIndex);
                boolean isItem = !variableProvider.getStack(playerID, varName).isEmpty() && variableProvider.getCoordinate(playerID, varName).isEmpty();
                String varValue = getVariableValue(varName, isItem);
                ret = ret.substring(0, index) + varValue + ret.substring(secondIndex + 1);
            } else {
                return ret.substring(0, index) + "Parsing error: Missing '}'";
            }
        }
        return ret;
    }

    public Set<String> getRelevantVariables() {
        return relevantVariables;
    }

    private String getVariableValue(String varNameWithExt, boolean isItem) {
        String[] f = StringUtils.splitByWholeSeparator(varNameWithExt, ".", 2);
        final String varName = f[0];
        final String ext = f.length == 2 ? f[1] : (isItem ? "item" : "pos");

        VariableRetriever handler = RETRIEVERS.get(ext);
        if (handler == null) return "";

        relevantVariables.add(varName);

        return handler.retrieve(variableProvider, playerID, varName);
    }

    private static String stackToStr(ItemStack stack, boolean id) {
        if (stack.isEmpty()) return "";
        return id ? PneumaticCraftUtils.getRegistryName(stack.getItem()).orElseThrow().toString() : stack.getDisplayName().getString();
    }

    @FunctionalInterface
    private interface VariableRetriever {
        String retrieve(IVariableProvider provider, UUID playerID, String varName);
    }
}
