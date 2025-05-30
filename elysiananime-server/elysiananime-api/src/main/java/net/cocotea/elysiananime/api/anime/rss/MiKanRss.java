package net.cocotea.elysiananime.api.anime.rss;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson2.JSONArray;
import com.dtflys.forest.Forest;
import net.cocotea.elysiananime.api.anime.model.dto.AniRssDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlDetail;
import net.cocotea.elysiananime.api.anime.rss.model.RenameInfo;
import net.cocotea.elysiananime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.elysiananime.api.system.service.SysNotifyService;
import net.cocotea.elysiananime.api.anime.service.AniOpusService;
import net.cocotea.elysiananime.api.anime.service.AniUserOpusService;
import net.cocotea.elysiananime.common.constant.CharConst;
import net.cocotea.elysiananime.common.constant.NotifyConst;
import net.cocotea.elysiananime.common.constant.RedisKeyConst;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.enums.LevelEnum;
import net.cocotea.elysiananime.common.enums.RssStatusEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.common.service.RedisService;
import net.cocotea.elysiananime.properties.DefaultProp;
import net.cocotea.elysiananime.properties.EmailSenderProp;
import net.cocotea.elysiananime.properties.QbittorrentProp;
import net.cocotea.elysiananime.util.QbApiUtils;
import net.cocotea.elysiananime.util.ResUtils;
import net.cocotea.elysiananime.api.anime.rss.model.MkXmlItem;
import net.cocotea.elysiananime.api.anime.rss.model.QbInfo;
import net.cocotea.elysiananime.util.RuleUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.cocotea.elysiananime.common.constant.RedisKeyConst.RSS_RESULT_CACHE;

/**
 * 蜜柑rss订阅服务
 *
 * @author CoCoTea
 * @since v1
 */
@Component
public class MiKanRss {
    private static final Logger log = LoggerFactory.getLogger(MiKanRss.class);

    @Inject
    private EmailSenderProp emailSenderProp;

    @Inject
    private DefaultProp defaultProp;

    @Inject
    private QbApiUtils qbApiUtils;

    @Inject
    private QbittorrentProp qbittorrentProp;

    @Inject
    private AniOpusService aniOpusService;

    @Inject
    private ResUtils resUtils;

    @Inject
    private SysNotifyService sysNotifyService;

    @Inject
    private AniUserOpusService aniUserOpusService;

    @Inject
    private RedisService redisService;

    /**
     * 保存并订阅rss
     */
    public void requestRss(String rssUrl, AniOpus opus) {
        String dir = resUtils.findASS(2) + opus.getNameCn() + CharConst.LEFT_LINE;
        List<RenameInfo> list = getRenames(rssUrl, opus);

        for (RenameInfo renameInfo : list) {
            boolean existRes = resUtils.exist(opus.getNameCn(), renameInfo.getRename());
            if (!existRes) {
                log.info("requestRss >>>>> saveDir={}", dir);
                String added = qbApiUtils.addNewTorrent(renameInfo.getEnclosureUrl(), dir, renameInfo.getRename());
                log.info("requestRss >>>>> {}下载请求完成，响应消息：{}", opus.getNameCn(), added);
            } else {
                log.warn("requestRss >>>>> 《{}》资源已存在,rename：{},title：{}",
                        opus.getNameCn(),
                        renameInfo.getRename(),
                        renameInfo.getTitle()
                );
            }
        }
    }

    private void doRemoveResourceIfExist(String opusDir) {
        JSONArray list = qbApiUtils.info("all");
        for (Object obj : list) {
            QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);

            File resFile = FileUtil.file(qbInfo.getContentPath());
            File dirFile = FileUtil.file(opusDir);
            log.info("doRemoveResourceIfExist >>>>> resFile:{},dirFile:{}", resFile.getPath(), dirFile.getPath());
            if (!resFile.getPath().startsWith(dirFile.getPath())) {
                continue;
            }

            // 暂停
            qbApiUtils.pause(qbInfo.getHash());
            // 移除
            qbApiUtils.delete(qbInfo.getHash(), true);
        }

