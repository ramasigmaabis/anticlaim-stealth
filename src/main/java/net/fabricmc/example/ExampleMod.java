package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chat = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();

            if (chat.contains("permission to build here")) {
                if (client.player != null) {
                    // 1. Matikan lompat
                    client.player.setJumping(false);
                    
                    // 2. Kirim perintah internal Baritone pakai prefix '#'
                    // Gunakan sendChatMessage agar diproses sebagai command oleh Baritone
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    client.player.networkHandler.sendChatMessage("#gc");
                    
                    // 3. Putar badan menjauh agar tidak stuck di border yang sama
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    new Thread(() -> {
                        try {
                            // 4. Paksa lari menjauh selama 1.5 detik
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(1500);
                            client.options.forwardKey.setPressed(false);
                            
                            // 5. Resume Baritone dengan target baru
                            client.player.networkHandler.sendChatMessage("#resume");
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
