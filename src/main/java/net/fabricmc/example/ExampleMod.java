package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayMessagingEvents;
import net.minecraft.client.MinecraftClient;

public class ExampleMod implements ModInitializer {
    private String lastBaritoneCommand = "#mine log"; // Default jika belum ngetik apa-apa

    @Override
    public void onInitialize() {
        // Bagian untuk mencatat perintah terakhir
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            String text = message.getString();
            if (text.startsWith("#") && !text.contains("blacklist") && !text.contains("stop")) {
                lastBaritoneCommand = text;
            }
            return message;
        });

        // Bagian eksekusi saat kena claim
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chatContent = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();

            if (chatContent.contains("permission to build here")) {
                if (client.player != null) {
                    client.player.setJumping(false);
                    
                    client.player.networkHandler.sendChatMessage("#stop");
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    new Thread(() -> {
                        try {
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(2000);
                            client.options.forwardKey.setPressed(false);
                            
                            Thread.sleep(1000);
                            // Mengulang perintah terakhir yang di ketik manual
                            client.player.networkHandler.sendChatMessage(lastBaritoneCommand);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });
    }
}
