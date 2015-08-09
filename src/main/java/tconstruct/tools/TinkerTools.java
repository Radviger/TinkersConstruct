package tconstruct.tools;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.apache.logging.log4j.Logger;

import mantle.pulsar.pulse.Handler;
import mantle.pulsar.pulse.Pulse;
import tconstruct.CommonProxy;
import tconstruct.TinkerPulse;
import tconstruct.common.block.BlockTable;
import tconstruct.common.item.ItemBlockMeta;
import tconstruct.common.tileentity.TileTable;
import tconstruct.library.TinkerRegistry;
import tconstruct.library.Util;
import tconstruct.library.modifiers.IModifier;
import tconstruct.library.modifiers.Modifier;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.tools.ToolPart;
import tconstruct.tools.block.BlockToolForge;
import tconstruct.tools.block.BlockToolTable;
import tconstruct.tools.debug.TempToolCrafting;
import tconstruct.tools.debug.TempToolModifying;
import tconstruct.tools.item.Pattern;
import tconstruct.tools.modifiers.DiamondModifier;
import tconstruct.tools.modifiers.RedstoneModifier;
import tconstruct.tools.modifiers.StoneboundModifier;
import tconstruct.tools.tileentity.TileCraftingStation;
import tconstruct.tools.tileentity.TilePartBuilder;
import tconstruct.tools.tileentity.TilePatternChest;
import tconstruct.tools.tileentity.TileStencilTable;
import tconstruct.tools.tileentity.TileToolForge;
import tconstruct.tools.tileentity.TileToolStation;

@Pulse(id = TinkerTools.PulseId, description = "All the tools and everything related to it.")
public class TinkerTools extends TinkerPulse {

  public static final String PulseId = "TinkerTools";
  static final Logger log = Util.getLogger(PulseId);

  @SidedProxy(clientSide = "tconstruct.tools.ToolClientProxy", serverSide = "tconstruct.CommonProxy")
  public static CommonProxy proxy;

  // Blocks
  public static BlockToolTable toolTables;
  public static BlockToolForge toolForge;

  // General Items
  public static Pattern pattern;

  // Tools
  public static ToolCore pickaxe;

  // Tool Parts
  public static ToolPart pickHead;

  public static ToolPart toolRod;
  public static ToolPart binding;
  public static ToolPart largePlate;

  // Modifiers
  public static IModifier diamondMod;
  public static IModifier fortifyMod;
  public static IModifier redstoneMod;

  // PRE-INITIALIZATION
  @Handler
  public void preInit(FMLPreInitializationEvent event) {
    TinkerMaterials.registerToolMaterials();

    // register items
    pattern = registerItem(new Pattern(), "Pattern");

    registerToolParts();
    registerTools();
    registerModifiers();

    // register blocks
    toolTables = registerBlock(new BlockToolTable(), ItemBlockMeta.class, "ToolTables");
    registerTE(TileTable.class, "Table");
    registerTE(TileCraftingStation.class, "CraftingStation");
    registerTE(TileStencilTable.class, "StencilTable");
    registerTE(TilePartBuilder.class, "PartBuilder");
    registerTE(TilePatternChest.class, "PatternChest");
    registerTE(TileToolStation.class, "ToolStation");
    registerTE(TileToolForge.class, "ToolForge");

    toolForge = registerBlock(new BlockToolForge(), ItemBlockMeta.class, "ToolForge");

    proxy.preInit();


    // debug things
    // todo: remove. ignore this
    new StoneboundModifier();
    GameRegistry.addRecipe(new TempToolCrafting());
    GameRegistry.addRecipe(new TempToolModifying());
  }

  private void registerToolParts() {
    // The order the items are registered in represents the order in the stencil table GUI too
    pickHead = registerToolPart(new ToolPart(ToolPart.COST_Ingot), "PickHead");

    toolRod = registerToolPart(new ToolPart(ToolPart.COST_Shard), "ToolRod");
    binding = registerToolPart(new ToolPart(ToolPart.COST_Shard), "Binding");

    largePlate = registerToolPart(new ToolPart(ToolPart.COST_Ingot*8), "LargePlate");
  }

  private void registerTools() {
    pickaxe = registerItem(new Pickaxe(), "Pickaxe");
  }

