package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.common.item.ItemMemoryStick;
import me.desht.pneumaticcraft.common.item.ItemMemoryStick.MemoryStickLocator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
            public void curioTick(String identifier, int index, LivingEntity livingEntity) {
                if (ItemMemoryStick.shouldAbsorbXPOrbs(event.getObject()) && livingEntity instanceof PlayerEntity) {
                    ItemMemoryStick.cacheMemoryStickLocation((PlayerEntity) livingEntity, MemoryStickLocator.namedInv(identifier, index));
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
