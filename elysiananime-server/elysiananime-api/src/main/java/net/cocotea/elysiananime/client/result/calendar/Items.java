package net.cocotea.elysiananime.client.result.calendar;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * @author CoCoTea
 */
@Data
public class Items {

	private Integer id;
	private String url;
	private Integer type;
	private String name;
	@JSONField(name = "name_cn")
	private String nameCn;
	private String summary;
	@JSONField(name = "air_date")
	private Date airDate;
	@JSONField(name = "air_weekday")
	private Integer airWeekday;
	private Rating rating;
	private Images images;
	private Collection collection;

}