  private void registerModifiers() {
    diamondMod = new DiamondModifier();
    redstoneMod = new RedstoneModifier(50);

    // todo: fix
    fortifyMod = new Modifier("Fortify") {

      @Override
      public void updateNBT(NBTTagCompound modifierTag) {

      }

      @Override
      public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {

      }


      @SideOnly(Side.CLIENT)
      @Override
      public boolean hasTexturePerMaterial() {
        return true;
      }
    };
  }

  // INITIALIZATION
  @Handler
  public void init(FMLInitializationEvent event) {
    registerToolBuilding();
    registerRecipies();

    proxy.init();
  }

  private void registerToolBuilding() {
    TinkerRegistry.registerToolCrafting(pickaxe);
  }

  private void registerRecipies() {
    // todo: remove debug recipe stuff
    ItemStack pattern = new ItemStack(TinkerTools.pattern);

    // Crafting Station
    GameRegistry.addRecipe(
        new ShapelessOreRecipe(new ItemStack(toolTables, 1, BlockToolTable.TableTypes.CraftingStation.meta),
                               "workbench"));
    // Stencil Table
    GameRegistry.addRecipe(
        new TableRecipe(OreDictionary.getOres("plankWood"), toolTables, BlockToolTable.TableTypes.StencilTable.meta,
                        "P", "B", 'P', pattern, 'B', "plankWood"));
    GameRegistry.addRecipe(BlockTable.createItemstack(toolTables, BlockToolTable.TableTypes.StencilTable.meta, Blocks.rail, 0),
                           "P", "B", 'P', pattern, 'B', Blocks.rail);
    GameRegistry.addRecipe(BlockTable.createItemstack(toolTables, BlockToolTable.TableTypes.StencilTable.meta, Blocks.melon_block, 0),
                           "P", "B", 'P', pattern, 'B', Blocks.melon_block);

    // Part Builder
    GameRegistry.addRecipe(
        new TableRecipe(OreDictionary.getOres("logWood"), toolTables, BlockToolTable.TableTypes.PartBuilder.meta, "P",
                        "B", 'P', pattern, 'B', "logWood"));
    GameRegistry.addRecipe(BlockTable.createItemstack(toolTables, BlockToolTable.TableTypes.PartBuilder.meta, Blocks.golden_rail, 0),
                           "P", "B", 'P', pattern, 'B', Blocks.rail);
    GameRegistry.addRecipe(BlockTable.createItemstack(toolTables, BlockToolTable.TableTypes.PartBuilder.meta, Blocks.cactus, 0),
                           "P", "B", 'P', pattern, 'B', Blocks.cactus);

    // Pattern Chest
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(toolTables, 1, BlockToolTable.TableTypes.PatternChest.meta),
                                               "P", "B", 'P', pattern, 'B', "chestWood"));

    // Tool Station
    GameRegistry.addRecipe(
        new ShapedOreRecipe(new ItemStack(toolTables, 1, BlockToolTable.TableTypes.ToolStation.meta),
                            "P", "B", 'P', pattern, 'B', "workbench"));
    // Tool Forge
    TinkerRegistry.addToolForgeBlock("blockIron");
    TinkerRegistry.addToolForgeBlock("blockGold");
  }

  // called by TinkerRegistry.addToolForgeBlock
  public static void registerToolForgeRecipe(String oredict) {
    // todo: change recipe to brick vs. smeltery-bricks wether smeltery pulse is active
    GameRegistry
        .addRecipe(new TableRecipe(OreDictionary.getOres(oredict), toolForge, 0,
                                   "BBB",
                                   "MTM",
                                   "M M",
                                   'B', Blocks.stonebrick,
                                   'M', oredict,
                                   'T', new ItemStack(toolTables, 1, BlockToolTable.TableTypes.ToolStation.meta)));
  }

  // POST-INITIALIZATION
  @Handler
  public void postInit(FMLPostInitializationEvent event) {
    proxy.postInit();
  }


  private ToolPart registerToolPart(ToolPart part, String name) {
    ToolPart ret = registerItem(part, name);

    ItemStack stencil = new ItemStack(pattern);
    Pattern.setTagForPart(stencil, part);
    TinkerRegistry.registerStencilTableCrafting(stencil);

    return ret;
  }
}
