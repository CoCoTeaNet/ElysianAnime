package net.cocotea.elysiananime.client.result.calendar;

import lombok.Data;

import java.util.Map;

/**
 * @author CoCoTea
 */
@Data
public class Rating {

	private int total;
	private Map<String, String> count;
	private double score;

}