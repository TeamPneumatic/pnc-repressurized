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

package me.desht.pneumaticcraft.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.client.gui.MicromissileScreen;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.MicromissileDefaults;
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class MicromissilesItem extends Item {
    public MicromissilesItem() {
        super(ModItems.defaultProps()
                .stacksTo(1)
                .durability(100)
                .repairable(Items.TNT)
                .component(ModDataComponents.MICROMISSILE_SETTINGS, Settings.DEFAULT)
        );
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().micromissiles.missilePodSize.get();
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (stack.get(ModDataComponents.MICROMISSILE_SETTINGS) == Settings.DEFAULT) {
            stack.set(ModDataComponents.MICROMISSILE_SETTINGS, MicromissileDefaults.INSTANCE.getDefaults(playerIn));
        }

        if (playerIn.isShiftKeyDown()) {
            if (worldIn.isClientSide()) {
                MicromissileScreen.openGui(stack.getHoverName(), handIn);
            }
            return InteractionResult.SUCCESS;
        }

        MicromissileEntity missile = new MicromissileEntity(worldIn, playerIn, stack);
        Vec3 newPos = missile.position().add(playerIn.getLookAngle().normalize());
        missile.setPos(newPos.x, newPos.y, newPos.z);
        missile.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, getInitialVelocity(stack), 0.0F);

        playerIn.getCooldowns().addCooldown(stack, ConfigHelper.common().micromissiles.launchCooldown.get());

        if (playerIn.level() instanceof ServerLevel serverLevel) {
            HitResult res = RayTraceUtils.getMouseOverServer(playerIn, 100);
            if (res instanceof EntityHitResult ertr) {
                if (missile.isValidTarget(ertr.getEntity())) {
                    missile.setTarget(ertr.getEntity());
                }
            }
            worldIn.addFreshEntity(missile);

            if (!playerIn.isCreative()) {
                stack.hurtAndBreak(1, serverLevel, playerIn, item -> { });
            }
        }

        return InteractionResult.SUCCESS;
    }

    private float getInitialVelocity(ItemStack stack) {
        Settings settings = stack.getOrDefault(ModDataComponents.MICROMISSILE_SETTINGS, Settings.DEFAULT);
        return settings.fireMode() == FireMode.SMART ? Math.max(0.2f, settings.topSpeed() / 2f) : 1 / 3f;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> curInfo, TooltipFlag extraInfo) {
        super.appendHoverText(stack, context, tooltipDisplay, curInfo, extraInfo);

        curInfo.accept(xlate("pneumaticcraft.gui.micromissile.remaining")
                .append(Component.literal(Integer.toString(stack.getMaxDamage() - stack.getDamageValue())).withStyle(ChatFormatting.AQUA))
        );
        if (stack.has(ModDataComponents.MICROMISSILE_SETTINGS)) {
            FireMode mode = getFireMode(stack);
            curInfo.accept(xlate("pneumaticcraft.gui.micromissile.firingMode")
                    .append(": ")
                    .append(xlate(mode.getTranslationKey()).withStyle(ChatFormatting.AQUA)));
            if (mode == FireMode.SMART) {
                String filter = stack.get(ModDataComponents.MICROMISSILE_SETTINGS).entityFilter();
                if (!filter.isEmpty()) {
                    curInfo.accept(xlate("pneumaticcraft.gui.sentryTurret.targetFilter")
                            .append(": ")
                            .append(ChatFormatting.AQUA + filter));
                }
            }
        }
        if (ConfigHelper.common().micromissiles.damageTerrain.get()) {
            curInfo.accept(xlate("pneumaticcraft.gui.tooltip.terrainWarning"));
        } else {
            curInfo.accept(xlate("pneumaticcraft.gui.tooltip.terrainSafe"));
        }
    }

    public static void setEntityFilter(ItemStack stack, String filterString) {
        if (stack.getItem() instanceof MicromissilesItem) {
            Settings settings = stack.getOrDefault(ModDataComponents.MICROMISSILE_SETTINGS, Settings.DEFAULT);
            stack.set(ModDataComponents.MICROMISSILE_SETTINGS, settings.withEntityFilter(filterString));
        }
    }

    public static FireMode getFireMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MICROMISSILE_SETTINGS, Settings.DEFAULT).fireMode();
    }

    @EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onMissilesRepair(AnvilUpdateEvent event) {
            // allow repeated repairing without XP costs spiralling
            // (AnvilRepairEvent is gone, and AnvilCraftEvent only has copies of the stacks, so this is done on the anvil result)
            if (event.getOutput().getItem() instanceof MicromissilesItem) {
                event.getOutput().set(DataComponents.REPAIR_COST, 0);
            }
        }
    }

    public record Tooltip(ItemStack stack) implements TooltipComponent {
    }

    public record Settings(float topSpeed, float turnSpeed, float damage, PointXY point, String entityFilter, FireMode fireMode) {
        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.FLOAT.fieldOf("top_speed").forGetter(Settings::topSpeed),
                Codec.FLOAT.fieldOf("turn_speed").forGetter(Settings::turnSpeed),
                Codec.FLOAT.fieldOf("damage").forGetter(Settings::damage),
                PointXY.CODEC.fieldOf("point").forGetter(Settings::point),
                Codec.STRING.fieldOf("filter").forGetter(Settings::entityFilter),
                StringRepresentable.fromEnum(FireMode::values).fieldOf("fire_mode").forGetter(Settings::fireMode)
        ).apply(builder, Settings::new));
        public static final StreamCodec<FriendlyByteBuf, Settings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Settings::topSpeed,
                ByteBufCodecs.FLOAT, Settings::turnSpeed,
                ByteBufCodecs.FLOAT, Settings::damage,
                PointXY.STREAM_CODEC, Settings::point,
                ByteBufCodecs.STRING_UTF8, Settings::entityFilter,
                NeoForgeStreamCodecs.enumCodec(FireMode.class), Settings::fireMode,
                Settings::new
        );

        public static final Settings DEFAULT = new Settings(1/3f, 1/3f, 1/3f, new PointXY(46, 54), "", FireMode.SMART);

        public Settings withEntityFilter(String filterString) {
            return new Settings(topSpeed, turnSpeed, damage, point, filterString, fireMode);
        }

        public Settings sanitize() {
            if (topSpeed < 0f || topSpeed > 1f || turnSpeed < 0f || turnSpeed > 1f || damage < 0f || damage > 1f
                    || topSpeed + turnSpeed + damage > 1.00001   // allow small epsilon for float imprecision
                    || !Float.isFinite(topSpeed) || !Float.isFinite(turnSpeed) || !Float.isFinite(damage))
            {
                return DEFAULT;
            }
            return this;
        }
    }

    public enum FireMode implements ITranslatableEnum, StringRepresentable {
        SMART("smart"), DUMB("dumb");

        private final String mode;

        FireMode(String mode) {
            this.mode = mode;
        }

        public static FireMode fromString(String mode) {
            try {
                return FireMode.valueOf(mode);
            } catch (IllegalArgumentException e) {
                return SMART;
            }
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.micromissile.mode." + mode;
        }

        @Override
        public String getSerializedName() {
            return mode;
        }
    }
}
