package gr.netmechanics.jmix.evrete.view.ruleset;

import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import gr.netmechanics.jmix.evrete.entity.RuleSet;
import gr.netmechanics.jmix.evrete.ui.ActiveRenderer;
import io.jmix.core.Messages;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Panos Bariamis (pbaris)
 */
@Route(value = "rule-sets", layout = DefaultMainViewParent.class)
@ViewController(id = "evrete_RuleSet.list")
@ViewDescriptor(path = "rule-set-list-view.xml")
@LookupComponent("ruleSetsDataGrid")
@DialogMode(width = "64em")
public class RuleSetListView extends StandardListView<RuleSet> {
    @Autowired private Messages messages;

    @Supply(to = "ruleSetsDataGrid.active", subject = "renderer")
    private Renderer<RuleSet> ruleSetsDataGridActiveRenderer() {
        return new ActiveRenderer<>(messages);
    }
}