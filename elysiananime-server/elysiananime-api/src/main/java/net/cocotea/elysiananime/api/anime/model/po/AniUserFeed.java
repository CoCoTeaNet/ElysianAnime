package net.cocotea.elysiananime.api.anime.model.po;

import java.io.Serial;
import java.io.Serializable;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;
import org.sagacity.sqltoy.config.annotation.Column;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(tableName="ani_user_feed",comment="用户投喂关联表",pk_constraint="PRIMARY")
public class AniUserFeed implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id(strategy="generator",generator="org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
	@Column(name="id",comment="投喂id",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger id;

	@Column(name="from_user_id",comment="投喂人id",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger fromUserId;

	@Column(name="to_user_id",comment="被投喂人id",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger toUserId;

	@Column(name="opus_id",comment="作品id",length=19L,type=java.sql.Types.BIGINT,nativeType="BIGINT",nullable=false)
	private BigInteger opusId;

	@Column(name="create_time",comment="投喂时间",length=19L,type=java.sql.Types.DATE,nativeType="DATETIME",nullable=false)
	private LocalDateTime createTime;

	@Column(name="is_deleted",comment="是否删除",length=3L,defaultValue="0",type=java.sql.Types.TINYINT,nativeType="TINYINT",nullable=false)
	private Integer isDeleted;

	public AniUserFeed() {
	}

	public AniUserFeed(BigInteger id) {
		this.id = id;
	}
}
