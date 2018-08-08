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
     * Gets a stream of points, and responds with statistics about the "trip": number of points,
     * number of known features visited, total distance traveled, and total time spent.
     *
     * @param responseObserver an observer to receive the response summary.
     * @return an observer to receive the requested route points.
     */
    @Override
    public StreamObserver<AudioStreamService.ByteStream> getAudioStream(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<AudioStreamService.ByteStream>() {
            @Override
            public void onNext(AudioStreamService.ByteStream byteStream) {
                logger.log(Level.INFO, "Got ByteStream chunk....");
                logger.log(Level.INFO, byteStream.toString());

            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "A ByteStream chunk did not transmit correctly....");

            }

            @Override
            public void onCompleted() {
                logger.log(Level.INFO, "ByteStream transmission completed....");
                logger.log(Level.INFO, "No errors detected during transmission....");
                responseObserver.onCompleted();

            }

        };

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private static final Logger logger = Logger.getLogger(AudioStreamServiceGrcpImpl.class.getName());

}
