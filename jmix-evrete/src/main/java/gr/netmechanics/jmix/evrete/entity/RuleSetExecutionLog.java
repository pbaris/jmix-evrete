package gr.netmechanics.jmix.evrete.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

import io.jmix.core.DeletePolicy;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@SystemLevel
@JmixEntity
@Table(name = "EVRETE_RULE_SET_EXECUTION_LOG", indexes = {
    @Index(name = "IDX_EVRETE_RULE_SET_EXECUTION_LOG_RULE_SET", columnList = "RULE_SET_ID")
})
@Entity(name = "evrete_RuleSetExecutionLog")
public class RuleSetExecutionLog {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "RULE_SET_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RuleSet ruleSet;

    @Column(name = "SUCCESS")
    private Boolean success;

    @Column(name = "ERROR_MESSAGE", length = 2000)
    private String errorMessage;

    @NotNull @Column(name = "EXECUTION_START_AT", nullable = false)
    private LocalDateTime executionStartAt;

    @NotNull @Column(name = "EXECUTION_END_AT", nullable = false)
    private LocalDateTime executionEndAt;

    @SystemLevel
    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

    //TODO add generated ruleset java source

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

    @InstanceName
    @DependsOnProperties({"ruleSet", "executionStartAt"})
    public String getDisplayName() {
        return "[%s] execution at %s".formatted(ruleSet.getName(), FORMATTER.format(executionStartAt));
    }
}