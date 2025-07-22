package gr.netmechanics.jmix.evrete.entity;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import gr.netmechanics.jmix.evrete.entity.trait.HasActive;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
@Table(name = "EVRETE_RULE_SET")
@Entity(name = "evrete_RuleSet")
public class RuleSet implements HasActive {
    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @InstanceName
    @NotBlank
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @NotNull
    @Column(name = "DEFAULT_SORT", nullable = false, length = 20)
    private String defaultSort;

    @Column(name = "ACTIVE")
    private Boolean active;

    @Composition
    @OrderBy("priority")
    @OneToMany(mappedBy = "ruleSet")
    private List<Rule> rules;

    @SystemLevel
    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

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

    public RuleSetSort getDefaultSort() {
        return defaultSort == null ? null : RuleSetSort.fromId(defaultSort);
    }

    public void setDefaultSort(final RuleSetSort defaultSort) {
        this.defaultSort = defaultSort == null ? null : defaultSort.getId();
    }

    public Map<String, Object> getExecutionData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", StringUtils.defaultIfBlank(name, "Undefined"));
        data.put("defaultSort", defaultSort);
        data.put("active", BooleanUtils.isTrue(active));
        if (CollectionUtils.isNotEmpty(rules)) {
            data.put("rules", rules.stream()
                .filter(Rule::isApplicable)
                .map(rule -> {
                    Map<String, Object> ruleMap = new LinkedHashMap<>();
                    ruleMap.put("name", StringUtils.defaultIfBlank(rule.getName(), "Undefined"));
                    ruleMap.put("priority", rule.getPriority() == null ? 0 : rule.getPriority());
                    ruleMap.put("active", BooleanUtils.isTrue(rule.getActive()));
                    ruleMap.put("metadata", rule.getRuleMetadata());
                    return ruleMap;
                })
                .toList());

        } else {
            data.put("rules", Collections.emptyList());
        }
        return data;
    }
}