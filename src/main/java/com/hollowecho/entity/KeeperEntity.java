package com.hollowecho.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Нил — эхо Хранителя Маяка. Говорит, жестикулирует, парит и умеет зажигать маяк. */
public class KeeperEntity extends PathfinderMob {
    public record Line(Component text, int delay, Runnable action) {}

    private static final EntityDataAccessor<Integer> TALK = SynchedEntityData.defineId(KeeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> JOY = SynchedEntityData.defineId(KeeperEntity.class, EntityDataSerializers.INT);
    private static final String[] WHISPERS = {
            "…ты слышишь эхо?", "…не уходи так быстро…", "…свет помнит тебя…", "…я ждал именно тебя…"};

    private final Deque<Line> queue = new ArrayDeque<>();
    private UUID listener;
    private int wait;
    private int whisperCd = 60;
    private boolean lit;
    private BlockPos lampPos;

    public KeeperEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setInvulnerable(true);
        setPersistenceRequired();
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TALK, 0);
        entityData.define(JOY, 0);
    }

    public int getTalkTicks() { return entityData.get(TALK); }
    public int getJoyTicks() { return entityData.get(JOY); }
    public void setJoy(int ticks) { entityData.set(JOY, ticks); }
    public boolean isBusy() { return !queue.isEmpty(); }

    public void speak(ServerPlayer p, List<Line> lines) {
        queue.clear();
        listener = p.getUUID();
        wait = 0;
        queue.addAll(lines);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            com.hollowecho.Story.talk(this, sp);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (!level().isClientSide && src.getEntity() instanceof ServerPlayer p && queue.isEmpty()) {
            speak(p, List.of(new Line(Component.literal("…Это больно только тебе."), 40, null)));
        }
        return super.hurt(src, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        ServerLevel sl = (ServerLevel) level();

        int t = entityData.get(TALK);
        if (t > 0) entityData.set(TALK, t - 1);
        int j = entityData.get(JOY);
        if (j > 0) entityData.set(JOY, j - 1);

        if (t > 0 && tickCount % 5 == 0)
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.3, getZ(), 1, 0.25, 0.2, 0.25, 0.01);
        if (j > 0)
            sl.sendParticles(ParticleTypes.FIREWORK, getX(), getY() + 1.0, getZ(), 4, 0.5, 0.8, 0.5, 0.05);

        processQueue(sl);

        if (lit && lampPos != null) {
            double h = (tickCount * 0.7) % 50;
            sl.sendParticles(ParticleTypes.END_ROD, lampPos.getX() + 0.5, lampPos.getY() + 1 + h, lampPos.getZ() + 0.5, 1, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.END_ROD, lampPos.getX() + 0.5, lampPos.getY() + 1 + ((h + 25) % 50), lampPos.getZ() + 0.5, 1, 0, 0, 0, 0);
        }

        if (whisperCd > 0) whisperCd--;
        if (tickCount % 20 == 0 && whisperCd <= 0 && queue.isEmpty()) {
            Player np = level().getNearestPlayer(this, 5.0);
            if (np instanceof ServerPlayer sp) {
                whisperCd = 1200;
                sp.displayClientMessage(Component.literal(WHISPERS[random.nextInt(WHISPERS.length)])
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
            }
        }
    }

    private void processQueue(ServerLevel sl) {
        if (queue.isEmpty()) return;
        if (wait > 0) { wait--; return; }
        ServerPlayer p = listener == null ? null : sl.getServer().getPlayerList().getPlayer(listener);
        if (p == null) { queue.clear(); return; }
        Line l = queue.poll();
        p.sendSystemMessage(Component.literal("Нил: ").withStyle(ChatFormatting.GOLD)
                .append(l.text().copy().withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
        sl.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.2f, 0.6f + random.nextFloat() * 0.4f);
        sl.playSound(null, blockPosition(), SoundEvents.VILLAGER_AMBIENT, SoundSource.NEUTRAL, 0.6f, 0.5f);
        entityData.set(TALK, Math.max(40, l.delay()));
        wait = l.delay();
        if (l.action() != null) l.action().run();
    }

    /** Зажигает лампу маяка (блок тонированного стекла над Хранителем) и рассвет. */
    public void lightLamp() {
        if (!(level() instanceof ServerLevel sl)) return;
        BlockPos base = blockPosition();
        lampPos = null;
        for (int dy = 0; dy <= 32; dy++) {
            BlockPos bp = base.above(dy);
            if (sl.getBlockState(bp).is(Blocks.TINTED_GLASS)) {
                sl.setBlock(bp, Blocks.SEA_LANTERN.defaultBlockState(), 3);
                lampPos = bp;
                break;
            }
        }
        if (lampPos == null) lampPos = base.above(2);
        lit = true;
        long time = sl.getDayTime();
        if (time % 24000L > 12000L) sl.setDayTime((time / 24000L + 1) * 24000L);
        sl.setWeatherParameters(12000, 0, false, false);
        sl.playSound(null, lampPos, SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 6f, 0.7f);
        sl.sendParticles(ParticleTypes.END_ROD, lampPos.getX() + 0.5, lampPos.getY() + 0.5, lampPos.getZ() + 0.5, 300, 1, 1, 1, 0.3);
        for (int dy = 0; dy < 60; dy += 2)
            sl.sendParticles(ParticleTypes.END_ROD, lampPos.getX() + 0.5, lampPos.getY() + dy, lampPos.getZ() + 0.5, 6, 0.1, 0.1, 0.1, 0.08);
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean canBeLeashed(Player p) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putBoolean("Lit", lit);
        if (lampPos != null) t.putLong("Lamp", lampPos.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        lit = t.getBoolean("Lit");
        if (t.contains("Lamp")) lampPos = BlockPos.of(t.getLong("Lamp"));
    }
}
