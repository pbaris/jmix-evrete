package gr.netmechanics.jmix.evrete.view.rule;

import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.Rule;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rules/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_Rule.detail")
@ViewDescriptor(path = "rule-detail-view.xml")
@EditedEntityContainer("ruleDc")
public class RuleDetailView extends StandardDetailView<Rule> {
}