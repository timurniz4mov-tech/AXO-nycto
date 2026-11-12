package com.hollowecho.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

public class EchoCompassItem extends Item {
    public static final TagKey<Structure> LIGHTHOUSE =
            TagKey.create(Registries.STRUCTURE, new ResourceLocation("hollowecho", "lighthouse"));

    public EchoCompassItem(Properties p) { super(p); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            player.getCooldowns().addCooldown(this, 60);
            BlockPos found = sl.findNearestMapStructure(LIGHTHOUSE, player.blockPosition(), 64, false);
            if (found == null) {
                player.displayClientMessage(Component.literal("Эхо молчит... Маяк слишком далеко.").withStyle(net.minecraft.ChatFormatting.GRAY), true);
            } else {
                double dx = found.getX() - player.getX(), dz = found.getZ() - player.getZ();
                String[] names = {"востоке", "юго-востоке", "юге", "юго-западе", "западе", "северо-западе", "севере", "северо-востоке"};
                double ang = (Math.toDegrees(Math.atan2(dz, dx)) + 360) % 360;
                int i = (int) Math.round(ang / 45.0) % 8;
                int dist = (int) Math.sqrt(dx * dx + dz * dz);
                player.displayClientMessage(Component.literal("Эхо Маяка звучит на " + names[i] + ", примерно " + dist + " блоков.")
                        .withStyle(net.minecraft.ChatFormatting.AQUA), true);
                sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1f, 0.8f);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
