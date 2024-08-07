package boerenkool.business.model;

import java.sql.SQLOutput;

/*
Student: Timothy Houweling
 */
public class Picture {

    /*
    Attributen
     */

    private static final int MAX_PICTURE_SIZE = 5 * 1024 * 1024; // 5 MB
    private int pictureId;
    private House house; //todo House object bestaat nog niet als model
    private byte[] picture;
    private String description;

    /*
    Constructors
     */

    public Picture(int pictureId, House house, byte[] picture, String description) {
        this.pictureId = pictureId;
        this.house = house;
        this.picture = picture;
        this.description = description;
    }

    public Picture(House house, byte[] picture, String description) {
        this(0, house, picture, description);
    }

    public Picture(House house, byte[] picture) {
        this(house, picture, "");
    }

    /*
    Methodes
     */

    @Override
    public String toString() {
        return "Picture id" + getPictureId();
    }

    /*
    Getters & Setters
     */

    public int getPictureId() {
        return pictureId;
    }

    public void setPictureId(int pictureId) {
        this.pictureId = pictureId;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) throws IllegalArgumentException {
        if (picture.length > MAX_PICTURE_SIZE) {
            throw new IllegalArgumentException("Foto overschrijd het maximum grootte van 5MB.");
        }
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


} // einde klasse