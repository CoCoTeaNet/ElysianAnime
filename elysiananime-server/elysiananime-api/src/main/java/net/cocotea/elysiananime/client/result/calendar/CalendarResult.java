package net.cocotea.elysiananime.client.result.calendar;

import lombok.Data;

import java.util.List;

@Data
public class CalendarResult {

    private Weekday weekday;
    private List<Items> items;

}
