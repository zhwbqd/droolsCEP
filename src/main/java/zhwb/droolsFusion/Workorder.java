package zhwb.droolsFusion;

import java.util.Date;

/**
 * @author jack.zhang
 * @since 2015/1/8
 */
public class Workorder {
    private String address;
    private long hours;
    private int costPerHour;
    private String gang;
    private Date date;

    public Workorder(String address, long hours, int costPerHour, String gang) {
        super();
        this.address = address;
        this.hours = hours;
        this.costPerHour = costPerHour;
        this.gang = gang;
        this.setDate(new Date());
    }

    public Workorder() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public int getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(int costPerHour) {
        this.costPerHour = costPerHour;
    }

    public String getGang() {
        return gang;
    }

    public void setGang(String gang) {
        this.gang = gang;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
