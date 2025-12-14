package com.yourname.library.ui;

import com.yourname.library.dao.BookDAO;
import com.yourname.library.dao.LoanDAO;
import com.yourname.library.dao.UserDAO;
import com.yourname.library.model.AbstractUser;
import com.yourname.library.model.Book;
import com.yourname.library.model.Loan;
import com.yourname.library.service.BookService;
import com.yourname.library.service.LoanService;
import com.yourname.library.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentView {
    private JFrame frame;
    private AbstractUser currentUser;

    private BookService bookService;
    private LoanService loanService;
    private UserService userService;

    private DefaultTableModel booksModel;
    private DefaultTableModel loansModel;
    private JTable booksTable;
    private JTable myLoansTable;


    private JTextField firstNameField, lastNameField, tcNoField, emailField, phoneField, numberField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public StudentView(AbstractUser user, BookDAO bookDAO, LoanDAO loanDAO) {
        this.currentUser = user;
        this.bookService = new BookService(bookDAO);
        this.loanService = new LoanService(loanDAO, bookService);
        this.userService = new UserService(new UserDAO());
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Öğrenci Paneli - " + currentUser.getFirstName() + " " + currentUser.getLastName());
        frame.setSize(1000, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Kitap İşlemleri", new ImageIcon(), createBookPanel(), "Kitap ara ve ödünç al");
        tabbedPane.addTab("Ödünçlerim", new ImageIcon(), createLoanPanel(), "Aldığım kitaplar ve cezalar");
        tabbedPane.addTab("Profil Bilgilerim", new ImageIcon(), createProfilePanel(), "Bilgilerimi güncelle");

        frame.add(tabbedPane);
        loadBooks();
        loadLoans();
        frame.setVisible(true);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(null);
        int y = 40; int gap = 40;

        addLabelAndField(panel, "Ad:", firstNameField = new JTextField(currentUser.getFirstName()), y); y+=gap;
        addLabelAndField(panel, "Soyad:", lastNameField = new JTextField(currentUser.getLastName()), y); y+=gap;
        addLabelAndField(panel, "T.C. No:", tcNoField = new JTextField(currentUser.getTcNo()), y); y+=gap;


        addLabelAndField(panel, "Okul No:", numberField = new JTextField(currentUser.getNumber()), y); y+=gap;

        addLabelAndField(panel, "E-posta:", emailField = new JTextField(currentUser.getEmail()), y); y+=gap;
        addLabelAndField(panel, "Telefon:", phoneField = new JTextField(currentUser.getPhone()), y); y+=gap;

        JLabel lblPass = new JLabel("Yeni Şifre:");
        lblPass.setBounds(50, y, 100, 25);
        panel.add(lblPass);
        passwordField = new JPasswordField(currentUser.getPassword());
        passwordField.setBounds(150, y, 200, 25);
        panel.add(passwordField);
        y+=gap;

        JLabel lblConfirmPass = new JLabel("Şifre Tekrar:");
        lblConfirmPass.setBounds(50, y, 100, 25);
        panel.add(lblConfirmPass);
        confirmPasswordField = new JPasswordField(currentUser.getPassword());
        confirmPasswordField.setBounds(150, y, 200, 25);
        panel.add(confirmPasswordField);
        y+=50;

        JButton updateBtn = new JButton("Bilgilerimi Güncelle");
        updateBtn.setBounds(150, y, 200, 30);
        panel.add(updateBtn);
        y+=50;

        JButton logoutBtn = new JButton("Çıkış Yap");
        logoutBtn.setBounds(150, y, 200, 30);
        logoutBtn.setBackground(Color.PINK);
        panel.add(logoutBtn);

        updateBtn.addActionListener(e -> {
            String pass = new String(passwordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());

            if (!pass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(frame, "Girdiğiniz şifreler eşleşmiyor!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentUser.setFirstName(firstNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setTcNo(tcNoField.getText());
            currentUser.setNumber(numberField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setPhone(phoneField.getText());
            currentUser.setPassword(pass);

            if (userService.updateUser(currentUser)) {
                JOptionPane.showMessageDialog(frame, "Profiliniz başarıyla güncellendi!");
                frame.setTitle("Öğrenci Paneli - " + currentUser.getFirstName() + " " + currentUser.getLastName());
            } else {
                JOptionPane.showMessageDialog(frame, "Güncelleme başarısız (Çakışan veri olabilir).", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        logoutBtn.addActionListener(e -> frame.dispose());

        return panel;
    }


    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Detaylı Arama Yap");
        JButton refreshBtn = new JButton("Listeyi Yenile");
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Başlık", "Yazar", "Kategori", "ISBN", "Yayınevi", "Durum", "Puan"};
        booksModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        booksTable = new JTable(booksModel);
        panel.add(new JScrollPane(booksTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton borrowBtn = new JButton("Seçili Kitabı Ödünç Al");
        bottomPanel.add(borrowBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadBooks());
        searchBtn.addActionListener(e -> new SearchBooksView(bookService, loanService, currentUser));
        borrowBtn.addActionListener(e -> borrowSelectedBook());

        return panel;
    }

    private JPanel createLoanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"Loan ID", "Kitap Başlığı", "Alım Tarihi", "Son Teslim Tarihi", "Durum", "Anlık Ceza (TL)"};
        loansModel = new DefaultTableModel(columns, 0);
        myLoansTable = new JTable(loansModel);
        panel.add(new JScrollPane(myLoansTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton returnBtn = new JButton("Seçili Kitabı İade Et");
        JButton refreshLoanBtn = new JButton("Durumu Yenile");
        btnPanel.add(returnBtn);
        btnPanel.add(refreshLoanBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        refreshLoanBtn.addActionListener(e -> loadLoans());
        returnBtn.addActionListener(e -> returnSelectedBook());
        return panel;
    }

    private void addLabelAndField(JPanel panel, String label, JTextField field, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(50, y, 100, 25);
        panel.add(lbl);
        field.setBounds(150, y, 200, 25);
        panel.add(field);
    }

    private void loadBooks() {
        booksModel.setRowCount(0);
        List<Book> books = bookService.getAllBooks();
        for (Book b : books) {
            Object[] row = {
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getIsbn(),
                    b.getPublisher(),
                    b.getStatus(),
                    b.getAvgRating()
            };
            booksModel.addRow(row);
        }
    }

    private void loadLoans() {
        loansModel.setRowCount(0);
        List<Loan> loans = loanService.getActiveLoansByUser(currentUser);
        for (Loan l : loans) {
            double fine = l.calculateFine();
            String fineText = (fine > 0) ? String.format("%.2f TL", fine) : "0.00 TL";
            loansModel.addRow(new Object[]{l.getId(), l.getBook().getTitle(), l.getLoanDate(), l.getDueDate(), l.getStatus(), fineText});
        }
    }

    private void borrowSelectedBook() {
        int row = booksTable.getSelectedRow();
        if (row != -1) {
            int bookId = (int) booksModel.getValueAt(row, 0);
            Book book = bookService.getBookById(bookId);
            try {
                loanService.borrowBook(currentUser, book);
                JOptionPane.showMessageDialog(frame, "Kitap başarıyla ödünç alındı! 15 gün süreniz başladı.");
                loadBooks(); loadLoans();
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "İşlem Başarısız: " + ex.getMessage()); }
        } else { JOptionPane.showMessageDialog(frame, "Lütfen listeden bir kitap seçin."); }
    }

    private void returnSelectedBook() {
        int row = myLoansTable.getSelectedRow();
        if (row != -1) {
            int loanId = (int) loansModel.getValueAt(row, 0);

            Loan loan = loanService.getAllLoans().stream().filter(l -> l.getId() == loanId).findFirst().orElse(null);

            if (loan != null) {
                double fine = loan.calculateFine();
                if (fine > 0) {
                    int confirm = JOptionPane.showConfirmDialog(frame,
                            "Bu kitabın " + fine + " TL gecikme cezası var. Ödemeyi onaylıyor musunuz?",
                            "Ceza Ödeme", JOptionPane.YES_NO_OPTION);

                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                loanService.returnBook(loanId);
                JOptionPane.showMessageDialog(frame, "Kitap iade edildi.");

                String[] ratings = {"Puan Verme", "1 - Çok Kötü", "2 - Kötü", "3 - Orta", "4 - İyi", "5 - Harika"};
                String selectedRating = (String) JOptionPane.showInputDialog(frame,
                        "Okuduğunuz kitabı puanlamak ister misiniz?",
                        "Kitap Puanla",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        ratings,
                        ratings[0]);

                if (selectedRating != null && !selectedRating.equals("Puan Verme")) {
                    try {
                        int score = Integer.parseInt(selectedRating.split(" - ")[0]);
                        bookService.rateBook(currentUser.getId(), loan.getBook().getId(), score);
                        JOptionPane.showMessageDialog(frame, "Puanınız kaydedildi! Teşekkürler.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                loadBooks();
                loadLoans();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Lütfen iade edilecek işlemi seçin.");
        }
    }
}