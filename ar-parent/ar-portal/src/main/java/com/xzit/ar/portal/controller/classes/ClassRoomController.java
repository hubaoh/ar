/**
 * @Title: ClassRoomController.java
 * @Package com.xzit.ar.portal.controller.classes
 * @Description: TODO
 * @author Mr.Black
 * @date 2016年2月20日 下午3:05:30
 * @version V1.0
 */
package com.xzit.ar.portal.controller.classes;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import com.xzit.ar.common.po.album.Album;
import com.xzit.ar.portal.service.image.AlbumService;
import com.xzit.ar.portal.service.information.CommentService;
import com.xzit.ar.portal.service.information.InformationService;
import com.xzit.ar.portal.service.my.TaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.xzit.ar.common.base.BaseController;
import com.xzit.ar.common.constant.PathConstant;
import com.xzit.ar.common.exception.ServiceException;
import com.xzit.ar.common.init.context.ARContext;
import com.xzit.ar.common.page.Page;
import com.xzit.ar.common.po.info.Comment;
import com.xzit.ar.common.po.info.Information;
import com.xzit.ar.common.po.user.UserOrigin;
import com.xzit.ar.common.util.CommonUtil;
import com.xzit.ar.portal.service.classes.ClassRoomService;

/**
 * @author Mr.Black
 * @ClassName: ClassRoomController
 * @Description: TODO 班级空间
 * @date 2016年2月20日 下午3:05:30
 */
@Controller
@RequestMapping("/classroom")
public class ClassRoomController extends BaseController {

    @Resource
    private ClassRoomService classRoomService;

    @Resource
    private CommentService commentService;

    @Resource
    private InformationService informationService;

    @Resource
    private TaService taService;

    @Resource
    private AlbumService albumService;

