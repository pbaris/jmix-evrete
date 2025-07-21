package gr.netmechanics.jmix.evrete.app;

import static io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import static io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation.ENDS_WITH;
import static io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation.IN_LIST;
import static io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation.NOT_CONTAINS;
import static io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation.NOT_IN_LIST;
import static io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation.STARTS_WITH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
public class RuleSetGeneratorHelper {

    private final Metadata metadata;
    private final ObjectToStringConverter objectToStringConverter;

    public String writeImports(final RuleSet ruleSet) {
        return Optional.ofNullable(ruleSet.getRules())
            .orElse(Collections.emptyList())
            .stream()
            .filter(Rule::isValidToProcess)
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

        var transformer = new RulePropertyConditionToWhereClause(metadata, objectToStringConverter);

        var conditions = propertyConditions.stream()
            .map(transformer::transform)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return "@Where(value = {\"%s\"})".formatted(String.join("\", \"", conditions));
    }

    public String writeParameters(final Rule rule) {
        List<String> parameters = new ArrayList<>();

        var propertyConditions = rule.getRuleMetadata().getPropertyConditions();

        if (!CollectionUtils.isEmpty(propertyConditions)) {
            var transformer = new RulePropertyConditionToParameter(metadata);

            propertyConditions.stream()
                .map(transformer::transform)
                .distinct()
                .collect(Collectors.toCollection(() -> parameters));
        }

        parameters.add("final RhsContext ctx");
        return String.join(", ", parameters);
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

    private static class RulePropertyConditionToWhereClause extends RuleSetMarshallerCache {

        private final ObjectToStringConverter objectToStringConverter;

        private RulePropertyConditionToWhereClause(final Metadata metadata, final ObjectToStringConverter objectToStringConverter) {
            super(metadata);
            this.objectToStringConverter = objectToStringConverter;
        }

        //TODO properly transform, take care of datatype and operators
        private String transform(final RulePropertyCondition rpc) {
            String operation = getOperation(rpc);
            if (operation == null) {
                return null;
            }


            MetaProperty metaProperty = getMetaProperty(rpc.getEntityMetaClass(), rpc.getProperty());

            String fact = ("$" + metaProperty).replaceAll("_", "");
            Object value = objectToStringConverter.convertFromString(metaProperty.getJavaType(), rpc.getValue());
            Operation rpcOperation = rpc.getOperation();

            StringBuilder sb = new StringBuilder();
            if (rpcOperation == Operation.CONTAINS || rpcOperation == NOT_CONTAINS
                || rpcOperation == IN_LIST || rpcOperation == NOT_IN_LIST
                || rpcOperation == STARTS_WITH || rpcOperation == ENDS_WITH) {

                sb.append(operation.formatted(fact, value));

            } else if (rpcOperation == Operation.IS_SET) {
                sb.append(fact).append(" ").append(operation);

            } else {
                sb.append(fact).append(" ").append(operation).append(" ");
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
                case CONTAINS, IN_LIST -> "%s.contains(\\\"%s\\\")";
                case NOT_CONTAINS, NOT_IN_LIST -> "!%s.contains(\\\"%s\\\")";
                case STARTS_WITH -> "%s.startsWith(\\\"%s\\\")";
                case ENDS_WITH -> "%s.endsWith(\\\"%s\\\")";
                case IS_SET -> BooleanUtils.isTrue(Boolean.valueOf(condition.getValue())) ? "!= null" : "== null";
//            case IN_INTERVAL -> return getInIntervalJpqlOperation(condition);
//            case IS_COLLECTION_EMPTY -> Boolean.TRUE.equals(condition.getParameterValue()) ? "is empty" : "is not empty";
//            case MEMBER_OF_COLLECTION -> "member of";
//            case NOT_MEMBER_OF_COLLECTION -> "not member of";
                default -> null;//throw new RuntimeException("Unknown PropertyCondition operation: " + condition.getOperation());
            };
        }
    }

    private static class RulePropertyConditionToParameter extends RuleSetMarshallerCache {
        private RulePropertyConditionToParameter(final Metadata metadata) {
            super(metadata);
        }

        private String transform(final RulePropertyCondition rpc) {
            MetaClass metaClass = getMetaClass(rpc.getEntityMetaClass());
            return "final %s $%s".formatted(metaClass.getJavaClass().getSimpleName(), metaClass.getName().replaceAll("_", ""));
        }
    }
}
