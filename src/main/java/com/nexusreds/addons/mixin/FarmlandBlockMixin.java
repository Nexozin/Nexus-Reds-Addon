package com.nexusreds.addons.mixin;

import com.nexusreds.addons.block.PlanterBlock;
import com.nexusreds.addons.block.entity.PlanterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {

    @Inject(method = "isWaterNearby", at = @At("RETURN"), cancellable = true)
    private static void onIsWaterNearby(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Se o jogo já achou água natural, ignoramos
        if (cir.getReturnValue()) return;

        // Procura pelo Planter num raio de 5 blocos
        for (BlockPos checkPos : BlockPos.iterate(pos.add(-5, -2, -5), pos.add(5, 2, 5))) {
            BlockState state = world.getBlockState(checkPos);
            
            if (state.getBlock() instanceof PlanterBlock) {
                BlockEntity be = world.getBlockEntity(checkPos);
                if (be instanceof PlanterBlockEntity planter) {
                    
                    // O Planter tem upgrade e água?
                    if (planter.isAtivamenteIrrigando(world, checkPos)) {
                        
                        // Varre os blocos onde a planta ficaria (pos.up() da terra)
                        for (BlockPos target : planter.getTargetArea(checkPos, state)) {
                            if (target.equals(pos.up())) {
                                cir.setReturnValue(true); // Diz ao jogo que existe água!
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}