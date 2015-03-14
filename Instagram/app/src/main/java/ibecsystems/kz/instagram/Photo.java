package ibecsystems.kz.instagram;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aibek on 11.03.15.
 */
public class Photo implements Parcelable{

    private String lowResolution;
    private String standardPhoto;
    private String likes;
    private String id;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String comments;
    private boolean userHasLiked;
    private String userName;

    public boolean isUserHasLiked() {
        return userHasLiked;
    }

    public void setUserHasLiked(boolean userHasLiked) {
        this.userHasLiked = userHasLiked;
    }

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



    public void setLowResolution(String lowResolution) {
        this.lowResolution = lowResolution;
    }

    public void setStandardPhoto(String standardPhoto) {
        this.standardPhoto = standardPhoto;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getLowResolution() {

        return lowResolution;
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
        dest.writeString(lowResolution);
        dest.writeString(standardPhoto);
        dest.writeString(id);
        dest.writeString(likes);
        dest.writeString(comments);
    }
}
