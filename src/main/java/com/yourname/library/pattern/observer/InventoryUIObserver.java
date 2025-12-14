package com.yourname.library.pattern.observer;

public class InventoryUIObserver implements IInventoryObserver {
    @Override
    public void update() {

        System.out.println("BİLDİRİM: Kütüphane envanterinde değişiklik oldu! Tablolar yenileniyor...");
    }
}