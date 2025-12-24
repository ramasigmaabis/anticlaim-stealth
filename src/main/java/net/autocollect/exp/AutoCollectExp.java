package net.autocollect.exp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import java.util.ArrayList;
import java.util.List;

public class AutoCollectExp implements ModInitializer {
    private boolean aktif = false;
    private int slotTarget = 13;
    private long timer5Menit = 0;
    private final long JEDA_5_MENIT = 5 * 60 * 1000;
    private final List<BlockPos> antreanSpawner = new ArrayList<>();
    private boolean sedangProses = false;
    private long jedaPerSpawner = 0;

    @Override
    public void onInitialize() {
        // Menggunakan Command System yang benar untuk Fabric 1.21.4
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("exp")
                .then(ClientCommandManager.literal("on").executes(context -> {
                    aktif = true;
                    timer5Menit = System.currentTimeMillis();
                    sendChat("§aAuto EXP: NYALA");
                    return 1;
                }))
                .then(ClientCommandManager.literal("off").executes(context -> {
                    aktif = false;
                    antreanSpawner.clear();
                    sendChat("§cAuto EXP: MATI");
                    return 1;
                }))
            );
            
            dispatcher.register(ClientCommandManager.literal("setslot")
                .then(ClientCommandManager.argument("angka", IntegerArgumentType.integer())
                .executes(context -> {
                    slotTarget = IntegerArgumentType.getInteger(context, "angka");
                    sendChat("§6Slot target diubah ke: §f" + slotTarget);
                    return 1;
                }))
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!aktif || mc.player == null || mc.world == null) return;
            long sekarang = System.currentTimeMillis();

            if (sekarang - timer5Menit >= JEDA_5_MENIT && antreanSpawner.isEmpty() && !sedangProses) {
                scanSpawner(mc);
                timer5Menit = sekarang;
            }

            if (!antreanSpawner.isEmpty() && !sedangProses && sekarang - jedaPerSpawner > 800) {
                bukaSpawner(mc);
            }

            if (sedangProses && mc.currentScreen instanceof HandledScreen<?> screen) {
                mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, slotTarget, 0, SlotActionType.PICKUP, mc.player);
                sedangProses = false;
                jedaPerSpawner = sekarang;
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
        if (antreanSpawner.isEmpty()) return;
        BlockPos pos = antreanSpawner.remove(0);
        sedangProses = true;
        Vec3d v = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(v, Direction.UP, pos, false));
    }

    private void sendChat(String s) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.of("§8[§6AutoExp§8] " + s), false);
    }
}

