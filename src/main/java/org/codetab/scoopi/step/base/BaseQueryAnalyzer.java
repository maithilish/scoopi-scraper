package org.codetab.scoopi.step.base;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.helper.AnalyzerConsole;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.step.Step;

public abstract class BaseQueryAnalyzer extends Step {

    protected Document document;

    @Inject
    private AnalyzerConsole analyzerConsole;

    @Override
    public boolean initialize() {
        validState(nonNull(getPayload()), "payload is null");
        validState(nonNull(getPayload().getData()), "payload data is null");

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

    @Override
    public boolean process() {

        while (true) {
            String input = analyzerConsole.getInput();
            if (StringUtils.isBlank(input)) {
                break;
            }
            switch (input) {
            case "1":
                String pageSource = getPageSource();
                analyzerConsole.showPageSource(pageSource);
                break;
            case "2":
                pageSource = getPageSource();
                analyzerConsole.writePageSource(pageSource);
                break;
            default:
                List<String> elements = getQueryElements(input);
                analyzerConsole.showElements(elements);
                break;
            }
        }
        setOutput("dummy");
        setConsistent(true);
        return true;
    }

    protected abstract boolean postInitialize();

    protected abstract List<String> getQueryElements(String query);

    protected abstract String getPageSource();

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean store() {
        return true;
    }

}
