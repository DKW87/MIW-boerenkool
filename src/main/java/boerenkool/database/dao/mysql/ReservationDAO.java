package boerenkool.database.dao.mysql;

import boerenkool.business.model.Reservation;
import boerenkool.database.dao.GenericDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author Adnan Kilic
 * @project Boerenkool
 */

public interface ReservationDAO extends GenericDAO<Reservation> {

    @Override
    boolean storeOne (Reservation reservation);

    @Override
    List<Reservation> getAll ();

    @Override
    Optional<Reservation> getOneById (int id);

    @Override
    boolean updateOne (Reservation reservation);

    @Override
    boolean removeOneById (int id);

    boolean existsByHouseIdAndDatesOverlap (int houseId, LocalDate startDate, LocalDate endDate);

    List<Reservation> getAllReservationsByLandlord(int landlordId);

    List<Reservation> getAllReservationsByTenant(int tenantId);

    List<Reservation> getAllReservationsByHouseId(int houseId);

    List<Reservation> getAllReservationsByUserId(int userId);
}
