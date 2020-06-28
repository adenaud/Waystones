package net.blay09.mods.waystones.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class SortWaystoneButton extends Button {

    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    private final int sortDir;
    private final int visibleRegionStart;
    private final int visibleRegionHeight;

    public SortWaystoneButton(int x, int y, int sortDir, int visibleRegionStart, int visibleRegionHeight, IPressable pressable) {
        super(x, y, 11, 7, new StringTextComponent(""), pressable);
        this.sortDir = sortDir;
        this.visibleRegionStart = visibleRegionStart;
        this.visibleRegionHeight = visibleRegionHeight;
    }

    @Override
    public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
        if (mouseY >= visibleRegionStart && mouseY < visibleRegionStart + visibleRegionHeight) {
            Minecraft.getInstance().getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            this.field_230692_n_ = mouseX >= this.field_230690_l_ && mouseY >= this.field_230691_m_ && mouseX < this.field_230690_l_ + this.field_230688_j_ && mouseY < this.field_230691_m_ + this.field_230689_k_;
            int renderY = field_230691_m_ - (sortDir == 1 ? 20 : 5);
            RenderSystem.enableBlend();
            if (field_230693_o_ && field_230692_n_) {
                RenderSystem.color4f(1f, 1f, 1f, 1f);
            } else if (field_230693_o_) {
                RenderSystem.color4f(1f, 1f, 1f, 0.75f);
            } else {
                RenderSystem.color4f(1f, 1f, 1f, 0.25f);
            }

            if (field_230692_n_ && field_230693_o_) {
                func_238474_b_(matrixStack, field_230690_l_ - 5, renderY, sortDir == 1 ? 64 : 96, 32, 32, 32);
            } else {
                func_238474_b_(matrixStack,field_230690_l_ - 5, renderY, sortDir == 1 ? 64 : 96, 0, 32, 32);
            }

            RenderSystem.disableBlend();
            RenderSystem.color4f(1f, 1f, 1f, 1f);
        }
    }

}
