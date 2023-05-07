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

package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.BlockTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.TrackerBlacklistManager;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.BlockTrackerHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockTrackerClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<BlockTrackerHandler> {
    static final int BLOCK_TRACKING_RANGE = 30;
    private static final int HARD_MAX_BLOCKS_PER_TICK = 50000;
    private static final StatPanelLayout DEFAULT_STAT_LAYOUT = new StatPanelLayout(0.995f, 0.1f, true);

    private final Map<BlockPos, RenderBlockTarget> blockTargets = new Object2ObjectOpenHashMap<>();
    private IGuiAnimatedStat blockTrackInfo;
    private final Object2IntMap<ResourceLocation> blockTypeCount = new Object2IntOpenHashMap<>();
    private final Object2IntMap<ResourceLocation> blockTypeCountPartial = new Object2IntOpenHashMap<>();
    private int xOff = 0, yOff = 0, zOff = 0;
    private RenderBlockTarget focusedTarget = null;
    private Direction focusedFace = null;

    public BlockTrackerClientHandler() {
        super(CommonUpgradeHandlers.blockTrackerHandler);
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        int blockTrackRange = BLOCK_TRACKING_RANGE + Math.min(armorHandler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.RANGE.get()), 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int blockTrackRangeSq = blockTrackRange * blockTrackRange;

        long now = System.nanoTime();

        Player player = armorHandler.getPlayer();
        Level world = armorHandler.getPlayer().level;

        SearchClientHandler searcher = ClientArmorRegistry.getInstance()
                .getClientHandler(CommonUpgradeHandlers.searchHandler, SearchClientHandler.class);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < HARD_MAX_BLOCKS_PER_TICK; i++) {
            // 1% of a tick = 500,000ns
            if ((i & 0xff) == 0 && System.nanoTime() - now > ConfigHelper.client().armor.blockTrackerMaxTimePerTick.get() * 500_000L) {
                break;
            }

            nextScanPos(pos, player, blockTrackRange);

            if (!world.isLoaded(pos)) break;

            if (world.isEmptyBlock(pos)) continue;

            BlockEntity te = world.getBlockEntity(pos);

            if (!MinecraftForge.EVENT_BUS.post(new BlockTrackEvent(world, pos, te))) {
                try {
                    if (te != null && !TrackerBlacklistManager.isInventoryBlacklisted(te) && te.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                        searcher.onBlockTrackStart(te);
                    }
                    List<IBlockTrackEntry> entries = BlockTrackHandler.getInstance().getEntriesForCoordinate(world, pos, te);
                    if (!entries.isEmpty()) {
                        entries.forEach(entry -> blockTypeCountPartial.mergeInt(entry.getEntryID(), 1, Integer::sum));

                        // there's at least one tracker type relevant to this blockpos
                        RenderBlockTarget blockTarget = blockTargets.get(pos);
                        if (blockTarget != null) {
                            // we already have a tracker active for this pos so just ensure that it stays valid
                            blockTarget.markValid();
                            blockTarget.setTileEntity(te);
                        } else if (pos.distSqr(player.blockPosition()) < blockTrackRangeSq) {
                            // no tracker currently active for this pos - add a new one
                            addBlockTarget(new RenderBlockTarget(world, player, pos.immutable(), te, this));
                        }
                    }
                } catch (Throwable e) {
                    TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
                }
            }
        }

        checkBlockFocus(player, blockTrackRange);

        processTrackerEntries(blockTrackRange);

        updateTrackerText();
    }

    private void checkBlockFocus(Player player, int blockTrackRange) {
        focusedTarget = null;
        focusedFace = null;
        Vec3 eyes = player.getEyePosition(1.0f);
        Vec3 v = eyes;
        Vec3 lookVec = player.getLookAngle().scale(0.25);  // scale down to minimise clipping across a corner and missing the block
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < blockTrackRange * 4; i++) {
            v = v.add(lookVec);
            checkPos.set(v.x, v.y, v.z);
            if (blockTargets.containsKey(checkPos)) {
                BlockState state = player.level.getBlockState(checkPos);
                BlockHitResult brtr = state.getShape(player.level, checkPos).clip(eyes, v, checkPos);
                if (brtr != null && brtr.getType() == HitResult.Type.BLOCK) {
                    focusedTarget = blockTargets.get(checkPos);
                    focusedFace = brtr.getDirection();
                    break;
                }
            }
        }
    }

    public BlockPos getFocusedPos() {
        return focusedTarget == null ? null : focusedTarget.getPos();
    }

    public Direction getFocusedFace() {
        return focusedFace;
    }

    /**
     * Advance the scan position but be clever about it; we never need to scan blocks behind the player
     */
    private void nextScanPos(BlockPos.MutableBlockPos pos, Player player, int range) {
        Vec3 look = player.getLookAngle();
        Direction dir = Direction.getNearest(look.x(), look.y(), look.z());
        switch (dir) {
            case UP:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = 0;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case DOWN:
                if (++xOff > range) {
                    xOff = -range;
                    if (--yOff < -range) {
                        yOff = 0;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case EAST:
                if (++xOff > range) {
                    xOff = 0;
                    if (++yOff > range) {
                        yOff = -range;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case WEST:
                if (--xOff < -range) {
                    xOff = 0;
                    if (++yOff > range) {
                        yOff = -range;
                        if (++zOff > range) {
                            zOff = -range;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case NORTH:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = -range;
                        if (--zOff < -range) {
                            zOff = 0;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
            case SOUTH:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = -range;
                        if (++zOff > range) {
                            zOff = 0;
                            updateBlockTypeCounts();
                        }
                    }
                }
                break;
        }
        int minY = player.level.getMinBuildHeight();
        int maxY = player.level.getMaxBuildHeight();
        pos.set(player.getX() + xOff, Mth.clamp(player.getY() + yOff, minY, maxY), player.getZ() + zOff);
    }

    private void updateBlockTypeCounts() {
        blockTypeCount.clear();
        blockTypeCount.putAll(blockTypeCountPartial);
        blockTypeCountPartial.clear();
    }

    /**
     * Update all existing trackers and cull any which are either out of range or otherwise invalid
     * @param blockTrackRange the track range
     */
    private void processTrackerEntries(int blockTrackRange) {
        int rangeSq = (blockTrackRange + 5) * (blockTrackRange + 5);

        for (RenderBlockTarget blockTarget : blockTargets.values()) {
            blockTarget.tick();
            blockTarget.checkValidity(rangeSq);
        }

        blockTargets.values().removeIf(RenderBlockTarget::shouldRemoveNow);
    }

    private void updateTrackerText() {
        if (focusedTarget != null) {
            blockTrackInfo.setTitle(xlate("pneumaticcraft.armor.upgrade.block_tracker"));
            blockTrackInfo.setText(focusedTarget.getTitle());
        } else {
            blockTrackInfo.setTitle(xlate("pneumaticcraft.blockTracker.info.trackedBlocks"));

            List<Component> textList = new ArrayList<>();
            blockTypeCount.forEach((upgradeId, count) -> {
                if (count > 0 && WidgetKeybindCheckBox.get(upgradeId).checked) {
                    textList.add(xlate("pneumaticcraft.message.misc.countedItem", count, xlate(IArmorUpgradeHandler.getStringKey(upgradeId))));
                }
            });

            if (textList.isEmpty()) textList.add(xlate("pneumaticcraft.blockTracker.info.noTrackedBlocks"));
            blockTrackInfo.setText(textList);
        }
    }

    private void addBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.put(blockTarget.getPos(), blockTarget);
    }

    public int countBlockTrackersOfType(IBlockTrackEntry type) {
        return blockTypeCount.getOrDefault(type.getEntryID(), 0);
    }

    @Override
    public void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        blockTargets.values().forEach(t -> t.render(matrixStack, buffer, partialTicks));
    }

    @Override
    public void render2D(PoseStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
    }

    @Override
    public void reset() {
        blockTypeCountPartial.clear();
        blockTypeCount.clear();
        blockTargets.clear();
        blockTrackInfo = null;
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new BlockTrackOptions(screen,this);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat() {
        if (blockTrackInfo == null) {
            ItemStack icon = ModUpgrades.BLOCK_TRACKER.get().getItemStack();
            blockTrackInfo = ClientArmorRegistry.getInstance().makeHUDStatPanel(xlate("pneumaticcraft.blockTracker.info.trackedBlocks"), icon, this);
            blockTrackInfo.setMinimumContractedDimensions(0, 0);
            blockTrackInfo.setAutoLineWrap(false);
        }
        return blockTrackInfo;
    }

    @Override
    public StatPanelLayout getDefaultStatLayout() {
        return DEFAULT_STAT_LAYOUT;
    }

    public void hack() {
        if (focusedTarget != null) focusedTarget.hack();
    }

    public RenderBlockTarget getTargetForCoord(BlockPos pos) {
        return blockTargets.get(pos);
    }

    public boolean scroll(InputEvent.MouseScrollingEvent event) {
        for (RenderBlockTarget target : blockTargets.values()) {
            if (target.scroll(event)) {
                getAnimatedStat().mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResolutionChanged() {
        blockTrackInfo = null;
    }

    @Override
    public Collection<ResourceLocation> getSubKeybinds() {
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        builder.addAll(BlockTrackHandler.getInstance().getIDs());
        return builder.build();
    }

    @Override
    public String getSubKeybindCategory() {
        return Names.PNEUMATIC_KEYBINDING_CATEGORY_BLOCK_TRACKER;
    }

    @Override
    public void setOverlayColor(int color) {
        super.setOverlayColor(color);

        blockTargets.values().forEach(target -> target.updateColor(color));
    }
}
