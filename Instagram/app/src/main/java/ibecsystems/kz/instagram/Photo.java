package ibecsystems.kz.instagram;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aibek on 11.03.15.
 */
public class Photo implements Parcelable{

    private String thumbnail;
    private String standardPhoto;
    private String likes;
    private String id;
    private String comments;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }



    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setStandardPhoto(String standardPhoto) {
        this.standardPhoto = standardPhoto;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getThumbnail() {

        return thumbnail;
    }

    public String getStandardPhoto() {
        return standardPhoto;
    }

    public String getLikes() {
        return likes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbnail);
        dest.writeString(standardPhoto);
        dest.writeString(id);
        dest.writeString(likes);
        dest.writeString(comments);
    }
}
