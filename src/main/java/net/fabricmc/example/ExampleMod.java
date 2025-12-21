package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String job = "";
    private boolean checkIdle = false;
    private Vec3d lastPos = Vec3d.ZERO;
    private long stopTime = 0;

    @Override
    public void onInitialize() {
        // Catat command mining terakhirmu
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.startsWith("#mine") || msg.startsWith("#click") || msg.startsWith("#goto")) {
                job = msg;
            }
        });

        // Deteksi kalau kena claim
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (text.contains("permission to build here") && mc.player != null && !job.isEmpty()) {
                checkIdle = true;
                stopTime = System.currentTimeMillis();
                
                mc.player.networkHandler.sendChatMessage("#cancel");
                mc.player.networkHandler.sendChatMessage("#blacklist");
                
                // Kabur ke kanan 35 blok
                Vec3d p = mc.player.getPos();
                float y = mc.player.getYaw();
                double r = Math.toRadians(y + 90);
                int tx = (int) (p.x - (40 * Math.sin(r)));
                int tz = (int) (p.z + (40 * Math.cos(r)));
                
                mc.player.networkHandler.sendChatMessage("#goto " + tx + " " + (int)p.y + " " + tz);
            }
        });

        // PANTauan OTOMATIS (Tanpa perlu buka chat)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || !checkIdle) return;

            Vec3d currentPos = mc.player.getPos();
            
            // Cek apakah posisi berubah (masih jalan atau tidak)
            if (currentPos.distanceTo(lastPos) > 0.01) {
                stopTime = System.currentTimeMillis(); // Reset waktu kalau masih gerak
            } else {
                // Kalau sudah diam selama 2 detik (2000ms)
                if (System.currentTimeMillis() - stopTime > 2000) {
                    checkIdle = false; // Matikan pengecekan
                    mc.player.networkHandler.sendChatMessage("#gc");
                    mc.player.networkHandler.sendChatMessage(job); // OTOMATIS MINE LAGI
                }
            }
            lastPos = currentPos;
        });
    }
}
