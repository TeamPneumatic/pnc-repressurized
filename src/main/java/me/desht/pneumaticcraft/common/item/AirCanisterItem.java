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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.PneumaticValues.*;

public class AirCanisterItem extends PressurizableItem {
    private final CanisterType type;

    public AirCanisterItem(CanisterType type) {
        super(ModItems.defaultProps().component(ModDataComponents.AIR_CANISTER_CHARGING, ChargeMode.NONE), type.maxAir, type.volume);
        this.type = type;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        // only completely empty (freshly crafted) canisters may stack
        // this makes it easier for players when needed in a crafting recipe
        return getPressure(stack) > 0f ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);

        MutableComponent c = xlate(pStack.getOrDefault(ModDataComponents.AIR_CANISTER_CHARGING, ChargeMode.NONE).getTranslationKey());
        pTooltipComponents.add(c.withStyle(ChatFormatting.AQUA));
        pTooltipComponents.add(xlate("pneumaticcraft.gui.air_canister.charging.sneak_right_click").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide) {
            ChargeMode mode = getChargeMode(stack).nextMode();
            pPlayer.getItemInHand(pUsedHand).set(ModDataComponents.AIR_CANISTER_CHARGING, mode);
            pPlayer.displayClientMessage(xlate(mode.getTranslationKey()).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity.level().isClientSide) return;

        ChargeMode mode = getChargeMode(pStack);
        if (pEntity instanceof Player player && mode != ChargeMode.NONE && pLevel.getGameTime() % PneumaticValues.ARMOR_CHARGER_INTERVAL == 15) {
            PNCCapabilities.getAirHandler(pStack).ifPresent(srcHandler -> {
                if (mode == ChargeMode.HELD) {
                    tryChargeItem(srcHandler, player.getMainHandItem(), type.maxTransfer);
                } else {
                    for (ItemStack stack : player.getInventory().items) {
                        if (srcHandler.getPressure() < 0.1f) {
                            break;
                        }
                        if (stack.getCount() == 1 && stack != pStack && !isChargingAirCanister(stack)) {
                            tryChargeItem(srcHandler, stack, type.maxTransfer);
                        }
                    }
                }
            });
        }
    }

    private static boolean isChargingAirCanister(ItemStack stack) {
        return stack.getItem() instanceof AirCanisterItem && getChargeMode(stack) != ChargeMode.NONE;
    }

    private void tryChargeItem(IAirHandler srcHandler, ItemStack dstStack, int maxAir) {
        PNCCapabilities.getAirHandler(dstStack).ifPresent(destHandler -> {
            float destPressure = destHandler.getPressure();
            if (destPressure < destHandler.maxPressure() && (destPressure - srcHandler.getPressure()) < -0.01f) {
                float targetPressure = Math.min((srcHandler.getPressure() + destHandler.getPressure()) / 2f, destHandler.maxPressure());
                int currentAir = destHandler.getAir();
                int targetAir = (int) (targetPressure * destHandler.getVolume());
                int airToMove = Mth.clamp(targetAir - currentAir, 0, maxAir);
                destHandler.addAir(airToMove);
                srcHandler.addAir(-airToMove);
            }
        });
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return getChargeMode(pStack) != ChargeMode.NONE;
    }

    public static ChargeMode getChargeMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AIR_CANISTER_CHARGING, ChargeMode.NONE);
    }

    public enum CanisterType {
        BASIC(AIR_CANISTER_MAX_AIR, AIR_CANISTER_VOLUME, 1000),
        BASIC_ARRAY(AIR_CANISTER_MAX_AIR * 4, AIR_CANISTER_VOLUME * 4, 4000),
        REINFORCED(REINFORCED_AIR_CANISTER_MAX_AIR, REINFORCED_AIR_CANISTER_VOLUME, 4000),
        REINFORCED_ARRAY(REINFORCED_AIR_CANISTER_MAX_AIR * 4, REINFORCED_AIR_CANISTER_VOLUME * 4, 16000);

        private final int maxAir;
        private final int volume;
        private final int maxTransfer;

        CanisterType(int maxAir, int volume, int maxTransfer) {
            this.maxAir = maxAir;
            this.volume = volume;
            this.maxTransfer = maxTransfer;
        }
    }

    public enum ChargeMode implements StringRepresentable, ITranslatableEnum {
        NONE("none"),
        HELD("held"),
        ALL("all");

        private final String name;

        ChargeMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.air_canister.charging." + name;
        }

        public ChargeMode nextMode() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
