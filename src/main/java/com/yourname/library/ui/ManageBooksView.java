package com.yourname.library.ui;

import com.yourname.library.model.Book;
import com.yourname.library.pattern.observer.IInventoryObserver;
import com.yourname.library.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ManageBooksView implements IInventoryObserver {
    private JFrame frame;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private BookService bookService;
    private JTextField searchField;
    private JComboBox<String> criteriaBox;

    public ManageBooksView(BookService bookService) {
        this.bookService = bookService;
        this.bookService.registerObserver(this);
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Kitap Yönetimi (Personel)");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        frame.add(panel);


        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Arama:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        String[] criteria = {"Tümü", "Başlık", "Yazar", "ISBN", "Kategori"};
        criteriaBox = new JComboBox<>(criteria);
        searchPanel.add(criteriaBox);

        JButton searchBtn = new JButton("Ara");
        JButton clearBtn = new JButton("Temizle");
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        panel.add(searchPanel, BorderLayout.NORTH);


        String[] columns = {"ID", "Başlık", "Yazar", "Kategori", "ISBN", "Yayınevi", "Yıl", "Durum", "Puan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        booksTable = new JTable(tableModel);
        panel.add(new JScrollPane(booksTable), BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("+ Yeni Kitap Ekle");
        addButton.setBackground(new Color(144, 238, 144));

        JButton updateButton = new JButton("Kitap Güncelle");
        updateButton.setBackground(Color.CYAN);

        JButton deleteButton = new JButton("Kitap Sil");
        deleteButton.setBackground(new Color(255, 102, 102));

        JButton refreshButton = new JButton("Kitapları Yenile");
        refreshButton.setBackground(Color.LIGHT_GRAY);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);


        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String selectedCriteria = (String) criteriaBox.getSelectedItem();

            if (!keyword.isEmpty()) {
                List<Book> allBooks = bookService.getAllBooks();
                List<Book> filteredBooks = allBooks.stream()
                        .filter(b -> {
                            switch (selectedCriteria) {
                                case "Başlık": return b.getTitle().toLowerCase().contains(keyword);
                                case "Yazar": return b.getAuthor().toLowerCase().contains(keyword);
                                case "ISBN": return b.getIsbn() != null && b.getIsbn().contains(keyword);
                                case "Kategori": return b.getCategory().toLowerCase().contains(keyword);
                                default:
                                    return b.getTitle().toLowerCase().contains(keyword) ||
                                            b.getAuthor().toLowerCase().contains(keyword) ||
                                            (b.getIsbn() != null && b.getIsbn().contains(keyword)) ||
                                            b.getCategory().toLowerCase().contains(keyword);
                            }
                        })
                        .collect(Collectors.toList());
                refreshTable(filteredBooks);
            } else {
                refreshTable(bookService.getAllBooks());
            }
        });

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            criteriaBox.setSelectedIndex(0);
            refreshTable(bookService.getAllBooks());
        });

        addButton.addActionListener(e -> showAddBookDialog());
        updateButton.addActionListener(e -> showUpdateBookDialog());
        refreshButton.addActionListener(e -> refreshTable(bookService.getAllBooks()));

        deleteButton.addActionListener(e -> {
            int row = booksTable.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                String title = (String) tableModel.getValueAt(row, 1);
                if (JOptionPane.showConfirmDialog(frame, "'" + title + "' silinsin mi?", "Onay", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    bookService.deleteBook(id);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Seçim yapınız.");
            }
        });

        refreshTable(bookService.getAllBooks());
        frame.setVisible(true);
    }

    @Override
    public void update() {
        refreshTable(bookService.getAllBooks());
    }

    private void refreshTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            Object[] row = {
                    b.getId(), b.getTitle(), b.getAuthor(), b.getCategory(),
                    b.getIsbn(), b.getPublisher(), b.getPublishYear(), b.getStatus(), b.getAvgRating()
            };
            tableModel.addRow(row);
        }
    }

    private void showAddBookDialog() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextField yearField = new JTextField();

        Object[] message = { "Başlık:", titleField, "Yazar:", authorField, "Kategori:", categoryField, "ISBN:", isbnField, "Yayınevi:", publisherField, "Yıl:", yearField };

        if (JOptionPane.showConfirmDialog(frame, message, "Yeni Kitap", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int year = Integer.parseInt(yearField.getText());
                Book newBook = new Book(titleField.getText(), authorField.getText(), categoryField.getText(), isbnField.getText(), publisherField.getText(), year);
                bookService.addBook(newBook);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Hata: " + ex.getMessage()); }
        }
    }

    private void showUpdateBookDialog() {
        int row = booksTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(frame, "Seçim yapın."); return; }

        int id = (int) tableModel.getValueAt(row, 0);
        Book book = bookService.getBookById(id);

        if(book != null) {
            JTextField titleField = new JTextField(book.getTitle());
            JTextField authorField = new JTextField(book.getAuthor());
            JTextField categoryField = new JTextField(book.getCategory());
            JTextField isbnField = new JTextField(book.getIsbn());
            JTextField publisherField = new JTextField(book.getPublisher());
            JTextField yearField = new JTextField(String.valueOf(book.getPublishYear()));

            Object[] message = { "Başlık:", titleField, "Yazar:", authorField, "Kategori:", categoryField, "ISBN:", isbnField, "Yayınevi:", publisherField, "Yıl:", yearField };

            if (JOptionPane.showConfirmDialog(frame, message, "Güncelle", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setCategory(categoryField.getText());
                book.setIsbn(isbnField.getText());
                book.setPublisher(publisherField.getText());
                book.setPublishYear(Integer.parseInt(yearField.getText()));
                bookService.updateBook(book);
            }
        }
    }
}