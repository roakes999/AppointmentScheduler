/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import static databaseproject.DBConnection.conn;
import static databaseproject.MainScreenController.AppointmentList;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob
 */
public class ReportsController implements Initializable {
    
    ObservableList<String> UserList = FXCollections.observableArrayList();
    
    @FXML
    private TableView<Appointments> ReportTable;
    @FXML
    private TextArea Output2Display;
    @FXML
    private TableColumn<Appointments, String> date;
    @FXML
    private TableColumn<Appointments, String> start;
    @FXML
    private TableColumn<Appointments, String> end;
    @FXML
    private TableColumn<Appointments, String> customer;
    @FXML
    private TableColumn<Appointments, String> consultant;
    @FXML
    private ComboBox<String> ConsultantDropDown;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        populateConsultantDropDown();
    }    

    @FXML
    private void BackBtnHandler(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void ConsultantDropDownHandler(ActionEvent event) {
        
        String sqlStatement;
        Statement stmt;
        String selectedConsultant = ConsultantDropDown.getValue();
        AppointmentList.clear();
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT appointmentId, start, end, customerName, userName\n" +
            "FROM appointment, customer, user\n" +
            "WHERE appointment.customerId = customer.customerId AND appointment.createdBy = user.userName ORDER BY start";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
                // converts UTC to users time zone for start and end times
                // start time
                Timestamp startTimestamp = Timestamp.valueOf(result.getString("start"));
                LocalDateTime startLocalDateTime = startTimestamp.toLocalDateTime();
                ZonedDateTime startZonedDateTimeUTC = ZonedDateTime.of(startLocalDateTime, ZoneId.of("UTC"));
                ZonedDateTime startDateTimeZonedAdjusted = startZonedDateTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
                //end time
                Timestamp endTimestamp = Timestamp.valueOf(result.getString("end"));
                LocalDateTime endLocalDateTime = endTimestamp.toLocalDateTime();
                ZonedDateTime endZonedDateTimeUTC = ZonedDateTime.of(endLocalDateTime, ZoneId.of("UTC"));
                ZonedDateTime endDateTimeZonedAdjusted = endZonedDateTimeUTC.withZoneSameInstant(ZoneId.systemDefault());

                if (selectedConsultant.equals(result.getString("userName"))) {     // Filters out results to show only appointments for selected consultant
                    AppointmentList.add(new Appointments(result.getInt("appointmentId"),
                                                   startDateTimeZonedAdjusted.toString().substring(0, 10),
                                                   startDateTimeZonedAdjusted.toString().substring(11, 16),
                                                   endDateTimeZonedAdjusted.toString().substring(11, 16),
                                                   result.getString("customerName"),
                                                   result.getString("userName")));
                }  
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        
            date.setCellValueFactory(new PropertyValueFactory<>("day"));
            start.setCellValueFactory(new PropertyValueFactory<>("start"));
            end.setCellValueFactory(new PropertyValueFactory<>("end"));
            customer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            consultant.setCellValueFactory(new PropertyValueFactory<>("userName"));
            ReportTable.setItems(AppointmentList);
            ReportTable.refresh();
    }

    @FXML
    void NumberOfAppointmentsByMonthHandler(ActionEvent event) {
        String sqlStatement;
        Statement stmt;
        int januaryCount = 0;
        int februaryCount = 0;
        int marchCount = 0;
        int aprilCount = 0;
        int mayCount = 0;
        int juneCount = 0;
        int julyCount = 0;
        int augustCount = 0;
        int septemberCount = 0;
        int octoberCount = 0;
        int novemberCount = 0;
        int decemberCount = 0;

        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT start FROM appointment;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
                int month = result.getDate("start").toLocalDate().getMonthValue();
                
                if (month == 1)
                    januaryCount = januaryCount + 1;
                if (month == 2)
                    februaryCount = februaryCount + 1;
                if (month == 3)
                    marchCount = marchCount + 1;
                if (month == 4)
                    aprilCount = aprilCount + 1;
                if (month == 5)
                    mayCount = mayCount + 1;
                if (month == 6)
                    juneCount = juneCount + 1;
                if (month == 7)
                    julyCount = julyCount + 1;
                if (month == 8)
                    augustCount = augustCount + 1;
                if (month == 9)
                    septemberCount = septemberCount + 1;
                if (month == 10)
                    octoberCount = octoberCount + 1;
                if (month == 11)
                    novemberCount = novemberCount + 1;
                if (month == 12)
                    decemberCount = decemberCount + 1;
            }    
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        Output2Display.clear();
        Output2Display.setText("JAN: " + januaryCount + "\nFEB: " + februaryCount
                                + "\nMAR: " + marchCount + "\nAPR: " + aprilCount
                                + "\nMAY: " + mayCount + "\nJUN: " + juneCount
                                + "\nJUL: " + julyCount + "\nAUG: " + augustCount
                                + "\nSEP: " + septemberCount + "\nOCT: " + octoberCount
                                + "\nNOV: " + novemberCount + "\nDEC: " + decemberCount);
    }
    
        @FXML
    void NumberOfCustomersByCountryHandler(ActionEvent event) {
        String sqlStatement;
        Statement stmt;
        int USCustomers = 0;
        int UKCustomers = 0;
        int CanadaCustomers = 0;
        int NorwayCustomers = 0;

        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT country FROM country, customer, address, city WHERE customer.addressId = address.addressId AND address.cityId = city.cityId AND city.countryId = country.countryId;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
                String country = result.getString("country");
                
                if (country.equals("US"))
                    USCustomers = USCustomers + 1;
                if (country.equals("UK"))
                    UKCustomers = UKCustomers + 1;
                if (country.equals("Canada"))
                    CanadaCustomers = CanadaCustomers + 1;
                if (country.equals("Norway"))
                    NorwayCustomers = NorwayCustomers + 1;
            }    
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        Output2Display.clear();
        Output2Display.setText("Canada: " + CanadaCustomers + "\nNorway: " + NorwayCustomers + "\nUK: " + UKCustomers + "\nUS: " + USCustomers);
        
    }
    
    private void populateConsultantDropDown() {
        UserList.clear();
        String sqlStatement;
        Statement stmt;
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT userName FROM user;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
               UserList.add(result.getString("userName"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
        ConsultantDropDown.setItems(UserList);
    }
    
}