    /**
     * TODO 加载班级主页
     *
     * @param model
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model, @RequestParam("classId") Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 加载班级最新消息和最近加入的成员
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), 3);
        model.addAttribute("lastInfos", classRoomService.classInfo(page, classId).getBeanList());
        model.addAttribute("memberList", classRoomService.getAllMemberIds(classId));

        return "class/classroom/classroom-index";
    }

    /**
     * TODO 加载班级通讯录
     *
     * @param model
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/directory")
    public String directory(Model model, @RequestParam("classId") Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 通讯录列表
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), getPageSize());
        model.addAttribute("page", classRoomService.classDirector(page, classId));
        model.addAttribute("letters", ARContext.lowerLetters);

        return "class/classroom/classroom-directory";
    }

    /**
     * TODO 加载班级成员列表
     *
     * @param model
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/member")
    public String member(Model model, @RequestParam("classId") Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 加载成员列表
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), getPageSize());
        model.addAttribute("page", classRoomService.classMember(page, classId));
        return "class/classroom/classroom-member";
    }

    /**
     * TODO 加载班级动态消息
     *
     * @param model
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/info")
    public String info(Model model, @RequestParam("classId") Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 加载信息列表
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), 8);
        model.addAttribute("page", classRoomService.classInfo(page, classId));

        return "class/classroom/classroom-info";
    }

    /**
     * TODO 加载班级动态消息详情
     *
     * @param model
     * @param classId
     * @param infoId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/infoDetail")
    public String infoDetail(Model model, @RequestParam("classId") Integer classId,
                             @RequestParam("infoId") Integer infoId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 班级动态消息详情
        Map<String, Object> info = classRoomService.classInfoDetail(classId, infoId);
        model.addAttribute("info", info);

        return "class/classroom/classroom-info-detail";
    }

    /**
     * TODO 动态加载评论
     *
     * @param model
     * @param infoId
     * @return
     */
    @RequestMapping("/infoCommentList")
    public String infoCommentList(Model model, @RequestParam("infoId") Integer infoId) throws ServiceException {
        // 构造 page 对象
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), getPageSize());
        // 加载列表
        commentService.dynamicLoadComment(page, infoId);
        model.addAttribute("page", page);

        return "class/classroom/classroom-info-comments";
    }

    /**
     * TODO 加载消息详情页面的侧边栏
     *
     * @param model
     * @param authorId
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/infoSide")
    public String infoSide(Model model, @RequestParam("authorId") Integer authorId, @RequestParam("classId") Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 查询班级内最新消息
        Page<Map<String, Object>> page1 = new Page<>(1, 4);
        model.addAttribute("classOtherInfos", informationService.getOriginInfos(page1, classId, "CI"));
        // 用户基本信息
        model.addAttribute("author", taService.getUserBasicInfo(authorId));
        // 查询用户最近消息
        Page<Map<String, Object>> page2 = new Page<>(1, 4);
        model.addAttribute("authorOtherInfos", informationService.getOriginUserInfos(page2, authorId, classId, "CI"));

        return "class/classroom/classroom-info-side";
    }

    //	@RequestMapping(value = "/commentInfo", method = RequestMethod.POST)
//	public @ResponseBody Comment commentInfo(@RequestParam("infoId") Integer infoId,
//			@RequestParam("content") String content) throws ServiceException {
//		Comment comment = new Comment();
//		comment.setContent(content);
//		comment.setCreateTime(new Date());
//		comment.setInfoId(infoId);
//		comment.setUserId(getCurrentUserId());
//		if (classRoomService.addClassInfoComment(comment) > 0) {
//			setMessage("评论成功");
//		}
//		return comment;
//	}
    @RequestMapping("/commentInfo")
    public String commentInfo(RedirectAttributes redirectAttributes, Comment comment, Integer classId) throws ServiceException {
        // 设置参数
        comment.setUserId(getCurrentUserId());
        comment.setCreateTime(new Date());
        // 存储
        commentService.saveComment(comment);
        // 重定向
        if (comment != null && CommonUtil.isNotEmpty(comment.getInfoId())) {
            redirectAttributes.addAttribute("infoId", comment.getInfoId());
            redirectAttributes.addAttribute("classId", classId);
            return "redirect:/classroom/infoDetail.action";
        } else {
            return "redirect:/class.action";
        }
    }

    /**
     * TODO 发布班级动态消息
     *
     * @throws ServiceException
     * @Title: publishClassInfo
     * @Description: TODO 发布班级动态
     */
    @RequestMapping(value = "/publishClassInfo", method = RequestMethod.POST)
    public String publishClassInfo(Model model, RedirectAttributes attr, @RequestParam("infoTitle") String infoTitle,
                                   @RequestParam("content") String content, MultipartFile infoImage, @RequestParam("classId") Integer classId) throws ServiceException {
        Information information = new Information();
        // 参数校验
        if (CommonUtil.isNotEmpty(content) && CommonUtil.isNotEmpty(infoTitle)) {
            information.setInfoTitle(infoTitle);
            information.setContent(content);
            information.setInfoType("CI");
            information.setIsTop("0");
            information.setUserId(getCurrentUserId());
            information.setOriginId(classId);
            information.setComments(0);
            information.setViews(0);
            information.setLoves(0);
            information.setState("A");
            information.setCreateTime(new Date());
            information.setStateTime(new Date());
            // 图片处理
            if (infoImage != null) {
                // 原始名称
                String originalFilename = infoImage.getOriginalFilename();
                // 上传图片
                if (originalFilename != null && originalFilename.length() > 0) {
                    // 存储图片的物理路径
                    String pic_path = PathConstant.infoPicAbsPath;
                    // 新的图片名称
                    String newFileName = UUID.randomUUID()
                            + originalFilename.substring(originalFilename.lastIndexOf("."));
                    // 新图片
                    File newFile = new File(pic_path + newFileName);
                    // 将内存中的数据写入磁盘
                    try {
                        infoImage.transferTo(newFile);
                    } catch (Exception e) {
                        throw new ServiceException("发布动态时出现异常");
                    }
                    // 将新图片名称写到 中
                    information.setThumbImage(PathConstant.infoPicRelPath + newFileName);
                    System.out.println(information.getThumbImage());
                }
            }
        } else {
            setMessage(model, "说两句吧");
        }
        classRoomService.addClassInfo(information);
        // 设置重定向参数
        attr.addAttribute("classId", classId);
        // 重定向
        return "redirect:/classroom/info.action";
    }

    /**
     * TODO 申请加入班级
     *
     * @Title: joinClass
     * @Description: TODO 加入班级
     */
    @RequestMapping("/joinClass")
    public String joinClass(Model model, RedirectAttributes attr, @RequestParam("classId") Integer classId)
            throws ServiceException {
        // 判断是否已存在
        if (!classRoomService.isMemberInClass(getCurrentUserId(), classId)) {
            // 构建加入记录
            UserOrigin userOrigin = new UserOrigin();
            userOrigin.setCreateTime(new Date());
            userOrigin.setOriginId(classId);
            userOrigin.setState("A");
            userOrigin.setStateTime(new Date());
            userOrigin.setUserId(getCurrentUserId());
            // 插入记录
            if (classRoomService.addToClass(userOrigin) > 0) {
                setMessage(attr, "加入成功");
                return "redirect:/classroom.action?classId=" + classId;
            } else {
                setMessage("加入失败");
            }
        } else {
            setMessage("您已经加入该班级");
        }
        return "forward:/classroom.action?classId=" + classId;
    }

    /**
     * TODO 加载班级留言页面
     *
     * @Title: message
     * @Description: TODO 加载班级留言
     */
    @RequestMapping("/message")
    public String message(Model model, @RequestParam("classId") Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 加载留言列表
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), getPageSize());
        model.addAttribute("page", classRoomService.classMessage(page, classId));

        return "class/classroom/classroom-message";
    }

    /**
     * TODO 发布班级留言
     *
     * @Title: publishClassMessage
     * @Description: TODO 发布班级留言
     */
    @RequestMapping("/publishClassMessage")
    public String publishClassMessage(Model model, RedirectAttributes attr, @RequestParam("content") String content,
                                      @RequestParam("classId") Integer classId) throws ServiceException {
        Information information = new Information();
        // 参数校验
        if (CommonUtil.isNotEmpty(content)) {
            information.setContent(content);
            information.setInfoType("CM");
            information.setIsTop("0");
            information.setUserId(getCurrentUserId());
            information.setOriginId(classId);
            information.setComments(0);
            information.setViews(0);
            information.setLoves(0);
            information.setState("A");
            information.setCreateTime(new Date());
            information.setStateTime(new Date());
        }
        classRoomService.addClassInfo(information);
        // 设置重定向参数
        attr.addAttribute("classId", classId);
        // 重定向
        return "redirect:/classroom/message.action";
    }

    /**
     * TODO 加载班级相册
     *
     * @param model
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/album")
    public String album(Model model, Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 加载相册
        Page<Album> page = new Page<>(getPageIndex(), 12);
        albumService.getAlbums(page, classId);
        // 传递数据
        model.addAttribute("page", page);

        return "class/classroom/classroom-album";
    }

    /**
     * TODO 加载创建相册界面
     *
     * @param model
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/album/add")
    public String addAlbum(Model model, Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);

        return "class/classroom/classroom-album-add";
    }

    /**
     * TODO 保存相册
     *
     * @param attributes
     * @param classId
     * @param albumName
     * @param albumDesc
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/album/save", method = RequestMethod.POST)
    public String saveAlbum(RedirectAttributes attributes, Integer classId, String albumName, String albumDesc) throws ServiceException {

        // 参数校验
        if (CommonUtil.isNotEmpty(classId) && CommonUtil.isNotEmpty(albumName) && CommonUtil.isNotEmpty(albumDesc)) {
            // 创建相册对象
            Album album = new Album();
            album.setAlbumName(albumName);
            album.setAlbumDesc(albumDesc);
            album.setInterests(0);
            album.setOriginId(classId);
            album.setUserId(getCurrentUserId());
            album.setCreateTime(new Date());
            album.setStateTime(new Date());
            album.setState("A");
            album.setCoverImage(PathConstant.albumCoverDefaultRelPath);

            // 存储相册
            attributes.addAttribute("classId", classId);
            // 插入相册后返回ID
            attributes.addAttribute("albumId", albumService.saveAlbum(album));

            return "redirect:/classroom/album.action";
        }
        return "redirect:/class.action";
    }

    /**
     * TODO 加载相册编辑界面
     *
     * @param model
     * @param albumId
     * @param classId
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/album/edit")
    public String editAlbum(Model model, Integer albumId, Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 查询相册信息
        model.addAttribute("album", albumService.getAlbumById(albumId));

        return "class/classroom/classroom-album-edit";
    }

    /**
     * TODO 更新相册信息
     *
     * @param attributes
     * @param classId
     * @param album
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/album/update", method = RequestMethod.POST)
    public String updateAlbum(RedirectAttributes attributes, Integer classId, Album album) throws ServiceException {
        // 参数校验
        if (album != null && CommonUtil.isNotEmpty(classId) && CommonUtil.isNotEmpty(album.getAlbumId())) {
            album.setStateTime(new Date());
            // 数据存储
            albumService.updateAlbum(album);
            attributes.addAttribute("classId", classId);
            return "redirect:/classroom/album.action";
        }
        return "redirect:/class.action";
    }

    /**
     * TODO 加载相册照片流
     *
     * @param model
     * @param albumId
     * @param classId
     * @return
     */
    @RequestMapping("/album/image")
    public String image(Model model, Integer albumId, Integer classId) throws ServiceException {
        // 班级基本信息
        Map<String, Object> classroom = classRoomService.classIndex(classId);
        if (classroom == null || CommonUtil.isEmpty(classroom.get("classId").toString())) {
            return "redirect:/class.action";
        }
        model.addAttribute("classroom", classroom);
        // 加载照片流
        Page<Map<String, Object>> page = new Page<>(getPageIndex(), getPageSize());
        albumService.getAlbumImage(page, albumId);
        model.addAttribute("page", page);
        // 相册信息
        model.addAttribute("album", albumService.getAlbumById(albumId));

        return "class/classroom/classroom-album-image";
    }

    /**
     * TODO 删除相册
     * @param attributes
     * @param classId
     * @param albumId
     * @return jsp
     * @throws ServiceException
     */
    @RequestMapping(value = "/album/delete", method = RequestMethod.POST)
    public String deleteAlbum(RedirectAttributes attributes, Integer classId, Integer albumId) throws ServiceException {
        // 参数校验
        if (CommonUtil.isNotEmpty(albumId) && CommonUtil.isNotEmpty(classId)) {
            // 删除相册
            albumService.deleteAlbum(albumId);
        }
        // 跳转页面
        attributes.addAttribute("classId", classId);

        return "redirect:/classroom/album.action";
    }

    /**
     * TODO 编辑班级简介
     *
     * @Title: description
     * @Description: TODO 编辑班级简介
     */
    @RequestMapping("/description")
    public String description(Model model, @RequestParam("classId") Integer classId) throws ServiceException {
        model.addAttribute("classroom", classRoomService.classIndex(classId));
        return "class/classroom/classroom-description";
    }

    /**
     * TODO 提交班级信息编辑
     *
     * @param model
     * @param attr
     * @param classId
     * @param description
     * @return
     * @throws ServiceException
     */
    @RequestMapping(value = "/setDescription", method = RequestMethod.POST)
    public String setDescription(Model model, RedirectAttributes attr, @RequestParam("classId") Integer classId,
                                 @RequestParam("description") String description) throws ServiceException {
        classRoomService.updateClassDesc(description, classId);
        // 设置重定向参数
        attr.addAttribute("classId", classId);
        // 重定向
        return "redirect:/classroom.action";
    }
}
