package ibecsystems.kz.instagram;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aibek on 11.03.15.
 */
public class Comment implements Parcelable{

    private String author;
    private String text;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(text);
    }
}
