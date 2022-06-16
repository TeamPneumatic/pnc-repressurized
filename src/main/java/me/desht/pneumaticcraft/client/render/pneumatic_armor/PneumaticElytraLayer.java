package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PneumaticElytraLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends ElytraLayer<T, M> {
    public PneumaticElytraLayer(RenderLayerParent<T, M> entityRenderer, EntityModelSet modelSet) {
        super(entityRenderer, modelSet);
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        if (entity instanceof Player player && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PneumaticArmorItem) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            return handler.upgradeUsable(CommonUpgradeHandlers.elytraHandler, true);
        }
        return false;
    }
}
