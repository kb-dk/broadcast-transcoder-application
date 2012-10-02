/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 *
 */
package dk.statsbiblioteket.broadcasttranscoder.processors;

import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
import dk.statsbiblioteket.broadcasttranscoder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class WavTranscoderProcessor extends ProcessorChainElement {

    private static Logger logger = LoggerFactory.getLogger(WavTranscoderProcessor.class);

    @Override
    protected void processThis(TranscodeRequest request, Context context) throws ProcessorException {
        String command = getMultiClipCommand(request, context);
        File outputFile = FileUtils.getMediaOutputFile(request, context);
        FileUtils.getMediaOutputFile(request, context).mkdirs();
        try {
            long programLength = MetadataUtils.findProgramLengthMillis(request);
            long timeout = programLength/context.getTranscodingTimeoutDivisor();
            logger.debug("Setting transcoding timeout for '" + context.getProgrampid() + "' to " + timeout + "ms");
            request.setClipperCommand(command);
            ExternalJobRunner.runClipperCommand(timeout, command);
        } catch (ExternalProcessTimedOutException e) {
            logger.warn("Deleting '" + outputFile.getAbsolutePath() + "'", e);
            outputFile.delete();
            throw new ProcessorException(e);
        }
        this.setChildElement(new PreviewClipperProcessor());
    }

    private String getMultiClipCommand(TranscodeRequest request, Context context) {
        String command = "cat ";
        List<TranscodeRequest.FileClip> clips = request.getClips();
        long bitrate = request.getBitrate();
        for (int i=0; i<clips.size(); i++) {
            TranscodeRequest.FileClip clip = clips.get(i);
            String soxTranscodeParameters = context.getSoxTranscodeParams();
            command += " <(sox " + clip.getFilepath() + " " + soxTranscodeParameters + " - ";
            if ((clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0) || clip.getClipLength() != null) {
                String trimFilter = " trim ";
                if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0) {
                    long startOffsetSeconds = clip.getStartOffsetBytes()/bitrate;
                    if (i == 0) {
                        startOffsetSeconds = Math.max(0, startOffsetSeconds);
                    }
                    trimFilter += startOffsetSeconds + ".0 ";
                } else {
                    trimFilter += " 0.0 ";
                }
                if (clip.getClipLength() != null) {
                    long clipLengthSeconds = clip.getClipLength()/bitrate;
                    if ( i==0 ) {
                        clipLengthSeconds = clipLengthSeconds;
                    }
                    trimFilter += clipLengthSeconds + ".0 ";
                }
                command += trimFilter;
            }
            command += " ) ";
        }
        command += "| ffmpeg -f s16le -i - "
                + " -ab " + context.getAudioBitrate() + "000 "
                + FileUtils.getMediaOutputFile(request, context).getAbsolutePath();
        return command;
    }

}
