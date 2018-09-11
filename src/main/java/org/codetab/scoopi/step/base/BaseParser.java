package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.apache.commons.lang3.time.StopWatch;
import org.codetab.scoopi.defs.IDataDefDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.persistence.DataPersistence;
import org.codetab.scoopi.step.Step;
import org.codetab.scoopi.step.parse.IValueParser;
import org.codetab.scoopi.step.parse.MemberStack;
import org.codetab.scoopi.step.parse.ValueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;

public abstract class BaseParser extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(BaseParser.class);

    @Inject
    private MemberStack memberStack;
    @Inject
    private ValueProcessor valueProcessor;
    @Inject
    private DataPersistence dataPersistence;
    @Inject
    private IDataDefDefs dataDefDefs;

    private Data data;
    protected Document document;

    /**
     * set by subclass
     */
    private IValueParser valueParser;

    private StopWatch sw = StopWatch.createStarted();

    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

        Object pData = getPayload().getData();
        if (pData instanceof Document) {
            document = (Document) pData;
        } else {
            String message =
                    String.join(" ", "payload data type is not Document but",
                            pData.getClass().getName());
            throw new StepRunException(message);
        }
        // TODO move this to load as loadPage()
        return postInitialize();
    }

    protected abstract boolean postInitialize();

    @Override
    public boolean load() {
        try {
            String dataDefName = getJobInfo().getDataDef();
            Long dataDefId = dataDefDefs.getDataDef(dataDefName).getId();
            Long documentId = document.getId();
            if (nonNull(documentId) && nonNull(dataDefId)) {
                data = dataPersistence.loadData(dataDefId, documentId);
            }
            return true;
        } catch (DataDefNotFoundException e) {
            String message = "unable to get datadef id";
            throw new StepRunException(message, e);
        }
    }

    @Override
    public boolean store() {
        if (persist()) {
            if (dataPersistence.storeData(data)) {
                data = dataPersistence.loadData(data.getId());
                LOGGER.debug(getLabeled("data stored"));
            }
        } else {
            LOGGER.debug(getLabeled("persist false, data not stored"));
        }
        return true;
    }

    @Override
    public boolean process() {
        Counter dataParseCounter =
                metricsHelper.getCounter(this, "data", "parse");
        Counter dataReuseCounter =
                metricsHelper.getCounter(this, "data", "reuse");
        if (data == null) {
            LOGGER.info("{}", getLabeled("parse data"));
            String dataDefName = getJobInfo().getDataDef();
            data = dataDefDefs.getDataTemplate(dataDefName);

            try {
                parse();
                setConsistent(true);
                dataParseCounter.inc();
            } catch (IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | DataDefNotFoundException
                    | ScriptException e) {
                String message = "unable to parse data";
                throw new StepRunException(message, e);
            }
        } else {
            setConsistent(true);
            dataReuseCounter.inc();
            LOGGER.info("{}", getLabeled("data exists, reuse"));
        }

        // TODO - remove this
        sw.stop();
        LOGGER.info(data.getMembers().toString());
        LOGGER.info("parse time : ", sw);

        setOutput(data);
        return true;
    }

    protected void setValueParser(final IValueParser valueParser) {
        this.valueParser = valueParser;
    }

    private void parse()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, DataDefNotFoundException, ScriptException {

        valueProcessor.addScriptObject("document", document);
        valueProcessor.addScriptObject("configs", configService);

        memberStack.pushMembers(data.getMembers());

        List<Member> members = new ArrayList<>(); // expanded member list
        String dataDefName = getJobInfo().getDataDef();
        DataDef dataDef = dataDefDefs.getDataDef(dataDefName);

        while (!memberStack.isEmpty()) {
            Member member = memberStack.popMember();
            members.add(member);
            // collections.sort not possible as axes is a Set so implied sort
            // as value field of an axis may be referred by later axis
            valueProcessor.setAxisValues(dataDef, member, valueParser);
            memberStack.pushAdjacentMembers(dataDef, member);
        }

        data.setMembers(members); // replace with expanded member list
        String line = System.lineSeparator();
        LOGGER.trace(getMarker(), "-- data after parse --{}{}{}", line,
                getLabel(), line, data);
    }

    private boolean persist() {
        // TODO code and move it to yaml
        Optional<Boolean> taskLevelPersistenceDefined =
                Optional.ofNullable(true);
        return dataPersistence.persist(taskLevelPersistenceDefined);
    }
}
