package test;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("=== Simple Test Application ===");
        System.out.println("If you see this, compilation is working!");
        
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✓ PostgreSQL driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("✗ PostgreSQL driver NOT found!");
            System.out.println("Make sure postgresql-42.2.29.jre7.jar is in lib folder");
        }
    }
}