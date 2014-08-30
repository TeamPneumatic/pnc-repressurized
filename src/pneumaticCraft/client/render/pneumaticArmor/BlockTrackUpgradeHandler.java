package pneumaticCraft.client.render.pneumaticArmor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.gui.pneumaticHelmet.GuiBlockTrackOptions;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.PneumaticValues;

public class BlockTrackUpgradeHandler implements IUpgradeRenderHandler{
    private static final int BLOCK_TRACKING_RANGE = 30;
    private final List<RenderBlockTarget> blockTargets = new ArrayList<RenderBlockTarget>();;
    private GuiAnimatedStat blockTrackInfo;
    private int statX;
    private int statY;
    private boolean statLeftSided;
    public int[] blockTypeCount;
    private int ticksExisted;

    @Override
    public String getUpgradeName(){
        return "blockTracker";
    }

    @Override
    public void initConfig(Configuration config){
        statX = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Block_Tracker", "stat X", -1).getInt();
        statY = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Block_Tracker", "stat Y", 46).getInt();
        statLeftSided = config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Block_Tracker", "stat leftsided", true).getBoolean(true);
    }

    @Override
    public void saveToConfig(){
        Configuration config = Config.config;
        config.load();
        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Block_Tracker", "stat X", -1).set(blockTrackInfo.getBaseX());
        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Block_Tracker", "stat Y", 46).set(blockTrackInfo.getBaseY());
        config.get("Helmet_Options" + Configuration.CATEGORY_SPLITTER + "Block_Tracker", "stat leftsided", true).set(blockTrackInfo.isLeftSided());
        statX = blockTrackInfo.getBaseX();
        statY = blockTrackInfo.getBaseY();
        statLeftSided = blockTrackInfo.isLeftSided();
        config.save();
    }

