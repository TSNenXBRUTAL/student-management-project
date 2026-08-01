import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
public class StudentDAO {
 
    public void addStudent(Student s) throws Exception {
 
        String sql = "INSERT INTO students(name,course,email,phone) VALUES(?,?,?,?)";
 
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, s.getName());
            ps.setString(2, s.getCourse());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
 
            ps.executeUpdate();
        }
    }
 
    public void deleteStudent(int id) throws Exception {
 
        String sql = "DELETE FROM students WHERE id=?";
 
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
 
    public void updateStudent(Student s) throws Exception {
 
        String sql = "UPDATE students SET name=?,course=?,email=?,phone=? WHERE id=?";
 
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, s.getName());
            ps.setString(2, s.getCourse());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setInt(5, s.getId());
 
            ps.executeUpdate();
        }
    }
 
    // Returns every student row, used to populate the table on load/refresh.
    public List<Student> getAllStudents() throws Exception {
 
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students"; // temporarily removed ORDER BY id to debug
 
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
 
            // ---- TEMPORARY DEBUG: prints the real column names to the console ----
            ResultSetMetaData meta = rs.getMetaData();
            System.out.println("---- students table columns ----");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.println(i + ": " + meta.getColumnName(i));
            }
            System.out.println("---------------------------------");
            // ------------------------------------------------------------------
 
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        }
        return list;
    }
 
    // Small stats used by the dashboard's summary bar.
    public int getStudentCount() throws Exception {
 
        String sql = "SELECT COUNT(*) FROM students";
 
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
 
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
 
    public int getDistinctCourseCount() throws Exception {
 
        String sql = "SELECT COUNT(DISTINCT course) FROM students WHERE course IS NOT NULL AND course <> ''";
 
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
 
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Student count grouped by course - used to feed the pie chart on the dashboard tab.
    public Map<String, Integer> getCourseWiseCount() throws Exception {

        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT course, COUNT(*) AS cnt FROM students " +
                     "WHERE course IS NOT NULL AND course <> '' " +
                     "GROUP BY course ORDER BY cnt DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                counts.put(rs.getString("course"), rs.getInt("cnt"));
            }
        }
        return counts;
    }
}