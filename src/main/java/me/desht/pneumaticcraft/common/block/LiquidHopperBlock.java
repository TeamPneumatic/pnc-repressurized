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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.render.fluid.IFluidItemRenderInfoProvider;
import me.desht.pneumaticcraft.client.render.fluid.RenderLiquidHopper;
import me.desht.pneumaticcraft.common.block.entity.hopper.LiquidHopperBlockEntity;
import me.desht.pneumaticcraft.common.item.IFluidCapProvider;
import me.desht.pneumaticcraft.common.item.IFluidRendered;
import me.desht.pneumaticcraft.common.item.PNCBlockItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

import javax.annotation.Nullable;

public class LiquidHopperBlock extends OmnidirectionalHopperBlock implements PneumaticCraftEntityBlock {
    public LiquidHopperBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new LiquidHopperBlockEntity(pPos, pState);
    }

    public static class ItemBlockLiquidHopper extends PNCBlockItem implements ColorHandlers.ITintableItem, IFluidRendered, IFluidCapProvider {
        Lazy<RenderLiquidHopper.ItemRenderInfoProvider> renderInfoProvider = Lazy.of(RenderLiquidHopper.ItemRenderInfoProvider::new);

        public ItemBlockLiquidHopper(Block block) {
            super(block, ModItems.defaultProps());
        }

        // TODO can (or indeed should) we support recipes which drain amounts other than 1000mB?
        @Override
        public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
            return FluidUtils.getDrainedCraftingRemainder(instance, FluidType.BUCKET_VOLUME, false);
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            int n = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.CREATIVE.get());
            return n > 0 ? 0xFFDB46CF : 0xFF2b2727;
        }

        @Override
        public IFluidItemRenderInfoProvider getFluidItemRenderer() {
            return renderInfoProvider.get();
        }

        @Override
        public ResourceHandler<FluidResource> provideFluidCapability(ItemAccess itemAccess) {
            return new ItemAccessFluidHandler(itemAccess, ModDataComponents.MAIN_TANK.get(), PneumaticValues.NORMAL_TANK_CAPACITY);
        }
    }
}
