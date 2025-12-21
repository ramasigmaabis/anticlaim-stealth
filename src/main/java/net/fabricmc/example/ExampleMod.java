package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Mod Anti-Claim V3.1: Active!");

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chat = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();

            if (chat.contains("permission to build here") || chat.contains("You can't build")) {
                if (client.player != null) {
                    // Berhenti loncat & putar badan 180 derajat
                    client.player.setJumping(false);
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    // Paksa jalan maju sedikit agar pindah lokasi
                    new Thread(() -> {
                        try {
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(800);
                            client.options.forwardKey.setPressed(false);
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
