package evrete.${RULE_SET_PACKAGE};

import org.evrete.api.RhsContext;
import org.evrete.api.events.SessionFireEvent;
import org.evrete.dsl.annotation.EventSubscription;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
${RSG_TOOLS.writeImports(RULE_SET)}

@RuleSet(value = "${RSG_TOOLS.writeName(RULE_SET.name)}", defaultSort = RuleSet.Sort.${RULE_SET.defaultSort ?: 'BY_NAME'})
public class ${RULE_SET_CLASS_NAME} {
	${RSG_TOOLS.writeClassProperties(RULE_SET)}

	@EventSubscription
	public void sessionFireObserver(SessionFireEvent event) {
		${RSG_TOOLS.loadClassProperties(RULE_SET)}
	}

<% RULES.eachWithIndex { rule, index -> %>
	@Rule(value = "${RSG_TOOLS.writeName(rule.name)}", salience = ${(rule.priority ?: 0) * -1})
	${RSG_TOOLS.writeWhere(rule)}
	public void rule${index + 1}(${RSG_TOOLS.writeParameters(rule)}) {
		${RSG_TOOLS.writeMethodBody(rule)}
	}

<% } %>
}
