package net.blay09.mods.waystones.block;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.config.WaystoneConfig;
import net.blay09.mods.waystones.core.*;
import net.blay09.mods.waystones.tileentity.WaystoneTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class WaystoneBlock extends Block {

    /**
     * We provide a slightly smaller render shape to prevent neighbour blocks from being culled.
     */
    private static final VoxelShape RENDER_SHAPE = VoxelShapes.create(1 / 16f, 1 / 16f, 1 / 16f, 15 / 16f, 15 / 16f, 15 / 16f);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public WaystoneBlock() {
        super(Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(5f, 2000f));
    }

    @Override
    public VoxelShape getRenderShape(BlockState p_196247_1_, IBlockReader p_196247_2_, BlockPos p_196247_3_) {
        return RENDER_SHAPE;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(HALF);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WaystoneTileEntity();
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos) {
        if (!PlayerWaystoneManager.mayBreakWaystone(player, world, pos)) {
            return -1f;
        }

        return super.getPlayerRelativeBlockHardness(state, player, world, pos);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        // Do not allow placing a waystone directly on top of another
        Block blockBelow = world.getBlockState(pos.down()).getBlock();
        if (blockBelow == this) {
            return false;
        }

        // Do not allow placing a waystone directly below of another
        Block blockTwoAbove = world.getBlockState(pos.up(2)).getBlock();
        BlockState stateAbove = world.getBlockState(pos.up());
        return blockTwoAbove != this && stateAbove.isAir(world, pos.up());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (!PlayerWaystoneManager.mayPlaceWaystone(context.getPlayer())) {
            return null;
        }

        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()).with(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockPos posAbove = pos.up();
        if (!world.isRemote) {
            world.setBlockState(posAbove, this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER));
        }

        TileEntity waystoneTileEntity = world.getTileEntity(pos);
        if (waystoneTileEntity instanceof WaystoneTileEntity) {
            ((WaystoneTileEntity) waystoneTileEntity).initializeWaystone(world, placer, false);

            TileEntity waystoneTileEntityAbove = world.getTileEntity(posAbove);
            if (waystoneTileEntityAbove instanceof WaystoneTileEntity) {
                ((WaystoneTileEntity) waystoneTileEntityAbove).initializeFromBase(((WaystoneTileEntity) waystoneTileEntity));
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        WaystoneTileEntity tileEntity = (WaystoneTileEntity) world.getTileEntity(pos);
        if (tileEntity != null) {
            IWaystone waystone = tileEntity.getWaystone();
            WaystoneManager.get().removeWaystone(waystone);
            PlayerWaystoneManager.removeKnownWaystone(waystone);
        }

        super.onReplaced(state, world, pos, newState, isMoving);

        // Also destroy the connect upper or lower waystone block
        if (world.getBlockState(pos.up()).getBlock() == this) {
            world.removeBlock(pos.up(), false);
        } else if (world.getBlockState(pos.down()).getBlock() == this) {
            world.removeBlock(pos.down(), false);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        WaystoneTileEntity tileEntity = (WaystoneTileEntity) world.getTileEntity(pos);
        if (tileEntity == null) {
            return ActionResultType.FAIL;
        }

        if (player.abilities.isCreativeMode) {
            ItemStack heldItem = player.getHeldItem(hand);
            if (heldItem.getItem() == Items.BAMBOO) {
                if (!world.isRemote) {
                    tileEntity.uninitializeWaystone();
                    player.sendStatusMessage(new StringTextComponent("Waystone was successfully reset - it will re-initialize once it is next loaded."), false);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.STICK) {
                if (!world.isRemote) {
                    player.sendStatusMessage(new StringTextComponent("Waystone UUID: " + tileEntity.getWaystone().getWaystoneUid()), false);
                }
                return ActionResultType.SUCCESS;
            }
        }

        IWaystone waystone = tileEntity.getWaystone();
        if (player.isSneaking()) {
            WaystoneEditPermissions result = PlayerWaystoneManager.mayEditWaystone(player, world, waystone);
            if (result != WaystoneEditPermissions.ALLOW) {
                if (result.getLangKey() != null) {
                    TranslationTextComponent chatComponent = new TranslationTextComponent(result.getLangKey());
                    chatComponent.getStyle().func_240720_a_(TextFormatting.RED);
                    player.sendStatusMessage(chatComponent, true);
                }
                return ActionResultType.SUCCESS;
            }

            if (!world.isRemote) {
                NetworkHooks.openGui(((ServerPlayerEntity) player), tileEntity.getWaystoneSettingsContainerProvider(), pos);
            }
            return ActionResultType.SUCCESS;
        }

        boolean isActivated = PlayerWaystoneManager.isWaystoneActivated(player, waystone);
        if (isActivated) {
            if (!world.isRemote) {
                NetworkHooks.openGui(((ServerPlayerEntity) player), tileEntity.getWaystoneSelectionContainerProvider(), it -> {
                    it.writeByte(WarpMode.WAYSTONE_TO_WAYSTONE.ordinal());
                    it.writeBlockPos(pos);
                });
            }
        } else {
            PlayerWaystoneManager.activateWaystone(player, waystone);

            if (!world.isRemote) {
                StringTextComponent nameComponent = new StringTextComponent(waystone.getName());
                nameComponent.getStyle().func_240720_a_(TextFormatting.WHITE);
                TranslationTextComponent chatComponent = new TranslationTextComponent("chat.waystones.waystone_activated", nameComponent);
                chatComponent.getStyle().func_240720_a_(TextFormatting.YELLOW);
                player.sendMessage(chatComponent, null);

                WaystoneSyncManager.sendKnownWaystones(player);
            }

            notifyObserversOfActivation(world, pos);

            if (world.isRemote) {
                Waystones.proxy.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, pos, 1f);
                for (int i = 0; i < 32; i++) {
                    world.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 2, pos.getY() + 3, pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 2, 0, -5, 0);
                    world.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 2, pos.getY() + 4, pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 2, 0, -5, 0);
                }
            }
        }

        return ActionResultType.SUCCESS;
    }

    private void notifyObserversOfActivation(World world, BlockPos pos) {
        if (!world.isRemote) {
            for (Direction direction : Direction.values()) {
                BlockPos offset = pos.offset(direction);
                BlockState neighbourState = world.getBlockState(offset);
                Block neighbourBlock = neighbourState.getBlock();
                if (neighbourBlock instanceof ObserverBlock && neighbourState.get(ObserverBlock.FACING) == direction.getOpposite()) {
                    if (!world.getPendingBlockTicks().isTickScheduled(offset, neighbourBlock)) {
                        world.getPendingBlockTicks().scheduleTick(offset, neighbourBlock, 2);
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!WaystoneConfig.CLIENT.disableParticles.get() && random.nextFloat() < 0.75f) {
            WaystoneTileEntity tileEntity = (WaystoneTileEntity) world.getTileEntity(pos);
            PlayerEntity player = Minecraft.getInstance().player;
            if (tileEntity != null && PlayerWaystoneManager.isWaystoneActivated(Objects.requireNonNull(player), tileEntity.getWaystone())) {
                world.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.5, pos.getY() + 0.5, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.5, 0, 0, 0);
                world.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.5, pos.getY() + 0.5, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.5, 0, 0, 0);
            }
        }
    }

}
