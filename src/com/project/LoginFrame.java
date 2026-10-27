package com.project;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginFrame extends JFrame{

	private JTextField username;
	private JPasswordField password;
	private JButton login,clear;
	
	public LoginFrame()
	{
		setTitle("Restaurant Food Ordering System");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 25, 25));

        panel.setBackground(new Color(255, 247, 240));

        JLabel title = new JLabel("Restaurant Login");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        username = new JTextField();
        password = new JPasswordField();

        login = new JButton("Login");
        clear = new JButton("Clear");

        panel.add(title);
        panel.add(new JLabel("Welcome!"));
        panel.add(new JLabel("Username:"));
        panel.add(username);
        panel.add(new JLabel("Password:"));
        panel.add(password);
        panel.add(login);
        panel.add(clear);

        add(panel);

        login.addActionListener(e -> checkLogin());

        clear.addActionListener(e -> {
            username.setText("");
            password.setText("");
        });

        setVisible(true);
	}
	
	
	 private void checkLogin() {

	        String user = username.getText().trim();
	        String pass = new String(password.getPassword());

	        String sql = "SELECT user_id, full_name, role FROM users "
	                   + "WHERE username=? AND password=?";

	        try (Connection con = DBConnection.getC();
	             PreparedStatement ps = con.prepareStatement(sql)) {

	            ps.setString(1, user);
	            ps.setString(2, pass);

	            try (ResultSet rs = ps.executeQuery()) {

	                if (rs.next()) {

	                    int userId = rs.getInt("user_id");
	                    String name = rs.getString("full_name");
	                    String role = rs.getString("role");

	                    JOptionPane.showMessageDialog(
	                        this, "Login successful!");

	                    dispose();

	                    // Next screen will be our dashboard.
	                    new DashboardFrame(userId, name, role);

	                } else {

	                    JOptionPane.showMessageDialog(
	                        this, "Invalid username or password!");
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();

	            JOptionPane.showMessageDialog(
	                this, "Database error: " + e.getMessage());
	        }
	    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		 SwingUtilities.invokeLater(() -> new LoginFrame());
		

	}

}
