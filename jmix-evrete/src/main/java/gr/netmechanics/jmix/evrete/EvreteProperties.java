package gr.netmechanics.jmix.evrete;

import gr.netmechanics.jmix.evrete.facts.DefaultFactsProvider;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@ConfigurationProperties(prefix = "jmix.evrete")
public class EvreteProperties {
    public enum Mode { NORMAL, ANNOTATED }

    /**
     * Defines the selection mode for entities used in rule property conditions.
     */
    private final Mode entitySelectionMode;

    /**
     * Forces the {@link DefaultFactsProvider} to be inapplicable, regardless of the rest of the configuration.
     */
    private final boolean defaultFactsProviderForceInapplicable;

    /**
     * Forces {@link DefaultFactsProvider} to be applicable when {@code entitySelectionMode} is {@code ANNOTATED}.
     */
    private final boolean defaultFactsProviderForceAnnotatedApplicable;

    /**
     * Forces {@link DefaultFactsProvider} to be inapplicable when {@code entitySelectionMode} is {@code NORMAL}.
     */
    private final boolean defaultFactsProviderForceNormalInapplicable;

    public EvreteProperties(@DefaultValue("normal") final Mode entitySelectionMode,
                            @DefaultValue("false") final boolean defaultFactsProviderForceInapplicable,
                            @DefaultValue("false") final boolean defaultFactsProviderForceAnnotatedApplicable,
                            @DefaultValue("false") final boolean defaultFactsProviderForceNormalInapplicable) {

        this.entitySelectionMode = entitySelectionMode;
        this.defaultFactsProviderForceInapplicable = defaultFactsProviderForceInapplicable;
        this.defaultFactsProviderForceAnnotatedApplicable = defaultFactsProviderForceAnnotatedApplicable;
        this.defaultFactsProviderForceNormalInapplicable = defaultFactsProviderForceNormalInapplicable;
    }
}
