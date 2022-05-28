package me.desht.pneumaticcraft.client.render.pneumatic_armor.entity_tracker;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IEntityTrackEntry;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderDroneAI;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EntityTrackEntryDrone implements IEntityTrackEntry {
    private RenderDroneAI droneAIRenderer;

    @Override
    public boolean isApplicable(Entity entity) {
        return entity instanceof AbstractDroneEntity;
    }

    public RenderDroneAI getDroneAIRenderer(AbstractDroneEntity drone) {
        if (droneAIRenderer == null) {
            droneAIRenderer = new RenderDroneAI(drone);
        }
        return droneAIRenderer;
    }

    @Override
    public void tick(Entity entity) {
        if (entity instanceof AbstractDroneEntity drone) {
            getDroneAIRenderer(drone).tick();
        }
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, Entity entity, float partialTicks) {
        if (entity instanceof AbstractDroneEntity drone) {
            getDroneAIRenderer(drone).render(matrixStack, buffer, partialTicks);
        }
    }

    @Override
    public void addInfo(Entity entity, List<Component> curInfo, boolean isLookingAtTarget) {
        AbstractDroneEntity droneBase = (AbstractDroneEntity) entity;
        curInfo.add(xlate("pneumaticcraft.entityTracker.info.tamed", droneBase.getOwnerName().getString()));
        curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.routine", droneBase.getLabel()));
        Player player = ClientUtils.getClientPlayer();
        if (DroneDebugClientHandler.enabledForPlayer(player)) {
            Component debugKey = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindDebuggingDrone);
            if (PneumaticArmorItem.isPlayerDebuggingDrone(player, droneBase)) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.debugging").withStyle(ChatFormatting.GOLD));
                Component optionsKey = ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindOpenOptions);
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.debugging.key", optionsKey).withStyle(ChatFormatting.GOLD));
                if (isLookingAtTarget) {
                    curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.stopDebugging.key", debugKey).withStyle(ChatFormatting.GOLD));
                }
            } else if (isLookingAtTarget) {
                curInfo.add(xlate("pneumaticcraft.entityTracker.info.drone.pressDebugKey", debugKey).withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
