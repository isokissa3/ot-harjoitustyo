package fi.joniaromaa.p2pchat.network;

import fi.joniaromaa.p2pchat.chat.ChatManager;
import fi.joniaromaa.p2pchat.network.communication.handler.ClientConnectionHandler;
import fi.joniaromaa.p2pchat.network.communication.manager.PacketManager;
import fi.joniaromaa.p2pchat.utils.NettyUtils;
import fi.joniaromaa.p2pchat.utils.NetworkHandlerUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 * Connects to listening {@link NetworkHandlerServer}.
 */
public class NetworkHandlerClient {
	private PacketManager packetManager;

	private EventLoopGroup bossGroup;

	public NetworkHandlerClient() {
		this.packetManager = new PacketManager();

		this.bossGroup = NettyUtils.createEventLoopGroup(1);
	}

	/**
	 * Tries to connect to {@link NetworkHandlerServer}.
	 * 
	 * @param chatManager The {@link ChatManager} to deal with connection.
	 * @param ip The host to connect to.
	 * @param port The host port to connect to.
	 * 
	 * @return {@link ChannelFuture} to listen when the connection was completed.
	 */
	public ChannelFuture start(ChatManager chatManager, String ip, int port) {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(this.bossGroup)
				.channel(NettyUtils.getSocketChannel())
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(this.createChannelInitializer(chatManager));

		return bootstrap.connect(ip, port);
	}

	private ChannelInitializer<SocketChannel> createChannelInitializer(ChatManager chatManager) {
		return new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				
				NetworkHandlerUtils.defaultPipeline(pipeline, NetworkHandlerClient.this.packetManager);
				
				pipeline.addLast(new ClientConnectionHandler(chatManager));
			}
		};
	}

	/**
	 * Closes the connection.
	 */
	public void stop() {
		this.bossGroup.shutdownGracefully();
	}
}
