package org.codetab.scoopi.step.convert;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.defs.yml.DataDefDefs;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.AxisName;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Filter;
import org.codetab.scoopi.model.Member;
import org.codetab.scoopi.model.helper.FilterHelper;
import org.codetab.scoopi.step.base.BaseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFilter extends BaseConverter {

    static final Logger LOGGER = LoggerFactory.getLogger(DataFilter.class);

    @Inject
    private FilterHelper filterHelper;
    @Inject
    private DataDefDefs dataDefDefs;

    @Override
    public boolean process() {
        try {
            String dataDefName = getPayload().getJobInfo().getDataDef();
            DataDef dataDef;
            dataDef = dataDefDefs.getDataDef(dataDefName);
            Map<AxisName, List<Filter>> filterMap =
                    filterHelper.getFilterMap(dataDef);
            List<Member> filterMembers =
                    filterHelper.getFilterMembers(data.getMembers(), filterMap);
            filterHelper.filter(data.getMembers(), filterMembers);
            setOutput(data);
            setConsistent(true);
            LOGGER.info("{}", data.getMembers());
        } catch (DataDefNotFoundException e) {
            String message = "unable to apply filters";
            throw new StepRunException(message, e);
        }
        return true;
    }
}
