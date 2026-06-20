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

        this.addSlot(new Slot(inventory, 0, 23, 35) {
            @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof HoeItem; }
        });

        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 3; ++l) {
                this.addSlot(new Slot(inventory, 1 + l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }

        // Slot 10: Verifica se o Slot 11 já tem um upgrade do mesmo tipo
        this.addSlot(new Slot(inventory, 10, 179, 14) {
            @Override
            public boolean canInsert(ItemStack stack) {
                ItemStack outroSlot = inventory.getStack(11);
                if (isRangeFamily(stack)) return !isRangeFamily(outroSlot);
                if (isIrrigationFamily(stack)) return !isIrrigationFamily(outroSlot);
                return false;
            }
            @Override public int getMaxItemCount() { return 1; }
        });

        // Slot 11: Verifica se o Slot 10 já tem um upgrade do mesmo tipo
        this.addSlot(new Slot(inventory, 11, 179, 40) {
            @Override
            public boolean canInsert(ItemStack stack) {
                ItemStack outroSlot = inventory.getStack(10);
                if (isRangeFamily(stack)) return !isRangeFamily(outroSlot);
                if (isIrrigationFamily(stack)) return !isIrrigationFamily(outroSlot);
                return false;
            }
            @Override public int getMaxItemCount() { return 1; }
        });

        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }

        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    // --- MÉTODOS AUXILIARES DE FAMÍLIA DE UPGRADES ---
    private boolean isRangeFamily(ItemStack stack) {
        return stack.isOf(ModItems.RANGE_UPGRADE) || stack.isOf(ModItems.RANGE_UPGRADE_TIER_2);
    }

    private boolean isIrrigationFamily(ItemStack stack) {
        return stack.isOf(ModItems.IRRIGATION_UPGRADE) || stack.isOf(ModItems.IRRIGATION_UPGRADE_TIER_2);
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
                if (!this.insertItem(originalStack, 12, this.slots.size(), true)) return ItemStack.EMPTY;
            } else { 
                // Se for um upgrade, tentamos enviar para os slots 10 e 11. 
                // O insertItem respeita o canInsert, portanto não vai deixar duplicar!
                if (isRangeFamily(originalStack) || isIrrigationFamily(originalStack)) { 
                    if (!this.insertItem(originalStack, 10, 12, false)) return ItemStack.EMPTY;
                } else if (originalStack.getItem() instanceof HoeItem) { 
                    if (!this.insertItem(originalStack, 0, 1, false)) return ItemStack.EMPTY;
                } else { 
                    if (!this.insertItem(originalStack, 1, 10, false)) return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }
}