package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.BlockTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.GuiBlockTrackOptions;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketDescriptionPacketRequest;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BlockTrackUpgradeHandler implements IUpgradeRenderHandler {
    private static final int BLOCK_TRACKING_RANGE = 30;
    private final List<RenderBlockTarget> blockTargets = new ArrayList<>();
    private GuiAnimatedStat blockTrackInfo;
    private int statX;
    private int statY;
    private boolean statLeftSided;
    private int[] blockTypeCount;
    private int ticksExisted;
    private int updateInterval = 20;
    private static final int MAX_TIME = 10;

    private long accTime;

    @Override
    public String getUpgradeName() {
        return "blockTracker";
    }

    @Override
    public void initConfig() {
        statX = ConfigHandler.helmetOptions.blockTrackerX;
        statY = ConfigHandler.helmetOptions.blockTrackerY;
        statLeftSided = ConfigHandler.helmetOptions.blockTrackerLeft;
    }

    @Override
    public void saveToConfig() {
        ConfigHandler.helmetOptions.blockTrackerX = statX = blockTrackInfo.getBaseX();
        ConfigHandler.helmetOptions.blockTrackerY = statY = blockTrackInfo.getBaseY();
        ConfigHandler.helmetOptions.blockTrackerLeft = statLeftSided = blockTrackInfo.isLeftSided();
        ConfigHandler.sync();
    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades) {
        ticksExisted++;

        SearchUpgradeHandler searchHandler = HUDHandler.instance().getSpecificRenderer(SearchUpgradeHandler.class);

        if (ticksExisted % updateInterval == 0) {
            int timeTaken = (int) accTime / updateInterval;
            updateInterval = updateInterval * timeTaken / MAX_TIME;
            if (updateInterval <= 1) updateInterval = 2;
            accTime = 0;
            ticksExisted = 0;
        }
        accTime -= System.currentTimeMillis();
        int blockTrackRange = BLOCK_TRACKING_RANGE + Math.min(rangeUpgrades, 5) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int baseX = (int) Math.floor(player.posX) - blockTrackRange;
        int baseY = (int) Math.floor(player.posY) - blockTrackRange + blockTrackRange * (ticksExisted % updateInterval) / (updateInterval / 2);
        int maxY = (int) Math.floor(player.posY) - blockTrackRange + blockTrackRange * (ticksExisted % updateInterval + 1) / (updateInterval / 2);
        baseY = MathHelper.clamp(baseY, 0, 255);
        maxY = MathHelper.clamp(maxY, 0, 255);
        int baseZ = (int) Math.floor(player.posZ) - blockTrackRange;
        IBlockAccess chunkCache = new ChunkCache(player.world, new BlockPos(baseX, baseY, baseZ), new BlockPos(baseX + 2 * blockTrackRange, maxY, baseZ + 2 * blockTrackRange), 0);
        for (int i = baseX; i <= baseX + 2 * blockTrackRange; i++) {
            for (int j = baseY; j < maxY; j++) {
                for (int k = baseZ; k <= baseZ + 2 * blockTrackRange; k++) {
                    if (player.getDistance(i, j, k) > blockTrackRange) continue;
                    BlockPos pos = new BlockPos(i, j, k);
                    TileEntity te = chunkCache.getTileEntity(pos);
                    if (MinecraftForge.EVENT_BUS.post(new BlockTrackEvent(player.world, pos, te))) continue;
                    if (searchHandler != null && te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                        searchHandler.checkInventoryForItems(te);
                    }
                    List<IBlockTrackEntry> entries = BlockTrackEntryList.instance.getEntriesForCoordinate(chunkCache, pos, te);
                    if (entries.isEmpty()) continue;
                    boolean inList = false;
                    for (int l = 0; l < blockTargets.size(); l++) {
                        if (blockTargets.get(l).isSameTarget(player.world, pos)) {
                            inList = true;
                            blockTargets.get(l).ticksExisted = Math.abs(blockTargets.get(l).ticksExisted);// cancel lost targets
                            blockTargets.get(l).setTileEntity(te);
                            break;
                        }
                    }
                    if (!inList) {
                        boolean sentUpdate = false;
                        for (IBlockTrackEntry entry : entries) {
                            if (entry.shouldBeUpdatedFromServer(te)) {
                                if (!sentUpdate) {
                                    NetworkHandler.sendToServer(new PacketDescriptionPacketRequest(pos));
                                    sentUpdate = true;
                                }
                            }
                        }
                        addBlockTarget(new RenderBlockTarget(player.world, player, pos, te, this));
                        for (IBlockTrackEntry entry : entries) {
                            if (countBlockTrackersOfType(entry) == entry.spamThreshold() + 1) {
                                HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("blockTracker.message.stopSpam", I18n.format(entry.getEntryName())), new ArrayList<String>(), 60, 0x7700AA00));
                            }
                        }
                    }
                }
            }
        }

        accTime += System.currentTimeMillis();
        for (int i = 0; i < blockTargets.size(); i++) {
            RenderBlockTarget blockTarget = blockTargets.get(i);

            boolean wasNegative = blockTarget.ticksExisted < 0;
            blockTarget.ticksExisted += CommonHUDHandler.getHandlerForPlayer(player).getSpeedFromUpgrades();
            if (blockTarget.ticksExisted >= 0 && wasNegative) blockTarget.ticksExisted = -1;

            blockTarget.update();
            if (blockTarget.getDistanceToEntity(player) > blockTrackRange + 5 || !blockTarget.isTargetStillValid()) {
                if (blockTarget.ticksExisted > 0) {
                    blockTarget.ticksExisted = -60;
                } else if (blockTarget.ticksExisted == -1) {
                    removeBlockTarget(i);
                    i--;
                }
            }
        }

        List<String> textList = new ArrayList<String>();
        RenderBlockTarget focusedTarget = null;
        for (RenderBlockTarget blockTarget : blockTargets) {
            if (blockTarget.isInitialized() && blockTarget.isPlayerLooking()) {
                focusedTarget = blockTarget;
                break;
            }
        }
        if (focusedTarget != null) {
            blockTrackInfo.setTitle(focusedTarget.stat.getTitle());
            textList.addAll(focusedTarget.textList);
        } else {
            blockTrackInfo.setTitle("Current tracked blocks:");
            if (blockTypeCount == null || ticksExisted % 40 == 0) {
                blockTypeCount = new int[BlockTrackEntryList.instance.trackList.size()];
                for (RenderBlockTarget target : blockTargets) {
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
        blockTargets.add(blockTarget);
    }

    private void removeBlockTarget(int index) {
        blockTargets.remove(index);
    }

    public int countBlockTrackersOfType(IBlockTrackEntry type) {
        int typeIndex = BlockTrackEntryList.instance.trackList.indexOf(type);
        if (blockTypeCount == null || typeIndex >= blockTypeCount.length) return 0;
        return blockTypeCount[typeIndex];
    }

    @Override
    public void render3D(float partialTicks) {
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (RenderBlockTarget blockTarget : blockTargets) {
            blockTarget.render(partialTicks);
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
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
        return PneumaticValues.USAGE_BLOCK_TRACKER * (1 + (float) Math.min(5, rangeUpgrades) * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / BLOCK_TRACKING_RANGE) * CommonHUDHandler.getHandlerForPlayer(player).getSpeedFromUpgrades();
    }

    @Override
    public IOptionPage getGuiOptionsPage() {
        return new GuiBlockTrackOptions(this);
    }

    @Override
    public GuiAnimatedStat getAnimatedStat() {
        if (blockTrackInfo == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft);
            blockTrackInfo = new GuiAnimatedStat(null, "Current tracked blocks:", CraftingRegistrator.getUpgrade(EnumUpgrade.BLOCK_TRACKER), statX != -1 ? statX : sr.getScaledWidth() - 2, statY, 0x3000AA00, null, statLeftSided);
            blockTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return blockTrackInfo;

    }

    public void hack() {
        for (RenderBlockTarget target : blockTargets) {
            target.hack();
        }
    }

    public RenderBlockTarget getTargetForCoord(BlockPos pos) {
        for (RenderBlockTarget target : blockTargets) {
            if (target.isSameTarget(null, pos)) return target;
        }
        return null;
    }

    public boolean scroll(MouseEvent event) {
        for (RenderBlockTarget target : blockTargets) {
            if (target.scroll(event)) {
                getAnimatedStat().handleMouseWheel(event.getDwheel());
                return true;
            }
        }
        return false;
    }

}
