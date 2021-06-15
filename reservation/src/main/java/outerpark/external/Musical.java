package outerpark.external;

public class Musical {

    private Long id;
    private Long musicalId;
    private String name;
    private Integer reservableSeat;

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
