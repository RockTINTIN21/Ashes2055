// com.ashes2055.net.Net
package com.ashes2055.net;

import com.ashes2055.Ashes2055;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    public static final String PROTOCOL = "1";
    public static SimpleChannel CH;
    private static boolean INITIALIZED = false;

    /** Вызывать ОДИН раз на старте мода (до начала игры) */
    public static void register() {
        if (INITIALIZED) return;

        CH = NetworkRegistry.newSimpleChannel(
                ResourceLocation.fromNamespaceAndPath(Ashes2055.MOD_ID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
        );

        int id = 0;
        CH.registerMessage(id++, ShotSfxS2C.class, ShotSfxS2C::encode, ShotSfxS2C::decode, ShotSfxS2C::handle);

        INITIALIZED = true;
    }

    private Net() {}
}
