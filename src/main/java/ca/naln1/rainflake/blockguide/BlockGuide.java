package ca.naln1.rainflake.blockguide;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BlockGuide.MOD_ID)
public class BlockGuide
{
    public static final String MOD_ID = "blockguide";
    //private static final Logger LOGGER = LogManager.getLogger();

    public BlockGuide() {
        //IMPLEMENTED
        //blocks/entities to chat, should say if is sky or air?
        //pickups to chat
        //sneak to toggle fluid mode
        //toggle extended mode
        //set extended range
        //read container contents by pressing c
        //press x to poll raycast for block/entity
        //tooltips on hover to chat
        //output selected hotbar itemstack when scrolling or otherwise selecting a new slot
        //output drops

        //NOT IMPLEMENTED
        //output placed block, used item, broke block
        //output killed mob
        //press z for distance to target? or add command for distance readout toggle
        //nearby hostile mobs in view to chat
        //to chat or narrator
        //nearby falls >= fall damage height(or jump height, or configurable) to chat
        // ; gets the y-lvl of your current block, and checks the nearby blocks based on movement speed
        // disabled while flying or swimming

        //registers our config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON);

        //mod events
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventHandler::clientSetupEvent);
    }

    public enum KEYBINDS {
        POLL_SELECTOR,
        POLL_CONTAINER,
    }
}