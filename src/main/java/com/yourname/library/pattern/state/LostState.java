package com.yourname.library.pattern.state;

import com.yourname.library.model.Book;

public class LostState implements BookState {
    @Override
    public void borrow(Book book) {
        System.out.println("HATA: Kayıp olan bir kitap ödünç verilemez.");
    }

    @Override
    public void returnBook(Book book) {

        System.out.println("Kayıp kitap bulundu ve iade edildi. Durum 'Available' (Mevcut) olarak güncelleniyor.");
        book.setState(new AvailableState());
    }

    @Override
    public void reportLost(Book book) {
        System.out.println("HATA: Bu kitap zaten kayıp listesinde.");
    }
}