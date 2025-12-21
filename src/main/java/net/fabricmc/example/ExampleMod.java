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
                    
                    // Setting otomatis agar Baritone tidak keras kepala
                    client.player.networkHandler.sendChatMessage("#settings blacklistThreshold 1");
                    client.player.networkHandler.sendChatMessage("#settings avoidPermissions true");
                    
                    // Reset total
                    client.player.networkHandler.sendChatMessage("#pause");
                    client.player.networkHandler.sendChatMessage("#cancel");
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    client.player.networkHandler.sendChatMessage("#gc");
                    
                    client.player.setYaw(client.player.getYaw() + 180);
                    
                    new Thread(() -> {
                        try {
                            // Lari menjauh agar keluar dari radius claim
                            client.options.forwardKey.setPressed(true);
                            Thread.sleep(4000);
                            client.options.forwardKey.setPressed(false);
                            
                            Thread.sleep(1500);
                            // Eksekusi ulang perintah terakhir
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