    @Override
    public void update(EntityPlayer player, int rangeUpgrades){
        ticksExisted++;

        SearchUpgradeHandler searchHandler = (SearchUpgradeHandler)HUDHandler.instance().getSpecificRenderer(SearchUpgradeHandler.class);

        int blockTrackRange = BLOCK_TRACKING_RANGE + rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE;
        int baseX = (int)Math.floor(player.posX) - blockTrackRange;
        int baseY = (int)Math.floor(player.posY) - blockTrackRange + blockTrackRange * (ticksExisted % 20) / 10;
        int maxY = (int)Math.floor(player.posY) - blockTrackRange + blockTrackRange * (ticksExisted % 20 + 1) / 10;
        int baseZ = (int)Math.floor(player.posZ) - blockTrackRange;
        for(int i = baseX; i <= baseX + 2 * blockTrackRange; i++) {
            for(int j = baseY; j < maxY; j++) {
                for(int k = baseZ; k <= baseZ + 2 * blockTrackRange; k++) {
                    if(player.getDistance(i, j, k) > blockTrackRange) continue;
                    TileEntity te = player.worldObj.getTileEntity(i, j, k);
                    if(searchHandler != null && te instanceof IInventory) {
                        searchHandler.checkInventoryForItems(te);
                    }
                    List<IBlockTrackEntry> entries = BlockTrackEntryList.instance.getEntriesForCoordinate(player.worldObj, i, j, k);
                    if(entries.isEmpty()) continue;
                    boolean inList = false;
                    for(int l = 0; l < blockTargets.size(); l++) {
                        if(blockTargets.get(l).isSameTarget(player.worldObj, i, j, k)) {
                            inList = true;
                            blockTargets.get(l).ticksExisted = Math.abs(blockTargets.get(l).ticksExisted);// cancel lost targets
                            break;
                        }
                    }
                    if(!inList) {
                        addBlockTarget(new RenderBlockTarget(player.worldObj, player, i, j, k, this));
                        for(IBlockTrackEntry entry : entries) {
                            if(countBlockTrackersOfType(entry) == entry.spamThreshold() + 1) {
                                HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("blockTracker.message.stopSpam", I18n.format(entry.getEntryName())), new ArrayList<String>(), 60, 0x7700AA00));
                            }
                        }
                    }
                }
            }
        }
        for(int i = 0; i < blockTargets.size(); i++) {
            RenderBlockTarget blockTarget = blockTargets.get(i);

            boolean wasNegative = blockTarget.ticksExisted < 0;
            blockTarget.ticksExisted += CommonHUDHandler.getHandlerForPlayer(player).getSpeedFromUpgrades();
            if(blockTarget.ticksExisted >= 0 && wasNegative) blockTarget.ticksExisted = -1;

            blockTarget.update();
            if(blockTarget.getDistanceToEntity(player) > blockTrackRange + 5 || !blockTarget.isTargetStillValid()) {
                if(blockTarget.ticksExisted > 0) {
                    blockTarget.ticksExisted = -60;
                } else if(blockTarget.ticksExisted == -1) {
                    removeBlockTarget(i);
                    i--;
                }
            }
        }

        List<String> textList = new ArrayList<String>();
        RenderBlockTarget focusedTarget = null;
        for(RenderBlockTarget blockTarget : blockTargets) {
            if(blockTarget.isInitialized() && blockTarget.isPlayerLooking()) {
                focusedTarget = blockTarget;
                break;
            }
        }
        if(focusedTarget != null) {
            blockTrackInfo.setTitle(focusedTarget.stat.getTitle());
            textList.addAll(focusedTarget.textList);
        } else {
            blockTrackInfo.setTitle("Current tracked blocks:");
            if(blockTypeCount == null || ticksExisted % 40 == 0) {
                blockTypeCount = new int[BlockTrackEntryList.instance.trackList.size()];
                for(RenderBlockTarget target : blockTargets) {
                    for(IBlockTrackEntry validEntry : target.getApplicableEntries()) {
                        blockTypeCount[BlockTrackEntryList.instance.trackList.indexOf(validEntry)]++;
                    }
                }
            }
            for(int i = 0; i < blockTypeCount.length; i++) {
                if(blockTypeCount[i] > 0) {
                    textList.add(blockTypeCount[i] + " " + I18n.format(BlockTrackEntryList.instance.trackList.get(i).getEntryName()));
                }
            }
            if(textList.size() == 0) textList.add("Tracking no blocks currently.");
        }

        blockTrackInfo.setText(textList);

    }

    private void addBlockTarget(RenderBlockTarget blockTarget){
        blockTargets.add(blockTarget);
    }

    private void removeBlockTarget(int index){
        blockTargets.remove(index);
    }

    public int countBlockTrackersOfType(IBlockTrackEntry type){
        int typeIndex = BlockTrackEntryList.instance.trackList.indexOf(type);
        if(blockTypeCount == null || typeIndex >= blockTypeCount.length) return 0;
        return blockTypeCount[typeIndex];
    }

    @Override
    public void render3D(float partialTicks){
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for(RenderBlockTarget blockTarget : blockTargets) {
            blockTarget.render(partialTicks);
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
    }

    @Override
    public void render2D(float partialTicks, boolean helmetEnabled){}

    @Override
    public boolean isEnabled(ItemStack[] upgradeStacks){
        for(ItemStack stack : upgradeStacks) {
            if(stack != null && stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER) return true;
        }
        return false;
    }

    @Override
    public void reset(){
        blockTypeCount = null;
        ticksExisted = 0;
        blockTrackInfo = null;
    }

    @Override
    public float getEnergyUsage(int rangeUpgrades, EntityPlayer player){
        return PneumaticValues.USAGE_BLOCK_TRACKER * (1 + (float)rangeUpgrades * PneumaticValues.RANGE_UPGRADE_HELMET_RANGE_INCREASE / BLOCK_TRACKING_RANGE) * CommonHUDHandler.getHandlerForPlayer(player).getSpeedFromUpgrades();
    }

    @Override
    public IOptionPage getGuiOptionsPage(){
        return new GuiBlockTrackOptions(this);
    }

    @Override
    public GuiAnimatedStat getAnimatedStat(){
        if(blockTrackInfo == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
            blockTrackInfo = new GuiAnimatedStat(null, "Current tracked blocks:", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER), statX != -1 ? statX : sr.getScaledWidth() - 2, statY, 0x3000AA00, null, statLeftSided);
            blockTrackInfo.setMinDimensionsAndReset(0, 0);
        }
        return blockTrackInfo;

    }

    public void hack(){
        for(RenderBlockTarget target : blockTargets) {
            target.hack();
        }
    }

    public RenderBlockTarget getTargetForCoord(int x, int y, int z){
        for(RenderBlockTarget target : blockTargets) {
            if(target.isSameTarget(null, x, y, z)) return target;
        }
        return null;
    }

}
