package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter.EnumOldAreaType;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;

public class ProgWidgetCC extends ProgWidgetInventoryBase implements IBlockOrdered, IGotoWidget,
        IEntityProvider, ITextWidget, ICondition, IItemDropper, ILiquidFiltered, IRedstoneEmissionWidget,
        IRenamingWidget, ICraftingWidget, IMaxActions, IBlockRightClicker, ILiquidExport, ISignEditWidget, IToolUser {
    private EnumOrder order = EnumOrder.CLOSEST;
    private boolean[] sides = new boolean[6];
    private final Set<BlockPos> area = new HashSet<>();
    private final List<ProgWidgetItemFilter> itemWhitelist = new ArrayList<>();
    private final List<ProgWidgetItemFilter> itemBlacklist = new ArrayList<>();
    private StringFilterEntitySelector whitelistFilter, blacklistFilter;

    private int emittingRedstone;
    private boolean dropItemStraight;
    private boolean useCount;
    private int count;
    private boolean useMaxActions;
    private int maxActions;
    private boolean isAndFunction;
    private Operator operator;
    private final List<ProgWidgetLiquidFilter> liquidBlacklist = new ArrayList<>();
    private final List<ProgWidgetLiquidFilter> liquidWhitelist = new ArrayList<>();
    private String renamingName;
    private ItemStack[] craftingGrid = new ItemStack[9];
    private boolean sneaking;
    private boolean placeFluidBlocks;
    private boolean requiresTool;
    String[] signText = new String[0];
    private boolean pickupDelay;

    public ProgWidgetCC() {
        super(ModProgWidgets.COMPUTER_CONTROL);
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.PURPLE;
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && ThirdPartyManager.instance().isModTypeLoaded(ThirdPartyManager.ModType.COMPUTER);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CC;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICC((EntityDrone) drone, (ProgWidgetCC) widget, false);
    }

    @Override
    public Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICC((EntityDrone) drone, (ProgWidgetCC) widget, true);
    }

    Set<BlockPos> getInterfaceArea() {
        Set<BlockPos> area = new HashSet<>();
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

    String[] getAreaTypes() {
        String[] areaTypes = new String[EnumOldAreaType.values().length];
        for (int i = 0; i < areaTypes.length; i++) {
            areaTypes[i] = EnumOldAreaType.values()[i].toString();
        }
        return areaTypes;
    }

    public synchronized void addArea(int x, int y, int z) {
        area.add(new BlockPos(x, y, z));
        invalidateAreaCache();
    }

    public synchronized void addArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException {
        area.addAll(getArea(x1, y1, z1, x2, y2, z2, areaType));
        invalidateAreaCache();
    }

    synchronized void removeArea(int x, int y, int z) {
        area.remove(new BlockPos(x, y, z));
        invalidateAreaCache();
    }

    synchronized void removeArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException {
        area.removeAll(getArea(x1, y1, z1, x2, y2, z2, areaType));
        invalidateAreaCache();
    }

    synchronized void clearArea() {
        area.clear();
        invalidateAreaCache();
    }

    @Override
    public synchronized void getArea(Set<BlockPos> area) {
        area.addAll(this.area);
    }

    private Set<BlockPos> getArea(int x1, int y1, int z1, int x2, int y2, int z2, String areaType) throws IllegalArgumentException {
        EnumOldAreaType type = EnumOldAreaType.valueOf(areaType.toUpperCase());
        ProgWidgetArea helperWidget = new ProgWidgetArea();
        helperWidget.x1 = x1;
        helperWidget.y1 = y1;
        helperWidget.z1 = z1;
        helperWidget.x2 = x2;
        helperWidget.y2 = y2;
        helperWidget.z2 = z2;
        helperWidget.type = LegacyAreaWidgetConverter.convertFromLegacyFormat(type, 0);
        Set<BlockPos> a = new HashSet<>();
        helperWidget.getArea(a);
        return a;
    }

    @Override
    public synchronized boolean isItemValidForFilters(ItemStack item, BlockState blockMetadata) {
        return ProgWidgetItemFilter.isItemValidForFilters(item, itemWhitelist, itemBlacklist, blockMetadata);
    }

    @Override
    public boolean isItemFilterEmpty() {
        return itemWhitelist.isEmpty() && itemBlacklist.isEmpty();
    }

    synchronized void addWhitelistItemFilter(String itemName, boolean useNBT, boolean useModSimilarity) throws IllegalArgumentException {
        itemWhitelist.add(getItemFilter(itemName, useNBT, useModSimilarity));
    }

    synchronized void addBlacklistItemFilter(String itemName, boolean useNBT, boolean useModSimilarity) throws IllegalArgumentException {
        itemBlacklist.add(getItemFilter(itemName, useNBT, useModSimilarity));
    }

    synchronized void clearItemWhitelist() {
        itemWhitelist.clear();
    }

    synchronized void clearItemBlacklist() {
        itemBlacklist.clear();
    }

    private ProgWidgetItemFilter getItemFilter(String itemName, boolean useNBT, boolean useModSimilarity) throws IllegalArgumentException {
        if (!itemName.contains(":")) throw new IllegalArgumentException("Item/Block name doesn't contain a ':'!");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) throw new IllegalArgumentException("Item not found for the name \"" + itemName + "\"!");
        ProgWidgetItemFilter itemFilter = new ProgWidgetItemFilter();
        itemFilter.setFilter(new ItemStack(item));
        itemFilter.useNBT = useNBT;
        itemFilter.useModSimilarity = useModSimilarity;
        return itemFilter;
    }

    synchronized void addWhitelistText(String text) {
        if (whitelistFilter == null) whitelistFilter = new StringFilterEntitySelector();
        whitelistFilter.addEntry(text);
    }

    synchronized void addBlacklistText(String text) {
        if (blacklistFilter == null) blacklistFilter = new StringFilterEntitySelector();
        blacklistFilter.addEntry(text);
    }

    synchronized void clearWhitelistText() {
        whitelistFilter = null;
    }

    synchronized void clearBlacklistText() {
        blacklistFilter = null;
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

    void setEmittingRedstone(int redstone) {
        emittingRedstone = redstone;
    }

    @Override
    public int getEmittingRedstone() {
        return emittingRedstone;
    }

    synchronized void addWhitelistLiquidFilter(String fluidName) throws IllegalArgumentException {
        liquidWhitelist.add(getFilterForArgs(fluidName));
    }

    synchronized void addBlacklistLiquidFilter(String fluidName) throws IllegalArgumentException {
        liquidBlacklist.add(getFilterForArgs(fluidName));
    }

    synchronized void clearLiquidWhitelist() {
        liquidWhitelist.clear();
    }

    synchronized void clearLiquidBlacklist() {
        liquidBlacklist.clear();
    }

    private ProgWidgetLiquidFilter getFilterForArgs(String fluidName) throws IllegalArgumentException {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
        if (fluid == null || fluid == Fluids.EMPTY) throw new IllegalArgumentException("Can't find fluid for the name \"" + fluidName + "\"!");
        return ProgWidgetLiquidFilter.withFilter(fluid);
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
    public boolean hasPickupDelay() {
        return pickupDelay;
    }

    @Override
    public void setPickupDelay(boolean pickupDelay) {
        this.pickupDelay = pickupDelay;
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
        throw new IllegalArgumentException("Invalid operator: '" + operator + "'. Valid operators are: '<=', '=', '>='");
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        return false;
    }

    void setNewName(String name) {
        renamingName = name;
    }

    @Override
    public String getNewName() {
        return renamingName;
    }

    void setCraftingGrid(String[] stackStrings) {
        ItemStack[] grid = new ItemStack[9];
        Arrays.fill(grid, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            if (stackStrings[i] != null)
                grid[i] = getItemFilter(stackStrings[i], false, false).getFilter();
        }
        craftingGrid = grid;
    }

    @Override
    public CraftingInventory getCraftingGrid() {
        CraftingInventory invCrafting = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity player) {
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

    @Override
    public boolean requiresTool(){
        return requiresTool;
    }

    @Override
    public void setRequiresTool(boolean requiresTool){
        this.requiresTool = requiresTool;
    }
}
