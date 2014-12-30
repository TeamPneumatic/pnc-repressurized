package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetItemFilter;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetItemFilter extends ProgWidget{
    public ItemStack filter;
    public boolean useMetadata = true, useNBT, useOreDict, useModSimilarity;
    public int specificMeta;
    @SideOnly(Side.CLIENT)
    private static RenderItem itemRender;

    @Override
    public void renderExtraInfo(){
        if(filter != null) {
            drawItemStack(filter, 10, 2, "");
        }
    }

    private void drawItemStack(ItemStack p_146982_1_, int p_146982_2_, int p_146982_3_, String p_146982_4_){
        RenderHelper.disableStandardItemLighting();
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        //  zLevel = 200.0F;
        if(itemRender == null) itemRender = new RenderItem();
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if(p_146982_1_ != null) font = p_146982_1_.getItem().getFontRenderer(p_146982_1_);
        if(font == null) font = mc.fontRenderer;
        itemRender.renderItemAndEffectIntoGUI(font, mc.getTextureManager(), p_146982_1_, p_146982_2_, p_146982_3_);
        itemRender.renderItemOverlayIntoGUI(font, mc.getTextureManager(), p_146982_1_, p_146982_2_, p_146982_3_, p_146982_4_);
        GL11.glPopMatrix();

        //GL11.glEnable(GL11.GL_LIGHTING);
        RenderHelper.enableGUIStandardItemLighting();
        // zLevel = 0.0F;
        //  itemRender.zLevel = 0.0F;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        if(filter != null) {
            curTooltip.add("Current filter:");
            curTooltip.add(filter.getDisplayName());
            if(useOreDict) {
                curTooltip.add("Using Ore Dictionary");
            } else if(useModSimilarity) {
                curTooltip.add("Using Mod similarity");
            } else {
                curTooltip.add((useMetadata ? "Using" : "Ignoring") + " metadata / damage values");
                curTooltip.add((useNBT ? "Using" : "Ignoring") + " NBT tags");
            }

        }
    }

    @Override
    public boolean hasStepInput(){
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return ProgWidgetItemFilter.class;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class};
    }

    @Override
    public String getWidgetString(){
        return "itemFilter";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ITEM_FILTER;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        if(filter != null) filter.writeToNBT(tag);
        tag.setBoolean("useMetadata", useMetadata);
        tag.setBoolean("useNBT", useNBT);
        tag.setBoolean("useOreDict", useOreDict);
        tag.setBoolean("useModSimilarity", useModSimilarity);
        tag.setInteger("specificMeta", specificMeta);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        filter = ItemStack.loadItemStackFromNBT(tag);
        useMetadata = tag.getBoolean("useMetadata");
        useNBT = tag.getBoolean("useNBT");
        useOreDict = tag.getBoolean("useOreDict");
        useModSimilarity = tag.getBoolean("useModSimilarity");
        specificMeta = tag.getInteger("specificMeta");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetItemFilter(this, guiProgrammer);
    }

    public static boolean isItemValidForFilters(ItemStack item, List<ProgWidgetItemFilter> whitelist, List<ProgWidgetItemFilter> blacklist, int blockMeta){
        if(blacklist != null) {
            for(ProgWidgetItemFilter black : blacklist) {
                if(PneumaticCraftUtils.areStacksEqual(black.filter, item, black.useMetadata && blockMeta == -1, black.useNBT, black.useOreDict, black.useModSimilarity)) {
                    if(blockMeta == -1 || !black.useMetadata || black.specificMeta == blockMeta) {
                        return false;
                    }
                }
            }
        }
        if(whitelist == null || whitelist.size() == 0) {
            return true;
        } else {
            for(ProgWidgetItemFilter white : whitelist) {
                if(PneumaticCraftUtils.areStacksEqual(white.filter, item, white.useMetadata && blockMeta == -1, white.useNBT, white.useOreDict, white.useModSimilarity)) {
                    if(blockMeta == -1 || !white.useMetadata || white.specificMeta == blockMeta) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public String getGuiTabText(){
        return "This program can be used to filter items. These can for example be used to specify which items are allowed to be imported in the Drone, or to specify which blocks are allowed to be dug.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF5f5f5f;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.PARAMETER;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.BURST_PLANT_DAMAGE;
    }
}
