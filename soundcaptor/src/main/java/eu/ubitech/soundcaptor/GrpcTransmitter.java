package eu.ubitech.soundcaptor;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import eu.ubitech.grcp.AudioStreamGrpc;
import eu.ubitech.grcp.AudioStreamService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by John Tsantilis on 9/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
public class GrpcTransmitter implements Runnable {
    /**
     * Async client-streaming. Sends {@code numPoints} randomly chosen points from {@code
     * features} with a variable delay in between.
     */
    private void setAudioStream() throws IOException {
        info("Setting AudioStream...");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty emptyResponse) {
                info("Transmission of sound stream finished successfully...");

            }

            @Override
            public void onError(Throwable t) {
                warning("ByteStream transmission failed: {0}", Status.fromThrowable(t));
                finishLatch.countDown();

            }

            @Override
            public void onCompleted() {
                info("Transmission of sound stream finished successfully...");
                info("No errors detected during transmission....");
                finishLatch.countDown();

            }

        };

        StreamObserver<AudioStreamService.ByteStream> requestObserver = asyncStub.setAudioStream(responseObserver);
        info("Begin transmission streaming byte chunks...");
        try {
            int bytes = 0;
            while ((bytes = stream.read(tempBuffer)) != 0) {
                //Sending sound byte chunks to server
                AudioStreamService.ByteStream chunk = AudioStreamService.ByteStream.newBuilder()
                        .setByteChunk(
                                ByteString.copyFrom(
                                        tempBuffer,
                                        0,
                                        tempBuffer.length))
                        .build();
                //info("Transmitting byte chunk: {0}", chunk);
                requestObserver.onNext(chunk); //Send stream
                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return;

                }

            }

        } catch (RuntimeException e) {
            requestObserver.onError(e); //Cancel RPC
            throw e;

        }

        //Mark the end of requests
        requestObserver.onCompleted();

    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);

    }

    private void info(String msg, Object... params) {
        LOGGER.log(Level.INFO, msg, params);

    }

    private void warning(String msg, Object... params) {
        LOGGER.log(Level.WARNING, msg, params);

    }

    private void error(String msg, Object... params) {
        LOGGER.log(Level.SEVERE, msg, params);

    }

    @Override
    public void run() {
        try {
            setAudioStream();

        } catch (IOException e) {
            error(e.getMessage());

        } finally {
            try {
                shutdown();

            } catch (InterruptedException e) {
                error(e.getMessage());

            }

        }

    }

    //==================================================================================================================
    //Entity constructor
    //==================================================================================================================
    /**
     *Construct client for accessing PercussionDetectorServer
     * */
    GrpcTransmitter(AudioInputStream stream) {
        this.stream = stream;
        //Create a gRPC channel for Stub
        channel = ManagedChannelBuilder.forAddress(HOSTNAME, PORT).usePlaintext(true).build();
        asyncStub = AudioStreamGrpc.newStub(channel);

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private AudioInputStream stream;
    private byte tempBuffer[] = new byte[512];
    private final ManagedChannel channel;
    private final AudioStreamGrpc.AudioStreamStub asyncStub;
    //=========================================================
    private final static int PORT = 50000;
    private final static String HOSTNAME = "localhost";
    private static final Logger LOGGER = Logger.getLogger(SoundCaptorClient.class.getName());

}