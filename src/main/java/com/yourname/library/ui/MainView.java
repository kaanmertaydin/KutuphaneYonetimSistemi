package com.yourname.library.ui;

import com.yourname.library.dao.BookDAO;
import com.yourname.library.dao.LoanDAO;
import com.yourname.library.dao.UserDAO;
import com.yourname.library.model.AbstractUser;
import com.yourname.library.service.BookService;
import com.yourname.library.service.LoanService;

import javax.swing.*;

public class MainView {
    private JFrame frame;
    private JButton manageBooksButton;
    private JButton searchBooksButton;
    private JButton borrowReturnButton;
    private AbstractUser currentUser;
    private BookDAO bookDAO;
    private BookService bookService;
    private LoanDAO loanDAO;
    private UserDAO userDAO;
    private LoanService loanService;

    public MainView(AbstractUser user, BookDAO bookDAO) {
        this.currentUser = user;
        this.bookDAO = bookDAO;
        this.bookService = new BookService(bookDAO);


        this.userDAO = new UserDAO();
        this.loanDAO = new LoanDAO(userDAO, bookDAO);
        this.loanService = new LoanService(loanDAO, bookService);

        initialize();
    }

    private void initialize() {
        frame = new JFrame("Ana Menü");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        manageBooksButton = new JButton("Kitap Yönetimi");
        manageBooksButton.setBounds(100, 50, 200, 40);
        panel.add(manageBooksButton);

        searchBooksButton = new JButton("Kitap Arama");
        searchBooksButton.setBounds(100, 110, 200, 40);
        panel.add(searchBooksButton);

        borrowReturnButton = new JButton("Ödünç Al / İade Et");
        borrowReturnButton.setBounds(100, 170, 200, 40);
        panel.add(borrowReturnButton);

        configureButtonsForUserType();


        manageBooksButton.addActionListener(e -> {
            System.out.println("Kitap Yönetimi Butonu Tıklandı.");
            if ("PERSONEL".equalsIgnoreCase(currentUser.getUserType()) || "Staff".equalsIgnoreCase(currentUser.getUserType())) {
                new ManageBooksView(bookService);
            } else {
                JOptionPane.showMessageDialog(frame, "Bu özelliğe yalnızca personel erişebilir.", "Erişim Engellendi", JOptionPane.ERROR_MESSAGE);
            }
        });


        searchBooksButton.addActionListener(e -> {
            System.out.println("Kitap Arama Butonu Tıklandı.");

            new SearchBooksView(bookService, loanService, currentUser);
        });


        borrowReturnButton.addActionListener(e -> {
            System.out.println("Ödünç Al / İade Et Butonu Tıklandı.");
            if ("ÖĞRENCİ".equalsIgnoreCase(currentUser.getUserType()) || "Student".equalsIgnoreCase(currentUser.getUserType())) {

                new BorrowReturnView(bookDAO, currentUser, loanDAO);
            } else {
                JOptionPane.showMessageDialog(frame, "Bu özelliğe yalnızca öğrenciler erişebilir.", "Erişim Engellendi", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void configureButtonsForUserType() {
        String userType = currentUser.getUserType();


        if ("PERSONEL".equalsIgnoreCase(userType) || "Staff".equalsIgnoreCase(userType)) {
            manageBooksButton.setEnabled(true);
            borrowReturnButton.setEnabled(false);
        } else if ("ÖĞRENCİ".equalsIgnoreCase(userType) || "Student".equalsIgnoreCase(userType)) {
            manageBooksButton.setEnabled(false);
            borrowReturnButton.setEnabled(true);
        } else {
            manageBooksButton.setEnabled(false);
            borrowReturnButton.setEnabled(false);
        }
    }
}