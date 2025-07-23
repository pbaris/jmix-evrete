package gr.netmechanics.jmix.evrete.view.rule;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import gr.netmechanics.jmix.evrete.action.RuleAction;
import gr.netmechanics.jmix.evrete.entity.Rule;
import gr.netmechanics.jmix.evrete.entity.RuleActionDefinition;
import gr.netmechanics.jmix.evrete.entity.RuleMetadata;
import gr.netmechanics.jmix.evrete.entity.RulePropertyCondition;
import io.jmix.core.DataManager;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardDetailView.ValidationEvent;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewValidation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Panos Bariamis (pbaris)
 */
@FragmentDescriptor("rule-detail-fragment.xml")
public class RuleDetailFragment extends Fragment<VerticalLayout> {
    @ViewComponent private InstanceContainer<Rule> ruleDc;
    @ViewComponent private VerticalLayout propertyConditionsContainer;
    @ViewComponent private CodeEditor actionEditor;
    @ViewComponent private JmixSelect<RuleAction> actionSelector;
    @ViewComponent private VerticalLayout actionContainer;

    @Autowired private Fragments fragments;
    @Autowired private DataManager dataManager;
    @Autowired private ViewValidation viewValidation;
    @Autowired private ObjectProvider<RuleAction> actionProvider;

    private RuleMetadata ruleMetadataToEdit;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        actionEditor.setMode(CodeEditorMode.JAVA);
        actionEditor.setFontSize("0.8rem");

        actionSelector.setItems(actionProvider.stream().toList());
    }

    @Subscribe(id = "ruleDc", target = Target.DATA_CONTAINER)
    public void onRuleDcItemChange(final InstanceContainer.ItemChangeEvent<Rule> event) {
        if (event.getItem() == null) {
            if (event.getPrevItem() != null) {
                notifyRuleMetadataChanged(event.getPrevItem());
            }
            ruleMetadataToEdit = null;
            propertyConditionsContainer.removeAll();
            actionSelector.clear();
            actionEditor.clear();
            return;
        }

        ruleMetadataToEdit = Optional.ofNullable(ruleDc.getItem().getRuleMetadata()).orElseGet(RuleMetadata::new);

        if (ruleMetadataToEdit.getAction() == null) {
            ruleMetadataToEdit.setAction(dataManager.create(RuleActionDefinition.class));
        }

        if (ruleMetadataToEdit.getPropertyConditions() == null) {
            ruleMetadataToEdit.setPropertyConditions(new ArrayList<>());
        }

        bindRulePropertyConditions();
        bindRuleAction();
    }

    @Supply(to = "actionSelector", subject = "renderer")
    private ComponentRenderer<Component, RuleAction> actionSelectorRenderer() {
        return new ComponentRenderer<>(action -> {
            Div div = new Div();
            div.add(new Div(action.getDisplayName()));

            if (StringUtils.isNotBlank(action.getHelpText())) {
                Div helperText = new Div(action.getHelpText());
                helperText.addClassNames("text-xs", "text-secondary", "mt-xs");
                div.add(helperText);
            }

            return div;
        });
    }

    @Subscribe(id = "addPropertyConditionButton", subject = "clickListener")
    public void onAddPropertyConditionButtonClick(final ClickEvent<JmixButton> event) {
        buildRulePropertyConditionRow(dataManager.create(RulePropertyCondition.class));
        displayRulePropertyConditionFragmentLabels();
    }

    @Subscribe("actionSelector")
    public void onActionSelectorComponentValueChange(final ComponentValueChangeEvent<JmixSelect<RuleAction>, RuleAction> event) {
        if (!event.isFromClient()) {
            return;
        }

        String beanClass = Optional.ofNullable(event.getValue())
            .map(bc -> bc.getClass().getCanonicalName()).orElse(null);

        ruleMetadataToEdit.getAction().setBeanClass(beanClass);
        notifyRuleMetadataChanged(ruleDc.getItem());
    }

    @Subscribe("actionEditor")
    public void onActionEditorComponentValueChange(final ComponentValueChangeEvent<CodeEditor, String> event) {
        if (!event.isFromClient()) {
            return;
        }

        ruleMetadataToEdit.getAction().setCode(event.getValue());
        notifyRuleMetadataChanged(ruleDc.getItem());
    }

    //TODO do we need it? should/can action be required
    public void onValidation(final ValidationEvent event) {
        UiComponentUtils.getOwnComponents(propertyConditionsContainer).forEach(cmp -> {
            RulePropertyConditionFragment fragment = (RulePropertyConditionFragment) cmp;
            ValidationErrors validationErrors = viewValidation.validateUiComponents(fragment.getContent());
            if (!validationErrors.isEmpty()) {
                event.addErrors(validationErrors);
            }
        });

        if (ruleMetadataToEdit != null) {
            ValidationErrors validationErrors = viewValidation.validateUiComponents(actionContainer);
            if (!validationErrors.isEmpty()) {
                event.addErrors(validationErrors);
            }
        }
    }

    private void notifyRuleMetadataChanged(final Rule item) {
        RuleMetadata newMetadata = new RuleMetadata();
        newMetadata.setAction(ruleMetadataToEdit.getAction());
        newMetadata.setPropertyConditions(ruleMetadataToEdit.getPropertyConditions().stream()
            .filter(RulePropertyCondition::isApplicable)
            .collect(Collectors.toList()));
        item.setRuleMetadata(newMetadata);
    }

    private void bindRulePropertyConditions() {
        propertyConditionsContainer.removeAll();
        ruleMetadataToEdit.getPropertyConditions().forEach(this::buildRulePropertyConditionRow);
        displayRulePropertyConditionFragmentLabels();
    }

    private void buildRulePropertyConditionRow(final RulePropertyCondition rpc) {
        if (!ruleMetadataToEdit.getPropertyConditions().contains(rpc)) {
            ruleMetadataToEdit.getPropertyConditions().add(rpc);
        }

        RulePropertyConditionFragment fragment = fragments.create(this, RulePropertyConditionFragment.class);
        fragment.setItem(rpc);
        fragment.setRemoveDelegate(this::rulePropertyConditionRemoveDelegate);
        fragment.setItemChangeDelegate(this::rulePropertyConditionItemChangeDelegate);
        propertyConditionsContainer.add(fragment);
    }

    private void rulePropertyConditionRemoveDelegate(final RulePropertyConditionFragment fragment, final RulePropertyCondition rpc) {
        propertyConditionsContainer.remove(fragment);
        ruleMetadataToEdit.getPropertyConditions().remove(rpc);
        displayRulePropertyConditionFragmentLabels();
        notifyRuleMetadataChanged(ruleDc.getItem());
    }

    private void rulePropertyConditionItemChangeDelegate(final RulePropertyCondition rpc) {
        notifyRuleMetadataChanged(ruleDc.getItem());
    }

    private void displayRulePropertyConditionFragmentLabels() {
        if (propertyConditionsContainer.getComponentCount() > 0) {
            ((RulePropertyConditionFragment) propertyConditionsContainer.getComponentAt(0)).displayLabels();
        }
    }

    private void bindRuleAction() {
        RuleActionDefinition actionDefinition = ruleMetadataToEdit.getAction();

        actionProvider.stream()
            .filter(action -> action.getClass().getCanonicalName().equals(actionDefinition.getBeanClass()))
            .findAny()
            .ifPresent(action -> actionSelector.setValue(action));

        actionEditor.setValue(actionDefinition.getCode());
    }
}