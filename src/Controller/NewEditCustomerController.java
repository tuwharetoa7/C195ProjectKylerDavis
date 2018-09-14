package Controller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.io.IOException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import static Controller.ManageCustomerController.customerToModifyIndex;
import static Controller.ManageCustomerController.customerList;


import static main.DBConnection.*;
import Model.Customer;
import Model.Customer.*;
import main.QueryManager;

import javax.management.Query;

import static main.QueryManager.addressCityOnly;

public class NewEditCustomerController {
    @FXML
    private ResourceBundle resources;
    @FXML
    private TextField customerCityField;
    @FXML
    private TextField customerPostalCodeField;
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField customerPhoneField;
    @FXML
    private TextField customerAddressField;
    @FXML
    private TextField customerAddress2Field;
    @FXML
    private TextField customerCountryField;
    @FXML
    private Button customerSaveButton;
    @FXML
    private Button customerCancelButton;
    @FXML
    void CancelCustomer(ActionEvent event) throws IOException{
        Stage stage = (Stage) customerCancelButton.getScene().getWindow();
        Parent manage = FXMLLoader.load(getClass().getResource("../View/ManageCustomer.fxml"), resources);
        Scene scene = new Scene(manage);
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    void SaveCustomer(ActionEvent event) throws IOException, SQLException{
        String name = customerNameField.getText();
        String phone = customerPhoneField.getText();
        String address = customerAddressField.getText();
        String address2 = customerAddress2Field.getText();
        String city = customerCityField.getText();
        String postalCode = customerPostalCodeField.getText();
        String country = customerCountryField.getText();
        String validCustomer = Customer.validCustomer(name, address, postalCode,phone,city,country);
        boolean cityChange = false;
        boolean countryChange = false;
        if(validCustomer != null){
            Customer editCustomer = customerList.get(customerToModifyIndex);

            if (!name.equals(editCustomer.getCustomerName())) editCustomer.setCustomerName(name);
            if (!phone.equals(editCustomer.getPhone())) editCustomer.setPhone(phone);
            if (!address.equals(editCustomer.getAddress())) editCustomer.setAddress(address);
            if (!address2.equals(editCustomer.getAddress2())) editCustomer.setAddress2(address2);
            if (!city.equals(editCustomer.getCity())){ editCustomer.setCity(city); cityChange = true;}
            if (!postalCode.equals(editCustomer.getPostalCode())) editCustomer.setPostalCode(postalCode);
            if (!country.equals(editCustomer.getCountry())){ editCustomer.setCountry(country); countryChange = true;}

            System.out.println(cityChange);
            System.out.println(countryChange);
            if(cityChange  && countryChange == false) {
                int updateCity = addressCityOnly(editCustomer.getAddressId(), editCustomer.getCity(), editCustomer.getCountryId());
                System.out.println(updateCity);
            }else if(cityChange && countryChange){

            }

            Stage stage = (Stage) customerSaveButton.getScene().getWindow();
            Parent manage = FXMLLoader.load(getClass().getResource("../View/ManageCustomer.fxml"), resources);
            Scene scene = new Scene(manage);
            stage.setScene(scene);
            stage.show();
        }else{
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(resources.getString("new.errorTitle"));
            alert.setHeaderText(resources.getString("new.errorHeader"));
            alert.setContentText(validCustomer);
            alert.showAndWait();
        }
    }
    /*public String updateSaveFunction(Customer customerToSave, boolean cityChange, boolean countryChange) throws SQLException {
        if(cityChange == true && countryChange == false){
            int updateCity = addressCityOnly(customerToSave.getAddressId(),customerToSave.getCity(),customerToSave.getCountryId());
            System.out.println(updateCity);
        }

            //isUpdateCustomer


        return "";
    }*/
    public void initialize() {
        if(customerToModifyIndex != -1){
            try {
                Customer customerToModify = customerList.get(customerToModifyIndex);
                customerNameField.setText(customerToModify.getCustomerName());
                customerPhoneField.setText(customerToModify.getPhone());
                customerAddressField.setText(customerToModify.getAddress());
                customerAddress2Field.setText(customerToModify.getAddress2());
                customerCityField.setText(customerToModify.getCity());
                customerPostalCodeField.setText(customerToModify.getPostalCode());
                customerCountryField.setText(customerToModify.getCountry());
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Error loading customer");
            }
        }
    }
}
