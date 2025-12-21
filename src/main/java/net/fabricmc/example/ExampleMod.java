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
                    // 1. Matikan lompat otomatis mod kita
                    client.player.setJumping(false);
                    
                    // 2. Blacklist koordinat sekarang agar Baritone kapok ke sini
                    // Perintah ini menandai area sekitar agar Baritone cari jalan memutar
                    client.player.networkHandler.sendChatCommand("baritone gc"); 
                    client.player.networkHandler.sendChatCommand("baritone blacklist");
                    
                    // 3. Putar badan menjauh
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    // 4. Paksa gerak menjauh sebentar agar tidak stuck di border
                    new Thread(() -> {
                        try {
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(1200);
                            client.options.forwardKey.setPressed(false);
                            
                            // 5. Perintahkan Baritone cari target baru
                            // Ini akan memaksa Baritone menghitung ulang rute
                            client.player.networkHandler.sendChatCommand("baritone resume");
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
