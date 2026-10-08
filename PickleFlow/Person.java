/** ABSTRACTION: base class for every user. INHERITANCE: Player and Staff extend it. */
public abstract class Person {
    private String personId;      // ENCAPSULATION: all fields private
    private String fullName;
    private String contactNo;
    private String password;

    public Person(String personId, String fullName, String contactNo, String password) {
        this.personId = personId;
        setFullName(fullName);
        setContactNo(contactNo);
        setPassword(password);
    }

    public String getPersonId() { return personId; }
    public String getFullName() { return fullName; }
    public String getContactNo() { return contactNo; }
    public String getPassword() { return password; }

    // Setters validate before storing (ENCAPSULATION)
    public void setFullName(String v) { this.fullName = FileManager.clean(v, "Full name"); }
    public void setContactNo(String v) { this.contactNo = FileManager.clean(v, "Contact number"); }
    public void setPassword(String v) {
        String c = FileManager.clean(v, "Password");
        if (c.length() < 4) throw new IllegalArgumentException("Password must be at least 4 characters.");
        this.password = c;
    }

    public boolean login(String id, String pw) {
        return personId.equalsIgnoreCase(id.trim()) && password.equals(pw);
    }

    /** POLYMORPHISM: each subclass supplies its own role. */
    public abstract String getRole();

    public String toRecord() { return String.join("|", personId, fullName, contactNo, password); }
}
