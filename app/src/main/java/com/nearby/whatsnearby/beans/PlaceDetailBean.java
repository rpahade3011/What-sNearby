package com.nearby.whatsnearby.beans;

import java.io.Serializable;

public class PlaceDetailBean implements Serializable {
    private String formatted_address;
    private String formatted_phone_number;
    private String compoundAddress;
    private double lat;
    private double lng;
    private String international_phone_number;
    private String name;
    private String[] photos;
    private String place_id;
    private float rating;
    private String[] weekday;
    private Review[] reviews;
    private boolean isOpen;

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getCompoundAddress() {
        return compoundAddress;
    }

    public void setCompoundAddress(String compoundAddress) {
        this.compoundAddress = compoundAddress;
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public void setFormatted_phone_number(String formatted_phone_number) {
        this.formatted_phone_number = formatted_phone_number;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getInternational_phone_number() {
        return international_phone_number;
    }

    public void setInternational_phone_number(String international_phone_number) {
        this.international_phone_number = international_phone_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String[] getWeekday() {
        return weekday;
    }

    public void setWeekday(String[] weekday) {
        this.weekday = weekday;
    }

    public Review[] getReviews() {
        return reviews;
    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public class Review implements Serializable {
        private String author_name;
        private String author_url;
        private float author_rating;
        private String author_text;
        private long written_time;
        private String relative_time_description;

        public String getAuthor_name() {
            return author_name;
        }

        public void setAuthor_name(String author_name) {
            this.author_name = author_name;
        }

        public String getAuthor_url() {
            return author_url;
        }

        public void setAuthor_url(String author_url) {
            this.author_url = author_url;
        }

        public float getRating() {
            return author_rating;
        }

        public void setRating(float rating) {
            this.author_rating = rating;
        }

        public String getAuthor_text() {
            return author_text;
        }

        public void setAuthor_text(String author_text) {
            this.author_text = author_text;
        }

        public long getWritten_time() {
            return written_time;
        }

        public void setWritten_time(long written_time) {
            this.written_time = written_time;
        }

        public String getRelative_time_description() {
            return relative_time_description;
        }

        public void setRelative_time_description(String relative_time_description) {
            this.relative_time_description = relative_time_description;
        }
    }
}