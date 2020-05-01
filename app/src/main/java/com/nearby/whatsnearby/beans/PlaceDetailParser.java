package com.nearby.whatsnearby.beans;

import android.util.Log;

import com.nearby.whatsnearby.services.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceDetailParser {

    private String jsonData;
    private PlaceDetailBean detailBean;

    public PlaceDetailParser(String data) {
        jsonData = data;
    }

    public String getStatus() {
        String status = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            status = jsonObject.getString("status");
        } catch (JSONException e) {
            Log.e("JSON Parser", "Unable to get status", e);
        }

        return status;
    }

    public PlaceDetailBean getPlaceDetail() throws Exception {
        detailBean = new PlaceDetailBean();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject result = jsonObject.getJSONObject("result");
            if (result.has("formatted_address")) {
                detailBean.setFormatted_address(result.getString("formatted_address"));
            } else {
                detailBean.setFormatted_address("Not available");
            }
            if (result.has("formatted_phone_number")) {
                detailBean.setFormatted_phone_number(result.getString("formatted_phone_number"));
            } else {
                detailBean.setFormatted_phone_number("Not available");
            }
            if (result.has("international_phone_number")) {
                detailBean.setInternational_phone_number(result.getString("international_phone_number"));
            } else {
                detailBean.setInternational_phone_number("Not available");
            }
            if (result.has("name")) {
                detailBean.setName(result.getString("name"));
            } else {
                detailBean.setName("Not available");
            }
            if (result.has("geometry")) {
                JSONObject geometry = result.getJSONObject("geometry");
                if (geometry.has("location")) {
                    JSONObject location = geometry.getJSONObject("location");
                    detailBean.setLat(location.getDouble("lat"));
                    detailBean.setLng(location.getDouble("lng"));
                } else {
                    detailBean.setLat(0.0);
                    detailBean.setLng(0.0);
                }
            } else {
                detailBean.setLat(0.0);
                detailBean.setLng(0.0);
            }
            if (result.has("id")) {
                detailBean.setPlace_id(result.getString("id"));
            } else {
                detailBean.setPlace_id(null);
            }
            if (result.has("rating")) {
                detailBean.setRating((float) result.getDouble("rating"));
            } else {
                detailBean.setRating(0.0f);
            }
            // Parsing opening timings
            if (result.has("opening_hours")) {
                JSONObject openingHoursObject = result.optJSONObject("opening_hours");
                if (openingHoursObject != null) {
                    detailBean.setOpen(openingHoursObject.optBoolean("open_now"));
                    JSONArray weekdayArray = openingHoursObject.optJSONArray("weekday_text");
                    if (weekdayArray != null && weekdayArray.length() > 0) {
                        String[] weekDay = new String[weekdayArray.length()];
                        for (int i = 0; i < weekdayArray.length(); i++) {
                            weekDay[i] = weekdayArray.optString(i);
                        }
                        detailBean.setWeekday(weekDay);
                    } else {
                        detailBean.setWeekday(null);
                    }
                }
            }

            if (result.has("plus_code")) {
                JSONObject plusCodeObject = result.optJSONObject("plus_code");
                detailBean.setCompoundAddress(plusCodeObject.optString("compound_code"));
            } else {
                detailBean.setCompoundAddress("Not available");
            }

            if (result.has("website")) {
                detailBean.setWebsiteUrl(result.optString("website"));
            } else {
                detailBean.setWebsiteUrl("Not available");
            }

            if (result.has("photos")) {
                JSONArray photosArray = result.getJSONArray("photos");
                String[] photoRef = new String[photosArray.length()];
                for (int i = 0; i < photosArray.length(); i++) {
                    JSONObject photo = photosArray.getJSONObject(i);
                    photoRef[i] = photo.getString("photo_reference");
                }
                detailBean.setPhotos(photoRef);
                AppController.getInstance().setPlacePhotos(photoRef);
            } else {
                detailBean.setPhotos(null);
                AppController.getInstance().setPlacePhotos(null);
            }

            if (result.has("reviews")) {
                JSONArray reviewArray = result.getJSONArray("reviews");
                PlaceDetailBean.Review[] reviews = new PlaceDetailBean.Review[reviewArray.length()];
                for (int i = 0; i < reviewArray.length(); i++) {
                    PlaceDetailBean.Review review = detailBean.new Review();
                    JSONObject reviewObj = reviewArray.getJSONObject(i);
                    if (reviewObj.has("author_name")) {
                        review.setAuthor_name(reviewObj.getString("author_name"));
                    } else {
                        review.setAuthor_name(null);
                    }
                    if (reviewObj.has("profile_photo_url")) {
                        review.setAuthor_profile_url(reviewObj.getString("profile_photo_url"));
                    } else {
                        review.setAuthor_profile_url(null);
                    }

                    if (reviewObj.has("rating")) {
                        review.setRating((float) reviewObj.getDouble("rating"));
                    } else {
                        review.setRating(0.0f);
                    }

                    if (reviewObj.has("relative_time_description")) {
                        review.setRelative_time_description(reviewObj.optString("relative_time_description"));
                    } else {
                        review.setRelative_time_description(null);
                    }
                    if (reviewObj.has("text")) {
                        review.setAuthor_text(reviewObj.getString("text"));
                    } else {
                        review.setAuthor_text(null);
                    }
                    if (reviewObj.has("time")) {
                        review.setWritten_time(reviewObj.getLong("time"));
                    } else {
                        review.setWritten_time(0);
                    }
                    reviews[i] = review;
                }
                detailBean.setReviews(reviews);
                AppController.getInstance().setReview(reviews);
            } else {
                detailBean.setReviews(null);
                AppController.getInstance().setReview(null);
            }
        } catch (JSONException ex) {
            Log.e("PlaceDetailParser", ex.toString());
            throw new Exception("Something went wrong on server.");
        }
        return detailBean;
    }
}