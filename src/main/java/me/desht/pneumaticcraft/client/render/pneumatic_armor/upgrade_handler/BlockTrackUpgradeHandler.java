package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.BlockTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiBlockTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockTrackUpgradeHandler implements IUpgradeRenderHandler {
    static final int BLOCK_TRACKING_RANGE = 30;
    private static final int HARD_MAX_BLOCKS_PER_TICK = 50000;

    private final Map<BlockPos, RenderBlockTarget> blockTargets = new HashMap<>();
    private GuiAnimatedStat blockTrackInfo;
    private int[] blockTypeCount;
    private int ticksExisted;
    private int xOff = 0, yOff = 0, zOff = 0;

    @Override
    public String getUpgradeName() {
        return "blockTracker";
    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades) {
        SearchUpgradeHandler searchHandler = HUDHandler.instance().getSpecificRenderer(SearchUpgradeHandler.class);

        int blockTrackRange = BLOCK_TRACKING_RANGE + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;

        long now = System.nanoTime();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < HARD_MAX_BLOCKS_PER_TICK; i++) {
            // 1% of a tick = 500,000ns
            if ((i & 0xff) == 0 && System.nanoTime() - now > ConfigHandler.client.blockTrackerMaxTimePerTick * 500000) {
                break;
            }

            nextScanPos(pos, player, blockTrackRange);

            if (!player.world.isBlockLoaded(pos)) break;

            TileEntity te = player.world.getTileEntity(pos);
            if (!MinecraftForge.EVENT_BUS.post(new BlockTrackEvent(player.world, pos, te))) {
                if (searchHandler != null && te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    searchHandler.checkInventoryForItems(te, null, GuiKeybindCheckBox.isHandlerEnabled(searchHandler));
                }
                List<IBlockTrackEntry> entries = BlockTrackEntryList.instance.getEntriesForCoordinate(player.world, pos, te);
                if (!entries.isEmpty()) {
                    // there's at least one tracker type relevant to this blockpos
                    RenderBlockTarget blockTarget = blockTargets.get(pos);
                    if (blockTarget != null) {
                        // we already have a tracker active for this pos
                        blockTarget.ticksExisted = Math.abs(blockTarget.ticksExisted); // cancel lost target
                        blockTarget.setTileEntity(te);
                        break;
                    } else {
                        // no tracker currently active - add one
                        boolean sentUpdateRequest = false;
                        for (IBlockTrackEntry entry : entries) {
                            if (entry.shouldBeUpdatedFromServer(te)) {
                                if (!sentUpdateRequest) {
                                    NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(pos));
                                    sentUpdateRequest = true;
                                }
                            }
                        }
                        addBlockTarget(new RenderBlockTarget(player.world, player, pos.toImmutable(), te, this));
                        for (IBlockTrackEntry entry : entries) {
                            if (countBlockTrackersOfType(entry) == entry.spamThreshold() + 1) {
                                HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("blockTracker.message.stopSpam", I18n.format(entry.getEntryName())), new ArrayList<>(), 60, 0x7700AA00));
                            }
                        }
                    }
                }
            }
        }

        RenderBlockTarget focusedTarget = processTrackerEntries(player, blockTrackRange);

        updateTrackerText(focusedTarget);
    }

    /**
     * Advance the scan position but be clever about it; we never need to scan blocks behind the player
     */
    private void nextScanPos(BlockPos.MutableBlockPos pos, EntityPlayer player, int range) {
        EnumFacing dir = PneumaticCraftUtils.getDirectionFacing(player, true);
        switch (dir) {
            case UP:
                if (++xOff > range) {
                    xOff = -range;
                    if (++yOff > range) {
                        yOff = 0;
                        if (++zOff > range) {
                            zOff = -range;
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
                        }
                    }
                }
                break;
        }
        pos.setPos(player.posX + xOff, MathHelper.clamp(player.posY + yOff, 0, 255), player.posZ + zOff);
    }

    /**
     * Update all existing trackers and cull any which are either out of range or otherwise invalid
     * @param player the player
     * @param blockTrackRange the track range
     * @return the render target that the player is focusing on, if any
     */
    private RenderBlockTarget processTrackerEntries(EntityPlayer player, int blockTrackRange) {
        RenderBlockTarget focusedTarget = null;

        List<RenderBlockTarget> toRemove = new ArrayList<>();
        for (RenderBlockTarget blockTarget : blockTargets.values()) {
            boolean wasNegative = blockTarget.ticksExisted < 0;
            blockTarget.ticksExisted += CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EntityEquipmentSlot.HEAD);
            if (blockTarget.ticksExisted >= 0 && wasNegative) blockTarget.ticksExisted = -1;

            blockTarget.update();
            if (blockTarget.isInitialized() && blockTarget.isPlayerLooking()) {
                focusedTarget = blockTarget;
            }
            if (blockTarget.getDistanceToEntity(player) > blockTrackRange + 5 || !blockTarget.isTargetStillValid()) {
                if (blockTarget.ticksExisted > 0) {
                    blockTarget.ticksExisted = -60;
                } else if (blockTarget.ticksExisted == -1) {
                    toRemove.add(blockTarget);
                }
            }
        }
        toRemove.forEach(this::removeBlockTarget);

        return focusedTarget;
    }

    private void updateTrackerText(RenderBlockTarget focusedTarget) {
        List<String> textList = new ArrayList<>();

        if (focusedTarget != null) {
            blockTrackInfo.setTitle(focusedTarget.stat.getTitle());
            textList.addAll(focusedTarget.textList);
        } else {
            blockTrackInfo.setTitle("Current tracked blocks:");
            if (blockTypeCount == null || ticksExisted % 40 == 0) {
                blockTypeCount = new int[BlockTrackEntryList.instance.trackList.size()];
                for (RenderBlockTarget target : blockTargets.values()) {
                    for (IBlockTrackEntry validEntry : target.getApplicableEntries()) {
                        blockTypeCount[BlockTrackEntryList.instance.trackList.indexOf(validEntry)]++;
                    }
                }
            }
            for (int i = 0; i < blockTypeCount.length; i++) {
                if (blockTypeCount[i] > 0) {
                    textList.add(blockTypeCount[i] + " " + I18n.format(BlockTrackEntryList.instance.trackList.get(i).getEntryName()));
                }
            }
            if (textList.size() == 0) textList.add("Tracking no blocks currently.");
        }

        blockTrackInfo.setText(textList);
    }

    private void addBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.put(blockTarget.getPos(), blockTarget);
    }

    private void removeBlockTarget(RenderBlockTarget blockTarget) {
        blockTargets.remove(blockTarget.getPos());
    }

    public int countBlockTrackersOfType(IBlockTrackEntry type) {
        int typeIndex = BlockTrackEntryList.instance.trackList.indexOf(type);
        if (blockTypeCount == null || typeIndex >= blockTypeCount.length) return 0;
        return blockTypeCount[typeIndex];
    }

    @Override
    public void render3D(float partialTicks) {
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (RenderBlockTarget blockTarget : blockTargets.values()) {
            blockTarget.render(partialTicks);
        }
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled) {
    }

    @Override
    public Item[] getRequiredUpgrades() {
        return new Item[]{Itemss.upgrades.get(EnumUpgrade.BLOCK_TRACKER)};
    }

    @Override
    public void reset() {
        blockTypeCount = null;
        ticksExisted = 0;
        blockTrackInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player) {
        return PneumaticValues.USAGE_BLOCK_TRACKER * (1 + (float) Math.min(5, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / BLOCK_TRACKING_RANGE) * CommonArmorHandler.getHandlerForPlayer(player).getSpeedFromUpgrades(EntityEquipmentSlot.HEAD);
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiBlockTrackOptions(this);
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot() {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    public GuiAnimatedStat getAnimatedStat() {
        if (blockTrackInfo == null) {
            GuiAnimatedStat.StatIcon icon = GuiAnimatedStat.StatIcon.of(CraftingRegistrator.getUpgrade(EnumUpgrade.BLOCK_TRACKER));
            blockTrackInfo = new GuiAnimatedStat(null, "Current tracked blocks:",
                    icon, 0x3000AA00, null, ArmorHUDLayout.INSTANCE.blockTrackerStat);
            blockTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return blockTrackInfo;

    }

    public void hack() {
        for (RenderBlockTarget target : blockTargets.values()) {
            target.hack();
        }
    }

    public RenderBlockTarget getTargetForCoord(BlockPos pos) {
        for (RenderBlockTarget target : blockTargets.values()) {
            if (target.isSameTarget(null, pos)) return target;
        }
        return null;
    }

    public boolean scroll(MouseEvent event) {
        for (RenderBlockTarget target : blockTargets.values()) {
            if (target.scroll(event)) {
                getAnimatedStat().handleMouseWheel(event.getDwheel());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResolutionChanged() {
        blockTrackInfo = null;
    }
}
