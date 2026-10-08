public class Court {
    public static final String AVAILABLE = "Available", MAINTENANCE = "Maintenance";
    private String courtId;
    private String name;
    private double hourlyRate;
    private String status;

    public Court(String courtId, String name, double hourlyRate, String status) {
        this.courtId = courtId;
        setName(name);
        setHourlyRate(hourlyRate);
        setStatus(status);
    }

    public String getCourtId() { return courtId; }
    public String getName() { return name; }
    public double getHourlyRate() { return hourlyRate; }
    public String getStatus() { return status; }
    public boolean isAvailable() { return AVAILABLE.equals(status); }

    public void setName(String v) { this.name = FileManager.clean(v, "Court name"); }
    public void setHourlyRate(double r) {
        if (r <= 0) throw new IllegalArgumentException("Hourly rate must be greater than zero.");
        this.hourlyRate = r;
    }
    public void setStatus(String s) {
        if (!AVAILABLE.equals(s) && !MAINTENANCE.equals(s))
            throw new IllegalArgumentException("Status must be Available or Maintenance.");
        this.status = s;
    }

    public String toRecord() { return String.join("|", courtId, name, String.valueOf(hourlyRate), status); }
    public static Court fromRecord(String line) {
        String[] p = line.split("\\|", -1);
        return new Court(p[0], p[1], Double.parseDouble(p[2]), p[3]);
    }
    @Override public String toString() { return courtId + " - " + name + " (PHP " + String.format("%.0f", hourlyRate) + "/hr)"; }
}
