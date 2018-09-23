package Model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sun.java2d.pipe.SpanShapeRenderer;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class Appointment {

    private static ResourceBundle resources;
    private IntegerProperty appointmentId;
    private IntegerProperty customerId;
    private StringProperty customerName;
    private IntegerProperty userId;
    private StringProperty userName;
    private StringProperty title;
    private StringProperty description;
    private StringProperty location;
    private StringProperty contact;
    private StringProperty type;
    private StringProperty url;
    private Timestamp start;
    private Timestamp end;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;

    public Appointment(){
        appointmentId = new SimpleIntegerProperty();
        customerId = new SimpleIntegerProperty();
        customerName = new SimpleStringProperty();
        userId = new SimpleIntegerProperty();
        userName = new SimpleStringProperty();
        title = new SimpleStringProperty();
        description = new SimpleStringProperty();
        location = new SimpleStringProperty();
        contact = new SimpleStringProperty();
        type = new SimpleStringProperty();
        url = new SimpleStringProperty();
        this.start = start;
        this.end = end;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public void setAppointmentId(IntegerProperty appointmentId){
        this.appointmentId = appointmentId;
    }
    public void setCustomerId(IntegerProperty customerId){ this.customerId = customerId; }
    public void setCustomerName(String customerName){ this.customerName.set(customerName); }
    public void setUserId(IntegerProperty userId){ this.userId = userId; }
    public void setUserName(String userName){ this.userName.set(userName); }
    public void setTitle(String title){ this.title.set(title); }
    public void setDescription(String description){ this.description.set(description); }
    public void setLocation(String location){ this.location.set(location); }
    public void setContact(String contact){ this.contact.set(contact); }
    public void setType(String type){ this.type.set(type); }
    public void setUrl(String url){ this.url.set(url); }
    public void setStart(Timestamp start){ this.start = start; }
    public void setEnd(Timestamp end){ this.end = end; }
    public void setStartDate(Date startDate){this.startDate = startDate; }
    public void setEndDate(Date endDate){this.endDate = endDate; }
    public void setStartTime(Time startTime){this.startTime = startTime; }
    public void setEndTime(Time endTime){this.endTime = endTime; }



    public IntegerProperty appointmentIdProperty(){ return appointmentId; }
    public IntegerProperty customerIdProperty(){ return customerId; }
    public StringProperty customerName(){ return customerName; }
    public IntegerProperty userIdProperty(){ return userId; }
    public StringProperty userName(){ return userName; }
    public StringProperty titleProperty(){ return title;    }
    public StringProperty descriptionProperty(){ return description; }
    public StringProperty locationProperty(){ return location; }
    public StringProperty contactProperty(){ return contact; }
    public StringProperty typeProperty(){ return type; }
    public StringProperty urlProperty(){ return url; }
    public Timestamp startProperty(){ return start; }
    public Timestamp endProperty(){ return end; }
    public Date startDateProperty(){ return startDate; }
    public Date endDateProperty(){ return endDate; }
    public Time startTimeProperty(){ return startTime; }
    public Time endTimeProperty(){ return endTime; }
    public int getAppointmentId(){ return appointmentId.get(); }
    public int getCustomerId(){ return customerId.get(); }
    public String getCustomerName(){ return customerName.get(); }
    public int getUserId(){ return userId.get(); }
    public String getUserName(){ return userName.get(); }
    public String getTitle(){ return title.get(); }
    public String getDescription(){ return description.get(); }
    public String getLocation(){ return location.get(); }
    public String getContact(){ return contact.get(); }
    public String getType(){ return type.get(); }
    public String getUrl(){ return url.get(); }
    public Timestamp getStart(){ return start; }
    public Timestamp getEnd(){ return end; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public Time getStartTime() { return startTime; }
    public Time getEndTime() { return endTime; }


    public static String validAppointment(String title, String description, String location, String contact,  String start, String end){
        String error = "";
        if(title.length() == 0)
            error = resources.getString("customer.name");
        if(description.length() == 0)
            error = resources.getString("customer.address");
        if(location.length() == 0)
            error = resources.getString("customer.postalCode");
        if(contact.length() == 0)
            error = resources.getString("customer.phone");
        if(start.length() == 0)
            error = resources.getString("customer.city");
        if(end.length() == 0)
            error = resources.getString("customer.country");

        return error;

    }
}
