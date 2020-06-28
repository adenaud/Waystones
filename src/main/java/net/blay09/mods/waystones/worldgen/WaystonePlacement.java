package net.blay09.mods.waystones.worldgen;

import com.mojang.serialization.Codec;
import net.blay09.mods.waystones.config.WaystoneConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.TopSolidOnce;

import java.util.Random;
import java.util.stream.Stream;

public class WaystonePlacement extends TopSolidOnce {
    public WaystonePlacement(Codec<NoPlacementConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public Stream<BlockPos> getPositions(IWorld world, ChunkGenerator chunkGenerator, Random random, NoPlacementConfig config, BlockPos pos) {
        if (isWaystoneChunk(world, pos)) {
            return super.getPositions(world, chunkGenerator, random, config, pos);
        } else {
            return Stream.empty();
        }
    }

    private boolean isWaystoneChunk(IWorld world, BlockPos pos) {
        final int chunkDistance = WaystoneConfig.COMMON.worldGenFrequency.get();
        final int maxDeviation = (int) Math.ceil(chunkDistance / 2f);
        int chunkX = pos.getX() / 16;
        int chunkZ = pos.getZ() / 16;
        int devGridX = pos.getX() / 16 * maxDeviation;
        int devGridZ = pos.getZ() / 16 * maxDeviation;
        Random random = new Random(((ISeedReader)world).getSeed() * devGridX * devGridZ);
        int chunkOffsetX = random.nextInt(maxDeviation);
        int chunkOffsetZ = random.nextInt(maxDeviation);
        return (chunkX + chunkOffsetX) % chunkDistance == 0 && (chunkZ + chunkOffsetZ) % chunkDistance == 0;
    }
}
