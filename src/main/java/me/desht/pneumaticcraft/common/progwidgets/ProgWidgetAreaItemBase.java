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

package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.ChunkCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for widgets which use Area and Item Filter widgets
 */
public abstract class ProgWidgetAreaItemBase extends ProgWidget
        implements IAreaProvider, IEntityProvider, IItemFiltering, IVariableWidget {
    private List<BlockPos> areaListCache;
    private Set<BlockPos> areaSetCache;
    private AxisAlignedBB areaExtents;
    private Map<String, BlockPos> areaVariableStates;
    protected DroneAIManager aiManager;
    private boolean canCache = true;
    private EntityFilterPair<ProgWidgetAreaItemBase> entityFilters;

    public ProgWidgetAreaItemBase(ProgWidgetType<?> type) {
        super(type);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.AREA.get(), ModProgWidgets.ITEM_FILTER.get());
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.noArea"));
        }
        Set<BlockPos> areaSet = getCachedAreaSet();
        if (areaSet.size() > PNCConfig.Common.General.maxProgrammingArea) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.area.error.areaTooBig", PNCConfig.Common.General.maxProgrammingArea));
        }
        EntityFilterPair.addErrors(this, curInfo);
    }

    public ICollisionReader getChunkCache(World world) {
        AxisAlignedBB aabb = getAreaExtents();
        return new ChunkCache(world, new BlockPos(aabb.minX, aabb.minY, aabb.minZ), new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ));
    }

    public AxisAlignedBB getAreaExtents() {
        if (areaExtents == null) {
            areaExtents = calculateExtents(getCachedAreaSet());
        }
        return areaExtents;
    }

    private static AxisAlignedBB calculateExtents(Collection<BlockPos> areaSet) {
        if (areaSet.isEmpty()) return new AxisAlignedBB(BlockPos.ZERO, BlockPos.ZERO);

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : areaSet) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new AxisAlignedBB(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    public List<BlockPos> getCachedAreaList() {
        if (areaListCache != null) {
            if (!canCache || updateVariables()) {
                areaSetCache = new HashSet<>(areaListCache.size());
                getArea(areaSetCache);
                areaExtents = null;
                areaListCache = new ArrayList<>(areaSetCache.size());
                areaListCache.addAll(areaSetCache);
            }
        } else {
            areaExtents = null;
            areaSetCache = new HashSet<>();
            getArea(areaSetCache);
            areaListCache = new ArrayList<>(areaSetCache.size());
            areaListCache.addAll(areaSetCache);
            initializeVariableCache();
        }
        return areaListCache;
    }

    public Set<BlockPos> getCachedAreaSet() {
        getCachedAreaList();
        return areaSetCache;
    }

    protected synchronized void invalidateAreaCache() {
        areaListCache = null;
        areaSetCache = null;
        areaExtents = null;
    }

    private void initializeVariableCache() {
        areaVariableStates = new HashMap<>();
        ProgWidgetArea whitelistWidget = (ProgWidgetArea) getConnectedParameters()[0];
        ProgWidgetArea blacklistWidget = (ProgWidgetArea) getConnectedParameters()[getParameters().size()];
        if (whitelistWidget == null) return;
        ProgWidgetArea widget = whitelistWidget;
        while (widget != null) {
            if (!widget.type.isDeterministic()) canCache = false;
            if (aiManager != null) {
                if (!widget.getCoord1Variable().equals(""))
                    areaVariableStates.put(widget.getCoord1Variable(), aiManager.getCoordinate(widget.getCoord1Variable()));
                if (!widget.getCoord2Variable().equals(""))
                    areaVariableStates.put(widget.getCoord2Variable(), aiManager.getCoordinate(widget.getCoord2Variable()));
            }
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        while (widget != null) {
            if (!widget.type.isDeterministic()) canCache = false;
            if (aiManager != null) {
                if (!widget.getCoord1Variable().equals(""))
                    areaVariableStates.put(widget.getCoord1Variable(), aiManager.getCoordinate(widget.getCoord1Variable()));
                if (!widget.getCoord2Variable().equals(""))
                    areaVariableStates.put(widget.getCoord2Variable(), aiManager.getCoordinate(widget.getCoord2Variable()));
            }
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
    }

    private boolean updateVariables() {
        boolean varChanged = false;
        for (Map.Entry<String, BlockPos> entry : areaVariableStates.entrySet()) {
            BlockPos newValue = aiManager.getCoordinate(entry.getKey());
            if (!newValue.equals(entry.getValue())) {
                varChanged = true;
                entry.setValue(newValue);
            }
        }
        return varChanged;
    }

    @Override
    public void getArea(Set<BlockPos> area) {
        getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[getParameters().size()]);
    }

    public static void getArea(Set<BlockPos> area, ProgWidgetArea whitelistWidget, ProgWidgetArea blacklistWidget) {
        if (whitelistWidget == null) return;
        ProgWidgetArea widget = whitelistWidget;
        while (widget != null) {
            widget.getArea(area);
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        Set<BlockPos> blacklistedArea = new HashSet<>();
        while (widget != null) {
            widget.getArea(blacklistedArea);
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        area.removeAll(blacklistedArea);
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item) {
        return isItemValidForFilters(item, null);
    }

    public boolean isItemValidForFilters(ItemStack item, BlockState blockState) {
        return ProgWidgetItemFilter.isItemValidForFilters(item,
                ProgWidget.getConnectedWidgetList(this, 1, ModProgWidgets.ITEM_FILTER.get()),
                ProgWidget.getConnectedWidgetList(this, getParameters().size() + 1, ModProgWidgets.ITEM_FILTER.get()),
                blockState
        );
    }

    public boolean isItemFilterEmpty() {
        return getConnectedParameters()[1] == null && getConnectedParameters()[3] == null;
    }

    public List<Entity> getEntitiesInArea(World world, Predicate<? super Entity> filter) {
        return getEntitiesInArea(
                (ProgWidgetArea) getConnectedParameters()[0],
                (ProgWidgetArea) getConnectedParameters()[getParameters().size()],
                world, filter, null
        );
    }

    @Override
    public List<Entity> getValidEntities(World world) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.getValidEntities(world);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        if (entityFilters == null) {
            entityFilters = new EntityFilterPair<>(this);
        }
        return entityFilters.isEntityValid(entity);
    }

    public static List<Entity> getEntitiesInArea(ProgWidgetArea whitelistWidget, ProgWidgetArea blacklistWidget, World world,
                                                  Predicate<? super Entity> whitelistPredicate,
                                                  Predicate<? super Entity> blacklistPredicate) {
        if (whitelistWidget == null) return new ArrayList<>();
        Set<Entity> entities = new HashSet<>();
        ProgWidgetArea widget = whitelistWidget;
        if (whitelistPredicate == null) whitelistPredicate = e -> true;
        while (widget != null) {
            entities.addAll(widget.getEntitiesWithinArea(world, whitelistPredicate));
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        while (widget != null) {
            widget.getEntitiesWithinArea(world, whitelistPredicate).forEach(entities::remove);
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        if (blacklistPredicate != null) {
            entities.removeIf(blacklistPredicate);
        }
        return new ArrayList<>(entities);
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.EASY;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager) {
        this.aiManager = aiManager;
    }

    @Override
    public void addVariables(Set<String> variables) {
    }
}
