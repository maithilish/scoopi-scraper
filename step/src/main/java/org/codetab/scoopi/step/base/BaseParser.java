package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.dashit;
import static org.codetab.scoopi.util.Util.spaceit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.helper.DataHelper;
import org.codetab.scoopi.model.helper.Fingerprints;
import org.codetab.scoopi.step.Step;
import org.codetab.scoopi.step.parse.IValueParser;
import org.codetab.scoopi.step.parse.Indexer;
import org.codetab.scoopi.step.parse.IndexerFactory;
import org.codetab.scoopi.step.parse.ValueProcessor;

import com.codahale.metrics.Counter;

public abstract class BaseParser extends Step {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private ValueProcessor valueProcessor;
    @Inject
    private DataFactory dataFactory;
    @Inject
    private IDataDefDef dataDefDef;

    @Inject
    private IDataDao dataDao;

    @Inject
    private DataHelper dataHelper;
    @Inject
    private StopWatch timer;
    @Inject
    private IndexerFactory indexerFactory;
    @Inject
    private Persists persists;

    private Data data;
    protected Document document;

    /**
     * set by subclass
     */
    private IValueParser valueParser;

    private boolean parseData = true;

    private Fingerprint dataFingerprint;

    private boolean persist;

    @Override
    public void initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        timer.start();

        Object pData = getPayload().getData();
        if (pData instanceof Document) {
            document = (Document) pData;
            // documents taken from cluster (multi task docs) are compressed,
            // local documents (single task) are not. Decompress if compressed.
            document.decompress();
        } else {
            String message = spaceit("payload data type is not Document but",
                    pData.getClass().getName());
            throw new StepRunException(message);
        }

        JobInfo jobInfo = getJobInfo();
        String dataId = dashit(jobInfo.getName(), jobInfo.getGroup(),
                jobInfo.getTask(), jobInfo.getDataDef(),
                dataDefDef.getFingerprint(jobInfo.getDataDef()).getValue());
        dataFingerprint = Fingerprints.fingerprint(dataId.getBytes());

        persist = persists.persistData(jobInfo);

        postInitialize();
    }

    protected abstract boolean postInitialize();

    @Override
    public void load() {

        if (!persist) {
            return;
        }
        /*
         * dataFingerprint is used to save and load the data. The datadef
         * fingerprint is part dataFingerprint, so new data is parsed when
         * datadef is modified. There is no way to find or delete the old saved
         * data. At present, the workaround to clear the dangling data files is
         * to delete all data files. Possible fix - move datadef fingerprint
         * from file fingerprint to metadata and match it after loading the file
         * to decide on re-parse.
         */
        try {
            try {
                data = dataDao.get(document.getLocatorId(), dataFingerprint);
            } catch (ChecksumException e) {
                LOG.error(jobMarker, getLabeled("load data {}"), e);
                dataDao.delete(document.getLocatorId(), dataFingerprint);
            }
            if (nonNull(data)) {
                parseData = false;
            } else {
                LOG.debug(jobMarker, getLabeled("no parsed data in datastore"));
            }
        } catch (DaoException e) {
            LOG.debug(jobMarker, getLabeled("load data {}"), e);
        }
    }

    @Override
    public void store() {

        if (parseData && persist) {
            try {
                dataDao.save(document.getLocatorId(), dataFingerprint, data);
                setOutput(data);
                LOG.debug(jobMarker, getLabeled("persist true, data stored"));
            } catch (DaoException e) {
                String message = "store data";
                throw new StepRunException(message, e);
            }
        } else {
            if (!persist) {
                LOG.debug(jobMarker,
                        getLabeled("persist false, data not stored"));
            }
        }
    }

    @Override
    public void process() {
        Counter dataParseCounter =
                metricsHelper.getCounter(this, "data", "parse");
        Counter dataReuseCounter =
                metricsHelper.getCounter(this, "data", "reuse");

        if (parseData) {
            try {
                LOG.debug(jobMarker, "{}", getLabeled("parse data"));
                String dataDefName = getJobInfo().getDataDef();
                data = dataFactory.createData(dataDefName, document.getId(),
                        getJobInfo().getLabel(), configs.getRunDateTime());

                dataHelper.addPageTag(data);
                dataHelper.addItemTag(data);
                dataHelper.addAxisTags(data, null);

                parse();
                dataParseCounter.inc();
            } catch (IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | DataDefNotFoundException
                    | ScriptException | InvalidDefException e) {
                String message = "unable to parse data";
                throw new StepRunException(message, e);
            }
        } else {
            dataReuseCounter.inc();
            LOG.debug(jobMarker, "{}",
                    getLabeled("parsed data found in datastore, reuse"));
        }

        setOutput(data);

        timer.stop();
        LOG.trace(jobMarker, "parse time: {}", timer.toString());
    }

    protected void setValueParser(final IValueParser valueParser) {
        this.valueParser = valueParser;
    }

    private void parse() throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            DataDefNotFoundException, ScriptException, InvalidDefException {

        valueProcessor.addScriptObject("document", document);
        valueProcessor.addScriptObject("configs", configs);

        // expanded item list
        List<DataComponent> newItems = new ArrayList<>();
        String dataDefName = getJobInfo().getDataDef();

        for (Item item : data.getItems()) {
            Indexer indexer = indexerFactory.createIndexer(dataDefName,
                    item.getItemNames());
            while (indexer.hasNext()) {
                Item newItem = item.copy();
                newItem.setParent(data);

                Map<String, Integer> indexMap = indexer.next();
                valueProcessor.setAxisValues(dataDefName, newItem, indexMap,
                        indexer, valueParser);
                newItems.add(newItem);
            }
        }

        // replace with expanded item list
        data.setItems(newItems);

        LOG.trace(jobMarker, "-- data after parse --{}{}{}", LINE, getLabel(),
                LINE, data);
    }

}
