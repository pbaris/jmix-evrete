package gr.netmechanics.jmix.evrete.action;

import io.jmix.flowui.Notifications;
import lombok.RequiredArgsConstructor;
import org.evrete.api.RhsContext;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_NotificationRuleAction")
public class NotificationRuleAction extends RuleAction {

    private final Notifications notifications;

    @Override
    public void execute(final RhsContext ctx) {
        notifications.create("test rule action")
            .show();
    }

    @Override
    public String getHelpText() {
        return "Notifies the user about the execution of the rule.";
    }
}
