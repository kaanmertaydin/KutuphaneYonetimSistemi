package com.yourname.library.model;

import com.yourname.library.pattern.state.AvailableState;
import com.yourname.library.pattern.state.BookState;
import com.yourname.library.pattern.state.BorrowedState;
import com.yourname.library.pattern.state.LostState;

public class Book extends AbstractBook {

    private BookState state;
    private String avgRating;


    public Book(int id, String title, String author, String category, String status, String avgRating) {

        super(id, title, author, category, null, null, 0);

        this.avgRating = avgRating != null ? avgRating : "Puan Yok";
        setStatus(status);
    }


    public Book(String title, String author, String category, String isbn, String publisher, int publishYear) {
        super(0, title, author, category, isbn, publisher, publishYear);
        this.state = new AvailableState();
        this.avgRating = "Puan Yok";
    }


    @Override
    public String getBookType() {
        return "Normal Kitap";
    }



    public BookState getState() {
        return state;
    }

    public void setState(BookState state) {
        this.state = state;
    }


    public String getStatus() {
        if (state instanceof BorrowedState) return "Borrowed";
        if (state instanceof LostState) return "Lost";
        return "Available";
    }

    public void setStatus(String status) {
        if (status == null) {
            this.state = new AvailableState();
            return;
        }

        switch (status.toLowerCase()) {
            case "borrowed":
            case "ödünç alındı":
                this.state = new BorrowedState();
                break;
            case "lost":
            case "kayıp":
                this.state = new LostState();
                break;
            default:
                this.state = new AvailableState();
                break;
        }
    }


    public void borrow() {
        if (state != null) {
            state.borrow(this);
        }
    }

    public void returnBook() {
        if (state != null) {
            state.returnBook(this);
        }
    }

    public void reportLost() {
        if (state != null) {
            state.reportLost(this);
        }
    }



    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }


    public String getSubject() {
        return getCategory();
    }

    public void setSubject(String subject) {
        setCategory(subject);
    }


    @Override
    public String toString() {
        return "Book{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", category='" + getCategory() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", rating='" + avgRating + '\'' +
                '}';
    }
}