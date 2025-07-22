package gr.netmechanics.jmix.evrete.view.rulesetexecutionlog;

import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionLog;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rule-set-execution-logs", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_RuleSetExecutionLog.list")
@ViewDescriptor(path = "rule-set-execution-log-list-view.xml")
@LookupComponent("ruleSetExecutionLogsDataGrid")
@DialogMode(width = "64em")
public class RuleSetExecutionLogListView extends StandardListView<RuleSetExecutionLog> {
}