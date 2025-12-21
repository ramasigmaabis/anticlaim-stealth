package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExampleMod implements ModInitializer {
    private String lastBlockPos = "";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onInitialize() {
        // 1. Deteksi Chat Error Claim (Bahasa Inggris Umum)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chat = message.getString().toLowerCase();
            if (chat.contains("you don't have permission") || 
                chat.contains("hey! you cannot") || 
                chat.contains("area is protected") ||
                chat.contains("claimed by")) {
                perintahCariJalanLain();
            }
        });

        // 2. Cek apakah block tidak hancur dalam 1 detik
        scheduler.scheduleAtFixedRate(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.crosshairTarget != null && 
                client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                
                BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
                String currentPos = hit.getBlockPos().toString();

                if (currentPos.equals(lastBlockPos)) {
                    // Jika posisi crosshair masih di block yang sama setelah 1 detik
                    perintahCariJalanLain();
                }
                lastBlockPos = currentPos;
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void perintahCariJalanLain() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Logika sederhana: Player akan mencoba melompat atau bergerak sedikit
            // untuk memicu bot mencari rute navigasi baru
            client.execute(() -> {
                client.player.jump();
                System.out.println("[Stealth] Stuck detected! Finding new path...");
            });
        }
    }
}
