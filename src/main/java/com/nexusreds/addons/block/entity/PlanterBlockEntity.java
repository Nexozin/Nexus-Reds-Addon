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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlanterBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(12, ItemStack.EMPTY);
    private static final int[] HOE_SLOT = {0};
    private static final int[] SEED_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private int tickCounter = 0; // Relógio interno para a irrigação passiva

    public PlanterBlockEntity(BlockPos pos, BlockState state) {
        super(NexusRedsMod.PLANTER_ENTITY, pos, state);
    }

    // --- MÉTODOS AUXILIARES PARA OS UPGRADES ---
    
    public boolean hasUpgrade(Item upgradeItem) {
        return this.getStack(10).isOf(upgradeItem) || this.getStack(11).isOf(upgradeItem);
    }

    public List<BlockPos> getTargetArea(BlockPos pos, BlockState state) {
        List<BlockPos> targetPositions = new ArrayList<>();
        Direction facing = state.get(PlanterBlock.FACING);
        
        if (!hasUpgrade(ModItems.RANGE_UPGRADE)) {
            if (facing == Direction.UP) targetPositions.add(pos.up(2));
            else targetPositions.add(pos.offset(facing));
        } else {
            if (facing == Direction.UP || facing == Direction.DOWN) {
                BlockPos center = (facing == Direction.UP) ? pos.up(2) : pos.down();
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        targetPositions.add(center.add(x, 0, z));
                    }
                }
            } else {
                for (int i = 1; i <= 5; i++) {
                    targetPositions.add(pos.offset(facing, i));
                }
            }
        }
        return targetPositions;
    }

    // --- IRRIGAÇÃO PASSIVA ---
    
    public static void tick(World world, BlockPos pos, BlockState state, PlanterBlockEntity entity) {
        if (world.isClient) return;

        entity.tickCounter++;
        if (entity.tickCounter >= 20) { // A cada 1 segundo
            entity.tickCounter = 0;
            
            if (entity.hasUpgrade(ModItems.IRRIGATION_UPGRADE)) {
                boolean isNearWater = false;
                
                for (Direction dir : Direction.values()) {
                    BlockPos checkPos = pos.offset(dir);
                    
                    // A MÁGICA AQUI: getFluidState() reconhece água pura E água dentro de blocos (Folhas, Lajes, etc.)
                    if (world.getFluidState(checkPos).isOf(Fluids.WATER)) {
                        isNearWater = true;
                        break;
                    }
                }

                if (isNearWater) {
                    List<BlockPos> area = entity.getTargetArea(pos, state);
                    for (BlockPos target : area) {
                        BlockPos soloPos = target.down();
                        BlockState soloState = world.getBlockState(soloPos);
                        
                        if (soloState.isOf(Blocks.FARMLAND) && soloState.get(FarmlandBlock.MOISTURE) < 7) {
                            world.setBlockState(soloPos, soloState.with(FarmlandBlock.MOISTURE, 7), 3);
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

            // Plantio
            if (cropState.isAir() || cropState.isReplaceable()) {
                for (int i = 1; i < 10; i++) {
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

    // --- REGRAS DE INVENTÁRIO ---

    @Override
    public int[] getAvailableSlots(Direction side) {
        Direction facing = this.getCachedState().get(PlanterBlock.FACING);
        if (side == Direction.DOWN || side == Direction.UP || side == facing.getOpposite()) return SEED_SLOTS; 
        return HOE_SLOT; 
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 0) return stack.getItem() instanceof HoeItem;
        if (slot == 10 || slot == 11) return stack.isOf(ModItems.RANGE_UPGRADE) || stack.isOf(ModItems.IRRIGATION_UPGRADE);
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