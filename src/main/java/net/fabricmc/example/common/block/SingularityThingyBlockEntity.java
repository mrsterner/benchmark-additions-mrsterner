package net.fabricmc.example.common.block;

import net.fabricmc.example.ExampleMod;
import net.fabricmc.example.common.recipe.SingularityRecipe;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickScheduler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Optional;

public class SingularityThingyBlockEntity extends BlockEntity implements Inventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private boolean hasRecipe = false;
    private int progressItemsConsumed = 0;
    private final long capacity = 10000000;
    SimpleEnergyStorage simpleStorage = new SimpleEnergyStorage(100, 5, 10);

    public SingularityThingyBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.SINGULARITY_BLOCK_ENTITY, pos, state);
    }

    public int getProgressItemsConsumed(){
        return progressItemsConsumed;
    }

    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(capacity, 200, 0) {
        @Override
        protected void onFinalCommit() {
            markDirty();
        }
    };

    @SuppressWarnings("UnstableApiUsage")
    public static void tick(World world, BlockPos pos, BlockState state, SingularityThingyBlockEntity blockEntity) {
        if (!world.isClient()) {
            try (var transaction = Transaction.openOuter()) {
                blockEntity.energyStorage.amount += 10000;
                transaction.commit();
            }
            if(!blockEntity.hasRecipe){
                SingularityRecipe foundSingularityRecipe = world.getRecipeManager().listAllOfType(SingularityRecipe.Type.INSTANCE).stream().filter(recipe -> recipe.matches((Inventory) blockEntity.inventory, world)).findFirst().orElse(null);
                if (foundSingularityRecipe != null) {
                    blockEntity.hasRecipe = true;
                }
            }else{
                if(state.get(SingularityThingyBlock.MODE_INPUT)){
                    SingularityRecipe foundSingularityRecipe = world.getRecipeManager().listAllOfType(SingularityRecipe.Type.INSTANCE).stream().filter(recipe -> recipe.matches((Inventory) blockEntity.inventory, world)).findFirst().orElse(null);

                    if(!blockEntity.inventory.isEmpty()){
                        if(blockEntity.energyStorage.amount >= 10000 && blockEntity.getStack(0).getCount() < 64) {
                            try (var transaction = Transaction.openOuter()) {
                                blockEntity.energyStorage.amount -= 10000;
                                transaction.commit();
                                ((Inventory) blockEntity.inventory).getStack(0).decrement(1);
                                blockEntity.progressItemsConsumed++;
                            }
                        }
                    }
                    if(foundSingularityRecipe.getInputAmount() == blockEntity.progressItemsConsumed){
                        if(foundSingularityRecipe.getOutput() == blockEntity.inventory.get(0) && blockEntity.getStack(0).getCount() < 64){
                            blockEntity.getStack(1).increment(1);
                        }else if(foundSingularityRecipe.getOutput() == ItemStack.EMPTY){
                            blockEntity.setStack(1,foundSingularityRecipe.getOutput());
                        }
                    }
                }else{
                    if(blockEntity.progressItemsConsumed > 0 && blockEntity.getStack(1).getCount() < 64){
                        SingularityRecipe foundSingularityRecipe = null;
                        for (SingularityRecipe recipe : world.getRecipeManager().listAllOfType(SingularityRecipe.Type.INSTANCE)) {
                            if (recipe.matches((Inventory) blockEntity.inventory, world)) {
                                foundSingularityRecipe = recipe;
                                break;
                            }
                        }
                        blockEntity.progressItemsConsumed--;
                        ItemStack potentialReclaim = foundSingularityRecipe.getInput().getMatchingStacks()[0];
                        ((Inventory) blockEntity.inventory).setStack(1, potentialReclaim);
                        ((Inventory) blockEntity.inventory).getStack(1).increment(1);
                    }
                }
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, inventory);
        progressItemsConsumed = nbt.getInt("ProgressAmount");
        hasRecipe = nbt.getBoolean("HasRecipe");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("ProgressAmount", progressItemsConsumed);
        nbt.putBoolean("HasRecipe", hasRecipe);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (getStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        inventory.clear();
    }


    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = super.toInitialChunkDataNbt();
        writeNbt(nbt);
        return nbt;
    }

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }
}
