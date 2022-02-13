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
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class ItemTubeModule extends Item {
    private final Function<ItemTubeModule, TubeModule> moduleFactory;

    public ItemTubeModule(Function<ItemTubeModule, TubeModule> moduleFactory) {
        super(ModItems.defaultProps());
        this.moduleFactory = moduleFactory;
    }

    @Nonnull
    public TubeModule createModule() {
        return moduleFactory.apply(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        TubeModule module = createModule();
        tooltip.add(new TextComponent("In line: " + (module.isInline() ? "Yes" : "No")).withStyle(ChatFormatting.DARK_AQUA));
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
