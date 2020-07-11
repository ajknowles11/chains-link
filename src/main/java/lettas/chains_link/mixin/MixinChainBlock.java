package lettas.chains_link.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.*;

@Mixin(ChainBlock.class)
public abstract class MixinChainBlock extends Block implements Waterloggable {
	private static final VoxelShape Y_SHAPE = Block.createCuboidShape(6.5D, 0.0D, 6.5D, 9.5D, 16.0D, 9.5D);
	private static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.5D, 6.5D, 0.0D, 9.5D, 9.5D, 16.0D);
	private static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0D, 6.5D, 6.5D, 16.0D, 9.5D, 9.5D);
	private static final EnumProperty<Direction.Axis> AXIS;
	private static final BooleanProperty WATERLOGGED;
	
	public MixinChainBlock(Block.Settings settings) {
		super(settings);
		this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.Y).with(WATERLOGGED, false));
	}
	
	@Overwrite 
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		switch((Direction.Axis)state.get(AXIS)) { 
	    case Y:
	    default:
	    	return Y_SHAPE;
	    case Z:
	    	return Z_SHAPE;
	    case X:
	    	return X_SHAPE;
	    }
	}
	
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		switch(rotation) {
	    case COUNTERCLOCKWISE_90:
	    case CLOCKWISE_90:
	    	switch((Direction.Axis)state.get(AXIS)) {
	        case X:
	        	return (BlockState)state.with(AXIS, Direction.Axis.Z);
	        case Z:
	            return (BlockState)state.with(AXIS, Direction.Axis.X);
	        default:
	            return state;
	        }
	    default:
	        return state;
	    }
	}

	@Overwrite
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
	    boolean bl = fluidState.getFluid() == Fluids.WATER;
		return (BlockState)this.getDefaultState().with(AXIS, ctx.getSide().getAxis()).with(WATERLOGGED, bl);
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if ((Boolean)state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
	    }
		
	    return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}
	
	@Overwrite
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
		builder.add(AXIS);
	}

	public FluidState getFluidState(BlockState state) {
		return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	static {
		AXIS = Properties.AXIS;
		WATERLOGGED = Properties.WATERLOGGED;
	}

	 
	 
	 
	 
}