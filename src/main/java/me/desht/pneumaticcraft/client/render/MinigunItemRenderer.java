package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Renders the minigun item in first and third person. The item model ({@code items/minigun.json}) selects on the
 * display context: other contexts use the plain block model, and first/third person use this special model with
 * {@code third_person} set accordingly. The base model of the special model has no display transforms, since all
 * positioning is done here.
 */
public class MinigunItemRenderer implements SpecialModelRenderer<MinigunItemRenderer.Argument> {
    private final ModelMinigun model;
    private final boolean thirdPerson;

    private MinigunItemRenderer(ModelMinigun model, boolean thirdPerson) {
        this.model = model;
        this.thirdPerson = thirdPerson;
    }

    @Override
    public @Nullable Argument extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof MinigunItem itemMinigun && stack.has(ModDataComponents.OWNER_ENTITY_ID)) {
            int id = stack.getOrDefault(ModDataComponents.OWNER_ENTITY_ID, 0);
            if (ClientUtils.getClientLevel().getEntity(id) instanceof Player player) {
                Minecraft mc = Minecraft.getInstance();
                ModelMinigun.MinigunRenderState state = new ModelMinigun.MinigunRenderState();
                state.extract(itemMinigun.getMinigun(stack, player), mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                return new Argument(state, mc.screen instanceof InventoryScreen, player.getOffhandItem() == stack, mc.options.mainHand().get());
            }
        }
        return null;
    }

    @Override
    public void submit(@Nullable Argument argument, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }
        matrixStack.pushPose();
        if (thirdPerson) {
            if (argument.inInventoryScreen()) {
                // our own gun in the rendered player model in inventory screen
                matrixStack.mulPose(Axis.XP.rotationDegrees(-180f));
                matrixStack.translate(0.5, -1, -0.5);
            } else {
                // rendering our own gun in 3rd person, or rendering someone else's gun
                matrixStack.scale(1f, -1f, -1f);
                matrixStack.mulPose(Axis.XP.rotationDegrees(75f));
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(0f));
                matrixStack.translate(-0.5, -2, -0.3);
            }
        } else {
            // Hides minigun in first person if in offhand because it's not usable in offhand anyway
            if (argument.offhand()) {
                matrixStack.scale(0f, 0f, 0f);
            }

            // Shows minigun in main hand appropriate to which side is set as the main hand
            else {
                // our own gun in 1st person
                matrixStack.scale(1.5f, 1.5f, 1.5f);
                matrixStack.mulPose(Axis.XP.rotationDegrees(0));
                matrixStack.mulPose(Axis.YP.rotationDegrees(0));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
                if (argument.mainArm() == HumanoidArm.RIGHT) {
                    matrixStack.translate(-1, -1.7, 0.1);
                } else {
                    matrixStack.translate(0.4, -1.7, 0.1);
                }
            }
        }
        model.submitMinigun(matrixStack, submitNodeCollector, lightCoords, overlayCoords, argument.state(), false, null);
        matrixStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // only used in first and third person, never in GUIs
    }

    public record Argument(ModelMinigun.MinigunRenderState state, boolean inInventoryScreen, boolean offhand, HumanoidArm mainArm) {
    }

    public record Unbaked(boolean thirdPerson) implements SpecialModelRenderer.Unbaked<Argument> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.BOOL.fieldOf("third_person").forGetter(Unbaked::thirdPerson)
        ).apply(builder, Unbaked::new));

        @Override
        public SpecialModelRenderer<Argument> bake(BakingContext context) {
            return new MinigunItemRenderer(new ModelMinigun(context.entityModelSet().bakeLayer(PNCModelLayers.MINIGUN)), thirdPerson);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
