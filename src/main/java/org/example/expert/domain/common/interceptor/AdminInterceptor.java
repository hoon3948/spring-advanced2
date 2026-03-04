package org.example.expert.domain.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    )throws Exception {
        UserRole userRole = (UserRole) request.getSession().getAttribute("role");

        if(userRole != UserRole.ADMIN){
            log.warn("관리자 아님. 접근 거부");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근권한이 없습니다.");
            return false;
            //throw new InvalidRequestException("접근권한이 없습니다.");
        }

        log.info("[ADMIN ACCESS] time={}, url={}",
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return true;
    }
}
