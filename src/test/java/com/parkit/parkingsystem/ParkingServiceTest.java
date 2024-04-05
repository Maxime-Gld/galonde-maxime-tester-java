package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    // test de la méthode processExitingVehicle()
    // quand tout est bon
    @Test
    public void processExitingVehicleTest() throws Exception {
        // given
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(argThat(spot -> spot.isAvailable()));
    }

    // test de la méthode processIncommingVehicle()
    // quand tout est bon
    @Test
    public void processIncommingVehicleTest() {
        // given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        // when
        parkingService.processIncomingVehicle();

        // then
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(argThat(spot -> !spot.isAvailable()));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    // test de la méthode processExitingVehicle()
    // avec updateTicket renvoyant false
    @Test
    public void processExitingVehicleTestUnableUpdate() {
        // given
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // when
        parkingService.processExitingVehicle();

        // then
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    // test de la méthode getNextParkingNumberIfAvailable()
    // avec un parkingNumber = 1
    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        // when
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        // then
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        assertEquals(1, spot.getId());
        assertTrue(spot.isAvailable());
    }

    // test de la méthode getNextParkingNumberIfAvailable()
    // avec un parkingNumber = 0
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

        // when
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        // then
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        assertNull(spot);
    }

    // test de la méthode getNextParkingNumberIfAvailable() avec un argument
    // invalide (3 pour le type de véhicule)
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // given
        when(inputReaderUtil.readSelection()).thenReturn(3);

        // when
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        // then
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
        assertNull(spot);
    }
}
