package gr.netmechanics.jmix.evrete.data;

import gr.netmechanics.jmix.evrete.entity.RuleSet;
import org.evrete.api.RuleSession;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface RuleSetDataProvider {

    Iterable<?> getData(final RuleSet ruleSet, final RuleSession<?> session);

    default boolean isApplicable(final RuleSet ruleSet, final RuleSession<?> session) {
        return true;
    }
}
