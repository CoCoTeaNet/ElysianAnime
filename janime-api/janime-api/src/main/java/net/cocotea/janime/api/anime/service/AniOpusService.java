package net.cocotea.janime.api.anime.service;

import net.cocotea.janime.api.anime.model.dto.AniOpusAddDTO;
import net.cocotea.janime.api.anime.model.dto.AniOpusHomeDTO;
import net.cocotea.janime.api.anime.model.dto.AniOpusPageDTO;
import net.cocotea.janime.api.anime.model.dto.AniOpusUpdateDTO;
import net.cocotea.janime.api.anime.model.po.AniOpus;
import net.cocotea.janime.api.anime.model.vo.AniOpusHomeVO;
import net.cocotea.janime.api.anime.model.vo.AniOpusVO;
import net.cocotea.janime.api.anime.model.vo.AniUserOpusVO;
import net.cocotea.janime.api.anime.model.vo.AniVideoVO;
import net.cocotea.janime.common.model.ApiPage;
import net.cocotea.janime.common.model.BusinessException;
import net.cocotea.janime.common.service.BaseService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * 作品
 *
 * @author CoCoTea 572315466@qq.com
 * @since 1.2.1 2023-03-07
 */
public interface AniOpusService extends BaseService<ApiPage<AniOpusVO>, AniOpusPageDTO, AniOpusAddDTO, AniOpusUpdateDTO> {
    boolean update(AniOpus aniOpus);

    AniOpus loadById(BigInteger id) throws BusinessException;

    AniOpusVO loadByName(String nameOriginal) throws BusinessException;

    AniOpus loadByNameCn(String nameCn) throws BusinessException;

    /**
     * 查找作品，并关联用户
     */
    ApiPage<AniOpusHomeVO> listByUser(AniOpusHomeDTO homeDTO);

    /**
     * 获取作品媒信息
     *
     * @param opusId 作品id
     * @return {@link AniVideoVO}
     */
    AniVideoVO getOpusMedia(BigInteger opusId) throws BusinessException;

    /**
     * 获取订阅的番剧
     *
     * @param rssStatus {@link net.cocotea.janime.common.enums.RssStatusEnum}
     * @return {@link AniOpus}
     */
    List<AniOpus> getRssOpus(int rssStatus);

    /**
     * 上传作品资源
     *
     * @param opusId        作品ID
     * @param multipartFile 文件
     * @return 资源浏览路径
     */
    String uploadRes(BigInteger opusId, MultipartFile multipartFile) throws BusinessException;

    /**
     * 响应二进制媒体流
     *
     * @param opusId   作品ID
     * @param resName  资源名称
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     */
    void getMedia(BigInteger opusId, String resName, HttpServletRequest request, HttpServletResponse response) throws BusinessException, IOException, ServletException;

    /**
     * 获取封面
     *
     * @param resName  资源名称
     * @param response {@link HttpServletResponse}
     */
    void getCover(String resName, HttpServletResponse response) throws BusinessException, IOException;
}