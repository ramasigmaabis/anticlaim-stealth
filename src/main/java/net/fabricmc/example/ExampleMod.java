package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String task = "";
    private boolean moving = false;

    @Override
    public void onInitialize() {
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.startsWith("#mine") || msg.startsWith("#click") || msg.startsWith("#goto")) {
                task = msg;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String raw = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            // 1. Deteksi Claim
            if (raw.contains("permission to build here")) {
                if (!task.isEmpty()) {
                    moving = true;
                    mc.player.networkHandler.sendChatMessage("#cancel");
                    mc.player.networkHandler.sendChatMessage("#blacklist");
                    
                    Vec3d p = mc.player.getPos();
                    float y = mc.player.getYaw();
                    double r = Math.toRadians(y + 90);
                    
                    int x = (int) (p.x - (35 * Math.sin(r)));
                    int z = (int) (p.z + (35 * Math.cos(r)));
                    
                    mc.player.networkHandler.sendChatMessage("#goto " + x + " " + (int)p.y + " " + z);
                }
            }

            // 2. Deteksi Selesai & Lanjut Mine
            String log = raw.toLowerCase();
            if (moving && (log.contains("path complete") || log.contains("goal reached"))) {
                moving = false;
                
                new Thread(() -> {
                    try {
                        // Jeda 2 detik agar posisi sinkron dan Baritone siap
                        Thread.sleep(2000);
                        mc.execute(() -> {
                            if (mc.player != null && task.startsWith("#")) {
                                mc.player.networkHandler.sendChatMessage("#gc");
                                mc.player.networkHandler.sendChatMessage(task);
                            }
                        });
                    } catch (Exception ignored) {}
                }).start();
            }
        });
    }
}
