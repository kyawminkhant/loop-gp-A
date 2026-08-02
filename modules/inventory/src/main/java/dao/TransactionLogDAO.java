package dao;

import LoopsFirstYearProject.LoopsFirstYearProject.db.DBConnection;
import model.TransactionLog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class TransactionLogDAO {


    public static void insertLog(
            String username,
            String action,
            String details,
            String dateTime
    ){

        String sql =
        """
        INSERT INTO inventory_stock_TransactionLog
        (username, action, details, dateTime)
        VALUES (?, ?, ?, ?)
        """;


        try(
            Connection conn = DBConnection.getConnectionURLlocation();
            PreparedStatement ps = conn.prepareStatement(sql)
        ){

            ps.setString(1, username);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, dateTime);


            ps.executeUpdate();


        }catch(Exception e){

            e.printStackTrace();

        }

    }



    public static ObservableList<TransactionLog> getAllLogs(){

        ObservableList<TransactionLog> list =
                FXCollections.observableArrayList();


        String sql =
                "SELECT * FROM inventory_stock_TransactionLog ORDER BY logID DESC";


        try(
            Connection conn = DBConnection.getConnectionURLlocation();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ){


            while(rs.next()){


                list.add(
                    new TransactionLog(

                        rs.getInt("logID"),

                        rs.getString("username"),

                        rs.getString("action"),

                        rs.getString("details"),

                        rs.getString("dateTime")

                    )
                );

            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return list;

    }

}