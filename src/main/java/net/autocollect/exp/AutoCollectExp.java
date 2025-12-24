package net.autocollect.exp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoCollectExp implements ModInitializer {
    private boolean aktif = false;
    private int slotTarget = 13; // Default slot (kotak ke-14)
    private long timer5Menit = 0;
    private final long JEDA_5_MENIT = 5 * 60 * 1000;
    
    private final List<BlockPos> antreanSpawner = new ArrayList<>();
    private boolean sedangProses = false;
    private long jedaPerSpawner = 0;
    private long timeoutGUI = 0;

    @Override
    public void onInitialize() {
        // PERINTAH CHAT
        ClientSendMessageEvents.CHAT.register(msg -> {
            if (msg.equalsIgnoreCase(".exp on")) {
                aktif = true;
                timer5Menit = System.currentTimeMillis();
                sendChat("§aAuto EXP: NYALA");
                return "";
            } 
            if (msg.equalsIgnoreCase(".exp off")) {
                aktif = false;
                antreanSpawner.clear();
                sendChat("§cAuto EXP: MATI");
                return "";
            }
            if (msg.startsWith(".setslot ")) {
                try {
                    slotTarget = Integer.parseInt(msg.split(" ")[1]);
                    sendChat("§6Slot target diubah ke: §f" + slotTarget);
                } catch (Exception e) {
                    sendChat("§cGunakan angka! Contoh: .setslot 13");
                }
                return "";
            }
            return msg;
        });

        // LOGIKA TICK
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!aktif || mc.player == null || mc.world == null) return;

            long sekarang = System.currentTimeMillis();

            // LANGKAH 1: Scan setiap 5 menit
            if (sekarang - timer5Menit >= JEDA_5_MENIT && antreanSpawner.isEmpty() && !sedangProses) {
                scanSpawner(mc);
                timer5Menit = sekarang;
            }

            // LANGKAH 2: Ambil spawner dari antrean (Jeda 0.8 detik biar aman)
            if (!antreanSpawner.isEmpty() && !sedangProses && sekarang - jedaPerSpawner > 800) {
                bukaSpawner(mc);
            }

            // LANGKAH 3: Klik icon di dalam GUI
            if (sedangProses && mc.currentScreen instanceof HandledScreen<?> screen) {
                // Klik kiri pada slot target
                mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, slotTarget, 0, SlotActionType.PICKUP, mc.player);
                
                // Menandai selesai (Tanpa tutup manual, biarkan server yang tutup)
                sedangProses = false;
                jedaPerSpawner = sekarang;
                timeoutGUI = 0;
            }

            // PENGAMAN: Jika GUI tidak muncul/eror dalam 4 detik, lanjut ke spawner lain
            if (sedangProses && timeoutGUI > 0 && sekarang - timeoutGUI > 4000) {
                sedangProses = false;
                jedaPerSpawner = sekarang;
                timeoutGUI = 0;
            }
        });
    }

    private void scanSpawner(MinecraftClient mc) {
        BlockPos pPos = mc.player.getBlockPos();
        antreanSpawner.clear();
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = pPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock().asItem().toString().contains("spawner")) {
                        antreanSpawner.add(pos);
                    }
                }
            }
        }
        if (!antreanSpawner.isEmpty()) sendChat("§eMenemukan " + antreanSpawner.size() + " spawner...");
    }

    private void bukaSpawner(MinecraftClient mc) {
        BlockPos pos = antreanSpawner.remove(0);
        sedangProses = true;
        timeoutGUI = System.currentTimeMillis();
        Vec3d v = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(v, Direction.UP, pos, false));
    }

    private void sendChat(String s) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.of("§8[§6AutoExp§8] " + s), false);
    }
  }
