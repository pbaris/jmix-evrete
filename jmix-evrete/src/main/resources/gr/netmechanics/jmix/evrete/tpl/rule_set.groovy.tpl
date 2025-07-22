package evrete.${RULE_SET_PACKAGE};

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
${RS_HELPER.writeImports(RULE_SET)}

@RuleSet(value = "${RULE_SET.name ?: 'Undefined'}", defaultSort = RuleSet.Sort.${RULE_SET.defaultSort ?: 'BY_NAME'})
public class ${RULE_SET_CLASS_NAME} {
<% RULES.eachWithIndex { rule, index -> %>
	@Rule(value = "${rule.name ?: 'Undefined'}", salience = ${rule.priority ?: 0})
	${RS_HELPER.writeWhere(rule)}
	public void rule${index + 1}(${RS_HELPER.writeParameters(rule)}) {
		${RS_HELPER.writeMethodBody(rule)}
	}

<% } %>
}
