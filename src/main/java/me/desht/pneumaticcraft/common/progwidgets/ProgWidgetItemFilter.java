package me.desht.pneumaticcraft.common.progwidgets;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemFilter extends ProgWidget implements IVariableWidget {
    private ItemStack filter = ItemStack.EMPTY;
    public boolean useItemDamage = true, useNBT, useItemTags, useModSimilarity, matchBlock;
    private DroneAIManager aiManager;
    private String variable = "";

    @OnlyIn(Dist.CLIENT)
    private static ItemRenderer itemRender;
    
    public static ProgWidgetItemFilter withFilter(ItemStack filter){
        ProgWidgetItemFilter widget = new ProgWidgetItemFilter();
        widget.filter = filter;
        return widget;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("") && filter == null) {
            curInfo.add(xlate("gui.progWidget.itemFilter.error.noFilter"));
        }
        if (matchBlock && !(filter.getItem() instanceof BlockItem)) {
            curInfo.add(xlate("gui.progWidget.itemFilter.error.notBlock"));
        }
    }

    @Override
    public void renderExtraInfo() {
        if (variable.equals("")) {
            if (!filter.isEmpty()) {
                drawItemStack(filter, 10, 2, "");
            }
        } else {
            super.renderExtraInfo();
        }
    }

    @Override
    public String getExtraStringInfo() {
        return "\"" + variable + "\"";
    }

    @Nonnull
    public ItemStack getFilter() {
        return variable.equals("") ? filter : aiManager != null ? aiManager.getStack(variable) : ItemStack.EMPTY;
    }

    public void setFilter(@Nonnull ItemStack filter) {
        this.filter = filter;
    }

    public static void drawItemStack(@Nonnull ItemStack stack, int x, int y, String text) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        Minecraft mc = Minecraft.getInstance();
        GlStateManager.translated(0.0F, 0.0F, 32.0F);
        if (itemRender == null) itemRender = Minecraft.getInstance().getItemRenderer();
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (!stack.isEmpty()) font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = mc.fontRenderer;
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, stack, x, y, text);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
    }

    @Override
    public void getTooltip(List<ITextComponent> curTooltip) {
        super.getTooltip(curTooltip);
        if (!filter.isEmpty()) {
            curTooltip.add(new StringTextComponent("Filter: ").applyTextStyle(TextFormatting.AQUA).appendSibling(filter.getDisplayName()));
            if (useItemTags) {
                curTooltip.add(new StringTextComponent("- Using Item Tag Similarity").applyTextStyle(TextFormatting.DARK_AQUA));
            } else if (useModSimilarity) {
                curTooltip.add(new StringTextComponent("- Using Mod Similarity").applyTextStyle(TextFormatting.DARK_AQUA));
            } else {
                curTooltip.add(new StringTextComponent((useItemDamage ? "Using" : "Ignoring") + " item damage").applyTextStyle(TextFormatting.DARK_AQUA));
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
    public Class<? extends IProgWidget> returnType() {
        return ProgWidgetItemFilter.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetItemFilter.class};
    }

    @Override
    public String getWidgetString() {
        return "itemFilter";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_ITEM_FILTER;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        if (filter != null) {
            filter.write(tag);
        }
        tag.putBoolean("useMetadata", useItemDamage);
        tag.putBoolean("useNBT", useNBT);
        tag.putBoolean("useOreDict", useItemTags);
        tag.putBoolean("useModSimilarity", useModSimilarity);
        tag.putBoolean("matchBlock", matchBlock);
        tag.putString("variable", variable);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        filter = ItemStack.read(tag);
        useItemDamage = tag.getBoolean("useMetadata");
        useNBT = tag.getBoolean("useNBT");
        useItemTags = tag.getBoolean("useOreDict");
        useModSimilarity = tag.getBoolean("useModSimilarity");
        matchBlock = tag.getBoolean("matchBlock");
        variable = tag.getString("variable");
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
            if (PneumaticCraftUtils.areStacksEqual(filter.getFilter(), stack, filter.useItemDamage && blockState == null, filter.useNBT, filter.useItemTags, filter.useModSimilarity)) {
                return blockState == null || !filter.useItemDamage;
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
