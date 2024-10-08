/**
 *@Generated by sagacity-quickvo 5.0
 */
package net.cocotea.elysiananime.api.system.model.po;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @project sqltoy-quickstart
 * @author CoCoTea
 * @version 1.0.0 
 */
@Data
@Accessors(chain = true)
@Entity(tableName="sys_user_role",comment="用户角色关联表",pk_constraint="PRIMARY")
public class SysUserRole implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6323900700011355424L;
/*---begin-auto-generate-don't-update-this-area--*/	

	@Id(strategy="generator",generator="org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
	@Column(name="id",comment="用户角色关联id",length=3L,type=java.sql.Types.TINYINT,nullable=false)
	private BigInteger id;

	@Column(name="user_id",comment="用户id",length=19L,type=java.sql.Types.BIGINT,nullable=false)
	private BigInteger userId;

	@Column(name="role_id",comment="角色id",length=19L,type=java.sql.Types.BIGINT,nullable=false)
	private BigInteger roleId;
	/** default constructor */
	public SysUserRole() {
	}
	
	/** pk constructor */
	public SysUserRole(BigInteger id)
	{
		this.id=id;
	}
/*---end-auto-generate-don't-update-this-area--*/
}
