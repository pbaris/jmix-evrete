package gr.netmechanics.jmix.evrete.view.rule;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import gr.netmechanics.jmix.evrete.entity.RulePropertyCondition;
import gr.netmechanics.jmix.evrete.util.JsonUtil;
import gr.netmechanics.jmix.evrete.util.ObjectToStringConverter;
import gr.netmechanics.jmix.evrete.util.RulePropertyConditionsHelper;
import io.jmix.core.Copier;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import io.jmix.flowui.fragmentrenderer.RendererItemContainer;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Panos Bariamis (pbaris)
 */
@FragmentDescriptor("rule-property-condition-fragment.xml")
@RendererItemContainer("rulePropertyConditionDc")
public class RulePropertyConditionFragment extends FragmentRenderer<HorizontalLayout, RulePropertyCondition> {

    @ViewComponent private JmixComboBox<String> entityMetaClassField;
    @ViewComponent private JmixComboBox<String> propertyField;
    @ViewComponent private JmixComboBox<Operation> operationField;
    @ViewComponent private TypedTextField<String> valueFieldPlaceHolder;
    @ViewComponent private JmixButton resetButton;

    @Autowired private Metadata metadata;
    @Autowired private Messages messages;
    @Autowired private Dialogs dialogs;
    @Autowired private SingleFilterSupport singleFilterSupport;
    @Autowired private PropertyFilterSupport propertyFilterSupport;
    @Autowired private ObjectToStringConverter objectToStringConverter;
    @Autowired private Copier copier;
    @Autowired private RulePropertyConditionsHelper conditionsHelper;

    @Setter private Consumer<RulePropertyCondition> itemChangeDelegate;
    @Setter private BiConsumer<RulePropertyConditionFragment, RulePropertyCondition> removeDelegate;

    private RulePropertyCondition itemInitial;
    private String itemInitialHash;
    private Component valueComponent;
    private boolean mustDisplayLabels;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        ComponentUtils.setItemsMap(entityMetaClassField, conditionsHelper.getEntityMetaClasses());

        operationField.setItemLabelGenerator(op -> propertyFilterSupport.getOperationText(op));
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    @Subscribe(id = "resetButton", subject = "clickListener")
    public void onResetButtonClick(final ClickEvent<JmixButton> event) {
        entityMetaClassField.setValue(itemInitial.getEntityMetaClass());
        propertyField.setValue(itemInitial.getProperty());
        operationField.setValue(itemInitial.getOperation());

        if (valueComponent instanceof HasValue hasValue) {
            MetaClass metaClass = metadata.getClass(itemInitial.getEntityMetaClass());
            Class<?> propertyJavaType = metaClass.getProperty(itemInitial.getProperty()).getJavaType();
            hasValue.setValue(objectToStringConverter.convertFromString(propertyJavaType, itemInitial.getValue()));
        }
    }

    @Subscribe(id = "removeButton", subject = "clickListener")
    public void onRemoveButtonClick(final ClickEvent<JmixButton> event) {
        Optional.ofNullable(removeDelegate).ifPresent(delegate ->
            Optional.ofNullable(getItem()).ifPresent(it -> dialogs.createOptionDialog()
                .withHeader(messages.getMessage("dialogs.Confirmation"))
                .withText(messages.getMessage("dialogs.Confirmation.Remove"))
                .withActions(
                    new DialogAction(DialogAction.Type.YES)
                        .withHandler(e -> delegate.accept(this, it))
                        .withVariant(ActionVariant.PRIMARY),
                    new DialogAction(DialogAction.Type.NO))
                .open()));
    }

    @Subscribe("entityMetaClassField")
    public void onEntityMetaClassFieldComponentValueChange(final ComponentValueChangeEvent<JmixComboBox<String>, String> event) {
        initPropertyField();
        checkItemChange();
    }

    @Subscribe("propertyField")
    public void onPropertyFieldComponentValueChange(final ComponentValueChangeEvent<JmixComboBox<Operation>, Operation> event) {
        initOperationField();
        checkItemChange();
    }

    @Subscribe("operationField")
    public void onOperationFieldComponentValueChange(final ComponentValueChangeEvent<JmixComboBox<Operation>, Operation> event) {
        initValueField();
        checkItemChange();
    }

