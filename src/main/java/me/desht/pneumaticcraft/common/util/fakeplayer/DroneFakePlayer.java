package me.desht.pneumaticcraft.common.util.fakeplayer;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class DroneFakePlayer extends FakePlayer {
    private final IDroneBase drone;
    private boolean sneaking;

    public DroneFakePlayer(ServerWorld world, GameProfile name, IDroneBase drone) {
        super(world, name);
        this.drone = drone;
    }

    @Override
    public void giveExperiencePoints(int amount) {
        Vector3d pos = drone.getDronePos();
        ExperienceOrbEntity orb = new ExperienceOrbEntity(drone.world(), pos.x, pos.y, pos.z, amount);
        drone.world().addEntity(orb);
    }

    @Override
    public void playSound(SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        drone.playSound(soundEvent, category, volume, pitch);
    }

    @Override
    public boolean isSteppingCarefully() {
        return sneaking;
    }

    @Override
    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public void tick() {
        ticksSinceLastSwing++;  // without this, drone's melee will be hopeless
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
        // nothing
    }
}