        ThreadUtil.sleep(5, TimeUnit.SECONDS);
        File[] ls = FileUtil.ls(opusDir);
        for (File f : ls) {
            FileUtil.del(f);
        }
    }

    public List<RenameInfo> getRenames(AniRssDTO aniRssDTO) throws BusinessException {
        AniOpus opus = aniOpusService.loadById(aniRssDTO.getId());
        // rss订阅配置参数
        opus.setRssUrl(aniRssDTO.getRssUrl());
        opus.setRssLevelIndex(aniRssDTO.getRssLevelIndex());
        opus.setRssFileType(aniRssDTO.getRssFileType());
        opus.setRssOnlyMark(aniRssDTO.getRssOnlyMark());
        if (StrUtil.isNotBlank(aniRssDTO.getRssExcludeRes())) {
            opus.setRssExcludeRes(aniRssDTO.getRssExcludeRes());
        }
        return getRenames(aniRssDTO.getRssUrl(), opus);
    }

    public List<RenameInfo> getRenames(String rssUrl, AniOpus opus) {
        String result;

        String cacheKey = StrUtil.format(RSS_RESULT_CACHE, rssUrl);
        String cacheContent = redisService.get(cacheKey);
        if (StrUtil.isBlank(cacheContent)) {
            result = Forest.get(rssUrl).executeAsString();
            redisService.saveByHours(cacheKey, result, 2);
        } else {
            result = cacheContent;
        }

        log.debug("订阅地址返回结果，result：{}", result);
        List<MkXmlItem> mkXmlItemList = doParseXmlItems(result);
        List<RenameInfo> renameInfoList = new ArrayList<>();

        for (MkXmlItem mkXmlItem : mkXmlItemList) {
            String title = mkXmlItem.getTitle();
            // 是标识的资源
            boolean remarkRes = RuleUtils.isRemarkRes(title, opus.getRssOnlyMark());
            if (!remarkRes) {
                log.warn("解析完成 >>>>> 不是标识资源，不进行下载, title={}, mark={}", title, opus.getRssOnlyMark());
                continue;
            }
            if (!resUtils.existDir(opus.getNameCn())) {
                log.warn("解析完成 >>>>> 目录不存在,opus：{}", opus.getNameCn());
                continue;
            }
            // 排除的资源
            if (StrUtil.isNotBlank(opus.getRssExcludeRes())) {
                boolean excludeRes = RuleUtils.isExcludeRes(title, opus.getRssExcludeRes());
                if (excludeRes) {
                    log.warn("解析结果 >>>>> 标识为排除的资源，不进行下载, title：{}, mark：{}", title, opus.getRssExcludeRes());
                    continue;
                }
            }
            // 资源是否存在
            String rename = RuleUtils.rename(title, opus.getRssLevelIndex(), opus.getRssFileType());

            RenameInfo renameInfo = new RenameInfo()
                    .setRename(rename).setTitle(title).setEnclosureUrl(mkXmlItem.getEnclosureUrl());
            renameInfoList.add(renameInfo);
        }

        return renameInfoList;
    }

    /**
     * 解析rss xml内容
     *
     * @param s xml字符串
     */
    public List<MkXmlItem> doParseXmlItems(String s) {
        Document xml = XmlUtil.parseXml(s);
        NodeList list = xml.getElementsByTagName("item");

        List<MkXmlItem> ls = new ArrayList<>(list.getLength());

        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            NodeList childNodes = item.getChildNodes();
            MkXmlItem mkXmlItem = new MkXmlItem();
            // 查找xml节点信息
            findItemInfo(childNodes, mkXmlItem);
            ls.add(mkXmlItem);
        }

        return ls;
    }

    public void findItemInfo(NodeList nodeList, MkXmlItem mkXmlItem) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String nodeName = item.getNodeName();
            switch (nodeName) {
                case "guid":
                    mkXmlItem.setGuid(item.getTextContent());
                    break;
                case "link":
                    mkXmlItem.setLink(item.getTextContent());
                    break;
                case "title":
                    mkXmlItem.setTitle(item.getTextContent());
                    break;
                case "description":
                    mkXmlItem.setDescription(item.getTextContent());
                    break;
                case "enclosure":
                    NamedNodeMap map = item.getAttributes();
                    mkXmlItem.setEnclosureLength(String.valueOf(map.getNamedItem("length").getNodeValue()));
                    mkXmlItem.setEnclosureUrl(String.valueOf(map.getNamedItem("url").getNodeValue()));
                    break;
                case "torrent":
                    NodeList list = item.getChildNodes();
                    findBtSource(list, mkXmlItem);
                    break;
            }
        }
    }

    public void findBtSource(NodeList nodeList, MkXmlItem mkXmlItem) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            String nodeName = item.getNodeName();
            switch (nodeName) {
                case "link":
                    mkXmlItem.setTorrentLink(item.getTextContent());
                    break;
                case "contentLength":
                    mkXmlItem.setTorrentContentLength(item.getTextContent());
                    break;
                case "pubDate":
                    mkXmlItem.setTorrentPubDate(item.getTextContent());
                    break;
            }
        }
    }

    /**
     * 获取订阅番剧并下载
     */
    public void doFindRssOpusAndDownload() throws InterruptedException {
        List<AniOpus> aniOpusList = aniOpusService.getRssOpus(RssStatusEnum.SUBSCRIBING.getCode());
        log.info("doFindRssOpusAndDownload------->start...opusSize={}", aniOpusList.size());
        for (AniOpus opus : aniOpusList) {
            log.info("{}-------->开始下载", opus.getNameCn());
            String rssUrl = opus.getRssUrl();
            if (StrUtil.isNotBlank(rssUrl)) {
                try {
                    log.info("存在订阅地址-------->rssUrl: {}", rssUrl);
                    requestRss(rssUrl, opus);
                } catch (Exception e) {
                    log.error("下载出现异常，作品是:{}, 堆栈信息:{}", opus.getNameCn(), e.getMessage());
                } finally {
                    Thread.sleep(3000L);
                }
            } else {
                log.warn("没有订阅地址, opus={}", opus);
            }
        }
    }

    /**
     * 订阅番剧
     *
     * @param aniRssDTO {@link AniRssDTO}
     * @return boolean
     */
    public boolean subscribe(AniRssDTO aniRssDTO) throws BusinessException {
        String baseMsg = "rss[subscribe]";
        log.info(baseMsg.concat("param={}"), aniRssDTO);
        AniOpus opus = aniOpusService.loadById(aniRssDTO.getId());
        opus.setHasResource(IsEnum.Y.getCode());
        opus.setRssStatus(RssStatusEnum.SUBSCRIBING.getCode());
        // rss订阅配置参数
        opus.setRssUrl(aniRssDTO.getRssUrl());
        opus.setRssLevelIndex(aniRssDTO.getRssLevelIndex());
        opus.setRssFileType(aniRssDTO.getRssFileType());
        opus.setRssOnlyMark(aniRssDTO.getRssOnlyMark());
        if (StrUtil.isNotBlank(aniRssDTO.getRssExcludeRes())) {
            opus.setRssExcludeRes(aniRssDTO.getRssExcludeRes());
        }
        // 创建订阅资源目录
        String path = resUtils.findASS(2) + opus.getNameCn();
        log.info(baseMsg.concat("path={}"), path);
        FileUtil.mkdir(path);
        // 重新刷新一下rss订阅
        if (StrUtil.isNotBlank(opus.getRssUrl())) {
            ThreadUtil.execAsync(() -> {
                // 覆盖更新
                if (aniRssDTO.getRssOverride() == IsEnum.Y.getCode().intValue()) {
                    String dir = path + CharConst.LEFT_LINE;
                    File[] files = FileUtil.ls(dir);
                    if (files.length > 0) {
                        doRemoveResourceIfExist(dir);
                    }
                }
                requestRss(opus.getRssUrl(), opus);
            });
        }
        return aniOpusService.update(opus);
    }

    /**
     * rss订阅开关控制
     *
     * @param opusId 作品id
     * @return true
     */
    public boolean closeSubscribe(BigInteger opusId) {
        AniOpus aniOpus = new AniOpus().setId(opusId).setRssStatus(RssStatusEnum.SUBSCRIPTION_COMPLETED.getCode());
        return aniOpusService.update(aniOpus);
    }

    /**
     * 解析资源信息详细
     *
     * @param rssUrl rss地址
     * @return {@link MkXmlDetail}
     */
    public MkXmlDetail doParseMkXmlDetail(String rssUrl) {
        String result = Forest.get(rssUrl).executeAsString();
        List<MkXmlItem> mkXmlItems = doParseXmlItems(result);
        // 标题碎片
        List<List<String>> titleFragmentList = new ArrayList<>();
        // 集数索引
        List<List<String>> episodeIndexList = new ArrayList<>();
        for (MkXmlItem item : mkXmlItems) {
            String[] split = item.getTitle().split("[\\[\\]/@#$%^&*()（）【】\\s]");
            List<String> titleFragment = Arrays.stream(split).filter(str -> !str.isEmpty()).collect(Collectors.toList());
            titleFragmentList.add(titleFragment);
            List<String> episodeIndex = ReUtil.findAllGroup0(RegexPool.NUMBERS, item.getTitle());
            episodeIndexList.add(episodeIndex);
            // 将数字字符高亮显示
            String title = item.getTitle();
            List<String> list = ReUtil.findAllGroup0(RegexPool.NUMBERS, title);
            for (String episodeStr : list) {
                title = title.replaceAll(episodeStr, "<span style=\"color:#f38181\">" + episodeStr + "</span>");
            }
            item.setTitleHtml(title);
        }
        return new MkXmlDetail()
                .setItemList(mkXmlItems)
                .setTitleFragmentList(titleFragmentList)
                .setEpisodeIndexList(episodeIndexList);
    }

    private void doNotifyCatchEx(AniOpus opus, File targetFile) {
        try {
            doNotify(opus, targetFile);
        } catch (BusinessException ex) {
            log.warn("doNotifyCatchEx >>>>> RSS通知失败，msg：{}", ex.getMessage());
        }
    }

    private void doNotify(AniOpus opus, File targetFile) throws BusinessException {
        SysNotifyAddDTO sysNotifyAddDTO = new SysNotifyAddDTO()
                .setTitle("【" + opus.getNameCn() + "】更新啦~~~")
                .setMemo("资源名：" + targetFile.getName())
                .setJumpUrl(String.valueOf(opus.getId()))
                .setNotifyTime(DateUtil.date().toTimestamp())
                .setLevel(LevelEnum.INFO.getCode())
                .setIsGlobal(IsEnum.Y.getCode())
                .setNotifyType(NotifyConst.OPUS_UPDATE);
        sysNotifyService.addNotify(sysNotifyAddDTO);

        if (defaultProp.getMailNotifyFlag() == null || !defaultProp.getMailNotifyFlag()) {
            log.warn("doNotify >>>>> 不启用邮件通知！！！");
            return;
        }

        // 5、发送下载完成邮件
        List<String> emails = aniUserOpusService.findFollowsEmail(opus.getId());
        String emailHtml = String.format(
                "    <div>" +
                "        <p>中文名：" + opus.getNameCn() + "</p>" +
                "        <p>原名：" + opus.getNameOriginal() + "</p>" +
                "        <p>资源名称：" + targetFile.getName() + "</p>" +
                "        <a href=\"%s/#/anime/video/" + opus.getId() + "/1/1\">点我前往ElysianAnime追番~~~</a>\n" +
                "    </div>", defaultProp.getWebUrl()
        );
        String emailTitle = "ElysianAnime：你追的番剧更新了~~~【" + opus.getNameCn() + "】";
        MailAccount mailAccount = new MailAccount()
                .setUser(emailSenderProp.getUser())
                .setFrom(emailSenderProp.getFrom())
                .setPass(emailSenderProp.getPass())
                .setHost(emailSenderProp.getHost())
                .setPort(emailSenderProp.getPort())
                .setSslEnable(emailSenderProp.getSslEnable());
        MailUtil.send(mailAccount, emails, emailTitle, emailHtml, true);
    }

    public JSONObject getRssWorkStatus() {
        return Optional
                .of(redisService.get(RedisKeyConst.RSS_WORKS_STATUS))
                .map(JSONUtil::parseObj)
                .orElse(JSONUtil.createObj());
    }

    public void doRenameBtV2() {
        String fileSeparator = SystemUtil.get(SystemUtil.FILE_SEPARATOR);
        JSONArray completedArr = qbApiUtils.info("completed");
        JSONArray seedingArr = qbApiUtils.info("seeding");
        completedArr.fluentAddAll(seedingArr);

        for (Object obj : completedArr) {
            QbInfo info = BeanUtil.toBean(obj, QbInfo.class);
            log.info("doRenameBtV2 >>>>> QbInfo: {}", info.toString());
            try {
                String contentPath = info.getContentPath();
                File file = new File(contentPath);
                if (!file.exists()) {
                    log.warn("doRenameBtV2 >>>>> 文件未找到，删除下载记录，info: {}", info);
                    qbApiUtils.delete(info.getHash());
                    continue;
                }
                // 用qb工具重命名操作（做种的时候qb占用资源没法外部操作）
                String relativePath = StrUtil.replace(info.getContentPath(), info.getSavePath() + fileSeparator, "");
                String msg = qbApiUtils.renameFile(info.getHash(), info.getName(), relativePath);
                log.info("doRenameBtV2 >>>>> call qb renameFile api, msg: {}", msg);
                // 通知
                String opusName = FileUtil.file(info.getSavePath()).getName();
                AniOpus aniOpus = aniOpusService.loadByNameCn(opusName);
                doNotifyCatchEx(aniOpus, file);
            } catch (Exception ex) {
                log.error("doRenameBtV2 >>>>> 重命名失败，作品：{}，errorMsg：{}", info.getName(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * 清除历史下载
     */
    public void clearHistory() {
        if (!defaultProp.getSeedFlag()) {
            JSONArray seeding = qbApiUtils.info("seeding");
            for (Object obj : seeding) {
                QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);
                qbApiUtils.pause(qbInfo.getHash());
            }
        }

        ThreadUtil.sleep(30, TimeUnit.SECONDS);

        // 查找已完成的种子
        JSONArray completed = qbApiUtils.info("completed");
        for (Object obj : completed) {
            QbInfo qbInfo = BeanUtil.toBean(obj, QbInfo.class);
            qbApiUtils.delete(qbInfo.getHash());
        }
    }
}
