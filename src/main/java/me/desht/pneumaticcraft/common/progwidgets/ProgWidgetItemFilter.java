package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.ProgWidgetRenderer;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemFilter extends ProgWidget implements IVariableWidget {
    private ItemStack filter = ItemStack.EMPTY;
    public boolean useItemDurability;
    public boolean useNBT;
    public boolean useModSimilarity;
    public boolean matchBlock;
    private DroneAIManager aiManager;
    private String variable = "";

    public ProgWidgetItemFilter() {
        super(ModProgWidgets.ITEM_FILTER);
    }

    public static ProgWidgetItemFilter withFilter(ItemStack filter){
        ProgWidgetItemFilter widget = new ProgWidgetItemFilter();
        widget.filter = filter;
        return widget;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("") && filter == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.error.noFilter"));
        }
        if (matchBlock && !(filter.getItem() instanceof BlockItem)) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.itemFilter.error.notBlock"));
        }
    }

    @Override
    public void renderExtraInfo() {
        ProgWidgetRenderer.renderItemFilterExtras(this);
        if (variable.isEmpty()) {
            if (!filter.isEmpty()) {
                GuiUtils.drawItemStack(filter, 10, 2, "");
            }
        } else {
            super.renderExtraInfo();
        }
    }

    @Override
    public String getExtraStringInfo() {
        return variable.isEmpty() ? "" : "\"" + variable + "\"";
    }

    @Nonnull
    public ItemStack getFilter() {
        return variable.isEmpty() ? filter : aiManager != null ? aiManager.getStack(variable) : ItemStack.EMPTY;
    }

    public ItemStack getRawFilter() {
        return filter;
    }

    public void setFilter(@Nonnull ItemStack filter) {
        this.filter = filter.copy();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (!filter.isEmpty()) {
            curTooltip.add(new StringTextComponent("Filter: ").applyTextStyle(TextFormatting.AQUA).appendSibling(filter.getDisplayName()));
            if (useModSimilarity) {
                curTooltip.add(new StringTextComponent("- Using Mod Similarity").applyTextStyle(TextFormatting.DARK_AQUA));
            } else {
                curTooltip.add(new StringTextComponent((useItemDurability ? "Using" : "Ignoring") + " item damage").applyTextStyle(TextFormatting.DARK_AQUA));
                if (matchBlock) {
                    curTooltip.add(new StringTextComponent("- Matching by block").applyTextStyle(TextFormatting.DARK_AQUA));
                } else {
                    curTooltip.add(new StringTextComponent(useNBT ? "Using NBT" : "Ignoring NBT").applyTextStyle(TextFormatting.DARK_AQUA));
                }
            }
        }
    }

    @Override
    public boolean hasStepInput() {
        return false;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return ModProgWidgets.ITEM_FILTER;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ITEM_FILTER;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (!filter.isEmpty()) {
            filter.write(tag);
        }
        tag.putBoolean("useMetadata", useItemDurability);
        tag.putBoolean("useNBT", useNBT);
        tag.putBoolean("useModSimilarity", useModSimilarity);
        tag.putBoolean("matchBlock", matchBlock);
        tag.putString("variable", variable);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        filter = ItemStack.read(tag);
        useItemDurability = tag.getBoolean("useMetadata");
        useNBT = tag.getBoolean("useNBT");
        useModSimilarity = tag.getBoolean("useModSimilarity");
        matchBlock = tag.getBoolean("matchBlock");
        variable = tag.getString("variable");
    }

    @Override
    public void writeToPacket(PacketBuffer buf) {
        super.writeToPacket(buf);
        buf.writeItemStack(filter);
        buf.writeBoolean(useItemDurability);
        buf.writeBoolean(useNBT);
        buf.writeBoolean(useModSimilarity);
        buf.writeBoolean(matchBlock);
        buf.writeString(variable);
    }

    @Override
    public void readFromPacket(PacketBuffer buf) {
        super.readFromPacket(buf);
        filter = buf.readItemStack();
        useItemDurability = buf.readBoolean();
        useNBT = buf.readBoolean();
        useModSimilarity = buf.readBoolean();
        matchBlock = buf.readBoolean();
        variable = buf.readString(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    public static boolean isItemValidForFilters(ItemStack item, List<ProgWidgetItemFilter> whitelist, List<ProgWidgetItemFilter> blacklist, BlockState blockState) {
        if (blacklist != null) {
            for (ProgWidgetItemFilter black : blacklist) {
                if (matchFilter(item, blockState, black)) return false;
            }
        }
        if (whitelist == null || whitelist.size() == 0) {
            return true;
        } else {
            for (ProgWidgetItemFilter white : whitelist) {
                if (matchFilter(item, blockState, white)) return true;
            }
            return false;
        }
    }

    private static boolean matchFilter(ItemStack stack, BlockState blockState, ProgWidgetItemFilter filter) {
        if (filter.matchBlock && stack.isEmpty() && blockState != null && filter.getFilter().getItem() instanceof BlockItem) {
            // match by block
            return blockState.getBlock() == ((BlockItem) filter.getFilter().getItem()).getBlock();
        } else {
            // match by item
            if (PneumaticCraftUtils.doesItemMatchFilter(filter.getFilter(), stack, filter.useItemDurability && blockState == null, filter.useNBT, filter.useModSimilarity)) {
                return blockState == null || !filter.useItemDurability;
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
}
