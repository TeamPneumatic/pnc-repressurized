package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import org.jspecify.annotations.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticElytraLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final ContextKey<Boolean> SHOW_ELYTRA = new ContextKey<>(RL("show_pneumatic_elytra"));


    private final ElytraModel elytraModel;
    private final ElytraModel elytraBabyModel;
    private final EquipmentLayerRenderer equipmentRenderer;
    // plain elytra stack, so the armor's own layer tint isn't applied to the wings
    private static final ItemStackTemplate ELYTRA = new ItemStackTemplate(Items.ELYTRA);

    public PneumaticElytraLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet, EquipmentLayerRenderer equipmentRenderer) {
        super(renderer);
        this.elytraModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
        this.elytraBabyModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (!state.getRenderDataOrDefault(SHOW_ELYTRA, false)) {
            return;
        }

        ElytraModel model = state.isBaby ? elytraBabyModel : elytraModel;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.WINGS, EquipmentAssets.ELYTRA, model, state, ELYTRA.create(),
                poseStack, submitNodeCollector, lightCoords, getPlayerElytraTexture(state), state.outlineColor, 0);
        poseStack.popPose();
    }

    private static @Nullable Identifier getPlayerElytraTexture(AvatarRenderState state) {
        PlayerSkin skin = state.skin;
        if (skin.elytra() != null) {
            return skin.elytra().texturePath();
        }
        if (skin.cape() != null && state.showCape) {
            return skin.cape().texturePath();
        }
        return null;
    }

    public static class StateModifier extends AvatarRenderStateModifier {
        @Override
        public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState) {
            boolean show = false;
            if (avatar instanceof Player player && player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PneumaticArmorItem) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                show = handler.upgradeUsable(CommonUpgradeHandlers.elytraHandler, true);
            }
            renderState.setRenderData(SHOW_ELYTRA, show);
        }
    }
}
