package compiler.ide;

import compiler.compiler.CompilationEngine;
import compiler.factory.ComponentFactory;
import compiler.factory.ParserFactory;
import compiler.model.CompilationResult;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Janela principal da IDE do compilador
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class CompilerIDE extends JFrame {
    
    private EditorPanel editorPanel;
    private ConsolePanel consolePanel;
    private SymbolTablePanel symbolTablePanel;
    private MenuBar menuBar;
    private StatusBar statusBar;
    
    private JButton compileButton;
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    
    private CompilationEngine compilationEngine;
    private File currentFile;
    
    private static final String TITLE = "IDE Compilador - T2";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    
    public CompilerIDE() {
        compilationEngine = ParserFactory.createCompilationEngine();
        initComponents();
        setupLayout();
        setupListeners();
        setupKeyBindings();
    }
    
    private void initComponents() {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        editorPanel = new EditorPanel();
        consolePanel = new ConsolePanel();
        symbolTablePanel = new SymbolTablePanel();
        statusBar = new StatusBar();
        menuBar = new MenuBar();
        
        compileButton = ComponentFactory.createCompileButton();
        newButton = ComponentFactory.createNewButton();
        openButton = ComponentFactory.createOpenButton();
        saveButton = ComponentFactory.createSaveButton();
        
        setJMenuBar(menuBar);
        updateTitle();
    }
    
    private void setupLayout() {
        // Editor (left) + symbol table (right)
        JSplitPane editorAndTableSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            createEditorSection(),
            symbolTablePanel
        );
        editorAndTableSplit.setResizeWeight(0.75);

        // Top (editor+table) + bottom (console)
        JSplitPane mainSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            editorAndTableSplit,
            consolePanel
        );
        mainSplit.setResizeWeight(0.7);
        mainSplit.setDividerLocation(0.7);

        add(mainSplit, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createEditorSection() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Barra de ferramentas
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(compileButton);
        
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(editorPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupListeners() {
        // Botões da barra de ferramentas
        compileButton.addActionListener(e -> compile());
        newButton.addActionListener(e -> newFile());
        openButton.addActionListener(e -> openFile());
        saveButton.addActionListener(e -> saveFile());
        
        // Ações do menu
        menuBar.setNewAction(e -> newFile());
        menuBar.setOpenAction(e -> openFile());
        menuBar.setSaveAction(e -> saveFile());
        menuBar.setSaveAsAction(e -> saveFileAs());
        menuBar.setExitAction(e -> exit());
        menuBar.setCompileAction(e -> compile());
        menuBar.setClearConsoleAction(e -> consolePanel.clear());
        menuBar.setAboutAction(e -> showAbout());
        menuBar.setShowSymbolValuesAction(e ->
            symbolTablePanel.setShowValues(e.getStateChange() == ItemEvent.SELECTED));

        editorPanel.addCaretListener(e -> {
            try {
                int caretPos = e.getDot();
                String text = editorPanel.getTextPane().getDocument().getText(0, caretPos);
                int line = text.split("\n", -1).length;
                int col = text.length() - text.lastIndexOf('\n');
                statusBar.updateCaret(line, col);
            } catch (javax.swing.text.BadLocationException ex) {
                // unreachable: caretPos is always valid
            }
        });
    }
    
    private void setupKeyBindings() {
        // F9 para compilar
        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        getRootPane().registerKeyboardAction(
            e -> compile(),
            f9,
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void compile() {
        consolePanel.appendInfo("Iniciando compilação...");
        
        String sourceCode = editorPanel.getText();
        
        if (sourceCode.trim().isEmpty()) {
            consolePanel.appendWarning("Código fonte vazio. Nada para compilar.");
            return;
        }
        
        try {
            CompilationResult result = compilationEngine.compile(sourceCode);
            consolePanel.display(result);

            // Show warnings in console
            for (String warning : result.getWarnings()) {
                consolePanel.appendWarning(warning);
            }

            // Update symbol table panel
            symbolTablePanel.update(result.getSymbolTableRows());

        } catch (Exception ex) {
            consolePanel.appendError("Erro inesperado: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void newFile() {
        if (confirmDiscard()) {
            editorPanel.clear();
            symbolTablePanel.clear();
            currentFile = null;
            updateTitle();
            consolePanel.appendInfo("Novo arquivo criado.");
        }
    }
    
    private void openFile() {
        if (!confirmDiscard()) {
            return;
        }
        
        JFileChooser fileChooser = createFileChooser();
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = Files.readString(file.toPath());
                editorPanel.setText(content);
                currentFile = file;
                updateTitle();
                consolePanel.appendInfo("Arquivo aberto: " + file.getName());
            } catch (IOException ex) {
                showError("Erro ao abrir arquivo: " + ex.getMessage());
            }
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            saveToFile(currentFile);
        }
    }
    
    private void saveFileAs() {
        JFileChooser fileChooser = createFileChooser();
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Adiciona extensão .txt se não estiver presente
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            saveToFile(file);
            currentFile = file;
            updateTitle();
        }
    }
    
    private void saveToFile(File file) {
        try {
            Files.writeString(file.toPath(), editorPanel.getText());
            consolePanel.appendInfo("Arquivo salvo: " + file.getName());
        } catch (IOException ex) {
            showError("Erro ao salvar arquivo: " + ex.getMessage());
        }
    }
    
    private void exit() {
        if (confirmDiscard()) {
            System.exit(0);
        }
    }
    
    private void showAbout() {
        String message = """
                IDE Compilador - Trabalho T2
                Disciplina: Compiladores
                
                Desenvolvido por:
                - Jordan Lippert
                - André Melo
                
                Tecnologias:
                - Java Swing
                - GALS (Gerador de Analisadores)
                - Padrões de Design (Strategy, Adapter, Chain of Responsibility, Factory)
                
                Versão: 1.0
                Data: Abril 2026
                """;
        
        JOptionPane.showMessageDialog(
            this,
            message,
            "Sobre",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private boolean confirmDiscard() {
        // Simplificado: sempre retorna true
        // TODO: Verificar se arquivo foi modificado e pedir confirmação
        return true;
    }
    
    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Arquivos de Texto (*.txt)", "txt"
        ));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        return fileChooser;
    }
    
    private void updateTitle() {
        String fileName = currentFile != null ? currentFile.getName() : "Sem título";
        setTitle(TITLE + " - " + fileName);
        statusBar.updateFile(fileName);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            CompilerIDE ide = new CompilerIDE();
            ide.setVisible(true);
        });
    }
}
