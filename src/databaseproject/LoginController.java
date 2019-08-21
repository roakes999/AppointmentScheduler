/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import static databaseproject.DBConnection.conn;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 *
 * @author Rob
 */
public class LoginController implements Initializable {
    
    Boolean spanishTest = false;  // set to true to test spanish login screen
    
    static String currentUser;
    
    @FXML
    private Label TitleLabel;

    @FXML
    private Label UserNameLabel;

    @FXML
    private TextField UserNameField;

    @FXML
    private Label PasswordLabel;

    @FXML
    private TextField PasswordField;

    @FXML
    private Button LoginBtn;

    @FXML
    private Button ExitBtn;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UserNameField.setText("test"); // For Testing
        PasswordField.setText("test"); // For Testing
        
        String english = "en";
        String spanish = "es";
        String systemLanguage = Locale.getDefault().getLanguage();
        if (english.equals(systemLanguage))
        {
            TitleLabel.setText("Appointment Scheduler Login");
            UserNameLabel.setText("User Name");
            PasswordLabel.setText("Password");
            LoginBtn.setText("Login");
            ExitBtn.setText("Exit");
            
        }
        if (spanish.equals(systemLanguage) || spanishTest)
        {
            TitleLabel.setText("Inicio de sesión programador de citas");
            UserNameLabel.setText("Usuario");
            PasswordLabel.setText("Contraseña");
            LoginBtn.setText("Iniciar sesión");
            ExitBtn.setText("Salida");
        }
    }    
    
    @FXML
    void LoginBtnHandler(ActionEvent event) throws SQLException, IOException {
        // Username1: test, Password: test   ;   Username2: joe, Password: joe     (2 users)
        String userName = UserNameField.getText();
        currentUser = UserNameField.getText();
        String password = PasswordField.getText();
        String sqlStatement;
        boolean userFound = false;
        boolean passwordMatch = false;
        
        Statement stmt = conn.createStatement();
        sqlStatement = "SELECT * FROM user";
        ResultSet result = stmt.executeQuery(sqlStatement);
        
        while(result.next()) {
                if (userName.equals(result.getString("userName")))
                {
                    userFound = true;
                    if (password.equals(result.getString("password")))
                    {
                        passwordMatch = true;
                    }
                }            
        }
        PasswordField.clear();
        if (userFound == false)
        {
            Alert userNameAlert = new Alert(Alert.AlertType.ERROR);
            if (Locale.getDefault().getLanguage().equals("en"))
            {
                userNameAlert.setTitle("Error Dialog");
                userNameAlert.setHeaderText("User name does not exist");
            }
            if (Locale.getDefault().getLanguage().equals("es") || spanishTest)
            {
                userNameAlert.setTitle("Diálogo de error");
                userNameAlert.setHeaderText("El usuario no existe");
            }
            userNameAlert.showAndWait();
        }
        if ((userFound == true) && (passwordMatch == false))
        {
            Alert passwordAlert = new Alert(Alert.AlertType.ERROR);
            if (Locale.getDefault().getLanguage().equals("en"))
            {
                passwordAlert.setTitle("Error Dialog");
                passwordAlert.setHeaderText("Incorrect password");
            }
            if (Locale.getDefault().getLanguage().equals("es") || spanishTest)
            {
                passwordAlert.setTitle("Diálogo de error");
                passwordAlert.setHeaderText("Contraseña incorrecta");
            }
            passwordAlert.showAndWait();
        }
        if (passwordMatch == true)
        
        LoginTracker();  // Records login activity to a text file
        AppointmentReminder();   // checks for appointments coming up in less than 15 minutes of user login
        {
            Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        }   
    }
    
    @FXML
    void ExitBtnHandler(ActionEvent event) throws SQLException, Exception {
        DBConnection.closeConnection();
        System.exit(0);
    }

    private void AppointmentReminder() {
        String sqlStatement;
        Statement stmt;
        
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT start FROM appointment;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
                Timestamp startTimestamp = Timestamp.valueOf(result.getString("start"));
                LocalDateTime startLocalDateTime = startTimestamp.toLocalDateTime();
                ZonedDateTime startZonedDateTimeUTC = ZonedDateTime.of(startLocalDateTime, ZoneId.of("UTC"));
                ZonedDateTime startDateTimeZonedAdjusted = startZonedDateTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
 
                LocalDateTime loginDateTime = LocalDateTime.now();
                if ((loginDateTime.getYear() == startDateTimeZonedAdjusted.getYear()) & (loginDateTime.getDayOfYear() == startDateTimeZonedAdjusted.getDayOfYear())){
                    LocalTime time2 = startDateTimeZonedAdjusted.toLocalTime();
                    LocalTime time1 = loginDateTime.toLocalTime();
                    int time2TotalMinutes = time2.getHour()*60 + time2.getMinute();
                    int time1TotalMinutes = time1.getHour()*60 + time1.getMinute();
                    int minutesUntilAppointment = time2TotalMinutes - time1TotalMinutes;  // finds difference between login in time and appointment time
                    if (minutesUntilAppointment > 0 & minutesUntilAppointment < 15){      // finds appointments that will start within 15 minutes
                        Alert appointmentReminder = new Alert(Alert.AlertType.INFORMATION);
                        appointmentReminder.setTitle("Appointment Reminder");
                        appointmentReminder.setHeaderText("An Appointment Is Scheduled To Start Within The Next 15 Minutes");
                        appointmentReminder.showAndWait();
                    }
                }
            }
            } catch (SQLException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error: " + ex);
            }  
    }

    private void LoginTracker() throws IOException {
        String filename = "loginTracker.txt", loginData;
        FileWriter fwriter = new FileWriter(filename, true);
        PrintWriter outputFile = new PrintWriter(fwriter);
        loginData = "User: " + currentUser + "    ";
        outputFile.println(loginData);
        loginData = "Login Time: " + LocalDateTime.now().toString() + "\n";
        outputFile.println(loginData);
        outputFile.close();
        
    }
}

