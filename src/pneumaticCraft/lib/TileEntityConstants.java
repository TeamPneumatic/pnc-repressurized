package pneumaticCraft.lib;

public class TileEntityConstants{
    public static final float CANNON_TURN_HIGH_SPEED = 3.0F;
    public static final float CANNON_TURN_LOW_SPEED = 0.5F;
    public static final float CANNON_SLOW_ANGLE = 20F;// the cannon starts to
                                                      // turn at the slow speed
                                                      // from x angle from
                                                      // destination angle.

    public static final float ELEVATOR_SPEED_SLOW = 0.02F;
    public static final float ELEVATOR_SPEED_FAST = 0.05F;
    public static final float ELEVATOR_SLOW_EXTENSION = 0.5F;

    public static final float PNEUMATIC_DOOR_SPEED_SLOW = 0.01F;
    public static final float PNEUMATIC_DOOR_SPEED_FAST = 0.04F;
    public static final float PNEUMATIC_DOOR_EXTENSION = 0.2F;
    public static final int RANGE_PNEUMATIC_DOOR_BASE = 2;

    public static final float ASSEMBLY_IO_UNIT_ARM_SPEED = 1.0F;
    public static final float ASSEMBLY_IO_UNIT_CLAW_SPEED = 0.05F;

    public static final float ASSEMBLY_DRILL_ACCELERATION = 1F;
    public static final float ASSEMBLY_DRILL_MAX_SPEED = 100F;

    public static final int LIGHT_BOX_0_100_TIME = 600; //seconds.

    public static final int PCB_ETCH_TIME = 300; //seconds

    public static final float NETWORK_NORMAL_BRIDGE_SPEED = 0.02F;//*100 % / tick
    public static final float NETWORK_AI_BRIDGE_SPEED = 0.03F;//*100 % / tick
    public static final float NETWORK_NOTE_RATING_MULTIPLIER = 1.2F;
    public static final int SECURITY_STATION_MAX_RANGE = 16;
    public static final int SECURITY_STATION_REBOOT_TIME = 1200;//ticks

    public static final double PACKET_UPDATE_DISTANCE = 64D;//maximum client distance before clients aren't being sent anymore from the server.
}
