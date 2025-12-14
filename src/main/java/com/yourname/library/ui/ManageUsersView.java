package com.yourname.library.ui;

import com.yourname.library.dao.BookDAO;
import com.yourname.library.dao.LoanDAO;
import com.yourname.library.dao.UserDAO;
import com.yourname.library.model.AbstractUser;
import com.yourname.library.service.BookService;
import com.yourname.library.service.LoanService;
import com.yourname.library.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ManageUsersView {
    private JFrame frame;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private UserService userService;
    private AbstractUser currentUser;
    private JTextField searchField;
    private JComboBox<String> criteriaBox;

    public ManageUsersView(UserService userService, AbstractUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Kullanıcı Yönetimi");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        frame.add(panel);


        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Arama:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);

        String[] criteria = {"Tümü", "Ad", "Soyad", "TC No", "E-posta", "Okul No"};
        criteriaBox = new JComboBox<>(criteria);
        searchPanel.add(criteriaBox);

        JButton searchBtn = new JButton("Ara");
        JButton clearBtn = new JButton("Temizle");
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        panel.add(searchPanel, BorderLayout.NORTH);


        String[] columns = {"ID", "Ad", "Soyad", "TC No", "E-posta", "Tip", "Okul No", "Telefon"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(tableModel);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        refreshTable(userService.getAllUsers());


        JPanel btnPanel = new JPanel();

        JButton addUserBtn = new JButton("+ Yeni Kullanıcı Ekle");
        addUserBtn.setBackground(new Color(144, 238, 144));

        JButton detailsBtn = new JButton("Detaylar & İşlemler");
        detailsBtn.setBackground(Color.CYAN);

        JButton deleteBtn = new JButton("Kullanıcı Sil");
        deleteBtn.setBackground(new Color(255, 102, 102));

        JButton refreshBtn = new JButton("Yenile");
        refreshBtn.setBackground(Color.LIGHT_GRAY);

        btnPanel.add(addUserBtn);
        btnPanel.add(detailsBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);



        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String selectedCriteria = (String) criteriaBox.getSelectedItem();
            if (!keyword.isEmpty()) {
                List<AbstractUser> filteredUsers = userService.getAllUsers().stream().filter(u -> {
                    switch (selectedCriteria) {
                        case "Ad": return u.getFirstName().toLowerCase().contains(keyword);
                        case "Soyad": return u.getLastName().toLowerCase().contains(keyword);
                        case "TC No": return u.getTcNo() != null && u.getTcNo().contains(keyword);
                        case "E-posta": return u.getEmail().toLowerCase().contains(keyword);
                        case "Okul No": return u.getNumber() != null && u.getNumber().contains(keyword);
                        default: return u.getFirstName().toLowerCase().contains(keyword) ||
                                u.getLastName().toLowerCase().contains(keyword) ||
                                (u.getTcNo() != null && u.getTcNo().contains(keyword)) ||
                                u.getEmail().toLowerCase().contains(keyword) ||
                                (u.getNumber() != null && u.getNumber().contains(keyword));
                    }
                }).collect(Collectors.toList());
                refreshTable(filteredUsers);
            } else { refreshTable(userService.getAllUsers()); }
        });

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            criteriaBox.setSelectedIndex(0);
            refreshTable(userService.getAllUsers());
        });

        addUserBtn.addActionListener(e -> {
            UserDAO uDAO = new UserDAO();
            BookDAO bDAO = new BookDAO();
            LoanDAO lDAO = new LoanDAO(uDAO, bDAO);
            new RegisterView(userService, bDAO, lDAO);
        });

        detailsBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row != -1) {
                int userId = (int) tableModel.getValueAt(row, 0);
                UserDAO tempUserDAO = new UserDAO();
                AbstractUser selectedUser = tempUserDAO.getUserById(userId);

                if (selectedUser != null) {
                    BookDAO bDAO = new BookDAO();
                    LoanDAO lDAO = new LoanDAO(tempUserDAO, bDAO);
                    BookService bService = new BookService(bDAO);
                    LoanService lService = new LoanService(lDAO, bService);


                    new UserDetailView(selectedUser, userService, lService, bService, currentUser);
                }
            } else { JOptionPane.showMessageDialog(frame, "Lütfen bir kullanıcı seçin."); }
        });

        deleteBtn.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(frame, "Kullanıcıyı silmek istiyor musunuz?", "Onay", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    userService.deleteUser(id);
                    refreshTable(userService.getAllUsers());
                }
            } else { JOptionPane.showMessageDialog(frame, "Seçim yapınız."); }
        });


        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            criteriaBox.setSelectedIndex(0);
            refreshTable(userService.getAllUsers());
        });

        frame.setVisible(true);
    }

    private void refreshTable(List<AbstractUser> users) {
        tableModel.setRowCount(0);
        for (AbstractUser u : users) {
            tableModel.addRow(new Object[]{
                    u.getId(), u.getFirstName(), u.getLastName(),
                    u.getTcNo(), u.getEmail(), u.getUserType(),
                    u.getNumber(), u.getPhone()
            });
        }
    }
}