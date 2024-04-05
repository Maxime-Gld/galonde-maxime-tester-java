package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        // TODO: check that a ticket is actualy saved in DB and Parking table is updated
        // with availability
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticket);

        assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
        assertEquals(ticket.getPrice(), 0.0);
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertNotNull(ticket.getParkingSpot());
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertSame(ticket.getParkingSpot().getParkingType(), ParkingType.CAR);
        assertNotEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), ticket.getParkingSpot().getId());
    }

    @Test
    public void testParkingLotExit() {
        testParkingACar();

        // récupération du ticket avant la sortie
        Ticket ticketBeforeOut = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketBeforeOut);
        assertNull(ticketBeforeOut.getOutTime());

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        // TODO: check that the fare generated and out time are populated correctly in
        // the database

        // recuperation du ticket apres la sortie
        Ticket ticketAfterOut = ticketDAO.getLastTicketOut("ABCDEF");
        assertNotNull(ticketAfterOut);

        // verification que les tickets sont bien les mêmes
        assertEquals(ticketBeforeOut.getVehicleRegNumber(), ticketAfterOut.getVehicleRegNumber());
        assertEquals(ticketBeforeOut.getId(), ticketAfterOut.getId());
        assertEquals(ticketBeforeOut.getParkingSpot().getId(), ticketAfterOut.getParkingSpot().getId());
        assertEquals(ticketBeforeOut.getInTime(), ticketAfterOut.getInTime());

        // vérification du TODO
        assertNotNull(ticketAfterOut.getOutTime());
        assertEquals(0.0, ticketAfterOut.getPrice());
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        // premier passage sur le parking
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        // deuxieme passage sur le parking en simulant un utilisateur récurrent avec
        // une durée de 1h
        Date inTime = new Date(System.currentTimeMillis() - (10 * 60 * 60 * 1000)); // 1h
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();

        // TODO: tester le calcul du prix en fonction d'un utilisateur récurrent
        // utilisant une voiture
        Ticket ticketDiscount = ticketDAO.getLastTicketOut("ABCDEF");
        assertNotNull(ticketDiscount);
        double duration = ((ticketDiscount.getOutTime().getTime() / (60 * 1000))
                - (ticketDiscount.getInTime().getTime() / (60 * 1000))) / 60.0;
        double priceDiscount = (duration * Fare.CAR_RATE_PER_HOUR) * 0.95;
        assertEquals(priceDiscount, ticketDiscount.getPrice(), 0.001);
    }

}
