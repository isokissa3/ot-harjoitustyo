package fi.joniaromaa.p2pchat.network.communication.manager;

import java.util.HashMap;
import java.util.Map;

import fi.joniaromaa.p2pchat.network.communication.IncomingPacket;
import fi.joniaromaa.p2pchat.network.communication.OutgoingPacket;
import fi.joniaromaa.p2pchat.network.communication.incoming.PingIncomingPacket;
import fi.joniaromaa.p2pchat.network.communication.incoming.authentication.RequestChallengeIncomingPacket;
import fi.joniaromaa.p2pchat.network.communication.incoming.authentication.SolveChallengeIncomingPacket;
import fi.joniaromaa.p2pchat.network.communication.incoming.authentication.WhoAreYouIncomingPacket;
import fi.joniaromaa.p2pchat.network.communication.incoming.chat.ChatMessageIncomingPacket;
import fi.joniaromaa.p2pchat.network.communication.outgoing.PingOutgoingPacket;
import fi.joniaromaa.p2pchat.network.communication.outgoing.authentication.RequestChallengeOutgoingPacket;
import fi.joniaromaa.p2pchat.network.communication.outgoing.authentication.SolveChallengeOutgoingPacket;
import fi.joniaromaa.p2pchat.network.communication.outgoing.authentication.WhoAreYouOutgoingPacket;
import fi.joniaromaa.p2pchat.network.communication.outgoing.chat.ChatMessageOutgoingPacket;
import io.netty.buffer.ByteBuf;

/**
 * Keeps list of packet id's and helps to write and read packets.
 */
public class PacketManager {
	private Map<Integer, Class<? extends IncomingPacket>> incoming;

	private Map<Class<? extends OutgoingPacket>, Integer> outgoing;

	public PacketManager() {
		this.incoming = new HashMap<>();

		this.outgoing = new HashMap<>();

		this.addIncomings();
		this.addOutgoings();
	}

	private void addIncomings() {
		this.addIncoming(0, WhoAreYouIncomingPacket.class);
		this.addIncoming(1, PingIncomingPacket.class);
		this.addIncoming(2, RequestChallengeIncomingPacket.class);
		this.addIncoming(3, SolveChallengeIncomingPacket.class);
		this.addIncoming(4, ChatMessageIncomingPacket.class);
	}

	private void addOutgoings() {
		this.addOutgoing(0, WhoAreYouOutgoingPacket.class);
		this.addOutgoing(1, PingOutgoingPacket.class);
		this.addOutgoing(2, RequestChallengeOutgoingPacket.class);
		this.addOutgoing(3, SolveChallengeOutgoingPacket.class);
		this.addOutgoing(4, ChatMessageOutgoingPacket.class);
	}

	protected void addIncoming(int id, Class<? extends IncomingPacket> packet) {
		this.incoming.put(id, packet);
	}

	protected void addOutgoing(int id, Class<? extends OutgoingPacket> packet) {
		this.outgoing.put(packet, id);
	}

	/**
	 * Creates according {@link IncomingPacket} based on the {@link ByteBuf} data.
	 * 
	 * @param in The {@link ByteBuf} containing the packet data.
	 * 
	 * @return The {@link IncomingPacket} that represents the {@link ByteBuf} data.
	 */
	public IncomingPacket readIncomingPacket(ByteBuf in) {
		int packetId = in.readByte(); // 7bit encoding could have been better

		Class<? extends IncomingPacket> clazz = this.incoming.get(packetId);
		if (clazz != null) {
			try {
				IncomingPacket packet = clazz.newInstance();
				packet.read(in);

				return packet;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	/**
	 * Writes {@link OutgoingPacket} data to {@link ByteBuf}.
	 * 
	 * @param packet The {@link OutgoingPacket} to write.
	 * @param out The {@link ByteBuf} to write the data to.
	 */
	public void writeOutgoingPacket(OutgoingPacket packet, ByteBuf out) {
		Integer packetId = this.outgoing.get(packet.getClass());
		if (packetId == null) {
			throw new RuntimeException("Packet id not found");
		}

		out.writeByte(packetId); // 7bit encoding could have been better

		packet.write(out);
	}
}
