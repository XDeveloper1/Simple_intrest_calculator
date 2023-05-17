import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

class Data {
    String id;
    String history;

    public Data(String id, String history) {
        this.id = id;
        this.history = history;
    }
}

public class Main implements ActionListener {

    private JFrame frame;
    private JTextField displayField;
    private JTextArea area;
    private double currentValue = 0.0;
    private boolean startNewNumber = true;
    private String lastOperator = "";
    ArrayList<Data> dataList = new ArrayList<>();
    Connection connection = null;
    String databaseName = "Calculations";
    String tableName = "history";

    public Main() {
        frame = new JFrame("Calculator");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        displayField = new JTextField();
        displayField.setFont(new Font("Arial", Font.PLAIN, 24));
        displayField.setEditable(false);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints displayConstraints = new GridBagConstraints();
        displayConstraints.gridx = 0;
        displayConstraints.gridy = 0;
        displayConstraints.gridwidth = 4;
        displayConstraints.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(displayField, displayConstraints);

        area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Arial", Font.PLAIN, 15));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints scrollPaneConstraints = new GridBagConstraints();
        scrollPaneConstraints.gridx = 0;
        scrollPaneConstraints.gridy = 1;
        scrollPaneConstraints.gridwidth = 4;
        scrollPaneConstraints.fill = GridBagConstraints.BOTH;
        scrollPaneConstraints.weightx = 2.0;
        scrollPaneConstraints.weighty = 1.0;
        scrollPaneConstraints.gridheight = 2;  // Increase the height to 2
        mainPanel.add(scrollPane, scrollPaneConstraints);

        String[] buttonLabels = {
                "C", "DEL", "/", "*", "7", "8", "9", "-", "4", "5", "6",
                "+", "1", "2", "3", "=", "0", ".", "SI", "MOD"
        };

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.BOTH;
        buttonConstraints.insets = new Insets(10, 10, 10, 10);

        int buttonIndex = 0;
        int row = 3; // Start from the 3rd row
        int col = 0;
        for (String buttonLabel : buttonLabels) {
            JButton button = new JButton(buttonLabel);
            button.addActionListener(this);
            button.setFocusable(false);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(211, 211, 211));
            button.setForeground(Color.BLACK);

            if (buttonLabel.equals("C")) {
                button.setBackground(new Color(226, 73, 73));
                button.setForeground(Color.black);
            } else if (buttonLabel.equals("DEL")) {
                button.setBackground(new Color(0, 123, 255));
                button.setForeground(Color.black);
            } else if (buttonLabel.matches("[+\\-*/=]")) {
                button.setBackground(new Color(40, 167, 69));
                button.setForeground(Color.black);
            }

            buttonConstraints.gridx = col;
            buttonConstraints.gridy = row;
            mainPanel.add(button, buttonConstraints);

            col++;
            if (col > 3) {
                col = 0;
                row++;
            }

            buttonIndex++;
        }

