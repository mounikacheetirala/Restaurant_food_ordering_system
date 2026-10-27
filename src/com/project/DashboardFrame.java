
package com.project;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    int userId;
    String name;
    String role;

    public DashboardFrame(int userId, String name, String role) {

        this.userId = userId;
        this.name = name;
        this.role = role;

        setTitle("Restaurant Food Ordering System");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 122, 0));

        JLabel title = new JLabel("  Welcome, " + name);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JButton logout = new JButton("Logout");

        header.add(title, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new GridLayout(0, 2, 15, 15));
        content.setBorder(
            BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JButton menuButton = new JButton("View Food Menu");
        JButton orderButton = new JButton("Place Order");
        JButton historyButton = new JButton("Order History");
        JButton manageFoodButton = new JButton("Manage Food");
        JButton exitButton = new JButton("Exit");

        menuButton.setFont(new Font("Arial", Font.BOLD, 18));
        orderButton.setFont(new Font("Arial", Font.BOLD, 18));
        historyButton.setFont(new Font("Arial", Font.BOLD, 18));
        manageFoodButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitButton.setFont(new Font("Arial", Font.BOLD, 18));

        content.add(menuButton);
        content.add(orderButton);
        content.add(historyButton);
        content.add(manageFoodButton);
        content.add(exitButton);

        add(content, BorderLayout.CENTER);

        // Button actions
        menuButton.addActionListener(e -> new FoodMenuFrame());

        orderButton.addActionListener(e ->
        new OrderFoodFrame(userId)
    );

        historyButton.addActionListener(e ->
        new OrderHistoryFrame(userId)
    );
        
        manageFoodButton.addActionListener(e -> {
            if ("ADMIN".equalsIgnoreCase(role)) {
                new ManageFoodFrame();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Only administrators can manage food items."
                );
            }
        });

        logout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        exitButton.addActionListener(e -> dispose());

        setVisible(true);
    }
}