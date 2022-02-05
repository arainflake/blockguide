package ca.naln1.rainflake.blockguide;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.settings.HotbarSnapshot;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.conditions.KilledByPlayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.model.b3d.B3DModel;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ca.naln1.rainflake.blockguide.commands.CommandHandler;

import static ca.naln1.rainflake.blockguide.Utilities.extendedSelector;
import static ca.naln1.rainflake.blockguide.Utilities.readContainer;
import static ca.naln1.rainflake.blockguide.Utilities.selectionHandler;
import static ca.naln1.rainflake.blockguide.Utilities.sendMessage;

@Mod.EventBusSubscriber(modid = BlockGuide.MOD_ID)
public class EventHandler {
    public static final NonNullList<KeyBinding> keyBinds = NonNullList.create();
    public static ItemStack lastHovered = ItemStack.EMPTY;
    static boolean isMenuOpened = false;

    @SubscribeEvent
    public static void onitemHoverEvent(ItemTooltipEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputTooltips.get()) return;

        String name = "";
        PlayerEntity player = event.getPlayer();
        ItemStack itemStack = event.getItemStack();

        //lastHovered = itemStack.getTooltipLines(player, ITooltipFlag.TooltipFlags.NORMAL).toString();

        if (itemStack != lastHovered) {
            lastHovered = itemStack;
            name += itemStack.getCount() + " ";
            name += itemStack.getItem().getName(itemStack).getString();

            Utilities.sendMessage(player, name);
        }
    }

    //keeps track of which hotbar keys are up/down
    static boolean[] hotbarToggle = new boolean[9];//{false, false, false, false, false, false, false, false, false};

    @SubscribeEvent
    public static void onKeybindPressEvent(TickEvent.ClientTickEvent event) {
        if (keyBinds.get(BlockGuide.KEYBINDS.POLL_SELECTOR.ordinal()).consumeClick()){
            PlayerEntity player = Minecraft.getInstance().player;
            extendedSelector(player, ConfigHandler.extendedRange.get(), true);
            //extendedSelector(player, ConfigHandler.extendedMode.get() ? ConfigHandler.extendedRange.get() : 6);
        }
        if (keyBinds.get(BlockGuide.KEYBINDS.POLL_CONTAINER.ordinal()).consumeClick()){
            PlayerEntity player = Minecraft.getInstance().player;
            readContainer(player, player.containerMenu);
        }
        KeyBinding[] keyBindings = Minecraft.getInstance().options.keyHotbarSlots;
        for (int x = 0; x < 9; x++) {//(KeyBinding key : Minecraft.getInstance().options.keyHotbarSlots) {
            if (keyBindings[x].isDown()) {
                if (!hotbarToggle[x]) {
                    hotbarToggle[x] = true;
                    getUseItem(x);
                }
            } else {
                hotbarToggle[x] = false;
            }
        }
    }

    public static void getUseItem(int selected) {
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputHotbarChange.get()) return;

        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack itemStack = player.inventory.getItem(selected);
        String name;

        if (itemStack.isEmpty()) {
            name = String.valueOf(selected);
        } else if (itemStack.getCount() == 1) {
            name = itemStack.getHoverName().getString();
        } else {
            name = itemStack.toString();
        }

        sendMessage(player, "Selected: " + name);
    }


    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onBlockBreak(BlockEvent.BreakEvent event){
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputBroke.get()) return;

        Block block = event.getState().getBlock();
        String name = block.getName().getString();
        String msg = "Broke: ";

        if (Blocks.FIRE.equals(block) || Blocks.SOUL_FIRE.equals(block)) {
            msg = "Put out: ";
        }

        sendMessage(event.getPlayer(), msg + name);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event){
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputPlaced.get()) return;
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntity();
        Block block = event.getPlacedBlock().getBlock();
        String name = block.getName().getString();
        String msg = "Placed: ";

        if (Blocks.FIRE.equals(block) || Blocks.SOUL_FIRE.equals(block)) {
            msg = "Lit: ";
        } else if (Blocks.FARMLAND.equals(block)) {
            msg = "Tilled: ";
        }

        sendMessage(player, msg + name);
    }

    //broken currently
    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onBucketFilled(FillBucketEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;

        if (event.getTarget() == null) return;

        String name = event.getEmptyBucket().getHoverName().getString();

        sendMessage(event.getPlayer(), "Filled bucket: " + name);
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;

        String name;

        if (event.getTo().equals(World.NETHER)) {
            name = "Nether";
        } else if (event.getTo().equals(World.END)) {
            name = "End";
        } else if (event.getTo().equals(World.OVERWORLD)) {
            name = "Overworld";
        } else {
            name = "Unknown";
        }

        sendMessage(event.getPlayer(), "Changed dimension to the " + name);
    }

    //broken currently
    //@SubscribeEvent
    public static void onSleepInBed(PlayerSleepInBedEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;


        if ((event.getResultStatus().equals(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE))
            || (event.getResultStatus().equals(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE))
            || (event.getResultStatus().equals(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW))
            || (event.getResultStatus().equals(PlayerEntity.SleepResult.NOT_SAFE))
            || (event.getResultStatus().equals(PlayerEntity.SleepResult.OBSTRUCTED))
            || (event.getResultStatus().equals(PlayerEntity.SleepResult.TOO_FAR_AWAY))
            || (event.getResultStatus().equals(PlayerEntity.SleepResult.OTHER_PROBLEM))){
            return;
        }

        sendMessage(event.getPlayer(), "Sleeping in bed...");
    }

    //broken currently
    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onWakeUp(SleepFinishedTimeEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;

        String msg = "day";
        long dayTime = event.getNewTime();

        if (dayTime >= 0 && dayTime < 6000){
            msg = "day";
        } else if (dayTime >= 6000 && dayTime < 12000){
            msg = "noon";
        } else if (dayTime >= 12000 && dayTime < 13000){
            msg = "sunset";
        } else if (dayTime >= 13000 && dayTime < 18000){
            msg = "night";
        } else if (dayTime >= 18000 && dayTime < 23000){
            msg = "midnight";
        } else if (dayTime >= 23000 && dayTime < 23999){
            msg = "sunrise";
        }

        if (!msg.equals("day")) return;

        PlayerEntity player = Minecraft.getInstance().player;

        sendMessage(player, "Time set to " + msg);
    }


    //broken currently
    //@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onItemUsed(LivingEntityUseItemEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;
        //if (!ConfigHandler.outputPlaced.get()) return;

        if (!(event.getEntity() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntity();

        //if client isnt this player, exit (cant test multiplayer easily)
        //if (Minecraft.getInstance().player != null && !player.is(Minecraft.getInstance().player)) return;

        Item item = event.getItem().getItem();
        String name = event.getItem().getHoverName().getString();
        String msg = "Used: ";

        if (Items.BOW == item) {
            msg = "Shot: ";
        }

        sendMessage(player, "Used: " + name);
    }

    @SubscribeEvent
    public static void openContainerEvent(PlayerContainerEvent event){
        if (event instanceof PlayerContainerEvent.Open) isMenuOpened = true;
        if (event instanceof PlayerContainerEvent.Close) isMenuOpened = false;
        //isMenuOpened = event instanceof PlayerContainerEvent.Open;
        if (!isMenuOpened) return;
        //if (Minecraft.getInstance().screen == null) return;
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputContainers.get()) return;

        PlayerEntity player = event.getPlayer();

        readContainer(player, event.getContainer());
    }

    @SubscribeEvent
    public static void pickupEvent(PlayerEvent.ItemPickupEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputPickups.get()) return;

        //Picked up: 34 Oak Logs
        String name = "Picked up: ";
        ItemStack itemStack = event.getStack();
        name += itemStack.getCount() + " ";
        name += itemStack.getItem().getName(itemStack).getString();
        sendMessage(event.getPlayer(), name);
    }

    @SubscribeEvent
    public static void onExtendedSelectorEvent(TickEvent.PlayerTickEvent event){
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.extendedMode.get()) return;
        if (!(ConfigHandler.outputBlocks.get() || ConfigHandler.outputEntities.get())) return;
        //dont keep sending messages when in a menu
        if (isMenuOpened) return;
        if (event.side.isServer()) return;

        extendedSelector(event.player, ConfigHandler.extendedRange.get(), false);
    }

    static BlockPos lastPos = BlockPos.ZERO;
    static int warnCount = 0;

    //broken currently
    //@SubscribeEvent
    public static void onPlayerMoveEvent(TickEvent.PlayerTickEvent event){
        if (!ConfigHandler.enableGuide.get()) return;

        PlayerEntity player = event.player;

        if (player.isUnderWater() || player.isFallFlying()) return;

        BlockPos currentPos = player.blockPosition();
        World world = player.level;

        //check if player has moved beyond last search area. return if not
        if (withinCube(currentPos, lastPos, 1, 1)) return;
        lastPos = currentPos;

        //test condition for fall distance
        Predicate<BlockPos> heightPredicate = (testPos) -> !fallCheck(world, testPos, player.getMaxFallDistance());

        //get all blocks in an area
        AxisAlignedBB aabb = (new AxisAlignedBB(currentPos)).inflate(2, 1, 2);
        ArrayList<BlockPos> dangerList = BlockPos.betweenClosedStream(aabb).collect(Collectors.toCollection(ArrayList::new));
        //BlockPos.spiralAround(currentPos, 3, Direction.NORTH, Direction.EAST).forEach(dangerList::add);

        //remove all blocks less than the fall distance
        dangerList.removeIf(heightPredicate);

        if (!dangerList.isEmpty()) {
            sendMessage(player, "Warning: " + warnCount);
            warnCount++;
        }
    }

    public static boolean notOnGround(PlayerEntity player) {
        return !player.getFeetBlockState().getMaterial().blocksMotion();
        //return !player.level.getBlockState(player.blockPosition().below()).getMaterial().isSolid();
    }

    public static boolean fallCheck(World world, BlockPos pos, int radius) {
        //loop through lower blocks, return closest danger, or list?
        BlockPos blockPos = pos;
        while(blockPos.getY() >= 0 && (pos.getY() - blockPos.getY()) <= radius ){
            BlockState blockstate = world.getBlockState(blockPos);
            if (blockstate.getMaterial().blocksMotion() || blockstate.getMaterial().isLiquid()) {
                return false;
            }
            blockPos = blockPos.below();
        }
        //true if block is too far down
        return true;
    }

    public static String getOffset(BlockPos pos1, BlockPos pos2) {
        return "";
    }

    public static boolean withinCube(BlockPos pos1, BlockPos pos2, int radius, int ydist) {
        return Math.abs(pos1.getX() - pos2.getX()) < radius
                && Math.abs(pos1.getZ() - pos2.getZ()) < radius
                && Math.abs(pos1.getY() - pos2.getY()) < ydist;
    }

    @SubscribeEvent
    public static void hotbarSelectEvent(InputEvent.MouseScrollEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputHotbarChange.get()) return;

        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        int selected = player.inventory.selected;
        if (event.getScrollDelta() == 1) {
            //selected--;
            selected = (selected == 0) ? 8 : selected - 1;
        } else if (event.getScrollDelta() == -1) {
            //selected++;
            selected = (selected == 8) ? 0 : selected + 1;
        }

        ItemStack itemStack = player.inventory.getItem(selected);
        String name;
        if (itemStack.isEmpty()) {
            name = String.valueOf(selected);
        } else if (itemStack.getCount() == 1) {
            name = itemStack.getHoverName().getString();
        } else {
            name = itemStack.toString();
        }

        sendMessage(player, "Selected: " + name);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void dropItemEvent(ItemTossEvent event) {
        if (!ConfigHandler.enableGuide.get()) return;
        if (!ConfigHandler.outputDrops.get()) return;

        String name;
        ItemStack itemStack = event.getEntityItem().getItem();
        name = (itemStack.getCount() == 1) ? itemStack.getHoverName().getString() : itemStack.toString();
        /*
        if (itemStack.getCount() == 1) {
            name = itemStack.getHoverName().getString();
        } else {
            name = itemStack.toString();
        }
         */
        sendMessage(event.getPlayer(), "Tossed: " + name);
    }

    @SubscribeEvent
    public static void onHighlightEvent(DrawHighlightEvent event){
        if (!ConfigHandler.enableGuide.get()) return;
        if (ConfigHandler.extendedMode.get()) return;

        BlockRayTraceResult brtr = null;
        EntityRayTraceResult ertr = null;

        PlayerEntity player = ((PlayerEntity) event.getInfo().getEntity());

        if(event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
            brtr = (BlockRayTraceResult) event.getTarget();
        } else if (event.getTarget().getType() == RayTraceResult.Type.ENTITY) {
            ertr = ((EntityRayTraceResult) event.getTarget());
        }

        selectionHandler(player, ertr, brtr, false);
    }

    @SubscribeEvent
    public static void registerCommandsEvent(final RegisterCommandsEvent event) {
        CommandHandler.register(event.getDispatcher());
    }

    public static void clientSetupEvent(final FMLClientSetupEvent event) {
        //register keybinds
        keyBinds.add(BlockGuide.KEYBINDS.POLL_SELECTOR.ordinal(), new KeyBinding("Poll the selector", -1, BlockGuide.MOD_ID));
        keyBinds.add(BlockGuide.KEYBINDS.POLL_CONTAINER.ordinal(), new KeyBinding("Poll the container", -1, BlockGuide.MOD_ID));
        ClientRegistry.registerKeyBinding(keyBinds.get(BlockGuide.KEYBINDS.POLL_SELECTOR.ordinal()));
        ClientRegistry.registerKeyBinding(keyBinds.get(BlockGuide.KEYBINDS.POLL_CONTAINER.ordinal()));
    }
}
