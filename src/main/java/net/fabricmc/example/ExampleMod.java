package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

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
                    
                    client.player.networkHandler.sendChatMessage("#settings blacklistThreshold 1");
                    client.player.networkHandler.sendChatMessage("#cancel");
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    
                    // Ambil koordinat saat ini dan hitung titik yang berjarak 20 blok di belakang player
                    Vec3d pos = client.player.getPos();
                    float yaw = client.player.getYaw();
                    double radians = Math.toRadians(yaw);
                    
                    // Rumus menghitung titik di belakang player
                    double targetX = pos.x + (20 * Math.sin(radians));
                    double targetZ = pos.z - (20 * Math.cos(radians));
                    int targetY = (int) pos.y;

                    // Perintah Baritone untuk lari ke koordinat aman tersebut
                    String escapeCommand = "#goto " + (int)targetX + " " + targetY + " " + (int)targetZ;
                    client.player.networkHandler.sendChatMessage(escapeCommand);
                    
                    new Thread(() -> {
                        try {
                            // Tunggu 5 detik selama Baritone berusaha menuju titik aman (kabur dari claim)
                            Thread.sleep(5000);
                            
                            // Bersihkan cache rute lama agar tidak "kangen" area claim
                            client.player.networkHandler.sendChatMessage("#gc");
                            Thread.sleep(500);
                            
                            // Lanjut mining perintah terakhir
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
