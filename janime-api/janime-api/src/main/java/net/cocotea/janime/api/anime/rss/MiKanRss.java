package net.cocotea.janime.api.anime.rss;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson.JSONArray;
import com.dtflys.forest.Forest;
import net.cocotea.janime.api.anime.model.dto.AniRssDTO;
import net.cocotea.janime.api.anime.model.po.AniOpus;
import net.cocotea.janime.api.anime.rss.model.MkXmlDetail;
import net.cocotea.janime.api.system.model.dto.SysNotifyAddDTO;
import net.cocotea.janime.api.system.service.SysNotifyService;
import net.cocotea.janime.api.anime.service.AniOpusService;
import net.cocotea.janime.api.anime.service.AniUserOpusService;
import net.cocotea.janime.common.constant.CharConst;
import net.cocotea.janime.common.constant.NotifyConst;
import net.cocotea.janime.common.enums.IsEnum;
import net.cocotea.janime.common.enums.LevelEnum;
import net.cocotea.janime.common.enums.RssStatusEnum;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.properties.DefaultProp;
import net.cocotea.janime.util.QbApiUtils;
import net.cocotea.janime.util.ResUtils;
import net.cocotea.janime.common.util.StrcUtis;
import net.cocotea.janime.api.anime.rss.model.MkXmlItem;
import net.cocotea.janime.api.anime.rss.model.QbInfo;
import net.cocotea.janime.util.RuleUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 蜜柑rss订阅服务
 *
 * @author CoCoTea
 * @since v1
 */
@Component
public class MiKanRss {
    private static final Logger logger = LoggerFactory.getLogger(MiKanRss.class);

    @Inject
    private DefaultProp defaultProp;

    @Inject
    private QbApiUtils qbApiUtils;

    @Inject
    private AniOpusService aniOpusService;

    @Inject
    private ResUtils resUtils;

    @Inject
    private SysNotifyService sysNotifyService;

    @Inject
    private AniUserOpusService aniUserOpusService;

