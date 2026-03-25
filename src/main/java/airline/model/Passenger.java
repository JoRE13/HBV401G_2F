package airline.model;

import java.util.Date;

public class Passenger {
    private String fullName;
    private String email;
    private String phone;
    private String nationality;
    private Date dateOfBirth;

    public Passenger(
            String fullName,
            String email,
            String phone,
            String nationality,
            Date dateOfBirth){
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
    }

    //Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getNationality() {
        return nationality;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    //setters
    public  void setFullName(String fullName){
        this.fullName = fullName;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public void setNationality(String nationality){
        this.nationality = nationality;
    }

    //methods
    public void setDateOfBirth(Date dateOfBirth){
        this.dateOfBirth = dateOfBirth;
    }

    public void updateContactInfo(String email, String phone){
        this.email = email;
        this.phone = phone;
    }

    public boolean validateDetails(){
        if (fullName == null || fullName.isEmpty()){
            return false;
        }

        if(email == null || email.isEmpty()){
            return false;
        }

        if (phone == null || phone.isEmpty()){
            return false;
        }

        return true;
    }

    public static void main(String[] args) {

    }
}
