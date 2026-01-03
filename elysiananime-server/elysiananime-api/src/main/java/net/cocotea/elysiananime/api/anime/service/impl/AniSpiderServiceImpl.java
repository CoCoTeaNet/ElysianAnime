package net.cocotea.elysiananime.api.anime.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.Forest;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniTag;
import net.cocotea.elysiananime.api.anime.service.AniSpiderService;
import net.cocotea.elysiananime.api.anime.service.AniTagService;
import net.cocotea.elysiananime.client.BangumiClient;
import net.cocotea.elysiananime.common.constant.CharConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.RssStatusEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

@Component
public class AniSpiderServiceImpl implements AniSpiderService {
    private static final Logger logger = LoggerFactory.getLogger(AniSpiderServiceImpl.class);

    @Inject
    private FileProp fileProp;

    @Db
    private LightDao lightDao;

    @Inject
    private AniTagService aniTagService;

    @Inject
    private BangumiClient bangumiClient;

    @Override
    public BigInteger addOpusFromBangumi(String bgmUrl, Integer isCover) throws BusinessException {
        // 判断校验URL是否正确
        if (!(bgmUrl.startsWith("https://bgm.tv/subject/") || bgmUrl.startsWith("http://bgm.tv/subject/"))) {
            throw new BusinessException("错误的链接");
        }
        // 详细链接
        String[] split = bgmUrl.split(CharConst.LEFT_LINE);
        String detailUrl = CharConst.LEFT_LINE + split[split.length - 2] + CharConst.LEFT_LINE + split[split.length - 1];
        // 已存在的作品
        Map<String, Object> aniOpusMapDTO = MapUtil.newHashMap(1);
        aniOpusMapDTO.put("detailUrl", detailUrl);
        AniOpus existOpus = lightDao.findOne("ani_opus_findList", aniOpusMapDTO, AniOpus.class);

        String subjectId = CollectionUtil.getLast(Arrays.stream(split).toList());
        JSONObject opusJSON = fetchOpusFromBangumi(subjectId);

        List<AniTag> aniTagList = opusJSON.getList(AniTag.class.getName(), AniTag.class);
        AniOpus aniOpus = opusJSON.getObject(AniOpus.class.getName(), AniOpus.class);
        aniOpus.setDetailInfoUrl(detailUrl);
        aniOpus.setRssStatus(RssStatusEnum.UNSUBSCRIBED.getCode());
        aniOpus.setRssLevelIndex(0);
        // 更新类型
        boolean isCoverFlag = (isCover != null && isCover == IsEnum.Y.getCode().intValue());
        logger.debug("addOpusFromBangumi >>> isCoverFlag: {}", isCoverFlag);
        if (isCoverFlag) {
            // 覆盖更新
            if (existOpus == null) {
                throw new BusinessException("作品不存在");
            }
            aniOpus.setId(existOpus.getId());
            lightDao.update(aniOpus);
        } else {
            if (existOpus != null) {
                throw new BusinessException("作品已经存在");
            }
            // 新增才默认无资源
            aniOpus.setHasResource(IsEnum.N.getCode());
            // 新增保存
            lightDao.save(aniOpus);
        }
        // 标签关联
        aniTagService.saveTagOfOpus(aniTagList, aniOpus.getId());
        return aniOpus.getId();
    }

