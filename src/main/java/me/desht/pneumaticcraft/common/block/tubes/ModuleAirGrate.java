package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.util.RangeLines;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.*;

public class ModuleAirGrate extends TubeModule {
    private int grateRange;
    private boolean vacuum;
    private final Set<TileEntityHeatSink> heatSinks = new HashSet<>();
    private final RangeLines rangeLineRenderer = new RangeLines(0x5500FF00);
    private boolean resetRendering = true;
    private EntityFilter entityFilter = null;
    private TileEntity adjacentInsertionTE = null;
    private Direction adjacentInsertionSide;
    private Map<BlockPos,Boolean> traceabilityCache = new HashMap<>();

    public ModuleAirGrate(ItemTubeModule itemTubeModule) {
        super(itemTubeModule);
    }

    private int getRange() {
        float range = pressureTube.getPressure() * 4f;
        vacuum = range < 0;
        if (vacuum) range *= -4f;
        return (int) range;
    }

    public int getGrateRange() {
        return grateRange;
    }

    @Override
    public double getWidth() {
        return 16D;
    }

    public RangeLines getRangeLineRenderer() {
        return rangeLineRenderer;
    }

    @Override
    public void update() {
        super.update();

        World world = pressureTube.getWorld();
        BlockPos pos = pressureTube.getPos();

        if ((world.getGameTime() & 0x1f) == 0) traceabilityCache.clear();

        if (!world.isRemote) {
            int oldGrateRange = grateRange;
            grateRange = getRange();
            pressureTube.addAir((vacuum ? 1 : -1) * grateRange * PneumaticValues.USAGE_AIR_GRATE);
            if (oldGrateRange != grateRange) sendDescriptionPacket();

            coolHeatSinks();
        } else {
            if (resetRendering) {
                rangeLineRenderer.startRendering(grateRange);
                resetRendering = false;
            }
            rangeLineRenderer.tick(world.rand);
        }

        pushEntities(world, pos, new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }

    private AxisAlignedBB getAffectedAABB() {
        return new AxisAlignedBB(pressureTube.getPos().offset(getDirection(), grateRange + 1)).grow(grateRange);
    }

    private void pushEntities(World world, BlockPos pos, Vec3d tileVec) {
        AxisAlignedBB bbBox = getAffectedAABB();
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, bbBox, entityFilter);
        double d0 = grateRange * 3;
        for (Entity entity : entities) {
            if (!entity.world.isRemote && entity instanceof ItemEntity && entity.isAlive()
                    && entity.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 1D) {
                tryItemInsertion((ItemEntity) entity);
            } else if (!ignoreEntity(entity) && rayTraceOK(entity, tileVec)) {
                double x = (entity.getPosX() - pos.getX() - 0.5D) / d0;
                double y = (entity.getPosY() + entity.getEyeHeight() - pos.getY() - 0.5D) / d0;
                BlockPos entityPos = entity.getPosition();
                if (!Block.hasSolidSide(world.getBlockState(entityPos), world, entityPos, Direction.UP) && !world.isAirBlock(entityPos)) {
                    y -= 0.15;  // kludge: avoid entities getting stuck on edges, e.g. farmland->full block
                }
                double z = (entity.getPosZ() - pos.getZ() - 0.5D) / d0;
                double d4 = Math.sqrt(x * x + y * y + z * z);
                double d5 = 1.0D - d4;

                if (d5 > 0.0D) {
                    d5 *= d5;
                    if (vacuum) d5 *= -1;
                    entity.move(MoverType.SELF, new Vec3d(x * d5, y * d5, z * d5));
                    if (world.isRemote && world.rand.nextDouble() * 0.85 > d4) {
                        if (vacuum) {
                            world.addParticle(AirParticleData.DENSE, entity.getPosX(), entity.getPosY(), entity.getPosZ(), -x, -y, -z);
                        } else {
                            world.addParticle(AirParticleData.DENSE, pos.getX() + 0.5 + x, pos.getY() + 0.5 + y, pos.getZ() + 0.5 + z, x, y, z);
                        }
                    }
                }
            }
        }
    }

