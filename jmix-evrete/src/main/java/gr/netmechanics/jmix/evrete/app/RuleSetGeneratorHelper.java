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

import gr.netmechanics.jmix.evrete.action.RuleAction;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleActionDefinition;
import gr.netmechanics.jmix.evrete.entity.RuleMetadata;
import gr.netmechanics.jmix.evrete.entity.RulePropertyCondition;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.util.JavaNamingUtil;
import gr.netmechanics.jmix.evrete.util.ObjectToStringConverter;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
@Component("evrete_RuleSetMarshallerHelper")
public class RuleSetGeneratorHelper {

    private final Metadata metadata;
    private final ObjectToStringConverter objectToStringConverter;
    private final ObjectProvider<RuleAction> actionProvider;

    @SuppressWarnings("unused")
    public String writeImports(final RuleSet ruleSet) {
        return Optional.ofNullable(ruleSet.getRules())
            .orElse(Collections.emptyList())
            .stream()
            .filter(Rule::isValidToProcess)
            .flatMap(r -> {
                List<String> lala = new ArrayList<>();
                RuleMetadata ruleMetadata = r.getRuleMetadata();

                // Property Conditions
                if (CollectionUtils.isNotEmpty(ruleMetadata.getPropertyConditions())) {
                    ruleMetadata.getPropertyConditions().stream()
                        .map(rpc -> metadata.getClass(rpc.getEntityMetaClass()).getJavaClass().getCanonicalName())
                        .collect(Collectors.toCollection(() -> lala));
                }

                // Action
                getRuleAction(ruleMetadata).ifPresent(pair -> {
                    RuleAction action = pair.getRight();
                    if (action != null) {
                        lala.add(action.getClass().getCanonicalName());
                    }
                });

                return lala.stream();
            })
            .map(i -> "import " + i + ";")
            .distinct()
            .collect(Collectors.joining("\n"));
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public String writeParameters(final Rule rule) {
        List<String> parameters = new ArrayList<>();

        RuleMetadata ruleMetadata = rule.getRuleMetadata();

        var propertyConditions = ruleMetadata.getPropertyConditions();
        if (!CollectionUtils.isEmpty(propertyConditions)) {
            var transformer = new RulePropertyConditionToParameter(metadata);
            propertyConditions.stream()
                .map(transformer::transform)
                .distinct()
                .collect(Collectors.toCollection(() -> parameters));
        }

        getRuleAction(ruleMetadata).ifPresent(pair -> {
            RuleAction action = pair.getRight();
            if (action != null) {
                parameters.add("final %s %s".formatted(action.getClass().getSimpleName(), getRuleActionParameterName(action)));
            }
        });

        parameters.add("final RhsContext ctx");
        return String.join(", ", parameters);
    }

    @SuppressWarnings("unused")
    public String writeMethodBody(final Rule rule) {
        StringBuilder sb = new StringBuilder();

        getRuleAction(rule.getRuleMetadata()).ifPresent(pair -> {
            RuleActionDefinition actionDefinition = pair.getLeft();
            if (StringUtils.isNotBlank(actionDefinition.getCode())) {
                sb.append(actionDefinition.getCode()).append("\n");
            }

            RuleAction action = pair.getRight();
            if (action != null) {
                sb.append(getRuleActionParameterName(action)).append(".execute(ctx);");
            }
        });

        return sb.isEmpty() ? null : sb.toString();
    }

    Optional<Pair<RuleActionDefinition, RuleAction>> getRuleAction(final RuleMetadata metadata) {
        RuleActionDefinition actionDefinition = metadata.getAction();
        if (actionDefinition == null) {
            return Optional.empty();
        }

        RuleAction action = actionProvider.stream()
            .filter(a -> a.getClass().getCanonicalName().equals(actionDefinition.getBeanClass()))
            .findAny()
            .orElse(null);

        return Optional.of(Pair.of(actionDefinition, action));
    }

    String getRuleActionParameterName(final RuleAction action) {
        return "$$%s".formatted(JavaNamingUtil.getParameterName(action.getClass().getSimpleName()));
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
