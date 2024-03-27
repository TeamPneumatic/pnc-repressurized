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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.MicromissileScreen;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.MicromissileDefaults;
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class MicromissilesItem extends Item {
    public static final String NBT_TOP_SPEED = "topSpeed";
    public static final String NBT_TURN_SPEED = "turnSpeed";
    public static final String NBT_DAMAGE = "damage";
    public static final String NBT_FILTER = "filter";
    public static final String NBT_PX = "px";
    public static final String NBT_PY = "py";
    public static final String NBT_FIRE_MODE = "fireMode";

    public enum FireMode implements ITranslatableEnum {
        SMART, DUMB;

        public static FireMode fromString(String mode) {
            try {
                return FireMode.valueOf(mode);
            } catch (IllegalArgumentException e) {
                return SMART;
            }
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.micromissile.mode." + this.toString().toLowerCase(Locale.ROOT);
        }
    }

    public MicromissilesItem() {
        super(ModItems.defaultProps().stacksTo(1).defaultDurability(100));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHelper.common().micromissiles.missilePodSize.get();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return toRepair.getItem() == this && repair.getItem() == Blocks.TNT.asItem();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (!NBTUtils.hasTag(stack, MicromissilesItem.NBT_TOP_SPEED)) {
            stack.setTag(MicromissileDefaults.INSTANCE.getDefaults(playerIn).toNBT());
        }

        if (playerIn.isShiftKeyDown()) {
            if (worldIn.isClientSide) {
                MicromissileScreen.openGui(stack.getHoverName(), handIn);
            }
            return InteractionResultHolder.success(stack);
        }

        MicromissileEntity missile = new MicromissileEntity(worldIn, playerIn, stack);
        Vec3 newPos = missile.position().add(playerIn.getLookAngle().normalize());
        missile.setPos(newPos.x, newPos.y, newPos.z);
        missile.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, getInitialVelocity(stack), 0.0F);

        playerIn.getCooldowns().addCooldown(this, ConfigHelper.common().micromissiles.launchCooldown.get());

        if (!worldIn.isClientSide) {
            HitResult res = RayTraceUtils.getMouseOverServer(playerIn, 100);
            if (res instanceof EntityHitResult ertr) {
                if (missile.isValidTarget(ertr.getEntity())) {
                    missile.setTarget(ertr.getEntity());
                }
            }
            worldIn.addFreshEntity(missile);
        }

        if (!playerIn.isCreative()) {
            stack.hurtAndBreak(1, playerIn, playerEntity -> { });
        }
        return InteractionResultHolder.success(stack);
    }

    private float getInitialVelocity(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = Objects.requireNonNull(stack.getTag());
            FireMode fireMode = FireMode.fromString(tag.getString(NBT_FIRE_MODE));
            if (fireMode == FireMode.SMART) {
                return Math.max(0.2f, tag.getFloat(NBT_TOP_SPEED) / 2f);
            } else {
                return 1/3f;
            }
        } else {
            return 1/3f;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> curInfo, TooltipFlag extraInfo) {
        super.appendHoverText(stack, worldIn, curInfo, extraInfo);

        curInfo.add(xlate("pneumaticcraft.gui.micromissile.remaining")
                .append(Component.literal(Integer.toString(stack.getMaxDamage() - stack.getDamageValue())).withStyle(ChatFormatting.AQUA))
        );
        if (NBTUtils.hasTag(stack, MicromissilesItem.NBT_TOP_SPEED)) {
            FireMode mode = getFireMode(stack);
            curInfo.add(xlate("pneumaticcraft.gui.micromissile.firingMode")
                    .append(": ")
                    .append(xlate(mode.getTranslationKey()).withStyle(ChatFormatting.AQUA)));
            if (mode == FireMode.SMART) {
                String filter = Objects.requireNonNull(stack.getTag()).getString(NBT_FILTER);
                if (!filter.isEmpty()) {
                    curInfo.add(xlate("pneumaticcraft.gui.sentryTurret.targetFilter")
                            .append(": ")
                            .append(ChatFormatting.AQUA + filter));
                }
            }
        }
        if (ConfigHelper.common().micromissiles.damageTerrain.get()) {
            curInfo.add(xlate("pneumaticcraft.gui.tooltip.terrainWarning"));
        } else {
            curInfo.add(xlate("pneumaticcraft.gui.tooltip.terrainSafe"));
        }
    }

    public static void setEntityFilter(ItemStack stack, String filterString) {
        if (stack.getItem() instanceof MicromissilesItem) {
            stack.getOrCreateTag().putString(MicromissilesItem.NBT_FILTER, filterString);
        }
    }

    public static FireMode getFireMode(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof MicromissilesItem);
        return stack.hasTag() ? FireMode.fromString(Objects.requireNonNull(stack.getTag()).getString(MicromissilesItem.NBT_FIRE_MODE)) : FireMode.SMART;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onMissilesRepair(AnvilRepairEvent event) {
            // allow repeated repairing without XP costs spiralling
            if (event.getOutput().getItem() instanceof MicromissilesItem && event.getOutput().hasTag()) {
                event.getOutput().setRepairCost(0);
            }
        }
    }

    public record Tooltip(ItemStack stack) implements TooltipComponent {
    }
}
