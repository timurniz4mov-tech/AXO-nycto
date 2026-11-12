package com.hollowecho;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModEvents {
    private static final String[] PAGES = {
            "Если ты читаешь это — значит, свет снова погас.\n\nЯ не помню, как ты здесь оказался. Я тоже не помнил, когда очнулся.",
            "Найди Маяк. Он стоит на берегу или на холме и ждёт.\n\nКомпас эха покажет дорогу: нажми ПКМ.",
            "Говорят, в Маяке до сих пор кто-то живёт.\n\nНе бойся его. Он просто очень давно один.\n\n— Н."
    };

    /** Пролог: игрок «приходит в себя» в темноте, с дневником и компасом эха. */
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        CompoundTag d = Story.data(sp);
        if (d.getBoolean("hollowecho_intro")) return;
        d.putBoolean("hollowecho_intro", true);

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag t = book.getOrCreateTag();
        t.putString("title", "Дневник без имени");
        t.putString("author", "Н.");
        ListTag pages = new ListTag();
        for (String s : PAGES) pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(s))));
        t.put("pages", pages);

        give(sp, book);
        give(sp, new ItemStack(ModItems.ECHO_COMPASS.get()));

        sp.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
        sp.serverLevel().playSound(null, sp.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 2f, 0.7f);
        Story.titles(sp, "ЭХО ПУСТОТЫ", "Ты помнишь, как сюда попал?");
        sp.sendSystemMessage(Component.literal("В кармане лежит дневник и странный компас. Они тёплые.")
                .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
    }

    private static void give(ServerPlayer p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }
}
