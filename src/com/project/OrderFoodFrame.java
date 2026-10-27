
package com.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OrderFoodFrame extends JFrame {

    JTable foodTable, cartTable;
    DefaultTableModel foodModel, cartModel;
    JLabel totalLabel;
    double total = 0;
    int userId;
    public OrderFoodFrame(int userId) {
        this.userId = userId;
        setTitle("Restaurant - Place Order");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("Order Your Favourite Food", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(255, 122, 0));
        add(heading, BorderLayout.NORTH);

        foodModel = new DefaultTableModel(
            new String[]{"ID", "Food Name", "Category", "Price"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        foodTable = new JTable(foodModel);
        foodTable.setRowHeight(25);

        cartModel = new DefaultTableModel(
            new String[]{"Food ID", "Food Name", "Price", "Quantity", "Amount"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(25);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(new JLabel("  Available Food"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(foodTable), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(new JLabel("  Shopping Cart"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel
        );
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Add to Cart");
        JButton removeButton = new JButton("Remove from Cart");
        JButton checkoutButton = new JButton("Checkout");

        totalLabel = new JLabel("Total: Rs. 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(addButton);
        bottomPanel.add(removeButton);
        bottomPanel.add(totalLabel);
        bottomPanel.add(checkoutButton);

        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addToCart());
        removeButton.addActionListener(e -> removeFromCart());
        checkoutButton.addActionListener(e -> checkout());

        loadFoodItems();
        setVisible(true);
    }

    private void loadFoodItems() {
        foodModel.setRowCount(0);

        String sql = "SELECT food_id, food_name, category, price "
                   + "FROM food_items WHERE available = TRUE";

        try (Connection con = DBConnection.getC()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    foodModel.addRow(new Object[]{
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getString("category"),
                        rs.getDouble("price")
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

    private void addToCart() {
        int row = foodTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a food item first.");
            return;
        }

        row = foodTable.convertRowIndexToModel(row);

        int foodId = Integer.parseInt(foodModel.getValueAt(row, 0).toString());
        String foodName = foodModel.getValueAt(row, 1).toString();
        double price = Double.parseDouble(foodModel.getValueAt(row, 3).toString());

        String input = JOptionPane.showInputDialog(
            this, "Enter quantity:", "1"
        );

        if (input == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(input.trim());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(
                    this, "Quantity must be greater than zero."
                );
                return;
            }

            int existingRow = -1;

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int existingId = Integer.parseInt(
                    cartModel.getValueAt(i, 0).toString()
                );

                if (existingId == foodId) {
                    existingRow = i;
                    break;
                }
            }

            if (existingRow >= 0) {
                int oldQuantity = Integer.parseInt(
                    cartModel.getValueAt(existingRow, 3).toString()
                );

                int newQuantity = oldQuantity + quantity;

                cartModel.setValueAt(newQuantity, existingRow, 3);
                cartModel.setValueAt(
                    price * newQuantity, existingRow, 4
                );
            } else {
                cartModel.addRow(new Object[]{
                    foodId, foodName, price, quantity, price * quantity
                });
            }

            calculateTotal();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Please enter a valid whole-number quantity."
            );
        }
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Select a cart item to remove."
            );
            return;
        }

        cartModel.removeRow(cartTable.convertRowIndexToModel(row));
        calculateTotal();
    }

    private void calculateTotal() {
        total = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += Double.parseDouble(
                cartModel.getValueAt(i, 4).toString()
            );
        }

        totalLabel.setText(String.format("Total: Rs. %.2f", total));
    }


private void checkout() {
    if (cartModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Your cart is empty.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
        this,
        String.format(
            "Subtotal: Rs. %.2f\nTax (5%%): Rs. %.2f\nTotal: Rs. %.2f\n\nConfirm order?",
            total, total * 0.05, total * 1.05
        ),
        "Confirm Checkout",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    String orderSql =
        "INSERT INTO orders (user_id, subtotal, tax, total) VALUES (?, ?, ?, ?)";

    String itemSql =
        "INSERT INTO order_items " +
        "(order_id, food_id, quantity, unit_price, line_total) " +
        "VALUES (?, ?, ?, ?, ?)";

    try (Connection con = DBConnection.getC()) {
        if (con == null) {
            JOptionPane.showMessageDialog(
                this, "Database connection failed."
            );
            return;
        }

        try {
            con.setAutoCommit(false);

            int orderId;

            try (PreparedStatement ps = con.prepareStatement(
                    orderSql,
                    PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, userId);
                ps.setDouble(2, total);
                ps.setDouble(3, total * 0.05);
                ps.setDouble(4, total * 1.05);

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new Exception("Could not generate order ID.");
                    }
                    orderId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int foodId = Integer.parseInt(
                        cartModel.getValueAt(i, 0).toString()
                    );

                    double price = Double.parseDouble(
                        cartModel.getValueAt(i, 2).toString()
                    );

                    int quantity = Integer.parseInt(
                        cartModel.getValueAt(i, 3).toString()
                    );

                    double lineTotal = price * quantity;

                    ps.setInt(1, orderId);
                    ps.setInt(2, foodId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, price);
                    ps.setDouble(5, lineTotal);

                    ps.addBatch();
                }

                ps.executeBatch();
            }

            con.commit();

            JOptionPane.showMessageDialog(
                this,
                String.format(
                    "Order placed successfully!\nOrder ID: %d\n" +
                    "Subtotal: Rs. %.2f\nTax: Rs. %.2f\nTotal: Rs. %.2f",
                    orderId, total, total * 0.05, total * 1.05
                )
            );

            cartModel.setRowCount(0);
            calculateTotal();

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            this, "Checkout failed: " + e.getMessage()
        );
    }
}
}