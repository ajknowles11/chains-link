package lettas.chains_link.mixin;

import lettas.chains_link.chains;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ChainBlock.class, priority = 420)
public abstract class clMixinChainBlock extends PillarBlock implements Waterloggable {
    private static final BooleanProperty WATERLOGGED;
    private static final BooleanProperty EDIT_DOWN;
    private static final BooleanProperty EDIT_UP;
    private static final BooleanProperty EDIT_NORTH;
    private static final BooleanProperty EDIT_SOUTH;
    private static final BooleanProperty EDIT_WEST;
    private static final BooleanProperty EDIT_EAST;
    private static final VoxelShape DOWN_SHAPE;
    private static final VoxelShape UP_SHAPE;
    private static final VoxelShape NORTH_SHAPE;
    private static final VoxelShape SOUTH_SHAPE;
    private static final VoxelShape WEST_SHAPE;
    private static final VoxelShape EAST_SHAPE;
    private static final VoxelShape CENTER_SHAPE;

    public clMixinChainBlock(Settings settings) {super(settings);}

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void clMixinChainBlock(AbstractBlock.Settings settings, CallbackInfo ci) {
        this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.Y)).with(WATERLOGGED, false)).with(EDIT_DOWN, false)).with(EDIT_UP, false)).with(EDIT_NORTH, false)).with(EDIT_SOUTH, false)).with(EDIT_WEST, false)).with(EDIT_EAST, false));
    }

    @Overwrite
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape CHAIN_SHAPE = CENTER_SHAPE;
        if(state.get(EDIT_DOWN) != (state.get(AXIS) == Direction.Axis.Y))
            CHAIN_SHAPE = VoxelShapes.union(CHAIN_SHAPE, DOWN_SHAPE);
        if(state.get(EDIT_UP) != (state.get(AXIS) == Direction.Axis.Y))
            CHAIN_SHAPE = VoxelShapes.union(CHAIN_SHAPE, UP_SHAPE);
        if(state.get(EDIT_NORTH) != (state.get(AXIS) == Direction.Axis.Z))
            CHAIN_SHAPE = VoxelShapes.union(CHAIN_SHAPE, NORTH_SHAPE);
        if(state.get(EDIT_SOUTH) != (state.get(AXIS) == Direction.Axis.Z))
            CHAIN_SHAPE = VoxelShapes.union(CHAIN_SHAPE, SOUTH_SHAPE);
        if(state.get(EDIT_WEST) != (state.get(AXIS) == Direction.Axis.X))
            CHAIN_SHAPE = VoxelShapes.union(CHAIN_SHAPE, WEST_SHAPE);
        if(state.get(EDIT_EAST) != (state.get(AXIS) == Direction.Axis.X))
            CHAIN_SHAPE = VoxelShapes.union(CHAIN_SHAPE, EAST_SHAPE);
        return CHAIN_SHAPE;
    }

    @Overwrite
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;
        return (BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl).with(AXIS, ctx.getSide().getAxis()).with(EDIT_DOWN, false).with(EDIT_UP, false).with(EDIT_NORTH, false).with(EDIT_SOUTH, false).with(EDIT_WEST, false).with(EDIT_EAST, false);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (item == Items.CHAIN) {
            BlockState oppState = world.getBlockState(pos.offset(hit.getSide()));
            if (state.getBlock() == Blocks.CHAIN && canAddSide(state, hit.getSide())) {
                world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return addDir(state, world, pos, hit.getSide());
            }
            return ActionResult.FAIL;
        }
        if (item == Items.SHEARS) {
            HitResult hitResult = world.rayTrace(new RayTraceContext(player.getCameraPosVec(1.0F), player.getCameraPosVec(1.0F).add((double)(MathHelper.sin(-player.yaw * 0.017453292F - 3.1415927F) * -MathHelper.cos(-player.pitch * 0.017453292F)) * 5.0D, (double)(MathHelper.sin(-player.pitch * 0.017453292F)) * 5.0D, (double)(MathHelper.cos(-player.yaw * 0.017453292F - 3.1415927F) * -MathHelper.cos(-player.pitch * 0.017453292F)) * 5.0D), RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.SOURCE_ONLY, player));
            Vec3d hitPos = hitResult.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
            return removeDir(state, world, pos, player, hitPos);
        }
        return ActionResult.FAIL;
    }

    public boolean canAddSide(BlockState state, Direction side) {
        switch (side) {
            case DOWN:
                return state.get(Properties.AXIS).equals(Direction.Axis.Y) == state.get(chains.EDIT_DOWN);
            case UP:
                return state.get(Properties.AXIS).equals(Direction.Axis.Y) == state.get(chains.EDIT_UP);
            case NORTH:
                return state.get(Properties.AXIS).equals(Direction.Axis.Z) == state.get(chains.EDIT_NORTH);
            case SOUTH:
                return state.get(Properties.AXIS).equals(Direction.Axis.Z) == state.get(chains.EDIT_SOUTH);
            case WEST:
                return state.get(Properties.AXIS).equals(Direction.Axis.X) == state.get(chains.EDIT_WEST);
            case EAST:
                return state.get(Properties.AXIS).equals(Direction.Axis.X) == state.get(chains.EDIT_EAST);
        }
        return false;
    }

    public ActionResult addDir(BlockState state, World world, BlockPos pos, Direction side) {
        switch (side) {
            case DOWN:
                if (state.get(Properties.AXIS).equals(Direction.Axis.Y) == state.get(chains.EDIT_DOWN)) {
                    world.setBlockState(pos, (BlockState) state.with(chains.EDIT_DOWN, !state.get(chains.EDIT_DOWN)));
                    return ActionResult.success(true);
                }
            case UP:
                if (state.get(Properties.AXIS).equals(Direction.Axis.Y) == state.get(chains.EDIT_UP)) {
                    world.setBlockState(pos, (BlockState) state.with(chains.EDIT_UP, !state.get(chains.EDIT_UP)));
                    return ActionResult.success(true);
                }
            case NORTH:
                if (state.get(Properties.AXIS).equals(Direction.Axis.Z) == state.get(chains.EDIT_NORTH)) {
                    world.setBlockState(pos, (BlockState) state.with(chains.EDIT_NORTH, !state.get(chains.EDIT_NORTH)));
                    return ActionResult.success(true);
                }
            case SOUTH:
                if (state.get(Properties.AXIS).equals(Direction.Axis.Z) == state.get(chains.EDIT_SOUTH)) {
                    world.setBlockState(pos, (BlockState) state.with(chains.EDIT_SOUTH, !state.get(chains.EDIT_SOUTH)));
                    return ActionResult.success(true);
                }
            case WEST:
                if (state.get(Properties.AXIS).equals(Direction.Axis.X) == state.get(chains.EDIT_WEST)) {
                    world.setBlockState(pos, (BlockState) state.with(chains.EDIT_WEST, !state.get(chains.EDIT_WEST)));
                    return ActionResult.success(true);
                }
            case EAST:
                if (state.get(Properties.AXIS).equals(Direction.Axis.X) == state.get(chains.EDIT_EAST)) {
                    world.setBlockState(pos, (BlockState) state.with(chains.EDIT_EAST, !state.get(chains.EDIT_EAST)));
                    return ActionResult.success(true);
                }
        }
        return ActionResult.FAIL;
    }

    public ActionResult removeDir(BlockState state, World world, BlockPos pos, PlayerEntity player, Vec3d hitPos) {
        Vec3d edge = new Vec3d(0.5, 0.0, 0.5);
        if (hitPos.isInRange(edge, 0.416927002) && state.get(AXIS).equals(Direction.Axis.Y) != state.get(EDIT_DOWN)) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, (BlockState) state.with(chains.EDIT_DOWN, !state.get(EDIT_DOWN)));
            return ActionResult.success(true);
        }
        edge = new Vec3d(0.5, 1.0, 0.5);
        if (hitPos.isInRange(edge, 0.416927002) && state.get(AXIS).equals(Direction.Axis.Y) != state.get(EDIT_UP)) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, (BlockState) state.with(chains.EDIT_UP, !state.get(EDIT_UP)));
            return ActionResult.success(true);
        }
        edge = new Vec3d(0.5, 0.5, 0.0);
        if (hitPos.isInRange(edge, 0.416927002) && state.get(AXIS).equals(Direction.Axis.Z) != state.get(EDIT_NORTH)) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, (BlockState) state.with(chains.EDIT_NORTH, !state.get(EDIT_NORTH)));
            return ActionResult.success(true);
        }
        edge = new Vec3d(0.5, 0.5, 1.0);
        if (hitPos.isInRange(edge, 0.416927002) && state.get(AXIS).equals(Direction.Axis.Z) != state.get(EDIT_SOUTH)) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, (BlockState) state.with(chains.EDIT_SOUTH, !state.get(EDIT_SOUTH)));
            return ActionResult.success(true);
        }
        edge = new Vec3d(0.0, 0.5, 0.5);
        if (hitPos.isInRange(edge, 0.416927002) && state.get(AXIS).equals(Direction.Axis.X) != state.get(EDIT_WEST)) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, (BlockState) state.with(chains.EDIT_WEST, !state.get(EDIT_WEST)));
            return ActionResult.success(true);
        }
        edge = new Vec3d(1.0, 0.5, 0.5);
        if (hitPos.isInRange(edge, 0.416927002) && state.get(AXIS).equals(Direction.Axis.X) != state.get(EDIT_EAST)) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, (BlockState) state.with(chains.EDIT_EAST, !state.get(EDIT_EAST)));
            return ActionResult.success(true);
        }
        return ActionResult.FAIL;
    }

    @Overwrite
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, EDIT_DOWN, EDIT_UP, EDIT_NORTH, EDIT_SOUTH, EDIT_WEST, EDIT_EAST, AXIS);
    }

    static {
        WATERLOGGED = Properties.WATERLOGGED;
        EDIT_DOWN = chains.EDIT_DOWN;
        EDIT_UP = chains.EDIT_UP;
        EDIT_NORTH = chains.EDIT_NORTH;
        EDIT_SOUTH = chains.EDIT_SOUTH;
        EDIT_WEST = chains.EDIT_WEST;
        EDIT_EAST = chains.EDIT_EAST;
        DOWN_SHAPE = Block.createCuboidShape(6.5D, 0.0D, 6.5D, 9.5D, 6.5D, 9.5D);
        UP_SHAPE = Block.createCuboidShape(6.5D, 6.5D, 6.5D, 9.5D, 16.0D, 9.5D);
        NORTH_SHAPE = Block.createCuboidShape(6.5D, 6.5D, 0.0D, 9.5D, 9.5D, 6.5D);
        SOUTH_SHAPE = Block.createCuboidShape(6.5D, 6.5D, 9.5D, 9.5D, 9.5D, 16.0D);
        WEST_SHAPE = Block.createCuboidShape(0.0D, 6.5D, 6.5D, 6.5D, 9.5D, 9.5D);
        EAST_SHAPE = Block.createCuboidShape(9.5D, 6.5D, 6.5D, 16.0D, 9.5D, 9.5D);
        CENTER_SHAPE = Block.createCuboidShape(6.5D, 6.5D, 6.5D, 9.5D, 9.5D, 9.5D);
    }
}