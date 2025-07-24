package gr.netmechanics.jmix.evrete.action;

import lombok.extern.slf4j.Slf4j;
import org.evrete.api.RhsContext;
import org.evrete.api.RuntimeRule;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@Component("evrete_LoggerRuleAction")
public class LoggerRuleAction extends RuleAction {

    @Override
    public void execute(final RhsContext ctx) {
        RuntimeRule rule = ctx.getRule();
        log.info("Rule [{}] executed", rule.getName());
    }

    @Override
    public String getHelpText() {
        return "Logs rule execution details using the logger";
    }
}
