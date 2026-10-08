public class Staff extends Person {
    public Staff(String id, String name, String contact, String password) { super(id, name, contact, password); }
    @Override public String getRole() { return "Staff"; }
    public static Staff fromRecord(String line) {
        String[] p = line.split("\\|", -1);
        return new Staff(p[0], p[1], p[2], p[3]);
    }
}
