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

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.block.entity.HeatSinkBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.RangeManager;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractSemiblockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdatePressureBlock;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AirGrateModule extends AbstractTubeModule {
    private int grateRange;
    private boolean vacuum;
    private final Set<HeatSinkBlockEntity> heatSinks = new HashSet<>();
    private boolean showRange;
    @Nonnull
    private EntityFilter entityFilter = EntityFilter.allow();
    private final Map<BlockPos,Boolean> traceabilityCache = new HashMap<>();

    private LazyOptional<IItemHandler> itemInsertionCap = null; // null = "unknown", LazyOptional.empty() = "known absent"
    private LazyOptional<IFluidHandler> fluidInsertionCap = null;

    public AirGrateModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    @Override
    public double getWidth() {
        return 16D;
    }

    @Override
    public Item getItem() {
        return ModItems.AIR_GRATE_MODULE.get();
    }

    @Override
    public void tickCommon() {
        Level world = pressureTube.nonNullLevel();
        BlockPos pos = pressureTube.getBlockPos();

        if ((world.getGameTime() & 0x1f) == 0) traceabilityCache.clear();

        int oldGrateRange = grateRange;
        grateRange = calculateRange();
        if (oldGrateRange != grateRange) {
            onGrateRangeChanged();
        }

        Vec3 tileVec = Vec3.atCenterOf(pos).add(getDirection().getStepX() * 0.49, getDirection().getStepY() * 0.49, getDirection().getStepZ() * 0.49);
        pushEntities(world, pos, tileVec);
    }

    private void onGrateRangeChanged() {
        if (!pressureTube.nonNullLevel().isClientSide) {
            getTube().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY)
                    .ifPresent(h -> NetworkHandler.sendToAllTracking(new PacketUpdatePressureBlock(getTube(), null, h.getSideLeaking(), h.getAir()), getTube()));
        } else {
            if (showRange) {
                AreaRenderManager.getInstance().showArea(RangeManager.getFrame(getAffectedAABB()), 0x60FFC060, pressureTube, false);
            }
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        coolHeatSinks();
    }

    @Override
    public void onNeighborBlockUpdate() {
        itemInsertionCap = null;
        fluidInsertionCap = null;
    }

    private AABB getAffectedAABB() {
        BlockPos pos = pressureTube.getBlockPos().relative(getDirection(), grateRange + 1);
        return new AABB(pos).inflate(grateRange);
    }

    private int calculateRange() {
        float range = pressureTube.getPressure() * 4f;
        vacuum = range < 0;
        if (vacuum) range *= -4f;
        return (int) range;
    }

    private void pushEntities(Level world, BlockPos pos, Vec3 traceVec) {
        AABB bbBox = getAffectedAABB();
        List<Entity> entities = world.getEntitiesOfClass(Entity.class, bbBox, entityFilter);
        double d0 = grateRange * 3;
        int entitiesMoved = 0;
        for (Entity entity : entities) {
            if (ignoreEntity(entity) || !entity.isAlive() || !rayTraceOK(entity, traceVec)) {
                continue;
            }
            if (!entity.level().isClientSide) {
                tryInsertion(traceVec, entity);
            }
            double x = (entity.getX() - pos.getX() - 0.5D) / d0;
            double y = (entity.getY() + entity.getEyeHeight() - pos.getY() - 0.5D) / d0;
            BlockPos entityPos = entity.blockPosition();
            if (!Block.canSupportCenter(world, entityPos, Direction.UP) && !world.isEmptyBlock(entityPos)) {
                y -= 0.15;  // kludge: avoid entities getting stuck on edges, e.g. farmland->full block
            }
            double z = (entity.getZ() - pos.getZ() - 0.5D) / d0;
            double d4 = Math.sqrt(x * x + y * y + z * z);
            double d5 = 1.0D - d4;

            if (d5 > 0.0D) {
                d5 *= d5;
                if (vacuum) d5 *= -1;
                if (entity.onGround() && entity instanceof ItemEntity) entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.25, 0));
                entity.move(MoverType.SELF, new Vec3(x * d5, y * d5, z * d5));
                entitiesMoved++;
                if (world.isClientSide && world.random.nextDouble() < 0.2) {
                    if (vacuum) {
                        world.addParticle(AirParticleData.DENSE, entity.getX(), entity.getY(), entity.getZ(), -x, -y, -z);
                    } else {
                        world.addParticle(AirParticleData.DENSE, pos.getX() + 0.5 + getDirection().getStepX(), pos.getY() + 0.5 + getDirection().getStepY(), pos.getZ() + 0.5 + getDirection().getStepZ(), x, y, z);
                    }
                }
            }
        }
        if (!world.isClientSide) {
            int usage = pressureTube.getPressure() > 0 ? -PneumaticValues.USAGE_AIR_GRATE : PneumaticValues.USAGE_AIR_GRATE;
            pressureTube.addAir(entitiesMoved * usage);
        }
    }

    private void tryInsertion(Vec3 traceVec, Entity entity) {
        if (entity instanceof ItemEntity && isCloseEnough(entity, traceVec)) {
            tryItemInsertion((ItemEntity) entity);
        } else if (entity instanceof ExperienceOrb && isCloseEnough(entity, traceVec)) {
            tryOrbInsertion((ExperienceOrb) entity);
        }
    }

    private void tryItemInsertion(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        getItemInsertionCap().ifPresent(handler -> {
            ItemStack excess = ItemHandlerHelper.insertItem(handler, stack, false);
            if (excess.isEmpty()) {
                entity.discard();
            } else {
                entity.setItem(excess);
            }
        });
    }

    private void tryOrbInsertion(ExperienceOrb entity) {
        getFluidInsertionCap().ifPresent(handler -> {
            if (PneumaticCraftUtils.fillTankWithOrb(handler, entity, IFluidHandler.FluidAction.EXECUTE)) {
                entity.discard();
            }
        });
    }

    private boolean isCloseEnough(Entity entity, Vec3 traceVec) {
        return entity.distanceToSqr(traceVec) < 1D;
    }

    private boolean ignoreEntity(Entity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).isCreative() || entity.isShiftKeyDown() || entity.isSpectator();
        }
        if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
            return false;
        }
        // don't touch semiblocks, at all
        return !entity.isPushable() || entity instanceof AbstractSemiblockEntity;
    }

    private boolean rayTraceOK(Entity entity, Vec3 traceVec) {
        BlockPos pos = BlockPos.containing(entity.getEyePosition(0f));
        return traceabilityCache.computeIfAbsent(pos, k -> {
            Vec3 entityVec = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
            ClipContext ctx = new ClipContext(entityVec, traceVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
            BlockHitResult trace = entity.getCommandSenderWorld().clip(ctx);
            return trace.getBlockPos().equals(pressureTube.getBlockPos());
        });
    }

    private LazyOptional<IItemHandler> getItemInsertionCap() {
        if (itemInsertionCap == null) {
            for (Direction dir : DirectionUtil.VALUES) {
                BlockEntity te = pressureTube.nonNullLevel().getBlockEntity(pressureTube.getBlockPos().relative(dir));
                if (te != null) {
                    LazyOptional<IItemHandler> cap = te.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite());
                    // bit of a kludge: exclude BE's which also offer a fluid capability on this side
                    if (cap.isPresent() && !te.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).isPresent()) {
                        itemInsertionCap = cap;
                        itemInsertionCap.addListener(l -> itemInsertionCap = null);
                        break;
                    }
                }
            }
            if (itemInsertionCap == null) itemInsertionCap = LazyOptional.empty();
        }
        return itemInsertionCap;
    }

    private LazyOptional<IFluidHandler> getFluidInsertionCap() {
        if (fluidInsertionCap == null) {
            for (Direction dir : DirectionUtil.VALUES) {
                BlockEntity te = pressureTube.nonNullLevel().getBlockEntity(pressureTube.getBlockPos().relative(dir));
                if (te != null) {
                    LazyOptional<IFluidHandler> cap = te.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
                    if (cap.isPresent()) {
                        fluidInsertionCap = cap;
                        fluidInsertionCap.addListener(l -> fluidInsertionCap = null);
                        break;
                    }
                }
            }
            if (fluidInsertionCap == null) fluidInsertionCap = LazyOptional.empty();
        }
        return fluidInsertionCap;
    }

    private void coolHeatSinks() {
        if (grateRange >= 2) {
            int curTeIndex = (int) (pressureTube.nonNullLevel().getGameTime() % 27);
            BlockPos curPos = pressureTube.getBlockPos().relative(dir, 2).offset(-1 + curTeIndex % 3, -1 + curTeIndex / 3 % 3, -1 + curTeIndex / 9 % 3);
            BlockEntity te = pressureTube.nonNullLevel().getBlockEntity(curPos);
            if (te instanceof HeatSinkBlockEntity) heatSinks.add((HeatSinkBlockEntity) te);

            Iterator<HeatSinkBlockEntity> iterator = heatSinks.iterator();
            int tubesCooled = 0;
            while (iterator.hasNext()) {
                HeatSinkBlockEntity heatSink = iterator.next();
                if (heatSink.isRemoved()) {
                    iterator.remove();
                } else {
                    for (int i = 0; i < 4; i++) {
                        heatSink.onFannedByAirGrate();
                    }
                    tubesCooled++;
                }
            }
            if (tubesCooled > 0) {
                pressureTube.addAir(-(5 + (tubesCooled / 3)));
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        vacuum = tag.getBoolean("vacuum");
        grateRange = tag.getInt("grateRange");
        String f = tag.getString("entityFilter");
        entityFilter = f.isEmpty() ? EntityFilter.allow() : EntityFilter.fromString(f, EntityFilter.allow());
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putBoolean("vacuum", vacuum);
        tag.putInt("grateRange", grateRange);
        if (entityFilter != EntityFilter.allow()) {
            tag.putString("entityFilter", entityFilter.toString());
        }
        return tag;
    }

    @Override
    public void addInfo(List<Component> curInfo) {
        super.addInfo(curInfo);
        String k = grateRange == 0 ? "idle" : vacuum ? "attracting" : "repelling";
        curInfo.add(xlate("pneumaticcraft.waila.airGrateModule." + k).withStyle(ChatFormatting.WHITE));
        if (grateRange != 0) {
            curInfo.add(xlate("pneumaticcraft.message.misc.range", grateRange).withStyle(ChatFormatting.WHITE));
        }
        if (entityFilter != EntityFilter.allow()) {
            curInfo.add(xlate("pneumaticcraft.gui.entityFilter.show", entityFilter.toString()).withStyle(ChatFormatting.WHITE));
        }
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(pressureTube.getBlockPos().relative(getDirection(), grateRange + 1)).inflate(grateRange * 2);
    }

    @Nonnull
    public EntityFilter getEntityFilter() {
        return entityFilter;
    }

    public void setEntityFilter(@Nonnull EntityFilter filter) {
        this.entityFilter = filter;
        setChanged();
    }

    public boolean isShowRange() {
        return showRange;
    }

    public void setShowRange(boolean showRange) {
        this.showRange = showRange;
        if (showRange) {
            AreaRenderManager.getInstance().showArea(RangeManager.getFrame(getAffectedAABB()), 0x60FFC060, pressureTube, false);
        } else {
            AreaRenderManager.getInstance().removeHandlers(pressureTube);
        }
        setChanged();
    }

    @Override
    public void onRemoved() {
        if (pressureTube.nonNullLevel().isClientSide) {
            AreaRenderManager.getInstance().removeHandlers(pressureTube);
        }
    }
}
