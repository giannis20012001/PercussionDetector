package eu.ubitech.percussiondetectorserver;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Created by John Tsantilis on 2/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */

public class PercussionDetectorServer {
    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        LOGGER.info("Server started, listening on " + PORT);
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    //Use stderr here since the LOGGER may has been reset by its JVM shutdown hook.
                    System.err.println("*** shutting down gRPC server since JVM is shutting down");
                    PercussionDetectorServer.this.stop();
                    System.err.println("*** server shut down");

                })

        );

    }

    /** Stop serving requests and shutdown resources. */
    public void stop() {
        if (server != null) {
            server.shutdown();

        }

    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();

        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        PercussionDetectorServer server = new PercussionDetectorServer();
        server.start();
        server.blockUntilShutdown();

    }

    //==================================================================================================================
    //Entity constructor
    //==================================================================================================================
    /** Create a RouteGuide server using serverBuilder as a base */
    public PercussionDetectorServer() {
        ServerBuilder builder = NettyServerBuilder.forAddress(new InetSocketAddress(HOSTNAME, PORT));
        Executor executor = MoreExecutors.directExecutor();
        builder.executor(executor);
        server = builder.addService(new AudioStreamServiceGrcpImpl()).build();

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private final Server server;
    private final static int PORT = 50000;
    private final static String HOSTNAME = "localhost";
    private static final Logger LOGGER = Logger.getLogger(PercussionDetectorServer.class.getName());

}