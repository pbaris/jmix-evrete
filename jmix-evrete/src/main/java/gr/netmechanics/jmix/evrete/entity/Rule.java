package gr.netmechanics.jmix.evrete.entity;

import java.util.Date;
import java.util.UUID;

import gr.netmechanics.jmix.evrete.entity.trait.HasActive;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter
@JmixEntity
@Table(name = "EVRETE_RULE", indexes = {
    @Index(name = "IDX_EVRETE_RULE_RULE_SET", columnList = "RULE_SET_ID")
})
@Entity(name = "evrete_Rule")
public class Rule implements HasActive {
    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @NotNull
    @NotBlank
    @InstanceName
    @Column(name = "NAME", nullable = false)
    private String name;

    @NotNull
    @NumberFormat(pattern = "0")
    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "ACTIVE")
    private Boolean active;

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "RULE_SET_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RuleSet ruleSet;

    @Column(name = "RULE_CONDITION")
    private RuleCondition ruleCondition;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private Date lastModifiedDate;
}