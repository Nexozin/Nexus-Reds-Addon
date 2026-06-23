package com.nexusreds.addons.block.entity;

import net.minecraft.fluid.Fluids;
import com.nexusreds.addons.item.ModItems;
import com.nexusreds.addons.NexusRedsMod;
import com.nexusreds.addons.block.PlanterBlock;
import com.nexusreds.addons.screen.PlanterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanterBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);
    private static final int[] HOE_SLOT = {0};
    private static final int[] SEED_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private int tickCounter = 0; 

    public PlanterBlockEntity(BlockPos pos, BlockState state) {
        super(NexusRedsMod.PLANTER_ENTITY, pos, state);
    }

    // --- SISTEMA DE TIERS (NÍVEIS) ---
    public int getRangeTier() {
        if (this.getStack(10).isOf(ModItems.RANGE_UPGRADE_TIER_2) || this.getStack(11).isOf(ModItems.RANGE_UPGRADE_TIER_2)) return 2;
        if (this.getStack(10).isOf(ModItems.RANGE_UPGRADE) || this.getStack(11).isOf(ModItems.RANGE_UPGRADE)) return 1;
        return 0;
    }

    public int getIrrigationTier() {
        if (this.getStack(10).isOf(ModItems.IRRIGATION_UPGRADE_TIER_2) || this.getStack(11).isOf(ModItems.IRRIGATION_UPGRADE_TIER_2)) return 2;
        if (this.getStack(10).isOf(ModItems.IRRIGATION_UPGRADE) || this.getStack(11).isOf(ModItems.IRRIGATION_UPGRADE)) return 1;
        return 0;
    }

    // --- CÁLCULO DE ÁREA POR NÍVEL ---
    public List<BlockPos> getTargetArea(BlockPos pos, BlockState state) {
        List<BlockPos> targetPositions = new ArrayList<>();
        Direction facing = state.get(PlanterBlock.FACING);
        int tier = getRangeTier();
        
        if (tier == 0) {
            if (facing == Direction.UP) targetPositions.add(pos.up(2));
            else targetPositions.add(pos.offset(facing));
        } else if (tier == 1) { 
            if (facing == Direction.UP || facing == Direction.DOWN) {
                BlockPos center = (facing == Direction.UP) ? pos.up(2) : pos.down();
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) targetPositions.add(center.add(x, 0, z));
                }
            } else {
                for (int i = 1; i <= 5; i++) targetPositions.add(pos.offset(facing, i));
            }
        } else if (tier == 2) { 
            if (facing == Direction.UP || facing == Direction.DOWN) {
                BlockPos center = (facing == Direction.UP) ? pos.up(2) : pos.down();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) targetPositions.add(center.add(x, 0, z));
                }
            } else {
                for (int i = 1; i <= 7; i++) targetPositions.add(pos.offset(facing, i));
            }
        }
        return targetPositions;
    }

    // --- VERIFICAÇÃO DE ÁGUA ---
    public boolean isAtivamenteIrrigando(WorldView world, BlockPos pos) {
        int tier = getIrrigationTier();
        
        if (tier == 0) return false; 
        if (tier == 2) return true;  
        
        for (Direction dir : Direction.values()) {
            if (world.getFluidState(pos.offset(dir)).isOf(Fluids.WATER)) return true;
        }
        return false;
    }

    // --- TICK (IRRIGAÇÃO E CRESCIMENTO 1.5x) ---
    public static void tick(World world, BlockPos pos, BlockState state, PlanterBlockEntity entity) {
        if (world.isClient) return;

        entity.tickCounter++;
        if (entity.tickCounter >= 20) { 
            entity.tickCounter = 0;
            
            if (entity.isAtivamenteIrrigando(world, pos)) {
                List<BlockPos> area = entity.getTargetArea(pos, state);
                int irrigTier = entity.getIrrigationTier();
                
                for (BlockPos target : area) {
                    BlockPos soloPos = target.down();
                    BlockState soloState = world.getBlockState(soloPos);
                    if (soloState.isOf(Blocks.FARMLAND) && soloState.get(FarmlandBlock.MOISTURE) < 7) {
                        world.setBlockState(soloPos, soloState.with(FarmlandBlock.MOISTURE, 7), 3);
                    }

                    if (irrigTier == 2 && world instanceof ServerWorld serverWorld) {
                        BlockState cropState = world.getBlockState(target);
                        if (cropState.getBlock() instanceof net.minecraft.block.CropBlock || cropState.getBlock() instanceof net.minecraft.block.NetherWartBlock) {
                            if (serverWorld.getRandom().nextFloat() < 0.0075f) {
                                cropState.randomTick(serverWorld, target, serverWorld.getRandom());
                            }
                        }
                    }
                }
            }
        }
    }

    // --- LÓGICA DE PLANTIO (REDSTONE) ---
    public void realizarPlantio(World world, BlockPos pos, BlockState state) {
        if (world.isClient) return;
        
        ItemStack enxada = this.getStack(0);
        if (!(enxada.getItem() instanceof HoeItem)) return; 

        boolean realizouAcao = false;
        List<BlockPos> targetPositions = getTargetArea(pos, state);

        for (BlockPos currentCropPos : targetPositions) {
            BlockState cropState = world.getBlockState(currentCropPos);
            net.minecraft.block.Block cropBlock = cropState.getBlock();
            
            // Colheita
            if (cropBlock instanceof net.minecraft.block.CropBlock crop) {
                if (crop.isMature(cropState)) {
                    net.minecraft.block.Block.dropStacks(cropState, world, currentCropPos, world.getBlockEntity(currentCropPos), null, enxada);
                    world.breakBlock(currentCropPos, false); 
                    cropState = world.getBlockState(currentCropPos);
                    realizouAcao = true;
                }
            } else if (cropBlock instanceof net.minecraft.block.NetherWartBlock) {
                if (cropState.get(net.minecraft.block.NetherWartBlock.AGE) >= 3) {
                    net.minecraft.block.Block.dropStacks(cropState, world, currentCropPos, world.getBlockEntity(currentCropPos), null, enxada);
                    world.breakBlock(currentCropPos, false);
                    cropState = world.getBlockState(currentCropPos);
                    realizouAcao = true;
                }
            }

            // Plantio (Agora Aleatório!)
            if (cropState.isAir() || cropState.isReplaceable()) {
                
                // Cria uma lista com os números dos slots (1 a 9) e embaralha
                List<Integer> slotsAleatorios = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
                Collections.shuffle(slotsAleatorios);
                
                // Em vez de i = 1; i < 10, usamos a nossa lista embaralhada
                for (int i : slotsAleatorios) {
                    ItemStack seedStack = this.getStack(i);
                    if (seedStack.isEmpty()) continue;

                    net.minecraft.block.Block seedBlock = null;

                    if (seedStack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                        seedBlock = blockItem.getBlock();
                    } else if (seedStack.getItem() instanceof net.minecraft.item.AliasedBlockItem aliasedItem) {
                        seedBlock = aliasedItem.getBlock();
                    }

                    if (seedBlock instanceof net.minecraft.block.PlantBlock plantBlock) {
                        BlockState plantState = plantBlock.getDefaultState();
                        
                        if (plantState.canPlaceAt(world, currentCropPos)) {
                            world.setBlockState(currentCropPos, plantState);
                            seedStack.decrement(1); 
                            realizouAcao = true;
                            break; 
                        }
                    }
                }
            }
        }

        if (realizouAcao && enxada.isDamageable()) {
            enxada.setDamage(enxada.getDamage() + 1);
            if (enxada.getDamage() >= enxada.getMaxDamage()) {
                enxada.decrement(1); 
            }
            this.markDirty(); 
        }
    }

    // --- REGRAS DE INVENTÁRIO (FUNIS) ---
    @Override
    public int[] getAvailableSlots(Direction side) {
        Direction facing = this.getCachedState().get(PlanterBlock.FACING);
        if (side == Direction.DOWN || side == Direction.UP || side == facing.getOpposite()) return SEED_SLOTS; 
        return HOE_SLOT; 
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 0) return stack.getItem() instanceof HoeItem;
        return true; 
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return (dir == Direction.DOWN && slot != 0 && slot != 10 && slot != 11);
    }

    // --- MÉTODOS NBT PADRÃO ---
    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, this.inventory, registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
    }

    @Override public Text getDisplayName() { return Text.literal("Planter"); }
    
    @Nullable @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new PlanterScreenHandler(syncId, playerInventory, this);
    }

    @Override public int size() { return inventory.size(); }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }
    @Override public ItemStack removeStack(int slot) { return Inventories.removeStack(this.inventory, slot); }
    @Override public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        markDirty();
    }
    @Override public void clear() { inventory.clear(); markDirty(); }
    @Override public boolean canPlayerUse(PlayerEntity player) { return true; }
}