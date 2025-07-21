package gr.netmechanics.jmix.evrete.view.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.vaadin.flow.component.AbstractField;
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
import org.apache.commons.collections4.CollectionUtils;
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

    @Autowired private Fragments fragments;
    @Autowired private DataManager dataManager;
    @Autowired private ViewValidation viewValidation;
    @Autowired private ObjectProvider<RuleAction> actionProvider;

    private final Map<UUID, RulePropertyCondition> propertyConditions = new HashMap<>();
    private RuleActionDefinition actionDefinition;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        actionEditor.setMode(CodeEditorMode.JAVA);
        actionEditor.setFontSize("0.8rem");

        actionSelector.setItems(actionProvider.stream().toList());
    }

    @Subscribe(id = "ruleDc", target = Target.DATA_CONTAINER)
    public void onRuleDcItemChange(final InstanceContainer.ItemChangeEvent<Rule> event) {
        Rule rule = event.getItem();
        if (rule == null) {
            return;
        }

        actionDefinition = dataManager.create(RuleActionDefinition.class);
        initRulePropertyConditions();
        initRuleAction();
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
    public void onActionSelectorComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixSelect<RuleAction>, RuleAction> event) {
        String beanClass = Optional.ofNullable(event.getValue())
            .map(bc -> bc.getClass().getCanonicalName()).orElse(null);
        actionDefinition.setBeanClass(beanClass);

        adjustRuleMetadata();
    }

    @Subscribe("actionEditor")
    public void onActionEditorComponentValueChange(final AbstractField.ComponentValueChangeEvent<CodeEditor, String> event) {
        actionDefinition.setCode(event.getValue());
        adjustRuleMetadata();
    }

    public void onValidation(final ValidationEvent event) {
        UiComponentUtils.getOwnComponents(propertyConditionsContainer).forEach(cmp -> {
            RulePropertyConditionFragment fragment = (RulePropertyConditionFragment) cmp;
            ValidationErrors validationErrors = viewValidation.validateUiComponents(fragment.getContent());
            if (!validationErrors.isEmpty()) {
                event.addErrors(validationErrors);
            }
        });
    }

    private void adjustRuleMetadata() {
        RuleActionDefinition ruleAD;
        if (ruleDc.getItem().getRuleMetadata() != null && (ruleAD = ruleDc.getItem().getRuleMetadata().getAction()) != null) {
            actionDefinition.setId(ruleAD.getId());
        }

        var ruleMetadata = new RuleMetadata();
        ruleMetadata.setAction(actionDefinition);
        ruleMetadata.setPropertyConditions(new ArrayList<>(propertyConditions.values()));
        ruleDc.getItem().setRuleMetadata(ruleMetadata);
    }

    private void initRulePropertyConditions() {
        propertyConditions.clear();
        propertyConditionsContainer.removeAll();

        RuleMetadata ruleMetadata = ruleDc.getItem().getRuleMetadata();
        if (ruleMetadata != null && CollectionUtils.isNotEmpty(ruleMetadata.getPropertyConditions())) {
            ruleMetadata.getPropertyConditions().forEach(this::buildRulePropertyConditionRow);
        }
        displayRulePropertyConditionFragmentLabels();
    }

    private void buildRulePropertyConditionRow(final RulePropertyCondition rpc) {
        propertyConditions.put(rpc.getId(), rpc);
        adjustRuleMetadata();

        RulePropertyConditionFragment fragment = fragments.create(this, RulePropertyConditionFragment.class);
        fragment.setItem(rpc);
        fragment.setRemoveDelegate(this::rulePropertyConditionRemoveDelegate);
        fragment.setItemChangeDelegate(this::rulePropertyConditionItemChangeDelegate);
        propertyConditionsContainer.add(fragment);
    }

    private void rulePropertyConditionRemoveDelegate(final RulePropertyConditionFragment fragment, final RulePropertyCondition rpc) {
        propertyConditionsContainer.remove(fragment);
        propertyConditions.remove(rpc.getId());
        adjustRuleMetadata();
        displayRulePropertyConditionFragmentLabels();
    }

    private void rulePropertyConditionItemChangeDelegate(final RulePropertyCondition rpc) {
        propertyConditions.put(rpc.getId(), rpc);
        adjustRuleMetadata();
    }

    private void displayRulePropertyConditionFragmentLabels() {
        if (propertyConditionsContainer.getComponentCount() > 0) {
            ((RulePropertyConditionFragment) propertyConditionsContainer.getComponentAt(0)).displayLabels();
        }
    }

    private void initRuleAction() {
        var ruleAD = Optional.ofNullable(ruleDc.getItem().getRuleMetadata())
            .map(RuleMetadata::getAction)
            .orElse(null);

        if (ruleAD == null) {
            actionSelector.clear();
            actionEditor.clear();
            return;
        }

        actionDefinition.setBeanClass(ruleAD.getBeanClass());
        actionDefinition.setCode(ruleAD.getCode());

        actionProvider.stream()
            .filter(action -> action.getClass().getCanonicalName().equals(ruleAD.getBeanClass()))
            .findAny()
            .ifPresent(action -> actionSelector.setValue(action));

        actionEditor.setValue(ruleAD.getCode());
    }
}