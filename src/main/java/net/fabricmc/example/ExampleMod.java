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

            if (chatContent.contains("permission to build here")) {
                if (client.player != null && !lastMiningCommand.isEmpty()) {
                    
                    client.player.networkHandler.sendChatMessage("#cancel");
                    client.player.networkHandler.sendChatMessage("#blacklist");
                    
                    // Hitung koordinat 35 blok ke arah KANAN player
                    Vec3d pos = client.player.getPos();
                    float yaw = client.player.getYaw();
                    // Menambah 90 derajat ke yaw untuk mendapatkan arah kanan
                    double radians = Math.toRadians(yaw + 90);
                    double targetX = pos.x - (35 * Math.sin(radians));
                    double targetZ = pos.z + (35 * Math.cos(radians));
                    
                    isEscaping = true;
                    client.player.networkHandler.sendChatMessage("#goto " + (int)targetX + " " + (int)pos.y + " " + (int)targetZ);
                }
            }

            // Jika Baritone selesai sampai di titik 35 blok
            if (isEscaping && (chatContent.contains("Path complete") || chatContent.contains("Goal reached"))) {
                isEscaping = false;
                if (client.player != null) {
                    client.player.networkHandler.sendChatMessage("#gc");
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500); // Jeda biar stabil
                            client.player.networkHandler.sendChatMessage(lastMiningCommand);
                        } catch (Exception e) {}
                    }).start();
                }
            }
        });
    }
}
