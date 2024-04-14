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

import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.tubemodules.AbstractTubeModule;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class TubeModuleItem extends Item {
    private final BiFunction<Direction, PressureTubeBlockEntity, AbstractTubeModule> moduleFactory;

    public TubeModuleItem(BiFunction<Direction, PressureTubeBlockEntity, AbstractTubeModule> moduleFactory) {
        super(ModItems.defaultProps());
        this.moduleFactory = moduleFactory;
    }

    @Nonnull
    public AbstractTubeModule createModule(Direction dir, @Nullable PressureTubeBlockEntity blockEntity) {
        return moduleFactory.apply(dir, blockEntity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        AbstractTubeModule module = createModule(Direction.UP,null);
        tooltip.add(Component.literal("In line: " + (module.isInline() ? "Yes" : "No")).withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            // sneak-click module to attach it to opposite side of tube, if possible
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            if (state.getBlock() instanceof PressureTubeBlock) {
                BlockHitResult brtr = new BlockHitResult(context.getClickLocation(), context.getClickedFace().getOpposite(), context.getClickedPos(), false);
                return state.use(context.getLevel(), context.getPlayer(), context.getHand(), brtr);
            }
        }
        return super.useOn(context);
    }
}
