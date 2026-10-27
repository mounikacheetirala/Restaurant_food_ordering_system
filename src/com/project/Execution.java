package com.project;

import java.sql.Connection;

public class Execution {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Connection con =DBConnection.getC();
		
		if(con != null)
		{
			System.out.println("Connected Successfully");
			 try {
	                con.close();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
		}
		else
		{
			System.out.println("Connection failed");
		}
	}

}
