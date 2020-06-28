package net.blay09.mods.waystones.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.container.WaystoneSettingsContainer;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.network.NetworkHandler;
import net.blay09.mods.waystones.network.message.EditWaystoneMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class WaystoneSettingsScreen extends ContainerScreen<WaystoneSettingsContainer> {

    private TextFieldWidget textField;
    private Button btnDone;
    private ToggleWidget chkGlobal;

    public WaystoneSettingsScreen(WaystoneSettingsContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        xSize = 270;
        ySize = 200;
    }

    @Override
    public void func_231160_c_() {
        // Leave no space for JEI!
        xSize = field_230708_k_;

        super.func_231160_c_();
        IWaystone waystone = container.getWaystone();
        String oldText = waystone.getName();
        if (textField != null) {
            oldText = textField.getText();
        }

        textField = new TextFieldWidget(Minecraft.getInstance().fontRenderer, field_230708_k_ / 2 - 100, field_230709_l_ / 2 - 20, 200, 20, textField, new StringTextComponent(""));
        textField.setMaxStringLength(128);
        textField.setText(oldText);
        textField.func_231049_c__(true);
        func_230480_a_(textField);
        setFocusedDefault(textField);

        btnDone = new Button(field_230708_k_ / 2, field_230709_l_ / 2 + 10, 100, 20, new TranslationTextComponent("gui.done"), button -> {
            if (textField.getText().isEmpty()) {
                textField.func_231049_c__(true);
                func_231035_a_(textField);
                return;
            }

            NetworkHandler.channel.sendToServer(new EditWaystoneMessage(waystone, textField.getText(), chkGlobal.isStateTriggered()));
        });
        func_230480_a_(btnDone);

        chkGlobal = new ToggleWidget(field_230708_k_ / 2 - 100, field_230709_l_ / 2 + 10, 20, 20, waystone.isGlobal());
        chkGlobal.initTextureValues(0, 0, 20, 20, new ResourceLocation(Waystones.MOD_ID, "textures/gui/checkbox.png"));
        if (!PlayerWaystoneManager.mayEditGlobalWaystones(Objects.requireNonNull(Minecraft.getInstance().player))) {
            chkGlobal.field_230694_p_ = false;
        }

        func_230480_a_(chkGlobal);

        getMinecraft().keyboardListener.enableRepeatEvents(true);
    }

    @Override
    public void func_231164_f_() {
        super.func_231164_f_();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        if (chkGlobal.func_231044_a_(mouseX, mouseY, button)) {
            chkGlobal.setStateTriggered(!chkGlobal.isStateTriggered());
            return true;
        }

        final int chkGlobalLabelX = field_230708_k_ / 2 - 100 + 25;
        final int chkGlobalLabelY = field_230709_l_ / 2 + 16;
        final int chkGlobalLabelWidth = getMinecraft().fontRenderer.getStringWidth(I18n.format("gui.waystones.waystone_settings.is_global"));
        if (mouseX >= chkGlobalLabelX && mouseX < chkGlobalLabelX + chkGlobalLabelWidth && mouseY >= chkGlobalLabelY && mouseY < chkGlobalLabelY + getMinecraft().fontRenderer.FONT_HEIGHT) {
            chkGlobal.setStateTriggered(!chkGlobal.isStateTriggered());
            return true;
        }

        if (textField.func_231044_a_(mouseX, mouseY, button)) {
            return true;
        }

        return super.func_231044_a_(mouseX, mouseY, button);
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            btnDone.func_230930_b_();
            return true;
        }

        if (textField.func_231046_a_(keyCode, scanCode, modifiers) || textField.func_230999_j_()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                Objects.requireNonNull(getMinecraft().player).closeScreen();
            }

            return true;
        }

        return super.func_231046_a_(keyCode, scanCode, modifiers);
    }

    @Override
    public void func_231023_e_() {
        textField.tick();
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        func_230446_a_(matrixStack);
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

        func_238476_c_(matrixStack, field_230712_o_, func_231171_q_().getString(), field_230708_k_ / 2 - 100, field_230709_l_ / 2 - 35, 0xFFFFFF);

        if (chkGlobal.field_230694_p_) {
            func_238476_c_(matrixStack, field_230712_o_, I18n.format("gui.waystones.waystone_settings.is_global"), field_230708_k_ / 2 - 100 + 25, field_230709_l_ / 2 + 16, 0xFFFFFF);
        }
    }

    @Override
    protected void func_230450_a_(MatrixStack p_230450_1_, float partialTicks, int mouseX, int mouseY) {
    }
}
