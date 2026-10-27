
package com.project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OrderHistoryFrame extends JFrame {

    JTable table;
    DefaultTableModel model;
    int userId;

    public OrderHistoryFrame(int userId) {
        this.userId = userId;

        setTitle("My Order History");
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("My Order History", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(new Color(255, 122, 0));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));

        model = new DefaultTableModel(
            new String[]{
                "Order ID", "Order Date", "Subtotal", "Tax", "Total"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadOrders());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);

        add(heading, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadOrders();
        setVisible(true);
    }

    private void loadOrders() {
        model.setRowCount(0);

        String sql =
            "SELECT order_id, order_date, subtotal, tax, total " +
            "FROM orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection con = DBConnection.getC()) {
            if (con == null) {
                JOptionPane.showMessageDialog(
                    this, "Database connection failed."
                );
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("order_id"),
                            rs.getTimestamp("order_date"),
                            rs.getDouble("subtotal"),
                            rs.getDouble("tax"),
                            rs.getDouble("total")
                        });
                    }
                }
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this, "You have not placed any orders yet."
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Unable to load order history: " + e.getMessage()
            );
        }
    }
}