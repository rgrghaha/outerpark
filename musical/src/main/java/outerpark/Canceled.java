
package outerpark;

public class Canceled extends AbstractEvent {

    private Long id;
    private String musicalId;
    private Long seats;
    private String status;

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

