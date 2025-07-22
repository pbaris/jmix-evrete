package gr.netmechanics.jmix.evrete.view.rulesetexecutionlog;

import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionLog;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rule-set-execution-logs/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_RuleSetExecutionLog.detail")
@ViewDescriptor(path = "rule-set-execution-log-detail-view.xml")
@EditedEntityContainer("ruleSetExecutionLogDc")
public class RuleSetExecutionLogDetailView extends StandardDetailView<RuleSetExecutionLog> {
}