    /**
     * 保存并订阅rss
     */
    public void requestRss(String rssUrl, AniOpus opus) {
        String result = Forest.get(rssUrl).executeAsString();
        logger.debug("订阅地址返回结果，result：{}", result);
        List<MkXmlItem> mkXmlItemList = doParseXmlItems(result);
        for (MkXmlItem mkXmlItem : mkXmlItemList) {
            String title = mkXmlItem.getTitle();
            // 是标识的资源
            boolean remarkRes = RuleUtils.isRemarkRes(title, opus.getRssOnlyMark());
            if (!remarkRes) {
                logger.warn("解析完成-------->不是标识资源，不进行下载, title={}, mark={}", title, opus.getRssOnlyMark());
                continue;
            }
            if (!resUtils.existDir(opus.getNameCn())) {
                logger.warn("解析完成-------->目录不存在,opus={}", opus.getNameCn());
                continue;
            }
            // 排除的资源
            if (StrUtil.isNotBlank(opus.getRssExcludeRes())) {
                boolean excludeRes = RuleUtils.isExcludeRes(title, opus.getRssExcludeRes());
                if (excludeRes) {
                    logger.warn("解析结果-------->标识为排除的资源，不进行下载, title={}, mark={}", title, opus.getRssExcludeRes());
                    continue;
                }
            }
            // 资源是否存在
            String rename = RuleUtils.rename(title, opus.getRssLevelIndex(), opus.getRssFileType());
            boolean existRes = resUtils.exist(opus.getNameCn(), rename);
            if (!existRes) {
                // 资源不存在
                logger.info("校验结果-------->资源不存在，作品名称为：{}，开始请求qbittorrent下载资源...", opus.getNameCn());
                String dir = resUtils.findASS(2) + opus.getNameCn() + CharConst.LEFT_LINE + rename;
                logger.debug("dir={}", dir);
                String added = qbApiUtils.addNewTorrent(mkXmlItem.getEnclosureUrl(), dir);
                logger.info("{}下载请求完成，响应消息：{}", opus.getNameCn(), added);
            } else {
                logger.warn("校验结果-------->《{}》资源已存在,rename={},title={}", opus.getNameCn(), rename, title);
            }
        }
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
     * 重命名bt种子下载完成的名称
     */
    public void doRenameBt() throws BusinessException {
        String baseMsg = "rss[doRenameBt]";
        // 查找已完成的种子
        JSONArray ls = qbApiUtils.info("completed");
        // 循环命名
        for (Object o : ls) {
            QbInfo info = Convert.convert(QbInfo.class, o);
            try {
                logger.debug(baseMsg.concat("o={}"), o);
                logger.info(baseMsg.concat("info={}"), info);
                String contentPath = info.getContentPath();
                File file = new File(contentPath);
                if (!file.exists()) {
                    logger.warn("{}文件未找到,info={}", baseMsg, info);
                }
                // 父级文件对象
                File parentFile = file.getParentFile();
                AniOpus opus = aniOpusService.loadByNameCn(parentFile.getParentFile().getName());
                if (opus == null) {
                    logger.warn(baseMsg.concat("找不到作品, 路径={}"), parentFile.getParent());
                    continue;
                }
                if (opus.getRssStatus() != RssStatusEnum.SUBSCRIBING.getCode().intValue()) {
                    logger.warn("《{}》作品不处于订阅状态", opus.getNameCn());
                    continue;
                }
                if (opus.getRssLevelIndex() == null) {
                    logger.warn(baseMsg.concat("没有配置重命名规则: rssLevelIndex，opus={}"), opus);
                    continue;
                }
                // 作品资源根目录
                String path = parentFile.getParentFile().getPath();
                // 目标移动路径
                String targetPath = path + File.separator + file.getName();
                File targetFile = FileUtil.file(targetPath);
                // 1、将文件移动到父级目录
                FileUtil.move(file, targetFile, true);
                // 2、删除父级目录
                FileUtil.del(parentFile);
                // 3、改名资源
                FileUtil.rename(targetFile, parentFile.getPath(), true);
                // 4、删除下载记录
                qbApiUtils.delete(info.getHash());
                // 系统通知
                doNotify(opus, targetFile);
            } catch (Exception ex) {
                logger.error(baseMsg.concat("重命名失败，作品：{}，errorMsg：{}"), info.getName(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * 暂停正在做种的
     */
    public void doPauseSeedingBt() {
        String baseMsg = "rss[doPauseSeedingBt]";
        // 查找已完成的种子
        JSONArray ls = qbApiUtils.info("seeding");
        // 循环暂停
        List<String> hashes = new ArrayList<>(ls.size());
        for (Object o : ls) {
            QbInfo info = Convert.convert(QbInfo.class, o);
            hashes.add(info.getHash());
        }
        if (!hashes.isEmpty()) {
            String hash = StrcUtis.addChar(hashes, CharConst.VERTICAL);
            String pause = qbApiUtils.pause(hash);
            logger.info(baseMsg.concat("body={}"), pause);
        } else {
            logger.info(baseMsg.concat("没有做种信息"));
        }
    }

    /**
     * 获取订阅番剧并下载
     */
    public void doFindRssOpusAndDownload() throws InterruptedException {
        List<AniOpus> aniOpusList = aniOpusService.getRssOpus(RssStatusEnum.SUBSCRIBING.getCode());
        logger.info("doFindRssOpusAndDownload------->start...opusSize={}", aniOpusList.size());
        for (AniOpus opus : aniOpusList) {
            logger.info("{}-------->开始下载", opus.getNameCn());
            String rssUrl = opus.getRssUrl();
            if (StrUtil.isNotBlank(rssUrl)) {
                try {
                    logger.info("存在订阅地址-------->rssUrl: {}", rssUrl);
                    requestRss(rssUrl, opus);
                } catch (Exception e) {
                    logger.error("下载出现异常，作品是:{}, 堆栈信息:{}", opus.getNameCn(), e.getMessage());
                } finally {
                    Thread.sleep(3000L);
                }
            } else {
                logger.warn("没有订阅地址, opus={}", opus);
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
        logger.info(baseMsg.concat("param={}"), aniRssDTO);
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
        logger.info(baseMsg.concat("path={}"), path);
        FileUtil.mkdir(path);
        // 重新刷新一下rss订阅
        if (StrUtil.isNotBlank(opus.getRssUrl())) {
            new Thread(() -> requestRss(opus.getRssUrl(), opus)).start();
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
                title = title.replaceAll(episodeStr, "<span style=\"color:yellow\">" + episodeStr + "</span>");
            }
            item.setTitleHtml(title);
        }
        return new MkXmlDetail()
                .setItemList(mkXmlItems)
                .setTitleFragmentList(titleFragmentList)
                .setEpisodeIndexList(episodeIndexList);
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
        MailUtil.send(emails, emailTitle, emailHtml, true);
    }

}
