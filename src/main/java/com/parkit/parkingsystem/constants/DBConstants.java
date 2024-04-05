package com.parkit.parkingsystem.constants;

public class DBConstants {

    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

    public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)";
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
    public static final String GET_TICKET = "SELECT t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE FROM ticket t, parking p WHERE p.parking_number = t.parking_number AND t.VEHICLE_REG_NUMBER = ? AND t.OUT_TIME IS NULL ORDER BY t.IN_TIME LIMIT 1";
    // compte le nombre de ticket pour un vehicule selon son immatriculation
    public static final String GET_NB_TICKET = "select count(ID) from ticket where VEHICLE_REG_NUMBER = ? AND OUT_TIME IS NOT NULL";
    public static final String GET_LAST_TICKET_OUT = "SELECT t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE FROM ticket t, parking p WHERE p.parking_number = t.parking_number AND t.VEHICLE_REG_NUMBER = ? AND t.OUT_TIME IS NOT NULL ORDER BY t.OUT_TIME DESC LIMIT 1";
}
