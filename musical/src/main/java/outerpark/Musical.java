package outerpark;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Musical_table")
public class Musical {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long musicalId;
    private String name;
    private Integer reservableSeat;

    @PostPersist
    public void onPostPersist(){
        MusicalRegistered musicalRegistered = new MusicalRegistered();
        BeanUtils.copyProperties(this, musicalRegistered);
        musicalRegistered.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        SeatModified seatModified = new SeatModified();
        BeanUtils.copyProperties(this, seatModified);
        seatModified.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getMusicalId() {
        return musicalId;
    }

    public void setMusicalId(Long musicalId) {
        this.musicalId = musicalId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Integer getReservableSeat() {
        return reservableSeat;
    }

    public void setReservableSeat(Integer reservableSeat) {
        this.reservableSeat = reservableSeat;
    }




}
