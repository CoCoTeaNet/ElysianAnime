package net.cocotea.elysiananime.common.enums;

/**
 * 作品类型：1动画 2漫画 3游戏 4音乐 5电影 6电视剧
 * @author cocotea
 * @since 2023-3-12 01:41:51
 */
public enum AcgOpusTypeEnum {
    ANIMATE(1, "动画"),
    COMIC(2, "漫画"),
    GAME(3, "游戏"),
    MUSIC(4, "音乐"),
    MOVIE(5, "电影"),
    TV_PLAY(6, "电视剧");

    final Integer code;
    final String desc;

    AcgOpusTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
