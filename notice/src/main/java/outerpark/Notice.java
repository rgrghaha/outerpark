package outerpark;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Notice_table")
public class Notice {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long paymentId;
    private String sendText;

    @PostPersist
    public void onPostPersist(){
        Noticed noticed = new Noticed();
        BeanUtils.copyProperties(this, noticed);
        noticed.publishAfterCommit();


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
