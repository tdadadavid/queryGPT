package org.example;

import javafx.application.Platform;

import javax.swing.*;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WebScannerApp {

    private final JFrame frame;
    private final JTextField queryTextField;
    private final JTextArea textArea;

    public WebScannerApp() {
        frame = new JFrame("Web Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        queryTextField = new JTextField(10);
        JButton searchButton = new JButton("Search");
        textArea = new JTextArea();

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Enter Query:"));
        inputPanel.add(queryTextField);
        inputPanel.add(searchButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            try {
                startSearch();
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
        });

        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.setVisible(true);
    }

    private void startSearch() throws UnsupportedEncodingException {
        String query = queryTextField.getText().trim();
        if (query.isEmpty()) {
            appendText();
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            WebScanner webScanner = new WebScanner();
            try {
                System.out.println("search starts");
                List<SearchResult> results = webScanner.startSearch(query);
                System.out.println("Search ends");
                return results;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).thenAcceptAsync(this::displayResults, Platform::runLater);

    }

    private void displayResults(List<SearchResult> results) {
        textArea.setText("Search Results:\n");
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            textArea.append((i + 1) + ". " + result.getFullUrl() + "\n");
        }
    }

    private void appendText() {
        textArea.append("Please enter a search query.\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WebScannerApp::new);
    }
}


