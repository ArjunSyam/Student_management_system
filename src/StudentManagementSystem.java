import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;  

//DBMS connection class
//done
class DBMS {
    private static final String URL = "jdbc:mysql://localhost:3306/java_project";
    private static final String USER = "root";
    private static final String PASSWORD = "Batman12";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// Base Login Page class
//done
abstract class LoginPage extends JFrame {
    protected JTextField usernameField;
    protected JPasswordField passwordField;

    public LoginPage(String title) {
        super(title);
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> authenticate());
        panel.add(loginButton);

        add(panel);
    }

    protected abstract void authenticate();
}

// Teacher Login Page
//done
class TeacherLoginPage extends LoginPage {
    public TeacherLoginPage() {
        super("Teacher Login");
    }

    @Override
    protected void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            // Database connection 
            Connection conn = DBMS.getConnection();
            //query statment
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM teacher WHERE teacher_id = ? and password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                //successful login
                System.out.println("Teacher successflly logged in: "+rs.getString("name"));

                dispose();
                new TeacherPortal(username).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error");
            e.printStackTrace();
        }
    }
}

// Student Login Page
//done
class StudentLoginPage extends LoginPage {
    public StudentLoginPage() {
        super("Student Login");
    }

    @Override
    protected void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            // Database connection 
            Connection conn = DBMS.getConnection();
            //query statment
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM student WHERE usn = ? and password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                //successful login
                System.out.println("Student successflly logged in: "+rs.getString("name"));

                dispose();
                new StudentPortal(username).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error");
            e.printStackTrace();
        }
    }
}

class AdminPortal extends JFrame {
    private void addStudentForm(JPanel form) {
        String[] labels = {"USN:", "Name:", "Section:", "Branch:", "Semester:"};
        JTextField[] fields = new JTextField[labels.length];
        
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            fields[i] = new JTextField();
            form.add(fields[i]);
        }

        JButton submit = new JButton("Add Student");
        submit.addActionListener(e -> {
            // Add database connection code here
            JOptionPane.showMessageDialog(this, "Student Added Successfully");
        });
        form.add(submit);
    }
}

// Teacher Portal
class TeacherPortal extends JFrame {
    private JPanel inputPanel;
    private DefaultTableModel tableModel;
    private JComboBox<String> semesterBox, sectionBox;
    private JLabel teacherNameLabel;

    public TeacherPortal(String teacherId) {
        super("Teacher Portal");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        try {
            Connection conn = DBMS.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM teacher WHERE teacher_id = ?");
            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                teacherNameLabel = new JLabel("Welcome, " + rs.getString("name"));
                teacherNameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 20));
                teacherNameLabel.setHorizontalAlignment(JLabel.RIGHT);
                add(teacherNameLabel, BorderLayout.NORTH);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Action buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addActionButtons(buttonPanel,teacherId);
        add(buttonPanel, BorderLayout.WEST);

        // Input panel
        inputPanel = new JPanel(new CardLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Form"));
        add(inputPanel, BorderLayout.CENTER);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setupTablePanel(tablePanel);
        add(tablePanel, BorderLayout.EAST);
    }

    private void addActionButtons(JPanel panel,String teacherId) {
        String[] buttons = {"Update Marks", "Update Attendance"};
        for (String button : buttons) {
            JButton btn = new JButton(button);
            btn.addActionListener(e -> showForm(button,teacherId));
            panel.add(btn);
        }
    }

