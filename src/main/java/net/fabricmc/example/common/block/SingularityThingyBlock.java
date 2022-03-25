package net.fabricmc.example.common.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class SingularityThingyBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty PROCESSING = BooleanProperty.of("processing");
    public static final BooleanProperty MODE_INPUT = BooleanProperty.of("mode_input");
    public SingularityThingyBlock(Settings settings) {
        super(settings);
        this.setDefaultState(((this.stateManager.getDefaultState())).with(PROCESSING, false).with(MODE_INPUT, true));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(PROCESSING).add(MODE_INPUT));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (tickerWorld, pos, tickerState, blockEntity) -> SingularityThingyBlockEntity.tick(tickerWorld, pos, tickerState, (SingularityThingyBlockEntity) blockEntity);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(world.getBlockEntity(pos) instanceof SingularityThingyBlockEntity singularityThingyBlockEntity){
            if(player.isSneaking()){
                //state.cycle(MODE_INPUT);
                singularityThingyBlockEntity.setStack(0, player.getStackInHand(hand));
            }else{
                System.out.println("Energy: "+singularityThingyBlockEntity.energyStorage.amount);
                System.out.println("Input: "+singularityThingyBlockEntity.getStack(0));
                System.out.println("Output: "+singularityThingyBlockEntity.getStack(1));
                System.out.println("ProgressConsumedItems: "+singularityThingyBlockEntity.getProgressItemsConsumed());
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SingularityThingyBlockEntity(pos, state);
    }
}
