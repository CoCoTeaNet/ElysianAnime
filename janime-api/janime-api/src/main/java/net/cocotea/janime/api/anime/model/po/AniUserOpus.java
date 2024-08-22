/**
 *@Generated by sagacity-quickvo 5.0
 */
package net.cocotea.janime.api.anime.model.po;

import java.io.Serial;
import java.io.Serializable;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import org.sagacity.sqltoy.config.annotation.Column;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @project sqltoy-quickstart
 * @author CoCoTea
 * @version 1.0.0 
 */
@Data
@Accessors(chain = true)
@Entity(tableName="ani_user_opus",comment="用户作品关联表",pk_constraint="PRIMARY")
public class AniUserOpus implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1503897870078379389L;
/*---begin-auto-generate-don't-update-this-area--*/	

	@Id(strategy="generator",generator="org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
	@Column(name="id",comment="用户作品关联id",length=3L,type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private BigInteger id;

	@Column(name="user_id",comment="用户id",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger userId;

	@Column(name="opus_id",comment="作品id",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger opusId;

	@Column(name="resource_url",comment="资源地址",length=200L,type=java.sql.Types.VARCHAR,nativeType="VARCHAR",nullable=true)
	private String resourceUrl;

	@Column(name="reading_num",comment="正在的播放集数",length=10L,defaultValue="0",type=java.sql.Types.INTEGER,nativeType="INT",nullable=false)
	private Integer readingNum;

	@Column(name="reading_time",comment="正在播放的时长",length=19L,defaultValue="0",type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger readingTime;

	@Column(name="read_status",comment="观看状态：0未看 1已看 2在看",length=3L,defaultValue="0",type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private Integer readStatus;

	@Column(name="create_time",comment="关联时间",length=19L,type=java.sql.Types.DATE,nativeType="DATETIME",nullable=false)
	private LocalDateTime createTime;

	@Column(name="is_share",comment="是否分享：0否 1是",length=3L,defaultValue="0",type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private Integer isShare;
	/** default constructor */
	public AniUserOpus() {
	}
	
	/** pk constructor */
	public AniUserOpus(BigInteger id)
	{
		this.id=id;
	}
/*---end-auto-generate-don't-update-this-area--*/
}
