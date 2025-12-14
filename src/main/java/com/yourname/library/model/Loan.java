package com.yourname.library.model;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Loan {
    private int id;
    private AbstractUser user;
    private Book book;
    private Date loanDate;
    private Date dueDate;
    private Date actualReturnDate;
    private String status;
    private double fine;


    public Loan(int id, AbstractUser user, Book book, Date loanDate, Date dueDate, Date actualReturnDate, String status) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
        this.fine = calculateFine();
    }


    public Loan(int id, AbstractUser user, Book book, Date loanDate, Date dueDate) {
        this(id, user, book, loanDate, dueDate, null, "Active");
    }


    public double calculateFine() {
        Date effectiveDate = (actualReturnDate != null) ? actualReturnDate : new Date();

        if (effectiveDate.after(dueDate)) {
            long diffInMillies = Math.abs(effectiveDate.getTime() - dueDate.getTime());
            long diffInDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);


            double dailyFine = com.yourname.library.util.SystemConfig.getInstance().getDailyFine();
            return diffInDays * dailyFine;
        }
        return 0.0;
    }



    public int getId() {
        return id;
    }

    public AbstractUser getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public Date getLoanDate() {
        return loanDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(Date actualReturnDate) {

        this.actualReturnDate = actualReturnDate;
        this.fine = calculateFine();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getFine() {

        return calculateFine();
    }

    public void setFine(double fine) {
        this.fine = fine;
    }
}