package com.hyunki.restapi.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hyunki.restapi.accounts.Account;
import com.hyunki.restapi.accounts.AccountSerializer;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder @AllArgsConstructor @NoArgsConstructor
@Getter @Setter @EqualsAndHashCode
@Entity
public class  Event extends RepresentationModel<Event> {
    @Id @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;
    @ManyToOne
    @JsonSerialize(using = AccountSerializer.class)
    private Account manager;

    public void update() {
        // Update free
        if(this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        }
        else {
            this.free = false;
        }
        //Update offline
        if(this.location == null || this.location.isBlank() ) {
            this.offline = false;
        }
        else {
            this.offline = true;
        }
    }
}
