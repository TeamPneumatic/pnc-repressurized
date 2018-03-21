package me.desht.pneumaticcraft.common.semiblock;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class SemiBlockSpawnerAgitator extends SemiBlockBasic<TileEntityMobSpawner>{

    public static final String ID = "spawner_agitator";
    public static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.randomUUID(), "SemiBlockSpawnerAgitator");
    
    @Override
    public boolean canPlace(EnumFacing facing){
        return getBlockState().getBlock() == Blocks.MOB_SPAWNER;
    }
    
    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            TileEntity te = getTileEntity();
            // can't assume the tile entity is definitely a mob spawner -
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/133
            if (te instanceof TileEntityMobSpawner){
                MobSpawnerBaseLogic spawnerLogic = ((TileEntityMobSpawner) te).getSpawnerBaseLogic();
                
                //Only tick the logic if it wasn't ticked already by the TE itself, to prevent double ticking.
                if(!Reflections.isActivated(spawnerLogic)){
                    
                    //Temporarily add a fake player to the world to trick the spawner into thinking there's a player nearby
                    EntityPlayer fakePlayer = FakePlayerFactory.get((WorldServer)world, FAKE_PLAYER_PROFILE);
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
    public void onPlaced(EntityPlayer player, ItemStack stack, EnumFacing facing) {
        super.onPlaced(player, stack, facing);
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
        TileEntity te = getTileEntity();
        // can't assume the tile entity is definitely a mob spawner -
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/133
        if (te instanceof TileEntityMobSpawner) {
            MobSpawnerBaseLogic spawnerLogic = ((TileEntityMobSpawner) te).getSpawnerBaseLogic();
            spawnerLogic.spawnData.getNbt().setBoolean("PersistenceRequired", persistent);
        }
    }

}
