package com.nexusreds.addons.screen;

import com.nexusreds.addons.item.ModItems;
import com.nexusreds.addons.NexusRedsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class PlanterScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public PlanterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(12));
    }

    public PlanterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(NexusRedsMod.PLANTER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 12);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // Slot 0: Enxada
        this.addSlot(new Slot(inventory, 0, 23, 35) {
            @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof HoeItem; }
        });

        // Slots 1 a 9: Sementes
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 3; ++l) {
                this.addSlot(new Slot(inventory, 1 + l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }

        // Slot 10: Upgrade Superior
        this.addSlot(new Slot(inventory, 10, 179, 14) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isValidUpgrade(stack, inventory.getStack(11));
            }
            @Override public int getMaxItemCount() { return 1; }
        });

        // Slot 11: Upgrade Inferior
        this.addSlot(new Slot(inventory, 11, 179, 40) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isValidUpgrade(stack, inventory.getStack(10));
            }
            @Override public int getMaxItemCount() { return 1; }
        });

        // Inventário do Jogador
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }

        // Hotbar do Jogador
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    // --- LÓGICA DE VALIDAÇÃO DE UPGRADES ---
    
    // (O isValidUpgrade continua o mesmo)
    private boolean isValidUpgrade(ItemStack stackParaInserir, ItemStack stackDoOutroSlot) {
        if (isRangeFamily(stackParaInserir)) return !isRangeFamily(stackDoOutroSlot);
        if (isIrrigationFamily(stackParaInserir)) return !isIrrigationFamily(stackDoOutroSlot);
        if (isPlowerFamily(stackParaInserir)) return !isPlowerFamily(stackDoOutroSlot);
        return false;
    }

    // Adicionado o TIER_3 na restrição de Range
    private boolean isRangeFamily(ItemStack stack) {
        return stack.isOf(ModItems.RANGE_UPGRADE) || 
               stack.isOf(ModItems.RANGE_UPGRADE_TIER_2) || 
               stack.isOf(ModItems.RANGE_UPGRADE_TIER_3);
    }

    // Adicionado o TIER_3 na restrição de Irrigação
    private boolean isIrrigationFamily(ItemStack stack) {
        return stack.isOf(ModItems.IRRIGATION_UPGRADE) || 
               stack.isOf(ModItems.IRRIGATION_UPGRADE_TIER_2) || 
               stack.isOf(ModItems.IRRIGATION_UPGRADE_TIER_3);
    }

    private boolean isPlowerFamily(ItemStack stack) {
        return stack.isOf(ModItems.PLOWER_UPGRADE);
    }

    // Identifica se o item é QUALQUER upgrade (usado pelo Shift-Click)
    private boolean isAnyUpgrade(ItemStack stack) {
        return isRangeFamily(stack) || isIrrigationFamily(stack) || isPlowerFamily(stack);
    }

    // --- LÓGICA DE SHIFT-CLICK ---
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            
            // Movendo do Planter para o Jogador
            if (invSlot < 12) { 
                if (!this.insertItem(originalStack, 12, this.slots.size(), true)) return ItemStack.EMPTY;
            } 
            // Movendo do Jogador para o Planter
            else { 
                if (isAnyUpgrade(originalStack)) { 
                    // Tenta colocar nos slots de upgrade (índices 10 e 11)
                    if (!this.insertItem(originalStack, 10, 12, false)) return ItemStack.EMPTY;
                } else if (originalStack.getItem() instanceof HoeItem) { 
                    // Tenta colocar no slot da enxada (índice 0)
                    if (!this.insertItem(originalStack, 0, 1, false)) return ItemStack.EMPTY;
                } else { 
                    // Tenta colocar nos slots de sementes (índices 1 a 9)
                    if (!this.insertItem(originalStack, 1, 10, false)) return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }
}