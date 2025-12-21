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
                    // 1. Matikan jumping agar tidak loncat di tempat
                    client.player.setJumping(false);
                    
                    // 2. Batalkan tugas yang sekarang (supaya dia lupa blok di dalam claim)
                    client.player.networkHandler.sendChatMessage("#cancel");
                    
                    // 3. Tandai lokasi itu sebagai blacklist agar tidak didatangi lagi
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    
                    // 4. Balik badan 180 derajat
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    new Thread(() -> {
                        try {
                            // 5. Paksa jalan menjauh selama 2 detik (cari lahan baru)
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(2000);
                            client.options.forwardKey.setPressed(false);
                            
                            // 6. NAH, INI KUNCINYA: Suruh Baritone mining lagi dari nol
                            // Dia akan mencari blok terdekat dari posisi barumu yang bukan di area blacklist
                            client.player.networkHandler.sendChatMessage("#mine log"); 
                            
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
