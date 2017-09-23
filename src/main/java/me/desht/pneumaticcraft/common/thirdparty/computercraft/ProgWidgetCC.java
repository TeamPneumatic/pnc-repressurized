package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import com.google.common.base.Predicate;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgWidgetCC extends ProgWidgetAreaItemBase implements IBlockOrdered, ISidedWidget, IGotoWidget,
        IEntityProvider, ITextWidget, ICondition, ICountWidget, IItemDropper, ILiquidFiltered, IRedstoneEmissionWidget,
        IRenamingWidget, ICraftingWidget, IMaxActions, IBlockRightClicker, ILiquidExport, ISignEditWidget {
    private EnumOrder order = EnumOrder.CLOSEST;
    private boolean[] sides = new boolean[6];
    private final Set<BlockPos> area = new HashSet<BlockPos>();
    private final List<ProgWidgetItemFilter> itemWhitelist = new ArrayList<ProgWidgetItemFilter>();
    private final List<ProgWidgetItemFilter> itemBlacklist = new ArrayList<ProgWidgetItemFilter>();
    private StringFilterEntitySelector whitelistFilter, blacklistFilter;

    private int emittingRedstone;
    private boolean dropItemStraight;
    private boolean useCount;
    private int count;
    private boolean useMaxActions;
    private int maxActions;
    private boolean isAndFunction;
    private Operator operator;
    private final List<ProgWidgetLiquidFilter> liquidBlacklist = new ArrayList<ProgWidgetLiquidFilter>();
    private final List<ProgWidgetLiquidFilter> liquidWhitelist = new ArrayList<ProgWidgetLiquidFilter>();
    private String renamingName;
    private ItemStack[] craftingGrid = new ItemStack[9];
    private boolean sneaking;
    private boolean placeFluidBlocks;
    public String[] signText = new String[0];

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CC;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICC((EntityDrone) drone, (ProgWidgetCC) widget, false);
    }

    @Override
    public EntityAIBase getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICC((EntityDrone) drone, (ProgWidgetCC) widget, true);
    }

    public Set<BlockPos> getInterfaceArea() {
        Set<BlockPos> area = new HashSet<BlockPos>();
        getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[1]);
        return area;
    }

    @Override
    public void setOrder(EnumOrder order) {
        this.order = order;
    }

    @Override
    public EnumOrder getOrder() {
        return order;
    }

    public String[] getAreaTypes() {
        String[] areaTypes = new String[ProgWidgetArea.EnumAreaType.values().length];
        for (int i = 0; i < areaTypes.length; i++) {
            areaTypes[i] = ProgWidgetArea.EnumAreaType.values()[i].toString();
        }
        return areaTypes;
    }

    public synchronized void addArea(int x, int y, int z) {
        area.add(new BlockPos(x, y, z));
    }

    public synchronized void addArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException {
        area.addAll(getArea(x1, y1, z1, x2, y2, z2, areaType));
    }

    public synchronized void removeArea(int x, int y, int z) {
        area.remove(new BlockPos(x, y, z));
    }

    public synchronized void removeArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException {
        area.removeAll(getArea(x1, y1, z1, x2, y2, z2, areaType));
    }

    public synchronized void clearArea() {
        area.clear();
    }

    @Override
    public synchronized void getArea(Set<BlockPos> area) {
        area.addAll(this.area);
    }

    private Set<BlockPos> getArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException {
        ProgWidgetArea.EnumAreaType type = null;
        for (ProgWidgetArea.EnumAreaType t : ProgWidgetArea.EnumAreaType.values()) {
            if (t.toString().equals(areaType)) {
                type = t;
                break;
            }
        }
        if (type == null) throw new IllegalArgumentException("Invalid area type: " + areaType);
        ProgWidgetArea helperWidget = new ProgWidgetArea();
        helperWidget.x1 = x1;
        helperWidget.y1 = y1;
        helperWidget.z1 = z1;
        helperWidget.x2 = x2;
        helperWidget.y2 = y2;
        helperWidget.z2 = z2;
        helperWidget.type = type;
        Set<BlockPos> a = new HashSet<BlockPos>();
        helperWidget.getArea(a);
        return a;
    }

    @Override
    public synchronized boolean isItemValidForFilters(ItemStack item, IBlockState blockMetadata) {
        return ProgWidgetItemFilter.isItemValidForFilters(item, itemWhitelist, itemBlacklist, blockMetadata);
    }

    @Override
    public boolean isItemFilterEmpty() {
        return itemWhitelist.isEmpty() && itemBlacklist.isEmpty();
    }

    public synchronized void addWhitelistItemFilter(String itemName, int damage, boolean useMetadata, boolean useNBT, boolean useOreDict, boolean useModSimilarity) throws IllegalArgumentException {
        itemWhitelist.add(getItemFilter(itemName, damage, useMetadata, useNBT, useOreDict, useModSimilarity));
    }

    public synchronized void addBlacklistItemFilter(String itemName, int damage, boolean useMetadata, boolean useNBT, boolean useOreDict, boolean useModSimilarity) throws IllegalArgumentException {
        itemBlacklist.add(getItemFilter(itemName, damage, useMetadata, useNBT, useOreDict, useModSimilarity));
    }

    public synchronized void clearItemWhitelist() {
        itemWhitelist.clear();
    }

    public synchronized void clearItemBlacklist() {
        itemBlacklist.clear();
    }

    private ProgWidgetItemFilter getItemFilter(String itemName, int damage, boolean useMetadata, boolean useNBT, boolean useOreDict, boolean useModSimilarity) throws IllegalArgumentException {
        if (!itemName.contains(":")) throw new IllegalArgumentException("Item/Block name doesn't contain a ':'!");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) throw new IllegalArgumentException("Item not found for the name \"" + itemName + "\"!");
        ProgWidgetItemFilter itemFilter = new ProgWidgetItemFilter();
        itemFilter.setFilter(new ItemStack(item, 1, damage));
        itemFilter.specificMeta = damage;
        itemFilter.useMetadata = useMetadata;
        itemFilter.useNBT = useNBT;
        itemFilter.useOreDict = useOreDict;
        itemFilter.useModSimilarity = useModSimilarity;
        return itemFilter;
    }

    public synchronized void addWhitelistText(String text) {
        if (whitelistFilter == null) whitelistFilter = new StringFilterEntitySelector();
        whitelistFilter.addEntry(text);
    }

    public synchronized void addBlacklistText(String text) {
        if (blacklistFilter == null) blacklistFilter = new StringFilterEntitySelector();
        blacklistFilter.addEntry(text);
    }

    public synchronized void clearWhitelistText() {
        whitelistFilter = null;
    }

    public synchronized void clearBlacklistText() {
        blacklistFilter = null;
    }

    @Override
    public String getWidgetString() {
        return "computerCraft";
    }

    @Override
    public synchronized List<Entity> getValidEntities(World world) {
        return ProgWidgetAreaItemBase.getEntitiesInArea(getEntityAreaWidget(), null, world, whitelistFilter, blacklistFilter);
    }

    private ProgWidgetArea getEntityAreaWidget() {
        ProgWidgetArea widget = new ProgWidgetArea();
        BlockPos minPos = getMinPos();
        BlockPos maxPos = getMaxPos();
        widget.x1 = minPos.getX();
        widget.y1 = minPos.getY();
        widget.z1 = minPos.getZ();
        widget.x2 = maxPos.getX();
        widget.y2 = maxPos.getY();
        widget.z2 = maxPos.getZ();
        return widget;
    }

    @Override
    public synchronized List<Entity> getEntitiesInArea(World world, Predicate<? super Entity> filter) {
        return ProgWidgetAreaItemBase.getEntitiesInArea(getEntityAreaWidget(), null, world, filter, null);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        return (whitelistFilter == null || whitelistFilter.apply(entity)) && (blacklistFilter == null || !blacklistFilter.apply(entity));
    }

    private BlockPos getMinPos() {
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        int z = Integer.MAX_VALUE;
        for (BlockPos p : area) {
            x = Math.min(p.getX(), x);
            y = Math.min(p.getY(), y);
            z = Math.min(p.getZ(), z);
        }
        return new BlockPos(x, y, z);
    }

    private BlockPos getMaxPos() {
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        int z = Integer.MIN_VALUE;
        for (BlockPos p : area) {
            x = Math.max(p.getX(), x);
            y = Math.max(p.getY(), y);
            z = Math.max(p.getZ(), z);
        }
        return new BlockPos(x, y, z);
    }

    @Override
    public boolean doneWhenDeparting() {
        return false;
    }

    @Override
    public void setDoneWhenDeparting(boolean bool) {
    }

    @Override
    public synchronized void setSides(boolean[] sides) {
        this.sides = sides;
    }

    @Override
    public synchronized boolean[] getSides() {
        return sides;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return null;
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.PURPLE;
    }

    public void setEmittingRedstone(int redstone) {
        emittingRedstone = redstone;
    }

    @Override
    public int getEmittingRedstone() {
        return emittingRedstone;
    }

    public synchronized void addWhitelistLiquidFilter(String fluidName) throws IllegalArgumentException {
        liquidWhitelist.add(getFilterForArgs(fluidName));
    }

    public synchronized void addBlacklistLiquidFilter(String fluidName) throws IllegalArgumentException {
        liquidBlacklist.add(getFilterForArgs(fluidName));
    }

    public synchronized void clearLiquidWhitelist() {
        liquidWhitelist.clear();
    }

    public synchronized void clearLiquidBlacklist() {
        liquidBlacklist.clear();
    }

    private ProgWidgetLiquidFilter getFilterForArgs(String fluidName) throws IllegalArgumentException {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) throw new IllegalArgumentException("Can't find fluid for the name \"" + fluidName + "\"!");
        ProgWidgetLiquidFilter filter = new ProgWidgetLiquidFilter();
        filter.setFluid(fluid);
        return filter;
    }

    @Override
    public synchronized boolean isFluidValid(Fluid fluid) {
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, liquidWhitelist, liquidBlacklist);
    }

    @Override
    public boolean dropStraight() {
        return dropItemStraight;
    }

    @Override
    public void setDropStraight(boolean dropStraight) {
        dropItemStraight = true;
    }

    @Override
    public boolean useCount() {
        return useCount;
    }

    @Override
    public void setUseCount(boolean useCount) {
        this.useCount = useCount;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean isAndFunction() {
        return isAndFunction;
    }

    @Override
    public void setAndFunction(boolean isAndFunction) {
        this.isAndFunction = isAndFunction;
    }

    @Override
    public int getRequiredCount() {
        return count;
    }

    @Override
    public void setRequiredCount(int count) {
        this.count = count;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public synchronized void setOperator(String operator) throws IllegalArgumentException {
        for (Operator op : Operator.values()) {
            if (op.toString().equals(operator)) {
                setOperator(op);
                return;
            }
        }
        throw new IllegalArgumentException("Invalid operator: \"" + operator + "\". Valid operators are: \">=\" and \"=\"");
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        return false;
    }

    public void setNewName(String name) {
        renamingName = name;
    }

    @Override
    public String getNewName() {
        return renamingName;
    }

    public void setCraftingGrid(String[] stackStrings) {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            if (stackStrings[i] != null)
                grid[i] = getItemFilter(stackStrings[i], 0, false, false, false, false).getFilter();
        }
        craftingGrid = grid;
    }

    @Override
    public InventoryCrafting getCraftingGrid() {
        InventoryCrafting invCrafting = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer p_75145_1_) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < 9; i++)
            invCrafting.setInventorySlotContents(i, craftingGrid[i]);
        return invCrafting;
    }

    @Override
    public void setMaxActions(int maxActions) {
        this.maxActions = maxActions;
    }

    @Override
    public int getMaxActions() {
        return maxActions;
    }

    @Override
    public void setUseMaxActions(boolean useMaxActions) {
        this.useMaxActions = useMaxActions;
    }

    @Override
    public boolean useMaxActions() {
        return useMaxActions;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    @Override
    public void setPlaceFluidBlocks(boolean placeFluidBlocks) {
        this.placeFluidBlocks = placeFluidBlocks;
    }

    @Override
    public boolean isPlacingFluidBlocks() {
        return placeFluidBlocks;
    }

    @Override
    public String[] getLines() {
        return signText;
    }

}
