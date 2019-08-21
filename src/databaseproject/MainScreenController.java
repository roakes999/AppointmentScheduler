/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import static databaseproject.DBConnection.conn;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Optional;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


/**
 * FXML Controller class
 *
 * @author Rob
 */
public class MainScreenController implements Initializable {
    
    public static ObservableList<Appointments> AppointmentList = FXCollections.observableArrayList();
    public static ObservableList<Appointments> selectedAppointment = FXCollections.observableArrayList();
    
    @FXML
    private TableView<Appointments> AppointmentTable;
    
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
    private RadioButton CurrentWeekRadioBtn;

    @FXML
    private RadioButton CurrentMonthRadioBtn;

    @FXML
    private RadioButton AllTimeRadioBtn;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CurrentWeekRadioBtn.setSelected(true);
        PopulateAppointmentTable();            
    }    
    
    @FXML
    void LogoutBtnHandler(ActionEvent event) throws IOException {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
    }
    
    @FXML
    void CustomerRecordsBtn(ActionEvent event) throws IOException {
            Parent root = FXMLLoader.load(getClass().getResource("CustomerRecords.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
    }
    
        @FXML
    void AddAppointmentBtn(ActionEvent event) throws IOException {
            Parent root = FXMLLoader.load(getClass().getResource("AddAppointment.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
    }

    @FXML
    void DeleteAppointmentBtn(ActionEvent event) {
        selectedAppointment = AppointmentTable.getSelectionModel().getSelectedItems();
        if (!(selectedAppointment.isEmpty())){   
            Alert confirmDelete = new Alert(AlertType.CONFIRMATION);
            confirmDelete.setTitle("Confirm Delete");
            confirmDelete.setHeaderText("Are you sure you want to delete this appointment?");
            Optional<ButtonType> result = confirmDelete.showAndWait();
        
        if (result.get() == ButtonType.OK){
            for (int i = 0; i < AppointmentList.size(); ++i){
                if ((AppointmentList.get(i).getAppointmentID() == selectedAppointment.get(0).getAppointmentID())){             
                    String sqlStatement;
                    Statement stmt;
                    try {
                        stmt = conn.createStatement();
                        sqlStatement = "DELETE FROM appointment WHERE appointmentId = " + selectedAppointment.get(0).getAppointmentID() + ";";
                        stmt.executeUpdate(sqlStatement);

                    } catch (SQLException ex) {
                        Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("Error: " + ex);
                    }
                }
            }
            AppointmentList.clear();
            PopulateAppointmentTable();
            AppointmentTable.setItems(AppointmentList);
            AppointmentTable.refresh();
       }
       } 
    }

    @FXML
    void ModAppointmentBtn(ActionEvent event) throws IOException {
       selectedAppointment = AppointmentTable.getSelectionModel().getSelectedItems();
       if (!(selectedAppointment.isEmpty())){
            Parent root = FXMLLoader.load(getClass().getResource("ModAppointment.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
       }
    }

    private void PopulateAppointmentTable() {
        
        String sqlStatement;
        Statement stmt;
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

                if (CurrentWeekRadioBtn.isSelected()){ // display current week appointments
                    Calendar c = Calendar.getInstance();
                    int currentDayInt = c.get(Calendar.DAY_OF_WEEK);
                    LocalDateTime currentDate = LocalDateTime.now();
                    LocalDateTime startOfWeekDate = currentDate.minusDays(currentDayInt - 1); //offset by one day for later comparison
                    LocalDateTime endOfWeekDate = currentDate.plusDays(8 - currentDayInt);    //offset by one day for later comparison
                    
                    if (startLocalDateTime.isAfter(startOfWeekDate) & endLocalDateTime.isBefore(endOfWeekDate)){
                        AppointmentList.add(new Appointments(result.getInt("appointmentId"),
                                                       startDateTimeZonedAdjusted.toString().substring(0, 10),
                                                       startDateTimeZonedAdjusted.toString().substring(11, 16),
                                                       endDateTimeZonedAdjusted.toString().substring(11, 16),
                                                       result.getString("customerName"),
                                                       result.getString("userName")));
                    }
                }
                else if (CurrentMonthRadioBtn.isSelected()) {   // display current month appointments
                    Calendar c = Calendar.getInstance();
                    int currentMonth = c.get(Calendar.MONTH);
                    
                    if (startLocalDateTime.getMonthValue() == currentMonth + 1){
                        AppointmentList.add(new Appointments(result.getInt("appointmentId"),
                                                       startDateTimeZonedAdjusted.toString().substring(0, 10),
                                                       startDateTimeZonedAdjusted.toString().substring(11, 16),
                                                       endDateTimeZonedAdjusted.toString().substring(11, 16),
                                                       result.getString("customerName"),
                                                       result.getString("userName")));
                    }
                }
                else if (AllTimeRadioBtn.isSelected()) {   // display all appointments in database
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
            AppointmentTable.setItems(AppointmentList);
            AppointmentTable.refresh();
    }
    
    @FXML
    void CurrentWeekRadioBtnHandler(ActionEvent event) {
        // Shows current week of Sunday through Saturday
        AppointmentList.clear();
        CurrentWeekRadioBtn.setSelected(true);
        CurrentMonthRadioBtn.setSelected(false);
        AllTimeRadioBtn.setSelected(false);
        PopulateAppointmentTable();
    }
    
    @FXML
    void CurrentMonthRadioBtnHandler(ActionEvent event) {
        // Shows current month
        AppointmentList.clear();
        CurrentMonthRadioBtn.setSelected(true);
        CurrentWeekRadioBtn.setSelected(false);
        AllTimeRadioBtn.setSelected(false);
        PopulateAppointmentTable();
    }
    
    @FXML
    void AllTimeRadioBtnHandler(ActionEvent event) {
        // Shows all appointments in database
        AppointmentList.clear();
        AllTimeRadioBtn.setSelected(true);
        CurrentMonthRadioBtn.setSelected(false);
        CurrentWeekRadioBtn.setSelected(false);
        PopulateAppointmentTable();
    }
    
    @FXML
    void ReportsBtn(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Reports.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}