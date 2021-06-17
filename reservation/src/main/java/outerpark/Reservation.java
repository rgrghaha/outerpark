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


    // *************************************************************************
    // 동기호출 : FeignClient를 통한 구현
    // *************************************************************************
    // Reservation 수행시, Musical에 사전 등록된 좌석수(혹은 타 reservation을 통해 차감된 최종 남은 좌석수)를 초과하여 예약시도 할 경우,
    // 신규 reservation의 persist(POST)가 되지 않도록 처리하여야 한다.
    //
    // 따라서 @PostPersist를 @PrePersist, onPrePersist()로 변경하고
    // Musical MicroService에서 좌석수 체크 (http://localhost:8081/musicals의 /chkAndModifySeat) 결과가 false 로 나올 경우
    // 강제 Exception을 발생시켜서, reservation 이 POST되지 않도록 처리한다.
    // *************************************************************************
    @PrePersist
    public void onPrePersist() throws Exception{

        boolean rslt = ReservationApplication.applicationContext.getBean(outerpark.external.MusicalService.class)
            .modifySeat(this.getMusicalId(), this.getSeats().intValue());


        if(rslt) {
            Reserved reserved = new Reserved();
            BeanUtils.copyProperties(this, reserved);
            reserved.publishAfterCommit();
        }else{
            throw new Exception("e");
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
