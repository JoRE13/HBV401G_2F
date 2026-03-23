package src.main.java.airline.model;

public class Airport {
    private String airportCode;
    private String name;
    private String city;
    private String country;

    //getters
    public String getAirportCode(){
        return airportCode;
    }

    public String getName(){
        return name;
    }

    public String getCity(){
        return city;
    }

    public String getCountry(){
        return  country;
    }

    //setters
    public void setAirportCode(){
        this.airportCode = airportCode;
    }

    public void setName(){
        this.name = name;
    }

    public void setCity(){
        this.city = city;
    }

    public void setCountry(){
        this.country = country;
    }

    public static void main(String[] args) {

    }
}
