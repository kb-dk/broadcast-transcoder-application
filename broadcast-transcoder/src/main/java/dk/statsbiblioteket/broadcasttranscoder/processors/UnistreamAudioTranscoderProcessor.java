package dk.statsbiblioteket.broadcasttranscoder.processors;

import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
import dk.statsbiblioteket.broadcasttranscoder.util.ExternalJobRunner;
import dk.statsbiblioteket.broadcasttranscoder.util.ExternalProcessTimedOutException;
import dk.statsbiblioteket.broadcasttranscoder.util.FileUtils;
import dk.statsbiblioteket.broadcasttranscoder.util.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 11/28/12
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class UnistreamAudioTranscoderProcessor extends ProcessorChainElement {

    private static Logger log = LoggerFactory.getLogger(UnistreamAudioTranscoderProcessor.class);


    @Override
    protected void processThis(TranscodeRequest request, Context context) throws ProcessorException {
        String command = "cat " + request.getClipperCommand() + " | " + getFfmpegCommandLine(request, context);
                File outputDir = FileUtils.getMediaOutputDir(request, context);
                outputDir.mkdirs();
                File outputFile = FileUtils.getMediaOutputFile(request, context);
                try {
                    long programLength = MetadataUtils.findProgramLengthMillis(request);
                    long timeout = programLength/context.getTranscodingTimeoutDivisor();
                    log.debug("Setting transcoding timeout for '" + context.getProgrampid() + "' to " + timeout + "ms");
                    request.setTranscoderCommand(command);
                    ExternalJobRunner.runClipperCommand(timeout, command);
                } catch (ExternalProcessTimedOutException e) {
                    log.warn("Deleting '" + outputFile + "'");
                    outputFile.delete();
                    throw new ProcessorException("External process timed out for " + context.getProgrampid(),e);
                }
    }

     public static String getFfmpegCommandLine(TranscodeRequest request, Context context) {
           File outputFile = FileUtils.getMediaOutputFile(request, context);
           String line = "ffmpeg -i - -acodec libmp3lame -ar 44100 "
                   + " -b:a " + context.getAudioBitrate() + "000 -y "
                   + outputFile.getAbsolutePath();
           return line;
       }

}