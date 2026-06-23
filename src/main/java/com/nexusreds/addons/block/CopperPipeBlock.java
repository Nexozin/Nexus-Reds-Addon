package com.nexusreds.addons.block;

import com.mojang.serialization.MapCodec;
import com.nexusreds.addons.NexusRedsMod;
import com.nexusreds.addons.block.entity.CopperPipeBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CopperPipeBlock extends BlockWithEntity implements Waterloggable {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty IS_JOINT = BooleanProperty.of("is_joint");
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;

    private static final VoxelShape CENTER_SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);

    public CopperPipeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(NORTH, false).with(EAST, false)
                .with(SOUTH, false).with(WEST, false)
                .with(UP, false).with(DOWN, false)
                .with(WATERLOGGED, false)
                .with(IS_JOINT, false)
                .with(AXIS, Direction.Axis.Z));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() { return null; }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED, IS_JOINT, AXIS);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = CENTER_SHAPE;
        if (state.get(UP)) shape = VoxelShapes.union(shape, UP_SHAPE);
        if (state.get(DOWN)) shape = VoxelShapes.union(shape, DOWN_SHAPE);
        if (state.get(NORTH)) shape = VoxelShapes.union(shape, NORTH_SHAPE);
        if (state.get(SOUTH)) shape = VoxelShapes.union(shape, SOUTH_SHAPE);
        if (state.get(EAST)) shape = VoxelShapes.union(shape, EAST_SHAPE);
        if (state.get(WEST)) shape = VoxelShapes.union(shape, WEST_SHAPE);
        return shape;
    }

    // --- CORREÇÃO VISUAL: Se usou a chave na ponta, força a junta! ---
    private BlockState updateJointsAndAxis(BlockState state, BlockView world, BlockPos pos) {
        boolean n = state.get(NORTH), s = state.get(SOUTH), e = state.get(EAST);
        boolean w = state.get(WEST), u = state.get(UP), d = state.get(DOWN);
        
        int connections = (n?1:0) + (s?1:0) + (e?1:0) + (w?1:0) + (u?1:0) + (d?1:0);
        boolean isJoint = true;
        
        if (connections == 2) {
            if ((n && s) || (e && w) || (u && d)) isJoint = false;
        }
        
        // Verifica se a conexão foi forçada pela Chave Nexus (Fim de linha no bloco)
        if (world != null && pos != null) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof CopperPipeBlockEntity pipe) {
                if (pipe.isForced(Direction.NORTH) || pipe.isForced(Direction.SOUTH) ||
                    pipe.isForced(Direction.EAST) || pipe.isForced(Direction.WEST) ||
                    pipe.isForced(Direction.UP) || pipe.isForced(Direction.DOWN)) {
                    isJoint = true; // Impede o modelo 'inter' liso nas pontas
                }
            }
        }
        
        Direction.Axis axis = Direction.Axis.Z;
        if (e || w) axis = Direction.Axis.X;
        if (u || d) axis = Direction.Axis.Y;
        
        return state.with(IS_JOINT, isJoint).with(AXIS, axis);
    }

    // --- INTERAÇÃO COM A CHAVE NEXUS ---
    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(NexusRedsMod.NEXUS_WRENCH)) {
            if (!world.isClient) {
                Direction sideClicked = hit.getSide();
                BlockEntity be = world.getBlockEntity(pos);
                
                if (be instanceof CopperPipeBlockEntity pipeEntity) {
                    pipeEntity.toggleForcedConnection(sideClicked);
                    
                    boolean isNowConnected = connectsTo(world.getBlockState(pos.offset(sideClicked))) || pipeEntity.isForced(sideClicked);
                    BlockState newState = state.with(getFacingProperty(sideClicked), isNowConnected);
                    
                    newState = updateJointsAndAxis(newState, world, pos);
                    world.setBlockState(pos, newState, 3);
                    
                    updatePipeNetwork(world, pos);
                }
            }
            return ItemActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        
        boolean isForced = false;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof CopperPipeBlockEntity pipe) isForced = pipe.isForced(direction);
        
        boolean isConnected = connectsTo(neighborState) || isForced;
        BlockState newState = state.with(getFacingProperty(direction), isConnected);
        
        return updateJointsAndAxis(newState, world, pos);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        
        boolean n = connectsTo(world.getBlockState(pos.north()));
        boolean s = connectsTo(world.getBlockState(pos.south()));
        boolean e = connectsTo(world.getBlockState(pos.east()));
        boolean w = connectsTo(world.getBlockState(pos.west()));
        boolean u = connectsTo(world.getBlockState(pos.up()));
        boolean d = connectsTo(world.getBlockState(pos.down()));

        BlockState state = this.getDefaultState()
                .with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER)
                .with(NORTH, n).with(SOUTH, s).with(EAST, e).with(WEST, w).with(UP, u).with(DOWN, d);
                
        return updateJointsAndAxis(state, null, null);
    }

    // --- ALGORITMO ANTI-FEEDBACK (O Fim do Piscar!) ---
    private void updatePipeNetwork(World world, BlockPos startPos) {
        if (world.isClient) return;

        Map<BlockPos, CopperPipeBlockEntity> network = new HashMap<>();
        Map<BlockPos, Integer> oldPowers = new HashMap<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        
        // 1. Mapeia a rede e DESLIGA tudo temporariamente para impedir loops
        while (!queue.isEmpty()) { 
            BlockPos currentPos = queue.poll();
            BlockState currentState = world.getBlockState(currentPos);
            
            if (!(currentState.getBlock() instanceof CopperPipeBlock)) continue;
            
            BlockEntity be = world.getBlockEntity(currentPos);
            if (be instanceof CopperPipeBlockEntity pipe && !network.containsKey(currentPos)) {
                network.put(currentPos, pipe);
                oldPowers.put(currentPos, pipe.powerLevel); // Guarda o estado anterior
                
                pipe.powerLevel = 0; // Isolamento elétrico
                
                if (network.size() < 24) { 
                    for (Direction dir : Direction.values()) {
                        if (currentState.get(getFacingProperty(dir))) {
                            BlockPos neighborPos = currentPos.offset(dir);
                            if (world.getBlockState(neighborPos).getBlock() instanceof CopperPipeBlock) {
                                if (!network.containsKey(neighborPos)) queue.add(neighborPos);
                            }
                        }
                    }
                }
            }
        }

        // 2. Mede a energia real dos blocos em volta (A alavanca não será enganada pelo próprio cano)
        int maxPower = 0;
        for (Map.Entry<BlockPos, CopperPipeBlockEntity> entry : network.entrySet()) {
            BlockPos pipePos = entry.getKey();
            BlockState pipeState = world.getBlockState(pipePos);
            
            for (Direction dir : Direction.values()) {
                if (pipeState.get(getFacingProperty(dir))) {
                    BlockPos neighborPos = pipePos.offset(dir);
                    BlockState neighborState = world.getBlockState(neighborPos);
                    
                    if (!(neighborState.getBlock() instanceof CopperPipeBlock)) {
                        int power = world.getEmittedRedstonePower(neighborPos, dir.getOpposite());
                        if (power > maxPower) maxPower = power;
                    }
                }
            }
        }

        // 3. Aplica a nova energia a toda a rede e avisa as lâmpadas apenas se houver mudança
        boolean changed = false;
        for (Map.Entry<BlockPos, CopperPipeBlockEntity> entry : network.entrySet()) {
            CopperPipeBlockEntity pipe = entry.getValue();
            int oldPower = oldPowers.get(entry.getKey());
            
            pipe.powerLevel = maxPower;
            if (oldPower != maxPower) {
                pipe.markDirty();
                changed = true;
            }
        }
        
        if (changed) {
            for (BlockPos pos : network.keySet()) {
                world.updateNeighborsAlways(pos, this);
            }
        }
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) { return true; }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient && !(sourceBlock instanceof CopperPipeBlock)) {
            updatePipeNetwork(world, pos);
        }
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof CopperPipeBlockEntity pipe) {
            if (state.get(getFacingProperty(direction.getOpposite()))) {
                return pipe.powerLevel;
            }
        }
        return 0;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, moved);
            for(Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.offset(dir);
                if (world.getBlockState(neighborPos).getBlock() instanceof CopperPipeBlock) {
                    updatePipeNetwork(world, neighborPos);
                }
            }
        } else {
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient && !state.isOf(oldState.getBlock())) {
            updatePipeNetwork(world, pos);
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state); }

    private boolean connectsTo(BlockState state) { return state.getBlock() instanceof CopperPipeBlock; }

    private BooleanProperty getFacingProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH; case SOUTH -> SOUTH; case EAST -> EAST;
            case WEST -> WEST; case UP -> UP; case DOWN -> DOWN;
        };
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperPipeBlockEntity(pos, state);
    }
}