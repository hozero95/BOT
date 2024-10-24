package com.example.bot.core.security.util;

import com.example.bot.biz.entity.Auth;
import com.example.bot.biz.entity.User;
import com.example.bot.biz.repository.AuthRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Security Custom UserDetails
 */
@SuppressWarnings("SpellCheckingInspection")
public class CustomUserDetails implements UserDetails {
    private final User user;
    private final AuthRepository authRepository;

    public CustomUserDetails(User user, AuthRepository authRepository) {
        this.user = user;
        this.authRepository = authRepository;
    }

    /**
     * 사용자에게 부여된 권한을 GrantedAuthority 객체의 컬렉션으로 반환
     *
     * @return Collection<GrantedAuthority>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Auth auth = authRepository.findByUsercd(user.getUsercd());

        authorities.add((GrantedAuthority) auth::getAuth);

        return authorities;
    }

    /**
     * 사용자의 암호화된 비밀번호를 반환
     *
     * @return String
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자의 이름을 반환 => 이 프로젝트에서는 usercd
     *
     * @return String
     */
    @Override
    public String getUsername() {
        return user.getUsercd();
    }

    /**
     * 사용자 계정이 만료되지 않았는지 여부를 반환 (default: true)
     *
     * @return boolean
     */
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    /**
     * 사용자 계정이 잠기지 않았는지 여부를 반환 (default: true)
     *
     * @return boolean
     */
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    /**
     * 사용자의 비밀번호가 만료되지 않았는지 여부를 반환 (default: true)
     *
     * @return boolean
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    /**
     * 사용자 계정이 활성화 되었는지 여부를 반환 (default: true)
     *
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
