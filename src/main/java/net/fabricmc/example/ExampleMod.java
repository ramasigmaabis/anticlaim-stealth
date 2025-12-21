package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;

public class ExampleMod implements ModInitializer {
    private static String lastMiningCommand = "";

    @Override
    public void onInitialize() {
        ClientSendMessageEvents.CHAT.register((message) -> {
            if (message.startsWith("#mine") || message.startsWith("#click") || message.startsWith("#goto")) {
                lastMiningCommand = message;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chatContent = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();

            if (chatContent.contains("permission to build here")) {
                if (client.player != null && !lastMiningCommand.isEmpty()) {
                    client.player.setJumping(false);
                    
                    // Berhenti dan lupakan target sekarang
                    client.player.networkHandler.sendChatMessage("#cancel");
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    // GC untuk membersihkan cache rute agar tidak balik ke rute lama
                    client.player.networkHandler.sendChatMessage("#gc");
                    
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    new Thread(() -> {
                        try {
                            // Jalan menjauh lebih lama (3 detik) agar benar-benar keluar area
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(3000);
                            client.options.forwardKey.setPressed(false);
                            
                            // Jeda agar posisi sinkron dengan server
                            Thread.sleep(1500);
                            
                            // Paksa Baritone mulai ulang tugas dari nol di lokasi baru
                            client.player.networkHandler.sendChatMessage(lastMiningCommand);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });
    }
}