        // Retrieve Data button
        JButton retrieveButton = new JButton("Retrieve Data");
        retrieveButton.addActionListener(this);
        retrieveButton.setFocusable(false);
        retrieveButton.setFont(new Font("Arial", Font.BOLD, 18));
        retrieveButton.setBackground(new Color(211, 211, 211));
        retrieveButton.setForeground(Color.BLACK);
        GridBagConstraints retrieveConstraints = new GridBagConstraints();
        retrieveConstraints.gridx = 0;
        retrieveConstraints.gridy = row;
        retrieveConstraints.gridwidth = 2;
        retrieveConstraints.insets = new Insets(10, 10, 10, 5);
        retrieveConstraints.anchor = GridBagConstraints.LINE_START;
        mainPanel.add(retrieveButton, retrieveConstraints);
        // Clear History button
        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(this);
        clearButton.setFocusable(false);
        clearButton.setFont(new Font("Arial", Font.BOLD, 18));
        clearButton.setBackground(new Color(211, 211, 211));
        clearButton.setForeground(Color.BLACK);
        GridBagConstraints clearConstraints = new GridBagConstraints();
        clearConstraints.gridx = 2;
        clearConstraints.gridy = row;
        clearConstraints.gridwidth = 2;
        clearConstraints.insets = new Insets(10, 5, 10, 10);
        clearConstraints.anchor = GridBagConstraints.LINE_END;
        mainPanel.add(clearButton, clearConstraints);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }


        @Override
    public void actionPerformed(ActionEvent e) {
        String buttonText = e.getActionCommand();
        String displayText = displayField.getText();

        if (buttonText.equals("C")) {
            currentValue = 0.0;
            lastOperator = "";
            displayField.setText("");
        } else if (buttonText.equals("DEL")) {
            if (displayText.length() > 0) {
                displayField.setText(displayText.substring(0, displayText.length() - 1));
            }
        } else if (buttonText.matches("\\d") || buttonText.equals(".")) {
            if (startNewNumber) {
                displayField.setText("");
                startNewNumber = false;
            }
            displayField.setText(displayField.getText() + buttonText);
        } else if (buttonText.matches("[+\\-*/]")) {
            if (!startNewNumber) {
                calculate();
            }
            lastOperator = buttonText;
            startNewNumber = true;
        } else if (buttonText.equals("=")) {
            calculate();
            startNewNumber = true;
            lastOperator = "";
        } else if (buttonText.equals("SI")) {
            SI();
        } else if (buttonText.equals("Clear History")) {

            clearHistory();

        } else if (buttonText.equals("Retrieve Data")) {
            retrieveData();
        }
    }

    private void calculate() {
        double newValue = Double.parseDouble(displayField.getText());
        switch (lastOperator) {
            case "":
                currentValue = newValue;
                break;
            case "+":
                currentValue += newValue;
                break;
            case "-":
                currentValue -= newValue;
                break;
            case "*":
                currentValue *= newValue;
                break;
            case "/":
                currentValue /= newValue;
                break;
        }
        jdbc();
        displayField.setText(Double.toString(currentValue));
    }

    private void jdbc() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
            System.out.println("Connected");
            String createDatabaseSql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(createDatabaseSql);
            System.out.println("Db Created");
            // Use database
            String selectDatabaseSql = "USE Calculations";
            stmt.execute(selectDatabaseSql);

            // Create table
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "histories INT NOT NULL"
                    + ")";
            stmt.executeUpdate(createTableSql);
            System.out.println("Table Created");
            // Insert record
            String insertSql = "INSERT INTO " + tableName + " (histories) VALUES (?)";
            PreparedStatement pstmt = connection.prepareStatement(insertSql);
            pstmt.setDouble(1, currentValue);
            pstmt.executeUpdate();
            System.out.println("Insert Record");

        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to clear the history?", "Clear History", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Your existing code to clear the history
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
                String selectDatabaseSql = "USE Calculations";
                Statement stmt = connection.createStatement();
                stmt.execute(selectDatabaseSql);

                String truncateSql = "TRUNCATE TABLE " + tableName;
                stmt.executeUpdate(truncateSql);

                area.setText("");
                dataList.clear();
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }


    private void retrieveData() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
            String selectDatabaseSql = "USE Calculations";
            Statement stmt = connection.createStatement();
            stmt.execute(selectDatabaseSql);

            String selectSql = "SELECT * FROM " + tableName;
            ResultSet rs = stmt.executeQuery(selectSql);
            StringBuilder sb = new StringBuilder();
            dataList.clear();

            while (rs.next()) {
                int id = rs.getInt("id");
                Double histories = rs.getDouble("histories");
                dataList.add(new Data(String.valueOf(id), histories
                        .toString()));
            }
            for (Data data : dataList) {
                sb.append(data.history + "\n");
            }
            area.setText(sb.toString());

        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void SI() {
        String principal = JOptionPane.showInputDialog(frame, "Enter principal amount:");
        String rate = JOptionPane.showInputDialog(frame, "Enter rate of interest:");
        String time = JOptionPane.showInputDialog(frame, "Enter time period in years:");
        double p = Double.parseDouble(principal);
        double r = Double.parseDouble(rate);
        double t = Double.parseDouble(time);

        double si = (p * r * t) / 100.0;

        displayField.setText(Double.toString(si));
        startNewNumber = true;
        lastOperator = "";
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Main());
    }
}