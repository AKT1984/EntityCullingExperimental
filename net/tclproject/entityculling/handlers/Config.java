package net.tclproject.entityculling.handlers;

import java.io.File;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config {
    private static final String GENERIC_CATEGORY = "Generic";

    public static boolean renderNametagsThroughWalls = true;
    public static String[] blockEntityWhitelist;
    public static int tracingDistance = 128;
    public static boolean debugMode = false;
    public static boolean aggressiveMode = false;
    public static int sleepDelay = 10;
    public static int hitboxLimit = 50;

    public static void load(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), "EntityCulling.cfg");
        Configuration config = new Configuration(configFile);

        config.load();

        tracingDistance = config.get(GENERIC_CATEGORY, "tracingDistance", 128,
                "128 works out to be roughly equal to Minecraft's defaults").getInt();
        sleepDelay = config.get(GENERIC_CATEGORY, "sleepDelay", 10,
                "The delay between async pathtracing runs that update which TEs need to be culled").getInt();
        hitboxLimit = config.get(GENERIC_CATEGORY, "hitboxLimit", 50,
                "Limit to a hitbox (anything larger than this will be considered too big to cull").getInt();

        String blockEntityWhitelistString = config.get(GENERIC_CATEGORY, "entityWhitelist", "tile.beacon",
                "Comma-separated list of entities and blocks whitelisted from this mod, e.g. tile.beacon").getString();
        blockEntityWhitelist = blockEntityWhitelistString.split(",");

        debugMode = config.get(GENERIC_CATEGORY, "debugMode", false,
                "Try this before sending an issue report.").getBoolean(false);
        aggressiveMode = config.get(GENERIC_CATEGORY, "aggressiveMode", false,
                "Aggressively calculate bounding box culling with no breathing room. May result in additional performance at the cost of stability and/or graphics issues.").getBoolean(false);

        config.save();
    }
}
