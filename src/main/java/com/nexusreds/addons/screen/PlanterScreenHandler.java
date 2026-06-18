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

        // 1. Slot da Enxada (Index 0)
        this.addSlot(new Slot(inventory, 0, 23, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof HoeItem;
            }
        });

        // 2. Slots das Sementes (Index 1 a 9)
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 3; ++l) {
                this.addSlot(new Slot(inventory, 1 + l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }

        // 3. Slot 10: Primeiro slot de Upgrade
        this.addSlot(new Slot(inventory, 10, 179, 14) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.RANGE_UPGRADE) || stack.isOf(ModItems.IRRIGATION_UPGRADE);
            }
            @Override
            public int getMaxItemCount() { return 1; }
        });

        // 4. Slot 11: Segundo slot de Upgrade
        this.addSlot(new Slot(inventory, 11, 179, 40) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.RANGE_UPGRADE) || stack.isOf(ModItems.IRRIGATION_UPGRADE); 
            }
            @Override
            public int getMaxItemCount() { return 1; }
        });

        // 5. Inventário do Jogador
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }

        // 6. Hotbar do Jogador
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

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
            
            if (invSlot < 12) { 
                if (!this.insertItem(originalStack, 12, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else { 
                // Permite o shift-click para os dois upgrades
                if (originalStack.isOf(ModItems.RANGE_UPGRADE) || originalStack.isOf(ModItems.IRRIGATION_UPGRADE)) { 
                    if (!this.insertItem(originalStack, 10, 12, false)) return ItemStack.EMPTY;
                } else if (originalStack.getItem() instanceof HoeItem) { 
                    if (!this.insertItem(originalStack, 0, 1, false)) return ItemStack.EMPTY;
                } else { 
                    if (!this.insertItem(originalStack, 1, 10, false)) return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }
}