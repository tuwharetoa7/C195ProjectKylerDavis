package main;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static main.DBConnection.conn;
import Model.Customer;
import Model.Appointment;

public class QueryManager{
    public static String loggedUser;
    public static int typeCount;

    public static String userValidation(String userName, String password)throws ParseException{
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT password FROM user WHERE userName = '" + userName + "'");
            if (!rs.next()) {
                return "notFound";
            } else {
                String userPass = rs.getString("password");
                if (userPass.equals(password)) {
                    loggedUser = userName;
                    return "authenticated";
                } else {
                    return "incorrectPassword";
                }
            }
        } catch (SQLException e) {
            System.out.println("Error querying for user information: " + e);
            return "error";
        }
    }
    public static String[] checkAppointmentsIncoming(String userName) throws ParseException{
        ObservableList<Appointment> appointmentList = getAppointmentTableView();
        Timestamp currentDateTime = new Timestamp(System.currentTimeMillis());
        Timestamp timeInFifteenMinutes = new Timestamp(System.currentTimeMillis()+15*60*1000);
        String[] arrayToReturn = new String[3];
        for(Appointment app: appointmentList){
            LocalDateTime appStartTime = app.getStartLocalDateTime();
            if(app.getUserName().equals(userName) && currentDateTime.toLocalDateTime().isBefore(appStartTime) && timeInFifteenMinutes.toLocalDateTime().isAfter(appStartTime)){
                arrayToReturn[0] = app.getTitle();
                arrayToReturn[1] = app.getCustomerName();
                arrayToReturn[2] = app.getStartLocalDateTime().toString();
                return arrayToReturn;
            }
        }
        return arrayToReturn;
    }
    public static ObservableList<Customer> getCustomerTableView(){
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        String allCustomer =
                "SELECT U04EE1.customer.customerid, U04EE1.customer.customerName, " +
                        "U04EE1.address.addressId, U04EE1.address.address, U04EE1.address.address2,  " +
                        "U04EE1.city.cityId, U04EE1.address.postalCode, U04EE1.address.phone, " +
                        "U04EE1.city.city, U04EE1.city.countryId, U04EE1.country.country, U04EE1.customer.active \n" +
                        "FROM U04EE1.customer \n" +
                        "JOIN U04EE1.address ON U04EE1.customer.addressid = U04EE1.address.addressid \n" +
                        "JOIN U04EE1.city ON U04EE1.address.cityid = U04EE1.city.cityid \n" +
                        "JOIN U04EE1.country ON U04EE1.city.countryid = U04EE1.country.countryid \n" +
                        "WHERE U04EE1.customer.active = 1;";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allCustomer);
            while (rs.next()) {
                Customer current = new Customer();
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setCustomerName(rs.getString("customerName"));
                current.setAddressId(Integer.parseInt((rs.getString("addressId"))));
                current.setAddress(rs.getString("address"));
                current.setAddress2(rs.getString("address2"));
                current.setCityId(Integer.parseInt((rs.getString("cityId"))));
                current.setPostalCode(rs.getString("postalCode"));
                current.setPhone(rs.getString("phone"));
                current.setCity(rs.getString("city"));
                current.setCountryId(Integer.parseInt((rs.getString("countryId"))));
                current.setCountry(rs.getString("country"));
                current.setActive(Integer.parseInt((rs.getString("active"))));
                customerList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + allCustomer);
        }
        return customerList;
    }
    public static int deleteTheCustomer(int customerToDelete) throws SQLException{
        int rows;
        String customerToDeleteQuery = "UPDATE U04EE1.customer \n" +
                "SET U04EE1.customer.active = 0, U04EE1.customer.lastUpdate = NOW(), U04EE1.customer.lastUpdateBy = '" + loggedUser +"' \n" +
                "WHERE U04EE1.customer.customerId = " + customerToDelete + ";";
        try {
            Statement stmt = conn.createStatement();
            rows = stmt.executeUpdate(customerToDeleteQuery);
        }catch(SQLException e){
            System.out.println("Error deleting customer:" + e + customerToDeleteQuery);
            rows = 0;
        }
        return rows;
    }
    public static int addressUpdate(int newCityId, int addressId){
        int rows=0;
        String addressUpdate =
                "UPDATE U04EE1.address \n" +
                "SET U04EE1.address.cityId = " + newCityId + ",U04EE1.address.lastUpdate = NOW(), U04EE1.address.lastUpdateBy = '" + loggedUser + "' \n" +
                "WHERE U04EE1.address.addressId = " + addressId + ";";
        try {
            Statement addressUpdateStmt = conn.createStatement();
            rows = addressUpdateStmt.executeUpdate(addressUpdate);

        }catch(SQLException e){
            System.out.println("Error updating address table: " + e + addressUpdate);
            rows =  -1;
        }
        return rows;
    }

    public static int insertCity(String city, int countryId){
        int newCityId = 0;
        String checkCity =
                "SELECT U04EE1.city.cityId \n" +
                        "FROM U04EE1.city \n" +
                        "WHERE U04EE1.city.city = '" + city + "';";
        String insertNewCity =
                "INSERT INTO U04EE1.city (city,countryId,createDate,createdBy,lastUpdate,lastUpdateBy) \n" +
                        "VALUES ('" + city + "'," + countryId + ",NOW(),'" + loggedUser + "',NOW(),'" + loggedUser + "');";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(checkCity);
            if(!rs.next()){
                try{
                    Statement insertStmt = conn.createStatement();
                    int insertRs = insertStmt.executeUpdate(insertNewCity);
                    if (insertRs == 0) {
                        //insert didn't work
                        System.out.println("Failed to insert data, no rows were updated");
                        newCityId = -1;
                    } else {
                        try {
                            Statement selectStmt = conn.createStatement();
                            ResultSet selectRs = selectStmt.executeQuery(checkCity);
                            selectRs.next();
                            newCityId = selectRs.getInt("cityId");
                        }catch(SQLException e){
                            System.out.println("Failed to get new city id: " +e);
                            newCityId = -1;
                        }
                    }
                }catch(SQLException e){
                    System.out.println("City insert was unsuccessful:" +e);
                    newCityId = -1;
                }
            }else{
                //found, need to return id
                newCityId = rs.getInt("cityId");
            }
        }catch(SQLException e){
            System.out.println("Failed to check for existing city: " + e + checkCity + insertNewCity);
            newCityId = -1;
        }
        return newCityId;
    }
    public static int insertCountry(String country){
        int newCountryId = 0;
        String checkCountry =
                "SELECT U04EE1.country.countryId \n" +
                "FROM U04EE1.country \n" +
                "WHERE U04EE1.country.country = '" + country + "';";
        String insertNewCountry =
                "INSERT INTO U04EE1.country (country,createDate,createdBy,lastUpdate,lastUpdateBy) \n" +
                "VALUES ('" + country + "',NOW(),'" + loggedUser + "',NOW(),'" + loggedUser + "');";
            try{
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkCountry);
                if(!rs.next()){
                    try {
                        //not found insert
                        Statement insertStmt = conn.createStatement();
                        int insertRs = insertStmt.executeUpdate(insertNewCountry);
                        if (insertRs == 0) {
                            //insert didn't work
                            System.out.println("Failed to insert data, no rows were updated");
                            newCountryId = -1;
                        } else {
                            try {
                                Statement selectStmt = conn.createStatement();
                                ResultSet selectRs = selectStmt.executeQuery(checkCountry);
                                selectRs.next();
                                newCountryId = selectRs.getInt("countryId");
                            }catch(SQLException e){
                                System.out.println("Failed to get new country id: " +e);
                                newCountryId = -1;
                            }
                        }
                    }catch(SQLException e){
                        System.out.println("Country insert was unsuccessful:" +e);
                        newCountryId = -1;
                    }
                }else{
                    //found, need to return id
                    newCountryId = rs.getInt("countryId");
                }
            }catch(SQLException e){
                System.out.println("Failed to check for existing country: " + e + checkCountry + insertNewCountry);
                newCountryId = -1;
            }
            return newCountryId;
    }
    public static int updateAddressTable(int addressId, String phone, String address, String address2, String postalCode){
        int rows = 0;
        String updateAddress =
                "UPDATE U04EE1.address \n" +
                "SET U04EE1.address.phone = '" + phone + "',U04EE1.address.address = '" + address + "',U04EE1.address.address2 = '" + address2 + "',U04EE1.address.postalCode = '" + postalCode + "',U04EE1.address.lastUpdate = NOW(), U04EE1.address.lastUpdateBy =  '" + loggedUser + "'\n" +
                "WHERE U04EE1.address.addressId = " + addressId + ";";
        try {
            Statement existCityAddressUpdate = conn.createStatement();
            rows = existCityAddressUpdate.executeUpdate(updateAddress);
            return rows;
        }catch(SQLException e){
            System.out.println("Failed to update address table: " + e + updateAddress);
            return -1;
        }
    }
    public static int updateCustomerTable(String name, int customerId){
        int rows = 0;
        String updateCustomerName =
                "UPDATE U04EE1.customer \n" +
                "SET U04EE1.customer.customerName = '" + name + "',U04EE1.customer.lastUpdate = NOW(), U04EE1.customer.lastUpdateBy =  '" + loggedUser + "'\n" +
                "WHERE U04EE1.customer.customerId = " + customerId + ";";
        try {
            Statement existCityAddressUpdate = conn.createStatement();
            rows = existCityAddressUpdate.executeUpdate(updateCustomerName);
            return rows;
        }catch(SQLException e){
            System.out.println("Failed to update customer table: " + e + updateCustomerName);
            return -1;
        }
    }
    public static int insertAddress(String address,String address2,int cityId,String postalCode,String phone){
        int newAddressId = 0;
        String insertAddress =
                "INSERT INTO U04EE1.address(address,address2,cityId,postalCode,phone,createDate,createdBy,lastUpdate,lastUpdateBy) \n" +
                "VALUES ('" +address+ "','" +address2+ "',"+cityId+",'" +postalCode+"','"+phone+"',NOW(),'"+loggedUser+"',NOW(),'" +loggedUser+"');";
        String checkNewAddress =
                "SELECT U04EE1.address.addressId \n" +
                "FROM U04EE1.address \n" +
                "WHERE U04EE1.address.address ='" + address +"';";
        try{
            Statement insertStmt = conn.createStatement();
            int insertRs = insertStmt.executeUpdate(insertAddress);
            if (insertRs == 0) {
                System.out.println("Failed to insert data, no rows were updated");
                newAddressId = -1;
            } else {
                try {
                    Statement selectStmt = conn.createStatement();
                    ResultSet selectRs = selectStmt.executeQuery(checkNewAddress);
                    selectRs.next();
                    newAddressId = selectRs.getInt("addressId");
                }catch(SQLException e){
                    System.out.println("Failed to get new country id: " +e);
                    newAddressId = -1;
                }
            }
        }catch(SQLException e){
            System.out.println("Address insert was unsuccessful:" + e + insertAddress + checkNewAddress);
            newAddressId = -1;
        }
        return newAddressId;
    }
    public static int insertCustomer(String customerName,int addressId){
        int rows = 0;
        String insertAddress =
                "INSERT INTO U04EE1.customer(customerName,addressId,active,createDate,createdBy,lastUpdate,lastUpdateBy) \n" +
                "VALUES ('" +customerName+ "','" +addressId+ "',1,NOW(),'"+loggedUser+"',NOW(),'" +loggedUser+"');";
        try{
            Statement insertStmt = conn.createStatement();
            int insertRs = insertStmt.executeUpdate(insertAddress);
            if (insertRs == 0) {
                System.out.println("Failed to insert data, no rows were updated");
                rows = -1;
            } else {
                return insertRs;
            }
        }catch(SQLException e){
            System.out.println("Customer Insert was unsuccessful:" + e + insertAddress);
            rows = -1;
        }
        return rows;
    }
    public static ObservableList<Appointment> getAppointmentTableView() throws ParseException {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        String allAppointment =
            "SELECT U04EE1.appointment.appointmentid, U04EE1.user.userName, U04EE1.appointment.customerId, U04EE1.appointment.userid, " +
            "U04EE1.appointment.title, U04EE1.customer.customerName, U04EE1.appointment.contact, U04EE1.appointment.type," +
            "U04EE1.appointment.description,  U04EE1.appointment.location, U04EE1.appointment.url, " +
            "U04EE1.appointment.start, U04EE1.appointment.end \n" +
            "FROM U04EE1.appointment \n" +
            "JOIN U04EE1.customer ON U04EE1.appointment.customerId = U04EE1.customer.customerId \n" +
            "JOIN U04EE1.user ON U04EE1.appointment.userId = U04EE1.user.userId \n" +
            "WHERE U04EE1.appointment.end > NOW();";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allAppointment);
            while (rs.next()) {
                Appointment current = new Appointment();
                current.setAppointmentId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("appointmentId"))));
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setUserId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("userId"))));

                current.setUserName(rs.getString("userName"));
                current.setTitle(rs.getString("title"));
                current.setCustomerName(rs.getString("customerName"));
                current.setType(rs.getString("type"));
                current.setContact(rs.getString("contact"));
                current.setDescription(rs.getString("description"));
                current.setLocation(rs.getString("location"));
                current.setUrl(rs.getString("url"));

                current.setStart(rs.getTimestamp("start"));
                current.setEnd(rs.getTimestamp("end"));

                DateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                dt.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date startDate = dt.parse(rs.getTimestamp("Start").toString());
                Date endDate = dt.parse(rs.getTimestamp("end").toString());

                DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                String startDateAsString = dateFormatter.format(startDate);
                String startTimeAsString = timeFormatter.format(startDate);

                String endDateAsString = dateFormatter.format(endDate);
                String endTimeAsString = timeFormatter.format(endDate);

                String startYearString = startDateAsString.substring(0,4);
                String startMonthString = startDateAsString.substring(5,7);
                String startDayString = startDateAsString.substring(8,10);
                String endYearString = endDateAsString.substring(0,4);
                String endMonthString = endDateAsString.substring(5,7);
                String endDayString = endDateAsString.substring(8,10);
                String startHourString = startTimeAsString.substring(0,2);
                String startMinuteString = startTimeAsString.substring(3,5);
                String endHourString = endTimeAsString.substring(0,2);
                String endMinuteString = endTimeAsString.substring(3,5);
                String startPeriod;
                String endPeriod;

                if(Integer.parseInt(startHourString) >= 12){
                    if(Integer.parseInt(startHourString) == 12){
                        startPeriod ="PM";
                    }else {
                        startHourString = Integer.toString(Integer.parseInt(startHourString) - 12);
                        startPeriod = "PM";
                    }
                }else{
                    startPeriod = "AM";
                }
                if(Integer.parseInt(endHourString) >= 12){
                    if(Integer.parseInt(endHourString) == 12){
                        endPeriod = "PM";
                    }else {
                        endHourString = Integer.toString(Integer.parseInt(endHourString) - 12);
                        endPeriod = "PM";
                    }
                }else{
                    endPeriod = "AM";
                }
                String wholeStartString = startMonthString + "/" + startDayString + "/" + startYearString +
                        " " + startHourString + ":" + startMinuteString + " " + startPeriod;
                String wholeEndString = endMonthString + "/" + endDayString + "/" + endYearString +
                        " " + endHourString + ":" + endMinuteString + " " + endPeriod;

                current.setStartDate(wholeStartString);
                current.setEndDate(wholeEndString);
                LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

                String startDay = startLocalDateTime.getDayOfWeek().toString().toLowerCase();
                String endDay = endLocalDateTime.getDayOfWeek().toString().toLowerCase();
                current.setStartLocalDateTime(startLocalDateTime);
                current.setEndLocalDateTime(endLocalDateTime);

                current.setStartDayOfWeek(startDay.substring(0, 1).toUpperCase() + startDay.substring(1));
                current.setEndDayOfWeek(endDay.substring(0,1).toUpperCase() + endDay.substring(1));
                appointmentList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + allAppointment);
        }
        return appointmentList;
    }
    public static boolean appointmentOverlaps(int appointmentIndex, Timestamp newStartTime, Timestamp newEndTime, ObservableList<Appointment> appointmentListInput)throws ParseException{
        ObservableList<Appointment> appointmentList = appointmentListInput;
        if(appointmentIndex != -1){
            appointmentList.remove(appointmentIndex);
        }
        for(Appointment app: appointmentList){
            Timestamp appStartTime = app.getStart();
            Timestamp appEndTime = app.getEnd();
            if(appStartTime.equals(newStartTime)){
                return true;
            }else if(newStartTime.after(appStartTime) && newStartTime.before(appEndTime)){
                return true;
            }else if(newStartTime.before(appStartTime) && newEndTime.after(appStartTime)){
                return true;
            }else if(newStartTime.after(appStartTime) && newStartTime.before(appEndTime)){
                return true;
            }else if(newEndTime.after(appStartTime)  && newEndTime.before(appEndTime)){
                return true;
            }
        }
        return false;
    }

    public static ObservableList<String> getCustomerNames(){
        ObservableList<String> allCustomersList  = FXCollections.observableArrayList();
        String allCustomers =
            "SELECT U04EE1.customer.customerName, U04EE1.customer.customerId\n" +
            "FROM U04EE1.customer \n" +
            "WHERE U04EE1.customer.active = 1;";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allCustomers);
            while(rs.next()){
                allCustomersList.add(rs.getString("customerName"));
            }
            return allCustomersList;
        }catch(SQLException e){
            System.out.println("Error getting user name data: " + e + allCustomers);
            return null;
        }
    }
    public static ObservableList<String> getUserNames(){
        ObservableList<String> allUsersList  = FXCollections.observableArrayList();
        String allUsers =
                        "SELECT U04EE1.user.userName \n" +
                        "FROM U04EE1.user \n" +
                        "WHERE U04EE1.user.active = 1;";
        try{

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allUsers);
            while(rs.next()){
                allUsersList.add(rs.getString("userName"));
            }
            return allUsersList;
        }catch(SQLException e){
            System.out.println("Error getting user name data: " + e + allUsers);
            return null;
        }
    }
    public static ObservableList<String> getHourDrops(){
        ObservableList<String> allHoursList = FXCollections.observableArrayList();
        allHoursList.add("01");
        allHoursList.add("02");
        allHoursList.add("03");
        allHoursList.add("04");
        allHoursList.add("05");
        allHoursList.add("06");
        allHoursList.add("07");
        allHoursList.add("08");
        allHoursList.add("09");
        allHoursList.add("10");
        allHoursList.add("11");
        allHoursList.add("12");
        return allHoursList;
    }
    public static ObservableList<String> getMinuteDrops(){
        ObservableList<String> allMinutesList = FXCollections.observableArrayList();
        allMinutesList.add("00");
        allMinutesList.add("15");
        allMinutesList.add("30");
        allMinutesList.add("45");
        return allMinutesList;
    }
    public static ObservableList<String> getPeriodDrops(){
        ObservableList<String> allPeriodsList = FXCollections.observableArrayList();
        allPeriodsList.add("AM");
        allPeriodsList.add("PM");
        return allPeriodsList;
    }
    public static ObservableList<String> getTypeDrops(){
        ObservableList<String> allTypesList  = FXCollections.observableArrayList();
        String allTypes =
                "SELECT DISTINCT U04EE1.appointment.type \n" +
                        "FROM U04EE1.appointment;";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allTypes);
            while(rs.next()){
                allTypesList.add(rs.getString("type"));
            }
            return allTypesList;
        }catch(SQLException e){
            System.out.println("Error getting user name data: " + e + allTypes);
            return null;
        }
    }
    public static int insertAppointment(int customerId, int userId, String title, String description, String location, String contact, String type, String url, Timestamp startTime, Timestamp endTime){
        int rows = 0;
        String insertAppointment =
                        "INSERT INTO U04EE1.appointment(customerId,userId,title,description,location,contact,type,url,start,end,createDate,createdBy,lastUpdate,lastUpdateBy) \n" +
                        "VALUES ("+customerId+","+userId+",'"+title+"','"+description+"','"+location+"','"+contact+"','"+type+"','"+url+"','"+startTime+"','"+endTime+"',NOW(),'"+loggedUser+"',NOW(),'"+loggedUser+"');";
        try{
            Statement insertStmt = conn.createStatement();
            rows = insertStmt.executeUpdate(insertAppointment);
            if (rows == 0) {
                //insert didn't work
                System.out.println("Failed to insert data, no rows were updated");
                rows = -1;
            } else {
                return rows;
            }
        }catch(SQLException e){
            System.out.println("Customer Insert was unsuccessful:" + e + insertAppointment);
            rows = -1;
        }
        return rows;
    }
    public static int updateAppointment(int appointmentId, int customerId, int userId, String title, String description, String location, String contact, String type, String url, Timestamp startTime, Timestamp endTime){
        int rows = 0;
        String updateAppointment =
                        "UPDATE U04EE1.appointment \n" +
                        "SET U04EE1.appointment.customerId = '"+customerId+"',U04EE1.appointment.userId = '"+userId+"',U04EE1.appointment.title='"+title+"'," +
                        "U04EE1.appointment.description='"+description+"',U04EE1.appointment.location='"+location+"',U04EE1.appointment.contact='"+contact+"'," +
                        "U04EE1.appointment.type='"+type+"',U04EE1.appointment.url='"+url+"',U04EE1.appointment.start='"+startTime+"',U04EE1.appointment.end='"+endTime+"'," +
                        "U04EE1.appointment.lastUpdate=NOW(),U04EE1.appointment.lastUpdateBy='"+loggedUser+"' \n" +
                        "WHERE U04EE1.appointment.appointmentId = "+appointmentId+";";
        try{
            Statement updateStmt = conn.createStatement();
            rows = updateStmt.executeUpdate(updateAppointment);
            if (rows == 0) {
                //update didn't work
                System.out.println("Failed to update data, no rows were updated");
                rows = -1;
            } else {
                return rows;
            }
        }catch(SQLException e){
            System.out.println("Customer Insert was unsuccessful:" + e + updateAppointment);
            rows = -1;
        }
        return rows;
    }
    public static int deleteTheAppointment(int appointmentId){
        int rows;
        String deleteAppointment =
                "DELETE FROM U04EE1.appointment \n" +
                "WHERE U04EE1.appointment.appointmentId = '" + appointmentId + "';";
        try{
            Statement deleteStmt = conn.createStatement();
            rows = deleteStmt.executeUpdate(deleteAppointment);
        }catch(SQLException e){
            System.out.println("Appointment delete was unsuccessful" + e + deleteAppointment);
            rows = -1;
        }
        return rows;
    }
    public static ObservableList<Appointment> getAppointmentsByMonth() throws ParseException {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        String monthAppointments =
                "SELECT U04EE1.appointment.appointmentid, U04EE1.user.userName, U04EE1.appointment.customerId, U04EE1.appointment.userid, " +
                        "U04EE1.appointment.title, U04EE1.customer.customerName, U04EE1.appointment.contact, U04EE1.appointment.type," +
                        "U04EE1.appointment.description,  U04EE1.appointment.location, U04EE1.appointment.url, " +
                        "U04EE1.appointment.start, U04EE1.appointment.end \n" +
                        "FROM U04EE1.appointment \n" +
                        "JOIN U04EE1.customer ON U04EE1.appointment.customerId = U04EE1.customer.customerId \n" +
                        "JOIN U04EE1.user ON U04EE1.appointment.userId = U04EE1.user.userId \n" +
                        "WHERE U04EE1.appointment.start >  DATE_ADD(DATE_ADD(LAST_DAY(CURDATE()), INTERVAL 1 DAY),\n" +
                        "INTERVAL - 1 MONTH) AND U04EE1.appointment.start < LAST_DAY(CURDATE());";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(monthAppointments);
            while (rs.next()) {
                Appointment current = new Appointment();
                current.setAppointmentId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("appointmentId"))));
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setUserId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("userId"))));

                current.setUserName(rs.getString("userName"));
                current.setTitle(rs.getString("title"));
                current.setCustomerName(rs.getString("customerName"));
                current.setType(rs.getString("type"));
                current.setContact(rs.getString("contact"));
                current.setDescription(rs.getString("description"));
                current.setLocation(rs.getString("location"));
                current.setUrl(rs.getString("url"));

                current.setStart(rs.getTimestamp("start"));
                current.setEnd(rs.getTimestamp("end"));

                DateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                dt.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date startDate = dt.parse(rs.getTimestamp("Start").toString());
                Date endDate = dt.parse(rs.getTimestamp("end").toString());

                DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                String startDateAsString = dateFormatter.format(startDate);
                String startTimeAsString = timeFormatter.format(startDate);

                String endDateAsString = dateFormatter.format(endDate);
                String endTimeAsString = timeFormatter.format(endDate);

                String startYearString = startDateAsString.substring(0,4);
                String startMonthString = startDateAsString.substring(5,7);
                String startDayString = startDateAsString.substring(8,10);
                String endYearString = endDateAsString.substring(0,4);
                String endMonthString = endDateAsString.substring(5,7);
                String endDayString = endDateAsString.substring(8,10);
                String startHourString = startTimeAsString.substring(0,2);
                String startMinuteString = startTimeAsString.substring(3,5);
                String endHourString = endTimeAsString.substring(0,2);
                String endMinuteString = endTimeAsString.substring(3,5);
                String startPeriod;
                String endPeriod;

                if(Integer.parseInt(startHourString) > 12){
                    startHourString = Integer.toString(Integer.parseInt(startHourString)-12);
                    startPeriod ="PM";
                }else{
                    startPeriod = "AM";
                }
                if(Integer.parseInt(endHourString) > 12){
                    endHourString = Integer.toString(Integer.parseInt(endHourString)-12);
                    endPeriod = "PM";
                }else{
                    endPeriod = "AM";
                }
                String wholeStartString = startMonthString + "/" + startDayString + "/" + startYearString +
                        " " + startHourString + ":" + startMinuteString + " " + startPeriod;
                String wholeEndString = endMonthString + "/" + endDayString + "/" + endYearString +
                        " " + endHourString + ":" + endMinuteString + " " + endPeriod;

                current.setStartDate(wholeStartString);
                current.setEndDate(wholeEndString);
                LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

                String startDay = startLocalDateTime.getDayOfWeek().toString().toLowerCase();
                String endDay = endLocalDateTime.getDayOfWeek().toString().toLowerCase();
                current.setStartLocalDateTime(startLocalDateTime);
                current.setEndLocalDateTime(endLocalDateTime);

                current.setStartDayOfWeek(startDay.substring(0, 1).toUpperCase() + startDay.substring(1));
                current.setEndDayOfWeek(endDay.substring(0,1).toUpperCase() + endDay.substring(1));
                appointmentList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + monthAppointments);
        }
        return appointmentList;
    }
    public static ObservableList<Appointment> getAppointmentsByWeek() throws ParseException {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        String monthAppointments =
                "SELECT U04EE1.appointment.appointmentid, U04EE1.user.userName, U04EE1.appointment.customerId, U04EE1.appointment.userid, " +
                        "U04EE1.appointment.title, U04EE1.customer.customerName, U04EE1.appointment.contact, U04EE1.appointment.type," +
                        "U04EE1.appointment.description,  U04EE1.appointment.location, U04EE1.appointment.url, " +
                        "U04EE1.appointment.start, U04EE1.appointment.end \n" +
                        "FROM U04EE1.appointment \n" +
                        "JOIN U04EE1.customer ON U04EE1.appointment.customerId = U04EE1.customer.customerId \n" +
                        "JOIN U04EE1.user ON U04EE1.appointment.userId = U04EE1.user.userId \n" +
                        "WHERE YEARWEEK(U04EE1.appointment.start) = YEARWEEK(CURDATE());";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(monthAppointments);
            while (rs.next()) {
                Appointment current = new Appointment();
                current.setAppointmentId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("appointmentId"))));
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setUserId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("userId"))));

                current.setUserName(rs.getString("userName"));
                current.setTitle(rs.getString("title"));
                current.setCustomerName(rs.getString("customerName"));
                current.setType(rs.getString("type"));
                current.setContact(rs.getString("contact"));
                current.setDescription(rs.getString("description"));
                current.setLocation(rs.getString("location"));
                current.setUrl(rs.getString("url"));

                current.setStart(rs.getTimestamp("start"));
                current.setEnd(rs.getTimestamp("end"));

                DateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                dt.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date startDate = dt.parse(rs.getTimestamp("Start").toString());
                Date endDate = dt.parse(rs.getTimestamp("end").toString());

                DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                String startDateAsString = dateFormatter.format(startDate);
                String startTimeAsString = timeFormatter.format(startDate);

                String endDateAsString = dateFormatter.format(endDate);
                String endTimeAsString = timeFormatter.format(endDate);

                String startYearString = startDateAsString.substring(0,4);
                String startMonthString = startDateAsString.substring(5,7);
                String startDayString = startDateAsString.substring(8,10);
                String endYearString = endDateAsString.substring(0,4);
                String endMonthString = endDateAsString.substring(5,7);
                String endDayString = endDateAsString.substring(8,10);
                String startHourString = startTimeAsString.substring(0,2);
                String startMinuteString = startTimeAsString.substring(3,5);
                String endHourString = endTimeAsString.substring(0,2);
                String endMinuteString = endTimeAsString.substring(3,5);
                String startPeriod;
                String endPeriod;

                if(Integer.parseInt(startHourString) > 12){
                    startHourString = Integer.toString(Integer.parseInt(startHourString)-12);
                    startPeriod ="PM";
                }else{
                    startPeriod = "AM";
                }
                if(Integer.parseInt(endHourString) > 12){
                    endHourString = Integer.toString(Integer.parseInt(endHourString)-12);
                    endPeriod = "PM";
                }else{
                    endPeriod = "AM";
                }
                String wholeStartString = startMonthString + "/" + startDayString + "/" + startYearString +
                        " " + startHourString + ":" + startMinuteString + " " + startPeriod;
                String wholeEndString = endMonthString + "/" + endDayString + "/" + endYearString +
                        " " + endHourString + ":" + endMinuteString + " " + endPeriod;

                current.setStartDate(wholeStartString);
                current.setEndDate(wholeEndString);
                LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

                String startDay = startLocalDateTime.getDayOfWeek().toString().toLowerCase();
                String endDay = endLocalDateTime.getDayOfWeek().toString().toLowerCase();
                current.setStartLocalDateTime(startLocalDateTime);
                current.setEndLocalDateTime(endLocalDateTime);

                current.setStartDayOfWeek(startDay.substring(0, 1).toUpperCase() + startDay.substring(1));
                current.setEndDayOfWeek(endDay.substring(0,1).toUpperCase() + endDay.substring(1));
                appointmentList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + monthAppointments);
        }
        return appointmentList;
    }
    public static ObservableList<Customer> getInactiveCustomers(){
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        String allCustomer =
                "SELECT U04EE1.customer.customerid, U04EE1.customer.customerName, " +
                        "U04EE1.address.addressId, U04EE1.address.address, U04EE1.address.address2,  " +
                        "U04EE1.city.cityId, U04EE1.address.postalCode, U04EE1.address.phone, " +
                        "U04EE1.city.city, U04EE1.city.countryId, U04EE1.country.country, U04EE1.customer.active \n" +
                        "FROM U04EE1.customer \n" +
                        "JOIN U04EE1.address ON U04EE1.customer.addressid = U04EE1.address.addressid \n" +
                        "JOIN U04EE1.city ON U04EE1.address.cityid = U04EE1.city.cityid \n" +
                        "JOIN U04EE1.country ON U04EE1.city.countryid = U04EE1.country.countryid \n" +
                        "WHERE U04EE1.customer.active = 0;";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allCustomer);
            while (rs.next()) {
                Customer current = new Customer();
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setCustomerName(rs.getString("customerName"));
                current.setAddressId(Integer.parseInt((rs.getString("addressId"))));
                current.setAddress(rs.getString("address"));
                current.setAddress2(rs.getString("address2"));
                current.setCityId(Integer.parseInt((rs.getString("cityId"))));
                current.setPostalCode(rs.getString("postalCode"));
                current.setPhone(rs.getString("phone"));
                current.setCity(rs.getString("city"));
                current.setCountryId(Integer.parseInt((rs.getString("countryId"))));
                current.setCountry(rs.getString("country"));
                current.setActive(Integer.parseInt((rs.getString("active"))));
                customerList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + allCustomer);
        }
        return customerList;
    }

    public static ObservableList<Appointment> getAppointmentsByConsultant(int consultantId) throws ParseException {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        String allAppointment =
                "SELECT U04EE1.appointment.appointmentid, U04EE1.user.userName, U04EE1.appointment.customerId, U04EE1.appointment.userid, " +
                        "U04EE1.appointment.title, U04EE1.customer.customerName, U04EE1.appointment.contact, U04EE1.appointment.type," +
                        "U04EE1.appointment.description,  U04EE1.appointment.location, U04EE1.appointment.url, " +
                        "U04EE1.appointment.start, U04EE1.appointment.end \n" +
                        "FROM U04EE1.appointment \n" +
                        "JOIN U04EE1.customer ON U04EE1.appointment.customerId = U04EE1.customer.customerId \n" +
                        "JOIN U04EE1.user ON U04EE1.appointment.userId = U04EE1.user.userId \n" +
                        "WHERE U04EE1.appointment.userId = " + consultantId + ";";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allAppointment);
            while (rs.next()) {
                Appointment current = new Appointment();
                current.setAppointmentId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("appointmentId"))));
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setUserId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("userId"))));

                current.setUserName(rs.getString("userName"));
                current.setTitle(rs.getString("title"));
                current.setCustomerName(rs.getString("customerName"));
                current.setType(rs.getString("type"));
                current.setContact(rs.getString("contact"));
                current.setDescription(rs.getString("description"));
                current.setLocation(rs.getString("location"));
                current.setUrl(rs.getString("url"));

                current.setStart(rs.getTimestamp("start"));
                current.setEnd(rs.getTimestamp("end"));

                DateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                dt.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date startDate = dt.parse(rs.getTimestamp("Start").toString());
                Date endDate = dt.parse(rs.getTimestamp("end").toString());

                DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                String startDateAsString = dateFormatter.format(startDate);
                String startTimeAsString = timeFormatter.format(startDate);

                String endDateAsString = dateFormatter.format(endDate);
                String endTimeAsString = timeFormatter.format(endDate);

                String startYearString = startDateAsString.substring(0,4);
                String startMonthString = startDateAsString.substring(5,7);
                String startDayString = startDateAsString.substring(8,10);
                String endYearString = endDateAsString.substring(0,4);
                String endMonthString = endDateAsString.substring(5,7);
                String endDayString = endDateAsString.substring(8,10);
                String startHourString = startTimeAsString.substring(0,2);
                String startMinuteString = startTimeAsString.substring(3,5);
                String endHourString = endTimeAsString.substring(0,2);
                String endMinuteString = endTimeAsString.substring(3,5);
                String startPeriod;
                String endPeriod;

                if(Integer.parseInt(startHourString) > 12){
                    startHourString = Integer.toString(Integer.parseInt(startHourString)-12);
                    startPeriod ="PM";
                }else{
                    startPeriod = "AM";
                }
                if(Integer.parseInt(endHourString) > 12){
                    endHourString = Integer.toString(Integer.parseInt(endHourString)-12);
                    endPeriod = "PM";
                }else{
                    endPeriod = "AM";
                }
                String wholeStartString = startMonthString + "/" + startDayString + "/" + startYearString +
                        " " + startHourString + ":" + startMinuteString + " " + startPeriod;
                String wholeEndString = endMonthString + "/" + endDayString + "/" + endYearString +
                        " " + endHourString + ":" + endMinuteString + " " + endPeriod;

                current.setStartDate(wholeStartString);
                current.setEndDate(wholeEndString);
                LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

                String startDay = startLocalDateTime.getDayOfWeek().toString().toLowerCase();
                String endDay = endLocalDateTime.getDayOfWeek().toString().toLowerCase();
                current.setStartLocalDateTime(startLocalDateTime);
                current.setEndLocalDateTime(endLocalDateTime);

                current.setStartDayOfWeek(startDay.substring(0, 1).toUpperCase() + startDay.substring(1));
                current.setEndDayOfWeek(endDay.substring(0,1).toUpperCase() + endDay.substring(1));
                appointmentList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + allAppointment);
        }
        return appointmentList;
    }
    public static int getUserIdWithUserName(String userName){
        int userId;
        String getSpecificUser =
                        "SELECT U04EE1.user.userId \n" +
                        "FROM U04EE1.user \n" +
                        "WHERE U04EE1.user.userName = '" + userName +"';";
        try{

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(getSpecificUser);
            if(rs.next()){
                userId = rs.getInt("userId");
            }else{
                userId = -1;
            }
        }catch(SQLException e){
            System.out.println("Error getting user Id data: " + e + getSpecificUser);
            userId = -1;
        }

        return userId;
    }
    public static ObservableList<Appointment> getAppointmentsByType(String type) throws ParseException {
        typeCount = 0;
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        String allAppointment =
                "SELECT U04EE1.appointment.appointmentid, U04EE1.user.userName, U04EE1.appointment.customerId, U04EE1.appointment.userid, " +
                        "U04EE1.appointment.title, U04EE1.customer.customerName, U04EE1.appointment.contact, U04EE1.appointment.type," +
                        "U04EE1.appointment.description,  U04EE1.appointment.location, U04EE1.appointment.url, " +
                        "U04EE1.appointment.start, U04EE1.appointment.end \n" +
                        "FROM U04EE1.appointment \n" +
                        "JOIN U04EE1.customer ON U04EE1.appointment.customerId = U04EE1.customer.customerId \n" +
                        "JOIN U04EE1.user ON U04EE1.appointment.userId = U04EE1.user.userId \n" +
                        "WHERE U04EE1.appointment.type = '" + type + "';";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(allAppointment);
            while (rs.next()) {
                typeCount = typeCount +1;
                Appointment current = new Appointment();
                current.setAppointmentId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("appointmentId"))));
                current.setCustomerId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("customerId"))));
                current.setUserId(new SimpleIntegerProperty(Integer.parseInt(rs.getString("userId"))));

                current.setUserName(rs.getString("userName"));
                current.setTitle(rs.getString("title"));
                current.setCustomerName(rs.getString("customerName"));
                current.setType(rs.getString("type"));
                current.setContact(rs.getString("contact"));
                current.setDescription(rs.getString("description"));
                current.setLocation(rs.getString("location"));
                current.setUrl(rs.getString("url"));

                current.setStart(rs.getTimestamp("start"));
                current.setEnd(rs.getTimestamp("end"));

                DateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                dt.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date startDate = dt.parse(rs.getTimestamp("Start").toString());
                Date endDate = dt.parse(rs.getTimestamp("end").toString());

                DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                String startDateAsString = dateFormatter.format(startDate);
                String startTimeAsString = timeFormatter.format(startDate);

                String endDateAsString = dateFormatter.format(endDate);
                String endTimeAsString = timeFormatter.format(endDate);

                String startYearString = startDateAsString.substring(0,4);
                String startMonthString = startDateAsString.substring(5,7);
                String startDayString = startDateAsString.substring(8,10);
                String endYearString = endDateAsString.substring(0,4);
                String endMonthString = endDateAsString.substring(5,7);
                String endDayString = endDateAsString.substring(8,10);
                String startHourString = startTimeAsString.substring(0,2);
                String startMinuteString = startTimeAsString.substring(3,5);
                String endHourString = endTimeAsString.substring(0,2);
                String endMinuteString = endTimeAsString.substring(3,5);
                String startPeriod;
                String endPeriod;

                if(Integer.parseInt(startHourString) > 12){
                    startHourString = Integer.toString(Integer.parseInt(startHourString)-12);
                    startPeriod ="PM";
                }else{
                    startPeriod = "AM";
                }
                if(Integer.parseInt(endHourString) > 12){
                    endHourString = Integer.toString(Integer.parseInt(endHourString)-12);
                    endPeriod = "PM";
                }else{
                    endPeriod = "AM";
                }
                String wholeStartString = startMonthString + "/" + startDayString + "/" + startYearString +
                        " " + startHourString + ":" + startMinuteString + " " + startPeriod;
                String wholeEndString = endMonthString + "/" + endDayString + "/" + endYearString +
                        " " + endHourString + ":" + endMinuteString + " " + endPeriod;

                current.setStartDate(wholeStartString);
                current.setEndDate(wholeEndString);
                LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

                String startDay = startLocalDateTime.getDayOfWeek().toString().toLowerCase();
                String endDay = endLocalDateTime.getDayOfWeek().toString().toLowerCase();
                current.setStartLocalDateTime(startLocalDateTime);
                current.setEndLocalDateTime(endLocalDateTime);

                current.setStartDayOfWeek(startDay.substring(0, 1).toUpperCase() + startDay.substring(1));
                current.setEndDayOfWeek(endDay.substring(0,1).toUpperCase() + endDay.substring(1));
                appointmentList.add(current);
            }
        }catch(SQLException e){
            System.out.println("Error on Building Data: " + e + allAppointment);
        }
        return appointmentList;
    }

}
