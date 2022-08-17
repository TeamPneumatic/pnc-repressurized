package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SpawnerAgitatorItem extends SemiblockItem {
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (ModList.get().isLoaded(ModIds.APOTHEOSIS)) {
            if (context.getPlayer() != null && context.getLevel().isClientSide()) {
                context.getPlayer().displayClientMessage(xlate("gui.tooltip.item.pneumaticcraft.spawner_agitator.apotheosis").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        if (ModList.get().isLoaded(ModIds.APOTHEOSIS)) {
            pTooltipComponents.add(xlate("gui.tooltip.item.pneumaticcraft.spawner_agitator.apotheosis").withStyle(ChatFormatting.RED));
        }
    }
}
