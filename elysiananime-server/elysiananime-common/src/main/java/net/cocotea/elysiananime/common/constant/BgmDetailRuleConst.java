package net.cocotea.elysiananime.common.constant;

/**
 * banggumi番剧详细字段抓取规则
 *
 * @author CoCoTea
 * @since v1
 */
@Deprecated
public class BgmDetailRuleConst {

    /**
     * 封面
     */
    public static final String COVER = "//*[@id=\"bangumiInfo\"]/div/div/a/img/@src";

    /**
     * 原始名称
     */
    public static final String NAME_ORIGINAL = "//*[@id=\"headerSubject\"]/h1/a/text()";

    /**
     * 中文名
     */
    public static final String NAME_CN = "//*[@id=\"infobox\"]/li/span[contains(text(), '中文名:')]/following-sibling::text()";

    /**
     * 话数
     */
    public static final String EPISODES = "//*[@id=\"infobox\"]/li/span[contains(text(), '话数:')]/following-sibling::text()";

    /**
     * 放送开始
     */
    public static final String LAUNCH_START = "//*[@id=\"infobox\"]/li/span[contains(text(), '放送开始:')]/following-sibling::text()";

    /**
     * 放送星期
     */
    public static final String DELIVERY_WEEK = "//*[@id=\"infobox\"]/li/span[contains(text(), '放送星期:')]/following-sibling::text()";

    /**
     * 简介
     */
    public static final String ACG_SUMMARY = "//*[@id=\"subject_summary\"]/text()";

    /**
     * 标签
     */
    public static final String ACG_TAGS = "//*[@id=\"subject_detail\"]/div[3]/div/a";

    /**
     * 标签（单个），必须先用 {@value ACG_TAGS} 抓出列表再使用
     */
    public static final String ACG_TAG = "//a/span/text()";

    /**
     * 无元素包裹的内容
     */
    public static final String COMMON_TEXT = "//text()";

    // public static void main(String[] args) {
    //     String url ="https://bgm.tv/subject/389772";
    //     // String html = HttpUtil.get(url);
    //
    //     FileReader reader = new FileReader("C:\\Users\\57231\\Downloads\\1.html");
    //     JXDocument document = JXDocument.create(reader.readString());
    //
    //     System.out.println(document.selNOne(NAME_CN).asString());
    //     System.out.println(document.selNOne(EPISODES).asString());
    //     System.out.println(document.selNOne(LAUNCH_START).asString());
    //     System.out.println(document.selNOne(DELIVERY_WEEK).asString());
    //     System.out.println(document.selNOne(NAME_ORIGINAL).asString());
    //     // List<JXNode> nodes = document.selN(ACG_SUMMARY);
    //     // for (JXNode node : nodes) {
    //     //     System.out.println(node.selOne(COMMON_TEXT).asString());
    //     // }
    //     // nodes = document.selN(ACG_TAGS);
    //     // for (JXNode node : nodes) {
    //     //     JXNode one = node.selOne(ACG_TAG);
    //     //     if (one != null) {
    //     //         System.out.println(one.asString());
    //     //     }
    //     // }
    //     String[] split = url.split(CharConstant.LEFT_LINE);
    //     String detailUrl = CharConstant.LEFT_LINE + split[split.length - 2] + CharConstant.LEFT_LINE + split[split.length - 1];
    //     System.out.println(detailUrl);
    // }

}
