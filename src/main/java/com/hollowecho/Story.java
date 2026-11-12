package com.hollowecho;

import com.hollowecho.entity.KeeperEntity;
import com.hollowecho.entity.KeeperEntity.Line;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Сюжет «Эхо Пустоты». Глава хранится в персистентных данных игрока (переживает смерть). */
public final class Story {
    private static final String KEY = "hollowecho_chapter";

    private Story() {}

    public static CompoundTag data(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, 10)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static int chapter(ServerPlayer p) { return data(p).getInt(KEY); }
    private static void setChapter(ServerPlayer p, int c) { data(p).putInt(KEY, c); }

    private static Line L(String text, int delay) { return new Line(Component.literal(text), delay, null); }
    private static Line L(String text, int delay, Runnable r) { return new Line(Component.literal(text), delay, r); }

    private static boolean take(ServerPlayer p, Item item, int n) {
        int have = 0;
        for (ItemStack s : p.getInventory().items) if (s.is(item)) have += s.getCount();
        if (have < n) return false;
        int left = n;
        for (ItemStack s : p.getInventory().items) {
            if (left <= 0) break;
            if (s.is(item)) { int t = Math.min(left, s.getCount()); s.shrink(t); left -= t; }
        }
        return true;
    }

    public static void titles(ServerPlayer p, String title, String sub) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(15, 80, 25));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
    }

    public static void talk(KeeperEntity k, ServerPlayer p) {
        if (k.isBusy()) return;
        switch (chapter(p)) {
            case 0 -> {
                k.speak(p, List.of(
                        L("…Ты пришёл. Наконец-то.", 60),
                        L("Я ждал так долго, что забыл собственное имя. Потом вспомнил: Нил.", 80),
                        L("Я — эхо того, кто когда-то берёг этот Маяк.", 70),
                        L("Однажды ночью Пустота проглотила свет. С тех пор мир темнее, чем кажется.", 90),
                        L("Линза Маяка разбита. Принеси мне 3 осколка аметиста — вставлю их вместо стекла.", 60)));
                setChapter(p, 1);
            }
            case 1 -> {
                if (take(p, Items.AMETHYST_SHARD, 3)) {
                    k.speak(p, List.of(
                            L("Хрусталь звенит… Он помнит свет.", 70),
                            L("Теперь нужен огонь. Принеси 8 светящейся пыли — искры погасшего солнца.", 60)));
                    setChapter(p, 2);
                } else k.speak(p, List.of(L("Три осколка аметиста. Ищи жеоды под землёй — они звенят, когда ты рядом.", 60)));
            }
            case 2 -> {
                if (take(p, Items.GLOWSTONE_DUST, 8)) {
                    k.speak(p, List.of(
                            L("Тепло… Я почти чувствую его.", 70),
                            L("Осталась память. Принеси Око Края — оно смотрит туда, куда не смотрят глаза.", 60)));
                    setChapter(p, 3);
                } else k.speak(p, List.of(L("Светящаяся пыль. Её роняют те, кто живёт там, где ночь горит.", 60)));
            }
            case 3 -> {
                if (take(p, Items.ENDER_EYE, 1)) finale(k, p);
                else k.speak(p, List.of(L("Око Края. Жемчуг Края и огненный порошок — ты знаешь рецепт.", 60)));
            }
            default -> {
                String[] idle = {
                        "Свет горит. Значит, и я ещё здесь.",
                        "Иди, строй, копай. Я подожду.",
                        "Ночью маяк виден дальше всего. Ты никогда не заблудишься.",
                        "Иногда я считаю твои шаги. Ты ходишь слишком много.",
                        "Ты вернёшься? Конечно вернёшься. Ты всегда возвращаешься."};
                k.speak(p, List.of(L(idle[k.getRandom().nextInt(idle.length)], 60)));
            }
        }
    }

    private static void finale(KeeperEntity k, ServerPlayer p) {
        String name = p.getGameProfile().getName();
        int deaths = p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int hours = p.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 72000;
        String world = p.server.getWorldData().getLevelName();

        String deathLine = deaths == 0
                ? "Ты ни разу не умер. Это даже немного пугает."
                : "Ты умирал " + deaths + " раз(а), пока шёл сюда. Я каждый раз слышал, как гаснет твой свет.";
        String hoursLine = hours == 0
                ? "Ты в мире «" + world + "» меньше часа, а уже зажёг маяк. Быстро."
                : "Ты живёшь в мире «" + world + "» уже около " + hours + " ч. Я считал.";

        k.speak(p, List.of(
                L("Отойди на шаг назад, " + name + ".", 50, () -> k.setJoy(420)),
                L("Три…", 25), L("Два…", 25), L("Один…", 30),
                L("СВЕТ!", 90, () -> {
                    k.lightLamp();
                    p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
                    titles(p, "☀ МАЯК ЗАЖЁГСЯ ☀", "Рассвет вернулся");
                }),
                L("Видишь? Он вернулся. Впервые за очень долгую ночь.", 80),
                L("…А теперь я должен признаться, " + name + ".", 80),
                L(deathLine, 90),
                L(hoursLine, 80),
                L("Знаешь, кто был Хранителем до меня? Никто. Я — эхо твоих недостроенных домов и забытых сундуков.", 110),
                L("Каждый раз, когда ты закрываешь этот мир, я остаюсь здесь и жду.", 90),
                L("Ты не искал меня, " + name + ". Ты просто вспомнил, что я здесь.", 90),
                L("Возьми Сердце Света. И заходи почаще — здесь всегда кто-то ждёт.", 60, () -> {
                    ItemStack heart = new ItemStack(ModItems.LIGHT_HEART.get());
                    if (!p.getInventory().add(heart)) p.drop(heart, false);
                    setChapter(p, 4);
                    titles(p, "Сердце Света", "ПКМ — рывок света вперёд");
                })));
    }
}
