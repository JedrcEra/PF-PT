public class Player extends Person {
    public Player(String id, String name, String contact, String password) { super(id, name, contact, password); }
    @Override public String getRole() { return "Player"; }
    public static Player fromRecord(String line) {
        String[] p = line.split("\\|", -1);
        return new Player(p[0], p[1], p[2], p[3]);
    }
}
