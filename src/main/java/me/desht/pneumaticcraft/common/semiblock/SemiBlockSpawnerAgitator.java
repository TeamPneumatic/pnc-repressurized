package me.desht.pneumaticcraft.common.semiblock;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class SemiBlockSpawnerAgitator extends SemiBlockBasic<TileEntityMobSpawner>{

    public static final String ID = "spawner_agitator";
    
    @Override
    public boolean canPlace(){
        return getBlockState().getBlock() == Blocks.MOB_SPAWNER;
    }
    
    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            TileEntityMobSpawner te = getTileEntity();
            if(te != null){
                MobSpawnerBaseLogic spawnerLogic = te.getSpawnerBaseLogic();
                
                //Only tick the logic if it wasn't ticked already by the TE itself, to prevent double ticking.
                if(!Reflections.isActivated(spawnerLogic)){
                    
                    //Temporarily add a fake player to the world to trick the spawner into thinking there's a player nearby
                    GameProfile profile = new GameProfile(UUID.randomUUID(), "SemiBlockSpawnerAgitator");
                    EntityPlayer fakePlayer = FakePlayerFactory.get((WorldServer)world, profile);
                    fakePlayer.posX = getPos().getX();
                    fakePlayer.posY = getPos().getY();
                    fakePlayer.posZ = getPos().getZ();
                    
                    world.playerEntities.add(fakePlayer);
                    spawnerLogic.updateSpawner();
                    world.playerEntities.remove(fakePlayer);
                }                
            }
        }
    }
    
    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack) {
        super.onPlaced(player, stack);
        if (!world.isRemote) {
            setSpawnPersistentEntities(true);
        }
    }
    
    @Override
    public void invalidate(){
        super.invalidate();
        if (!world.isRemote) {
            setSpawnPersistentEntities(false);
        }
    }
    
    private void setSpawnPersistentEntities(boolean persistent){
        TileEntityMobSpawner te = getTileEntity();
        if(te != null){
            MobSpawnerBaseLogic spawnerLogic = te.getSpawnerBaseLogic();
            spawnerLogic.spawnData.getNbt().setBoolean("PersistenceRequired", persistent);
        }
    }

}
