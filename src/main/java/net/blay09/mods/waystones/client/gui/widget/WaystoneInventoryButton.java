package net.blay09.mods.waystones.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.config.WaystoneConfig;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.Objects;

public class WaystoneInventoryButton extends Button {

    private static final ResourceLocation INVENTORY_BUTTON_TEXTURE = new ResourceLocation(Waystones.MOD_ID, "textures/gui/inventory_button.png");

    private final ContainerScreen<?> parentScreen;
    private final ItemStack iconItem;
    private final ItemStack iconItemHovered;

    public WaystoneInventoryButton(ContainerScreen<?> parentScreen, IPressable pressable) {
        super(0, 0, 16, 16, new StringTextComponent(""), pressable);
        this.parentScreen = parentScreen;
        this.iconItem = new ItemStack(ModItems.boundScroll);
        this.iconItemHovered = new ItemStack(ModItems.warpScroll);
    }

    @Override
    public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (field_230694_p_) {
            field_230690_l_ = parentScreen.getGuiLeft() + WaystoneConfig.CLIENT.teleportButtonX.get();
            field_230691_m_ = parentScreen.getGuiTop() + WaystoneConfig.CLIENT.teleportButtonY.get();
            field_230692_n_ = mouseX >= field_230690_l_ && mouseY >= field_230691_m_ && mouseX < field_230690_l_ + field_230688_j_ && mouseY < field_230691_m_ + field_230689_k_;

            PlayerEntity player = Minecraft.getInstance().player;
            if (PlayerWaystoneManager.canUseInventoryButton(Objects.requireNonNull(player))) {
                ItemStack icon = field_230692_n_ ? iconItemHovered : iconItem;
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                itemRenderer.renderItemAndEffectIntoGUI(icon, field_230690_l_, field_230691_m_);
            } else {
                Minecraft.getInstance().getTextureManager().bindTexture(INVENTORY_BUTTON_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.color4f(1f, 1f, 1f, 0.5f);
                func_238463_a_(matrixStack, field_230690_l_, field_230691_m_, 0, 0, 16, 16, 16, 16);
                RenderSystem.color4f(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
            }
        }
    }

    public boolean isHovered() {
        return field_230692_n_;
    }
}
