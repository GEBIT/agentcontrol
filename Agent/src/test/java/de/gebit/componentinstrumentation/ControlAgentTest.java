package de.gebit.componentinstrumentation;

import static de.gebit.componentinstrumentation.ControlAgent.HelperFunctions;
import static de.gebit.componentinstrumentation.ControlAgent.agentLog;
import static de.gebit.componentinstrumentation.ControlAgent.helperFunctions;
import static de.gebit.componentinstrumentation.ControlAgent.onSwingComponentAdded;
import static de.gebit.componentinstrumentation.ControlAgent.reset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;

class ControlAgentTest {

    @BeforeEach
    void setuo() {
        reset();
    }

    @Test
    void rejectObjectsOtherThanComponent() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new Object());
        assertEquals(0, helperFunctions.getComponentsByClass().size());
        assertEquals("Instance class is not java.awtComponent class java.lang.Object\n", agentLog.toString());
    }

    @Test
    void addContainer() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new Container());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getContainers().size());
        assertTrue(agentLog.toString().matches("Added java.awt.Container.*\n"), agentLog.toString());
    }

    @Test
    void addJEditorPane() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JEditorPane());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJEditorPanes().size());
        assertEquals(1, helperFunctions.getJTextComponents().size());
        assertEquals(1, helperFunctions.getJComponents().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JEditorPane.*\n"), agentLog.toString());
    }

    @Test
    void addJTextComponent() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JTextArea());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(0, helperFunctions.getJEditorPanes().size());
        assertEquals(1, helperFunctions.getJTextComponents().size());
        assertEquals(1, helperFunctions.getJComponents().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JTextArea.*\n"), agentLog.toString());
    }


    @Test
    void addJComponent() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new MyJComponent());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJComponents().size());
        assertTrue(agentLog.toString().matches("Added de.gebit.componentinstrumentation.MyJComponent.*\n"), agentLog.toString());
    }

    @Test
    void addJFrame() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JFrame());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(0, helperFunctions.getJComponents().size());
        assertEquals(1, helperFunctions.getJFrames().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JFrame.*\n"), agentLog.toString());
    }

    @Test
    void addJPanel() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JPanel());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJPanels().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JPanel.*\n"), agentLog.toString());
    }

    @Test
    void addJButton() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JButton());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJButtons().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JButton.*\n"), agentLog.toString());
    }

    @Test
    void addJLabel() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JLabel());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJLabels().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JLabel.*\n"), agentLog.toString());
    }

    @Test
    void addJTextField() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JTextField());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJTextFields().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JTextField.*\n"), agentLog.toString());
    }

    @Test
    void addJCheckBox() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JCheckBox());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJCheckBoxes().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JCheckBox.*\n"), agentLog.toString());
    }

    @Test
    void addJList() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JList<>());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJLists().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JList.*\n"), agentLog.toString());
    }

    @Test
    void addJTable() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JTable());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJTables().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JTable.*\n"), agentLog.toString());
    }

    @Test
    void addJScrollPane() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        onSwingComponentAdded(new JScrollPane());
        assertEquals(1, helperFunctions.getComponentsByClass().size());
        assertEquals(1, helperFunctions.getJScrollPanes().size());
        assertTrue(agentLog.toString().matches("Added javax.swing.JScrollPane.*\n"), agentLog.toString());
    }

    @Test
    void clickOnButton() {
        HelperFunctions helperFunctions = helperFunctions();
        helperFunctions.clearLog();
        JButton jButton = new JButton();
        jButton.setName("someName");
        onSwingComponentAdded(jButton);
        helperFunctions.clickOn(helperFunctions.findComponentByName("someName"));

        assertEquals(1, helperFunctions.getClickedComponents().size());
        assertEquals(jButton, helperFunctions.getClickedComponents().toArray()[0]);
    }


}