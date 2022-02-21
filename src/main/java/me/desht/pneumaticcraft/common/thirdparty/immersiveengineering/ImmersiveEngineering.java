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

package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.harvesting.HarvestHandlerCactusLike;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ImmersiveEngineering implements IThirdParty {
    private static final ResourceLocation HEMP_BLOCK = new ResourceLocation("immersiveengineering:hemp");

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(ElectricAttackHandler.class);
        MinecraftForge.EVENT_BUS.register(ExternalHeatCapListener.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(HarvestListener.class);
    }

    public static class HarvestListener {
        @SubscribeEvent
        public static void registerHarvestHandler(RegistryEvent.Register<HarvestHandler> event) {
            final Block hempBlock = ForgeRegistries.BLOCKS.getValue(HEMP_BLOCK);
            if (hempBlock != null && hempBlock != Blocks.AIR) {
                event.getRegistry().register(new HarvestHandlerCactusLike(state -> state.getBlock() == hempBlock)
                        .setRegistryName(RL("ie_hemp")));
            } else {
                Log.error("block 'immersiveengineering:hemp' did not get registered? PneumaticCraft drone harvesting won't work!");
            }
        }
    }

    public static class ExternalHeatCapListener {
        @SubscribeEvent
        public static void attachExternalHeatHandler(AttachCapabilitiesEvent<BlockEntity> event) {
            if (event.getObject() instanceof IHeatExchangingTE) {
                IEHeatHandler.Provider provider = new IEHeatHandler.Provider(event.getObject());
                event.addCapability(RL("ie_external_heatable"), provider);
                event.addListener(provider::invalidate);
            }
        }
    }
}
