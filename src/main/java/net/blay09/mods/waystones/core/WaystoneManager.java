package net.blay09.mods.waystones.core;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.tileentity.WaystoneTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

public class WaystoneManager extends WorldSavedData {

    private static final String DATA_NAME = Waystones.MOD_ID;
    private static final String TAG_WAYSTONES = "Waystones";
    private static final WaystoneManager clientStorageCopy = new WaystoneManager();

    private final Map<UUID, IWaystone> waystones = new HashMap<>();

    public WaystoneManager() {
        super(DATA_NAME);
    }

    public void addWaystone(IWaystone waystone) {
        waystones.put(waystone.getWaystoneUid(), waystone);
        markDirty();
    }

    public void updateWaystone(IWaystone waystone) {
        Waystone mutableWaystone = (Waystone) waystones.getOrDefault(waystone.getWaystoneUid(), waystone);
        mutableWaystone.setName(waystone.getName());
        mutableWaystone.setGlobal(waystone.isGlobal());
        waystones.put(waystone.getWaystoneUid(), mutableWaystone);
        markDirty();
    }

    public void removeWaystone(IWaystone waystone) {
        waystones.remove(waystone.getWaystoneUid());
        markDirty();
    }

    public Optional<IWaystone> getWaystoneAt(IBlockReader world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof WaystoneTileEntity) {
            return Optional.of(((WaystoneTileEntity) tileEntity).getWaystone());
        }

        return Optional.empty();
    }

    public Optional<IWaystone> getWaystoneById(UUID waystoneUid) {
        return Optional.ofNullable(waystones.get(waystoneUid));
    }

    public Optional<IWaystone> findWaystoneByName(String name) {
        return waystones.values().stream().filter(it -> it.getName().equals(name)).findFirst();
    }

    public List<IWaystone> getGlobalWaystones() {
        return waystones.values().stream().filter(IWaystone::isGlobal).collect(Collectors.toList());
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        ListNBT tagList = tagCompound.getList(TAG_WAYSTONES, Constants.NBT.TAG_COMPOUND);
        for (INBT tag : tagList) {
            CompoundNBT compound = (CompoundNBT) tag;
            UUID waystoneUid = compound.getUniqueId("WaystoneUid");
            String name = compound.getString("Name");

            RegistryKey<DimensionType> dimensionType = RegistryKey.func_240903_a_(Registry.field_239698_ad_, new ResourceLocation(compound.getString("DimensionTypeName")));


            if (dimensionType == null) {
                dimensionType = DimensionType.field_235999_c_;
            }


            BlockPos pos = NBTUtil.readBlockPos(compound.getCompound("BlockPos"));
            boolean wasGenerated = compound.getBoolean("WasGenerated");
            UUID ownerUid = compound.contains("OwnerUid") ? compound.getUniqueId("OwnerUid") : null;
            Waystone waystone = new Waystone(waystoneUid, dimensionType, pos, wasGenerated, ownerUid);
            waystone.setName(name);
            waystone.setGlobal(compound.getBoolean("IsGlobal"));
            waystones.put(waystoneUid, waystone);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        ListNBT tagList = new ListNBT();
        for (IWaystone waystone : waystones.values()) {
            CompoundNBT compound = new CompoundNBT();
            compound.put("WaystoneUid", NBTUtil.func_240626_a_(waystone.getWaystoneUid()));
            compound.putString("Name", waystone.getName());
            //compound.putInt("DimensionTypeId", waystone.getDimensionType().getId());
            compound.putString("DimensionTypeName", waystone.getDimensionType().func_240901_a_().getPath());
            compound.put("BlockPos", NBTUtil.writeBlockPos(waystone.getPos()));
            compound.putBoolean("WasGenerated", waystone.wasGenerated());
            if (waystone.getOwnerUid() != null) {
                compound.put("OwnerUid", NBTUtil.func_240626_a_(waystone.getOwnerUid()));
            }
            compound.putBoolean("IsGlobal", waystone.isGlobal());
            tagList.add(compound);
        }
        tagCompound.put(TAG_WAYSTONES, tagList);
        return tagCompound;
    }

    public static WaystoneManager get() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerWorld overworld = server.getWorld(World.field_234918_g_);
            DimensionSavedDataManager storage = overworld.getSavedData();
            return storage.getOrCreate(WaystoneManager::new, DATA_NAME);
        }

        return clientStorageCopy;
    }
}
