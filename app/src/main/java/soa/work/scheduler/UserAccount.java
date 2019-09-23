package soa.work.scheduler;

public class UserAccount {
    private String account_created_on;
    private String contact_number;
    private String email;
    private String name;
    private String work_category;

    public UserAccount() {
    }

    public UserAccount(String account_created_on, String contact_number, String email, String name, String work_category) {
        this.account_created_on = account_created_on;
        this.contact_number = contact_number;
        this.email = email;
        this.name = name;
        this.work_category = work_category;
    }

    public String getAccount_created_on() {
        return account_created_on;
    }

    public void setAccount_created_on(String account_created_on) {
        this.account_created_on = account_created_on;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWork_category() {
        return work_category;
    }

    public void setWork_category(String work_category) {
        this.work_category = work_category;
    }
}
