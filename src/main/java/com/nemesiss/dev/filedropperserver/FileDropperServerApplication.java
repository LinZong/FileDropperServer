package com.nemesiss.dev.filedropperserver;

import com.nemesiss.dev.filedropperserver.services.configuration.FileDropperConfigurationLoader;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
public class FileDropperServerApplication {

    public static void main(String[] args) throws InterruptedException {

        defaultLaunch(args);
//        broadcaster();
    }

    public static void defaultLaunch(String[] args) {
        FileDropperConfigurationLoader.injectServerConfigurationToSpringProperties(
                new SpringApplication(FileDropperServerApplication.class)
        ).run(args);
    }

    public static void tcp() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new StringEncoder());


        Channel channel = bootstrap.connect("127.0.0.1", 39393).sync().channel();

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    channel.writeAndFlush("Hello, world!");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
        thread.join();
    }

    public static void monitor() throws InterruptedException {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageToMessageDecoder<DatagramPacket>() {
                            @Override
                            protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
                                ByteBuf data = msg.content();
                                System.out.println("RECEIVER --- " + data.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                });


        Channel channel = bootstrap.bind(39393).sync().channel();
        channel.closeFuture().sync();
    }

    public static void broadcaster() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageToMessageEncoder<String>() {
                            @Override
                            protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
                                byte[] strBytes = s.getBytes();
                                ByteBuf buffer = channelHandlerContext.alloc().buffer(strBytes.length);
                                buffer.writeBytes(strBytes);
                                list.add(new DatagramPacket(buffer, new InetSocketAddress("255.255.255.255", 39393)));
                            }
                        });

                        pipeline.addLast(new MessageToMessageDecoder<DatagramPacket>() {
                            @Override
                            protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
                                ByteBuf content = msg.content();
                                System.out.println("Received:  " + content.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                });


        ChannelFuture future = bootstrap.bind(39393).sync();
        Channel channel = future.channel();

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    channel.writeAndFlush("Hello, world!");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread.join();

        eventLoopGroup.shutdownGracefully();
    }
}
