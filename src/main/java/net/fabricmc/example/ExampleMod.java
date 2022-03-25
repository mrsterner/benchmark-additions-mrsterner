package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.common.block.SingularityThingyBlock;
import net.fabricmc.example.common.block.SingularityThingyBlockEntity;
import net.fabricmc.example.common.recipe.SingularityRecipe;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings.copyOf;

public class ExampleMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
	public static final String MODID = "modid";
	public static final Block SINGULARITY_BLOCK = new SingularityThingyBlock(copyOf(Blocks.IRON_BLOCK).strength(4.0f));

	public static BlockEntityType<SingularityThingyBlockEntity> SINGULARITY_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "singularity_block"), SINGULARITY_BLOCK);
		SINGULARITY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "modid:demo_block_entity", FabricBlockEntityTypeBuilder.create(SingularityThingyBlockEntity::new, SINGULARITY_BLOCK).build(null));

		Registry.register(Registry.RECIPE_SERIALIZER, SingularityRecipe.SingularityRecipeSerializer.ID,
		SingularityRecipe.SingularityRecipeSerializer.INSTANCE);
		Registry.register(Registry.RECIPE_TYPE, new Identifier(MODID, SingularityRecipe.Type.ID), SingularityRecipe.Type.INSTANCE);
	}

}
