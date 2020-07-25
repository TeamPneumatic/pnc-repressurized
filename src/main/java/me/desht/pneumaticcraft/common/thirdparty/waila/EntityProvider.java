package me.desht.pneumaticcraft.common.thirdparty.waila;

public class EntityProvider {

//    public static class Data implements IServerDataProvider<Entity> {
//        @Override
//        public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, Entity entity) {
//            entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
//                    .ifPresent(h -> compoundNBT.putFloat("Pressure", h.getPressure()));
//            entity.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY)
//                    .ifPresent(h -> compoundNBT.putFloat("Temperature", h.getTemperatureAsInt()));
//            if (entity instanceof ISemiBlock) {
//                ((ISemiBlock) entity).serializeNBT(compoundNBT);
//            }
//        }
//    }
//
//    public static class Component implements IEntityComponentProvider {
//        @Override
//        public void appendHead(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
//            tooltip.add(accessor.getEntity().getDisplayName().applyTextStyle(TextFormatting.WHITE));
//        }
//
//        @Override
//        public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
//            if (accessor.getServerData().contains("Pressure")) {
//                float pressure = accessor.getServerData().getFloat("Pressure");
//                tooltip.add(new TranslationTextComponent("pneumaticcraft.gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1)));
//            }
//            if (accessor.getServerData().contains("Temperature")) {
//                tooltip.add(HeatUtil.formatHeatString(accessor.getServerData().getInt("Temperature")));
//            }
//            if (accessor.getEntity() instanceof ISemiBlock) {
//                ((ISemiBlock) accessor.getEntity()).addTooltip(tooltip, accessor.getPlayer(), accessor.getServerData(), accessor.getPlayer().isSneaking());
//            }
//        }
//
//        @Override
//        public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
//            String modName = ModNameCache.getModName(Names.MOD_ID);
//            tooltip.add(new StringTextComponent(modName).applyTextStyles(TextFormatting.BLUE, TextFormatting.ITALIC));
//        }
//    }
}
