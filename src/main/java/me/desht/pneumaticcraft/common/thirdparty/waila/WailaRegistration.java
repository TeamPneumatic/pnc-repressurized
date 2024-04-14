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

import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractSemiblockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class WailaRegistration implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration iRegistrar) {
        iRegistrar.registerBlockDataProvider(new PneumaticProvider.DataProvider(), AbstractPneumaticCraftBlockEntity.class);
        iRegistrar.registerBlockDataProvider(new SemiblockProvider.DataProvider(), BlockEntity.class);
        iRegistrar.registerBlockDataProvider(new RedstoneControlProvider.DataProvider(), AbstractPneumaticCraftBlockEntity.class);
        iRegistrar.registerBlockDataProvider(new TubeModuleProvider.DataProvider(), PressureTubeBlockEntity.class);

        iRegistrar.registerEntityDataProvider(new EntityProvider.DataProvider(), LivingEntity.class);
        iRegistrar.registerEntityDataProvider(new EntityProvider.DataProvider(), AbstractSemiblockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration iRegistrar) {
        iRegistrar.registerBlockComponent(new PneumaticProvider.ComponentProvider(), Block.class);
        iRegistrar.registerBlockComponent(new SemiblockProvider.ComponentProvider(), Block.class);
        iRegistrar.registerBlockComponent(new RedstoneControlProvider.ComponentProvider(), AbstractPneumaticCraftBlock.class);
        iRegistrar.registerBlockComponent(new TubeModuleProvider.ComponentProvider(), PressureTubeBlock.class);
        iRegistrar.registerBlockComponent(new CamoProvider.ComponentProvider(), AbstractPneumaticCraftBlock.class);

        iRegistrar.registerEntityComponent(new EntityProvider.ComponentProvider(), LivingEntity.class);
        iRegistrar.registerEntityComponent(new EntityProvider.ComponentProvider(), AbstractSemiblockEntity.class);
    }
}
