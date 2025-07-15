package gr.netmechanics.jmix.evrete.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import gr.netmechanics.jmix.evrete.entity.trait.HasActive;
import io.jmix.core.Messages;
import org.apache.commons.lang3.BooleanUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
public class ActiveRenderer<T extends HasActive> extends ComponentRenderer<Component, T> {

    public ActiveRenderer(final Messages messages) {
        super(it -> {
            boolean active = BooleanUtils.isTrue(it.getActive());

            Span wrapper = new Span();
            wrapper.getElement().getThemeList().add("badge");
            wrapper.getElement().getThemeList().add(active ? "success" : "error");

            Icon icon = active ? VaadinIcon.CHECK_CIRCLE_O.create() : VaadinIcon.CLOSE_CIRCLE_O.create();
            icon.addClassName(LumoUtility.Padding.XSMALL);
            wrapper.add(icon);

            String text = messages.getMessage(active
                ? "gr.netmechanics.jmix.evrete.ui/activeRenderer.active" : "gr.netmechanics.jmix.evrete.ui/activeRenderer.inactive");
            wrapper.add(new Span(text));

            return wrapper;
        });
    }
}
