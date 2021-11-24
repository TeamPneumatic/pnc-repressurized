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

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketNotifyVariablesRemote;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemRemote extends Item {
    private static final String NBT_SECURITY_POS = "securityPos";

    public ItemRemote() {
        super(ModItems.defaultProps().stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        if (!world.isClientSide) {
            openGui(player, stack, handIn);
        }
        return ActionResult.success(stack);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack remote, ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntitySecurityStation) {
            if (!world.isClientSide && player.isShiftKeyDown() && isAllowedToEdit(player, remote)) {
                if (((TileEntitySecurityStation) te).doesAllowPlayer(player)) {
                    GlobalPos gPos = GlobalPosHelper.makeGlobalPos(world, pos);
                    setSecurityStationPos(remote, gPos);
                    player.displayClientMessage(xlate("pneumaticcraft.gui.remote.boundSecurityStation", GlobalPosHelper.prettyPrint(gPos)), false);
                    return ActionResultType.SUCCESS;
                } else {
                    player.displayClientMessage(xlate("pneumaticcraft.gui.remote.cantBindSecurityStation"), true);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack remote, World world, List<ITextComponent> curInfo, ITooltipFlag moreInfo) {
        super.appendHoverText(remote, world, curInfo, moreInfo);
        curInfo.add(xlate("pneumaticcraft.gui.remote.tooltip.sneakRightClickToEdit"));
        GlobalPos gPos = getSecurityStationPos(remote);
        if (gPos != null) {
            curInfo.add(xlate("pneumaticcraft.gui.remote.tooltip.boundToSecurityStation", GlobalPosHelper.prettyPrint(gPos)));
        } else {
            curInfo.add(xlate("pneumaticcraft.gui.remote.tooltip.rightClickToBind"));
        }
    }

    private void openGui(PlayerEntity player, ItemStack remote, Hand hand) {
        if (player.isCrouching()) {
            if (isAllowedToEdit(player, remote)) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new RemoteEditorContainerProvider(remote, hand), buf -> buf.writeBoolean(hand == Hand.MAIN_HAND));
                NetworkHandler.sendToPlayer(new PacketNotifyVariablesRemote(GlobalVariableManager.getInstance().getAllActiveVariableNames()), (ServerPlayerEntity) player);
            }
        } else {
            NetworkHooks.openGui((ServerPlayerEntity) player, new RemoteContainerProvider(remote, hand), buf -> buf.writeBoolean(hand == Hand.MAIN_HAND));
        }
    }

    public static boolean hasSameSecuritySettings(ItemStack remote1, ItemStack remote2) {
        GlobalPos g1 = getSecurityStationPos(remote1);
        GlobalPos g2 = getSecurityStationPos(remote2);
        return g1 == null && g2 == null || g1 != null && g1.equals(g2);
    }

    private boolean isAllowedToEdit(PlayerEntity player, ItemStack remote) {
        GlobalPos gPos = getSecurityStationPos(remote);
        if (gPos != null) {
            TileEntity te = GlobalPosHelper.getTileEntity(gPos);
            if (te instanceof TileEntitySecurityStation) {
                boolean canAccess = ((TileEntitySecurityStation) te).doesAllowPlayer(player);
                if (!canAccess) {
                    player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.gui.remote.noEditRights", gPos).withStyle(TextFormatting.RED), false);
                }
                return canAccess;
            }
        }
        return true;
    }

    private static GlobalPos getSecurityStationPos(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(NBT_SECURITY_POS) ?
                GlobalPosHelper.fromNBT(stack.getTag().getCompound(NBT_SECURITY_POS)) : null;
    }

    private static void setSecurityStationPos(ItemStack stack, GlobalPos gPos) {
        NBTUtils.setCompoundTag(stack, NBT_SECURITY_POS, GlobalPosHelper.toNBT(gPos));
    }

    @Override
    public void inventoryTick(ItemStack remote, World world, Entity entity, int slot, boolean holdingItem) {
        if (!world.isClientSide) {
            GlobalPos gPos = getSecurityStationPos(remote);
            if (gPos != null) {
                TileEntity te = GlobalPosHelper.getTileEntity(gPos);
                if (!(te instanceof TileEntitySecurityStation) && remote.hasTag()) {
                    remote.getTag().remove(NBT_SECURITY_POS);
                }
            }
        }
    }

    static class RemoteContainerProvider implements INamedContainerProvider {
        private final ItemStack stack;
        private final Hand hand;

        RemoteContainerProvider(ItemStack stack, Hand hand) {
            this.stack = stack;
            this.hand = hand;
        }

        @Override
        public ITextComponent getDisplayName() {
            return stack.getHoverName();
        }

        @Nullable
        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerRemote(getType(), windowId, playerInventory, hand);
        }

        protected ContainerType<? extends ContainerRemote> getType() {
            return ModContainers.REMOTE.get();
        }
    }

    static class RemoteEditorContainerProvider extends RemoteContainerProvider {
        RemoteEditorContainerProvider(ItemStack stack, Hand hand) {
            super(stack, hand);
        }

        @Override
        protected ContainerType<? extends ContainerRemote> getType() {
            return ModContainers.REMOTE_EDITOR.get();
        }
    }
}
