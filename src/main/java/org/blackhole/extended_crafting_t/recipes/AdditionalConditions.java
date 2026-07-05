package org.blackhole.extended_crafting_t.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record AdditionalConditions(
        Optional<String> requiredDimension,
        Optional<XpCondition> xpCost,
        Optional<XpCondition> xpLvl,
        Optional<String> requiredAdvancement,
        Optional<String> requiredHp,
        Optional<TimeCondition> requiredTime
) {
    public static final Codec<AdditionalConditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("required_dimension").forGetter(AdditionalConditions::requiredDimension),
            XpCondition.CODEC.optionalFieldOf("xpCost").forGetter(AdditionalConditions::xpCost),
            XpCondition.CODEC.optionalFieldOf("xpLvl").forGetter(AdditionalConditions::xpLvl),
            Codec.STRING.optionalFieldOf("required_advancement").forGetter(AdditionalConditions::requiredAdvancement),
            Codec.STRING.optionalFieldOf("required_hp").forGetter(AdditionalConditions::requiredHp),
            TimeCondition.CODEC.optionalFieldOf("required_time").forGetter(AdditionalConditions::requiredTime)
    ).apply(instance, AdditionalConditions::new));

    public record XpCondition(int value, boolean consumable) {
        public static final Codec<XpCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("value").forGetter(XpCondition::value),
                Codec.BOOL.fieldOf("consumable").forGetter(XpCondition::consumable)
        ).apply(instance, XpCondition::new));
    }

    public record TimeCondition(String value, int interval, String intervalValue) {
        public static final Codec<TimeCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("value").forGetter(TimeCondition::value),
                Codec.INT.fieldOf("interval").forGetter(TimeCondition::interval),
                Codec.STRING.fieldOf("interval_value").forGetter(TimeCondition::intervalValue)
        ).apply(instance, TimeCondition::new));
    }
}
