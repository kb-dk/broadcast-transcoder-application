
package dk.statsbiblioteket.broadcasttranscoder.fetcher;

import dk.statsbiblioteket.broadcasttranscoder.cli.OptionParseException;
import dk.statsbiblioteket.broadcasttranscoder.fetcher.cli.FetcherContext;
import dk.statsbiblioteket.broadcasttranscoder.fetcher.cli.OptionsParser;
import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.BroadcastTranscodingRecordDAO;
import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.HibernateUtil;
import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.ReklamefilmTranscodingRecordDAO;
import dk.statsbiblioteket.broadcasttranscoder.persistence.dao.TranscodingProcessInterface;
import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.TranscodingRecord;
import dk.statsbiblioteket.broadcasttranscoder.processors.ProcessorException;
import dk.statsbiblioteket.broadcasttranscoder.util.CentralWebserviceFactory;
import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.central.RecordDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 11/21/12
 * Time: 11:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class BtaDomsFetcher {

    private static Logger logger = LoggerFactory.getLogger(BtaDomsFetcher.class);


    public static void main(String[] args) throws OptionParseException, ProcessorException {
        logger.debug("Entered main method.");
        FetcherContext<TranscodingRecord> context = new OptionsParser<TranscodingRecord>().parseOptions(args);
        try {

            CentralWebservice doms = CentralWebserviceFactory.getServiceInstance(context);
            List<RecordDescription> records = requestInBatches(doms, context);

            HibernateUtil util = HibernateUtil.getInstance(context.getHibernateConfigFile().getAbsolutePath());

            TranscodingProcessInterface<? extends TranscodingRecord> dao;
            if (context.getCollection().equals("doms:RadioTV_Collection")){
                dao = new BroadcastTranscodingRecordDAO(util);
            } else if (context.getCollection().equals("doms:Collection_Reklamefilm")){
                dao = new ReklamefilmTranscodingRecordDAO(util);
            } else {
                logger.error("Error in initial environment");
                System.exit(6);
                return;
            }

            for (RecordDescription record : records) {
                dao.markAsChangedInDoms(record.getPid(), record.getDate());
            }

        } catch (Exception e) {
            logger.error("Error in initial environment", e);
            System.exit(5);
        }


        try {


        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);


        } finally {
        }
    }


    static List<RecordDescription> requestInBatches(CentralWebservice doms,FetcherContext context) throws InvalidCredentialsException, MethodFailedException {
        long since = getSince(context);
        String collection = getCollection(context);
        String viewAngle = getViewAngle(context);
        String state = getState(context);
        int batchSize = getBatchSize(context);


        int start = 0;
        List<RecordDescription> records = doms.getIDsModified(since, collection, viewAngle, state,start,batchSize);
        int size = records.size();
        start += size;
        while (size == batchSize){
            List<RecordDescription> temp = doms.getIDsModified(since, collection, viewAngle, state, start, batchSize);
            size = temp.size();
            start += size;
            records.addAll(temp);
        }
        return records;
    }


    static List<RecordDescription> requestInBatches(CentralWebservice doms,FetcherContext context, int max) throws InvalidCredentialsException, MethodFailedException {
        long since = getSince(context);
        String collection = getCollection(context);
        String viewAngle = getViewAngle(context);
        String state = getState(context);
        int batchSize = getBatchSize(context);
        if (batchSize < max){
            batchSize = max;
        }


        int start = 0;
        List<RecordDescription> records = doms.getIDsModified(since, collection, viewAngle, state,start,batchSize);
        int size = records.size();
        start += size;

        while (size == batchSize && start < max){
            List<RecordDescription> temp = doms.getIDsModified(since, collection, viewAngle, state, start, batchSize);
            size = temp.size();
            start += size;
            records.addAll(temp);
        }
        if (records.size() > max){
            records = records.subList(0,max);
        }
        return records;
    }


    private static int getBatchSize(FetcherContext context) {
        return context.getBatchSize();
    }

    private static String getState(FetcherContext context) {
        return context.getFedoraState();

    }

    private static String getViewAngle(FetcherContext context) {
        return context.getViewAngle();
    }

    private static String getCollection(FetcherContext context) {
        return context.getCollection();
    }

    private static long getSince(FetcherContext context) {
        return context.getSince();
    }
}
