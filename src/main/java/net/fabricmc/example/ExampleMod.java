package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String task = "";
    private boolean pending = false;
    private Vec3d lastPos = Vec3d.ZERO;
    private long stopTime = 0;

    @Override
    public void onInitialize() {
        // Catat command terakhir
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.startsWith("#mine") || msg.startsWith("#click") || msg.startsWith("#goto")) {
                task = msg;
            }
        });

        // Deteksi Claim
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String txt = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();
            
            if (txt.contains("permission to build here") && mc.player != null && !task.isEmpty()) {
                pending = true; // Kunci antrian
                stopTime = System.currentTimeMillis();
                
                mc.player.networkHandler.sendChatMessage("#cancel");
                mc.player.networkHandler.sendChatMessage("#blacklist");
                
                Vec3d p = mc.player.getPos();
                float y = mc.player.getYaw();
                double r = Math.toRadians(y + 90);
                int tx = (int) (p.x - (35 * Math.sin(r)));
                int tz = (int) (p.z + (35 * Math.cos(r)));
                
                mc.player.networkHandler.sendChatMessage("#goto " + tx + " " + (int)p.y + " " + tz);
            }
        });

        // Tick Monitor (Pengecekan gerakan 20x per detik)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || !pending) return;

            Vec3d currentPos = mc.player.getPos();
            
            // Jika ada pergerakan (jarak lebih dari 0.01 blok)
            if (currentPos.distanceTo(lastPos) > 0.01) {
                stopTime = System.currentTimeMillis(); // Reset timer
            } else {
                // Jika sudah diam total selama 2 detik
                if (System.currentTimeMillis() - stopTime > 2000) {
                    pending = false; // Matikan antrian agar tidak spam
                    
                    // Eksekusi paksa
                    mc.player.networkHandler.sendChatMessage("#gc");
                    mc.player.networkHandler.sendChatMessage(task);
                }
            }
            lastPos = currentPos;
        });
    }
}
