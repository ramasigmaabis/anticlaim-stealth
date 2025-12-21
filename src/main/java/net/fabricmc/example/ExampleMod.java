package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class ExampleMod implements ModInitializer {
    private String currentJob = "";
    private boolean activeRedirect = false;

    @Override
    public void onInitialize() {
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.startsWith("#mine") || msg.startsWith("#click") || msg.startsWith("#goto")) {
                currentJob = msg;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String content = message.getString();
            MinecraftClient mc = MinecraftClient.getInstance();

            if (content.contains("permission to build here")) {
                if (mc.player != null && !currentJob.isEmpty()) {
                    activeRedirect = true;
                    
                    mc.player.networkHandler.sendChatMessage("#cancel");
                    mc.player.networkHandler.sendChatMessage("#blacklist");
                    
                    Vec3d pos = mc.player.getPos();
                    float yaw = mc.player.getYaw();
                    double rad = Math.toRadians(yaw + 90);
                    
                    int tx = (int) (pos.x - (35 * Math.sin(rad)));
                    int tz = (int) (pos.z + (35 * Math.cos(rad)));
                    
                    mc.player.networkHandler.sendChatMessage("#goto " + tx + " " + (int)pos.y + " " + tz);
                }
            }

            if (activeRedirect && (content.contains("Path complete") || content.contains("Goal reached"))) {
                activeRedirect = false;
                
                if (mc.player != null && !currentJob.isEmpty()) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(1200);
                            mc.execute(() -> {
                                mc.player.networkHandler.sendChatMessage("#gc");
                                mc.player.networkHandler.sendChatMessage(currentJob);
                            });
                        } catch (InterruptedException e) {}
                    }).start();
                }
            }
        });
    }
}
