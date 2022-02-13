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

package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

public class TANModifierPNCBlock /*implements ITemperatureModifier*/ {
//    private static final PosCache modifierCache = new PosCache();

//    @Override
//    public Temperature applyEnvironmentModifiers(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
//        if (ConfigHandler.integration.tanHeatDivider == 0f) {
//            return initialTemperature;
//        }
//
//        if (world.getTotalWorldTime() % ConfigHandler.integration.tanRefreshInterval == 0) {
//            modifierCache.prune(world);
//        }
//
//        Temperature res = new Temperature(initialTemperature.getRawValue() + Math.round(modifierCache.getModifier(world, pos)));
//        monitor.addEntry(new IModifierMonitor.Context(this.getId(), "PneumaticCraft Heat Sources", initialTemperature, res));
//        return res;
//    }
//
//    @Override
//    public Temperature applyPlayerModifiers(@Nonnull PlayerEntity player, @Nonnull Temperature initialTemperature, @Nonnull IModifierMonitor monitor) {
//        return initialTemperature;
//    }
//
//    @Override
//    public boolean isPlayerSpecific() {
//        return false;
//    }
//
//    @Nonnull
//    @Override
//    public String getId() {
//        return "pneumaticcraft:temperature";
//    }
//
//    private static float getModifierAt(World world, BlockPos pos) {
//        float blockTemperatureModifier = 0.0F;
//
//        Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
//
//        BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos(pos);
//        for (int x = -3; x <= 3; x++) {
//            for (int y = -2; y <= 2; y++) {
//                for (int z = -3; z <= 3; z++) {
//                    pos2.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
//                    TileEntity te = world.getTileEntity(pos2);
//                    if (te instanceof IHeatExchanger) {
//                        Direction side;
//                        float div3 = 1f;
//                        if (te instanceof HeatSinkBlockEntity) {
//                            side = null;  // heat sinks radiate heat off in all directions
//                            div3 = 0.1f;  // heat sinks transfer heat very quickly, by design
//                        } else {
//                            Vec3d vec2 = new Vec3d(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);
//                            RayTraceResult rtr = new AxisAlignedBB(pos2).calculateIntercept(vec, vec2);
//                            side = rtr == null ? null : rtr.sideHit;
//                        }
//                        IHeatExchangerLogic logic = ((IHeatExchanger) te).getHeatExchangerLogic(side);
//                        if (logic != null && (logic.getTemperature() > 308 || logic.getTemperature() < 273)) {
//                            float div = Math.max(1, Math.abs(x) + Math.abs(y) + Math.abs(z));
//                            float div2 = side != null && world.isAirBlock(pos2.offset(side)) ? 1f : 3f;
//                            float mod = (float) (logic.getTemperature() - 273) / (ConfigHandler.integration.tanHeatDivider * div * div2 * div3);
//                            if (Math.abs(mod) > Math.abs(blockTemperatureModifier)) {
//                                blockTemperatureModifier = mod;
//                            }
//                        }
//                    }
//                    if (te instanceof AirCompressorBlockEntity && ((AirCompressorBlockEntity) te).isActive()
//                            || te instanceof LiquidCompressorBlockEntity && ((LiquidCompressorBlockEntity) te).isProducing) {
//                        blockTemperatureModifier += 3;
//                    }
//                }
//            }
//        }
//
//        return blockTemperatureModifier;
//    }
//
//    private static class TimeAndModifier {
//        private final long ticks;
//        private final float modifier;
//
//        private TimeAndModifier(long ticks, float modifier) {
//            this.ticks = ticks;
//            this.modifier = modifier;
//        }
//    }
//
//    private static class PosCache {
//        private final Map<Integer, Map<BlockPos, TimeAndModifier>> cache = new HashMap<>();
//
//        private void prune(World world) {
//            Map<BlockPos, TimeAndModifier> posMap = cache.get(world.provider.getDimension());
//            if (posMap != null) {
//                long now = world.getTotalWorldTime();
//                posMap.entrySet().removeIf(entry -> now - entry.getValue().ticks > ConfigHandler.integration.tanRefreshInterval);
//            }
//        }
//
//        private float getModifier(World world, BlockPos pos) {
//            Map<BlockPos, TimeAndModifier> posMap = cache.computeIfAbsent(world.provider.getDimension(), k -> new HashMap<>());
//            TimeAndModifier tm = posMap.get(pos);
//
//            if (tm == null) {
//                float modifier = getModifierAt(world, pos);
//                posMap.put(pos, new TimeAndModifier(world.getTotalWorldTime(), modifier));
//                return modifier;
//            } else {
//                return tm.modifier;
//            }
//        }
//    }
}
