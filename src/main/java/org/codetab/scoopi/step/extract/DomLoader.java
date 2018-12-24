package org.codetab.scoopi.step.extract;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.plugin.script.ScriptExecutor;
import org.codetab.scoopi.pool.WebDriverPool;
import org.codetab.scoopi.pool.WebDrivers;
import org.codetab.scoopi.step.base.BaseLoader;
import org.openqa.selenium.WebDriver;

public class DomLoader extends BaseLoader {

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private WebDriverPool webDriverPool;
    @Inject
    private WebDrivers webDrivers;
    @Inject
    private ScriptExecutor scriptExecutor;

    // TODO WebDriver get local file and itest local quote files
    @Override
    public byte[] fetchDocumentObject(final String url) throws IOException {
        try {
            String taskGroup = getPayload().getJobInfo().getGroup();
            String taskName = getPayload().getJobInfo().getTask();
            String stepName = getStepName();

            Optional<List<Plugin>> plugins =
                    pluginDef.getPlugins(taskGroup, taskName, stepName);
            WebDriver webDriver = webDriverPool.borrowObject();
            try {
                webDriver.get(url);
                webDrivers.explicitlyWaitForDomReady(webDriver);
                if (plugins.isPresent()) {
                    scriptExecutor.execute(plugins.get(), webDriver);
                }
                webDrivers.explicitlyWaitForDomReady(webDriver);
                String pageSrc = webDriver.getPageSource();
                return pageSrc.getBytes();
            } finally {
                // close and quit are handled by WebDriverPool
                if (nonNull(webDriver)) {
                    webDriverPool.returnObject(webDriver);
                }
            }
        } catch (Exception e) {
            throw new StepRunException("unable to load dom", e);
        }
    }

}
