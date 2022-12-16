package net.nonswag.tnl.mappings.v1_19_R2.api.player.channel;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.tnl.listener.api.event.EventManager;
import net.nonswag.tnl.listener.api.mapper.Mapping;
import net.nonswag.tnl.listener.api.packets.incoming.IncomingPacket;
import net.nonswag.tnl.listener.api.packets.listener.PacketReader;
import net.nonswag.tnl.listener.api.packets.listener.PacketWriter;
import net.nonswag.tnl.listener.api.packets.outgoing.OutgoingPacket;
import net.nonswag.tnl.listener.api.player.TNLPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicBoolean;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PlayerChannelHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        try {
            var readers = EventManager.getAllReaders(packet.getClass());
            if (!readers.isEmpty()) {
                AtomicBoolean cancelled = new AtomicBoolean();
                var incoming = Mapping.get().packetManager().incoming().map(packet);
                readers.forEach(reader -> tryCatch(() -> ((PacketReader<IncomingPacket>) reader).read(getPlayer(), incoming, cancelled)));
                if (!cancelled.get()) super.channelRead(context, incoming.build());
            } else super.channelRead(context, packet);
        } catch (Exception e) {
            e.printStackTrace();
            super.channelRead(context, packet);
        }
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise channel) throws Exception {
        try {
            var writers = EventManager.getAllWriters(packet.getClass());
            if (!writers.isEmpty()) {
                AtomicBoolean cancelled = new AtomicBoolean();
                var outgoing = Mapping.get().packetManager().outgoing().map(packet);
                writers.forEach(writer -> tryCatch(() -> ((PacketWriter<OutgoingPacket>) writer).write(getPlayer(), outgoing, cancelled)));
                if (!cancelled.get()) super.write(context, outgoing.build(), channel);
            } else super.write(context, packet, channel);
        } catch (Exception e) {
            e.printStackTrace();
            super.write(context, packet, channel);
        }
    }

    private void tryCatch(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract TNLPlayer getPlayer();
}
