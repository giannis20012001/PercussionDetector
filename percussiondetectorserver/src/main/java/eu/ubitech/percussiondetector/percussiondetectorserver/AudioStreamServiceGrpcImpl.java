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
public class AudioStreamServiceGrpcImpl extends AudioStreamGrpc.AudioStreamImplBase {
    /**
     * It receives a stream of bytes. It then passes the stream through an algorithm to detect possible percussion.
     *
     * @param responseObserver an empty (dummy) observer ro maintain correct structure.
     * @return an observer to receive the requested stream of bytes.
     */
    @Override
    public StreamObserver<AudioStreamService.ByteStream> setAudioStream(StreamObserver<Empty> responseObserver) {
          /*LOGGER.log(Level.INFO, "No. of active thread: " + Thread.activeCount());
        LOGGER.log(Level.INFO, percussionDetector.getThread().toString());
        LOGGER.log(Level.INFO, String.valueOf(percussionDetector.getThread().isAlive()));*/

        return new StreamObserver<AudioStreamService.ByteStream>() {
            @Override
            public void onNext(AudioStreamService.ByteStream byteStream) {
                synchronized(this) {
                    receivedAudioData = byteStream.getByteChunk().toByteArray();
                    //LOGGER.log(Level.INFO, Arrays.toString(receivedAudioData));
                    if (!setDispatcherFlag) {
                        LOGGER.info("Started listening to input stream with params: "
                                + percussionDetector.getSensitivity() + "%, "
                                + percussionDetector.getThreshold() + "dB");
                        setDispatcherFlag = true;

                    }

                    percussionDetector.setDispatcher();

                }

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
    //Getter & setters
    //==================================================================================================================
    public static byte[] getReceivedAudioData() {
        return receivedAudioData;

    }

    //==================================================================================================================
    //Entity constructor
    //==================================================================================================================
    /**
     * Default constructor
     */
    AudioStreamServiceGrpcImpl() {
        super();
        //initialize input stream buffer (byte array)
        receivedAudioData = new byte[10000];
        //Set Percussion detection engine
        percussionDetector = new PercussionDetector();

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private boolean setDispatcherFlag = false;
    private static byte[] receivedAudioData;
    private PercussionDetector percussionDetector;
    private static final Logger LOGGER = Logger.getLogger(AudioStreamServiceGrpcImpl.class.getName());

}