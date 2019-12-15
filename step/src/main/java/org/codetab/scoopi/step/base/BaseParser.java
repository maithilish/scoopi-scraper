package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.LINE;
import static org.codetab.scoopi.util.Util.spaceit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.scoopi.defs.IDataDefDef;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.InvalidDefException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataComponent;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.helper.DataHelper;
import org.codetab.scoopi.persistence.DataPersistence;
import org.codetab.scoopi.step.Step;
import org.codetab.scoopi.step.parse.IValueParser;
import org.codetab.scoopi.step.parse.Indexer;
import org.codetab.scoopi.step.parse.IndexerFactory;
import org.codetab.scoopi.step.parse.ValueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;

public abstract class BaseParser extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseParser.class);

    @Inject
    private ValueProcessor valueProcessor;
    @Inject
    private DataPersistence dataPersistence;
    @Inject
    private DataFactory dataFactory;
    @Inject
    private IDataDefDef dataDefDef;
    @Inject
    private DataHelper dataHelper;
    @Inject
    private StopWatch timer;
    @Inject
    private IndexerFactory indexerFactory;

    private Data data;
    protected Document document;

    /**
     * set by subclass
     */
    private IValueParser valueParser;

    private boolean parseData;

    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        timer.start();

        Object pData = getPayload().getData();
        if (pData instanceof Document) {
            document = (Document) pData;
        } else {
            String message = spaceit("payload data type is not Document but",
                    pData.getClass().getName());
            throw new StepRunException(message);
        }

        // TODO move this to load as loadPage()
        return postInitialize();
    }

    protected abstract boolean postInitialize();

    @Override
    public boolean load() {
        parseData = true;
        try {
            String dataDefName = getJobInfo().getDataDef();
            Long dataDefId = dataDefDef.getDataDefId(dataDefName);
            Long documentId = document.getId();
            if (nonNull(documentId) && nonNull(dataDefId)) {
                data = dataPersistence.loadData(dataDefId, documentId);
                if (nonNull(data)) {
                    parseData = false;
                }
            }
            return true;
        } catch (DataDefNotFoundException e) {
            String message = "unable to get datadef id";
            throw new StepRunException(message, e);
        }
    }

    @Override
    public boolean store() {
        boolean persist = persist();
        if (persist && parseData) {
            if (dataPersistence.storeData(data)) {
                data = dataPersistence.loadData(data.getId());
                setOutput(data);
                LOGGER.debug(marker, getLabeled("data stored"));
            }
        }
        if (!persist) {
            LOGGER.debug(marker, getLabeled("persist false, data not stored"));
        }
        return true;
    }

    @Override
    public boolean process() {
        Counter dataParseCounter =
                metricsHelper.getCounter(this, "data", "parse");
        Counter dataReuseCounter =
                metricsHelper.getCounter(this, "data", "reuse");

        if (parseData) {
            try {
                LOGGER.debug(marker, "{}", getLabeled("parse data"));
                String dataDefName = getJobInfo().getDataDef();
                data = dataFactory.createData(dataDefName, document.getId(),
                        getJobInfo().getLabel(), configs.getRunDateTime());

                dataHelper.addPageTag(data);
                dataHelper.addItemTag(data);
                dataHelper.addAxisTags(data, null);

                parse();
                setConsistent(true);
                dataParseCounter.inc();
            } catch (IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | DataDefNotFoundException
                    | ScriptException | InvalidDefException e) {
                String message = "unable to parse data";
                throw new StepRunException(message, e);
            }
        } else {
            setConsistent(true);
            dataReuseCounter.inc();
            LOGGER.debug(marker, "{}", getLabeled("data exists, reuse"));
        }

        setOutput(data);

        timer.stop();
        LOGGER.trace(marker, "parse time: {}", timer.toString());

        return true;
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

        LOGGER.trace(marker, "-- data after parse --{}{}{}", LINE, getLabel(),
                LINE, data);
    }

    private boolean persist() {
        // TODO code and move it to yaml
        // write itest and verify Ex-12
        String taskGroup = getJobInfo().getGroup();
        String taskName = getJobInfo().getTask();
        boolean persistData = true;
        try {
            persistData = Boolean.valueOf(taskDef.getFieldValue(taskGroup,
                    taskName, "persist", "data"));
        } catch (DefNotFoundException e) {
        }
        Optional<Boolean> taskLevelPersistence =
                Optional.ofNullable(persistData);
        return dataPersistence.persist(taskLevelPersistence);
    }
}
