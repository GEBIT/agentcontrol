package de.gebit.componentinstrumentation;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import groovy.lang.GroovyShell;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.instrument.Instrumentation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class ControlAgent {

    public static Map<String, List<WeakReference<Component>>> awtComponentsByClass;
    public static Set<WeakReference<Component>> awtComponents;

    public static WeakHashMap<Component, Dimension> hidden;

    // Swing
    public static Set<WeakReference<Container>> containerList;
    public static Set<WeakReference<JTextComponent>> jTextComponentList;
    public static Set<WeakReference<JComponent>> jComponentList;
    public static Set<WeakReference<JEditorPane>> jEditorPaneList;
    // JFrame: JFrame is a top-level container that represents the main window of a GUI application. It provides a title bar, and minimizes, maximizes, and closes buttons.
    public static Set<WeakReference<JFrame>> jFrameList;
    // JPanel: JPanel is a container that can hold other components. It is commonly used to group related components together.
    public static Set<WeakReference<JPanel>> jPanelList;
    // JButton: JButton is a component that represents a clickable button. It is commonly used to trigger actions in a GUI application.
    public static Set<WeakReference<JButton>> jButtonList;
    // JLabel: JLabel is a component that displays text or an image. It is commonly used to provide information or to label other components.
    public static Set<WeakReference<JLabel>> jLabelList;
    // JTextField: JTextField is a component that allows the user to input text. It is commonly used to get input from the user, such as a name or an address.
    public static Set<WeakReference<JTextField>> jTextFieldList;
    // JCheckBox: JCheckBox is a component that represents a checkbox. It is commonly used to get a binary input from the user, such as whether or not to enable a feature.
    public static Set<WeakReference<JCheckBox>> jCheckBoxList;
    // JList: JList is a component that represents a list of elements. It is typically used to display a list of options from which the user can select one or more items.
    public static Set<WeakReference<JList>> jListList;
    // JTable: JTable is a component that represents a data table. It is typically used to present data in a tabular fashion, such as a list of products or a list of orders.
    public static Set<WeakReference<JTable>> jTableList;
    // JScrollPane: JScrollPane is a component that provides scrolling functionality to other components. It is commonly used to add scrolling to a panel or a table.
    public static Set<WeakReference<JScrollPane>> jScrollPaneList;


    public static Set<WeakReference<Object>> fxComponents;
    public static Set<WeakReference<Object>> clickedComponents;

    public static StringBuilder agentLog;

    // Flags
    public static boolean isDebugging;
    public static boolean isHiglighting;
    public static boolean isHidingComponentsOnClick;

    public static void reset() {
        awtComponentsByClass = new WeakHashMap<>();
        awtComponents = new LinkedHashSet<>();

        hidden = new WeakHashMap<>();

        containerList = new LinkedHashSet<>();
        jTextComponentList = new LinkedHashSet<>();
        jComponentList = new LinkedHashSet<>();
        jEditorPaneList = new LinkedHashSet<>();
        jFrameList = new LinkedHashSet<>();
        jPanelList = new LinkedHashSet<>();
        jButtonList = new LinkedHashSet<>();
        jLabelList = new LinkedHashSet<>();
        jTextFieldList = new LinkedHashSet<>();
        jCheckBoxList = new LinkedHashSet<>();
        jListList = new LinkedHashSet<>();
        jTableList = new LinkedHashSet<>();
        jScrollPaneList = new LinkedHashSet<>();

        fxComponents = new LinkedHashSet<>();
        clickedComponents = new LinkedHashSet<>();

        agentLog = new StringBuilder();

        // Flags
        isDebugging = false;
        isHiglighting = true;
        isHidingComponentsOnClick = false;
    }

    public static HelperFunctions helperFunctions() {
        return new HelperFunctions();
    }

    static {
        reset();
        agentLog.append("Static initialization called\n");
        agentLog.append("Welcome agent!\n");

        GroovyShell shell = new GroovyShell();
        shell.setVariable("ac", helperFunctions());

        // http://localhost:4567
        staticFileLocation("/public");
        get("/", (req, res) -> {
            res.redirect("/public/index.html");
            return null;
        });

        post("/execute", (req, res) -> {
            try {
                Object result = shell.evaluate(req.body());
                if (result == null) return "null";
                return result.toString();
            } catch (Exception e) {
                return e.getMessage() + "<br>" + Arrays.toString(e.getStackTrace()).replaceAll("\\n", "<br>");
            }
        });
    }

    public static class HelperFunctions {

        @Override
        public String toString() {
            return "HelperFunctions methods:\n" + getMethodsFor(this);
        }

        public String getMethodsFor(Object obj) {
            StringBuilder sb = new StringBuilder();

            // Use reflection to get all methods in the class
            Method[] methods = obj.getClass().getDeclaredMethods();

            // Append the names, arguments, and return type of all methods to the StringBuilder
            Arrays.stream(methods)
                    .filter(m -> !m.getName().startsWith("lambda$"))
                    .forEach(method -> {
                        sb.append("\n\t- ").append(method.getName()).append("(");

                        // Append method parameters
                        Parameter[] parameters = method.getParameters();
                        for (int i = 0; i < parameters.length; i++) {
                            if (i > 0) {
                                sb.append(", ");
                            }
                            sb.append(parameters[i].getType().getSimpleName()).append(" ").append(parameters[i].getName());
                        }

                        sb.append(") : ").append(method.getReturnType().getSimpleName());
                    });

            return sb.toString();
        }

        private <T> Set<T> weakToConcrete(Set<WeakReference<T>> weakSet) {
            return weakSet.stream()
                    .filter(o -> o.get() != null)
                    .map(Reference::get)
                    .collect(Collectors.toSet());
        }

        public Set<JEditorPane> getJEditorPanes() {
            return weakToConcrete(jEditorPaneList);
        }

        public Set<JTextComponent> getJTextComponents() {
            return weakToConcrete(jTextComponentList);
        }

        public Set<Container> getContainers() {
            return weakToConcrete(containerList);
        }

        public Set<JComponent> getJComponents() {
            return weakToConcrete(jComponentList);
        }

        public Set<JFrame> getJFrames() {
            return weakToConcrete(jFrameList);
        }

        public Set<JPanel> getJPanels() {
            return weakToConcrete(jPanelList);
        }

        public Set<JButton> getJButtons() {
            return weakToConcrete(jButtonList);
        }

        public Set<JLabel> getJLabels() {
            return weakToConcrete(jLabelList);
        }

        public Set<JTextField> getJTextFields() {
            return weakToConcrete(jTextFieldList);
        }

        public Set<JCheckBox> getJCheckBoxes() {
            return weakToConcrete(jCheckBoxList);
        }

        public Set<JList> getJLists() {
            return weakToConcrete(jListList);
        }

        public Set<JTable> getJTables() {
            return weakToConcrete(jTableList);
        }

        public Set<JScrollPane> getJScrollPanes() {
            return weakToConcrete(jScrollPaneList);
        }

        public Set<?> getAWTComponents() {
            return weakToConcrete(awtComponents);
        }

        public Set<?> getFxComponents() {
            return weakToConcrete(fxComponents);
        }

        public String getLog() {
            return agentLog.toString();
        }

        public String getLogTail() {
            int tailSize = 5000;
            if (agentLog.length() < tailSize) {
                return agentLog.toString();
            }
            return agentLog.substring(agentLog.length() - tailSize);
        }

        public Set<Object> getClickedComponents() {
            return weakToConcrete(clickedComponents);
        }

        public Object findFirst(Collection<?> anyCollection, String searchTerm) {
            return anyCollection.stream().filter(o -> String.valueOf(o.toString()).contains(searchTerm)).findFirst().orElseThrow();
        }

        public Component findComponentByName(String name) {
            return awtComponents.stream()
                    .filter(Objects::nonNull)
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .filter(o -> o.getName().equals(name))
                    .findFirst().orElseThrow();
        }

        public Object[] findAll(Collection<?> anyCollection, String searchTerm) {
            return anyCollection.stream().filter(o -> String.valueOf(o.toString()).contains(searchTerm)).toArray();
        }

        public void clickOn(Component component) {
            try {
                // Calculate the center point of the component to click
                int x = component.getWidth() / 2;
                int y = component.getHeight() / 2;

                // Create a MouseEvent for a click at the center of the component
                MouseEvent pressEvent = new MouseEvent(component, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                        0, x, y, 1, false, MouseEvent.BUTTON1);
                MouseEvent releaseEvent = new MouseEvent(component, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
                        0, x, y, 1, false, MouseEvent.BUTTON1);
                MouseEvent clickEvent = new MouseEvent(component, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                        0, x, y, 1, false, MouseEvent.BUTTON1);

                // Dispatch the events to the component
                component.dispatchEvent(pressEvent);
                component.dispatchEvent(releaseEvent);
                component.dispatchEvent(clickEvent);

            } catch (Exception e) {
                agentLog.append("ERROR " + e.getMessage() + "\n");
            }
        }

        public Map<String, List<Component>> getComponentsByClass() {
            LinkedHashMap<String, List<Component>> objectObjectLinkedHashMap = new LinkedHashMap<>();
            awtComponentsByClass.forEach((className, listOfWeakComponents) -> {
                List<Component> components = listOfWeakComponents.stream()
                        .filter(o -> o.get() != null)
                        .map(Reference::get).collect(Collectors.toList());
                objectObjectLinkedHashMap.put(className, components);
            });
            return objectObjectLinkedHashMap;
        }

        public void setDebug(boolean shouldDebug) {
            isDebugging = shouldDebug;
        }

        public static boolean isDebugEnabled() {
            return isDebugging;
        }

        public void setHighlight(boolean shouldHighlight) {
            isHiglighting = shouldHighlight;
        }

        public static boolean isHiglighting() {
            return isHiglighting;
        }

        public void setHideComponentsOnClick(boolean shouldHideComponentsOnClick) {
            isHidingComponentsOnClick = shouldHideComponentsOnClick;
        }

        public static boolean isHidingComponentsOnClick() {
            return isHidingComponentsOnClick;
        }

        public void unhideAllComponents() {
            hidden.forEach(Component::setSize);
            hidden.clear();
        }

        public void clearLog() {
            agentLog = new StringBuilder();
        }
    }


    public static void onSwingComponentAdded(Object obj) {
        if (!(obj instanceof Component)) {
            agentLog.append("Instance class is not java.awtComponent ").append(obj.getClass()).append("\n");
            return;
        }
        Component c = (Component) obj;
        WeakReference<Component> componentWeakReference = new WeakReference<>(c);

        agentLog.append("Added ").append(c).append("\n");

        String className = c.getClass().getName();
        List<WeakReference<Component>> components = awtComponentsByClass.get(className);
        if (components == null) components = new ArrayList<>();
        components.add(componentWeakReference);
        awtComponentsByClass.put(className, components);
        awtComponents.add(componentWeakReference);

        if (c instanceof Container) {
            Container component = (Container) c;
            containerList.add(new WeakReference<>(component));
        }
        if (c instanceof JEditorPane) {
            JEditorPane editorPane = (JEditorPane) c;
            jEditorPaneList.add(new WeakReference<>(editorPane));
        }
        if (c instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) c;
            jTextComponentList.add(new WeakReference<>(textComponent));
        }
        if (c instanceof JComponent) {
            JComponent component = (JComponent) c;
            jComponentList.add(new WeakReference<>(component));
        }
        if (c instanceof JFrame) {
            JFrame frame = (JFrame) c;
            jFrameList.add(new WeakReference<>(frame));
        }
        if (c instanceof JPanel) {
            JPanel panel = (JPanel) c;
            jPanelList.add(new WeakReference<>(panel));
        }
        if (c instanceof JButton) {
            JButton button = (JButton) c;
            jButtonList.add(new WeakReference<>(button));
        }
        if (c instanceof JLabel) {
            final JLabel label = (JLabel) c;
            jLabelList.add(new WeakReference<>(label));
        }
        if (c instanceof JTextField) {
            JTextField textField = (JTextField) c;
            jTextFieldList.add(new WeakReference<>(textField));
        }
        if (c instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) c;
            jCheckBoxList.add(new WeakReference<>(checkBox));
        }
        if (c instanceof JList) {
            JList list = (JList) c;
            jListList.add(new WeakReference<>(list));
        }
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            jTableList.add(new WeakReference<>(table));
        }
        if (c instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) c;
            jScrollPaneList.add(new WeakReference<>(scrollPane));
        }

        c.addMouseListener(new MouseListener() {
            Color realColor;

            @Override
            public void mouseClicked(MouseEvent e) {
                addLogEntry(c + " was clicked");
            }

            @Override
            public void mousePressed(MouseEvent e) {

                if (isHidingComponentsOnClick) {
                    Component component = e.getComponent();
                    Dimension size = component.getSize();
                    component.setSize(0, 0); // When component is in front of another, we hide them
                    hidden.put(component, size);
                    // useful on some screens where a component above all may be capturing clicks
                }

                clickedComponents.add(new WeakReference<>(c));
                agentLog.append("Pressed " + c + " at " + e.getPoint() + "\n");
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                realColor = component.getBackground();

                if (!isHiglighting) return;

                component.setBackground(Color.BLUE);
                if (component instanceof JButton) {
                    JButton b = (JButton) component;
                    System.out.print("text: " + b.getText() + " ");
                }
                if (component instanceof JLabel) {
                    JLabel l = (JLabel) component;
                    System.out.print("text: " + l.getText() + " ");
                }
                if (component instanceof JLabel) {
                    JLabel l = (JLabel) component;
                    System.out.print("text: " + l.getText() + " ");
                }
                System.out.print(
                        "name: " + component.getName() +
                                " class: " + component.getClass() +
                                " position: (" + component.getX() + "," + component.getY() + ")"
                );

                System.out.println();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                component.setBackground(realColor);
            }
        });

    }

    public static void onFxComponentAdded(Object fx) {
        agentLog.append("Added FX ").append(fx.toString()).append("\n");
        fxComponents.add(new WeakReference<>(fx));
    }

    public static void addLogEntry(String logEntry) {
        agentLog.append(logEntry).append("\n");
    }

    public static void premain(
            String argumentParameter,
            Instrumentation instrumentation
    ) {
        System.out.println("Agent Loaded");
        agentLog.append("Premain called.");
        try {
            AgentBuilder.Ignored withAllCals = new AgentBuilder.Default().ignore(none());
            withAllCals
                    .type(nameContains("java.awt.Component"))
                    .transform((
                            aBuilder,
                            aTypeDescription,
                            aClassLoader,
                            aJavaModule,
                            aProtectionDomain
                    ) -> {
                        try {
                            agentLog.append("SWING");
                            return aBuilder.visit(Advice.to(SpyAdviceSwing.class)
                                    .on(isConstructor()));
                        } catch (Exception e) {
                            agentLog.append(e);
                            System.out.println("Instrumentation exception " + e);
                            return aBuilder;
                        }
                    })
                    .type(nameContains("com.sun.javafx.scene.control"))
                    .transform((
                            aBuilder,
                            aTypeDescription,
                            aClassLoader,
                            aJavaModule,
                            aProtectionDomain
                    ) -> {
                        try {
                            agentLog.append("FX");
                            return aBuilder.visit(Advice.to(SpyAdviceFx.class).on(isMethod().and(nameContains("initHelper"))));
                        } catch (Exception e) {
                            agentLog.append(e);
                            System.out.println("Instrumentation exception " + e);
                            return aBuilder;
                        }
                    })
                    .installOn(instrumentation);
        } catch (Exception e) {
            agentLog.append(e);
            System.out.println("General exception " + e);
        }
        System.out.println("Finished loading agent");
    }
}