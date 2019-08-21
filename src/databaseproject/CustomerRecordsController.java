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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob
 */
public class CustomerRecordsController implements Initializable {
    
    public static ObservableList<Customer> CustomerList = FXCollections.observableArrayList();
    
    public static ObservableList<Customer> selectedCustomer = FXCollections.observableArrayList();
    
    @FXML
    private TableView<Customer> CustomerTable;

    @FXML
    private TableColumn<Customer, String> customerName;

    @FXML
    private TableColumn<Customer, String> address;
    
    @FXML
    private TableColumn<Customer, String> city;

    @FXML
    private TableColumn<Customer, String> postalCode;

    @FXML
    private TableColumn<Customer, String> phone;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PopulateCustomerTable();
   
    }    
    
        @FXML
    void AddCustomerBtn(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("AddCustomer.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    void DeleteCustomerBtn(ActionEvent event) {
       selectedCustomer = CustomerTable.getSelectionModel().getSelectedItems();
       if (!(selectedCustomer.isEmpty())){
            Alert confirmDelete = new Alert(AlertType.CONFIRMATION);
            confirmDelete.setTitle("Confirm Delete");
            confirmDelete.setHeaderText("Are you sure you want to delete this customer?");
            Optional<ButtonType> result = confirmDelete.showAndWait();
            if (result.get() == ButtonType.OK){   
                for (int i = 0; i < CustomerList.size(); ++i){
                     if ((CustomerList.get(i).getCustomerID() == selectedCustomer.get(0).getCustomerID())){             
                         String sqlStatement;
                         Statement stmt;
                         try {
                             stmt = conn.createStatement();
                             sqlStatement = "DELETE FROM customer WHERE customerId = " + selectedCustomer.get(0).getCustomerID() + ";";
                             stmt.executeUpdate(sqlStatement);
                             sqlStatement = "DELETE FROM address WHERE address = \"" + selectedCustomer.get(0).getAddress() + "\";";
                             stmt.executeUpdate(sqlStatement);
                             sqlStatement = "DELETE FROM appointment WHERE customerId = \"" + selectedCustomer.get(0).getCustomerID() + "\";";
                             stmt.executeUpdate(sqlStatement);

                         } catch (SQLException ex) {
                             Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                             System.out.println("Error: " + ex);
                         }
                     }
                }
                CustomerList.clear();
                PopulateCustomerTable();
                CustomerTable.setItems(CustomerList);
                CustomerTable.refresh();
            }
        }
    }

    @FXML
    void ModifyCustomerBtn(ActionEvent event) throws IOException {
       selectedCustomer = CustomerTable.getSelectionModel().getSelectedItems();
       if (!(selectedCustomer.isEmpty())){
            Parent root = FXMLLoader.load(getClass().getResource("ModCustomer.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
       }
    }
    
        @FXML
    void BackBtnHandler(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void PopulateCustomerTable() {
        String sqlStatement;
        Statement stmt;
        CustomerList.clear();
        try {
            stmt = conn.createStatement();
            sqlStatement = "SELECT customerId, customerName, address, city, postalCode, phone "
                    + "FROM customer, address, city "
                    + "WHERE customer.addressId = address.addressId AND address.cityId = city.cityId  "
                    + "ORDER BY customerId;";
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next())
            {
               CustomerList.add(new Customer(result.getInt("customerId"),
                                             result.getString("customerName"),
                                             result.getString("address"),
                                             result.getString("city"),
                                             result.getString("postalCode"),
                                             result.getString("phone")));
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error: " + ex);
        }
            customerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            address.setCellValueFactory(new PropertyValueFactory<>("address"));
            city.setCellValueFactory(new PropertyValueFactory<>("city"));
            postalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
            phone.setCellValueFactory(new PropertyValueFactory<>("phone1"));
            CustomerTable.setItems(CustomerList);
            CustomerTable.refresh();
    }
}