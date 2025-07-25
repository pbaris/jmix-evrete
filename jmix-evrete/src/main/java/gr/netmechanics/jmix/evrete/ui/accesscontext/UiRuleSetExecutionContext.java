package gr.netmechanics.jmix.evrete.ui.accesscontext;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * @author Panos Bariamis (pbaris)
 */
public class UiRuleSetExecutionContext extends SpecificOperationAccessContext {
    public static final String NAME = "evrete.ruleSetExecution";

    public UiRuleSetExecutionContext() {
        super(NAME);
    }
}