    @Override
    public void autoFetchOpus() {
        // 获取Admin角色
        Map<String, Object> userMapDTO = MapUtil.newHashMap(1);
        BigInteger adminId = lightDao.findOne(
                "select user_id from sys_user_role where role_id = (select id from sys_role where role_key  = 'role:super:admin') limit 1"
                , userMapDTO, BigInteger.class
        );
        bangumiClient.calendar().forEach(dayObj -> dayObj.getJSONArray("items").forEach(item -> {
            JSONObject itemJSON = JSON.parseObject(item.toString());
            String url = itemJSON.getString("url");
            try {
                // 详细链接
                String[] split = url.split(CharConst.LEFT_LINE);
                String detailUrl = CharConst.LEFT_LINE + split[split.length - 2] + CharConst.LEFT_LINE + split[split.length - 1];
                // 已存在的作品
                Map<String, Object> aniOpusMapDTO = MapUtil.newHashMap(1);
                aniOpusMapDTO.put("detailUrl", detailUrl);
                AniOpus existOpus = lightDao.findOne("ani_opus_findList", aniOpusMapDTO, AniOpus.class);

                String subjectId = CollectionUtil.getLast(Arrays.stream(split).toList());
                JSONObject opusJSON = fetchOpusFromBangumi(subjectId);

                List<AniTag> aniTagList = opusJSON.getList(AniTag.class.getName(), AniTag.class);
                AniOpus aniOpus = opusJSON.getObject(AniOpus.class.getName(), AniOpus.class);

                if (existOpus == null) {
                    aniOpus.setDetailInfoUrl(detailUrl);
                    aniOpus.setHasResource(IsEnum.N.getCode());
                    aniOpus.setRssStatus(RssStatusEnum.UNSUBSCRIBED.getCode());
                    aniOpus.setRssLevelIndex(0);

                    aniOpus.setCreateBy(adminId).setUpdateBy(adminId);
                    lightDao.save(aniOpus);
                } else {
                    aniOpus.setId(existOpus.getId());
                    aniOpus.setUpdateBy(existOpus.getUpdateBy());
                    lightDao.update(aniOpus);
                }

                for (AniTag aniTag : aniTagList) {
                    aniTag.setCreateBy(adminId).setUpdateBy(adminId);
                }
                // 标签关联
                aniTagService.saveTagOfOpus(aniTagList, aniOpus.getId());
            } catch (Exception ex) {
                logger.error("autoFetchOpus >>> error,msg:{}", ex.getMessage());
            }
            ThreadUtil.sleep(6000);
        }));
    }

    private JSONObject fetchOpusFromBangumi(String subjectId) throws BusinessException {
        JSONObject subjects = bangumiClient.subjects(subjectId);
        Assert.isFalse(subjects == null, () -> new BusinessException("未找到条目"));

        AniOpus aniOpus = new AniOpus()
                .setNameOriginal(subjects.getString("name"))// 原名称
                .setNameCn(Opt.ofBlankAble(subjects.getString("name_cn")).orElseGet(() -> subjects.getString("name")))// 中文名称
                .setEpisodes(subjects.getString("total_episodes")) // 话数
                .setAniSummary(subjects.getString("summary")); // 简介

        String infobox = subjects.getString("infobox");
        for (Object json : JSON.parseArray(infobox)) {
            JSONObject item = JSON.parseObject(JSON.toJSONString(json));
            Object key = item.get("key");
            Object value = item.get("value");
            if (ObjUtil.equal("放送开始", key)) {
                aniOpus.setLaunchStart(value.toString());
            } else if (ObjUtil.equal("放送星期", key)) {
                aniOpus.setDeliveryWeek(value.toString());
            }
        }

        String tags = subjects.getString("tags");
        List<AniTag> aniTags = new ArrayList<>();
        for (Object json : JSON.parseArray(tags)) {
            JSONObject item = JSON.parseObject(json.toString());
            AniTag aniTag = new AniTag().setTagName(item.getString("name"));
            aniTags.add(aniTag);
        }

        String coverUrl = subjects.getByPath("images.common").toString();
        // 获取封面的文件名
        String[] split = coverUrl.split("/");
        String fileName = split[split.length - 1];
        aniOpus.setCoverUrl(fileName);
        // 封面保存目录
        File file = FileUtil.file(fileProp.getAnimationCoverSavePath());
        if (file.exists()) {
            ThreadUtil.execAsync(() -> Forest.get(coverUrl).setDownloadFile(file.getPath(), fileName).execute());
        } else {
            logger.warn("fetchOpusFromBangumi >>>>> coverUrl={}, fileDir={}", coverUrl, file.getPath());
        }

        return new JSONObject()
                .fluentPut(AniOpus.class.getName(), aniOpus)
                .fluentPut(AniTag.class.getName(), aniTags);
    }

}
