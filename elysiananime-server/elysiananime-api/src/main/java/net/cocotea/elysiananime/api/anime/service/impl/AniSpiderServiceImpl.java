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
import net.cocotea.elysiananime.common.constant.BgmDetailRuleConst;
import net.cocotea.elysiananime.common.constant.CharConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.RssStatusEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    final String treaty = "https:";

    final String domain = "https://bgm.tv";

    @Tran
    @Override
    public boolean addAniOpusByBgmUrl(String bgmUrl, Integer isCover) throws BusinessException {
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
        logger.debug("isCoverFlag: {}", isCoverFlag);
        boolean updateFlag;
        if (isCoverFlag) {
            // 覆盖更新
            if (existOpus == null) {
                throw new BusinessException("作品不存在");
            }
            aniOpus.setId(existOpus.getId());
            Long update = lightDao.update(aniOpus);
            updateFlag = update > 0;
        } else {
            if (existOpus != null) {
                throw new BusinessException("作品已经存在");
            }
            // 新增才默认无资源
            aniOpus.setHasResource(IsEnum.N.getCode());
            // 新增保存
            Object save = lightDao.save(aniOpus);
            updateFlag = save != null;
        }
        // 标签关联
        aniTagService.saveTagOfOpus(aniTagList, aniOpus.getId());
        return updateFlag;
    }

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

    @Deprecated
    private AniOpus doParseHtmlToAcgOpus(String html, List<AniTag> aniTagList) throws BusinessException {
        JXDocument document = JXDocument.create(html);
        AniOpus aniOpus = new AniOpus();
        // 原名称
        JXNode nameOriginal = document.selNOne(BgmDetailRuleConst.NAME_ORIGINAL);
        if (nameOriginal != null) {
            aniOpus.setNameOriginal(nameOriginal.asString());
        }
        // 中文名称
        JXNode nameCn = document.selNOne(BgmDetailRuleConst.NAME_CN);
        if (nameCn != null) {
            aniOpus.setNameCn(nameCn.asString());
        } else if (nameOriginal != null) {
            // 查不到中文名称就用原名称代替
            aniOpus.setNameCn(nameOriginal.asString());
        }
        // 话数
        JXNode episodes = document.selNOne(BgmDetailRuleConst.EPISODES);
        if (episodes != null) {
            aniOpus.setEpisodes(episodes.asString());
        }
        // 放送开始
        JXNode launchStart = document.selNOne(BgmDetailRuleConst.LAUNCH_START);
        if (launchStart != null) {
            aniOpus.setLaunchStart(launchStart.asString());
        }
        // 放送星期
        JXNode deliveryWeek = document.selNOne(BgmDetailRuleConst.DELIVERY_WEEK);
        if (deliveryWeek != null) {
            aniOpus.setDeliveryWeek(deliveryWeek.asString());
        }
        // 简介
        List<JXNode> nodes = document.selN(BgmDetailRuleConst.ACG_SUMMARY);
        StringBuilder acgSummary = new StringBuilder();
        String wrap = "<br/>";
        for (int i = 0; i < nodes.size(); i++) {
            JXNode jxNode = nodes.get(i);
            JXNode one = jxNode.selOne(BgmDetailRuleConst.COMMON_TEXT);
            if (one != null) {
                acgSummary.append(one.asString());
                if (i < nodes.size() - 1) {
                    acgSummary.append(wrap);
                }
            }
        }
        aniOpus.setAniSummary(acgSummary.toString());
        // 抓取标签
        nodes = document.selN(BgmDetailRuleConst.ACG_TAGS);
        for (JXNode jxNode : nodes) {
            JXNode one = jxNode.selOne(BgmDetailRuleConst.ACG_TAG);
            if (one != null) {
                aniTagList.add(new AniTag().setTagName(one.asString()));
            }
        }
        // 抓取封面
        JXNode coverUrlNode = document.selNOne(BgmDetailRuleConst.COVER);
        if (coverUrlNode != null) {
            String coverUrl = coverUrlNode.asString();
            StringBuilder fullUrl = new StringBuilder();
            if (coverUrl.contains("lain.bgm.tv")) {
                fullUrl.append(treaty).append(coverUrl);
            } else {
                fullUrl.append(domain).append(coverUrl);
            }
            // 获取封面的文件名
            String[] split = coverUrl.split("/");
            String fileName = split[split.length - 1];
            aniOpus.setCoverUrl(fileName);
            // 封面保存目录
            File file = FileUtil.file(fileProp.getAnimationCoverSavePath());
            if (file.exists()) {
                ThreadUtil.execAsync(() -> Forest.get(fullUrl.toString()).setDownloadFile(file.getPath(), fileName).execute());
            } else {
                logger.warn("doParseHtmlToAcgOpus >>>>> coverUrl={}, fileDir={}", fullUrl, file.getPath());
            }
        }
        return aniOpus;
    }

}
