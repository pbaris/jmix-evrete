package gr.netmechanics.jmix.evrete.entity;

import java.util.UUID;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter
@SystemLevel
@JmixEntity(name = "evrete_RulePropertyCondition")
public class RulePropertyCondition {
    @JmixId
    @JmixGeneratedValue
    private UUID id;

    @NotNull
    @NotBlank
    @InstanceName
    @JmixProperty(mandatory = true)
    private String entityMetaClass;

    @NotNull
    @NotBlank
    @JmixProperty(mandatory = true)
    private String property;

    @NotNull
    @JmixProperty(mandatory = true)
    private Operation operation;

    @NotNull
    @NotBlank
    @JmixProperty(mandatory = true)
    private String value;
}