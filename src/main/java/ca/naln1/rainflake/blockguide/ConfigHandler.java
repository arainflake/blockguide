package ca.naln1.rainflake.blockguide;


import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON;

    public static final ForgeConfigSpec.BooleanValue outputBlocks;
    public static final ForgeConfigSpec.BooleanValue outputEntities;
    public static final ForgeConfigSpec.BooleanValue outputPickups;
    public static final ForgeConfigSpec.BooleanValue outputContainers;
    public static final ForgeConfigSpec.BooleanValue outputTooltips;
    public static final ForgeConfigSpec.BooleanValue outputHotbarChange;
    public static final ForgeConfigSpec.BooleanValue outputDrops;
    public static final ForgeConfigSpec.BooleanValue extendedMode;
    public static final ForgeConfigSpec.BooleanValue enableGuide;
    public static final ForgeConfigSpec.BooleanValue warnNearbyFalls;
    public static final ForgeConfigSpec.BooleanValue warnNearbyMobs;
    public static final ForgeConfigSpec.IntValue extendedRange;

    static {
        String desc;
        BUILDER.comment("Common config settings").push("common");

        desc = "Enable Block Guide by default";
        enableGuide = BUILDER.comment(desc).define("enableGuide", true);

        desc = "Output targeted blocks to chat";
        outputBlocks = BUILDER.comment(desc).define("outputBlocks", true);

        desc = "Output targeted entities to chat";
        outputEntities = BUILDER.comment(desc).define("outputEntities", true);

        desc = "Output picked up items to chat";
        outputPickups = BUILDER.comment(desc).define("outputPickups", true);

        desc = "Output contents of chests and other containers to chat";
        outputContainers = BUILDER.comment(desc).define("outputContainers", true);

        desc = "Output tooltips when hovering over items in containers to chat";
        outputTooltips = BUILDER.comment(desc).define("outputTooltips", true);

        desc = "Output selected itemstack to chat when changed by scrolling or keybinds";
        outputHotbarChange = BUILDER.comment(desc).define("outputHotbarChange", true);

        desc = "Output dropped itemstack to chat";
        outputDrops = BUILDER.comment(desc).define("outputDrops", true);

        desc = "Enable extended mode";
        extendedMode = BUILDER.comment(desc).define("extendedMode", false);

        desc = "Warn if any falls that would damage the player. disabled in creative";
        warnNearbyFalls = BUILDER.comment(desc).define("warnNearbyFalls", false);

        desc = "Warn if any hostile mobs get close. disabled in creative";
        warnNearbyMobs = BUILDER.comment(desc).define("warnNearbyMobs", false);

        desc = "Extended selection range (large values may lower fps)";
        extendedRange = BUILDER.comment(desc).defineInRange("extendedRange", 32, 6, 256);

        BUILDER.pop();
        COMMON = BUILDER.build();
    }
}