    private boolean ignoreEntity(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return ((PlayerEntity) entity).isCreative() || entity.isSteppingCarefully();
        } else {
            return false;
        }
    }

    private boolean rayTraceOK(Entity entity, Vec3d tileVec) {
        BlockPos pos = new BlockPos(entity.getEyePosition(0f));
        return traceabilityCache.computeIfAbsent(pos, k -> {
            Vec3d entityVec = new Vec3d(entity.getPosX(), entity.getPosY() + entity.getEyeHeight(), entity.getPosZ());
            RayTraceContext ctx = new RayTraceContext(entityVec, tileVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
            BlockRayTraceResult trace = entity.getEntityWorld().rayTraceBlocks(ctx);
            return trace != null && trace.getPos().equals(pressureTube.getPos());
        });
    }

    private void tryItemInsertion(ItemEntity entity) {
        if (getAdjacentInventory() != null) {
            ItemStack stack = entity.getItem();
            ItemStack excess = IOHelper.insert(getAdjacentInventory(), stack, adjacentInsertionSide, false);
            if (excess.isEmpty()) {
                entity.remove();
            } else {
                entity.setItem(excess);
            }
        }
    }

    private TileEntity getAdjacentInventory() {
        if (adjacentInsertionTE != null && !adjacentInsertionTE.isRemoved()) {
            return adjacentInsertionTE;
        }

        adjacentInsertionTE = null;
        for (Direction dir : Direction.VALUES) {
            TileEntity inv = pressureTube.getWorld().getTileEntity(pressureTube.getPos().offset(dir));
            if (inv != null && inv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).isPresent()) {
                adjacentInsertionTE = inv;
                adjacentInsertionSide = dir.getOpposite();
                break;
            }
        }
        return adjacentInsertionTE;
    }

    private void coolHeatSinks() {
        if (grateRange > 2) {
            int curTeIndex = (int) (pressureTube.getWorld().getGameTime() % 27);
            BlockPos curPos = pressureTube.getPos().offset(dir, 2).add(-1 + curTeIndex % 3, -1 + curTeIndex / 3 % 3, -1 + curTeIndex / 9 % 3);
            TileEntity te = pressureTube.getWorld().getTileEntity(curPos);
            if (te instanceof TileEntityHeatSink) heatSinks.add((TileEntityHeatSink) te);

            Iterator<TileEntityHeatSink> iterator = heatSinks.iterator();
            while (iterator.hasNext()) {
                TileEntityHeatSink heatSink = iterator.next();
                if (heatSink.isRemoved()) {
                    iterator.remove();
                } else {
                    for (int i = 0; i < 4; i++)
                        heatSink.onFannedByAirGrate();
                }
            }
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        vacuum = tag.getBoolean("vacuum");
        grateRange = tag.getInt("grateRange");
        String f = tag.getString("entityFilter");
        entityFilter = f.isEmpty() ? null : EntityFilter.fromString(f);
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("vacuum", vacuum);
        tag.putInt("grateRange", grateRange);
        tag.putString("entityFilter", entityFilter == null ? "" : entityFilter.toString());
    }

    @Override
    public void addInfo(List<ITextComponent> curInfo) {
        super.addInfo(curInfo);
        String txt = grateRange == 0 ? "Idle" : vacuum ? "Attracting" : "Repelling";
        curInfo.add(new StringTextComponent("Status: ").appendText(txt).applyTextStyle(TextFormatting.WHITE));
        curInfo.add(new StringTextComponent("Range: ").appendText(grateRange + " blocks").applyTextStyle(TextFormatting.WHITE));
        if (entityFilter != null)
            curInfo.add(new StringTextComponent("Entity Filter: \"").appendText(entityFilter.toString()).appendText("\"").applyTextStyle(TextFormatting.WHITE));
    }

    @Override
    public boolean hasGui() {
        return upgraded;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getAffectedAABB();
    }

    public String getEntityFilterString() {
        return entityFilter == null ? "" : entityFilter.toString();
    }

    public void setEntityFilter(String filter) {
        entityFilter = EntityFilter.fromString(filter);
    }

    @Override
    public boolean onActivated(PlayerEntity player, Hand hand) {
        if (player.world.isRemote && !rangeLineRenderer.shouldRender()) {
            resetRendering = true;
        }
        return super.onActivated(player, hand);
    }
}