    private void setupTablePanel(JPanel panel) {
        // Table
        String[] columns = {"USN", "Name", "Section", "Branch", "Semester"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void showForm(String formType,String teacherId) {
        inputPanel.removeAll();
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        switch (formType) {
            case "Update Marks":
                updateMarksForm(form,teacherId);
                break;
            case "Update Attendance":
                updateAttendanceForm(form,teacherId);
                break;
        }

        inputPanel.add(form);
        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void updateMarksForm(JPanel form,String teacherId) {
        form.add(new JLabel("USN:"));
        JComboBox<String> usnDropdown = new JComboBox<>();
        
        try {
            Connection conn = DBMS.getConnection();

            PreparedStatement section = conn.prepareStatement("SELECT section FROM teacher where teacher_id = ?");
            section.setString(1, teacherId);
            ResultSet rs_sec = section.executeQuery();
            rs_sec.next();

            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT usn FROM student where section = ?");
            stmt.setString(1, rs_sec.getString("section"));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                usnDropdown.addItem(rs.getString("usn"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        form.add(usnDropdown);

        // Add exam type dropdown
        form.add(new JLabel("Exam:"));
        String[] examTypes = {"CIE1", "CIE2", "CIE3", "END_SEM"};
        JComboBox<String> examTypeDropdown = new JComboBox<>(examTypes);
        form.add(examTypeDropdown);

        form.add(new JLabel("Marks:"));
        JTextField marksField = new JTextField();
        form.add(marksField);

        JButton submit = new JButton("Update Marks");
        submit.addActionListener(e -> {
            try{
                Connection conn = DBMS.getConnection();

                //get marks
                double marks = Double.parseDouble(marksField.getText());
                String usn = (String) usnDropdown.getSelectedItem();
                String exam = (String) examTypeDropdown.getSelectedItem();

                if ((exam == "CIE1" && marks > 20) || ((exam == "CIE2" || exam == "CIE3") && marks > 25) || (exam == "END_SEM" && marks > 30)) {
                    JOptionPane.showMessageDialog(null, "Marks are too high");
                }

                else {
                    PreparedStatement course = conn.prepareStatement("SELECT c_id FROM teacher where teacher_id = ?");
                    course.setString(1, teacherId);
                    ResultSet rs_course = course.executeQuery();
                    rs_course.next();

                    PreparedStatement stmt_sub = conn.prepareStatement("UPDATE student_takes_course SET " + exam + " = ? where usn = ? and c_id = ?");
                    stmt_sub.setDouble(1, marks);
                    stmt_sub.setString(2, usn);
                    stmt_sub.setString(3, rs_course.getString("c_id"));
                    int rowsAffected = stmt_sub.executeUpdate(); 

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Marks Updated Successfully");
                    } else {
                        JOptionPane.showMessageDialog(this, "No records were updated");
                    }
                }

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Error Updating Marks");
                ex.printStackTrace();
            }
        });
        form.add(submit);
    }

    private void updateAttendanceForm(JPanel form,String teacherId) {
        form.add(new JLabel("USN:"));
        JComboBox<String> usnDropdown = new JComboBox<>();
        
        try {
            Connection conn = DBMS.getConnection();

            PreparedStatement section = conn.prepareStatement("SELECT section FROM teacher where teacher_id = ?");
            section.setString(1, teacherId);
            ResultSet rs_sec = section.executeQuery();
            rs_sec.next();

            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT usn FROM student where section = ?");
            stmt.setString(1, rs_sec.getString("section"));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                usnDropdown.addItem(rs.getString("usn"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        form.add(usnDropdown);

        form.add(new JLabel("Attendance (%):"));
        JTextField attendanceField = new JTextField();
        form.add(attendanceField);

        JButton submit = new JButton("Update Attendance");
        submit.addActionListener(e -> {
            // Add database connection code here
            try{
                Connection conn = DBMS.getConnection();

                //get attendance
                double attendance = Double.parseDouble(attendanceField.getText());
                String usn = (String) usnDropdown.getSelectedItem();
                
                PreparedStatement course = conn.prepareStatement("SELECT c_id FROM teacher where teacher_id = ?");
                course.setString(1, teacherId);
                ResultSet rs_course = course.executeQuery();
                rs_course.next();

                PreparedStatement smt_attendance = conn.prepareStatement("UPDATE student_takes_course SET attendance = ? where usn = ? and c_id = ?");
                smt_attendance.setDouble(1, attendance);
                smt_attendance.setString(2, usn);
                smt_attendance.setString(3, rs_course.getString("c_id"));
                int rowsAffected = smt_attendance.executeUpdate(); 

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Attendance Updated Successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "No records were updated");
                }

                JOptionPane.showMessageDialog(this, "Attendance Updated Successfully");
            } catch(Exception ex ){
                JOptionPane.showMessageDialog(this, "Error Updating Attendance");
                ex.printStackTrace();
            }
        });
        form.add(submit);
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        // Add database query code here to populate table based on semester and section
        // For now, adding sample data
        tableModel.addRow(new Object[]{"1BM20CS001", "John Doe", "A", "CSE", "4"});
    }
}

// Student Portal
// done
class StudentPortal extends JFrame {
    public StudentPortal(String usn) {
        super("Student Portal - " + usn);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Personal Details Panel
        JPanel personalPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        personalPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        addPersonalDetails(personalPanel, usn);
        tabbedPane.addTab("Personal Details", personalPanel);

        // Academic Details Panel
        JPanel academicPanel = new JPanel(new BorderLayout(10, 10));
        academicPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        addAcademicDetails(academicPanel, usn);
        tabbedPane.addTab("Academic Details", academicPanel);

        add(tabbedPane);
    }

    private void addPersonalDetails(JPanel panel, String usn) {
        try {
            // Database connection 
            Connection conn = DBMS.getConnection();
            //query statment
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM student WHERE usn = ?");
            stmt.setString(1, usn);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            String[][] details = {
                {"USN:", rs.getString("usn")},
                {"Name:", rs.getString("name")},
                {"Branch:", rs.getString("branch")},
                {"Semester:", String.valueOf(rs.getInt("semester"))},
                {"Section:", rs.getString("section")}
            };
    
            for (String[] detail : details) {
                panel.add(new JLabel(detail[0]));
                panel.add(new JLabel(detail[1]));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error");
            e.printStackTrace();
        }
    }

    private void addAcademicDetails(JPanel panel, String usn) {
        try {
            // Database connection 
            Connection conn = DBMS.getConnection();
            //query statment
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM student_takes_course s join course c on s.c_id = c.c_id WHERE usn = ?");
            stmt.setString(1, usn);

            ResultSet rs = stmt.executeQuery();

            // Create table for marks
            String[] columns = {"Subject", "CIE 1 (20)","CIE 2 (25)","CIE 3 (25)","Final (30)", "Attendance %","next_exam_date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            JTable table = new JTable(model);

            while(rs.next()) {
                String subject = rs.getString("c_name");
                String cie1 = rs.getString("CIE1");
                String cie2 = rs.getString("CIE2");
                String cie3 = rs.getString("CIE3");
                String end_sem = rs.getString("end_sem");
                String attendance = rs.getString("attendance");
                String next_exam = rs.getString("next_exam_date");

                model.addRow(new Object[]{subject,cie1,cie2,cie3,end_sem,attendance,next_exam});
            }

            panel.add(new JScrollPane(table));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error");
            e.printStackTrace();
        }
    }
}

// Main class
public class StudentManagementSystem extends JFrame {
    public StudentManagementSystem() {
        super("Student Management System");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton teacherButton = new JButton("Teacher Login");
        teacherButton.addActionListener(e -> {
            dispose();
            new TeacherLoginPage().setVisible(true);
        });

        JButton studentButton = new JButton("Student Login");
        studentButton.addActionListener(e -> {
            dispose();
            new StudentLoginPage().setVisible(true);
        });

        panel.add(teacherButton);
        panel.add(studentButton);
        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentManagementSystem().setVisible(true));
    }
}