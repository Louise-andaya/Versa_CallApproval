package lamcomis.landaya.versa_callapproval;

public class SendCallApprovalList {
    private boolean isChecked = false;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
    String send_ca_date, send_ca_no, send_customer, send_purpose;
    public String getSend_ca_date() {
        return send_ca_date;
    }

    public void setSend_ca_date(String send_ca_date) {
        this.send_ca_date = send_ca_date;
    }

    public String getSend_ca_no() {
        return send_ca_no;
    }

    public void setSend_ca_no(String send_ca_no) {
        this.send_ca_no = send_ca_no;
    }

    public String getSend_customer() {
        return send_customer;
    }

    public void setSend_customer(String send_customer) {
        this.send_customer = send_customer;
    }

    public String getSend_purpose() {
        return send_purpose;
    }

    public void setSend_purpose(String send_purpose) {
        this.send_purpose = send_purpose;
    }

}
