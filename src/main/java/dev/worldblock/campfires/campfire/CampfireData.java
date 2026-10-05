package dev.worldblock.campfires.campfire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CampfireData {
    public static final Codec<CampfireData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("fuel", -1).forGetter(data -> data.fuel),
                    Codec.INT.optionalFieldOf("ember", 0).forGetter(data -> data.emberTimer),
                    Codec.INT.optionalFieldOf("rain_ticks", 0).forGetter(data -> data.rainTicks),
                    Codec.INT.optionalFieldOf("ignite", 0).forGetter(data -> data.igniteTimer),
                    Codec.INT.optionalFieldOf("ignite_total", 0).forGetter(data -> data.igniteTotal),
                    ItemStack.CODEC.listOf().optionalFieldOf("logs", List.of()).forGetter(data -> data.logTypes))
            .apply(instance, CampfireData::new));

    public int fuel = -1;
    public int emberTimer;
    public int rainTicks;
    public int rainCheck;
    public int igniteTimer;
    public int igniteTotal;
    public List<ItemStack> logTypes = new ArrayList<>();
    public long lastTick;
    public long lastIgniteActionTick;
    public java.util.UUID ignitingPlayer;

    public CampfireData() {
    }

    private CampfireData(int fuel, int emberTimer, int rainTicks, int igniteTimer, int igniteTotal, List<ItemStack> logTypes) {
        this.fuel = fuel;
        this.emberTimer = emberTimer;
        this.rainTicks = rainTicks;
        this.igniteTimer = igniteTimer;
        this.igniteTotal = igniteTotal;
        this.logTypes = new ArrayList<>(logTypes);
    }

    public List<ItemStack> remainingLogs() {
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : this.logTypes) {
            remaining.add(stack.isEmpty() ? new ItemStack(Items.OAK_LOG) : stack.copy());
        }
        return remaining;
    }

    public void addLog(ItemStack stack) {
        this.logTypes.add(stack.copyWithCount(1));
    }

    public void setLogs(List<ItemStack> logs) {
        this.logTypes = new ArrayList<>();
        for (ItemStack stack : logs) {
            this.logTypes.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
    }

    public void consumeOldest() {
        if (!this.logTypes.isEmpty()) {
            this.logTypes.remove(0);
        }
    }
}
