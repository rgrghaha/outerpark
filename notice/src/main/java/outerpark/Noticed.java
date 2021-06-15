package outerpark;

public class Noticed extends AbstractEvent {

    private Long id;
    private Long paymentId;
    private String sendText;

    public Noticed(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    public String getSendText() {
        return sendText;
    }

    public void setSendText(String sendText) {
        this.sendText = sendText;
    }
}
