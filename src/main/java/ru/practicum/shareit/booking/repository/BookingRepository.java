package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {


    List<Booking> findByBookerId(Long bookerId, Sort sort);

    @Query("select b " +
            "from Booking as b " +
            "join b.booker as u " +
            "where u.id  = ?1 " +
            "and ?2 between b.start and b.end")
    List<Booking> findByBookerIdAndCurrentState(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus bookingStatus, Sort sort);

    String QUERY = "select b " +
            "from Booking as b " +
            "join b.item as it " +
            "where it.id in " +
            "(select it.id from it " +
            "where it.ownerId = ?1)";

    @Query(QUERY + " order by b.start desc")
    List<Booking> findByOwnerId(Long ownerId);

    @Query(QUERY +
            " and ?2 between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findByOwnerIdCurrentState(Long ownerId, LocalDateTime now);

    @Query(QUERY +
            " and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerIdPastState(Long ownerId, LocalDateTime now);

    @Query(QUERY +
            " and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerIdFutureState(Long ownerId, LocalDateTime now);

    @Query(QUERY +
            " and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus bookingStatus);

    List<Booking> findByItemIdOrderByStart(Long itemId);
}
