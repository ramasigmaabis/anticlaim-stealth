package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String savedJob = "";
    private boolean isRedirecting = false;

    @Override
    public void onInitialize() {
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.startsWith("#mine") || msg.startsWith("#click") || msg.startsWith("#goto")) {
                savedJob = msg;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String txt = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            // 1. Deteksi Claim
            if (txt.contains("permission to build here")) {
                if (!savedJob.isEmpty()) {
                    isRedirecting = true;
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

            // 2. Deteksi Selesai (Ditingkatkan ke Ignore Case & Trim)
            String raw = txt.toLowerCase().trim();
            if (isRedirecting && (raw.contains("path complete") || raw.contains("goal reached") || raw.contains("finished"))) {
                isRedirecting = false;
                
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        // Menggunakan mc.execute untuk memastikan perintah terkirim di thread utama
                        mc.execute(() -> {
                            if (mc.player != null && !savedJob.isEmpty()) {
                                mc.player.networkHandler.sendChatMessage("#gc");
                                mc.player.networkHandler.sendChatMessage(savedJob);
                            }
                        });
                    } catch (Exception e) {}
                }).start();
            }
        });
    }
                    }
                        