    @Override
    public void setItem(@NonNull final RulePropertyCondition item) {
        super.setItem(item);
        itemInitial = copier.copy(item);
        itemInitialHash = JsonUtil.toJson(itemInitial).orElse("");
        checkItemChange();
    }

    public void displayLabels() {
        mustDisplayLabels = true;
        entityMetaClassField.setLabel(messages.getMessage("gr.netmechanics.jmix.evrete.entity/RulePropertyCondition.entityMetaClass"));
        propertyField.setLabel(messages.getMessage("gr.netmechanics.jmix.evrete.entity/RulePropertyCondition.property"));
        operationField.setLabel(messages.getMessage("gr.netmechanics.jmix.evrete.entity/RulePropertyCondition.operation"));

        if (valueComponent instanceof HasLabel hasLabelComponent) {
            hasLabelComponent.setLabel(messages.getMessage("gr.netmechanics.jmix.evrete.entity/RulePropertyCondition.value"));

        } else {
            valueFieldPlaceHolder.setLabel(messages.getMessage("gr.netmechanics.jmix.evrete.entity/RulePropertyCondition.value"));
        }
    }

    private void initPropertyField() {
        String entityMetaClass = entityMetaClassField.getValue();

        if (StringUtils.isBlank(entityMetaClass)) {
            propertyField.setItems(Collections.emptyList());
            propertyField.setEnabled(false);
            return;
        }

        List<String> properties = conditionsHelper.getMetaProperties(metadata.getClass(entityMetaClass)).stream()
            .map(MetadataObject::getName)
            .sorted()
            .collect(Collectors.toList());

        propertyField.setItems(properties);
        propertyField.setEnabled(true);
    }

    private void initOperationField() {
        String entityMetaClass = entityMetaClassField.getValue();
        String property = propertyField.getValue();

        if (StringUtils.isBlank(entityMetaClass) || StringUtils.isBlank(property)) {
            operationField.setItems(Collections.emptyList());
            operationField.setEnabled(false);
            return;
        }

        EnumSet<Operation> operations = propertyFilterSupport.getAvailableOperations(metadata.getClass(entityMetaClass), property);
        operationField.setItems(operations);
        operationField.setEnabled(true);
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    private void initValueField() {
        String entityMetaClass = entityMetaClassField.getValue();
        String property = propertyField.getValue();
        Operation operation = operationField.getValue();

        if (StringUtils.isBlank(entityMetaClass) || StringUtils.isBlank(property) || operation == null) {
            if (valueComponent != null) {
                Optional.ofNullable(getItem()).ifPresent(rpc -> rpc.setValue(null));
                getContent().replace(valueComponent, valueFieldPlaceHolder);
                valueComponent = null;
            }
            return;
        }

        MetaClass metaClass = metadata.getClass(entityMetaClass);

        Component oldComponent = valueComponent == null ? valueFieldPlaceHolder : valueComponent;
        valueComponent = (Component) singleFilterSupport.generateValueComponent(metaClass, property, operation);
        getContent().replace(oldComponent, valueComponent);

        valueComponent.getElement().getThemeList().add("small");
        ((HasSize) valueComponent).setWidthFull();
        ((HasEnabled) valueComponent).setEnabled(true);
        ((HasRequired) valueComponent).setRequired(true);

        if (mustDisplayLabels) {
            displayLabels();
        }

        if (valueComponent instanceof HasValue hasValue) {
            Optional.ofNullable(getItem()).ifPresent(rpc -> {
                hasValue.setValue(objectToStringConverter.convertFromString(metaClass.getProperty(property).getJavaType(), rpc.getValue()));
                hasValue.addValueChangeListener(e -> {
                    rpc.setValue(objectToStringConverter.convertToString(e.getValue()));
                    checkItemChange();
                });
            });
        }
    }

    private void checkItemChange() {
        Optional.ofNullable(getItem()).ifPresent(rpc -> {
            String hash = JsonUtil.toJson(rpc).orElse("");
            boolean itemChanged = !hash.equals(itemInitialHash);

            if (itemChanged) {
                Optional.ofNullable(itemChangeDelegate).ifPresent(delegate -> delegate.accept(rpc));
            }

            resetButton.setEnabled(itemChanged);
        });
    }
}