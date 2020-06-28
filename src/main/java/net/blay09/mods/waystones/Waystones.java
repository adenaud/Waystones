package net.blay09.mods.waystones;

import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.client.ClientProxy;
import net.blay09.mods.waystones.client.ModRenderers;
import net.blay09.mods.waystones.client.ModScreens;
import net.blay09.mods.waystones.config.WaystoneConfig;
import net.blay09.mods.waystones.container.ModContainers;
import net.blay09.mods.waystones.item.ModItems;
import net.blay09.mods.waystones.network.NetworkHandler;
import net.blay09.mods.waystones.tileentity.ModTileEntities;
import net.blay09.mods.waystones.worldgen.ModWorldGen;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;


@Mod(Waystones.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Waystones {

    public static final String MOD_ID = "waystones";

    public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public static final ItemGroup itemGroup = new ItemGroup(Waystones.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.waystone);
        }
    };

    public Waystones() {
        DeferredWorkQueue.runLater(NetworkHandler::init);
        DeferredWorkQueue.runLater(ModStats::registerStats);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WaystoneConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WaystoneConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, WaystoneConfig.clientSpec);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        //ModWorldGen.setupRandomWorldGen();
        //ModWorldGen.setupVillageWorldGen();
    }

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        ModRenderers.registerRenderers();
        ModScreens.registerScreens();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModBlocks.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerWorldGenFeatures(RegistryEvent.Register<Feature<?>> event) {
        ModWorldGen.registerFeatures(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerWorldGenPlacements(RegistryEvent.Register<Placement<?>> event) {
        ModWorldGen.registerPlacements(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        ModTileEntities.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModItems.register(event.getRegistry());
        ModBlocks.registerBlockItems(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        ModContainers.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        // TODO FMLInterModComms.sendFunctionMessage(Compat.THEONEPROBE, "getTheOneProbe", "net.blay09.mods.waystones.compat.TheOneProbeAddon");
    }

}
