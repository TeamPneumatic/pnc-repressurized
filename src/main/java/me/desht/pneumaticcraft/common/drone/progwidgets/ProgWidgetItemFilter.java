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

package me.desht.pneumaticcraft.common.drone.progwidgets;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.item.TagFilterItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModProgWidgetTypes;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemFilter extends ProgWidget implements IVariableWidget {
    public static final MapCodec<ProgWidgetItemFilter> CODEC = RecordCodecBuilder.mapCodec(builder ->
            baseParts(builder).and(builder.group(
                            ItemStack.CODEC.optionalFieldOf("chk_item", ItemStack.EMPTY).forGetter(ProgWidgetItemFilter::getFilter),
                            Codec.BOOL.optionalFieldOf("chk_durability", false).forGetter(ProgWidgetItemFilter::isCheckDurability),
                            Codec.BOOL.optionalFieldOf("chk_components", false).forGetter(ProgWidgetItemFilter::isMatchComponents),
                            Codec.BOOL.optionalFieldOf("chk_mod", false).forGetter(ProgWidgetItemFilter::isMatchMod),
                            Codec.BOOL.optionalFieldOf("chk_block", false).forGetter(ProgWidgetItemFilter::isMatchBlock),
                            Codec.STRING.optionalFieldOf("var", "").forGetter(ProgWidgetItemFilter::getVariable)
                    )
            ).apply(builder, ProgWidgetItemFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ProgWidgetItemFilter> STREAM_CODEC = NeoForgeStreamCodecs.composite(
            PositionFields.STREAM_CODEC, ProgWidget::getPosition,
            ItemStack.OPTIONAL_STREAM_CODEC, ProgWidgetItemFilter::getFilter,
            ByteBufCodecs.BOOL, ProgWidgetItemFilter::isCheckDurability,
            ByteBufCodecs.BOOL, ProgWidgetItemFilter::isMatchComponents,
            ByteBufCodecs.BOOL, ProgWidgetItemFilter::isMatchMod,
            ByteBufCodecs.BOOL, ProgWidgetItemFilter::isMatchBlock,
            ByteBufCodecs.STRING_UTF8, ProgWidgetItemFilter::getVariable,
            ProgWidgetItemFilter::new
    );

    private ItemStack filter;
    private boolean checkDurability;
    private boolean matchComponents;
    private boolean matchMod;
    private boolean matchBlock;
    private DroneAIManager aiManager;
    private String variable;

    public ProgWidgetItemFilter(PositionFields pos, ItemStack filter, boolean checkDurability, boolean matchComponents, boolean matchMod, boolean matchBlock, String variable) {
        super(pos);
        this.filter = filter;
        this.checkDurability = checkDurability;
        this.matchComponents = matchComponents;
        this.matchMod = matchMod;
        this.matchBlock = matchBlock;
        this.variable = variable;
    }

    public ProgWidgetItemFilter() {
        this(PositionFields.DEFAULT, ItemStack.EMPTY, false, false, false, false, "");
    }

    @Override
    public IProgWidget copyWidget() {
        return new ProgWidgetItemFilter(getPosition(), filter.copy(), checkDurability, matchComponents, matchMod, matchBlock, variable);
    }

    public static ProgWidgetItemFilter withFilter(ItemStack filter){
        ProgWidgetItemFilter widget = new ProgWidgetItemFilter();
        widget.filter = filter;
        return widget;
    }

    @Override
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.isEmpty() && filter == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.error.noFilter"));
        }
        if (matchBlock && !(filter.getItem() instanceof BlockItem) && variable.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.error.notBlock"));
        }
    }

    @Override
    public void addWarnings(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addWarnings(curInfo, widgets);
        IProgWidget p = getParent();
        int n = 1;
        while (p != null) {
            // Item Assign widget only pays attention to the first connected item filter
            if (p instanceof ProgWidgetItemAssign && n > 1) {
                curInfo.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.warning.ignoredItemAssign"));
                break;
            }
            n++;
            p = p.getParent();
        }
    }

    @Override
    public List<Component> getExtraStringInfo() {
        return variable.isEmpty() ? Collections.emptyList() : Collections.singletonList(varAsTextComponent(variable));
    }

    @Nonnull
    public ItemStack getFilter() {
        return variable.isEmpty() ? filter : aiManager != null ? aiManager.getStack(aiManager.getDrone().getOwnerUUID(), variable) : ItemStack.EMPTY;
    }

    public ItemStack getRawFilter() {
        return filter;
    }

    public void setFilter(@Nonnull ItemStack filter) {
        this.filter = filter.copy();
    }

    public boolean isCheckDurability() {
        return checkDurability;
    }

    public void setCheckDurability(boolean checkDurability) {
        this.checkDurability = checkDurability;
    }

    public boolean isMatchComponents() {
        return matchComponents;
    }

    public void setMatchComponents(boolean matchComponents) {
        this.matchComponents = matchComponents;
    }

    public boolean isMatchMod() {
        return matchMod;
    }

    public void setMatchMod(boolean matchMod) {
        this.matchMod = matchMod;
    }

    public boolean isMatchBlock() {
        return matchBlock;
    }

    public void setMatchBlock(boolean matchBlock) {
        this.matchBlock = matchBlock;
    }

    @Override
    public ProgWidgetType<?> getType() {
        return ModProgWidgetTypes.ITEM_FILTER.get();
    }

    @Override
    public void getTooltip(List<Component> curTooltip) {
        super.getTooltip(curTooltip);

        if (!variable.isEmpty()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.coordinate.variable").append(": ").append(varAsTextComponent(variable)).withStyle(ChatFormatting.AQUA));
        } else if (!filter.isEmpty()) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.filterLabel").withStyle(ChatFormatting.AQUA)
                    .append(": ").append(filter.getHoverName()));
            if (filter.getItem() == ModItems.TAG_FILTER.get()) {
                curTooltip.addAll(TagFilterItem.getConfiguredTagList(filter).stream()
                        .map(s -> Symbols.bullet().append(Component.literal(s.location().toString()).withStyle(ChatFormatting.YELLOW)))
                        .toList());
            }
        }
        if (matchMod) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.matchMod", ModNameCache.getModName(filter.getItem()))
                    .withStyle(ChatFormatting.DARK_AQUA));
        } else if (matchBlock) {
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.matchBlock")
                    .withStyle(ChatFormatting.DARK_AQUA));
        } else {
            if (getRawFilter().getMaxDamage() > 0) {
                curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemFilter." + (checkDurability ? "useDurability" : "ignoreDurability"))
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
            curTooltip.add(xlate("pneumaticcraft.gui.progWidget.itemFilter." + (matchComponents ? "useComponents" : "ignoreComponents"))
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgetTypes.ITEM_FILTER.get();
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgetTypes.ITEM_FILTER.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ITEM_FILTER;
    }

    public static boolean isItemValidForFilters(ItemStack item, List<ProgWidgetItemFilter> whitelist, List<ProgWidgetItemFilter> blacklist, BlockState blockState) {
        if (blacklist != null) {
            for (ProgWidgetItemFilter black : blacklist) {
                if (matchFilter(item, blockState, black)) return false;
            }
        }
        if (whitelist == null || whitelist.isEmpty()) {
            return true;
        } else {
            for (ProgWidgetItemFilter white : whitelist) {
                if (matchFilter(item, blockState, white)) return true;
            }
            return false;
        }
    }

    private static boolean matchFilter(ItemStack stack, BlockState blockState, ProgWidgetItemFilter filter) {
        if (filter.matchBlock && blockState != null && filter.getFilter().getItem() instanceof BlockItem) {
            // match by block
            return blockState.getBlock() == ((BlockItem) filter.getFilter().getItem()).getBlock();
        } else {
            // match by item
            if (PneumaticCraftUtils.doesItemMatchFilter(filter.getFilter(), stack, filter.checkDurability && blockState == null, filter.matchComponents, filter.matchMod)) {
                return blockState == null || !filter.checkDurability;
            }
        }
        return false;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.GRAY;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public void addVariables(Set<String> variables) {
        variables.add(variable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProgWidgetItemFilter that = (ProgWidgetItemFilter) o;
        return baseEquals(that) && checkDurability == that.checkDurability && matchComponents == that.matchComponents && matchMod == that.matchMod && matchBlock == that.matchBlock && Objects.equals(filter, that.filter) && Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHashCode(), filter, checkDurability, matchComponents, matchMod, matchBlock, variable);
    }
}
