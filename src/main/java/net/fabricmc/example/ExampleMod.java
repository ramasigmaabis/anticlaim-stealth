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
                    // 1. Matikan jumping
                    client.player.setJumping(false);
                    
                    // 2. Blacklist blok bermasalah TANPA menghentikan proses utama
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    
                    // 3. Putar badan menjauh
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    new Thread(() -> {
                        try {
                            // 4. Paksa jalan menjauh 2 detik agar keluar dari claim
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(2000);
                            client.options.forwardKey.setPressed(false);
                            
                            // 5. Jeda sebentar agar posisi sinkron
                            Thread.sleep(500);
                            
                            // 6. PAKSA BARITONE MENCARI JALAN BARU
                            // Perintah ini akan membuat Baritone tetap menjalankan tugas terakhirnya 
                            // (apapun itu) tapi dari koordinat kamu yang baru.
                            client.player.networkHandler.sendChatMessage("#repath");
                            
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
