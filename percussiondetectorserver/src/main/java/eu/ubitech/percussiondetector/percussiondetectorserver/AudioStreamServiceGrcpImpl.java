package eu.ubitech.percussiondetector.percussiondetectorserver;

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
        PercussionDetector percussionDetector = new PercussionDetector(receivedAudioData);
        percussionDetector.setNewMixer();

        return new StreamObserver<AudioStreamService.ByteStream>() {
            @Override
            public void onNext(AudioStreamService.ByteStream byteStream) {
                /*LOGGER.log(Level.INFO, "Received ByteStream chunk....");
                LOGGER.log(Level.INFO, byteStream.toString());*/
                receivedAudioData = byteStream.getByteChunk().toByteArray();

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
    //Entity constructor
    //==================================================================================================================
    /**
     * Default constructor
     *
     */
    AudioStreamServiceGrcpImpl() {
        super();

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private byte receivedAudioData[] = new byte[4096];
    private static final Logger LOGGER = Logger.getLogger(AudioStreamServiceGrcpImpl.class.getName());

}
