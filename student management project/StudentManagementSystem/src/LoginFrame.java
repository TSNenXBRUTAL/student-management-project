import javax.swing.*;
import java.sql.*;

public class LoginFrame {

    public static void main(String[] args) {

        String username =
                JOptionPane.showInputDialog("Username");

        String password =
                JOptionPane.showInputDialog("Password");

        try {

            Connection con =
                    DBConnection.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(
                    "SELECT * FROM admin WHERE username=? AND password=?");

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                new Dashboard();

            } else {

                JOptionPane.showMessageDialog(
                        null,
                        "Invalid Login"
                );
            }

        } catch(Exception e) {

            e.printStackTrace();
        }
    }
} 