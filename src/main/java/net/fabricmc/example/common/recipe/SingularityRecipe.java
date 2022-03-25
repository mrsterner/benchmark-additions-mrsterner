package net.fabricmc.example.common.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.example.ExampleMod;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SingularityRecipe implements Recipe<Inventory> {
    private final Ingredient input;
    private final int inputAmount;
    private final ItemStack result;
    private final Identifier id;

    public SingularityRecipe(Identifier id, ItemStack result, Ingredient input, int inputAmount) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.inputAmount = inputAmount;
    }

    public int getInputAmount(){
        return inputAmount;
    }

    public Ingredient getInput() {
        return this.input;
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        if (inv.size() < 1) return false;
        return input.test(inv.getStack(0));
    }


    @Override
    public ItemStack craft(Inventory inventory) {
        return this.getOutput().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput() {
        return this.result;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SingularityRecipeSerializer.INSTANCE;
    }

    public static class Type implements RecipeType<SingularityRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "singularity_recipe";
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public class SingularityRecipeFormat{
        JsonObject input;
        int inputAmount;
        String outputItem;
        int outputAmount;
    }

    public static class SingularityRecipeSerializer implements RecipeSerializer<SingularityRecipe> {
        private SingularityRecipeSerializer() {
        }

        public static final SingularityRecipeSerializer INSTANCE = new SingularityRecipeSerializer();
        public static final Identifier ID = new Identifier(ExampleMod.MODID, "singularity_recipe");

        @Override
        public SingularityRecipe read(Identifier id, JsonObject json) {
            SingularityRecipeFormat recipeFormat = new Gson().fromJson(json, SingularityRecipeFormat.class);
            if (recipeFormat.input == null || recipeFormat.outputItem == null) {
                throw new JsonSyntaxException("A required attribute is missing!");
            }

            if (recipeFormat.inputAmount == 0) {
                recipeFormat.inputAmount = 10000;
            }

            if (recipeFormat.outputAmount == 0) {
                recipeFormat.outputAmount = 1;
            }


            Ingredient input = Ingredient.fromJson(recipeFormat.input);
            Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(recipeFormat.outputItem)).orElseThrow(() -> new JsonSyntaxException("No such item " + recipeFormat.outputItem));
            ItemStack output = new ItemStack(outputItem, recipeFormat.outputAmount);

            return new SingularityRecipe(id, output, input, recipeFormat.inputAmount);
        }

        @Override
        public SingularityRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient input = Ingredient.fromPacket(buf);
            ItemStack output = buf.readItemStack();
            int inputAmount = buf.readInt();
            return new SingularityRecipe(id, output, input, inputAmount);
        }

        @Override
        public void write(PacketByteBuf buf, SingularityRecipe recipe) {
            recipe.getInput().write(buf);
            buf.writeItemStack(recipe.getOutput());
        }
    }
}
