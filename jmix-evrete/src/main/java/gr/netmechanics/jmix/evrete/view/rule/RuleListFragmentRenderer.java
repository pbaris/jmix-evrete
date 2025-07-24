package gr.netmechanics.jmix.evrete.view.rule;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.ui.ActiveRenderer;
import io.jmix.core.Messages;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Panos Bariamis (pbaris)
 */
@FragmentDescriptor("rule-list-fragment-renderer.xml")
@RendererItemContainer("ruleDc")
public class RuleListFragmentRenderer extends FragmentRenderer<VerticalLayout, Rule> {
    @ViewComponent private Div activePlaceholder;
    @ViewComponent private Hr ruleSeparator;

    @Autowired private Messages messages;

    private ActiveRenderer<Rule> activeRenderer;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        activeRenderer = new ActiveRenderer<>(messages);
    }

    @Override
    public void setItem(@NonNull final Rule item) {
        super.setItem(item);
        activePlaceholder.add(activeRenderer.createComponent(item));

        if (!item.isApplicable()) {
            ruleSeparator.addClassName("bg-error");
        }
    }
}