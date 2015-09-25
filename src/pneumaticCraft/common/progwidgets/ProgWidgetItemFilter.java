package pneumaticCraft.common.progwidgets;

import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetItemFilter;
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetItemFilter extends ProgWidget implements IVariableWidget{
    private ItemStack filter;
    public boolean useMetadata = true, useNBT, useOreDict, useModSimilarity;
    public int specificMeta;
    private DroneAIManager aiManager;
    private String variable = "";

    @SideOnly(Side.CLIENT)
    private static RenderItem itemRender;

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(variable.equals("") && filter == null) {
            curInfo.add("gui.progWidget.itemFilter.error.noFilter");
        }
    }

    @Override
    public void renderExtraInfo(){
        if(variable.equals("")) {
            if(filter != null) {
                drawItemStack(filter, 10, 2, "");
            }
        } else {
            super.renderExtraInfo();
        }
    }

    @Override
    public String getExtraStringInfo(){
        return "\"" + variable + "\"";
    }

    public ItemStack getFilter(){
        return variable.equals("") ? filter : aiManager != null ? aiManager.getStack(variable) : null;
    }

    public void setFilter(ItemStack filter){
        this.filter = filter;
    }

    public static void drawItemStack(ItemStack p_146982_1_, int p_146982_2_, int p_146982_3_, String p_146982_4_){
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
        GL11.glDisable(GL11.GL_LIGHTING);
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
        if(filter != null) {
            saveItemStackByName(filter, tag);
        }
        tag.setBoolean("useMetadata", useMetadata);
        tag.setBoolean("useNBT", useNBT);
        tag.setBoolean("useOreDict", useOreDict);
        tag.setBoolean("useModSimilarity", useModSimilarity);
        tag.setInteger("specificMeta", specificMeta);
        tag.setString("variable", variable);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        filter = tag.getTag("id") instanceof NBTPrimitive ? ItemStack.loadItemStackFromNBT(tag) : loadItemStackByName(tag);
        useMetadata = tag.getBoolean("useMetadata");
        useNBT = tag.getBoolean("useNBT");
        useOreDict = tag.getBoolean("useOreDict");
        useModSimilarity = tag.getBoolean("useModSimilarity");
        specificMeta = tag.getInteger("specificMeta");
        variable = tag.getString("variable");
    }

    private static void saveItemStackByName(ItemStack stack, NBTTagCompound tag){
        tag.setString("id", GameData.getItemRegistry().getNameForObject(stack.getItem()));
        tag.setByte("Count", (byte)stack.stackSize);
        tag.setShort("Damage", (short)stack.getItemDamage());
        if(stack.hasTagCompound()) {
            tag.setTag("tag", stack.getTagCompound());
        }
    }

    private static ItemStack loadItemStackByName(NBTTagCompound tag){
        Item item = GameData.getItemRegistry().getObject(tag.getString("id"));
        if(item == null) return null;
        ItemStack stack = new ItemStack(item, tag.getByte("Count"), tag.getShort("Damage"));
        if(stack.getItemDamage() < 0) stack.setItemDamage(0);

        if(tag.hasKey("tag", 10)) {
            stack.setTagCompound(tag.getCompoundTag("tag"));
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetItemFilter(this, guiProgrammer);
    }

    public static boolean isItemValidForFilters(ItemStack item, List<ProgWidgetItemFilter> whitelist, List<ProgWidgetItemFilter> blacklist, int blockMeta){
        if(blacklist != null) {
            for(ProgWidgetItemFilter black : blacklist) {
                if(PneumaticCraftUtils.areStacksEqual(black.getFilter(), item, black.useMetadata && blockMeta == -1, black.useNBT, black.useOreDict, black.useModSimilarity)) {
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
                if(PneumaticCraftUtils.areStacksEqual(white.getFilter(), item, white.useMetadata && blockMeta == -1, white.useNBT, white.useOreDict, white.useModSimilarity)) {
                    if(blockMeta == -1 || !white.useMetadata || white.specificMeta == blockMeta) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.BURST_PLANT_DAMAGE;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager){
        this.aiManager = aiManager;
    }

    public void setVariable(String variable){
        this.variable = variable;
    }

    public String getVariable(){
        return variable;
    }

    @Override
    public void addVariables(Set<String> variables){
        variables.add(variable);
    }
}
