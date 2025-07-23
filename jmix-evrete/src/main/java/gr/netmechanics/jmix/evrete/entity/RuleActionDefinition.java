package gr.netmechanics.jmix.evrete.entity;

import java.util.UUID;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Panos Bariamis
 */
@Getter
@Setter
@SystemLevel
@JmixEntity(name = "evrete_RuleActionDefinition")
public class RuleActionDefinition {
    @JmixId
    @JmixGeneratedValue
    private UUID id;

    @InstanceName
    private String beanClass;

    private String code;
}