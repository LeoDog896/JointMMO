package unnamed.mmo.data.number;

import com.google.auto.service.AutoService;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import unnamed.mmo.data.NumberSource;
import unnamed.mmo.util.ExtraCodecs;

record ConstantNumberProvider(
        @NotNull Number value
) implements NumberProvider {

    public static Codec<ConstantNumberProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.NUMBER.fieldOf("value").forGetter(ConstantNumberProvider::value)
    ).apply(i, ConstantNumberProvider::new));


    @Override
    public long nextLong(@NotNull NumberSource numbers) {
        return value().longValue();
    }

    @Override
    public double nextDouble(@NotNull NumberSource numbers) {
        return value().doubleValue();
    }


    @AutoService(NumberProvider.Factory.class)
    public static final class Factory extends NumberProvider.Factory {
        public Factory() {
            super(
                    NamespaceID.from("unnamed:constant"),
                    ConstantNumberProvider.class,
                    ConstantNumberProvider.CODEC
            );
        }
    }

}
