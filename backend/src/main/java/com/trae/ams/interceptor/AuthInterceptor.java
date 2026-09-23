package com.trae.ams.interceptor;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trae.ams.common.util.JwtUtil;
import com.trae.ams.common.context.UserContext;
import com.trae.ams.entity.SysUser;
import com.trae.ams.mapper.SysUserMapper;
import com.trae.ams.mapper.SysUserRoleMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        
        // Debug log
        System.out.println("AuthInterceptor: " + uri);
        
        // Get Token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token) || !token.startsWith("Bearer ")) {
            return sendError(response, 401, "未登录或登录已失效");
        }
        
        token = token.substring(7);
        
        if (!jwtUtil.verify(token)) {
             return sendError(response, 401, "未登录或登录已失效");
        }
        
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            return sendError(response, 401, "未登录或登录已失效");
        }

        // Set UserContext
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            UserContext.setUserId(userId);
            UserContext.setStoreId(user.getStoreId());
        }
        
        // Fetch roles and set isAdmin
        List<String> roles = sysUserRoleMapper.selectRoleCodesByUserId(userId);
        // Admin, Manager, and Technician are considered staff with elevated privileges
        boolean isAdmin = roles != null && (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MANAGER") || roles.contains("ROLE_TECH"));
        UserContext.setIsAdmin(isAdmin);
        
        boolean isSuperAdmin = roles != null && roles.contains("ROLE_ADMIN");
        UserContext.setIsSuperAdmin(isSuperAdmin);
        
        // RBAC check for /admin/
        // Use AntPathMatcher for robust path matching
        // Matches /api/admin/**, /admin/**, etc.
        if (pathMatcher.match("/**/admin/**", uri)) {
            if (!isAdmin) {
                 return sendError(response, 403, "无权访问");
            }
        }

        // Pass userId to controller if needed (via request attribute)
        request.setAttribute("userId", userId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
    
    private boolean sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code == 401 ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}
