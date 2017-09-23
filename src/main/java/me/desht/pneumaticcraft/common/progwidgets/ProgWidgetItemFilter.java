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
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Set;

public class ProgWidgetItemFilter extends ProgWidget implements IVariableWidget {
    private ItemStack filter;
    public boolean useMetadata = true, useNBT, useOreDict, useModSimilarity;
    public int specificMeta;
    private DroneAIManager aiManager;
    private String variable = "";

    @SideOnly(Side.CLIENT)
    private static RenderItem itemRender;

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
            if (filter != null) {
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

    public ItemStack getFilter() {
        return variable.equals("") ? filter : aiManager != null ? aiManager.getStack(variable) : null;
    }

    public void setFilter(ItemStack filter) {
        this.filter = filter;
    }

    public static void drawItemStack(ItemStack stack, int x, int y, String text) {
        RenderHelper.disableStandardItemLighting();
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        //  zLevel = 200.0F;
        if (itemRender == null) itemRender = Minecraft.getMinecraft().getRenderItem();
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (stack != null) font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = mc.fontRenderer;
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(font, stack, x, y, text);
        GL11.glPopMatrix();

        //GL11.glEnable(GL11.GL_LIGHTING);
        RenderHelper.enableGUIStandardItemLighting();
        // zLevel = 0.0F;
        //  itemRender.zLevel = 0.0F;
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    public void getTooltip(List<String> curTooltip) {
        super.getTooltip(curTooltip);
        if (filter != null) {
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
