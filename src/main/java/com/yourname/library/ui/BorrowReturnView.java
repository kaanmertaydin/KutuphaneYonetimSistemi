package com.yourname.library.ui;

import com.yourname.library.dao.BookDAO;
import com.yourname.library.dao.LoanDAO;
import com.yourname.library.model.AbstractUser;
import com.yourname.library.model.Book;
import com.yourname.library.model.Loan;
import com.yourname.library.service.BookService;
import com.yourname.library.service.LoanService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BorrowReturnView {
    private JFrame frame;
    private JTable bookTable;
    private JButton borrowButton, returnButton;
    private BookService bookService;
    private LoanService loanService;
    private AbstractUser currentUser;

    public BorrowReturnView(BookDAO bookDAO, AbstractUser currentUser, LoanDAO loanDAO) {

        this.bookService = new BookService(bookDAO);

        this.loanService = new LoanService(loanDAO, bookService);

        this.currentUser = currentUser;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Ödünç Al / İade Et");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());


        String[] columnNames = {"ID", "Başlık", "Yazar", "Kategori", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        bookTable = new JTable(model);
        frame.add(new JScrollPane(bookTable), BorderLayout.CENTER);


        JPanel btnPanel = new JPanel();
        borrowButton = new JButton("Ödünç Al");
        returnButton = new JButton("İade Et");
        JButton refreshBtn = new JButton("Yenile");

        btnPanel.add(borrowButton);
        btnPanel.add(returnButton);
        btnPanel.add(refreshBtn);
        frame.add(btnPanel, BorderLayout.SOUTH);


        fillTable(model);




        refreshBtn.addActionListener(e -> fillTable(model));


        borrowButton.addActionListener(e -> {
            int selected = bookTable.getSelectedRow();
            if(selected != -1) {
                int bookId = (int) bookTable.getValueAt(selected, 0);
                Book book = bookService.getBookById(bookId);

                try {
                    loanService.borrowBook(currentUser, book);
                    JOptionPane.showMessageDialog(frame, "Kitap ödünç alındı.");
                    fillTable(model);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Hata: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Lütfen bir kitap seçin.");
            }
        });


        returnButton.addActionListener(e -> {
            int selected = bookTable.getSelectedRow();
            if(selected != -1) {
                int bookId = (int) bookTable.getValueAt(selected, 0);


                Loan loan = loanService.getAllLoans().stream()
                        .filter(l -> l.getBook().getId() == bookId &&
                                l.getUser().getId() == currentUser.getId() &&
                                "Active".equals(l.getStatus()))
                        .findFirst().orElse(null);

                if(loan != null) {
                    loanService.returnBook(loan.getId());

                    String msg = "Kitap iade edildi.";
                    if(loan.getFine() > 0) {
                        msg += "\nGecikme Cezası: " + loan.getFine() + " TL";
                    }
                    JOptionPane.showMessageDialog(frame, msg);
                    fillTable(model);
                } else {
                    JOptionPane.showMessageDialog(frame, "Bu kitap sizde ödünçte görünmüyor.");
                }
            }
        });

        frame.setVisible(true);
    }

    private void fillTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<Book> books = bookService.getAllBooks();
        for(Book b : books) {
            String stateName = (b.getState() != null) ? b.getStatus() : "Bilinmiyor";
            Object[] row = {b.getId(), b.getTitle(), b.getAuthor(), b.getCategory(), stateName};
            model.addRow(row);
        }
    }
}