package com.nexusreds.addons.block.entity;

import com.nexusreds.addons.NexusRedsMod;
import com.nexusreds.addons.block.SequentialHopperBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SequentialHopperBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int lastSlotTargeted = -1;
    private int cooldown = 0;
    private int idleTicks = 0;

    public SequentialHopperBlockEntity(BlockPos pos, BlockState state) {
        super(NexusRedsMod.SEQUENTIAL_HOPPER_ENTITY, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) return;
        
        // Se travado por redstone, reseta tudo e para
        if (!state.get(SequentialHopperBlock.ENABLED)) {
            if (this.lastSlotTargeted != -1) {
                this.lastSlotTargeted = -1;
                this.idleTicks = 0;
                markDirty();
            }
            return; 
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Tenta agir
        boolean coletou = coletarItemDeCima(world, pos);
        boolean moveu = transferirItemSequencialmente(world, pos, state);

        if (moveu || coletou) {
            this.cooldown = 8;
            this.idleTicks = 0; // Se ele trabalhou, zera o contador de inatividade!
            markDirty();
        } else {
            // Se ele não trabalhou neste tick, começa a contar o tempo
            this.idleTicks++;
            
            // 12 ticks = 0.6 segundos
            if (this.idleTicks >= 12) {
                if (this.lastSlotTargeted != -1) {
                    this.lastSlotTargeted = -1; // Esquece a sequência
                    markDirty();
                }
                this.idleTicks = 12; // Trava o contador no 12 para não contar infinitamente
            }
        }
    }

    private boolean coletarItemDeCima(World world, BlockPos pos) {
        // Tenta pegar itens de inventários acima (baús, outros funis)
        Inventory invAcima = net.minecraft.block.entity.HopperBlockEntity.getInventoryAt(world, pos.up());
        if (invAcima != null) {
            for (int i = 0; i < invAcima.size(); i++) {
                ItemStack stackAcima = invAcima.getStack(i);
                if (!stackAcima.isEmpty()) {
                    ItemStack itemParaMover = stackAcima.copy();
                    itemParaMover.setCount(1);
                    if (this.podeReceber(itemParaMover)) {
                        this.adicionarAoInventario(itemParaMover);
                        stackAcima.decrement(1);
                        invAcima.markDirty();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean transferirItemSequencialmente(World world, BlockPos pos, BlockState state) {
        Direction facing = state.get(SequentialHopperBlock.FACING);
        BlockPos targetPos = pos.offset(facing);
        
        BlockEntity blockAlvo = world.getBlockEntity(targetPos);
        if (!(blockAlvo instanceof Inventory targetInventory)) return false;

        // Descobre de que lado estamos tentando inserir
        Direction ladoDeInsercao = facing.getOpposite();

        for (int i = 0; i < this.size(); i++) {
            ItemStack stackNoFunil = this.getStack(i);
            
            if (!stackNoFunil.isEmpty()) {
                int targetSize = targetInventory.size();
                if (targetSize == 0) return false;

                for (int offset = 1; offset <= targetSize; offset++) {
                    int proximoSlot = (this.lastSlotTargeted + offset) % targetSize;
                    
                    // REGRA DE SEGURANÇA: Respeitar SidedInventory (Fornalhas, Panelas de Mods, etc)
                    if (targetInventory instanceof SidedInventory sidedInv) {
                        int[] slotsDisponiveis = sidedInv.getAvailableSlots(ladoDeInsercao);
                        if (!isSlotAvailable(slotsDisponiveis, proximoSlot) || !sidedInv.canInsert(proximoSlot, stackNoFunil, ladoDeInsercao)) {
                            continue; // Slot bloqueado para este lado/item, pula pro próximo
                        }
                    } else if (!targetInventory.isValid(proximoSlot, stackNoFunil)) {
                        continue; // Respeita bloqueios básicos de inventário
                    }

                    ItemStack stackNoAlvo = targetInventory.getStack(proximoSlot);

                    if (podeInserir(stackNoFunil, stackNoAlvo, targetInventory.getMaxCountPerStack())) {
                        
                        ItemStack itemParaTransferir = stackNoFunil.copy();
                        itemParaTransferir.setCount(1);
                        
                        if (stackNoAlvo.isEmpty()) {
                            targetInventory.setStack(proximoSlot, itemParaTransferir);
                        } else {
                            stackNoAlvo.increment(1);
                        }

                        stackNoFunil.decrement(1);
                        this.lastSlotTargeted = proximoSlot;
                        targetInventory.markDirty();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Método auxiliar para checar listas de slots do SidedInventory
    private boolean isSlotAvailable(int[] slots, int slot) {
        for (int s : slots) {
            if (s == slot) return true;
        }
        return false;
    }

    // Métodos auxiliares para facilitar:
    private boolean podeReceber(ItemStack stack) {
        for (int i = 0; i < this.size(); i++) {
            ItemStack slot = this.getStack(i);
            if (slot.isEmpty() || (ItemStack.areItemsEqual(slot, stack) && slot.getCount() < slot.getMaxCount())) {
                return true;
            }
        }
        return false;
    }

    private void adicionarAoInventario(ItemStack stack) {
        for (int i = 0; i < this.size(); i++) {
            ItemStack slot = this.getStack(i);
            if (slot.isEmpty()) {
                this.setStack(i, stack);
                return;
            } else if (ItemStack.areItemsEqual(slot, stack)) {
                slot.increment(1);
                return;
            }
        }
    }

    private boolean podeInserir(ItemStack source, ItemStack target, int maxTargetLimit) {
        if (target.isEmpty()) return true;
        if (!ItemStack.areItemsEqual(source, target)) return false;
        return target.getCount() < target.getMaxCount() && target.getCount() < maxTargetLimit;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
        nbt.putInt("LastSlotTargeted", this.lastSlotTargeted);
        nbt.putInt("IdleTicks", this.idleTicks); // Salva o tempo ocioso
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, this.inventory, registryLookup);
        this.lastSlotTargeted = nbt.getInt("LastSlotTargeted");
        this.idleTicks = nbt.getInt("IdleTicks"); // Lê o tempo ocioso
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Funil Sequencial");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new HopperScreenHandler(syncId, playerInventory, this);
    }

    @Override public int size() { return this.inventory.size(); }
    @Override public boolean isEmpty() { return this.inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return this.inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }
    @Override public ItemStack removeStack(int slot) { return Inventories.removeStack(this.inventory, slot); }
    @Override public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
    }
    @Override public void clear() { this.inventory.clear(); markDirty(); }
    @Override public boolean canPlayerUse(PlayerEntity player) { return true; }
}