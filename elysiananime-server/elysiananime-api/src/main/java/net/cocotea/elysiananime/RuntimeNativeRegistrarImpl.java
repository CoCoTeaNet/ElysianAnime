package net.cocotea.elysiananime;

import org.noear.solon.annotation.Component;
import org.noear.solon.aot.RuntimeNativeMetadata;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.aot.hint.MemberCategory;
import org.noear.solon.core.AppContext;

@Component
public class RuntimeNativeRegistrarImpl implements RuntimeNativeRegistrar {
    @Override
    public void register(AppContext context, RuntimeNativeMetadata metadata) {
        metadata.registerReflection(org.sagacity.sqltoy.solon.integration.SqlToyPluginImpl.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);
        metadata.registerReflection(net.cocotea.elysiananime.handler.SqlToyUnifyFieldsHandler.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);

        // ========== 系统模块模型类 ==========
        // PO 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysUser.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysRole.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysMenu.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysUserRole.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysRoleMenu.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysDictionary.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysFile.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysNotify.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysTheme.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysVersion.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.po.SysLog.class);

        // DTO 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysLoginDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysUserAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysUserUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysUserPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysLoginUserUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysRoleAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysRoleUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysRolePageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysMenuAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysMenuUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysMenuPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysMenuTreeDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysDictionaryAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysDictionaryUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysDictionaryPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysDictionaryTreeDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysThemeUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysVersionAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysVersionUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysVersionPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysFileAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysFileUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysFilePageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysLogAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysLogPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysLogUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO.class);
        // 系统模块 - 静态内部 Query 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysRolePageDTO.Query.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysMenuPageDTO.Query.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysLogPageDTO.Query.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysFilePageDTO.Query.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.dto.SysVersionPageDTO.Query.class);

        // VO 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysUserVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysRoleVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysRoleMenuVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysMenuVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysMenuTreeVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysDictionaryVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysThemeVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysVersionVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysCaptchaVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysFileVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysLogVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysLoginUserVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysNotifyVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysOverviewVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SysUserRoleVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.system.model.vo.SystemInfoVO.class);

        // ========== 动漫模块模型类 ==========
        // PO 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.po.AniOpus.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.po.AniTag.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.po.AniOpusGroup.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.po.AniOpusTag.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.po.AniUserOpus.class);

        // DTO 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniAddOpusTorrentDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniOpusAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniOpusHomeDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniOpusPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniOpusUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniRssDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusAddDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusPageDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUpdateDTO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.RssWorksStatusDTO.class);
        // 静态内部 Query 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.dto.AniOpusPageDTO.Query.class);

        // VO 类
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniOpusHomeVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniOpusInfoVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniOpusTagVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniOpusVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusSharesVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniUserOpusVO.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.model.vo.AniVideoVO.class);

        // RSS 模型类
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.rss.model.RenameInfo.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.rss.model.QbInfo.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.rss.model.MkXmlDetail.class);
        registerModelClass(metadata, net.cocotea.elysiananime.api.anime.rss.model.MkXmlItem.class);
    }

    /**
     * 注册模型类的反射和序列化信息
     */
    private void registerModelClass(RuntimeNativeMetadata metadata, Class<?> clazz) {
        metadata.registerReflection(clazz,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.DECLARED_FIELDS);
        metadata.registerSerialization(clazz);
    }
}
