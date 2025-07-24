package gr.netmechanics.jmix.evrete.facts;

import java.util.HashSet;
import java.util.Set;

import gr.netmechanics.jmix.evrete.EvreteProperties;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.util.RuleSetGeneratorTools;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.evrete.api.RuleSession;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_DefaultFactsProvider")
public class DefaultFactsProvider implements FactsProvider {

    private final DataManager dataManager;
    private final Metadata metadata;
    private final MetadataTools metadataTools;
    private final RuleSetGeneratorTools generatorTools;
    private final EvreteProperties properties;

    @Override
    public Iterable<?> getFacts(final RuleSet ruleSet, final RuleSession<?> session) {
        Set<Object> facts = new HashSet<>();

        generatorTools.getRules(ruleSet)
            .filter(rule -> CollectionUtils.isNotEmpty(rule.getRuleMetadata().getPropertyConditions()))
            .flatMap(rule -> rule.getRuleMetadata().getPropertyConditions().stream())
            .map(rpc -> metadata.getClass(rpc.getEntityMetaClass()).getJavaClass())
            .distinct()
            .filter(metadataTools::isJpaEntity)
            .forEach(clazz -> facts.addAll(dataManager.load(clazz).all().list()));

        return facts;
    }

    /**
     * Determines whether this default facts provider is applicable based on the configured entity selection mode.
     *
     * <p>The applicability is determined as follows:</p>
     * <ul>
     *   <li>If the {@code entitySelectionMode} is {@code NORMAL}, the provider is applicable
     *       unless {@code defaultFactsProviderForceNormalInapplicable} is set to {@code true}.</li>
     *   <li>If the {@code entitySelectionMode} is {@code ANNOTATED}, the provider is applicable
     *       only if {@code defaultFactsProviderForceAnnotatedApplicable} is set to {@code true}.</li>
     * </ul>
     *
     * @param ruleSet the active rule set (unused in this implementation)
     * @param session the current rule session (unused in this implementation)
     * @return {@code true} if this provider is applicable, according to the configuration; {@code false} otherwise
     */
    @Override
    public boolean isApplicable(final RuleSet ruleSet, final RuleSession<?> session) {
        return switch (properties.getEntitySelectionMode()) {
            case NORMAL -> !properties.isDefaultFactsProviderForceNormalInapplicable();
            case ANNOTATED -> properties.isDefaultFactsProviderForceAnnotatedApplicable();
        };
    }
}
