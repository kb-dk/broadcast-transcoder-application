package dk.statsbiblioteket.broadcasttranscoder.processors;

import dk.statsbiblioteket.broadcasttranscoder.cli.SingleTranscodingContext;
import dk.statsbiblioteket.broadcasttranscoder.mock.DomsMockApi;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import junit.framework.TestCase;

import java.util.LinkedList;

/**
 *
 */
public class ProgramMetadataFetcherProcessorTest extends TestCase {

    public void testProcessThis() throws ProcessorException, MethodFailedException, InvalidResourceException, InvalidCredentialsException {
        ProgramMetadataFetcherProcessor processor = new ProgramMetadataFetcherProcessor();
        TranscodeRequest request = new TranscodeRequest();
        SingleTranscodingContext context = new SingleTranscodingContext();
        DomsMockApi mockApi = new DomsMockApi();
        String pid = mockApi.newObject("sfdds", new LinkedList<String>(), "Sdfds");
        mockApi.modifyDatastream(pid,"PROGRAM_BROADCAST","\n" +
                "\n" +
                "<programBroadcast xmlns=\"http://doms.statsbiblioteket.dk/types/program_broadcast/0/1/#\">\n" +
                "  <timeStart>2007-02-01T00:43:47.000+01:00</timeStart>\n" +
                "  <timeStop>2007-02-01T01:05:49.000+01:00</timeStop>\n" +
                "  <channelId>tv2z</channelId>\n" +
                "</programBroadcast>","comment");
        context.setDomsApi(mockApi);
        context.setProgrampid(pid);
        processor.processThis(request,context);
    }

}
