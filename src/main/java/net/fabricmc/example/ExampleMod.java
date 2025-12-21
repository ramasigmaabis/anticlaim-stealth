package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String lastTask = "";
    private boolean needResume = false;

    @Override
    public void onInitialize() {
        // Simpan command manual kamu
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.startsWith("#mine") || msg.startsWith("#click") || msg.startsWith("#goto")) {
                lastTask = msg;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String raw = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            // 1. DETEKSI CLAIM -> KABUR
            if (raw.contains("permission to build here")) {
                if (!lastTask.isEmpty()) {
                    needResume = true; // Tandai bahwa kita harus balik kerja nanti
                    mc.player.networkHandler.sendChatMessage("#cancel");
                    mc.player.networkHandler.sendChatMessage("#blacklist");
                    
                    Vec3d p = mc.player.getPos();
                    float y = mc.player.getYaw();
                    double r = Math.toRadians(y + 90);
                    
                    int tx = (int) (p.x - (35 * Math.sin(r)));
                    int tz = (int) (p.z + (35 * Math.cos(r)));
                    
                    mc.player.networkHandler.sendChatMessage("#goto " + tx + " " + (int)p.y + " " + tz);
                }
            }

            // 2. DETEKSI STOP/SELESAI -> PAKSA RESUME
            // Baritone akan mengirim pesan ini kalau rute selesai atau dibatalkan
            String chat = raw.toLowerCase();
            if (needResume && (chat.contains("path complete") || chat.contains("goal reached") || chat.contains("canceled"))) {
                needResume = false; // Reset tanda
                
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Tunggu Baritone tenang
                        mc.execute(() -> {
                            if (mc.player != null && !lastTask.isEmpty()) {
                                mc.player.networkHandler.sendChatMessage("#gc");
                                mc.player.networkHandler.sendChatMessage(lastTask);
                            }
                        });
                    } catch (Exception ignored) {}
                }).start();
            }
        });
    }
}
