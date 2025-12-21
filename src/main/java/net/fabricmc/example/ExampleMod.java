package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String lastJob = "";
    private boolean isWaiting = false;
    private Vec3d lastPos = Vec3d.ZERO;
    private long lastMoveTime = 0;

    @Override
    public void onInitialize() {
        // Simpan perintah mining kamu
        ClientSendMessageEvents.CHAT.register(m -> {
            if (m.startsWith("#mine") || m.startsWith("#click") || m.startsWith("#goto")) {
                lastJob = m;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            // 1. Kena Claim -> Kabur ke kanan
            if (text.contains("permission to build here")) {
                if (!lastJob.isEmpty()) {
                    isWaiting = true; // Mulai pantau gerakan
                    lastMoveTime = System.currentTimeMillis();
                    
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

            // 2. Cek Gerakan (Idle Detection)
            if (isWaiting) {
                Vec3d currentPos = mc.player.getPos();
                
                // Jika player bergerak, reset waktu diam
                if (currentPos.distanceTo(lastPos) > 0.05) {
                    lastMoveTime = System.currentTimeMillis();
                } 
                // Jika player diam lebih dari 2 detik (2000ms)
                else if (System.currentTimeMillis() - lastMoveTime > 2000) {
                    isWaiting = false;
                    
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            mc.execute(() -> {
                                if (mc.player != null && !lastJob.isEmpty()) {
                                    mc.player.networkHandler.sendChatMessage("#gc");
                                    mc.player.networkHandler.sendChatMessage(lastJob);
                                }
                            });
                        } catch (Exception ignored) {}
                    }).start();
                }
                lastPos = currentPos;
            }
        });
    }
}
