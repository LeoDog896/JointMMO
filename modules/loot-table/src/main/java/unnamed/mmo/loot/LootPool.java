package unnamed.mmo.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import unnamed.mmo.data.number.NumberProvider;

import java.util.ArrayList;
import java.util.List;

public record LootPool(
        @NotNull List<LootPredicate> conditions,
        @NotNull List<LootModifier> modifiers,
        @NotNull List<LootEntry<?>> entries,
        @NotNull NumberProvider rolls
) {

    public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(i -> i.group(
            LootPredicate.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(LootPool::conditions),
            LootModifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(LootPool::modifiers),
            LootEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(LootPool::entries),
            NumberProvider.CODEC.optionalFieldOf("rolls", NumberProvider.constant(1)).forGetter(LootPool::rolls)
    ).apply(i, LootPool::new));

    public @NotNull List<@NotNull Object> generate(@NotNull LootContext context) {
        // Ensure all conditions match
        if (!LootPredicate.all(context, conditions()))
            return List.of();

        // Generate available entries
        var options = entries()
                .stream()
                .map(entry -> entry.generate(context))
                .flatMap(List::stream)
                .toList();
        int totalWeight = options.stream()
                .mapToInt(LootEntry.Option::weight).sum();

        // Roll for entries
        List<@NotNull Object> output = new ArrayList<>();
        for (int i = 0; i < rolls().nextLong(context); i++) {
            int roll = (int) (context.random() * totalWeight);
            for (LootEntry.Option<?> option : options) {
                roll -= option.weight();
                if (roll <= 0) {
                    output.addAll(option.loot());
                    break;
                }
            }
        }

        // Apply modifiers to final entries
        return output.stream()
                .map(entry -> {
                    for (LootModifier modifier : modifiers())
                        entry = modifier.apply(entry);
                    return entry;
                })
                .toList();
    }

}
