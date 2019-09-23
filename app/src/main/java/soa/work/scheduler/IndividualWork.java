package soa.work.scheduler;

public class IndividualWork {
    private String work_category;
    private String work_description;
    private String work_address;
    private String user_phone;
    private String created_date;
    private String assigned_to;
    private String assigned_at;
    private boolean work_completed;
    private String work_deadline;
    private String price_range_from;

    public String getPrice_range_from() {
        return price_range_from;
    }

    public void setPrice_range_from(String price_range_from) {
        this.price_range_from = price_range_from;
    }

    public String getPrice_range_to() {
        return price_range_to;
    }

    public void setPrice_range_to(String price_range_to) {
        this.price_range_to = price_range_to;
    }

    private String price_range_to;

    public IndividualWork() {
    }

    public String getWork_category() {
        return work_category;
    }

    public void setWork_category(String work_category) {
        this.work_category = work_category;
    }

    public String getWork_description() {
        return work_description;
    }

    public void setWork_description(String work_description) {
        this.work_description = work_description;
    }

    public String getWork_address() {
        return work_address;
    }

    public void setWork_address(String work_address) {
        this.work_address = work_address;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getAssigned_to() {
        return assigned_to;
    }

    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    public String getAssigned_at() {
        return assigned_at;
    }

    public void setAssigned_at(String assigned_at) {
        this.assigned_at = assigned_at;
    }

    public boolean getWork_completed() {
        return work_completed;
    }

    public void setWork_completed(boolean work_completed) {
        this.work_completed = work_completed;
    }

    public String getWork_deadline() {
        return work_deadline;
    }

    public void setWork_deadline(String work_deadline) {
        this.work_deadline = work_deadline;
    }
}
