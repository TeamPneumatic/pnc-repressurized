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

package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.*;
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractSemiblockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

@SuppressWarnings("UnstableApiUsage")
@WailaPlugin
public class WailaRegistration implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration iRegistrar) {
        iRegistrar.registerBlockDataProvider(new PneumaticProvider.Data(), AbstractPneumaticCraftBlockEntity.class);
        iRegistrar.registerBlockDataProvider(new SemiblockProvider.Data(), BlockEntity.class);
        iRegistrar.registerBlockDataProvider(new RedstoneControlProvider.Data(), AbstractPneumaticCraftBlockEntity.class);
        iRegistrar.registerBlockDataProvider(new TubeModuleProvider.Data(), PressureTubeBlockEntity.class);
        iRegistrar.registerEntityDataProvider(new EntityProvider.Data(), LivingEntity.class);
        iRegistrar.registerEntityDataProvider(new EntityProvider.Data(), AbstractSemiblockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration iRegistrar) {
        iRegistrar.registerComponentProvider(new PneumaticProvider.Component(), TooltipPosition.BODY, Block.class);
        iRegistrar.registerComponentProvider(new SemiblockProvider.Component(), TooltipPosition.BODY, Block.class);
        iRegistrar.registerComponentProvider(new RedstoneControlProvider.Component(), TooltipPosition.BODY, AbstractPneumaticCraftBlock.class);
        iRegistrar.registerComponentProvider(new TubeModuleProvider.Component(), TooltipPosition.BODY, PressureTubeBlock.class);
        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.BODY, LivingEntity.class);
//        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.HEAD, EntitySemiblockBase.class);
        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.BODY, AbstractSemiblockEntity.class);
//        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.TAIL, EntitySemiblockBase.class);
        iRegistrar.registerComponentProvider(new CamoProvider.Component(), TooltipPosition.BODY, AbstractPneumaticCraftBlock.class);
    }
}
