package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        int inHour = getInTimeInMinutes(ticket);
        int outHour = getOutTimeInMinutes(ticket);

        double duration = getDurationInHours(inHour, outHour);

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(calculatePriceOfTicket(duration, Fare.CAR_RATE_PER_HOUR, discount));
                break;
            }
            case BIKE: {
                ticket.setPrice(calculatePriceOfTicket(duration, Fare.BIKE_RATE_PER_HOUR, discount));
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    private int getInTimeInMinutes(Ticket ticket) {
        return (int) ticket.getInTime().getTime() / (60 * 1000); // convert miliseconds to minutes
    }

    private int getOutTimeInMinutes(Ticket ticket) {
        return (int) ticket.getOutTime().getTime() / (60 * 1000);
    }

    private double getDurationInHours(int inHour, int outHour) {
        int duration = outHour - inHour;
        return convertMinutesToHours(duration);
    }

    private double convertMinutesToHours(int duration) {
        return duration / 60.0;
    }

    private double calculatePriceOfTicket(double durationInHour, double ratePerHour, boolean discount) {
        // 0.5 hours is free
        double price = durationInHour <= 0.5 ? 0.0 : durationInHour * ratePerHour;

        if (discount) {
            price = applyDiscountInPercent(price, 5.0); // 5% discount
        }
        return price;
    }

    private double applyDiscountInPercent(double price, double amountOfDiscount) {
        if (price <= 0.0) {
            return 0.0;
        } else {
            return price - (price * (amountOfDiscount / 100.0));
        }
    }
}