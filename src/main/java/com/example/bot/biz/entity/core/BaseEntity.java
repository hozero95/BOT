package com.example.bot.biz.entity.core;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base Entity
 */
@SuppressWarnings({"SpellCheckingInspection"})
@MappedSuperclass // 상속받는 엔티티에 공통 필드를 적용
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 활성화
@Getter
@Setter
public abstract class BaseEntity {
    @Column(name = "addipaddr", updatable = false)
    private String addipaddr; // 생성IP

    @CreatedBy
    @Column(name = "adduser", updatable = false)
    private String adduser; // 생성자

    @CreatedDate
    @Column(name = "adddate", updatable = false)
    private LocalDateTime adddate; // 생성일자

    @Column(name = "updipaddr")
    private String updipaddr; // 수정IP

    @LastModifiedBy
    @Column(name = "upduser")
    private String upduser; // 수정자

    @LastModifiedDate
    @Column(name = "upddate")
    private LocalDateTime upddate; // 수정일자
}
