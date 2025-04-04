package boerenkool.communication.dto;

/**
 * @author Danny KWANT
 * @project Boerenkool
 * @created 25/08/2024 - 11:19
 */
public class HouseListDTO {

    // attributes
    int houseId;
    PictureDTO picture;
    String houseName;
    String houseType;
    String province;
    String city;
    int price;

    public HouseListDTO() { /* empty constructor for jackson, use setters */ }

    // setters
    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public void setPicture(PictureDTO picture) {
        this.picture = picture;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public void setHouseType(String houseType) {
        this.houseType = houseType;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    // getters
    public int getHouseId() {
        return houseId;
    }

    public PictureDTO getPicture() {
        return picture;
    }

    public String getHouseName() {
        return houseName;
    }

    public String getHouseType() {
        return houseType;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public int getPrice() {
        return price;
    }

} // class
