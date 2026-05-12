package com.it210_prj.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /** ACTIVE = giu ghe; CANCELLED = da huy, khong chiem cho */
    @Column(nullable = false, length = 32)
    private String status;

    /** Duy nhat cho moi cap (showtime, ghe) dang ACTIVE; NULL khi CANCELLED */
    @Column(name = "hold_key", unique = true, length = 128)
    private String holdKey;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
