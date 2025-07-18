package gr.netmechanics.jmix.evrete.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RulePropertyCondition;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.util.ObjectToStringConverter;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_RuleSetMarshallerHelper")
public class RuleSetMarshallerHelper {

    private final Metadata metadata;
    private final ObjectToStringConverter objectToStringConverter;

    public String writeImports(final RuleSet ruleSet) {
        return ruleSet.getRules().stream()
            .filter(r -> BooleanUtils.isTrue(r.getActive()))
            .flatMap(r -> r.getRuleMetadata().getPropertyConditions().stream())
            .map(rpc -> "import " + metadata.getClass(rpc.getEntityMetaClass()).getJavaClass().getCanonicalName() + ";")
            .distinct()
            .collect(Collectors.joining("\n"));
    }

    public String writeWhere(final Rule rule) {
        var propertyConditions = rule.getRuleMetadata().getPropertyConditions();
        if (CollectionUtils.isEmpty(propertyConditions)) {
            return "";
        }

        var transformer = new RulePropertyConditionTransformer(metadata, objectToStringConverter);

        var conditions = propertyConditions.stream()
            .map( transformer::transform)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return "@Where(value = {\"%s\"})".formatted(String.join("\", \"", conditions));
    }

    @RequiredArgsConstructor
    private static class RuleSetMarshallerCache {
        private final Map<String, MetaClass> metaClassCache = new HashMap<>();
        private final Map<String, MetaProperty> metaPropertiesCache = new HashMap<>();

        private final Metadata metadata;

        public MetaClass getMetaClass(final String metaClass) {
            return metaClassCache.computeIfAbsent(metaClass, metadata::getClass);
        }

        public MetaProperty getMetaProperty(final String metaClass, final String property) {
            return metaPropertiesCache.computeIfAbsent(property, getMetaClass(metaClass)::getProperty);
        }
    }

    private static class RulePropertyConditionTransformer extends RuleSetMarshallerCache {

        private final ObjectToStringConverter objectToStringConverter;

        public RulePropertyConditionTransformer(final Metadata metadata, final ObjectToStringConverter objectToStringConverter) {
            super(metadata);
            this.objectToStringConverter = objectToStringConverter;
        }

        private String transform(final RulePropertyCondition rpc) {
            String operation = getOperation(rpc);
            if (operation == null) {
                return null;
            }

            MetaProperty metaProperty = getMetaProperty(rpc.getEntityMetaClass(), rpc.getProperty());

            String fact = "$" + metaProperty;
            Object value = objectToStringConverter.convertFromString(metaProperty.getJavaType(), rpc.getValue());

            StringBuilder sb = new StringBuilder(fact);
            if (operation.startsWith(".")) {
                sb.append(operation).append("(");
                appendValue(sb, value);
                sb.append(")");

            } else {
                sb.append(" ").append(operation).append(" ");
                appendValue(sb, value);
            }

            return sb.toString();
        }

        private static void appendValue(final StringBuilder sb, final Object value) {
            if (value instanceof String) {
                sb.append("\\\"").append(value).append("\\\"");

            } else {
                sb.append(value);
            }
        }

        private static String getOperation(final RulePropertyCondition condition) {
            return switch (condition.getOperation()) {
                case EQUAL -> "==";
                case NOT_EQUAL -> "!=";
                case GREATER -> ">";
                case GREATER_OR_EQUAL -> ">=";
                case LESS -> "<";
                case LESS_OR_EQUAL -> "<=";
                case CONTAINS, NOT_CONTAINS, IN_LIST, NOT_IN_LIST -> ".contains";
                case STARTS_WITH -> ".startsWith";
                case ENDS_WITH -> ".endsWith";
                case IS_SET -> BooleanUtils.isTrue(Boolean.valueOf(condition.getValue())) ? "!= null" : "== null";
//            case IN_INTERVAL -> return getInIntervalJpqlOperation(condition);
//            case IS_COLLECTION_EMPTY -> Boolean.TRUE.equals(condition.getParameterValue()) ? "is empty" : "is not empty";
//            case MEMBER_OF_COLLECTION -> "member of";
//            case NOT_MEMBER_OF_COLLECTION -> "not member of";
                default -> null;//throw new RuntimeException("Unknown PropertyCondition operation: " + condition.getOperation());
            };
        }
    }
}
