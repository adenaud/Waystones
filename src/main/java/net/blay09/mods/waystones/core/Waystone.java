package net.blay09.mods.waystones.core;

import net.blay09.mods.waystones.api.IWaystone;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;

import javax.annotation.Nullable;
import java.util.UUID;

public class Waystone implements IWaystone {

    private final UUID waystoneUid;
    private final RegistryKey<DimensionType> dimensionType;
    private final BlockPos pos;
    private final boolean wasGenerated;

    private String name;
    private boolean isGlobal;
    private UUID ownerUid;

    public Waystone(UUID waystoneUid, RegistryKey<DimensionType> dimensionType, BlockPos pos, boolean wasGenerated, @Nullable UUID ownerUid) {
        this.waystoneUid = waystoneUid;
        this.dimensionType = dimensionType;
        this.pos = pos;
        this.wasGenerated = wasGenerated;
        this.ownerUid = ownerUid;
    }

    @Override
    public UUID getWaystoneUid() {
        return waystoneUid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public RegistryKey<DimensionType> getDimensionType() {
        return dimensionType;
    }

    @Override
    public boolean wasGenerated() {
        return wasGenerated;
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    @Override
    public boolean isOwner(PlayerEntity player) {
        return ownerUid == null || player.getGameProfile().getId().equals(ownerUid) || player.abilities.isCreativeMode;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public UUID getOwnerUid() {
        return ownerUid;
    }

}
