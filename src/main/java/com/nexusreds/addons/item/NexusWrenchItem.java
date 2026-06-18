package com.nexusreds.addons.item;

import com.nexusreds.addons.block.PlanterBlock;
import com.nexusreds.addons.block.SequentialHopperBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class NexusWrenchItem extends Item {

    public NexusWrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        // Verifica se é o Funil Sequencial OU o Planter
        if (state.getBlock() instanceof SequentialHopperBlock || state.getBlock() instanceof PlanterBlock) {
            if (!world.isClient) {
                Direction currentFacing = state.get(Properties.FACING);
                
                // Se for o Funil, usa a regra especial (não aponta para cima)
                // Se for o Planter, usa o ciclo completo (incluindo UP e DOWN)
                Direction nextFacing = (state.getBlock() instanceof SequentialHopperBlock) 
                                       ? getNextHopperDirection(currentFacing) 
                                       : getNextFullDirection(currentFacing);
                
                world.setBlockState(pos, state.with(Properties.FACING, nextFacing), 3);
            }
            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }

    // Regra do Funil (Não aponta para cima)
    private Direction getNextHopperDirection(Direction current) {
        return switch (current) {
            case DOWN -> Direction.NORTH;
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.DOWN;
            default -> Direction.DOWN;
        };
    }

    // Regra do Planter (Ciclo completo com todas as 6 direções)
    private Direction getNextFullDirection(Direction current) {
        return switch (current) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.UP;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.NORTH;
        };
    }
}