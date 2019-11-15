package me.desht.pneumaticcraft.common.semiblock;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class SemiBlockSpawnerAgitator extends SemiBlockBasic<MobSpawnerTileEntity>{

    public static final ResourceLocation ID = RL("spawner_agitator");
    private static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.randomUUID(), "SemiBlockSpawnerAgitator");
    
    public SemiBlockSpawnerAgitator(){
        super(MobSpawnerTileEntity.class);
    }
    
    @Override
    public boolean canPlace(Direction facing){
        return getBlockState().getBlock() == Blocks.SPAWNER;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote) {
            MobSpawnerTileEntity te = getTileEntity();
            if(te != null){
                AbstractSpawner spawnerLogic = te.getSpawnerBaseLogic();
                
                //Only tick the logic if it wasn't ticked already by the TE itself, to prevent double ticking.
                if(!Reflections.isActivated(spawnerLogic)){
                    
                    //Temporarily add a fake player to the world to trick the spawner into thinking there's a player nearby
                    FakePlayer fakePlayer = FakePlayerFactory.get((ServerWorld)world, FAKE_PLAYER_PROFILE);
                    if (fakePlayer.connection == null) {
                        fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
                    }
                    fakePlayer.world = world;
                    fakePlayer.posX = getPos().getX();
                    fakePlayer.posY = getPos().getY();
                    fakePlayer.posZ = getPos().getZ();
                    
                    world.addEntity(fakePlayer);
                    spawnerLogic.tick();
                    ((ServerWorld) world).removeEntity(fakePlayer);
                }                
            }
        }
    }
    
    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
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
        MobSpawnerTileEntity te = getTileEntity();
        if(te != null){
            AbstractSpawner spawnerLogic = te.getSpawnerBaseLogic();
            spawnerLogic.spawnData.getNbt().putBoolean("PersistenceRequired", persistent);
        }
    }

}
