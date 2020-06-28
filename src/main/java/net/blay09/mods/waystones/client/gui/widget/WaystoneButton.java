package net.blay09.mods.waystones.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WarpMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.Objects;

public class WaystoneButton extends Button {

    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");

    private final int xpLevelCost;

    public WaystoneButton(int x, int y, IWaystone waystone, WarpMode warpMode, IPressable pressable) {
        super(x, y, 200, 20, new StringTextComponent((waystone.isGlobal() ? TextFormatting.YELLOW : "") + waystone.getName()), pressable);
        PlayerEntity player = Minecraft.getInstance().player;
        this.xpLevelCost = Math.round(PlayerWaystoneManager.getExperienceLevelCost(Objects.requireNonNull(player), waystone, warpMode));
        if (!PlayerWaystoneManager.mayTeleportToWaystone(player, waystone)) {
            field_230693_o_ = false;
        } else if (player.experienceLevel < xpLevelCost && !player.abilities.isCreativeMode) {
            field_230693_o_ = false;
        }
    }

    @Override
    public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.func_230431_b_(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.color4f(1f, 1f, 1f, 1f);

        Minecraft mc = Minecraft.getInstance();
        if (xpLevelCost > 0) {
            boolean canAfford = Objects.requireNonNull(mc.player).experienceLevel >= xpLevelCost || mc.player.abilities.isCreativeMode;
            mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
            func_238474_b_(matrixStack, field_230690_l_ + 2, field_230691_m_ + 2, (Math.min(xpLevelCost, 3) - 1) * 16, 223 + (!canAfford ? 16 : 0), 16, 16);

            if (xpLevelCost > 3) {
                mc.fontRenderer.func_238405_a_(matrixStack, "+", field_230690_l_ + 17, field_230691_m_ + 6, 0xC8FF8F);
            }

            if (field_230692_n_ && mouseX <= field_230690_l_ + 16) {
                //GuiUtils.drawHoveringText(Lists.newArrayList((canAfford ? TextFormatting.GREEN : TextFormatting.RED) + I18n.format("gui.waystones.waystone_selection.level_requirement", xpLevelCost)), mouseX, mouseY + mc.fontRenderer.FONT_HEIGHT, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), 200, mc.fontRenderer);
                GuiUtils.drawHoveringText(matrixStack,
                        Lists.newArrayList(
                                new StringTextComponent((canAfford ? TextFormatting.GREEN : TextFormatting.RED) +
                                        I18n.format("gui.waystones.waystone_selection.level_requirement", xpLevelCost))),
                        mouseX, mouseY + mc.fontRenderer.FONT_HEIGHT, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), 200, mc.fontRenderer);
            }
            RenderSystem.disableLighting();
        }
    }

}
