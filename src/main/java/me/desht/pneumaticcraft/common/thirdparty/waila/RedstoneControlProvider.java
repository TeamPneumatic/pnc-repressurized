package me.desht.pneumaticcraft.common.thirdparty.waila;

public class RedstoneControlProvider {
//    public static class Data implements IServerDataProvider<TileEntity> {
//        @Override
//        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
//            if (te instanceof IRedstoneControl) {
//                compoundNBT.putInt("redstoneMode", ((IRedstoneControl) te).getRedstoneMode());
//            }
//        }
//    }
//
//    public static class Component implements IComponentProvider {
//        @Override
//        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
//            CompoundNBT tag = accessor.getServerData();
//            // This is used so that we can split values later easier and have them all in the same layout.
//            Map<String, String> values = new HashMap<>();
//
//            if (tag.contains("redstoneMode")) {
//                int mode = tag.getInt("redstoneMode");
//                TileEntity te = accessor.getTileEntity();
//                if (te instanceof TileEntityBase) {
//                    values.put(((TileEntityBase) te).getRedstoneTabTitle(), ((TileEntityBase) te).getRedstoneButtonText(mode));
//                }
//            }
//
//            // Get all the values from the map and put them in the list.
//            values.forEach((k, v) -> tooltip.add(
//                    new TranslationTextComponent(k)
//                            .appendText(": ")
//                            .appendSibling(new TranslationTextComponent(v).applyTextStyle(TextFormatting.RED))
//            ));
//        }
//    }
}
