package com.example.tinder.log;

import com.example.tinder.config.security.SecurityUtils;
import com.example.tinder.entity.AuditLog;
import com.example.tinder.entity.BaseEntity;
import com.example.tinder.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

@Component("customEntityAuditListener")
public class EntityAuditListener {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    public EntityAuditListener(@Lazy AuditLogRepository auditLogRepository, SecurityUtils securityUtils, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.securityUtils = securityUtils;
        this.objectMapper = objectMapper;
    }

    @PostPersist
    public void postPersist(Object entity) {
        saveAuditLog(entity, "CREATED");
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        saveAuditLog(entity, "UPDATED");
    }

    @PreRemove
    public void preRemove(Object entity) {
        saveAuditLog(entity, "DELETED");
    }

    private void saveAuditLog(Object entity, String action) {
        AuditLog log = new AuditLog();

        Optional<SecurityUtils.CurrentUser> user = securityUtils.getCurrentUserForLog();

        if (user.isPresent()) {
            SecurityUtils.CurrentUser currentUser = user.get();
            log.setUsername(currentUser.username());
            log.setUserId(currentUser.id());
            log.setRoles(currentUser.role());
        }

        log.setIpAddress(securityUtils.getClientIp());
        log.setEndpoint(securityUtils.getRequestUri());
        log.setSourceType(securityUtils.getUserAgent());
        log.setAction(action);
        log.setEntityId(getEntityId(entity));
        log.setEntityName(entity.getClass().getSimpleName());
        log.setDescription(action + " action on " + entity.getClass().getSimpleName());
        log.setTime(LocalDateTime.now());

        // Entity snapshot
        try {
            log.setEntitySnapshot(objectMapper.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            log.setEntitySnapshot("{}");
        }

        auditLogRepository.save(log);
    }

    private Long getEntityId(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            return baseEntity.getId();
        }
        return null;
    }
}
