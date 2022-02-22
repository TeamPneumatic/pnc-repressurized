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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.ProgressBarRenderer;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.network.PacketHackingBlockStart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RenderBlockTarget {
    private static final int SERVER_REQUEST_INTERVAL = 60; // ticks
    private static final float STAT_SCALE = 0.02F;

    private final Level world;
    private final BlockPos pos;
    private final int posHash;
    private final IGuiAnimatedStat stat;
    private final Player player;
    private final BlockTrackerClientHandler blockTracker;
    public int ticksExisted = 0;
    private int hackTime;
    private BlockEntity te;
    private int nEntries;
    private boolean refreshSent = false;

    public RenderBlockTarget(Level world, Player player, BlockPos pos, BlockEntity te,
                             BlockTrackerClientHandler blockTracker) {
        this.world = world;
        this.player = player;
        this.pos = pos;
        this.posHash = pos.hashCode();
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

    public void setTileEntity(BlockEntity te) {
        this.te = te;
    }

    /**
     * Check if this target still has any applicable entries. This should be called after calling tick().
     *
     * @return true if valid, false otherwise
     */
    public boolean isTargetStillValid() {
        return nEntries > 0;
    }

    private List<IBlockTrackEntry> getApplicableEntries() {
        return world.isLoaded(pos) ?
                BlockTrackEntryList.INSTANCE.getEntriesForCoordinate(world, pos, te) :
                Collections.emptyList();
    }

    public BlockPos getPos() {
        return pos;
    }

    public void requestServerUpdate(List<IBlockTrackEntry> applicableTrackEntries) {
        for (IBlockTrackEntry entry : applicableTrackEntries) {
            entry.getServerUpdatePositions(te).forEach(p -> NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(p)));
        }
        refreshSent = true;
    }

    public void tick() {
        if (te != null && te.isRemoved()) {
            te = null;
        }

        stat.tickWidget();

        List<IBlockTrackEntry> applicableTrackEntries = getApplicableEntries();
        nEntries = applicableTrackEntries.size();

        if (isPlayerLookingAtTarget() && (!refreshSent || world.getGameTime() % SERVER_REQUEST_INTERVAL == posHash % SERVER_REQUEST_INTERVAL)) {
            requestServerUpdate(applicableTrackEntries);
        }

        if (!world.isEmptyBlock(pos)) {
            List<Component> textList = new ArrayList<>();
            if (ticksExisted > 120) {
                stat.closeStat();
                if (applicableTrackEntries.stream().anyMatch(entry -> blockTracker.countBlockTrackersOfType(entry) <= entry.spamThreshold())) {
                    stat.openStat();
                }
                if (isPlayerLookingAtTarget()) {
                    stat.openStat();
                    addBlockTrackInfo(textList, applicableTrackEntries);
                }
            } else if (ticksExisted < -30) {
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

        if (ticksExisted > 50 && ticksExisted <= 120) {
            ProgressBarRenderer.render3d(matrixStack, buffer,0, 0.4F, 1.8F, 0.7F, 0, targetAcquireProgress, 0xD0FFFF00, 0xD000FF00);
        }

        matrixStack.scale(STAT_SCALE, STAT_SCALE, STAT_SCALE);

        if (!world.isEmptyBlock(pos)) {
            if (ticksExisted > 120) {
                if (isPlayerLookingAtTarget()) {
                    // a bit of growing or shrinking to keep the stat on screen and/or of legible size
                    float mul = ClientUtils.getStatSizeMultiplier(Mth.sqrt((float) ClientUtils.getClientPlayer().distanceToSqr(x, y, z)));
                    matrixStack.scale(mul, mul, mul);
                    stat.renderStat(matrixStack, buffer, partialTicks);
                }
            } else if (ticksExisted > 50) {
                RenderUtils.renderString3d(new TranslatableComponent("pneumaticcraft.entityTracker.info.acquiring"), 0, 0, 0xFFD0D0D0, matrixStack, buffer, false, true);
                RenderUtils.renderString3d(new TextComponent((int)targetAcquireProgress + "%"), 37, 24, 0xFFD0D0D0, matrixStack, buffer, false, true);
            } else if (ticksExisted < -30) {
                matrixStack.scale(1.5F, 1.5F, 1.5F);
                stat.renderStat(matrixStack, buffer, partialTicks);
                RenderUtils.renderString3d(new TranslatableComponent("pneumaticcraft.blockTracker.info.lostTarget"), 0, -ticksExisted / 2.5f, 0xFFFF0000, matrixStack, buffer, false, true);
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

        float alpha = 0.5f;
        if (blockTracker.getFocusedPos() != null) {
            alpha = blockTracker.getFocusedPos().equals(pos) ? 0.75f : 0.15f;
        }
        matrixStack.pushPose();
        matrixStack.translate(-0.5, -0.5, -0.5);
        RenderType type = RenderUtils.renderFrame(matrixStack, buffer, aabb, 1/64f, 0.25f, 0.75f, 0.75f, alpha, RenderUtils.FULL_BRIGHT);
        RenderUtils.finishBuffer(buffer, type);
        matrixStack.popPose();
    }

    private boolean isInitialized() {
        return ticksExisted >= 120;
    }

    private void addBlockTrackInfo(List<Component> textList, List<IBlockTrackEntry> entries) {
        entries.forEach(e -> e.addInformation(world, pos, te, isPlayerLookingAtTarget() ? blockTracker.getFocusedFace() : null, textList));
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

    public boolean scroll(InputEvent.MouseScrollEvent event) {
        if (isInitialized() && isPlayerLookingAtTarget()) {
            return stat.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        return false;
    }

    public void updateColor(int color) {
        stat.setBackgroundColor(color);
    }

    public Component getTitle() {
        return stat.getTitle();
    }
}
