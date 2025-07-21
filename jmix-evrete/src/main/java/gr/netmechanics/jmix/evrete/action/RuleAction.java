package gr.netmechanics.jmix.evrete.action;

import org.evrete.api.RhsContext;

/**
 * @author Panos Bariamis (pbaris)
 */
public abstract class RuleAction {

    public abstract void execute(RhsContext ctx);

    public String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    public String getHelpText() {
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
