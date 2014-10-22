package pneumaticCraft.common;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableBlock;
import pneumaticCraft.api.client.pneumaticHelmet.IHackableEntity;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.client.render.pneumaticArmor.hacking.HackableHandler;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketHackingBlockFinish;
import pneumaticCraft.common.network.PacketHackingEntityFinish;
import pneumaticCraft.common.util.WorldAndCoord;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CommonHUDHandler{
    private final HashMap<String, CommonHUDHandler> playerHudHandlers = new HashMap<String, CommonHUDHandler>();
    public int rangeUpgradesInstalled;
    public int speedUpgradesInstalled;
    public boolean[] upgradeRenderersInserted = new boolean[UpgradeRenderHandlerList.instance().upgradeRenderers.size()];
    public boolean[] upgradeRenderersEnabled = new boolean[UpgradeRenderHandlerList.instance().upgradeRenderers.size()];
    public int ticksExisted;
    public float helmetPressure;

    private int hackTime;
    private WorldAndCoord hackedBlock;
    private Entity hackedEntity;

    public static CommonHUDHandler getHandlerForPlayer(EntityPlayer player){
        CommonHUDHandler handler = PneumaticCraft.proxy.getCommonHudHandler().playerHudHandlers.get(player.getCommandSenderName());
        if(handler != null) return handler;
        PneumaticCraft.proxy.getCommonHudHandler().playerHudHandlers.put(player.getCommandSenderName(), new CommonHUDHandler());
        return getHandlerForPlayer(player);
    }

    @SideOnly(Side.CLIENT)
    public static CommonHUDHandler getHandlerForPlayer(){
        return getHandlerForPlayer(FMLClientHandler.instance().getClient().thePlayer);
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            EntityPlayer player = event.player;
            if(this == PneumaticCraft.proxy.getCommonHudHandler()) {
                getHandlerForPlayer(player).tickEnd(event);
            } else {
                ItemStack helmetStack = player.getCurrentArmor(3);
                if(helmetStack != null && helmetStack.getItem() == Itemss.pneumaticHelmet) {
                    helmetPressure = ((IPressurizable)helmetStack.getItem()).getPressure(helmetStack);
                    if(ticksExisted == 0) {
                        checkHelmetInventory(helmetStack);
                    }
                    ticksExisted++;
                    if(!player.worldObj.isRemote) {
                        if(ticksExisted > getStartupTime() && !player.capabilities.isCreativeMode) {
                            ((IPressurizable)helmetStack.getItem()).addAir(helmetStack, (int)-UpgradeRenderHandlerList.instance().getAirUsage(player, false));
                        }
                    }

                } else {
                    ticksExisted = 0;
                }
                if(!player.worldObj.isRemote) handleHacking(player);
            }
        }
    }

    private void handleHacking(EntityPlayer player){
        if(hackedBlock != null) {
            IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(hackedBlock, player);
            if(hackableBlock != null) {
                if(++hackTime >= hackableBlock.getHackTime(hackedBlock.world, hackedBlock.x, hackedBlock.y, hackedBlock.z, player)) {
                    hackableBlock.onHackFinished(player.worldObj, hackedBlock.x, hackedBlock.y, hackedBlock.z, player);
                    PneumaticCraft.proxy.getHackTickHandler().trackBlock(hackedBlock, hackableBlock);
                    NetworkHandler.sendToAllAround(new PacketHackingBlockFinish(hackedBlock), player.worldObj);
                    setHackedBlock(null);
                }
            } else {
                setHackedBlock(null);
            }
        } else if(hackedEntity != null) {
            IHackableEntity hackableEntity = HackableHandler.getHackableForEntity(hackedEntity, player);
            if(hackableEntity != null) {
                if(++hackTime >= hackableEntity.getHackTime(hackedEntity, player)) {
                    hackableEntity.onHackFinished(hackedEntity, player);
                    PneumaticCraft.proxy.getHackTickHandler().trackEntity(hackedEntity, hackableEntity);
                    NetworkHandler.sendToAllAround(new PacketHackingEntityFinish(hackedEntity), new NetworkRegistry.TargetPoint(hackedEntity.worldObj.provider.dimensionId, hackedEntity.posX, hackedEntity.posY, hackedEntity.posZ, 64));
                    setHackedEntity(null);
                }
            } else {
                setHackedEntity(null);
            }
        }
    }

    public void checkHelmetInventory(ItemStack helmetStack){
        ItemStack[] helmetStacks = ItemPneumaticArmor.getUpgradeStacks(helmetStack);
        rangeUpgradesInstalled = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_RANGE, helmetStack);
        speedUpgradesInstalled = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, helmetStack);
        upgradeRenderersInserted = new boolean[UpgradeRenderHandlerList.instance().upgradeRenderers.size()];
        for(int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
            upgradeRenderersInserted[i] = UpgradeRenderHandlerList.instance().upgradeRenderers.get(i).isEnabled(helmetStacks);
        }
    }

    public int getSpeedFromUpgrades(){
        return 1 + speedUpgradesInstalled;
    }

    public int getStartupTime(){
        return 200 / getSpeedFromUpgrades();
    }

    public void setHackedBlock(WorldAndCoord blockPos){
        hackedBlock = blockPos;
        hackedEntity = null;
        hackTime = 0;
    }

    public void setHackedEntity(Entity entity){
        hackedEntity = entity;
        hackedBlock = null;
        hackTime = 0;
    }
}
