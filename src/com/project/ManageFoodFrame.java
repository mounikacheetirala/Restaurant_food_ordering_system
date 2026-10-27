
package com.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageFoodFrame extends JFrame {

    JTextField foodName, category, price;
    JCheckBox available;
    JTable table;
    DefaultTableModel model;

    public ManageFoodFrame() {
        setTitle("Manage Food Items");
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        foodName = new JTextField();
        category = new JTextField();
        price = new JTextField();
        available = new JCheckBox("Available", true);

        form.add(new JLabel("Food Name:"));
        form.add(foodName);
        form.add(new JLabel("Category:"));
        form.add(category);
        form.add(new JLabel("Price:"));
        form.add(price);
        form.add(new JLabel("Availability:"));
        form.add(available);

        JButton addButton = new JButton("Add Food");
        JButton deleteButton = new JButton("Delete Selected");
        JButton updateButton = new JButton("Update Food");
        JButton refreshButton = new JButton("Refresh");

        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);

        model = new DefaultTableModel(
            new String[]{"ID", "Food Name", "Category", "Price", "Available"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addFood());
        deleteButton.addActionListener(e -> deleteFood());
        refreshButton.addActionListener(e -> loadFood());
        updateButton.addActionListener(e -> updateFood());

        table.getSelectionModel().addListSelectionListener(e -> {
        	if (!e.getValueIsAdjusting()) {
        		int row = table.getSelectedRow();

        if (row != -1) {
            row = table.convertRowIndexToModel(row);

            foodName.setText(
                model.getValueAt(row, 1).toString()
            );

            category.setText(
                model.getValueAt(row, 2).toString()
            );

            price.setText(
                model.getValueAt(row, 3).toString()
            );

            available.setSelected(
                model.getValueAt(row, 4).toString().equals("Yes")
            );
        }
    }
});
        loadFood();
        setVisible(true);
    }

    private void addFood() {
        String name = foodName.getText().trim();
        String cat = category.getText().trim();
        String priceText = price.getText().trim();

        if (name.isEmpty() || cat.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(priceText);

            if (!Double.isFinite(amount) || amount <= 0) {
                JOptionPane.showMessageDialog(
                    this, "Price must be a positive number."
                );
                return;
            }

            String sql = "INSERT INTO food_items "
                       + "(food_name, category, price, available) "
                       + "VALUES (?, ?, ?, ?)";

            try (Connection con = DBConnection.getC()) {
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, cat);
                    ps.setDouble(3, amount);
                    ps.setBoolean(4, available.isSelected());

                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Food added successfully!");
            foodName.setText("");
            category.setText("");
            price.setText("");
            available.setSelected(true);
            loadFood();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid price.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Unable to add food: " + e.getMessage()
            );
        }
    }

    private void loadFood() {
        model.setRowCount(0);

        String sql = "SELECT food_id, food_name, category, price, available "
                   + "FROM food_items ORDER BY food_id";

        try (Connection con = DBConnection.getC()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getBoolean("available") ? "Yes" : "No"
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Unable to load food: " + e.getMessage()
            );
        }
    }


private void updateFood() {

    int selectedRow = table.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
            this, "Please select a food item to update."
        );
        return;
    }

    String name = foodName.getText().trim();
    String cat = category.getText().trim();
    String priceText = price.getText().trim();

    if (name.isEmpty() || cat.isEmpty() || priceText.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "Please fill all fields."
        );
        return;
    }

    try {
        double amount = Double.parseDouble(priceText);

        if (!Double.isFinite(amount) || amount <= 0) {
            JOptionPane.showMessageDialog(
                this, "Price must be a positive number."
            );
            return;
        }

        selectedRow = table.convertRowIndexToModel(selectedRow);

        int foodId = Integer.parseInt(
            model.getValueAt(selectedRow, 0).toString()
        );

        String sql = "UPDATE food_items "
                   + "SET food_name = ?, category = ?, "
                   + "price = ?, available = ? "
                   + "WHERE food_id = ?";

        try (Connection con = DBConnection.getC()) {

            if (con == null) {
                JOptionPane.showMessageDialog(
                    this, "Database connection failed."
                );
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, cat);
                ps.setDouble(3, amount);
                ps.setBoolean(4, available.isSelected());
                ps.setInt(5, foodId);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(
                        this, "Food updated successfully!"
                    );

                    foodName.setText("");
                    category.setText("");
                    price.setText("");
                    available.setSelected(true);

                    loadFood();
                    table.clearSelection();

                } else {
                    JOptionPane.showMessageDialog(
                        this, "Food item was not found."
                    );
                }
            }
        }

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
            this, "Please enter a valid price."
        );
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            this, "Unable to update food: " + e.getMessage()
        );
    }
}

    private void deleteFood() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a food item first."
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this food item?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int foodId = (int) model.getValueAt(
            table.convertRowIndexToModel(selectedRow), 0
        );

        String sql = "DELETE FROM food_items WHERE food_id = ?";

        try (Connection con = DBConnection.getC()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, foodId);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Food deleted successfully!");
            loadFood();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Unable to delete food. It may already be used in an order. "
                    + e.getMessage()
            );
        }
    }
}