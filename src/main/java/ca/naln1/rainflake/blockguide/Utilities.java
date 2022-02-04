package ca.naln1.rainflake.blockguide;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class Utilities {
    static RayTraceResult.Type rtrt = RayTraceResult.Type.MISS;
    static BlockPos hitBlockPos = BlockPos.ZERO;
    static BlockPos entBlockPos = BlockPos.ZERO;
    static Block hitBlock = Blocks.AIR;
    static Entity hitEntity = null;
    static int pollTicks = 0;


    public static void sendMessage(PlayerEntity player, String message){
        if (message.equals("")) return;
        player.sendMessage(ITextComponent.nullToEmpty(message), UUID.randomUUID());
        //NarratorChatListener.INSTANCE.updateNarratorStatus(NarratorStatus.ALL);
    }

    public static EntityRayTraceResult getPickedEntity(PlayerEntity player, int distance) {
        World world = player.level;
        Entity hitEntity = null;
        double d0 = distance;
        final ArrayList<Entity> entityList = new ArrayList<>();

        Vector3d vec_from = player.getEyePosition(1F);
        Vector3d look = player.getLookAngle().scale(distance);
        Vector3d vec_to = vec_from.add(look);

        AxisAlignedBB aabb = player.getBoundingBox().expandTowards(look).inflate(1);

        world.getEntities(player, aabb, Entity::isAlive).forEach((entity -> {
            AxisAlignedBB ebb = entity.getBoundingBox();
            Optional<Vector3d> optional = ebb.clip(vec_from, vec_to);
            if (optional.isPresent()){
                entityList.add(entity);
            }
        }));

        if (entityList.size() != 0){
            entityList.sort(Comparator.comparing(entity -> entity.distanceTo(player)));
            hitEntity = entityList.get(0);
        }

        return hitEntity == null ? null : new EntityRayTraceResult(hitEntity, hitEntity.position());
    }

    public static BlockRayTraceResult getNearestBlockCollided(PlayerEntity player, int distance) {
        Vector3d vec_from = player.getEyePosition(1F);
        Vector3d look = player.getLookAngle().scale(distance);
        Vector3d vec_to = vec_from.add(look);

        //crouch will allow raytracing through fluids
        RayTraceContext.FluidMode fluidMode = player.isCrouching() ? RayTraceContext.FluidMode.NONE : RayTraceContext.FluidMode.ANY;
        //dont raytrace collide with fluids if underwater/in fluids
        if (player.isUnderWater()) fluidMode = RayTraceContext.FluidMode.NONE;

        return player.level.clip(new RayTraceContext(vec_from, vec_to, RayTraceContext.BlockMode.OUTLINE, fluidMode, player));
    }

    public static boolean sameBlockPos(BlockPos pos1, BlockPos pos2){
        return ((pos1.getX() == pos2.getX()) && (pos1.getY() == pos2.getY()) && (pos1.getZ() == pos2.getZ()));
    }

    public static void readContainer(PlayerEntity player, Container container) {
        StringBuilder name = new StringBuilder();
        World world = player.level;

        BlockRayTraceResult rtr = getNearestBlockCollided(player, 6);
        //Block block = player.level.getBlockState(rtr.getBlockPos()).getBlock();
        //name.append(block.getName().getString()).append(" contents:\n");
        INamedContainerProvider cont = world.getBlockState(rtr.getBlockPos()).getMenuProvider(world, rtr.getBlockPos());
        if (cont == null) {
            name.append("Container contents:\n");
        } else  {
            name.append(cont.getDisplayName().getString()).append(" contents:\n");
        }

        for (Slot slot: container.slots) {
            ItemStack itemStack = slot.getItem();
            if (itemStack.getCount() > 0) {
                //cant reliably read only the non-player inventory parts
                //if (!slot.isSameInventory(player.inventoryMenu.getSlot(slot.getSlotIndex()))) {
                    name.append(itemStack.getCount()).append(" ").append(itemStack.getHoverName().getString()).append("\n");
                //}
            }
        }
        sendMessage(player, name.toString());
    }

    public static void extendedSelector(PlayerEntity player, int range, boolean polled) {
        EntityRayTraceResult ertr = getPickedEntity(player, range);
        BlockRayTraceResult brtr = getNearestBlockCollided(player, range);

        selectionHandler(player, ertr, brtr, polled);
    }

    public static void selectionHandler(PlayerEntity player, EntityRayTraceResult ertr, BlockRayTraceResult brtr, boolean polled) {
        //flags
        boolean hitTypeChanged = false;
        boolean blockPosChanged = false;
        boolean blockChanged = false;
        boolean entityPosChanged = false;
        boolean entityChanged = false;

        World world = player.level;
        String name = "";

        if (!polled) {
            //disable entities and blocks as nesessary
            if (!ConfigHandler.outputBlocks.get()) brtr = null;
            if (!ConfigHandler.outputEntities.get()) ertr = null;
        }

        boolean blockIsCloser = true;
        if (ertr == null && brtr == null) {
            return;
        }else if (brtr == null) {
            blockIsCloser = false;
        }else if (ertr == null) {
            blockIsCloser = true;
        }else {
            blockIsCloser = brtr.getLocation().distanceTo(player.position()) <= ertr.getLocation().distanceTo(player.position());
        }
        RayTraceResult rtr = blockIsCloser ? brtr : ertr;

        if(rtr.getType() == RayTraceResult.Type.BLOCK) {
            //block is closer

            Block block = world.getBlockState(brtr.getBlockPos()).getBlock();
            name = block.getName().getString();

            if (!polled) {
                //checks what the last hit type was
                if (rtrt != RayTraceResult.Type.BLOCK) {
                    hitTypeChanged = true;
                    rtrt = RayTraceResult.Type.BLOCK;
                }
                if (!sameBlockPos(brtr.getBlockPos(), hitBlockPos)) {
                    hitBlockPos = brtr.getBlockPos();
                    blockPosChanged = true;
                }
                if (!block.is(hitBlock)) {
                    hitBlock = block;
                    blockChanged = true;
                }
            }
        } else if (rtr.getType() == RayTraceResult.Type.ENTITY) {
            //entity is closer

            Entity entity = ertr.getEntity();
            name = entity.getDisplayName().getString();

            if (!polled) {
                //checks what the last hit type was
                if (rtrt != RayTraceResult.Type.ENTITY) {
                    hitTypeChanged = true;
                    rtrt = RayTraceResult.Type.ENTITY;
                }
                if (!sameBlockPos(entity.blockPosition(), entBlockPos)) {
                    entBlockPos = entity.blockPosition();
                    entityPosChanged = true;
                }
                if (!entity.is(hitEntity)) {
                    hitEntity = entity;
                    entityChanged = true;
                }
            }
        } else if (player.getLookAngle().y() > 0) {
            name = "Sky";
        }

        //if (highlightTicks % 80 == 0) {
        pollTicks = 0;
        if (blockPosChanged || blockChanged || entityPosChanged || entityChanged || hitTypeChanged || polled) {
            if (rtr.getType() == RayTraceResult.Type.BLOCK) {
                sendMessage(player, name);
            } else if (rtr.getType() == RayTraceResult.Type.ENTITY) {
                sendMessage(player, name);
            }
        }
        //}
        pollTicks++;
    }
}
