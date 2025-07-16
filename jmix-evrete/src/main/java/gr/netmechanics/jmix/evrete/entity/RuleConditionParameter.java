package gr.netmechanics.jmix.evrete.entity;

import java.io.Serializable;
import java.util.UUID;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter
@JmixEntity(name = "evrete_RuleConditionParameter")
public class RuleConditionParameter implements Serializable {
    @JmixId
    @JmixGeneratedValue
    private UUID id;

    @NotNull
    @NotBlank
    @Pattern(regexp = "[a-z][a-zA-Z0-9_]*")
    @InstanceName
    private String name;

    @NotNull
    private Integer type;

    private String entityMetaClass;

    private String enumerationClass;

    private String defaultValue;

    private Boolean defaultDateIsCurrent;

    public ParameterType getType() {
        return type == null ? null : ParameterType.fromId(type);
    }

    public void setType(final ParameterType type) {
        this.type = type == null ? null : type.getId();
    }
}