package me.desht.pneumaticcraft.common.thirdparty.waila;

public class SemiblockProvider {

//    public static class Data implements IServerDataProvider<TileEntity> {
//        @Override
//        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity) {
//            CompoundNBT tag = new CompoundNBT();
//            SemiblockTracker.getInstance().getAllSemiblocks(world, tileEntity.getPos())
//                    .forEach((semiBlock) -> {
//                        NonNullList<ItemStack> drops = semiBlock.getDrops();
//                        if (!drops.isEmpty()) {
//                            tag.put(Integer.toString(semiBlock.getTrackingId()), semiBlock.serializeNBT(new CompoundNBT()));
//                        }
//                    });
//            compoundNBT.put("semiBlocks", tag);
//        }
//    }
//
//    public static class Component implements IComponentProvider {
//        @Override
//        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
//            CompoundNBT tag = accessor.getServerData().getCompound("semiBlocks");
//
//            for (String name : tag.keySet()) {
//                try {
//                    int entityId = Integer.parseInt(name);
//                    ISemiBlock entity = ISemiBlock.byTrackingId(accessor.getWorld(), entityId);
//                    if (entity != null) {
//                        tooltip.add(new StringTextComponent("[")
//                                .appendSibling(entity.getDisplayName())
//                                .appendSibling(new StringTextComponent("]"))
//                                .applyTextStyle(TextFormatting.YELLOW));
//                        entity.addTooltip(tooltip, accessor.getPlayer(), tag.getCompound(name), accessor.getPlayer().isSneaking());
//                    }
//                } catch (NumberFormatException ignored) {
//                }
//            }
//        }
//    }
}
