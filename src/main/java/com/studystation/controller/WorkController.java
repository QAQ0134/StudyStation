package com.studystation.controller;

import com.studystation.pojo.Result;
import com.studystation.pojo.Work;
import com.studystation.service.WorkService;
import com.studystation.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/work")
public class WorkController {

    @Autowired
    private WorkService workService;

    // 发布作品
    @PostMapping("/publish")
    public Result publish(@RequestHeader("token") String token, @RequestBody Work work) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            workService.publishWork(work, userId);
            return Result.success();
        } catch (Exception e) {
            // 👇 加上这行，把真正的报错打印在控制台 👇
            e.printStackTrace();
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    // 获取推荐列表
    @GetMapping("/recommend")
    public Result recommend() {
        List<Work> works = workService.getRecommendWorks();
        return Result.success(works);
    }

    // 获取作品详情
    @GetMapping("/{id}")
    public Result detail(@PathVariable Integer id) {
        Work work = workService.getWorkDetail(id);
        return Result.success(work);
    }

    // 根据学科查询作品列表
    @GetMapping("/subject/{subjectId}")
    public Result getBySubject(@PathVariable Integer subjectId) {
        return Result.success(workService.getWorksBySubjectId(subjectId));
    }

    // 点赞/取消点赞
    @PostMapping("/{id}/like")
    public Result like(@PathVariable Integer id, @RequestParam boolean isLike, @RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            workService.likeWork(id, userId, isLike);
            return Result.success();
        } catch (Exception e) {
            return Result.error("请先登录");
        }
    }

    // 收藏/取消收藏
    @PostMapping("/{id}/collect")
    public Result collect(@PathVariable Integer id, @RequestParam boolean isCollect, @RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            workService.collectWork(id, userId, isCollect);
            return Result.success();
        } catch (Exception e) {
            return Result.error("请先登录");
        }
    }

    // 查询当前用户是否已点赞、收藏（用于进入页面时的状态回显）
    @GetMapping("/{id}/status")
    public Result getInteractionStatus(@PathVariable Integer id, @RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            return Result.success(workService.getUserInteractionStatus(id, userId));
        } catch (Exception e) {
            // 没登录就返回 false
            Map<String, Boolean> map = new HashMap<>();
            map.put("isLiked", false);
            map.put("isCollected", false);
            return Result.success(map);
        }
    }

    // 获取我的收藏作品列表
    @GetMapping("/myCollect")
    public Result getMyCollect(@RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            return Result.success(workService.getCollectWorksByUserId(userId));
        } catch (Exception e) {
            return Result.error("获取失败，请先登录");
        }
    }

    // 删除文章
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id, @RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            Integer role = claims.get("role", Integer.class); // 获取角色，判断是否管理员

            String errorMsg = workService.deleteWork(id, userId, role);
            if (errorMsg != null) {
                return Result.error(errorMsg);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("操作失败，请先登录");
        }
    }

    // 获取我的作品
    @GetMapping("/myWorks")
    public Result getMyWorks(@RequestHeader("token") String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = claims.get("id", Integer.class);
            return Result.success(workService.getWorksByUserId(userId));
        } catch (Exception e) {
            return Result.error("获取失败，请先登录");
        }
    }

    // 获取作品列表（支持获取自己的或他人的）
    @GetMapping("/userWorks")
    public Result getUserWorks(
            @RequestParam(required = false) Integer userId,
            @RequestHeader(value = "token", required = false) String token) {
        try {
            // 如果没传 userId，就从 token 里解析自己的 id
            if (userId == null) {
                if (token == null || token.isEmpty()) {
                    return Result.error("请先登录");
                }
                Claims claims = JwtUtils.parseToken(token);
                userId = claims.get("id", Integer.class);
            }
            // 这里的 workService.getWorksByUserId 是之前已经写好的
            return Result.success(workService.getWorksByUserId(userId));
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}