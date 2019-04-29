package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import toughasnails.api.temperature.IModifierMonitor;
import toughasnails.api.temperature.ITemperatureModifier;
import toughasnails.api.temperature.Temperature;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class PNCBlockModifier implements ITemperatureModifier {
    private static final PosCache modifierCache = new PosCache();

    @Override
    public Temperature applyEnvironmentModifiers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
        if (ConfigHandler.integration.tanHeatDivider == 0f) return initialTemperature;

        int newTemperatureLevel = initialTemperature.getRawValue();

        float blockTemperatureModifier = modifierCache.getModifier(world, pos);

        newTemperatureLevel += blockTemperatureModifier;
        Temperature res = new Temperature(newTemperatureLevel);
        monitor.addEntry(new IModifierMonitor.Context(this.getId(), "PneumaticCraft Heat Sources", initialTemperature, res));
        return res;
    }

    @Override
    public Temperature applyPlayerModifiers(@Nonnull EntityPlayer player, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
        return initialTemperature;
    }

    @Override
    public boolean isPlayerSpecific() {
        return false;
    }

    @Nonnull
    @Override
    public String getId() {
        return "pneumaticcraft:temperature";
    }

    private static float getModifierAt(World world, BlockPos pos) {
        float blockTemperatureModifier = 0.0F;

        Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos(pos);
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    pos2.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    TileEntity te = world.getTileEntity(pos2);
                    if (te instanceof IHeatExchanger) {
                        EnumFacing side;
                        float div3 = 1f;
                        if (te instanceof TileEntityHeatSink) {
                            side = null;  // heat sinks radiate heat off in all directions
                            div3 = 0.1f;  // heat sinks transfer heat very quickly, by design
                        } else {
                            Vec3d vec2 = new Vec3d(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
                            RayTraceResult rtr = new AxisAlignedBB(pos2).calculateIntercept(vec, vec2);
                            side = rtr == null ? null : rtr.sideHit;
                        }
                        IHeatExchangerLogic logic = ((IHeatExchanger) te).getHeatExchangerLogic(side);
                        if (logic != null) {
                            float div = Math.max(1, Math.abs(x) + Math.abs(y) + Math.abs(z));
                            float div2 = side != null && world.isAirBlock(pos2.offset(side)) ? 1f : 3f;
                            float mod = (float) (logic.getTemperature() - 273) / (ConfigHandler.integration.tanHeatDivider * div * div2 * div3);
                            if (Math.abs(mod) > Math.abs(blockTemperatureModifier)) {
                                blockTemperatureModifier = mod;
                            }
                        }
                    }
                }
            }
        }

        return blockTemperatureModifier;
    }

    private static class TimeAndModifier {
        private final long ticks;
        private final float modifier;

        private TimeAndModifier(long ticks, float modifier) {
            this.ticks = ticks;
            this.modifier = modifier;
        }
    }

    private static class PosCache extends HashMap<String, TimeAndModifier> {
        private float getModifier(World world, BlockPos pos) {
            String k = world.provider.getDimension() + ":" + pos.toLong();
            TimeAndModifier tm = get(k);

            if (tm == null || world.getTotalWorldTime() - tm.ticks >= ConfigHandler.integration.tanRefreshInterval) {
                float modifier = getModifierAt(world, pos);
                put(k, new TimeAndModifier(world.getTotalWorldTime(), modifier));
                return modifier;
            }
            return tm.modifier;
        }
    }
}
