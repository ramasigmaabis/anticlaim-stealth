package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private static String lastMiningCommand = "";
    private static boolean isEscaping = false;

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

            // 1. Deteksi wilayah claim
            if (chatContent.contains("permission to build here")) {
                if (client.player != null && !lastMiningCommand.isEmpty() && !isEscaping) {
                    isEscaping = true; // Tandai sedang dalam mode kabur
                    
                    client.player.networkHandler.sendChatMessage("#cancel");
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    
                    // Hitung koordinat 50 blok di belakang player
                    Vec3d pos = client.player.getPos();
                    float yaw = client.player.getYaw();
                    double radians = Math.toRadians(yaw);
                    double targetX = pos.x + (50 * Math.sin(radians));
                    double targetZ = pos.z - (50 * Math.cos(radians));
                    
                    client.player.networkHandler.sendChatMessage("#goto " + (int)targetX + " " + (int)pos.y + " " + (int)targetZ);
                }
            }

            // 2. Deteksi Baritone selesai jalan (berhenti)
            // Baritone biasanya mengirim pesan "Path complete" atau "Goal reached" saat selesai #goto
            if (isEscaping && (chatContent.contains("Path complete") || chatContent.contains("Goal reached"))) {
                isEscaping = false; 
                if (client.player != null) {
                    client.player.networkHandler.sendChatMessage("#gc");
                    // Tambah jeda sedikit biar aman sebelum lanjut mine
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            client.player.networkHandler.sendChatMessage(lastMiningCommand);
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
