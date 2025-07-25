package gr.netmechanics.jmix.evrete;

import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.notification.Notification;
import gr.netmechanics.jmix.evrete.entity.ExecutionType;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.entity.RuleSetExecutionLog;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.component.UiComponentUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_RuleSetExecutionSupport")
public class RuleSetExecutionSupport {
    private final Messages messages;
    private final Notifications notifications;
    private final Dialogs dialogs;
    private final RuleSetExecutionService executionService;

    public void execute(final RuleSet ruleSet, final ExecutionType executionType) {
        dialogs.createBackgroundTaskDialog(new RuleSetExecutionTask(ruleSet, executionType))
            .withHeader(messages.getMessage("gr.netmechanics.jmix.evrete.ui/ruleSetExecution.dialog.header"))
            .withText(messages.getMessage("gr.netmechanics.jmix.evrete.ui/ruleSetExecution.dialog.wait"))
            .open();
    }


    private class RuleSetExecutionTask extends BackgroundTask<Integer, RuleSetExecutionLog> {
        private final RuleSet ruleSet;
        private final ExecutionType executionType;

        private RuleSetExecutionTask(final RuleSet ruleSet, final ExecutionType executionType) {
            super(10, TimeUnit.MINUTES, UiComponentUtils.getCurrentView());
            this.ruleSet = ruleSet;
            this.executionType = executionType;
        }

        @NonNull
        @Override
        public RuleSetExecutionLog run(@NonNull final TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            return executionType == ExecutionType.TEST
                ? executionService.executeTest(ruleSet) : executionService.execute(ruleSet);
        }

        @Override
        public void done(final RuleSetExecutionLog executionLog) {
            boolean success = BooleanUtils.isTrue(executionLog.getSuccess());

            notifications.create(messages.getMessage("gr.netmechanics.jmix.evrete.ui/ruleSetExecution.dialog.completed"),
                    messages.getMessage("gr.netmechanics.jmix.evrete.ui/ruleSetExecution.dialog." + (success ? "success" : "error")))
                .withType(success ? Notifications.Type.SUCCESS : Notifications.Type.ERROR)
                .withPosition(Notification.Position.MIDDLE)
                .withCloseable(false)
                .withDuration(10000)
                .show();
        }
    }
}
