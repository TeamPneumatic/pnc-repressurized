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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.ProgressBarRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockStart;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RenderBlockTarget {
    private static final int SERVER_REQUEST_INTERVAL = 30; // ticks
    private static final float STAT_SCALE = 0.02F;

    // some special values for the tick counter
    // note that negative values for the tick counter have some special meanings...
    private static final int START_ACQUIRE_TIME = 50;
    private static final int FINISH_ACQUIRE_TIME = 120;
    private static final int REMOVE_NOW_TIME = -1;
    private static final int MARK_FOR_REMOVAL_TIME = -100;
    private static final int LOST_TARGET_TIME = -30;

    private final Level world;
    private final BlockPos pos;
    private final int posHash;
    private final IGuiAnimatedStat stat;
    private final Player player;
    private final BlockTrackerClientHandler blockTracker;
    private int ticksExisted = 0;
    private int hackTime;
    private BlockEntity te;
    private int nEntries;

    public RenderBlockTarget(Level world, Player player, BlockPos pos, BlockEntity te,
                             BlockTrackerClientHandler blockTracker) {
        this.world = world;
        this.player = player;
        this.pos = pos;
        this.posHash = Math.abs(pos.hashCode());
        this.te = te;
        this.blockTracker = blockTracker;

        BlockState state = world.getBlockState(pos);
        ItemStack stack = state.getBlock().getCloneItemStack(state, Minecraft.getInstance().hitResult, world, pos, player);

        Component title = stack.isEmpty() ? xlate(world.getBlockState(pos).getBlock().getDescriptionId()) : stack.getHoverName();
        stat = new WidgetAnimatedStat(null, title, WidgetAnimatedStat.StatIcon.of(stack), 20, -20, HUDHandler.getInstance().getStatOverlayColor(), null, false);
        stat.setMinimumContractedDimensions(0, 0);
        stat.setMinimumExpandedDimensions(16, 16);
        stat.setAutoLineWrap(false);
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setTileEntity(BlockEntity te) {
        this.te = te;
    }

    /**
     * Check if this target still has any applicable entries - called after calling tick().
     *
     * @return true if valid, false otherwise
     */
    public boolean isTargetStillValid() {
        return nEntries > 0;
    }

    public void tick() {
        if (!world.isLoaded(pos)) return;

        int incr = CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EquipmentSlot.HEAD);
        int prevTicks = ticksExisted;
        ticksExisted += incr;
        if (prevTicks < 0 && ticksExisted >= 0) {
            ticksExisted = REMOVE_NOW_TIME;
        }

        if (te != null && te.isRemoved()) {
            te = null;
        }

        stat.tickWidget();

        List<IBlockTrackEntry> applicableTrackEntries = BlockTrackHandler.getInstance().getEntriesForCoordinate(world, pos, te);
        nEntries = applicableTrackEntries.size();

        maybeUpdateFromServer(applicableTrackEntries);

        if (!world.isEmptyBlock(pos)) {
            List<Component> textList = new ArrayList<>();
            if (ticksExisted > FINISH_ACQUIRE_TIME) {
                stat.closeStat();
                if (applicableTrackEntries.stream().anyMatch(entry -> blockTracker.countBlockTrackersOfType(entry) <= entry.spamThreshold())) {
                    stat.openStat();
                }
                if (isPlayerLookingAtTarget()) {
                    stat.openStat();
                    applicableTrackEntries.forEach(e -> e.addInformation(world, pos, te, isPlayerLookingAtTarget() ? blockTracker.getFocusedFace() : null, textList));
                }
            } else if (ticksExisted < LOST_TARGET_TIME) {
                stat.closeStat();
            }
            stat.setText(textList);
        }

        if (hackTime > 0) {
            IHackableBlock hackableBlock = HackManager.getHackableForBlock(world, pos, player);
            if (hackableBlock != null) {
                hackTime++;
            } else {
                hackTime = 0;
            }
        }

    }

    public void checkValidity(int rangeSq) {
        if (ticksExisted > 0 && (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > rangeSq || !isTargetStillValid())) {
            ticksExisted = MARK_FOR_REMOVAL_TIME;
        }
    }

    private void maybeUpdateFromServer(List<IBlockTrackEntry> applicableTrackEntries) {
        if (te == null) return;

        boolean searching = CommonArmorHandler.getHandlerForPlayer().upgradeUsable(CommonUpgradeHandlers.searchHandler, true);
        boolean focused = isPlayerLookingAtTarget();
        int searchInterval = focused ? SERVER_REQUEST_INTERVAL : SERVER_REQUEST_INTERVAL * 2;

        // periodically refresh focused block from the server (or any block on a longer interval if search is active)
        if ((focused || searching) && world.getGameTime() % searchInterval == posHash % searchInterval) {
            Set<BlockPos> posSet = applicableTrackEntries.stream()
                    .flatMap(entry -> entry.getServerUpdatePositions(te).stream())
                    .collect(Collectors.toSet());
            posSet.forEach(pos -> NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(pos)));
        }
    }

    public void render(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        matrixStack.pushPose();

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        matrixStack.translate(x, y, z);

        if (!world.isEmptyBlock(pos)) {
            renderBlockHighlight(matrixStack, buffer, world, pos, partialTicks);
        }

        RenderUtils.rotateToPlayerFacing(matrixStack);

        float targetAcquireProgress = (ticksExisted + partialTicks) / 1.2f;

        if (ticksExisted > START_ACQUIRE_TIME && ticksExisted <= FINISH_ACQUIRE_TIME) {
            ProgressBarRenderer.render3d(matrixStack, buffer,0, 0.4F, 1.8F, 0.7F, 0, targetAcquireProgress, 0xD0FFFF00, 0xD000FF00);
        }

        matrixStack.scale(STAT_SCALE, STAT_SCALE, STAT_SCALE);

        if (!world.isEmptyBlock(pos)) {
            if (ticksExisted > FINISH_ACQUIRE_TIME) {
                if (isPlayerLookingAtTarget()) {
                    // a bit of growing or shrinking to keep the stat on screen and/or of legible size
                    float mul = ClientUtils.getStatSizeMultiplier(Mth.sqrt((float) ClientUtils.getClientPlayer().distanceToSqr(x, y, z)));
                    matrixStack.scale(mul, mul, mul);
                    stat.renderStat(matrixStack, buffer, partialTicks);
                }
            } else if (ticksExisted > START_ACQUIRE_TIME) {
                RenderUtils.renderString3d(Component.translatable("pneumaticcraft.entityTracker.info.acquiring"), 0, 0, 0xFFD0D0D0, matrixStack, buffer, false, true);
                RenderUtils.renderString3d(Component.literal((int)targetAcquireProgress + "%"), 37, 24, 0xFFD0D0D0, matrixStack, buffer, false, true);
            } else if (ticksExisted < LOST_TARGET_TIME) {
                matrixStack.scale(1.5F, 1.5F, 1.5F);
                stat.renderStat(matrixStack, buffer, partialTicks);
                RenderUtils.renderString3d(Component.translatable("pneumaticcraft.blockTracker.info.lostTarget"), 0, -ticksExisted / 2.5f, 0xFFFF0000, matrixStack, buffer, false, true);
            }
        }

        matrixStack.popPose();
    }

    private void renderBlockHighlight(PoseStack matrixStack, MultiBufferSource buffer, Level world, BlockPos pos, float partialTicks) {
        BlockState state = world.getBlockState(pos);
        VoxelShape shape = state.getShape(world, pos);
        if (shape.isEmpty()) return;

        float progress = ((world.getGameTime() & 0x1f) + partialTicks) / 32f;
        float cycle = Mth.sin((float) (progress * Math.PI));

        float shrink = (shape == Shapes.block() ? 0.05f : 0f) + cycle / 60f;
        AABB aabb = shape.bounds().deflate(shrink);

        float alpha = blockTracker.getFocusedPos() != null ? (blockTracker.getFocusedPos().equals(pos) ? 0.75f : 0.15f) : 0.5f;

        matrixStack.pushPose();
        matrixStack.translate(-0.5, -0.5, -0.5);
        RenderType type = RenderUtils.renderFrame(matrixStack, buffer, aabb, 1/64f, 0.25f, 0.75f, 0.75f, alpha, RenderUtils.FULL_BRIGHT);
        RenderUtils.finishBuffer(buffer, type);
        matrixStack.popPose();
    }

    private boolean isInitialized() {
        return ticksExisted >= FINISH_ACQUIRE_TIME;
    }

    private boolean isPlayerLookingAtTarget() {
        return pos.equals(blockTracker.getFocusedPos());
    }

    public void hack() {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            IHackableBlock block = HackManager.getHackableForBlock(world, pos, player);
            if (block != null && (hackTime == 0 || hackTime > block.getHackTime(world, pos, player)))
                NetworkHandler.sendToServer(new PacketHackingBlockStart(pos));
        }
    }

    public void onHackConfirmServer() {
        hackTime = 1;
    }

    public int getHackTime() {
        return hackTime;
    }

    public boolean scroll(InputEvent.MouseScrollingEvent event) {
        return isInitialized() && isPlayerLookingAtTarget() && stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY());
    }

    public void updateColor(int color) {
        stat.setBackgroundColor(color);
    }

    public Component getTitle() {
        return stat.getTitle();
    }

    public boolean shouldRemoveNow() {
        return ticksExisted == REMOVE_NOW_TIME;
    }

    public void markValid() {
        ticksExisted = Math.abs(ticksExisted); // cancel possible "lost target" status
    }
}
