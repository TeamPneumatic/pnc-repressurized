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

package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.recipes.RecipeCache;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.DummyContainer;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter.EnumOldAreaType;
import me.desht.pneumaticcraft.common.util.StringFilterEntitySelector;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;

public class ProgWidgetCC extends ProgWidgetInventoryBase implements IBlockOrdered, IGotoWidget, IItemPickupWidget,
        IEntityProvider, ITextWidget, ICondition, IItemDropper, ILiquidFiltered, IRedstoneEmissionWidget,
        IRenamingWidget, ICraftingWidget, IMaxActions, IBlockRightClicker, ILiquidExport, ISignEditWidget,
        IToolUser, ICheckLineOfSight, IStandbyWidget {
    private Ordering order = Ordering.CLOSEST;
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
    private RightClickType clickType = RightClickType.CLICK_ITEM;
    private String measureVar = "";
    private boolean canSteal;
    private boolean checkSight;
    private boolean signBackSide;
    private boolean allowStandbyPickup;

    public ProgWidgetCC() {
        super(ModProgWidgets.COMPUTER_CONTROL.get());
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get());
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
        return new DroneAICC((DroneEntity) drone, (ProgWidgetCC) widget, false);
    }

    @Override
    public Goal getWidgetTargetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAICC((DroneEntity) drone, (ProgWidgetCC) widget, true);
    }

    Set<BlockPos> getInterfaceArea() {
        Set<BlockPos> area = new HashSet<>();
        getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[1]);
        return area;
    }

    @Override
    public void setOrder(Ordering order) {
        this.order = order;
    }

    @Override
    public Ordering getOrder() {
        return order;
    }

    String[] getAreaTypes() {
        String[] areaTypes = new String[EnumOldAreaType.values().length];
        Arrays.setAll(areaTypes, i -> EnumOldAreaType.values()[i].toString());
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
        EnumOldAreaType type = EnumOldAreaType.byName(areaType);
        if (type == null) {
            throw new IllegalArgumentException("Unknown area type: '" + areaType + "'. Use `getAreaTypes()` to list accepted values.");
        }
        ProgWidgetArea helperWidget = new ProgWidgetArea();
        helperWidget.setPos(0, new BlockPos(x1, y1, z1));
        helperWidget.setPos(1, new BlockPos(x2, y2, z2));
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
    public synchronized List<Entity> getValidEntities(Level world) {
        return ProgWidgetAreaItemBase.getEntitiesInArea(getEntityAreaWidget(), null, world, whitelistFilter, blacklistFilter);
    }

    private ProgWidgetArea getEntityAreaWidget() {
        ProgWidgetArea widget = new ProgWidgetArea();
        widget.setPos(0, getMinPos());
        widget.setPos(1, getMaxPos());
        return widget;
    }

    @Override
    public synchronized List<Entity> getEntitiesInArea(Level world, Predicate<? super Entity> filter) {
        return ProgWidgetAreaItemBase.getEntitiesInArea(getEntityAreaWidget(), null, world, filter, null);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        return (whitelistFilter == null || whitelistFilter.test(entity)) && (blacklistFilter == null || !blacklistFilter.test(entity));
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

    @Override
    public String getMeasureVar() {
        return measureVar;
    }

    @Override
    public void setMeasureVar(String var) {
        this.measureVar = var;
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
    public CraftingContainer getCraftingGrid() {
        CraftingContainer invCrafting = new TransientCraftingContainer(new DummyContainer(), 3, 3);
        for (int i = 0; i < 9; i++) {
            invCrafting.setItem(i, craftingGrid[i]);
        }
        return invCrafting;
    }

    @Override
    public Optional<CraftingRecipe> getRecipe(Level world, CraftingContainer grid) {
        return RecipeCache.CRAFTING.getCachedRecipe(world, grid);
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
    public RightClickType getClickType() {
        return clickType;
    }

    public void setClickType(RightClickType clickType) {
        this.clickType = clickType;
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
    public boolean isSignBackSide() {
        return signBackSide;
    }

    @Override
    public void setSignBackSide(boolean signBackSide) {
        this.signBackSide = signBackSide;
    }

    @Override
    public boolean requiresTool(){
        return requiresTool;
    }

    @Override
    public void setRequiresTool(boolean requiresTool){
        this.requiresTool = requiresTool;
    }

    @Override
    public boolean canSteal() {
        return canSteal;
    }

    @Override
    public void setCanSteal(boolean canSteal) {
        this.canSteal = canSteal;
    }

    @Override
    public void setCheckSight(boolean checkSight) {
        this.checkSight = checkSight;
    }

    @Override
    public boolean isCheckSight() {
        return checkSight;
    }

    @Override
    public boolean allowPickupOnStandby() {
        return allowStandbyPickup;
    }

    @Override
    public void setAllowStandbyPickup(boolean allowStandbyPickup) {
        this.allowStandbyPickup = allowStandbyPickup;
    }
}
