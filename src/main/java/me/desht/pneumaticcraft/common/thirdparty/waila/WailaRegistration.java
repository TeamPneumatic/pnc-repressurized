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

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.entity.semiblock.EntitySemiblockBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

@WailaPlugin
public class WailaRegistration implements IWailaPlugin {
    @Override
    public void register(IRegistrar iRegistrar) {
        iRegistrar.registerBlockDataProvider(new PneumaticProvider.Data(), TileEntityBase.class);
        iRegistrar.registerBlockDataProvider(new SemiblockProvider.Data(), BlockEntity.class);
        iRegistrar.registerBlockDataProvider(new RedstoneControlProvider.Data(), TileEntityBase.class);
        iRegistrar.registerBlockDataProvider(new TubeModuleProvider.Data(), TileEntityPressureTube.class);
        iRegistrar.registerEntityDataProvider(new EntityProvider.Data(), LivingEntity.class);
        iRegistrar.registerEntityDataProvider(new EntityProvider.Data(), EntitySemiblockBase.class);

        iRegistrar.registerComponentProvider(new PneumaticProvider.Component(), TooltipPosition.BODY, Block.class);
        iRegistrar.registerComponentProvider(new SemiblockProvider.Component(), TooltipPosition.BODY, Block.class);
        iRegistrar.registerComponentProvider(new RedstoneControlProvider.Component(), TooltipPosition.BODY, AbstractPneumaticCraftBlock.class);
        iRegistrar.registerComponentProvider(new TubeModuleProvider.Component(), TooltipPosition.BODY, PressureTubeBlock.class);
        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.BODY, LivingEntity.class);
//        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.HEAD, EntitySemiblockBase.class);
        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.BODY, EntitySemiblockBase.class);
//        iRegistrar.registerComponentProvider(new EntityProvider.Component(), TooltipPosition.TAIL, EntitySemiblockBase.class);
        iRegistrar.registerComponentProvider(new CamoProvider.Component(), TooltipPosition.BODY, AbstractPneumaticCraftBlock.class);
    }
}
