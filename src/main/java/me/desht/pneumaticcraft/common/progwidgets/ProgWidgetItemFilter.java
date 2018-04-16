package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetItemFilter;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class ProgWidgetItemFilter extends ProgWidget implements IVariableWidget {
    private ItemStack filter = ItemStack.EMPTY;
    public boolean useMetadata = true, useNBT, useOreDict, useModSimilarity;
    public int specificMeta;
    private DroneAIManager aiManager;
    private String variable = "";

    @SideOnly(Side.CLIENT)
    private static RenderItem itemRender;
    
    public static ProgWidgetItemFilter withFilter(ItemStack filter){
        ProgWidgetItemFilter widget = new ProgWidgetItemFilter();
        widget.filter = filter;
        return widget;
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (variable.equals("") && filter == null) {
            curInfo.add("gui.progWidget.itemFilter.error.noFilter");
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
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        if (itemRender == null) itemRender = Minecraft.getMinecraft().getRenderItem();
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
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        if (!filter.isEmpty()) {
            curTooltip.add("Current filter:");
            curTooltip.add(filter.getDisplayName());
            if (useOreDict) {
                curTooltip.add("Using Ore Dictionary");
            } else if (useModSimilarity) {
                curTooltip.add("Using Mod similarity");
            } else {
                curTooltip.add((useMetadata ? "Using" : "Ignoring") + " metadata / damage values");
                curTooltip.add((useNBT ? "Using" : "Ignoring") + " NBT tags");
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
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (filter != null) {
            filter.writeToNBT(tag);
        }
        tag.setBoolean("useMetadata", useMetadata);
        tag.setBoolean("useNBT", useNBT);
        tag.setBoolean("useOreDict", useOreDict);
        tag.setBoolean("useModSimilarity", useModSimilarity);
        tag.setInteger("specificMeta", specificMeta);
        tag.setString("variable", variable);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        filter = new ItemStack(tag);
        useMetadata = tag.getBoolean("useMetadata");
        useNBT = tag.getBoolean("useNBT");
        useOreDict = tag.getBoolean("useOreDict");
        useModSimilarity = tag.getBoolean("useModSimilarity");
        specificMeta = tag.getInteger("specificMeta");
        variable = tag.getString("variable");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetItemFilter(this, guiProgrammer);
    }

    public static boolean isItemValidForFilters(ItemStack item, List<ProgWidgetItemFilter> whitelist, List<ProgWidgetItemFilter> blacklist, IBlockState blockState) {
        if (blacklist != null) {
            for (ProgWidgetItemFilter black : blacklist) {
                if (PneumaticCraftUtils.areStacksEqual(black.getFilter(), item, black.useMetadata && blockState == null, black.useNBT, black.useOreDict, black.useModSimilarity)) {
                    if (blockState == null || !black.useMetadata || black.specificMeta == blockState.getBlock().getMetaFromState(blockState)) {
                        return false;
                    }
                }
            }
        }
        if (whitelist == null || whitelist.size() == 0) {
            return true;
        } else {
            for (ProgWidgetItemFilter white : whitelist) {
                if (PneumaticCraftUtils.areStacksEqual(white.getFilter(), item, white.useMetadata && blockState == null, white.useNBT, white.useOreDict, white.useModSimilarity)) {
                    if (blockState == null || !white.useMetadata || white.specificMeta == blockState.getBlock().getMetaFromState(blockState)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.GREY;
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
