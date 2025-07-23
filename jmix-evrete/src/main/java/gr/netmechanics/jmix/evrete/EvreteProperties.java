package gr.netmechanics.jmix.evrete;

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

    private final Mode entitiesSelectionMode;

    public EvreteProperties(@DefaultValue("normal") final Mode entitiesSelectionMode) {
        this.entitiesSelectionMode = entitiesSelectionMode;
    }
}
