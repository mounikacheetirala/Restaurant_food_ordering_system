
package com.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Image;

public class FoodMenuFrame extends JFrame {

    JTable table;
    DefaultTableModel model;

    public FoodMenuFrame() {
        setTitle("Restaurant - Food Menu");
        setSize(750, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel heading = new JLabel("Food Menu", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(new Color(255, 122, 0));

        String[] columns = {
        	    "Image", "Food ID", "Food Name", "Category", "Price", "Available"
        	};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


table = new JTable(model);

table.setRowHeight(70);
table.setFont(new Font("Arial", Font.PLAIN, 14));

table.getColumnModel().getColumn(0).setCellRenderer(
    new DefaultTableCellRenderer() {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);

            if (value instanceof ImageIcon) {
                label.setIcon((ImageIcon) value);
            } else {
                label.setText("No image");
            }

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setOpaque(true);
            }

            return label;
        }
    }
);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 14)
        );

        JScrollPane scrollPane = new JScrollPane(table);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadFoodItems());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);

        add(heading, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadFoodItems();

        setVisible(true);
    }

    private void loadFoodItems() {
        model.setRowCount(0);

        String sql = "SELECT food_id, food_name, category, price, available "
                   + "FROM food_items";

        try (Connection con = DBConnection.getC()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this, "Database connection failed."
                );
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {


while (rs.next()) {

    String foodName = rs.getString("food_name");
    String imageFile;

    if (foodName.toLowerCase().contains("pizza")) {
        imageFile = "pizza.jpg";
    } else if (foodName.toLowerCase().contains("burger")) {
        imageFile = "burger.jpg";
    } else if (foodName.toLowerCase().contains("biryani")) {
        imageFile = "biryani.jpg";
    } else {
        imageFile = "";
    }

    ImageIcon foodImage = imageFile.isEmpty()
        ? null : getFoodImage(imageFile);

    model.addRow(new Object[] {
        foodImage,
        rs.getInt("food_id"),
        foodName,
        rs.getString("category"),
        rs.getDouble("price"),
        rs.getBoolean("available") ? "Yes" : "No"
    });
}
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this, "No food items found in the database."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Unable to load food menu: " + e.getMessage()
            );
        }
    }

private ImageIcon getFoodImage(String fileName) {
    java.io.File file = new java.io.File("images/" + fileName);

    if (!file.exists()) {
        return null;
    }

    ImageIcon icon = new ImageIcon(file.getAbsolutePath());

    Image image = icon.getImage().getScaledInstance(
        80, 60, Image.SCALE_SMOOTH
    );

    return new ImageIcon(image);
}
}