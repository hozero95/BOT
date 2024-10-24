package com.example.bot.biz.service;

import com.example.bot.biz.dto.JoinDTO;
import com.example.bot.biz.entity.Auth;
import com.example.bot.biz.entity.User;
import com.example.bot.biz.repository.AuthRepository;
import com.example.bot.biz.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("SpellCheckingInspection")
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final HttpServletRequest request;
    private final AuthRepository authRepository;

    @Value("${spring.init.password}")
    private String password;

    public AdminService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, HttpServletRequest request, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.request = request;
        this.authRepository = authRepository;
    }

    /**
     * 회원 가입
     *
     * @param joinDTO p1
     */
    public void signup(JoinDTO joinDTO) {
        String usernm = joinDTO.getUsernm();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime birthdate = LocalDateTime.parse(joinDTO.getBirthdate() + " 00:00:00", formatter);

        // usercd 생성
        String year = Integer.toString(LocalDate.now().getYear());
        StringBuilder usercd;
        int userIndex = 1;
        while (true) {
            usercd = new StringBuilder("ER" + year);
            if (userIndex < 10) {
                usercd.append("000");
            } else if (userIndex < 100) {
                usercd.append("00");
            } else if (userIndex < 1000) {
                usercd.append("0");
            }
            usercd.append(userIndex);

            Boolean isExist = userRepository.existsByUsercd(usercd.toString());
            if (!isExist) {
                break;
            }
            userIndex++;
        }

        String ip = request.getRemoteAddr();

        User user = new User();
        user.setUsercd(usercd.toString());
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setUsernm(usernm);
        user.setBirthdate(birthdate);
        user.setJoindate(LocalDateTime.now());
        user.setAddipaddr(ip);
        user.setUpdipaddr(ip);

        userRepository.save(user);

        Auth auth = new Auth();
        auth.setUsercd(user.getUsercd());
        auth.setAuth("ROLE_USER");
        auth.setAddipaddr(ip);
        auth.setUpdipaddr(ip);

        authRepository.save(auth);
    }
}
