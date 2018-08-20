package eu.ubitech.percussiondetector.test.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPEchoServer {
    public static void main(String[] args) {
        int port = 9000;
        System.out.println("UDP Echo Server Started");
        try (DatagramChannel channel = DatagramChannel.open();
             DatagramSocket socket = channel.socket()){
            //Inside try-catch
            SocketAddress address = new InetSocketAddress(port);
            socket.bind(address);
            ByteBuffer buffer = ByteBuffer.allocateDirect(65507);
            while (true) {
                //Get message
                SocketAddress client = channel.receive(buffer);
                buffer.flip();//Switch buffer operations to reading mode

                //Display message
                // Mark and then reset so that it will send properly
                buffer.mark();
                System.out.print("Received: [");
                StringBuilder message = new StringBuilder();
                while (buffer.hasRemaining()) {
                    message.append((char) buffer.get());

                }

                System.out.println(message + "]");
                buffer.reset();

                //Return message
                channel.send(buffer, client);
                System.out.println("Sent: [" + message + "]");
                buffer.clear();

            }

        } catch (IOException ex) {
            ex.printStackTrace();

        }

        System.out.println("UDP Echo Server Terminated");

    }

}