package com.nexusreds.addons.client.screen;

import com.nexusreds.addons.screen.PlanterScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PlanterScreen extends HandledScreen<PlanterScreenHandler> {
    // Caminho da sua textura!
    private static final Identifier TEXTURE = Identifier.of("nexusreds", "textures/gui/planter_gui.png");

    public PlanterScreen(PlanterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Centraliza os títulos (Opcional)
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        
        // A largura de 215 continua perfeita, pois cobre as duas abas laterais.
        context.drawTexture(TEXTURE, x, y, 0, 0, 215, backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        // Verifica se o rato está em cima das DUAS abas laterais (X entre 176 e 205)
        // Aumentamos o limite de 'top + 45' para 'top + 65' para englobar a altura do segundo slot!
        boolean clicouNaAba = mouseX >= left + 176 && mouseX < left + 205 && mouseY >= top + 10 && mouseY < top + 65;
        
        // Só joga o item no chão se NÃO clicou nas abas E clicou fora do fundo principal
        return !clicouNaAba && super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }
}