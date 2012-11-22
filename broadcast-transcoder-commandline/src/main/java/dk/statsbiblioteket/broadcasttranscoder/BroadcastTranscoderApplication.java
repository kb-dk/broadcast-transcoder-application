package dk.statsbiblioteket.broadcasttranscoder;

import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
import dk.statsbiblioteket.broadcasttranscoder.cli.OptionParseException;
import dk.statsbiblioteket.broadcasttranscoder.cli.OptionsParser;
import dk.statsbiblioteket.broadcasttranscoder.processors.*;
import dk.statsbiblioteket.broadcasttranscoder.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class BroadcastTranscoderApplication {

    private static Logger logger = LoggerFactory.getLogger(BroadcastTranscoderApplication.class);

    public static void main(String[] args) throws OptionParseException, ProcessorException {
        logger.debug("Entered main method.");
        Context context = new OptionsParser().parseOptions(args);
        TranscodeRequest request = new TranscodeRequest();
        File lockFile = FileUtils.getLockFile(request, context);
        if (lockFile.exists()) {
            logger.warn("Lockfile " + lockFile.getAbsolutePath() + " already exists. Exiting.");
            System.exit(2);
        }
        try {
            boolean created = lockFile.createNewFile();
            if (!created) {
                logger.warn("Could not create lockfile: " + lockFile.getAbsolutePath() + ". Exiting.");
                System.exit(3);
            }
        } catch (IOException e) {
            logger.warn("Could not create lockfile: " + lockFile.getAbsolutePath() + ". Exiting.");
            System.exit(3);
        }
        try {
            ProcessorChainElement programFetcher = new ProgramMetadataFetcherProcessor();
            //ProcessorChainElement metadataExtractor = new PersistentMetadataExtractorProcessor();
            ProcessorChainElement filedataFetcher    = new FileMetadataFetcherProcessor();
            ProcessorChainElement sorter = new BroadcastMetadataSorterProcessor();
            ProcessorChainElement fileFinderFetcher = new FilefinderFetcherProcessor();
            ProcessorChainElement identifier = new FilePropertiesIdentifierProcessor();
            ProcessorChainElement clipper = new ClipFinderProcessor();
            ProcessorChainElement coverage = new CoverageAnalyserProcessor();
            ProcessorChainElement updater = new ProgramStructureUpdaterProcessor();
            ProcessorChainElement fixer = new StructureFixerProcessor();
            ProcessorChainElement dispatcher = new TranscoderDispatcherProcessor();
            programFetcher.setChildElement(filedataFetcher);
            //metadataExtractor.setChildElement(filedataFetcher);
            filedataFetcher.setChildElement(sorter);
            sorter.setChildElement(fileFinderFetcher);
            fileFinderFetcher.setChildElement(identifier);
            identifier.setChildElement(clipper);
            clipper.setChildElement(coverage);
            coverage.setChildElement(updater);
            updater.setChildElement(fixer);
            fixer.setChildElement(dispatcher);
            programFetcher.processIteratively(request, context);
        } finally {
            boolean deleted = lockFile.delete();
            if (!deleted) {
                logger.error("Could not delete lockfile: " + lockFile.getAbsolutePath());
                System.exit(4);
            }
        }
    }

}
