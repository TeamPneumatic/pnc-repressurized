package pneumaticCraft.common.thirdparty.computercraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.ai.StringFilterEntitySelector;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IBlockOrdered;
import pneumaticCraft.common.progwidgets.IEntityProvider;
import pneumaticCraft.common.progwidgets.IGotoWidget;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ITextWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.progwidgets.ProgWidgetItemFilter;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetCC extends ProgWidgetAreaItemBase implements IBlockOrdered, ISidedWidget, IGotoWidget,
        IEntityProvider, ITextWidget{
    private EnumOrder order = EnumOrder.CLOSEST;
    private boolean[] sides = new boolean[6];
    private final Set<ChunkPosition> area = new HashSet<ChunkPosition>();
    private final List<ProgWidgetItemFilter> itemWhitelist = new ArrayList<ProgWidgetItemFilter>();
    private final List<ProgWidgetItemFilter> itemBlacklist = new ArrayList<ProgWidgetItemFilter>();
    private StringFilterEntitySelector whitelistFilter, blacklistFilter;

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CC;
    }

    @Override
    public int getHeight(){
        return 22;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAICC(drone, (ProgWidgetCC)widget, false);
    }

    @Override
    public EntityAIBase getWidgetTargetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAICC(drone, (ProgWidgetCC)widget, true);
    }

    public Set<ChunkPosition> getInterfaceArea(){
        return getArea((ProgWidgetArea)getConnectedParameters()[0], (ProgWidgetArea)getConnectedParameters()[1]);
    }

    @Override
    public void setOrder(EnumOrder order){
        this.order = order;
    }

    @Override
    public EnumOrder getOrder(){
        return order;
    }

    public String[] getAreaTypes(){
        String[] areaTypes = new String[ProgWidgetArea.EnumAreaType.values().length];
        for(int i = 0; i < areaTypes.length; i++) {
            areaTypes[i] = ProgWidgetArea.EnumAreaType.values()[i].toString();
        }
        return areaTypes;
    }

    public void addArea(int x, int y, int z){
        area.add(new ChunkPosition(x, y, z));
    }

    public void addArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException{
        area.addAll(getArea(x1, y1, z1, x2, y2, z2, areaType));
    }

    public void removeArea(int x, int y, int z){
        area.remove(new ChunkPosition(x, y, z));
    }

    public void removeArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException{
        area.removeAll(getArea(x1, y1, z1, x2, y2, z2, areaType));
    }

    public void clearArea(){
        area.clear();
    }

    @Override
    public Set<ChunkPosition> getArea(){
        return area;
    }

    private Set<ChunkPosition> getArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException{
        ProgWidgetArea.EnumAreaType type = null;
        for(ProgWidgetArea.EnumAreaType t : ProgWidgetArea.EnumAreaType.values()) {
            if(t.toString().equals(areaType)) {
                type = t;
                break;
            }
        }
        if(type == null) throw new IllegalArgumentException("Invalid area type: " + areaType);
        ProgWidgetArea helperWidget = new ProgWidgetArea();
        helperWidget.x1 = x1;
        helperWidget.y1 = y1;
        helperWidget.z1 = z1;
        helperWidget.x2 = x2;
        helperWidget.y2 = y2;
        helperWidget.z2 = z2;
        helperWidget.type = type;
        return helperWidget.getArea();
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item, int blockMetadata){
        return ProgWidgetItemFilter.isItemValidForFilters(item, itemWhitelist, itemBlacklist, blockMetadata);
    }

    public void addWhitelistItemFilter(String itemName, int damage, boolean useMetadata, boolean useNBT, boolean useOreDict, boolean useModSimilarity) throws IllegalArgumentException{
        itemWhitelist.add(getItemFilter(itemName, damage, useMetadata, useNBT, useOreDict, useModSimilarity));
    }

    public void addBlacklistItemFilter(String itemName, int damage, boolean useMetadata, boolean useNBT, boolean useOreDict, boolean useModSimilarity) throws IllegalArgumentException{
        itemBlacklist.add(getItemFilter(itemName, damage, useMetadata, useNBT, useOreDict, useModSimilarity));
    }

    public void clearItemWhitelist(){
        itemWhitelist.clear();
    }

    public void clearItemBlacklist(){
        itemBlacklist.clear();
    }

    private ProgWidgetItemFilter getItemFilter(String itemName, int damage, boolean useMetadata, boolean useNBT, boolean useOreDict, boolean useModSimilarity) throws IllegalArgumentException{
        if(!itemName.contains(":")) throw new IllegalArgumentException("Item/Block name doesn't contain a ':'!");
        String[] itemParts = itemName.split(":");
        Item item = GameRegistry.findItem(itemParts[0], itemParts[1]);
        if(item == null) throw new IllegalArgumentException("Item not found for the name \"" + itemName + "\"!");
        ProgWidgetItemFilter itemFilter = new ProgWidgetItemFilter();
        itemFilter.filter = new ItemStack(item, 1, damage);
        itemFilter.specificMeta = damage;
        itemFilter.useMetadata = useMetadata;
        itemFilter.useNBT = useNBT;
        itemFilter.useOreDict = useOreDict;
        itemFilter.useModSimilarity = useModSimilarity;
        return itemFilter;
    }

    public void addWhitelistText(String text){
        if(whitelistFilter == null) whitelistFilter = new StringFilterEntitySelector();
        whitelistFilter.addEntry(text);
    }

    public void addBlacklistText(String text){
        if(blacklistFilter == null) blacklistFilter = new StringFilterEntitySelector();
        blacklistFilter.addEntry(text);
    }

    public void clearWhitelistText(){
        whitelistFilter = null;
    }

    public void clearBlacklistText(){
        blacklistFilter = null;
    }

    @Override
    public String getWidgetString(){
        return "computerCraft";
    }

    @Override
    public String getGuiTabText(){
        return "With this widget you can control a Drone via ComputerCraft. To do this attach an Area that has a Drone Interface in it. When this piece gets triggered it will send an event to the connected computers and you can control it. When done, invoke an exitPiece() to allow the program to resume running the other pieces.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF6e2988;
    }

    @Override
    public List<EntityLivingBase> getValidEntities(World world){
        List<Entity> entities = ProgWidgetAreaItemBase.getEntitiesInArea(getEntityAreaWidget(), null, world, whitelistFilter, blacklistFilter);
        List<EntityLivingBase> livingEntities = new ArrayList<EntityLivingBase>();
        for(Entity entity : entities) {
            if(entity instanceof EntityLivingBase) {
                livingEntities.add((EntityLivingBase)entity);
            }
        }
        return livingEntities;
    }

    private ProgWidgetArea getEntityAreaWidget(){
        ProgWidgetArea widget = new ProgWidgetArea();
        ChunkPosition minPos = getMinPos();
        ChunkPosition maxPos = getMaxPos();
        widget.x1 = minPos.chunkPosX;
        widget.y1 = minPos.chunkPosY;
        widget.z1 = minPos.chunkPosZ;
        widget.x2 = maxPos.chunkPosX;
        widget.y2 = maxPos.chunkPosY;
        widget.z2 = maxPos.chunkPosZ;
        return widget;
    }

    @Override
    public List<Entity> getEntitiesInArea(World world, IEntitySelector filter){
        return ProgWidgetAreaItemBase.getEntitiesInArea(getEntityAreaWidget(), null, world, filter, null);
    }

    private ChunkPosition getMinPos(){
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        int z = Integer.MAX_VALUE;
        for(ChunkPosition p : area) {
            x = Math.min(p.chunkPosX, x);
            y = Math.min(p.chunkPosY, y);
            z = Math.min(p.chunkPosZ, z);
        }
        return new ChunkPosition(x, y, z);
    }

    private ChunkPosition getMaxPos(){
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        int z = Integer.MIN_VALUE;
        for(ChunkPosition p : area) {
            x = Math.max(p.chunkPosX, x);
            y = Math.max(p.chunkPosY, y);
            z = Math.max(p.chunkPosZ, z);
        }
        return new ChunkPosition(x, y, z);
    }

    @Override
    public boolean doneWhenDeparting(){
        return false;
    }

    @Override
    public void setDoneWhenDeparting(boolean bool){}

    @Override
    public void setSides(boolean[] sides){
        this.sides = sides;
    }

    @Override
    public boolean[] getSides(){
        return sides;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return null;
    }

    @Override
    public String getLegacyString(){
        return "CC-Int";
    }

}
