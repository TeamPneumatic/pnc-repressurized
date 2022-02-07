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

package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.common.item.ItemMemoryStick;
import me.desht.pneumaticcraft.common.item.ItemMemoryStick.MemoryStickLocator;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Allows us to intercept ticks for memory stick(s) in a Curios inventory handler
 * Need to do this to cache what memory sticks players have on them for efficient
 * XP orb absorption.
 */
public class CuriosTickerCapability {
    public static void addCuriosCap(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(RL("curio_ticker"), new CuriosTickerProvider(new ICurio() {
            @Override
            public ItemStack getStack() {
                return event.getObject();
            }

            @Override
            public void curioTick(String identifier, int index, LivingEntity livingEntity) {
                if (ItemMemoryStick.shouldAbsorbXPOrbs(event.getObject()) && livingEntity instanceof Player) {
                    ItemMemoryStick.cacheMemoryStickLocation((Player) livingEntity, MemoryStickLocator.namedInv(identifier, index));
                }
            }
        }));
    }

    private static class CuriosTickerProvider implements ICapabilityProvider {
        final LazyOptional<ICurio> capability;

        CuriosTickerProvider(ICurio curio) {
            this.capability = LazyOptional.of(() -> curio);
        }

        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CuriosCapability.ITEM.orEmpty(cap, this.capability);
        }
    }
}
