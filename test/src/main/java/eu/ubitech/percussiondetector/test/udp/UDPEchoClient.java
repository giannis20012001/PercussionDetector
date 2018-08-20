package eu.ubitech.percussiondetector.test.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPEchoClient {
    public static void main(String[] args) {
        System.out.println("UDP Echo Client Started");
        try {
            //Establish connection to server
            SocketAddress remote = new InetSocketAddress("127.0.0.1", 9000);
            DatagramChannel channel = DatagramChannel.open();//Create channel
            channel.connect(remote);//Connect socket address to channel

            //Create & send the message to the channel
            String message = "The message";
            ByteBuffer buffer = ByteBuffer.allocate(message.length());
            buffer.put(message.getBytes());
            buffer.flip();//To send the buffer to the server, a flip operation must take place (it sets position & limit for the server)
            channel.write(buffer);
            System.out.println("Sent: [" + message + "]");

            //Get reply message
            buffer.clear();//Clear buffer so that it can be reused
            channel.read(buffer);
            buffer.flip();//Switch buffer operations to reading mode
            System.out.print("Received: [");
            while(buffer.hasRemaining()) {
                System.out.print((char)buffer.get());

            }

            System.out.println("]");

        } catch (IOException ex) {
            ex.printStackTrace();

        }

        System.out.println("UDP Echo Client Terminated");

    }

}