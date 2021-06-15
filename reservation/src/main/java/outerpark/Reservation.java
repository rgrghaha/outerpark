package outerpark;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String musicalId;
    private Long price;
    private Long seats;
    private String status;

    @PostPersist
    public void onPostPersist(){

        boolean rslt = ReservationApplication.applicationContext.getBean(outerpark.external.MusicalService.class)
            .modifySeat(this.getMusicalId(), this.getSeats().intValue());


        if(rslt) {
            Reserved reserved = new Reserved();
            BeanUtils.copyProperties(this, reserved);
            reserved.publishAfterCommit();
        }
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.
    }

    @PreRemove
    public void onPreRemove(){
        
        Canceled canceled = new Canceled();
        BeanUtils.copyProperties(this, canceled);
        canceled.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMusicalId() {
        return musicalId;
    }

    public void setMusicalId(String musicalId) {
        this.musicalId = musicalId;
    }
    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Long getSeats() {
        return seats;
    }

    public void setSeats(Long seats) {
        this.seats = seats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
