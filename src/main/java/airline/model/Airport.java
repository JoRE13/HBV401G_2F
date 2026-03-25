package airline.model;

public class Airport {
    private String airportCode;
    private String name;
    private String city;
    private String country;

    //constructor
    public Airport(
            String airportCode,
            String name,
            String city,
            String country){
        this.airportCode = airportCode;
        this.name = name;
        this.city = city;
        this.country = country;
    }

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

    public static void main(String[] args) {

    }
}
