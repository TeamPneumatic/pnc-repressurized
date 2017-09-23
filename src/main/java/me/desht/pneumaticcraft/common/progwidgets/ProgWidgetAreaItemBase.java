package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.base.Predicate;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetAreaShow;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public abstract class ProgWidgetAreaItemBase extends ProgWidget implements IAreaProvider, IEntityProvider,
        IItemFiltering, IVariableWidget {
    private List<BlockPos> areaListCache;
    private Set<BlockPos> areaSetCache;
    private Map<String, BlockPos> areaVariableStates;
    protected DroneAIManager aiManager;
    private boolean canCache = true;

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetArea.class, ProgWidgetItemFilter.class};
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) {
            curInfo.add("gui.progWidget.area.error.noArea");
        }
    }

    public static IBlockAccess getCache(Collection<BlockPos> area, World world) {
        if (area.size() == 0) return world;
        int minX, minY, minZ, maxX, maxY, maxZ;
        Iterator<BlockPos> iterator = area.iterator();
        BlockPos p = iterator.next();
        minX = maxX = p.getX();
        minY = maxY = p.getY();
        minZ = maxZ = p.getZ();
        while (iterator.hasNext()) {
            p = iterator.next();
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }
        return new ChunkCache(world, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), 0);
    }

    public List<BlockPos> getCachedAreaList() {
        if (areaListCache != null) {
            if (!canCache || updateVariables()) {
                areaSetCache = new HashSet<BlockPos>(areaListCache.size());
                getArea(areaSetCache);
                areaListCache = new ArrayList<BlockPos>(areaSetCache.size());
                areaListCache.addAll(areaSetCache);
            }
        } else {
            areaSetCache = new HashSet<BlockPos>();
            getArea(areaSetCache);
            areaListCache = new ArrayList<BlockPos>(areaSetCache.size());
            areaListCache.addAll(areaSetCache);
            initializeVariableCache();
        }
        return areaListCache;
    }

    public Set<BlockPos> getCachedAreaSet() {
        getCachedAreaList();
        return areaSetCache;
    }

    private void initializeVariableCache() {
        areaVariableStates = new HashMap<String, BlockPos>();
        ProgWidgetArea whitelistWidget = (ProgWidgetArea) getConnectedParameters()[0];
        ProgWidgetArea blacklistWidget = (ProgWidgetArea) getConnectedParameters()[getParameters().length];
        if (whitelistWidget == null) return;
        ProgWidgetArea widget = whitelistWidget;
        while (widget != null) {
            if (widget.type == ProgWidgetArea.EnumAreaType.RANDOM) canCache = false;
            if (!widget.getCoord1Variable().equals(""))
                areaVariableStates.put(widget.getCoord1Variable(), aiManager.getCoordinate(widget.getCoord1Variable()));
            if (!widget.getCoord2Variable().equals(""))
                areaVariableStates.put(widget.getCoord2Variable(), aiManager.getCoordinate(widget.getCoord2Variable()));
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        while (widget != null) {
            if (widget.type == ProgWidgetArea.EnumAreaType.RANDOM) canCache = false;
            if (!widget.getCoord1Variable().equals(""))
                areaVariableStates.put(widget.getCoord1Variable(), aiManager.getCoordinate(widget.getCoord1Variable()));
            if (!widget.getCoord2Variable().equals(""))
                areaVariableStates.put(widget.getCoord2Variable(), aiManager.getCoordinate(widget.getCoord2Variable()));
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
        getArea(area, (ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[getParameters().length]);
    }

    public static void getArea(Set<BlockPos> area, ProgWidgetArea whitelistWidget, ProgWidgetArea blacklistWidget) {
        if (whitelistWidget == null) return;
        ProgWidgetArea widget = whitelistWidget;
        while (widget != null) {
            widget.getArea(area);
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        Set<BlockPos> blacklistedArea = new HashSet<BlockPos>();
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

    public boolean isItemValidForFilters(ItemStack item, IBlockState blockState) {
        return ProgWidgetItemFilter.isItemValidForFilters(item, ProgWidget.getConnectedWidgetList(this, 1), ProgWidget.getConnectedWidgetList(this, getParameters().length + 1), blockState);
    }

    public boolean isItemFilterEmpty() {
        return getConnectedParameters()[1] == null && getConnectedParameters()[3] == null;
    }

    public List<Entity> getEntitiesInArea(World world, Predicate<? super Entity> filter) {
        return getEntitiesInArea((ProgWidgetArea) getConnectedParameters()[0], (ProgWidgetArea) getConnectedParameters()[getParameters().length], world, filter, null);
    }

    public static List<Entity> getValidEntities(World world, IProgWidget widget) {
        StringFilterEntitySelector whitelistFilter = getEntityFilter((ProgWidgetString) widget.getConnectedParameters()[1], true);
        StringFilterEntitySelector blacklistFilter = getEntityFilter((ProgWidgetString) widget.getConnectedParameters()[widget.getParameters().length + 1], false);
        return getEntitiesInArea((ProgWidgetArea) widget.getConnectedParameters()[0], (ProgWidgetArea) widget.getConnectedParameters()[widget.getParameters().length], world, whitelistFilter, blacklistFilter);
    }

    @Override
    public List<Entity> getValidEntities(World world) {
        return getValidEntities(world, this);
    }

    public static boolean isEntityValid(Entity entity, IProgWidget widget) {
        StringFilterEntitySelector whitelistFilter = getEntityFilter((ProgWidgetString) widget.getConnectedParameters()[1], true);
        StringFilterEntitySelector blacklistFilter = getEntityFilter((ProgWidgetString) widget.getConnectedParameters()[widget.getParameters().length + 1], false);
        return whitelistFilter.apply(entity) && !blacklistFilter.apply(entity);
    }

    @Override
    public boolean isEntityValid(Entity entity) {
        return isEntityValid(entity, this);
    }

    public static StringFilterEntitySelector getEntityFilter(ProgWidgetString widget, boolean allowEntityIfNoFilter) {
        StringFilterEntitySelector filter = new StringFilterEntitySelector();
        if (widget != null) {
            while (widget != null) {
                filter.addEntry(widget.string);
                widget = (ProgWidgetString) widget.getConnectedParameters()[0];
            }
        } else if (allowEntityIfNoFilter) {
            filter.setFilter("");
        }
        return filter;
    }

    public static List<Entity> getEntitiesInArea(ProgWidgetArea whitelistWidget, ProgWidgetArea blacklistWidget, World world, Predicate<? super Entity> whitelistPredicate, Predicate<? super Entity> blacklistPredicate) {
        if (whitelistWidget == null) return new ArrayList<Entity>();
        Set<Entity> entities = new HashSet<Entity>();
        ProgWidgetArea widget = whitelistWidget;
        while (widget != null) {
            entities.addAll(widget.getEntitiesWithinArea(world, whitelistPredicate));
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        widget = blacklistWidget;
        while (widget != null) {
            entities.removeAll(widget.getEntitiesWithinArea(world, whitelistPredicate));
            widget = (ProgWidgetArea) widget.getConnectedParameters()[0];
        }
        if (blacklistPredicate != null) {
            Iterator<Entity> iterator = entities.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (!blacklistPredicate.apply(entity)) iterator.remove();
            }
        }
        return new ArrayList<Entity>(entities);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetAreaShow(this, guiProgrammer);
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
