package compiler.ide;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Barra de menu da IDE
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class MenuBar extends JMenuBar {
    
    private JMenuItem newItem;
    private JMenuItem openItem;
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenuItem exitItem;
    
    private JMenuItem compileItem;
    private JMenuItem clearConsoleItem;
    
    private JMenuItem aboutItem;
    
    public MenuBar() {
        createFileMenu();
        createBuildMenu();
        createHelpMenu();
    }
    
    private void createFileMenu() {
        JMenu fileMenu = new JMenu("Arquivo");
        fileMenu.setMnemonic(KeyEvent.VK_A);
        
        newItem = new JMenuItem("Novo");
        newItem.setMnemonic(KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        
        openItem = new JMenuItem("Abrir...");
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        
        saveItem = new JMenuItem("Salvar");
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        
        saveAsItem = new JMenuItem("Salvar Como...");
        saveAsItem.setMnemonic(KeyEvent.VK_A);
        
        exitItem = new JMenuItem("Sair");
        exitItem.setMnemonic(KeyEvent.VK_R);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        add(fileMenu);
    }
    
    private void createBuildMenu() {
        JMenu buildMenu = new JMenu("Compilar");
        buildMenu.setMnemonic(KeyEvent.VK_C);
        
        compileItem = new JMenuItem("Compilar");
        compileItem.setMnemonic(KeyEvent.VK_C);
        compileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        
        clearConsoleItem = new JMenuItem("Limpar Console");
        clearConsoleItem.setMnemonic(KeyEvent.VK_L);
        clearConsoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        
        buildMenu.add(compileItem);
        buildMenu.addSeparator();
        buildMenu.add(clearConsoleItem);
        
        add(buildMenu);
    }
    
    private void createHelpMenu() {
        JMenu helpMenu = new JMenu("Ajuda");
        helpMenu.setMnemonic(KeyEvent.VK_J);
        
        aboutItem = new JMenuItem("Sobre");
        aboutItem.setMnemonic(KeyEvent.VK_S);
        
        helpMenu.add(aboutItem);
        
        add(helpMenu);
    }
    
    // Setters for action listeners
    public void setNewAction(ActionListener action) {
        newItem.addActionListener(action);
    }
    
    public void setOpenAction(ActionListener action) {
        openItem.addActionListener(action);
    }
    
    public void setSaveAction(ActionListener action) {
        saveItem.addActionListener(action);
    }
    
    public void setSaveAsAction(ActionListener action) {
        saveAsItem.addActionListener(action);
    }
    
    public void setExitAction(ActionListener action) {
        exitItem.addActionListener(action);
    }
    
    public void setCompileAction(ActionListener action) {
        compileItem.addActionListener(action);
    }
    
    public void setClearConsoleAction(ActionListener action) {
        clearConsoleItem.addActionListener(action);
    }
    
    public void setAboutAction(ActionListener action) {
        aboutItem.addActionListener(action);
    }
}
