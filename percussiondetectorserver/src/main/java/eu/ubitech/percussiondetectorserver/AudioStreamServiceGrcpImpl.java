package eu.ubitech.percussiondetectorserver;

import com.google.protobuf.Empty;
import eu.ubitech.grcp.AudioStreamGrpc;
import eu.ubitech.grcp.AudioStreamService;
import io.grpc.stub.StreamObserver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by John Tsantilis on 8/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
public class AudioStreamServiceGrcpImpl extends AudioStreamGrpc.AudioStreamImplBase {
    /**
     * It receives a stream of bytes. It then passes the stream through an algorithm to detect possible percussion.
     *
     * @param responseObserver an empty (dummy) observer ro maintain correct structure.
     * @return an observer to receive the requested stream of bytes.
     */
    @Override
    public StreamObserver<AudioStreamService.ByteStream> setAudioStream(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<AudioStreamService.ByteStream>() {
            @Override
            public void onNext(AudioStreamService.ByteStream byteStream) {
                LOGGER.log(Level.INFO, "Received ByteStream chunk....");
                LOGGER.log(Level.INFO, byteStream.toString());

            }

            @Override
            public void onError(Throwable t) {
                LOGGER.log(Level.WARNING, "A ByteStream chunk did not transmit correctly....");

            }

            @Override
            public void onCompleted() {
                LOGGER.log(Level.INFO, "ByteStream transmission completed....");
                LOGGER.log(Level.INFO, "No errors detected during transmission....");
                responseObserver.onCompleted();

            }

        };

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private static final Logger LOGGER = Logger.getLogger(AudioStreamServiceGrcpImpl.class.getName());

}
